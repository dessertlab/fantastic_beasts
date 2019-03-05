/*
 * Copyright (C) 2009 Ole André Vadla Ravnås <ole.andre.ravnas@tillitech.com>
 *
 * Licence: wxWindows Library Licence, Version 3.1
 */

#ifndef __FAKE_EVENT_SINK_H__
#define __FAKE_EVENT_SINK_H__

#include <frida-gum.h>

#define GUM_TYPE_FAKE_EVENT_SINK (gum_fake_event_sink_get_type ())
#define GUM_FAKE_EVENT_SINK(obj) (G_TYPE_CHECK_INSTANCE_CAST ((obj),\
    GUM_TYPE_FAKE_EVENT_SINK, GumFakeEventSink))
#define GUM_FAKE_EVENT_SINK_CLASS(klass) (G_TYPE_CHECK_CLASS_CAST ((klass),\
    GUM_TYPE_FAKE_EVENT_SINK, GumFakeEventSinkClass))
#define GUM_IS_FAKE_EVENT_SINK(obj) (G_TYPE_CHECK_INSTANCE_TYPE ((obj),\
    GUM_TYPE_FAKE_EVENT_SINK))
#define GUM_IS_FAKE_EVENT_SINK_CLASS(klass) (G_TYPE_CHECK_CLASS_TYPE (\
    (klass), GUM_TYPE_FAKE_EVENT_SINK))
#define GUM_FAKE_EVENT_SINK_GET_CLASS(obj) (G_TYPE_INSTANCE_GET_CLASS (\
    (obj), GUM_TYPE_FAKE_EVENT_SINK, GumFakeEventSinkClass))

typedef struct _GumFakeEventSink GumFakeEventSink;
typedef struct _GumFakeEventSinkClass GumFakeEventSinkClass;

struct _GumFakeEventSink
{
    GObject parent;
    GumEventType mask;
    GHashTable * events;

    GMutex mutex;
    gboolean enabled;
};

struct _GumFakeEventSinkClass
{
  GObjectClass parent_class;
};

G_BEGIN_DECLS

GType gum_fake_event_sink_get_type (void) G_GNUC_CONST;

GumEventSink * gum_fake_event_sink_new (void);


G_END_DECLS

#endif
