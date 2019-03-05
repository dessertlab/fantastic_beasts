/*
 * Copyright (C) 2009 Ole André Vadla Ravnås <ole.andre.ravnas@tillitech.com>
 *
 * Licence: wxWindows Library Licence, Version 3.1
 */

#include <android/log.h>
#include <unistd.h>
#include "fakeeventsink.h"

#define LOG_TAG "NativeStalkerServer"
#define  LOGF(...)  __android_log_print(ANDROID_LOG_FATAL,LOG_TAG,__VA_ARGS__)
#define  LOGE(...)  __android_log_print(ANDROID_LOG_ERROR,LOG_TAG,__VA_ARGS__)
#define  LOGW(...)  __android_log_print(ANDROID_LOG_WARN,LOG_TAG,__VA_ARGS__)
#define  LOGI(...)  __android_log_print(ANDROID_LOG_INFO,LOG_TAG,__VA_ARGS__)
#define  LOGD(...)  __android_log_print(ANDROID_LOG_DEBUG,LOG_TAG,__VA_ARGS__)

static void gum_fake_event_sink_iface_init (gpointer g_iface,
    gpointer iface_data);
static void gum_fake_event_sink_finalize (GObject * obj);
static GumEventType gum_fake_event_sink_query_mask (GumEventSink * sink);
static void gum_fake_event_sink_process (GumEventSink * sink,
    const GumEvent * ev);

G_DEFINE_TYPE_EXTENDED (GumFakeEventSink,
                        gum_fake_event_sink,
                        G_TYPE_OBJECT,
                        0,
                        G_IMPLEMENT_INTERFACE (GUM_TYPE_EVENT_SINK,
                                               gum_fake_event_sink_iface_init));

static void
gum_fake_event_sink_class_init (GumFakeEventSinkClass * klass)
{
  GObjectClass * object_class = G_OBJECT_CLASS (klass);

  object_class->finalize = gum_fake_event_sink_finalize;
}

static void
gum_fake_event_sink_iface_init (gpointer g_iface,
                                gpointer iface_data)
{
  GumEventSinkIface * iface = (GumEventSinkIface *) g_iface;

  iface->query_mask = gum_fake_event_sink_query_mask;
  iface->process = gum_fake_event_sink_process;
}

static void
gum_fake_event_sink_init (GumFakeEventSink * self)
{
    //EMPTY
}

static void
gum_fake_event_sink_finalize (GObject * obj)
{
    G_OBJECT_CLASS (gum_fake_event_sink_parent_class)->finalize (obj);
}

GumEventSink *
gum_fake_event_sink_new (void)
{
    GumFakeEventSink * sink;

    sink = g_object_new (GUM_TYPE_FAKE_EVENT_SINK, NULL);

    return GUM_EVENT_SINK (sink);
}

void
gum_fake_event_sink_reset (GumFakeEventSink * self)
{
    //EMPTY
}

static GumEventType
gum_fake_event_sink_query_mask (GumEventSink * sink)
{
  return 0;
}

static void
gum_fake_event_sink_process (GumEventSink * sink,
                             const GumEvent * ev)
{
    g_assert_cmpstr("unreachable code", ==, "gum_fake_event_sink_process");
}
