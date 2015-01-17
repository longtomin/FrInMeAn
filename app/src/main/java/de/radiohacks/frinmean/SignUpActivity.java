package de.radiohacks.frinmean;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import org.simpleframework.xml.Serializer;
import org.simpleframework.xml.core.Persister;

import java.io.Reader;
import java.io.StringReader;
import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import de.radiohacks.frinmean.model.OutSignUp;
import de.radiohacks.frinmean.service.ErrorHelper;
import de.radiohacks.frinmean.service.RestClient;

public class SignUpActivity extends Activity {

    private static final String TAG = SignUpActivity.class.getSimpleName();
    private EditText usernameText;
    private EditText passwordText;
    private EditText passwordAgainText;
    private EditText eMailText;
    private String username;
    private String password;
    private String email;
    private String server;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.d(TAG, "start onCreate");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);
        setTitle(R.string.sign_up);

        getPreferenceInfo();
        if (!server.equalsIgnoreCase("NULL")) {

            Button signUpButton = (Button) findViewById(R.id.signUp);
            Button cancelButton = (Button) findViewById(R.id.cancel_signUp);
            usernameText = (EditText) findViewById(R.id.userName);
            passwordText = (EditText) findViewById(R.id.password);
            passwordAgainText = (EditText) findViewById(R.id.passwordAgain);
            eMailText = (EditText) findViewById(R.id.email);

            signUpButton.setOnClickListener(new View.OnClickListener() {
                public void onClick(View arg0) {
                    if (usernameText.length() > 0 &&
                            passwordText.length() > 0 &&
                            passwordAgainText.length() > 0 &&
                            eMailText.length() > 0
                            ) {
                        if (passwordText.getText().toString().equals(passwordAgainText.getText().toString())) {
                            username = usernameText.getText().toString();
                            password = hashPassword(passwordText.getText().toString());
                            // password = passwordText.getText().toString();
                            email = eMailText.getText().toString();

                            SignUpLoader loadFeedData = new SignUpLoader();

                            if (!server.endsWith("/")) {
                                loadFeedData.execute(server + "/user/signup");
                            } else {
                                loadFeedData.execute(server + "user/signup");
                            }

                        } else {
                            Toast.makeText(getApplicationContext(), R.string.signup_type_same_password_in_password_fields, Toast.LENGTH_LONG).show();
                        }

                    } else {
                        Toast.makeText(getApplicationContext(), R.string.signup_fill_all_fields, Toast.LENGTH_LONG).show();

                    }
                }
            });

            cancelButton.setOnClickListener(new View.OnClickListener() {
                public void onClick(View arg0) {
                    finish();
                }
            });
        } else {
            Toast.makeText(getApplicationContext(), R.string.no_server_given, Toast.LENGTH_LONG).show();
        }
        Log.d(TAG, "end onCreate");
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        Log.d(TAG, "start onCreateOptionsMenu");
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.sign_up, menu);
        Log.d(TAG, "end onCreateOptionsMenu");
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        Log.d(TAG, "start onOptionsItemSelected");
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            return true;
        }
        Log.d(TAG, "end onOptionsItemSelected");
        return super.onOptionsItemSelected(item);
    }

    protected void getPreferenceInfo() {
        Log.d(TAG, "start getPreferenceInfo");
        SharedPreferences sharedPrefs = PreferenceManager
                .getDefaultSharedPreferences(this);

        server = sharedPrefs.getString("prefServername", "NULL");
        username = sharedPrefs.getString("prefUsername", "NULL");
        password = sharedPrefs.getString("prefPassword", "NULL");
        Log.d(TAG, "end getPreferenceInfo ");
    }

    protected String hashPassword(String in) {
        Log.d(TAG, "start hashPassword");

        StringBuffer hexString = new StringBuffer();

        MessageDigest digest = null;
        try {
            digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(in.getBytes("UTF-8"));

            for (int i = 0; i < hash.length; i++) {

                String hex = Integer.toHexString(0xff & hash[i]);

                if (hex.length() == 1) {
                    hexString.append('0');
                }

                hexString.append(hex);
            }

        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }

        Log.d(TAG, "end hashPassword ");
        return hexString.toString();
    }

    private class SignUpLoader extends AsyncTask<String, Void, OutSignUp> {

        private final ProgressDialog dialog = new ProgressDialog(SignUpActivity.this);

        @Override
        protected void onPostExecute(OutSignUp result) {
            Log.d(TAG, "start onPostExecute");
            super.onPostExecute(result);
            dialog.dismiss();

            // TODO zuerst Result prüfen ob es null ist

            if (result.getErrortext() != null && !result.getErrortext().isEmpty()) {
                if (result.getErrortext().equalsIgnoreCase(Constants.ERROR_USER_ALREADY_EXISTS)) {
                    // TODO Sende Request wegen USER_ALREADY_EXISTS und authenthicate wegen Passwortprüfung durchführen.
                    if (result.getErrortext().equalsIgnoreCase(Constants.ERROR_USER_ALREADY_EXISTS)) {
                        SharedPreferences shP = PreferenceManager
                                .getDefaultSharedPreferences(SignUpActivity.this);
                        SharedPreferences.Editor ed = shP.edit();
                        ed.putString("prefUsername", username);
                        ed.putString("prefPassword", password);
                        // ed.putInt("prefUserID", result.getUserID());
                        ed.commit();
                        Toast.makeText(getApplicationContext(), R.string.signup_user_already_exists_saved, Toast.LENGTH_LONG).show();
                    } else {
                        ErrorHelper eh = new ErrorHelper(SignUpActivity.this);
                        eh.CheckErrorText(result.getErrortext());
                    }
                }

            } else {
                if (result.getSignUp() != null && !result.getSignUp().isEmpty()) {
                    if (result.getSignUp().equalsIgnoreCase("SUCCESSFUL")) {
                        SharedPreferences shP = PreferenceManager
                                .getDefaultSharedPreferences(SignUpActivity.this);
                        SharedPreferences.Editor ed = shP.edit();
                        ed.putString("prefUsername", username);
                        ed.putString("prefPassword", password);
                        ed.commit();
                    }
                }
            }
            Log.d(TAG, "end onPostExecute");
        }

        @Override
        protected void onPreExecute() {
            Log.d(TAG, "start onPreExecute ");
            super.onPreExecute();
            dialog.setMessage("Anmeldung wird durchgeführt...");
            dialog.show();
            Log.d(TAG, "end onPreExecute");
        }

        @Override
        protected OutSignUp doInBackground(String... params) {
            Log.d(TAG, "start doInBackground");
            OutSignUp res = null;

            RestClient rc = new RestClient(params[0]);
            rc.AddParam("username", username);
            rc.AddParam("password", password);
            rc.AddParam("email", email);
            try {
                String ret = rc.ExecuteRequestXML(rc.BevorExecuteGetQuery());
                Serializer serializer = new Persister();
                Reader reader = new StringReader(ret);

                res = serializer.read(OutSignUp.class, reader, false);
            } catch (Exception e) {
                e.printStackTrace();
            }
            Log.d(TAG, "end doInBackground");
            return res;
        }
    }
}
