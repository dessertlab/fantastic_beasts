package italiaken.fantasticbeasts.chizpurfle.instrumentation;

import android.net.LocalSocket;
import android.net.LocalSocketAddress;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import italiaken.fantasticbeasts.chizpurfle.L;
import italiaken.fantasticbeasts.chizpurfle.configuration.ConfigurationException;
import italiaken.fantasticbeasts.chizpurfle.configuration.ConfigurationManager;
import italiaken.fantasticbeasts.chizpurfle.instrumentation.trace.Block;
import italiaken.fantasticbeasts.chizpurfle.instrumentation.trace.Branch;
import italiaken.fantasticbeasts.chizpurfle.instrumentation.trace.ITrace;
import italiaken.fantasticbeasts.chizpurfle.instrumentation.trace.TracesMap;

/**
 * Created by ken on 21/11/17 for fantastic_beasts
 */

public class ProcessTracer implements Closeable {

    /* MUST BE SYNCHRONIZED WITH stalker-server.cpp */
    private static final int FOLLOW_THREADS_M = 0;
    private static final int START_TRACING_M = 1;
    private static final int STOP_TRACING_M = 2;

    private LocalSocket localSocket;
    private OutputStream outputStream;
    private BufferedReader bufferedReader;
    private boolean serviceDead;

    public ProcessTracer() throws InstrumentationException {
        initializeConnection();
        followThreads();
    }

    private void initializeConnection() throws InstrumentationException {
        localSocket = new LocalSocket(LocalSocket.SOCKET_STREAM);
        LocalSocketAddress localSocketAddress = new LocalSocketAddress("stalker_socket",
                LocalSocketAddress.Namespace.ABSTRACT);

        OutputStream os = null;
        InputStream is = null;

        boolean success = false;
        for (int trials = 1; trials <= ConfigurationManager.getMaxConnectionTrials(); trials++){

            success = true;
            try {
                localSocket.connect(localSocketAddress);
                os = localSocket.getOutputStream();
                is = localSocket.getInputStream();
            } catch (IOException e) {
                L.w("Not ready to connect the local socket (trial "+trials+")", e);
                success = false;
            }

            if (success)
                break;
            else
                try {
                    Thread.sleep(trials * trials * 1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
        }

        if (!success || os == null || is == null ){
            throw new InstrumentationException("Unable to set and connect the local socket");
        }

        outputStream = os;
        bufferedReader = new BufferedReader(new InputStreamReader(is));
    }

    private void followThreads() throws InstrumentationException{
        JSONObject object;

        try {

            object = new JSONObject();
            object.put("m", FOLLOW_THREADS_M);

            try {
                JSONObject listObject = ConfigurationManager.getThreadsList();
                object.put("white", listObject.get("white"));
                object.put("black", listObject.get("black"));
            } catch (ConfigurationException ce) {
                L.w("Using the default threads list (" + ce.getMessage() + ")", ce);

                JSONArray white = new JSONArray();
                white.put("thread");
                object.put("white", white);

                JSONArray black = new JSONArray();
                black.put("binder");
                object.put("black", black);

            }
        } catch (JSONException e) {
            throw new InstrumentationException("can't create the white and black list", e);
        }

        try {
            outputStream.write(object.toString().getBytes());
            deliver();
        } catch (IOException | JSONException e) {
            throw new InstrumentationException("can't remotely set the threads list", e);
        }
    }

    public void startTracing() throws InstrumentationException {

        try {

            JSONObject object = new JSONObject();
            object.put("m", START_TRACING_M);
            outputStream.write(object.toString().getBytes());

            deliver();

        } catch (JSONException | IOException e) {
            throw new InstrumentationException(" problem with start tracing ", e);
        }

    }

    public TracesMap stopTracing() throws InstrumentationException {

        try {

            JSONObject object = new JSONObject();
            object.put("m", STOP_TRACING_M);
            outputStream.write(object.toString().getBytes());

            JSONObject deliveredMessage = deliver();

            return new TracesMap(deliveredMessage);

        } catch (JSONException | IOException e) {
            throw new InstrumentationException(" problem with stop tracing ", e);
        }
    }

    private JSONObject deliver() throws IOException, JSONException, InstrumentationException {

        String receivedMessage = bufferedReader.readLine();

        if (receivedMessage == null)
            throw new InstrumentationException("connection closed");

        L.d("received " + receivedMessage.length() + " chars:" + receivedMessage);

        return new JSONObject(receivedMessage);

    }

    @Override
    public void close() throws IOException {
        bufferedReader.close();
        outputStream.close();
        localSocket.close();
    }

}
