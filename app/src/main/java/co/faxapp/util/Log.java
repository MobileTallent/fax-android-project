package co.faxapp.util;

public class Log {
    private static boolean show = false;

    public static void i(String tag, String msg) {
        if (show) android.util.Log.i(tag, msg);
    }

    public static void d(String tag, String msg) {
        if (show) android.util.Log.d(tag, msg);
    }

    public static void e(String tag, String msg, Throwable t) {
        android.util.Log.e(tag, msg, t);
    }

    public static void e(String tag, String msg) {
        android.util.Log.e(tag, msg);
    }
}
