package de.radiohacks.frinmean;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import org.simpleframework.xml.Serializer;
import org.simpleframework.xml.core.Persister;

import java.io.Reader;
import java.io.StringReader;

import de.radiohacks.frinmean.adapters.SyncUtils;
import de.radiohacks.frinmean.model.OutAuthenticate;
import de.radiohacks.frinmean.service.CustomExceptionHandler;
import de.radiohacks.frinmean.service.ErrorHelper;
import de.radiohacks.frinmean.service.MeBaService;


public class StartActivity extends Activity {

    private static final String TAG = StartActivity.class.getSimpleName();
    private String username;
    private String password;
    private String directory;
    private int syncFreq;
    private String server = "NULL";
    private int port = 80;
    private boolean https = true;
    private String CommunicationURL;
    private StartReceiver mStartReceiver = new StartReceiver();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.d(TAG, "start onCreate");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_start);

        getPreferenceInfo();
        if (directory.equalsIgnoreCase("NULL")) {
            if (!(Thread.getDefaultUncaughtExceptionHandler() instanceof CustomExceptionHandler)) {
                Thread.setDefaultUncaughtExceptionHandler(new CustomExceptionHandler(Environment.getExternalStorageDirectory().toString()));
            }
        } else {
            if (!(Thread.getDefaultUncaughtExceptionHandler() instanceof CustomExceptionHandler)) {
                Thread.setDefaultUncaughtExceptionHandler(new CustomExceptionHandler(directory));
            }
        }

        if (server.equalsIgnoreCase("NULL")) {
            Toast.makeText(StartActivity.this, this.getString(R.string.no_server_given), Toast.LENGTH_SHORT).show();
        } else {
            if (username.equalsIgnoreCase("NULL")
                    || password.equalsIgnoreCase("NULL")) {
                Toast.makeText(StartActivity.this, this.getString(R.string.no_user_or_password_given), Toast.LENGTH_SHORT).show();
            } else {
                /*buildServerURL();
                AuthenticateLoader loadFeedData = new AuthenticateLoader();
                loadFeedData.execute(CommunicationURL + "user/authenticate"); */
                IntentFilter statusIntentFilter = new IntentFilter(
                        Constants.BROADCAST_AUTHENTICATE);
                // Sets the filter's category to DEFAULT
                statusIntentFilter.addCategory(Intent.CATEGORY_DEFAULT);

                mStartReceiver = new StartReceiver();

                // Registers the DownloadStateReceiver and its intent filters
                LocalBroadcastManager.getInstance(this).registerReceiver(
                        mStartReceiver,
                        statusIntentFilter);

                Intent authIntent = new Intent(StartActivity.this, MeBaService.class);
                authIntent.setAction(Constants.ACTION_AUTHENTICATE);
                startService(authIntent);
            }
        }
        Log.d(TAG, "end onCreate");
    }

    protected void getPreferenceInfo() {
        Log.d(TAG, "start getPreferenceInfo");
        SharedPreferences sharedPrefs = PreferenceManager
                .getDefaultSharedPreferences(this);

        this.server = sharedPrefs.getString(Constants.PrefServername, "NULL");
        this.https = sharedPrefs.getBoolean(Constants.PrefHTTPSCommunication, true);
        if (this.https) {
            this.port = Integer.parseInt(sharedPrefs.getString(Constants.PrefServerport, "443"));
        } else {
            this.port = Integer.parseInt(sharedPrefs.getString(Constants.PrefServerport, "80"));
        }
        this.username = sharedPrefs.getString(Constants.PrefUsername, "NULL");
        this.password = sharedPrefs.getString(Constants.PrefPassword, "NULL");
        this.syncFreq = Integer.parseInt(sharedPrefs.getString(Constants.PrefSyncfrequency, "-1"));
        this.directory = sharedPrefs.getString("prefDirectory", "NULL");
        Log.d(TAG, "end getPferefenceInfo");
    }

    protected void buildServerURL() {
        this.CommunicationURL = "";
        if (this.https) {
            this.CommunicationURL += "https://";
        } else {
            this.CommunicationURL += "http://";
        }
        this.CommunicationURL += server + ":" + port + "/frinmeba/";
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onPause() {
        /* if (mAuthenticateStateReceiver != null) {
            unregisterReceiver(mAuthenticateStateReceiver);
        } */
        super.onPause();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        Log.d(TAG, "start onCreateOptionMenu");
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.start, menu);
        Log.d(TAG, "end onCreateOptionMenu");
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        Log.d(TAG, "start onOptionsItemSelected");
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.

        switch (item.getItemId()) {
            case R.id.action_settings:
                Intent su = new Intent(StartActivity.this, SettingsActivity.class);
                startActivity(su);
                return true;
            case R.id.action_signup:
                Intent us = new Intent(StartActivity.this, SignUpActivity.class);
                startActivity(us);
                return true;
        }
        Log.d(TAG, "end onOptionsItemSelected");
        return super.onOptionsItemSelected(item);
    }

    public class StartReceiver extends BroadcastReceiver {

        private final String TAG = StartReceiver.class.getSimpleName();

        public StartReceiver() {
            super();

            // prevents instantiation by other packages.
        }

        /**
         * This method is called by the system when a broadcast Intent is matched by this class'
         * intent filters
         *
         * @param context An Android context
         * @param intent  The incoming broadcast Intent
         */
        @Override
        public void onReceive(Context context, Intent intent) {
            Log.d(TAG, "start onReceive");

            /*
             * Gets the status from the Intent's extended data, and chooses the appropriate action
             */
            if (intent.getAction().equalsIgnoreCase(Constants.BROADCAST_AUTHENTICATE)) {
                Log.d(TAG, "start broadcast " + Constants.BROADCAST_AUTHENTICATE);
                try {
                    String ret = intent.getStringExtra(Constants.BROADCAST_DATA);
                    Serializer serializer = new Persister();
                    Reader reader = new StringReader(ret);

                    OutAuthenticate res = serializer.read(OutAuthenticate.class, reader, false);
                    if (res == null) {
                        ErrorHelper eh = new ErrorHelper(StartActivity.this);
                        eh.CheckErrorText(Constants.ERROR_NO_CONNECTION_TO_SERVER);
                    } else {
                        if (res.getErrortext() != null && !res.getErrortext().isEmpty()) {
                            ErrorHelper eh = new ErrorHelper(StartActivity.this);
                            eh.CheckErrorText(res.getErrortext());
                        } else {
                            if (res.getAuthenticated() != null && !res.getAuthenticated().isEmpty()) {
                                if (res.getAuthenticated().equalsIgnoreCase(Constants.AUTHENTICATE_TRUE)) {
                                    // Create Acount if needed
                                    SyncUtils.CreateSyncAccount(StartActivity.this);
                                    if (syncFreq != -1) {
                                        SyncUtils.ChangeSyncFreq(syncFreq);
                                        //SyncUtils.ChangeSyncFreq(1);
                                    } else {
                                        // set Default to 1 hour
                                        SyncUtils.ChangeSyncFreq(60);
                                    }
                                    SyncUtils.TriggerRefresh();
                                    Intent startchat = new Intent(StartActivity.this, ChatActivity.class);
                                    startchat.putExtra(Constants.USERID, res.getUserID());
                                    startchat.putExtra(Constants.PrefSyncfrequency, syncFreq);
                                    startchat.putExtra(Constants.CHAT_ACTIVITY_MODE, Constants.CHAT_ACTIVITY_FULL);
                                    startActivity(startchat);
                                    StartActivity.this.finish();
                                }
                            }
                        }
                    }

                } catch (Exception e) {
                    e.printStackTrace();
                }
                Log.d(TAG, "end broadcast " + Constants.BROADCAST_AUTHENTICATE);
            }
        }
    }


    /* private class AuthenticateLoader extends AsyncTask<String, Void, OutAuthenticate> {
        private final ProgressDialog dialog = new ProgressDialog(StartActivity.this);
        private final String TAG = AuthenticateLoader.class.getSimpleName();

        @Override
        protected void onPostExecute(OutAuthenticate result) {
            Log.d(TAG, "start onPostExecute");
            super.onPostExecute(result);
            dialog.dismiss();

            if (result == null) {
                ErrorHelper eh = new ErrorHelper(StartActivity.this);
                eh.CheckErrorText(Constants.ERROR_NO_CONNECTION_TO_SERVER);
            } else {
                if (result.getErrortext() != null && !result.getErrortext().isEmpty()) {
                    ErrorHelper eh = new ErrorHelper(StartActivity.this);
                    eh.CheckErrorText(result.getErrortext());
                } else {
                    if (result.getAuthenticated() != null && result.getAuthenticated().equalsIgnoreCase("TRUE")) {
                        int uid = result.getUserID();

                        SharedPreferences shP = PreferenceManager
                                .getDefaultSharedPreferences(StartActivity.this);
                        SharedPreferences.Editor ed = shP.edit();
                        ed.putString("prefUsername", username);
                        ed.putString("prefPassword", password);
                        ed.putInt("prefUserID", result.getUserID());
                        ed.commit();

                        Intent startchat = new Intent(StartActivity.this, ChatActivity.class);
                        startchat.putExtra(Constants.USERID, uid);
                        startchat.putExtra(Constants.PrefSyncfrequency, syncFreq);
                        startActivity(startchat);
                        StartActivity.this.finish();
                    }
                }
            }
            Log.d(TAG, "end onPostExecute");
        }

        @Override
        protected void onPreExecute() {
            Log.d(TAG, "start onPreExecute");
            super.onPreExecute();
            dialog.setMessage("Authenticate User...");
            dialog.show();
            Log.d(TAG, "end onPreExecute");
        }

        @Override
        protected OutAuthenticate doInBackground(String... params) {
            Log.d(TAG, "start doInBackground");

            RestFunctions rf = new RestFunctions();
            return rf.authenticate(username, password);
        }
    } */
}