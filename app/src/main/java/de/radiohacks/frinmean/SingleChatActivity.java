package de.radiohacks.frinmean;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.provider.MediaStore;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.PopupMenu;
import android.widget.Toast;

import org.apache.http.entity.mime.content.ByteArrayBody;
import org.simpleframework.xml.Serializer;
import org.simpleframework.xml.core.Persister;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.Reader;
import java.io.StringReader;

import de.radiohacks.frinmean.adapters.SingleChatAdapter;
import de.radiohacks.frinmean.model.OutFetchMessageFromChat;
import de.radiohacks.frinmean.model.OutInsertMessageIntoChat;
import de.radiohacks.frinmean.service.ErrorHelper;
import de.radiohacks.frinmean.service.LocalDBHandler;
import de.radiohacks.frinmean.service.MeBaService;


public class SingleChatActivity extends ActionBarActivity {

    private static final String TAG = SingleChatActivity.class.getSimpleName();
    private static final int CAPTURE_IMAGE_ACTIVITY_REQUEST_CODE = 100;
    private static final int CAPTURE_VIDEO_ACTIVITY_REQUEST_CODE = 200;
    private Uri fileUri;

    private ByteArrayOutputStream stream = new ByteArrayOutputStream();
    private SingleChatReceiver mSingleChatReceiver = new SingleChatReceiver();
    // private MeBaService mService;
    // private boolean mBound = false;
    private SingleChatAdapter mAdapter;
    private String username;
    private String password;
    private String directory;
    private int userid;
    private ByteArrayBody fileBody;
    private String server;
    private int ChatID;
    private String ChatName;
    private int OwningUserID;
    private String OwningUserName;
    private EditText Message;
    private LocalDBHandler ldb;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.d(TAG, "start onCreate");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_single_chat);

        Intent i = getIntent();
        ChatID = i.getIntExtra(Constants.CHATID, 0);
        ChatName = i.getStringExtra(Constants.CHATNAME);
        OwningUserName = i.getStringExtra(Constants.OWNINGUSERNAME);
        OwningUserID = i.getIntExtra(Constants.OWNINGUSERID, -1);

        getPreferenceInfo();

        ActionBar actionBar = getSupportActionBar();
//        actionBar.setDisplayHomeAsUpEnabled(true);

        actionBar.setTitle(ChatName);

//        android.app.ActionBar.LayoutParams lp = new android.app.ActionBar.LayoutParams(android.app.ActionBar.LayoutParams.WRAP_CONTENT, android.app.ActionBar.LayoutParams.WRAP_CONTENT, Gravity.RIGHT | Gravity.CENTER_VERTICAL);
        //View customNav = LayoutInflater.from(this).inflate(R.menu.single_chat_action, null); // layout which contains your button.
        //actionBar.setCustomView(customNav, lp);
        actionBar.setDisplayShowCustomEnabled(true);


        long time = System.currentTimeMillis() / 1000L;

        ldb = new LocalDBHandler(this);
        Cursor c = ldb.get(ChatID, time);
        mAdapter = new SingleChatAdapter(this, c, this.userid);

        ListView lv = (ListView) findViewById(R.id.singlechatlist);
        lv.setAdapter(mAdapter);

        IntentFilter statusIntentFilter = new IntentFilter(
                Constants.BROADCAST_GETMESSAGEFROMCHAT);
        statusIntentFilter.addAction(Constants.BROADCAST_ADDUSERTOCHAT);
        statusIntentFilter.addAction(Constants.BROADCAST_LISTUSER);
        statusIntentFilter.addAction(Constants.BROADCAST_REMOVEUSERFROMCHAT);
        statusIntentFilter.addAction(Constants.BROADCAST_SENDTEXTMESSAGE);
        statusIntentFilter.addAction(Constants.BROADCAST_INSERTMESSAGEINTOCHAT);

        // Sets the filter's category to DEFAULT
        statusIntentFilter.addCategory(Intent.CATEGORY_DEFAULT);

        mSingleChatReceiver = new SingleChatReceiver();

        // Registers the DownloadStateReceiver and its intent filters
        LocalBroadcastManager.getInstance(this).registerReceiver(
                mSingleChatReceiver,
                statusIntentFilter);

        //Start MeBaService
        Intent intentMyIntentService = new Intent(this, MeBaService.class);

        intentMyIntentService.setAction(Constants.ACTION_GETMESSAGEFROMCHAT);
        intentMyIntentService.putExtra(Constants.CHATNAME, ChatName);
        intentMyIntentService.putExtra(Constants.CHATID, ChatID);
        intentMyIntentService.putExtra(Constants.TIMESTAMP, time);

        startService(intentMyIntentService);

        Message = (EditText) findViewById(R.id.chatText);
        Button send = (Button) findViewById(R.id.buttonSend);
        send.setOnClickListener(new View.OnClickListener() {
            public void onClick(View arg0) {
                if (Message.getText().toString().length() > 0) {

                    Intent iSendTextMsgService = new Intent(SingleChatActivity.this, MeBaService.class);

                    iSendTextMsgService.setAction(Constants.ACTION_SENDTEXTMESSAGE);
                    iSendTextMsgService.putExtra(Constants.CHATNAME, ChatName);
                    iSendTextMsgService.putExtra(Constants.CHATID, ChatID);
                    String tmpmsg = Message.getText().toString();
                    iSendTextMsgService.putExtra(Constants.TEXTMESSAGE, tmpmsg);

                    startService(iSendTextMsgService);

                    Message.setText("");
                    Message.clearFocus();
                }
            }
        });
        Log.d(TAG, "end onCreate");
    }

    @Override
    protected void onResume() {
        super.onResume();
        IntentFilter infi = new IntentFilter(Constants.BROADCAST_GETMESSAGEFROMCHAT);
        infi.addAction(Constants.BROADCAST_ADDUSERTOCHAT);
        infi.addAction(Constants.BROADCAST_LISTUSER);
        infi.addAction(Constants.BROADCAST_REMOVEUSERFROMCHAT);
        infi.addAction(Constants.BROADCAST_SENDTEXTMESSAGE);
        infi.addAction(Constants.BROADCAST_INSERTMESSAGEINTOCHAT);

        registerReceiver(mSingleChatReceiver, infi);
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (mSingleChatReceiver != null) {
            unregisterReceiver(mSingleChatReceiver);
        }
    }

    protected void getPreferenceInfo() {
        Log.d(TAG, "start getPreferenceInfo");
        SharedPreferences sharedPrefs = PreferenceManager
                .getDefaultSharedPreferences(this);

        server = sharedPrefs.getString("prefServername", "NULL");
        username = sharedPrefs.getString("prefUsername", "NULL");
        password = sharedPrefs.getString("prefPassword", "NULL");
        userid = sharedPrefs.getInt("prefUserID", 0);
        directory = sharedPrefs.getString("prefDirectory", "NULL");
        Log.d(TAG, "end getPreferenceInfo");
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        Log.d(TAG, "start onActivityResult");
        if (requestCode == CAPTURE_IMAGE_ACTIVITY_REQUEST_CODE) {
            if (resultCode == RESULT_OK) {

                //Start MeBaService
                Intent picintent = new Intent(this, MeBaService.class);

                picintent.setAction(Constants.ACTION_SENDIMAGEMESSAGE);
                picintent.putExtra(Constants.IMAGELOCATION, data.getData());

                startService(picintent);

                /* InputStream stream1 = null;
                Bitmap bitmap = null;
                try {
                    stream1 = getContentResolver().openInputStream(data.getData());
                    bitmap = BitmapFactory.decodeStream(stream1);
                    stream.close();
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }

                ByteArrayOutputStream stream = new ByteArrayOutputStream();
                //bitmap.compress(Bitmap.CompressFormat.JPEG, 90, stream);

                byte[] byte_arr = stream.toByteArray();

                fileBody = new ByteArrayBody(byte_arr, data.getData().toString());

                if (!server.endsWith("/")) {
                    server += "/image/upload";
                } else {
                    server += "image/upload";
                } */
                // ImageUploader uploadPicData = new ImageUploader();
                // uploadPicData.execute(server);

                // Image captured and saved to fileUri specified in the Intent

            } else if (resultCode == RESULT_CANCELED) {
                // User cancelled the image capture
            } else {
                // Image capture failed, advise user
            }
        }

        if (requestCode == CAPTURE_VIDEO_ACTIVITY_REQUEST_CODE) {
            if (resultCode == RESULT_OK) {
                // Video captured and saved to fileUri specified in the Intent
                Toast.makeText(this, "Video saved to:\n" +
                        data.getData(), Toast.LENGTH_LONG).show();
            } else if (resultCode == RESULT_CANCELED) {
                // User cancelled the video capture
            } else {
                // Video capture failed, advise user
            }
        }
        Log.d(TAG, "end onActivityResult");
    }

    private void SendPicture() {
        Log.d(TAG, "start SendPicture");
        // create Intent to take a picture and return control to the calling application
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);

        // TODO Prefernce mit Speicherort holen und Bilder da ablegen
        File imagesFolder = new File(Environment.getExternalStorageDirectory(), "MyImages");
        imagesFolder.mkdirs(); // <----
        File image = new File(imagesFolder, "image_001.jpg");
        Uri uriSavedImage = Uri.fromFile(image);
        intent.putExtra(MediaStore.EXTRA_OUTPUT, uriSavedImage);


        intent.putExtra(MediaStore.EXTRA_OUTPUT, fileUri); // set the image file name

        // start the image capture Intent
        startActivityForResult(intent, CAPTURE_IMAGE_ACTIVITY_REQUEST_CODE);
        Log.d(TAG, "end SendPicture");
    }

    private void SendVideo() {
        Log.d(TAG, "start SendVideo");
        Intent intent = new Intent(MediaStore.ACTION_VIDEO_CAPTURE);

        // TODO Prefernce mit Speicherort holen und Bilder da ablegen
        // Name ist der Unix Timestamp um eindeutigkeit zu erzielen.
        File imagesFolder = new File(Environment.getExternalStorageDirectory(), "MyImages");
        imagesFolder.mkdirs(); // <----
        File image = new File(imagesFolder, "image_001.mp4");
        Uri uriSavedImage = Uri.fromFile(image);
        intent.putExtra(MediaStore.EXTRA_OUTPUT, uriSavedImage);


        intent.putExtra(MediaStore.EXTRA_OUTPUT, fileUri); // set the image file name

        // start the image capture Intent
        startActivityForResult(intent, CAPTURE_VIDEO_ACTIVITY_REQUEST_CODE);
        Log.d(TAG, "start SendVideo");
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        Log.d(TAG, "start onCreateOptionsMenu");
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.single_chat_action, menu);
        // View acbview = View.inflate(this, R.menu.single_chat_action, null);
        //getActionBar().setCustomView(acbview);
        Log.d(TAG, "end onCreateOptionsMenu");
        return true;
    }

    public void showPopup() {
        View menuItemView = findViewById(R.id.action_show_popup);
        PopupMenu popup = new PopupMenu(this, menuItemView);
        MenuInflater inflate = popup.getMenuInflater();
        inflate.inflate(R.menu.single_chat_show_popup, popup.getMenu());
        popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem menuItem) {
                Log.d(TAG, "start Popup onMenuItemClick");
                // Handle action bar item clicks here. The action bar will
                // automatically handle clicks on the Home/Up button, so long
                // as you specify a parent activity in AndroidManifest.xml.
                int id = menuItem.getItemId();

                switch (menuItem.getItemId()) {
                    case R.id.action_sendcontact:
//                openCreateChat();
                        return true;
                    case R.id.action_sendfile:
//                SendFileAction;
                        return true;
                    case R.id.action_sendpicture:
                        SendPicture();
                        return true;
                    case R.id.action_sendvideo:
                        SendVideo();
                        return true;
                }
                Log.d(TAG, "end Popup onMenuItemClick");
                // return super.onMenuItemClick(menuItem);
                return false;
            }
        });
        popup.show();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        Log.d(TAG, "start onOptionsItemSelected");
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        switch (item.getItemId()) {
            case R.id.action_show_popup:
                showPopup();
                return true;
            case R.id.action_sendcontact:
//                openCreateChat();
                return true;
            case R.id.action_sendfile:
//                SendFileAction;
                return true;
            case R.id.action_sendpicture:
                SendPicture();
                return true;
            case R.id.action_sendvideo:
                SendVideo();
                return true;
            case R.id.action_settings:
                Intent su = new Intent(SingleChatActivity.this, SettingsActivity.class);
                startActivity(su);
                return true;
        }
        Log.d(TAG, "end onOptionsItemSelected");
        return super.onOptionsItemSelected(item);
    }

/*    private OutFetchTextMessage FetchTextMessage(int ID) {
        Integer mid = ID;
        OutFetchTextMessage res = null;

        String fetchurl = server;
        if (!fetchurl.endsWith("/")) fetchurl += "/";

        fetchurl += "user/gettextmessage";


        RestClient rc = new RestClient(fetchurl);
        rc.AddParam("username", username);
        rc.AddParam("password", password);
        rc.AddParam("textmessageid", mid.toString());

        try {
            String ret = rc.ExecuteRequestXML(rc.BevorExecuteGet());
            Serializer serializer = new Persister();
            Reader reader = new StringReader(ret);

            res = serializer.read(OutFetchTextMessage.class, reader, false);
        } catch (Exception e) {
            e.printStackTrace();
        }

        return res;
    } */

    /* private class ImageUploader extends AsyncTask<String, Void, OutSendImageMessage> {

        @Override
        protected void onPostExecute(OutSendImageMessage result) {
            super.onPostExecute(result);

            if (result.getErrortext() != null && !result.getErrortext().isEmpty()) {
                ErrorHelper eh = new ErrorHelper(SingleChatActivity.this);
                eh.CheckErrorText(result.getErrortext());
            } else {
                if (result.getImageID() != 0) {
                    Toast.makeText(SingleChatActivity.this, "Image uploaded.\nID: " + result.getImageID().toString(), Toast.LENGTH_LONG).show();
                }
            }
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected OutSendImageMessage doInBackground(String... params) {
            OutSendImageMessage res = null;

            RestClient rc = new RestClient(params[0]);
            rc.reqEntity.addPart("file", fileBody);
            rc.AddParam("username", username);
            rc.AddParam("password", password);

            try {
                String ret = rc.ExecuteRequestXML(rc.BevorExecutePost());
                Serializer serializer = new Persister();
                Reader reader = new StringReader(ret);

                res = serializer.read(OutSendImageMessage.class, reader, false);
            } catch (Exception e) {
                e.printStackTrace();
            }
            return res;
        }
    } */

    private class SingleChatReceiver extends BroadcastReceiver {

        private SingleChatReceiver() {
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
            if (intent.getAction().equalsIgnoreCase(Constants.BROADCAST_GETMESSAGEFROMCHAT)) {
                try {
                    String ret = intent.getStringExtra(Constants.BROADCAST_DATA);
                    Serializer serializer = new Persister();
                    Reader reader = new StringReader(ret);

                    OutFetchMessageFromChat res = serializer.read(OutFetchMessageFromChat.class, reader, false);
                    if (res == null) {
                        ErrorHelper eh = new ErrorHelper(SingleChatActivity.this);
                        eh.CheckErrorText(Constants.NO_CONNECTION_TO_SERVER);
                    } else {
                        if (res.getErrortext() != null && !res.getErrortext().isEmpty()) {
                            ErrorHelper eh = new ErrorHelper(SingleChatActivity.this);
                            eh.CheckErrorText(res.getErrortext());
                        } else {
                            if (res.getMessage() != null && !res.getMessage().isEmpty()) {
                                mAdapter.notifyDataSetChanged();
                            }
                        }
                    }

                } catch (Exception e) {
                    e.printStackTrace();
                }
            } else if (intent.getAction().equalsIgnoreCase(Constants.BROADCAST_INSERTMESSAGEINTOCHAT)) {
                try {
                    String ret = intent.getStringExtra(Constants.BROADCAST_DATA);
                    Serializer serializer = new Persister();
                    Reader reader = new StringReader(ret);

                    OutInsertMessageIntoChat res = serializer.read(OutInsertMessageIntoChat.class, reader, false);
                    if (res == null) {
                        ErrorHelper eh = new ErrorHelper(SingleChatActivity.this);
                        eh.CheckErrorText(Constants.NO_CONNECTION_TO_SERVER);
                    } else {
                        if (res.getErrortext() != null && !res.getErrortext().isEmpty()) {
                            ErrorHelper eh = new ErrorHelper(SingleChatActivity.this);
                            eh.CheckErrorText(res.getErrortext());
                        } else {
                            if (res.getMessageID() > 0) {
                                long newtime = System.currentTimeMillis() / 1000L;
                                Cursor newc = ldb.get(ChatID, newtime);
                                mAdapter.changeCursor(newc);
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
