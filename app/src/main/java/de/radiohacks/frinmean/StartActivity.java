package de.radiohacks.frinmean;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import de.radiohacks.frinmean.model.OutAuthenticate;
import de.radiohacks.frinmean.service.ErrorHelper;
import de.radiohacks.frinmean.service.RestFunctions;


public class StartActivity extends Activity {

    private static final String TAG = StartActivity.class.getSimpleName();
    private String username;
    private String password;
    private String server = "NULL";
    private int port = 80;
    private boolean https = true;
    private String CommunicationURL;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.d(TAG, "start onCreate");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_start);

        getPreferenceInfo();

        if (server.equalsIgnoreCase("NULL")) {
            Toast.makeText(StartActivity.this, this.getString(R.string.no_server_given), Toast.LENGTH_SHORT).show();
        } else {
            if (username.equalsIgnoreCase("NULL")
                    || password.equalsIgnoreCase("NULL")) {
                Toast.makeText(StartActivity.this, this.getString(R.string.no_user_or_password_given), Toast.LENGTH_SHORT).show();
            } else {
                buildServerURL();
                AuthenticateLoader loadFeedData = new AuthenticateLoader();
                loadFeedData.execute(CommunicationURL + "user/authenticate");
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

    private class AuthenticateLoader extends AsyncTask<String, Void, OutAuthenticate> {
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
                        Intent startchat = new Intent(StartActivity.this, ChatActivity.class);
                        startchat.putExtra(Constants.USERID, uid);
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

            /* RestClient rc = new RestClient(params[0], https, port);
            rc.AddParam("username", username);
            rc.AddParam("password", password);
            try {
                String ret = rc.testDirect();
                String ret = rc.ExecuteRequestXML(rc.BevorExecuteGetQuery());
                Serializer serializer = new Persister();
                Reader reader = new StringReader(ret);

                res = serializer.read(OutAuthenticate.class, reader, false);
            } catch (Exception e) {
                e.printStackTrace();
            }
            Log.d(TAG, "end doInBackground");
            return res; */
        }
    }
}