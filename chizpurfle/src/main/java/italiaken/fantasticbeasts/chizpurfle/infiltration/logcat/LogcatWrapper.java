package italiaken.fantasticbeasts.chizpurfle.infiltration.logcat;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.List;

import italiaken.fantasticbeasts.chizpurfle.L;

/**
 * Created by ken on 17/03/17 for fantastic_beasts
 */

public class LogcatWrapper {

    static public String logcatClear = "logcat -b all -c";
    static public String logcatCommand = "logcat -d -v long ";
    //-b all -v descriptive " + "CHIZPURFLE_OUTPUT_ROOT_DIR:S NativeStalkerServer:S";

    public static boolean clearLogcat(){

        Process logcat;
        boolean ok = true;

        try {
            logcat = Runtime.getRuntime().exec(logcatClear);
        } catch (IOException e) {
            L.e("can't clear logcat", e);
            return false;
        }

        try {
            logcat.waitFor();
        } catch (InterruptedException e) {
            L.w("got a problem in clearing the log", e);
            ok = false;
        }

        logcat.destroy();

        return ok;
    }

    public static List<LogcatLine> dumpLogcatLines() {

        Process logcat;
        List<LogcatLine> result = null;

        try {
            logcat = Runtime.getRuntime().exec(logcatCommand);
        } catch (IOException e) {
            L.e("can't dump logcat", e);
            return null;
        }

        try (LongLogcatReader reader =
                     new LongLogcatReader(new BufferedReader(new InputStreamReader(
                             logcat.getInputStream())))) {
            result = reader.readAllLogcat();
        } catch (IOException e) {
            L.w("got a problem in reading the dump logcat", e);
        }

        logcat.destroy();

        return result;
    }

    public static List<String> dumpLines() {

        Process logcat;
        List<String> result = null;

        try {
            logcat = Runtime.getRuntime().exec(logcatCommand);
        } catch (IOException e) {
            L.e("can't dump logcat", e);
            return null;
        }

        try (LongLogcatReader reader =
                     new LongLogcatReader(new BufferedReader(new InputStreamReader(
                             logcat.getInputStream())))) {
            result = reader.readAll();
        } catch (IOException e) {
            L.w("got a problem in reading the dump logcat", e);
        }

        logcat.destroy();

        return result;
    }
}
