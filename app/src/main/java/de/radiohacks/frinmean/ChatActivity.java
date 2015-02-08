package de.radiohacks.frinmean;

import android.os.Bundle;
import android.support.v4.app.FragmentActivity;

import de.radiohacks.frinmean.service.CustomExceptionHandler;


public class ChatActivity extends FragmentActivity {

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        if (!(Thread.getDefaultUncaughtExceptionHandler() instanceof CustomExceptionHandler)) {
            Thread.setDefaultUncaughtExceptionHandler(new CustomExceptionHandler(this));
        }
    }
}
