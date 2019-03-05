package italiaken.fantasticbeasts.chizpurfle.fuzz.blackbox;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.util.Arrays;

import italiaken.fantasticbeasts.chizpurfle.L;
import italiaken.fantasticbeasts.chizpurfle.Utils;
import italiaken.fantasticbeasts.chizpurfle.fuzz.TestOutputAnalyzer;

/**
 * Created by ken on 17/01/18 for fantastic_beasts
 */

public class BlackBoxTestAnalyzer extends TestOutputAnalyzer<BlackBoxTest> {

    @Override
    public void analyze(BlackBoxTest test) {

    }

    @Override
    public void save(BlackBoxTest test, File testFolder) {

        /* TEST INFO */
        saveBlackBoxTestInfo(test, testFolder);

        /* EXCEPTION INFO */
        saveExceptionInfo(test, testFolder);

        /* LOGS */
        saveLogs(test, testFolder);

        /* TRACES */
        if (test.getTraces() != null) {
            saveTraces(testFolder, test.getTraces().getJsonString());
        }
    }

    private void saveTraces(File testFolder, JSONObject jsonString) {
        Utils.saveJsonToFile(jsonString, testFolder, "traces");
    }

    private void saveLogs(BlackBoxTest test, File testFolder) {
        try {
            JSONObject logs = new JSONObject();

            JSONArray logcat = new JSONArray();
            for (String log : test.getLogcat()){
                logcat.put(log);
            }
            logs.put("logcat", logcat);

            Utils.saveJsonToFile(logs, testFolder, "logs");

        } catch (JSONException e) {
            L.e("can't save logs", e);
        }

    }

    private void saveExceptionInfo(BlackBoxTest test, File testFolder) {
        try {
            JSONObject exceptionInfo = new JSONObject();

            Throwable throwable;

            throwable = test.getMethodCallerException();
            JSONArray methodCallerException = new JSONArray();
            while (throwable != null){
                methodCallerException.put(throwable.toString());
                throwable = throwable.getCause();
            }
            exceptionInfo.put("method_caller_exception", methodCallerException);

            throwable = test.getInstrumentationException();
            JSONArray instrumentationException = new JSONArray();
            while (throwable != null){
                instrumentationException.put(throwable.toString());
                throwable = throwable.getCause();
            }
            exceptionInfo.put("instrumentation_exception", instrumentationException);

            Utils.saveJsonToFile(exceptionInfo, testFolder, "exceptions");

        } catch (JSONException e) {
            L.e("can't save instrumentation info", e);
        }

    }

    private void saveBlackBoxTestInfo(BlackBoxTest test, File testFolder) {
        try {
            JSONObject individualInfo = new JSONObject()
                    .put("method", test.getMethodCaller().getMethodName());

            JSONArray parameter_types = new JSONArray();
            for (Class k : test.getMethodCaller().getParameterTypes()){
                parameter_types.put(k.getName());
            }
            individualInfo.put("parameter_types", parameter_types);

            JSONArray parameter_values = new JSONArray();
            for (Object o : test.getParameters()){
                if (o == null){
                    parameter_values.put("null");
                } else if (o.getClass().isArray()) {
                    if (o.getClass().getComponentType().equals(Byte.TYPE))
                        parameter_values.put(Arrays.toString((byte[])o));
                    else if (o.getClass().getComponentType().equals(Short.TYPE))
                        parameter_values.put(Arrays.toString((short[])o));
                    else if (o.getClass().getComponentType().equals(Integer.TYPE))
                        parameter_values.put(Arrays.toString((int[])o));
                    else if (o.getClass().getComponentType().equals(Long.TYPE))
                        parameter_values.put(Arrays.toString((long[])o));
                    else if (o.getClass().getComponentType().equals(Float.TYPE))
                        parameter_values.put(Arrays.toString((float[])o));
                    else if (o.getClass().getComponentType().equals(Double.TYPE))
                        parameter_values.put(Arrays.toString((double[])o));
                    else if (o.getClass().getComponentType().equals(Boolean.TYPE))
                        parameter_values.put(Arrays.toString((boolean[])o));
                    else if (o.getClass().getComponentType().equals(Character.TYPE))
                        parameter_values.put(Arrays.toString((char[])o));
                    else
                        parameter_values.put(Arrays.toString((Object[]) o));
                } else{
                    try {
                        parameter_values.put(o.toString());
                    } catch (NullPointerException npe) {
                        L.w("NullPointerException when calling toString on a not null object",
                                npe);
                        parameter_values.put("null");
                    }
                }
            }
            individualInfo.put("parameter_values", parameter_values);

            Utils.saveJsonToFile(individualInfo, testFolder, "blackboxtest");

        } catch (JSONException e) {
            L.e("can't save black box test info", e);
        }
    }
}
