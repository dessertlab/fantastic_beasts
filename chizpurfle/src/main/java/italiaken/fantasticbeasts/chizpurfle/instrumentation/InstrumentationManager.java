package italiaken.fantasticbeasts.chizpurfle.instrumentation;

import java.io.IOException;

import italiaken.fantasticbeasts.chizpurfle.L;

/**
 * Created by ken on 21/11/17 for fantastic_beasts
 */

public class InstrumentationManager {

    static {
        try {
            System.loadLibrary("libchizpurfle-native.so");
        } catch (UnsatisfiedLinkError e) {
            System.load("/data/local/tmp/libchizpurfle-native.so");
        }
    }

    private ProcessTracer tracer = null;

    private native void injectServerInProcess(String processName);

    public ProcessTracer instrumentProcess(String processName) throws InstrumentationException {

        if (tracer != null){
            L.d("instrumentProcess was already invoked once");
            reset();
        }

        injectServerInProcess(processName);

        tracer = new ProcessTracer();

        return getTracer();

    }

    public ProcessTracer getTracer(){
        return tracer;
    }

    public void reset() throws InstrumentationException {
        try {
            if (tracer!= null)
                tracer.close();
            tracer = null;
        } catch (IOException e) {
            throw new InstrumentationException(e);
        }
    }

}
