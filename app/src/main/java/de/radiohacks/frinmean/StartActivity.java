package de.radiohacks.frinmean;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.content.LocalBroadcastManager;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import org.simpleframework.xml.Serializer;
import org.simpleframework.xml.core.Persister;

import java.io.Reader;
import java.io.StringReader;

import de.radiohacks.frinmean.model.OutAuthenticate;
import de.radiohacks.frinmean.service.ErrorHelper;
import de.radiohacks.frinmean.service.MeBaService;


public class StartActivity extends Activity {

    // boolean mBound = false;
    private AuthenticateStateReceiver mAuthenticateStateReceiver;
    // private MeBaService mService;
    private String username;
    private String password;
    private String server;
    /* private ServiceConnection mConnection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName className,
                                       IBinder service) {
            // We'vepublic static final String ACTION_LISTUSER = "listuser"; bound to LocalService, cast the IBinder and get LocalService instance
            MeBaService.LocalBinder binder = (MeBaService.LocalBinder) service;
            mService = binder.getService();
            mBound = true;
            if (server != null && !server.equalsIgnoreCase("NULL") && !server.isEmpty()) {
                mService.setServer(server);
            }
            if (username != null && !username.equalsIgnoreCase("NULL") && !username.isEmpty()) {
                mService.setUsername(username);
            }
            if (password != null && !password.equalsIgnoreCase("NULL") && !password.isEmpty()) {
                mService.setPassword(password);
            }
        }

        @Override
        public void onServiceDisconnected(ComponentName arg0) {
            mBound = false;
        }
    }; */
    // private String directory;
    // private boolean connect = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
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
                // The filter's action is BROADCAST_ACTION
                IntentFilter statusIntentFilter = new IntentFilter(
                        Constants.BROADCAST_AUTHENTICATE);

                // Sets the filter's category to DEFAULT
                statusIntentFilter.addCategory(Intent.CATEGORY_DEFAULT);

                mAuthenticateStateReceiver = new AuthenticateStateReceiver();

                // Registers the DownloadStateReceiver and its intent filters
                LocalBroadcastManager.getInstance(getApplicationContext()).registerReceiver(
                        mAuthenticateStateReceiver,
                        statusIntentFilter);

                //Start MeBaService
                Intent intentMyIntentService = new Intent(this, MeBaService.class);

                intentMyIntentService.setAction(Constants.ACTION_AUTHENTICATE);
                intentMyIntentService.putExtra(Constants.USERNAME, username);
                intentMyIntentService.putExtra(Constants.PASSWORD, password);

                startService(intentMyIntentService);

                // connect = true;
                /* if (!server.endsWith("/")) {
                    server += "/user/authenticate";
                } else {
                    server += "user/authenticate";
                }
                AuthenticateLoader loadFeedData = new AuthenticateLoader();
                loadFeedData.execute(server);

                if (!server.endsWith("/")) {
                    server += "/";
                }
                server += "image/downloadpreview/" + username + "/" + password + "/3";

                URL u = null;
                try {
                    u = new URL(server);
                } catch (MalformedURLException e) {
                    e.printStackTrace();
                }

                PhotoView pv = new PhotoView(this);
                pv.setImageURL(u, true, null);

                // ImageLoader loadFeedData = new ImageLoader();
                // loadFeedData.execute(server);
*/
            }
        }
    }

    protected void getPreferenceInfo() {
        SharedPreferences sharedPrefs = PreferenceManager
                .getDefaultSharedPreferences(this);

        server = sharedPrefs.getString("prefServername", "NULL");
        username = sharedPrefs.getString("prefUsername", "NULL");
        password = sharedPrefs.getString("prefPassword", "NULL");
        // directory = sharedPrefs.getString("prefDirectory", "NULL");
    }

    /* @Override
    protected void onStart() {
        super.onStart();
        // Bind to LocalService
        if (connect) {
            Intent intent = new Intent(this, MeBaService.class);
            bindService(intent, mConnection, Context.BIND_AUTO_CREATE);
        }
    } */

    /* @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);

        if (mService != null) {
            if (mService.isNetworkConnected()) {
                mService.startActionAuthenticate(this, username, password);
            }
        }
    } */

    /* @Override
    protected void onStop() {
        super.onStop();
        // Unbind from the service
        if (mBound) {
            unbindService(mConnection);
            mBound = false;
        }
    } */

    @Override
    protected void onResume() {
        super.onResume();
        registerReceiver(mAuthenticateStateReceiver, new IntentFilter(
                Constants.BROADCAST_AUTHENTICATE));
    }

    @Override
    protected void onPause() {
        super.onPause();
        unregisterReceiver(mAuthenticateStateReceiver);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.start, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
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
        return super.onOptionsItemSelected(item);
    }

    /* private class AuthenticateLoader extends AsyncTask<String, Void, OutAuthenticate> {
        private final ProgressDialog dialog = new ProgressDialog(StartActivity.this);

        @Override
        protected void onPostExecute(OutAuthenticate result) {
            super.onPostExecute(result);
            dialog.dismiss();

            if (result == null) {
                ErrorHelper eh = new ErrorHelper(StartActivity.this);
                eh.CheckErrorText(Constants.NO_CONNECTION_TO_SERVER);
            } else {
                if (result.getErrortext() != null && !result.getErrortext().isEmpty()) {
                    ErrorHelper eh = new ErrorHelper(StartActivity.this);
                    eh.CheckErrorText(result.getErrortext());
                } else {
                    if (result.getAuthenticated() != null && result.getAuthenticated().equalsIgnoreCase("TRUE")) {
                        startActivity(new Intent(StartActivity.this, ChatActivity.class));
                        StartActivity.this.finish();
                    }
                }
            }
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            dialog.setMessage("Authenticate User...");
            dialog.show();
        }

        @Override
        protected OutAuthenticate doInBackground(String... params) {
            OutAuthenticate res = null;

            RestClient rc = new RestClient(params[0]);
            rc.AddParam("username", username);
            rc.AddParam("password", password);
            try {
                String ret = rc.ExecuteRequestXML(rc.BevorExecuteGet());
                Serializer serializer = new Persister();
                Reader reader = new StringReader(ret);

                res = serializer.read(OutAuthenticate.class, reader, false);
            } catch (Exception e) {
                e.printStackTrace();
            }
            return res;
        }
    } */

    /* private class ImageLoader extends AsyncTask<String, Void, Bitmap> {
        private final ProgressDialog dialog = new ProgressDialog(StartActivity.this);
        private String filename;

        @Override
        protected void onPostExecute(Bitmap result) {
            super.onPostExecute(result);
            dialog.dismiss();

            if (result == null) {
                ErrorHelper eh = new ErrorHelper(StartActivity.this);
                eh.CheckErrorText(Constants.NO_CONNECTION_TO_SERVER);
            } else {
                OutputStream fOut = null;
                File file = new File(directory + "/" + filename);
                try {
                    fOut = new FileOutputStream(file);
                    result.compress(Bitmap.CompressFormat.JPEG, 85, fOut);
                    fOut.flush();
                    fOut.close();

                    MediaStore.Images.Media.insertImage(getContentResolver(), file.getAbsolutePath(), file.getName(), file.getName());
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            dialog.setMessage("Loading Image...");
            dialog.show();
        }

        @Override
        protected Bitmap doInBackground(String... params) {
            Bitmap ret = null;

            RestClient rc = new RestClient(params[0]);
            rc.setSaveDirectory(directory + "/images/preview/");
            rc.AddHeader("Accept", "image/jpeg");
            try {
                ret = rc.ExecuteRequestImage(rc.BevorExecuteGet());
                filename = rc.getFilename();
            } catch (Exception e) {
                e.printStackTrace();
            }
            return ret;
        }
    } */

    private class AuthenticateStateReceiver extends BroadcastReceiver {

        private AuthenticateStateReceiver() {
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

            /*
             * Gets the status from the Intent's extended data, and chooses the appropriate action
             */
            if (intent.getAction().equalsIgnoreCase(Constants.BROADCAST_AUTHENTICATE)) {
                try {
                    String ret = intent.getStringExtra(Constants.BROADCAST_DATA);
                    Serializer serializer = new Persister();
                    Reader reader = new StringReader(ret);

                    OutAuthenticate res = serializer.read(OutAuthenticate.class, reader, false);
                    if (res == null) {
                        ErrorHelper eh = new ErrorHelper(StartActivity.this);
                        eh.CheckErrorText(Constants.NO_CONNECTION_TO_SERVER);
                    } else {
                        if (res.getErrortext() != null && !res.getErrortext().isEmpty()) {
                            ErrorHelper eh = new ErrorHelper(StartActivity.this);
                            eh.CheckErrorText(res.getErrortext());
                        } else {
                            if (res.getAuthenticated() != null && res.getAuthenticated().equalsIgnoreCase("TRUE")) {
                                startActivity(new Intent(StartActivity.this, ChatActivity.class));
                                StartActivity.this.finish();
                            }
                        }
                    }

                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }
}