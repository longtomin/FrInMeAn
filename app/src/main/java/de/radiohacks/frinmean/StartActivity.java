/*
 * Copyright © 2015, Thomas Schreiner, thomas1.schreiner@googlemail.com
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * 1. Redistributions of source code must retain the above copyright notice, this
 *    list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR
 * ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 *
 * The views and conclusions contained in the software and documentation are those
 * of the authors and should not be interpreted as representing official policies,
 * either expressed or implied, of the FreeBSD Project.
 */

package de.radiohacks.frinmean;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import java.io.File;

import de.radiohacks.frinmean.adapters.SyncUtils;
import de.radiohacks.frinmean.service.CustomExceptionHandler;
import de.radiohacks.frinmean.service.MeBaService;


public class StartActivity extends Activity {

    private static final String TAG = StartActivity.class.getSimpleName();
    protected int port;
    private int userid;
    private String username;
    private String password;
    private String askedrestore;
    private String userAlreadyExists;
    private int syncFreq;
    private String server = "NULL";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.d(TAG, "start onCreate");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_start);

        getPreferenceInfo();
        String basedir = Environment.getExternalStorageDirectory().getAbsolutePath() + File.separator + Constants.BASEDIR;
        File baseFile = new File(basedir);
        if (!baseFile.exists()) {
            if (!baseFile.mkdirs()) {
                Log.e(TAG, "Base Directory creation failed");
            }
        }
        if (!(Thread.getDefaultUncaughtExceptionHandler() instanceof CustomExceptionHandler)) {
            Thread.setDefaultUncaughtExceptionHandler(new CustomExceptionHandler(baseFile.toString()));
        }

        if (server.equalsIgnoreCase("NULL")) {
            Toast.makeText(StartActivity.this, this.getString(R.string.no_server_given), Toast.LENGTH_SHORT).show();
        } else {
            if (username.equalsIgnoreCase("NULL")
                    || password.equalsIgnoreCase("NULL")) {
                Toast.makeText(StartActivity.this, this.getString(R.string.no_user_or_password_given), Toast.LENGTH_SHORT).show();
            } else {

                /* Alternative 1 mit dem AsyncTask
                buildServerURL();
                AuthenticateLoader loadFeedData = new AuthenticateLoader();
                loadFeedData.execute(CommunicationURL + "user/authenticate"); */

                /* Alternative 2 mit dem Service und dem Broadcast Receiver
                IntentFilter statusIntentFilter = new IntentFilter(
                        Constants.BROADCAST_AUTHENTICATE);
                statusIntentFilter.addCategory(Intent.CATEGORY_DEFAULT);

                StartReceiver mStartReceiver = new StartReceiver();

                LocalBroadcastManager.getInstance(this).registerReceiver(
                        mStartReceiver,
                        statusIntentFilter);

                Intent authIntent = new Intent(StartActivity.this, MeBaService.class);
                authIntent.setAction(Constants.ACTION_AUTHENTICATE);
                startService(authIntent); */

                SyncUtils.CreateSyncAccount(this);
                /* Alernative 3 keine Authentifizierung, einfach eiter zu den Chats, offline Modus möglich */
                /*Intent startchat = new Intent(StartActivity.this, ChatActivity.class);
                startchat.putExtra(Constants.USERID, this.userid);
                startchat.putExtra(Constants.CHAT_ACTIVITY_MODE, Constants.CHAT_ACTIVITY_FULL);
                startchat.putExtra(Constants.PrefSyncfrequency, syncFreq);
                startchat.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);*/
                Intent startchat = new Intent(StartActivity.this, ChatUserActivity.class);
                startchat.putExtra(Constants.USERID, this.userid);
                startchat.putExtra(Constants.CHAT_ACTIVITY_MODE, Constants.CHAT_ACTIVITY_FULL);
                startchat.putExtra(Constants.PrefSyncfrequency, syncFreq);
                startchat.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                startActivity(startchat);
                StartActivity.this.finish();
            }
        }
        Log.d(TAG, "end onCreate");
    }

    protected void getPreferenceInfo() {
        Log.d(TAG, "start getPreferenceInfo");
        SharedPreferences sharedPrefs = PreferenceManager
                .getDefaultSharedPreferences(this);

        this.server = sharedPrefs.getString(Constants.PrefServername, "NULL");
        boolean https = sharedPrefs.getBoolean(Constants.PrefHTTPSCommunication, true);
        if (https) {
            this.port = Integer.parseInt(sharedPrefs.getString(Constants.PrefServerport, "-1"));
        } else {
            this.port = Integer.parseInt(sharedPrefs.getString(Constants.PrefServerport, "-1"));
        }
        this.userid = sharedPrefs.getInt(Constants.PrefUserID, -1);
        this.username = sharedPrefs.getString(Constants.PrefUsername, "NULL");
        this.password = sharedPrefs.getString(Constants.PrefPassword, "NULL");
        this.syncFreq = Integer.parseInt(sharedPrefs.getString(Constants.PrefSyncfrequency, "-1"));
        this.askedrestore = sharedPrefs.getString(Constants.PrefAskedRestore, "NULL");
        this.userAlreadyExists = sharedPrefs.getString(Constants.PrefUserAlreadExists, "NULL");
        Log.d(TAG, "end getPferefenceInfo");
    }

    @Override
    protected void onResume() {
        super.onResume();
        getPreferenceInfo();
        if (this.askedrestore.equalsIgnoreCase("NULL")) {
            if (!this.server.equalsIgnoreCase("NULL") &&
                    this.port != -1 &&
                    !this.username.equalsIgnoreCase("NULL") &&
                    !this.password.equalsIgnoreCase("NULL") &&
                    !this.userAlreadyExists.equalsIgnoreCase("NULL")) {

                final int tmpuserid = this.userid;
                AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder.setTitle(R.string.title_restore_dialog);
                builder.setPositiveButton(this.getText(R.string.OK), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int id) {
                        Intent restoreintent = new Intent(StartActivity.this, MeBaService.class);
                        restoreintent.setAction(Constants.ACTION_REFRESH);
                        restoreintent.putExtra(Constants.TIMESTAMP, 1L);
                        startService(restoreintent);

                        //Start the Chat-Activity
                        Intent startchat = new Intent(StartActivity.this, ChatUserActivity.class);
                        startchat.putExtra(Constants.USERID, tmpuserid);
                        startchat.putExtra(Constants.CHAT_ACTIVITY_MODE, Constants.CHAT_ACTIVITY_FULL);
                        startchat.putExtra(Constants.PrefSyncfrequency, syncFreq);
                        startchat.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                        startActivity(startchat);
                        StartActivity.this.finish();
                    }
                }).setNegativeButton(this.getText(R.string.cancel), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int id) {
                        //Start the Chat-Activity
                        Intent startchat = new Intent(StartActivity.this, ChatUserActivity.class);
                        startchat.putExtra(Constants.USERID, tmpuserid);
                        startchat.putExtra(Constants.CHAT_ACTIVITY_MODE, Constants.CHAT_ACTIVITY_FULL);
                        startchat.putExtra(Constants.PrefSyncfrequency, syncFreq);
                        startchat.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                        startActivity(startchat);
                        StartActivity.this.finish();
                    }
                });
                AlertDialog dlg = builder.create();
                dlg.show();

                // Set the Preference that we habe already asked for the Restore
                SharedPreferences shP = PreferenceManager
                        .getDefaultSharedPreferences(StartActivity.this);
                SharedPreferences.Editor ed = shP.edit();
                ed.putString(Constants.PrefAskedRestore, "YES");
                ed.commit();
            }
        }
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

/*    public class StartReceiver extends BroadcastReceiver {

        private final String TAG = StartReceiver.class.getSimpleName();

        public StartReceiver() {
            super();

            // prevents instantiation by other packages.
        }

        @Override
        public void onReceive(Context context, Intent intent) {
            Log.d(TAG, "start onReceive");

            if (intent.getAction().equalsIgnoreCase(Constants.BROADCAST_AUTHENTICATE)) {
                Log.d(TAG, "start broadcast " + Constants.BROADCAST_AUTHENTICATE);
                try {
                    String ret = intent.getStringExtra(Constants.BROADCAST_DATA);
                    Serializer serializer = new Persister();
                    Reader reader = new StringReader(ret);

                    OAuth res = serializer.read(OAuth.class, reader, false);
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
                                    //if (syncFreq != -1) {
                                    //    SyncUtils.ChangeSyncFreq(syncFreq);
                                        //SyncUtils.ChangeSyncFreq(1);
                                    //} else {
                                        // set Default to 1 hour
                                    //    SyncUtils.ChangeSyncFreq(60);
                                    //}
                                    // SyncUtils.TriggerRefresh();
                                    Intent startchat = new Intent(StartActivity.this, ChatActivity.class);
                                    startchat.putExtra(Constants.USERID, res.getUserID());
                                    //startchat.putExtra(Constants.PrefSyncfrequency, syncFreq);
                                    startchat.putExtra(Constants.CHAT_ACTIVITY_MODE, Constants.CHAT_ACTIVITY_FULL);
                                    startchat.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
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
    } */


    /* private class AuthenticateLoader extends AsyncTask<String, Void, OAuth> {
        private final ProgressDialog dialog = new ProgressDialog(StartActivity.this);
        private final String TAG = AuthenticateLoader.class.getSimpleName();

        @Override
        protected void onPostExecute(OAuth result) {
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
            dialog.setMessage("Authenticate Benutzer...");
            dialog.show();
            Log.d(TAG, "end onPreExecute");
        }

        @Override
        protected OAuth doInBackground(String... params) {
            Log.d(TAG, "start doInBackground");

            RestFunctions rf = new RestFunctions();
            return rf.authenticate(username, password);
        }
    } */
}