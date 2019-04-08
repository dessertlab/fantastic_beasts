#include <jni.h>
#include <string>
#include <stdlib.h>

#include <frida-core.h>
#include <string.h>

bool initialized = false;

extern "C"
JNIEXPORT void JNICALL
Java_italiaken_fantasticbeasts_chizpurfle_instrumentation_InstrumentationManager_injectServerInProcess(
        JNIEnv *env, jobject instance, jstring processName_) {

    guint target_pid;
    GError * error = NULL;

    const char *processName = env->GetStringUTFChars(processName_, 0);

    char line[50];
    char cmdLine[50];
    //TODO pgrep è solo per huawei
    strcpy(cmdLine, "pidof ");
    //strcpy(cmdLine, "pgrep ");
    strcat(cmdLine, processName);
    FILE *cmd = popen(cmdLine, "r");
    fgets(line, 50, cmd);
    target_pid = (guint) strtoul(line, NULL, 10);
    pclose(cmd);

    if (! initialized){
        frida_init ();
        initialized = true;
    }

    FridaInjector* injector = frida_injector_new();

    g_print("injecting server in process %s with pid %d\n", processName, target_pid);
    env->ReleaseStringUTFChars(processName_, processName);

    gint a = frida_injector_inject_library_file_sync(injector, target_pid,
                                            "/data/local/tmp/libstalker-server.so",
                                            "stalker_server_main", "", &error);
    if (error != NULL) {
        g_print("%s\n",error->message);
        g_print("%d\n", a);
        g_print("Thank you\n");
    }
    else {
	g_print("injector loaded\n");
    }

    frida_injector_close_sync(injector);

    frida_unref(injector);

    //frida_deinit();

}
