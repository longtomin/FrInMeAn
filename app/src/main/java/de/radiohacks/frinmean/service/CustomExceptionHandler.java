package de.radiohacks.frinmean.service;

import android.content.Context;
import android.content.ContextWrapper;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.os.AsyncTask;
import android.util.Log;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.Writer;

public class CustomExceptionHandler implements Thread.UncaughtExceptionHandler {

    ContextWrapper activity;
    // public static String sendErrorLogsTo = "tushar.pandey@virtualxcellence.com";
    private Thread.UncaughtExceptionHandler defaultUEH;

    public CustomExceptionHandler(ContextWrapper activity) {
        this.defaultUEH = Thread.getDefaultUncaughtExceptionHandler();
        this.activity = activity;
    }

    public void uncaughtException(Thread t, Throwable e) {

        final Writer result = new StringWriter();
        final PrintWriter printWriter = new PrintWriter(result);
        e.printStackTrace(printWriter);
        String stacktrace = result.toString();
        printWriter.close();
        String filename = "error" + System.nanoTime() + ".stacktrace";

        Log.e("Hi", "url != null");
        sendToServer(stacktrace, filename);

        StackTraceElement[] arr = e.getStackTrace();
        String report = e.toString() + "\n\n";
        report += "--------- Stack trace ---------\n\n";
        for (int i = 0; i < arr.length; i++) {
            report += "    " + arr[i].toString() + "\n";
        }
        report += "-------------------------------\n\n";

        report += "--------- Cause ---------\n\n";
        Throwable cause = e.getCause();
        if (cause != null) {
            report += cause.toString() + "\n\n";
            arr = cause.getStackTrace();
            for (int i = 0; i < arr.length; i++) {
                report += "    " + arr[i].toString() + "\n";
            }
        }
        report += "-------------------------------\n\n";

        defaultUEH.uncaughtException(t, e);
    }

    private void sendToServer(String stacktrace, String filename) {
        AsyncTaskClass async = new AsyncTaskClass(stacktrace, filename,
                getAppLable(activity));
        async.execute("");
    }

    public String getAppLable(Context pContext) {
        PackageManager lPackageManager = pContext.getPackageManager();
        ApplicationInfo lApplicationInfo = null;
        try {
            lApplicationInfo = lPackageManager.getApplicationInfo(
                    pContext.getApplicationInfo().packageName, 0);
        } catch (final PackageManager.NameNotFoundException e) {
        }
        return (String) (lApplicationInfo != null ? lPackageManager
                .getApplicationLabel(lApplicationInfo) : "Unknown");
    }

    public class AsyncTaskClass extends AsyncTask<String, String, Void> {
        final String filename;
        String stacktrace;
        String applicationName;

        AsyncTaskClass(final String stacktrace, final String filename,
                       String applicationName) {
            this.applicationName = applicationName;
            this.stacktrace = stacktrace;
            this.filename = filename;
        }

        @Override
        protected Void doInBackground(String... params) {
            RestFunctions rf = new RestFunctions();
            rf.sendErrorReport(this.filename);
            return null;
        }
    }
}
