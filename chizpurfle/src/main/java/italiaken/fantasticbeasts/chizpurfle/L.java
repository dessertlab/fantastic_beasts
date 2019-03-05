package italiaken.fantasticbeasts.chizpurfle;

import android.util.Log;

/**
 * Created by ken on 22/03/17.
 */

public class L {

    private static String TAG = "KenLoggingUtility";

    private static boolean logcatMode = true;
    private static boolean consoleMode = true;

    private static int logLevel = Log.DEBUG;

    public static void setTag(String tag){
        TAG = tag;
    }

    public static final String ANSI_RESET = "\u001B[0m";
    public static final String ANSI_BLACK = "\u001B[30m";
    public static final String ANSI_RED = "\u001B[31m";
    public static final String ANSI_GREEN = "\u001B[32m";
    public static final String ANSI_YELLOW = "\u001B[33m";
    public static final String ANSI_BLUE = "\u001B[34m";
    public static final String ANSI_PURPLE = "\u001B[35m";
    public static final String ANSI_CYAN = "\u001B[36m";
    public static final String ANSI_WHITE = "\u001B[37m";

    public static final String ANSI_BLACK_BACKGROUND = "\u001B[40m";
    public static final String ANSI_RED_BACKGROUND = "\u001B[41m";
    public static final String ANSI_GREEN_BACKGROUND = "\u001B[42m";
    public static final String ANSI_YELLOW_BACKGROUND = "\u001B[43m";
    public static final String ANSI_BLUE_BACKGROUND = "\u001B[44m";
    public static final String ANSI_PURPLE_BACKGROUND = "\u001B[45m";
    public static final String ANSI_CYAN_BACKGROUND = "\u001B[46m";
    public static final String ANSI_WHITE_BACKGROUND = "\u001B[47m";

    public static void v(String s) {
        if (logLevel > Log.VERBOSE) return;
        if (logcatMode) Log.v(TAG, s);
        if (consoleMode) System.out.println(ANSI_CYAN+s+ANSI_RESET);
    }

    public static void d(String s) {
        if (logLevel > Log.DEBUG) return;
        if (logcatMode) Log.d(TAG, s);
        if (consoleMode) System.out.println(ANSI_BLACK+s+ANSI_RESET);
    }

    public static void d(String s, Exception e) {
        if (logLevel > Log.DEBUG) return;
        if (logcatMode) Log.d(TAG, s, e);
        if (consoleMode) System.out.println(ANSI_BLACK+s+ANSI_RESET);
    }

    public static void i(String s) {
        if (logLevel > Log.INFO) return;
        if (logcatMode) Log.i(TAG, s);
        if (consoleMode) System.out.println(ANSI_GREEN+s+ANSI_RESET);
    }

    public static void w(String s) {
        if (logLevel > Log.WARN) return;
        if (logcatMode) Log.w(TAG, s);
        if (consoleMode) System.out.println(ANSI_YELLOW+s+ANSI_RESET);
    }

    public static void w(String s, Exception e) {
        if (logLevel > Log.WARN) return;
        if (logcatMode) Log.w(TAG, s, e);
        if (consoleMode) System.out.println(ANSI_YELLOW+s+ANSI_RESET);
    }

    public static void e(String s) {
        Log.e(TAG, s);
        System.out.println(ANSI_RED+s+ANSI_RESET);
    }

    public static void e(String s, Exception e) {
        Log.e(TAG, s, e);
        System.out.println(ANSI_RED+s+ANSI_RESET);
    }

}
