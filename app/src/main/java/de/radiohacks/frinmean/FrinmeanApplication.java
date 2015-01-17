package de.radiohacks.frinmean;

import android.app.Application;
import android.content.Context;

import java.io.InputStream;

/**
 * Created by thomas on 13.01.15.
 */
public class FrinmeanApplication extends Application {

    private static Context context;

    public static Context getAppContext() {
        return FrinmeanApplication.context;
    }

    public static InputStream loadCertAsInputStream() {
        return FrinmeanApplication.context.getResources().openRawResource(
                R.raw.dc6ri_de_x_509_inkl_ausstellern);
    }

    @Override
    public void onCreate() {
        super.onCreate();
        FrinmeanApplication.context = getApplicationContext();
    }

}
