//
// Created by Antonio Ken Iannillo on 13/03/17.
//

#include <stdio.h>
#include <string.h>
#include <unistd.h>

#include <android/log.h>
#include <frida-gum.h>
#include <sys/prctl.h>
#include <sys/socket.h>
#include <sys/un.h>
#include <stdlib.h>
#include <errno.h>

#include "../jsmn.h"
#include "fakeeventsink.h"


#define LOG_TAG "NativeStalkerServer"
#define  LOGF(...)  __android_log_print(ANDROID_LOG_FATAL,LOG_TAG,__VA_ARGS__)
#define  LOGE(...)  __android_log_print(ANDROID_LOG_ERROR,LOG_TAG,__VA_ARGS__)
#define  LOGW(...)  __android_log_print(ANDROID_LOG_WARN,LOG_TAG,__VA_ARGS__)
#define  LOGI(...)  __android_log_print(ANDROID_LOG_INFO,LOG_TAG,__VA_ARGS__)
#define  LOGD(...)  __android_log_print(ANDROID_LOG_DEBUG,LOG_TAG,__VA_ARGS__)

// DEFINE
#define MAX_RECEIVE_BUFFER_SIZE 2048


// FUNCTIONT PROTOTYPES
void check_and_enlarge_stack_size();

void server_loop(int client);
void free_tri(gpointer data);
void collapse_tri (gpointer key, gpointer value, gpointer user_data);
void collapse_counters (gpointer key, gpointer value, gpointer user_data);
void key_value_to_json(gpointer key, gpointer value, gpointer user_data);

gint follow_threads_by_lists(GPtrArray * white_list, GPtrArray * black_list);
gboolean addThread (const GumThreadDetails * details, gpointer data);
gchar * read_task_comm(guint tid);
static void chizpurfle_transformer (GumStalkerIterator * iterator,GumStalkerWriter * output,
                                    gpointer user_data);
static void process_block_address (GumCpuContext * cpu_context, gpointer user_data);

gint start_tracing(void);
void clean_tri (gpointer key, gpointer value, gpointer user_data);
gint stop_tracing(void);

ssize_t receive_from_socket (int sd, char * buffer, size_t bufferSize);
ssize_t send_to_socket (int sd, const char * buffer, size_t bufferSize);

int jsoneq(const char *json, jsmntok_t *tok, const char *s);

// DEFINITIONS
enum method_id{
    FOLLOW_THREADS = 0,
    START_TRACING,
    STOP_TRACING
};

// our stalker 8-)
GumStalker * stalker;

// enabling flag
gboolean enabled;

typedef struct _ThreadRuntimeInformation ThreadRuntimeInformation;

struct _ThreadRuntimeInformation
{
    GumThreadId tid; //gsize printed as "lu"
    guint64 previous_block_id; // save the previously executed block address by a thread
    GHashTable * block_counters;
    GHashTable * branch_counters;
};

/*
 * This map should need locking mechanism, because we could prepare the responce to stop tracing
 * without being sure every thread actually stopped. Unfortunately, we cannot use synchronization
 * primitive inside the code to be executed by system server threads. It simply crashes.
 */
GHashTable * thread_runtime_information_map;

// module map for determining which module a given memory address belogs to, if any
GumModuleMap* module_map;

extern "C"
void
stalker_server_main (const gchar * data, bool * stay_resident)
{

    gint s_fd;
    const gchar * name;
    struct sockaddr_un address;
    gchar* sun_path;
    size_t pathLength;
    gint client;

    /* init */
    (void) data;
    *stay_resident = false;

    LOGI("enter");

    check_and_enlarge_stack_size();

    /* create socket */
    s_fd = socket(PF_LOCAL, SOCK_STREAM, 0);
    if (s_fd == -1){
        LOGF("can't open the local socket");
        return;
    }
    LOGD("socket open (%d)", s_fd);

    /* bind to name */
    name = "stalker_socket";
    memset(&address, 0, sizeof(address));

    address.sun_family = PF_LOCAL;

    sun_path = address.sun_path;
    *sun_path++ = '\0';
    strcpy(sun_path, name);

    pathLength = strlen(name)+1;
    socklen_t addressLength = (offsetof(struct sockaddr_un, sun_path)) + pathLength;

    unlink(address.sun_path);

    LOGI("binding to local socket: %s",  name);
    if (bind(s_fd, (struct sockaddr*) &address, addressLength) == -1)
    {
        LOGF("can't bind the local socket: %s", strerror(errno));
        return;
    }

    /* listen for incoming connection */
    LOGI("listening from the local socket");

    if (listen(s_fd, 1) == -1)
    {
        LOGF("can't listen from the local socket");
        return;
    }

    /* wait for stalker client */
    client = accept(s_fd, NULL, NULL);

    if (client == -1)
    {
        LOGF("can't accept from the local socket");
        return;
    }

    /* Receive message, parse json, call method and send return value back */
    server_loop(client);

    /* Close the client socket */
    close(client);
    if (s_fd > 0)
    {
        close(s_fd);
    }

    LOGI("exit");

}

void
check_and_enlarge_stack_size()
{

    gchar cmdLine[100];
    FILE * cmd;
    gchar * line;

    sprintf(cmdLine, "ulimit -s unlimited; ulimit -s");

    cmd = popen(cmdLine, "r");
    line = (gchar *) g_malloc0(50);
    fgets(line, 50, cmd);

    pclose(cmd);

    LOGD("ulimit %s", line);

}

gboolean exclude_module(const GumModuleDetails *details, gpointer user_data)
{

    if (g_strcmp0 (details->name, "libc.so") == 0 ||
        g_strcmp0 (details->name, "libstalker-server.so") == 0 ||
        g_strcmp0 (details->name, "libc++.so") == 0 ||
        g_strcmp0 (details->name, "libutils.so") == 0){
        LOGI("excluding %s %s %p %p",
             details->name,
             details->path,
             (void *) details->range->base_address,
             (void *) (details->range->base_address + details->range->size));
        gum_stalker_exclude(stalker, details->range);
    }

    return TRUE;
}

void
server_loop(int client)
{

    gchar buffer[MAX_RECEIVE_BUFFER_SIZE];
    ssize_t recvSize;
    ssize_t sentSize;

    guint i;

    gum_init_embedded ();

    stalker = gum_stalker_new();
    gum_stalker_set_trust_threshold(stalker, 3);

    gum_process_enumerate_modules(exclude_module, NULL);

    module_map = gum_module_map_new();
    gum_module_map_update(module_map);

    /* check dumpable state */
    prctl(PR_SET_DUMPABLE, 1);
    gint result = prctl(PR_GET_DUMPABLE);
    g_assert_cmpint(result, ==, 1);

    thread_runtime_information_map = g_hash_table_new_full(NULL, NULL, NULL, free_tri);

    // actual server loop
    while (1)
    {
        jsmn_parser p;
        jsmntok_t t[128];
        gint r;
        method_id m;

        /* Receive from the socket */
        recvSize = receive_from_socket(client, buffer, MAX_RECEIVE_BUFFER_SIZE);
        if (recvSize <= 0){
            LOGI("can't receive from the local socket");
            break;
        }

        /* init jsmn to read the message */
        jsmn_init(&p);
        r = jsmn_parse(&p, buffer, strlen(buffer), t, sizeof(t)/sizeof(t[0]));
        if (r < 0) {
            LOGW("failed to parse JSON, returned %d", r);
            break;
        }

        /* Assume the top-level element is an object and the method id*/
        i = 0;
        if (r < 1 || t[i].type != JSMN_OBJECT) {
            LOGW("expected a json object, unable to continue");
            break;
        }

        if (jsoneq(buffer, &t[++i], "m") != 0) {
            LOGW("expected the method id, unable to continue");
            break;
        }
        m = (method_id) strtol(buffer + t[++i].start, NULL, 10);

        GString * response;
        switch (m){
            case FOLLOW_THREADS:
            {
                gint j;
                GPtrArray * white_list;
                GPtrArray * black_list;

                white_list = g_ptr_array_new();
                g_ptr_array_set_free_func (white_list, g_free);

                if (jsoneq(buffer, &t[++i], "white") != 0 ||
                    t[i+1].type != JSMN_ARRAY){
                    LOGW("expected the white list json array, return error");
                    response = g_string_new("{\"error\":-1}\n");
                    break;
                }
                for (j = 0; j < t[i+1].size; j++) {
                    jsmntok_t *g = &t[i+j+2];
                    g_ptr_array_add (white_list, g_strdup_printf
                            ("%.*s", g->end - g->start, buffer + g->start));
                }
                i += t[i+1].size + 1;

                black_list = g_ptr_array_new();
                g_ptr_array_set_free_func (black_list, g_free);

                if (jsoneq(buffer, &t[++i], "black") != 0 ||
                    t[i+1].type != JSMN_ARRAY){
                    LOGW("expected the black list json array, return error");
                    response = g_string_new("{\"error\":-2}\n");
                    break;
                }
                for (j = 0; j < t[i+1].size; j++) {
                    jsmntok_t *g = &t[i+j+2];
                    g_ptr_array_add (black_list, g_strdup_printf
                            ("%.*s", g->end - g->start, buffer + g->start));
                }
                i += t[i+1].size + 1;

                response = g_string_new("");
                g_string_printf(response,
                                "{\"following\":%d}\n",
                                follow_threads_by_lists(white_list, black_list));

                g_ptr_array_free(white_list, TRUE);
                g_ptr_array_free(black_list, TRUE);

                break;
            }
            case START_TRACING:{
                response = g_string_new("");
                g_string_printf(response,
                                "{\"error\":%d}\n",
                                start_tracing());
                break;
            }
            case STOP_TRACING:{
                gint error;
                ThreadRuntimeInformation * collapsed_tri;

                response = g_string_new("");

                error = stop_tracing();
                if (error != 0){
                    g_string_printf(response,
                                    "{\"error\":%d}\n",
                                    error);
                    break;
                }

                collapsed_tri = (ThreadRuntimeInformation *)
                        g_malloc0(sizeof(ThreadRuntimeInformation));
                collapsed_tri->block_counters = g_hash_table_new(NULL, NULL);
                collapsed_tri->branch_counters = g_hash_table_new(NULL, NULL);

                g_hash_table_foreach(thread_runtime_information_map, collapse_tri, collapsed_tri);

                response = g_string_new("{\"blocks\":{ ");
                g_hash_table_foreach(collapsed_tri->block_counters, key_value_to_json, response);
                g_string_overwrite(response, response->len-1,"},\"branches\":{ ");
                g_hash_table_foreach(collapsed_tri->branch_counters, key_value_to_json, response);
                g_string_overwrite(response, response->len-1,"}}\n");

                break;
            }
            default:{
                response = g_string_new("{\"error\":-999}\n");
            }
        }

        // Send to the socket
        sentSize = send_to_socket(client, response->str, response->len);
        if (sentSize <= 0){
            LOGI("can't send to the local socket");
            break;
        }

    }

    gum_stalker_stop(stalker);
    i = 0;
    while (gum_stalker_garbage_collect(stalker)){
        LOGD("SGC %d", i++);
        g_usleep(G_USEC_PER_SEC);
    }
    g_object_unref (stalker);

    g_hash_table_unref(thread_runtime_information_map);

    gum_deinit_embedded ();

    LOGD("the end of stalking");

}

void
free_tri(gpointer data)
{
    ThreadRuntimeInformation * tri;

    tri = (ThreadRuntimeInformation *) data;

    g_hash_table_unref(tri->block_counters);
    g_hash_table_unref(tri->branch_counters);

    g_free(tri);

}

void
collapse_tri (gpointer key, gpointer value, gpointer user_data)
{
    ThreadRuntimeInformation * tri;
    ThreadRuntimeInformation * collapsed_tri;

    tri = (ThreadRuntimeInformation *) value;
    collapsed_tri = (ThreadRuntimeInformation *) user_data;

    g_hash_table_foreach(tri->block_counters, collapse_counters, collapsed_tri->block_counters);
    g_hash_table_foreach(tri->branch_counters, collapse_counters, collapsed_tri->branch_counters);
}

void
collapse_counters (gpointer key, gpointer value, gpointer user_data)
{
    GHashTable * collapsed_counters;
    guint64 v;

    collapsed_counters = (GHashTable *) user_data;

    v = GPOINTER_TO_UINT (value) + GPOINTER_TO_UINT (g_hash_table_lookup (collapsed_counters, key));

    g_hash_table_insert (collapsed_counters, key, GUINT_TO_POINTER(v));

}

void
key_value_to_json (gpointer key, gpointer value, gpointer user_data)
{
    guint64 k = GPOINTER_TO_UINT(key);
    guint64 v = GPOINTER_TO_UINT(value);
    GString * s = (GString *) user_data;

    g_string_append_printf(s,
                           "\"%li\":%li,",
                           k,
                           v);
}

gint
follow_threads_by_lists(GPtrArray * white_list, GPtrArray * black_list)
{
    GumThreadId this_thread_id;
    GHashTable * thread_set;
    GHashTableIter thread_iter;
    gpointer key, value;
    GumFakeEventSink * sink;
    GumStalkerTransformer * transformer;

    guint i;

    this_thread_id = gum_process_get_current_thread_id();

    thread_set = g_hash_table_new (NULL, NULL);
    gum_process_enumerate_threads(addThread, thread_set);

    g_assert_cmpint(g_hash_table_size(thread_set), >, 0);

    g_hash_table_iter_init (&thread_iter, thread_set);

    sink = GUM_FAKE_EVENT_SINK (gum_fake_event_sink_new ());
    enabled = false;

    transformer = gum_stalker_transformer_make_from_callback(chizpurfle_transformer, NULL, NULL);

    while (g_hash_table_iter_next (&thread_iter, &key, &value))
    {
        guint tid;
        gchar * name;
        gchar * list_element;
        GError *err;
        GMatchInfo *matchInfo;
        GRegex *regex;
        gboolean in_list;
        ThreadRuntimeInformation * tri;

        tid = GPOINTER_TO_UINT(key);
        if (tid == this_thread_id) continue;
        name = read_task_comm(tid);
        LOGD("considering thread %s", name);

        in_list = FALSE;
        for (i = 0; i < black_list->len; i++){
            list_element = (gchar *) g_ptr_array_index(black_list, i);
            err = NULL;
            regex = g_regex_new (list_element, G_REGEX_CASELESS, G_REGEX_MATCH_NOTEMPTY, &err);
            if (err != NULL){
                LOGE("regex error %d - %s\n", err->code, err->message);
            }
            g_regex_match (regex, name, G_REGEX_MATCH_NOTEMPTY, &matchInfo);
            if (g_match_info_matches (matchInfo)){
                in_list = TRUE;
                break;
            }
        }
        if (in_list){
            g_hash_table_iter_remove (&thread_iter);
            g_free(name);
            continue;
        }

        in_list = TRUE;
        if (white_list->len > 0){
            in_list = FALSE;
        }
        for (i = 0; i < white_list->len; i++){
            list_element = (gchar *) g_ptr_array_index(white_list, i);
            err = NULL;
            regex = g_regex_new (list_element, G_REGEX_CASELESS, G_REGEX_MATCH_NOTEMPTY, &err);
            if (err != NULL){
                LOGE("regex error %d - %s\n", err->code, err->message);
            }
            g_regex_match (regex, name, G_REGEX_MATCH_NOTEMPTY, &matchInfo);
            if (g_match_info_matches (matchInfo)){
                in_list = TRUE;
                break;
            }
        }
        if (!in_list){
            g_hash_table_iter_remove (&thread_iter);
            g_free(name);
            continue;
        }

        LOGI("following %d - %s", tid, name);
        tri = (ThreadRuntimeInformation *) g_malloc0(sizeof(ThreadRuntimeInformation));
        tri->tid = tid;
        tri->previous_block_id = 0;
        tri->block_counters = g_hash_table_new(NULL, NULL);
        tri->branch_counters = g_hash_table_new(NULL, NULL);
        g_hash_table_insert(thread_runtime_information_map, GSIZE_TO_POINTER(tri->tid), tri);
        gum_stalker_follow(stalker,
                           (GumThreadId) tid,
                           transformer,
                           GUM_EVENT_SINK(sink));
        g_usleep(G_USEC_PER_SEC); //TODO NEED SOME SIGNALS TO WAKE UP SOMETHING ELSE: IS THIS THE RIGHT PLACE?
        g_free(name);
    }

    g_assert_cmpint(g_hash_table_size(thread_set), !=, 0);

    return g_hash_table_size(thread_set);

}

gboolean
addThread (const GumThreadDetails * details, gpointer data)
{
    GHashTable * thread_set;
    GumThreadId threadId;

    thread_set = (GHashTable *) data;
    threadId = details->id;

    g_hash_table_add (thread_set, GSIZE_TO_POINTER (threadId));

    return true;
}

gchar *
read_task_comm(guint tid)
{
    gchar cmdLine[100];
    FILE * cmd;
    gchar * line;

    sprintf(cmdLine, "cat /proc/%d/task/%d/comm", getpid(), tid);

    cmd = popen(cmdLine, "r");
    line = (gchar *) g_malloc0(50);
    fgets(line, 50, cmd);

    pclose(cmd);

    return g_strstrip(line);
}

static void
chizpurfle_transformer (GumStalkerIterator * iterator, GumStalkerWriter * output,
                        gpointer user_data)
{

    const cs_insn * insn;
    gpointer block_address;
    const GumModuleDetails *details;

    while (gum_stalker_iterator_next (iterator, &insn)) {

        switch (insn->id) {
            case ARM64_INS_BL:
            case ARM64_INS_B:
            case ARM64_INS_BLR:
            case ARM64_INS_BR:
            case ARM64_INS_CBZ:
            case ARM64_INS_CBNZ:
            case ARM64_INS_TBZ:
            case ARM64_INS_TBNZ:
            case ARM64_INS_RET: {

                block_address = GUINT_TO_POINTER(insn->address);

                details = gum_module_map_find(module_map, GUM_ADDRESS(block_address));
                if (details != NULL) {
                    LOGD("transformed block in module %s %p", details->name, GUM_ADDRESS(block_address));

                    block_address =
                            GUINT_TO_POINTER(
                                    (GUM_ADDRESS(g_str_hash(details->name)) << 32) ^
                                    (GUM_ADDRESS(block_address) - details->range->base_address)
                            );

                    gum_stalker_iterator_put_callout(iterator, process_block_address, block_address,
                                                     NULL);

                }
            }
            default:
                break;
        }

        gum_stalker_iterator_keep (iterator);

    }

}

static void
process_block_address (GumCpuContext * cpu_context, gpointer user_data)
{

    gsize tid;
    ThreadRuntimeInformation * tri;
    guint64 n;
    guint64 block_id, branch_id;

    if (!enabled)
        return;

    tid = gum_process_get_current_thread_id();
    tri = (ThreadRuntimeInformation *)
            g_hash_table_lookup(thread_runtime_information_map, GSIZE_TO_POINTER(tid));

    block_id = (GPOINTER_TO_UINT(user_data)>>4) ^ (GPOINTER_TO_UINT(user_data) << 8);

    branch_id = tri->previous_block_id^block_id;

    n = GPOINTER_TO_UINT(g_hash_table_lookup(tri->block_counters, GUINT_TO_POINTER(block_id)));
//    if (n == 0)
//        LOGD("new block id %lu", block_id);
    g_hash_table_insert(tri->block_counters, GUINT_TO_POINTER(block_id), GUINT_TO_POINTER(n+1));
    //LOGD("%li, %li", block_id, n);

    n = GPOINTER_TO_UINT(g_hash_table_lookup(tri->branch_counters, GUINT_TO_POINTER(branch_id)));
    g_hash_table_insert(tri->branch_counters, GUINT_TO_POINTER(branch_id), GUINT_TO_POINTER(n+1));
    //LOGD("%li, %li", branch_id, n);

    tri->previous_block_id = block_id>>1;

}

gint
start_tracing(void)
{
    g_hash_table_foreach(thread_runtime_information_map, clean_tri, NULL);

    enabled = true;
    g_usleep(G_USEC_PER_SEC); //TODO NEED SOME SIGNALS TO WAKE UP SOMETHING ELSE: IS THIS THE RIGHT PLACE?
    return (enabled==true)?0:-1;

}

void
clean_tri (gpointer key, gpointer value, gpointer user_data)
{

    ThreadRuntimeInformation * tri;

    tri = (ThreadRuntimeInformation *) value;

    tri->previous_block_id=0;
    g_hash_table_remove_all(tri->block_counters);
    g_hash_table_remove_all(tri->branch_counters);

}

gint
stop_tracing(void)
{
    enabled = false;
    g_usleep(G_USEC_PER_SEC); //TODO NEED SOME SIGNALS TO WAKE UP SOMETHING ELSE: IS THIS THE RIGHT PLACE?
    return (enabled==false)?0:-1;
}

/*
 * SOCKET FUNCTIONS
 */

ssize_t
receive_from_socket (int sd, gchar * buffer, size_t bufferSize)
{

    LOGD("receiving...");
    ssize_t recvSize = recv(sd, buffer, bufferSize - 1, 0);

    // If receive is failed
    if (recvSize == -1)
    {
        LOGE("receive is failed");
        return -1;
    }
    else
    {
        buffer[recvSize] = '\0';

        if (recvSize > 0)
            LOGD("received %zu bytes: %s", recvSize, buffer);
        else
            LOGW("client disconnected.");
    }

    return recvSize;
}

ssize_t
send_to_socket (int sd, const gchar * buffer, size_t bufferSize)
{
    ssize_t sentSize = send(sd, buffer, bufferSize, 0);

    if (sentSize == -1)
    {
        LOGE("send is failed");
        return -1;
    }
    else
    {
        if (sentSize > 0)
            LOGD("sent %zu/%zu bytes: %.15s ...", sentSize, bufferSize, buffer);
        else
            LOGW("client disconnected.");
    }

    return sentSize;
}

/*
 * JSON FUNCTIONS
 */

int
jsoneq(const gchar *json, jsmntok_t *tok, const gchar *s)
{
    if (tok->type == JSMN_STRING && (int) strlen(s) == tok->end - tok->start &&
        strncmp(json + tok->start, s, (size_t) (tok->end - tok->start)) == 0) {
        return 0;
    }
    return -1;
}