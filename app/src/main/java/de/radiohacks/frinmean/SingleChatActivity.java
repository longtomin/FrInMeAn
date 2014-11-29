package de.radiohacks.frinmean;

import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
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
import java.util.ArrayList;
import java.util.List;

import de.radiohacks.frinmean.adapters.SingleChatAdapter;
import de.radiohacks.frinmean.model.OutAddUserToChat;
import de.radiohacks.frinmean.model.OutFetchMessageFromChat;
import de.radiohacks.frinmean.model.OutInsertMessageIntoChat;
import de.radiohacks.frinmean.model.OutListUser;
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

        actionBar.setDisplayShowCustomEnabled(true);


        long time = System.currentTimeMillis() / 1000L - (60 * 60 * 24 * 7);

        ldb = new LocalDBHandler(this);
        Cursor c = ldb.get(ChatID, time);
        mAdapter = new SingleChatAdapter(this, c, this.userid, directory);

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
                picintent.putExtra(Constants.CHATID, ChatID);
                picintent.putExtra(Constants.CHATNAME, ChatName);

                String[] filePathColumn = {MediaStore.Images.Media.DATA};
                Cursor cursor = this.getContentResolver().query(data.getData(), filePathColumn, null, null, null);
                cursor.moveToFirst();
                int columnIndex = cursor.getColumnIndex(filePathColumn[0]);
                String filePath = cursor.getString(columnIndex);
                cursor.close();

                picintent.putExtra(Constants.IMAGELOCATION, filePath);

                startService(picintent);
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

        if (userid == OwningUserID ) {
            // Inflate OwningUserMenu; this adds items to the action bar if it is present.
            getMenuInflater().inflate(R.menu.single_chat_action_owninguser, menu);
        } else {
            // Inflate menu for Participants; this adds items to the action bar if it is present.
            getMenuInflater().inflate(R.menu.single_chat_action_participant, menu);
        }
        Log.d(TAG, "end onCreateOptionsMenu");
        return true;
    }

    private void openEnterUserName() {
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(SingleChatActivity.this);

        alertDialogBuilder.setTitle(this.getTitle());
        alertDialogBuilder.setMessage(R.string.username);

        final EditText input = new EditText(this);
        alertDialogBuilder.setView(input);


        alertDialogBuilder.setPositiveButton(R.string.action_searchuser
                , new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {

                String searchStr = input.getText().toString();
                Intent listuserIntent = new Intent(SingleChatActivity.this, MeBaService.class);
                listuserIntent.setAction(Constants.ACTION_LISTUSER);
                listuserIntent.putExtra(Constants.SEARCH, searchStr);
                startService(listuserIntent);
            }
        });
        alertDialogBuilder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                // cancel the alert box and put a Toast to the user
                dialog.cancel();
            }
        });

        AlertDialog alertDialog = alertDialogBuilder.create();
        // show alert
        alertDialog.show();
    }

    private void openSelectUserDialog(final OutListUser in) {
        final CharSequence users[];

        users = new String[in.getUser().size()];

        for (int i = 0; i < in.getUser().size(); i++) {
            users[i] = String.valueOf(in.getUser().get(i).getUserID()) + " | " + in.getUser().get(i).getUsername() + " | " + in.getUser().get(i).getEmail();
        }

        final List<Integer> selectedItems = new ArrayList<Integer>(1);

        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(SingleChatActivity.this);

        alertDialogBuilder.setTitle(R.string.option_adduser);
        alertDialogBuilder.setMultiChoiceItems(users, null, new DialogInterface.OnMultiChoiceClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which, boolean isChecked) {
                if (isChecked) {
                    selectedItems.add(which);
                } else if (selectedItems.contains(which)) {
                    // Else, if the item is already in the array, remove it
                    // write your code when user Uchecked the checkbox
                    selectedItems.remove(Integer.valueOf(which));
                }
            }
        }).setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int id) {
                for (int j = 0; j < selectedItems.size(); j++) {
                    CharSequence tmpuser = users[selectedItems.get(j)];
                    String stmpuser = tmpuser.toString();
                    String stmpuid = stmpuser.substring(0, stmpuser.indexOf("|"));
                    int tmpuserid = Integer.parseInt(stmpuid.trim());

                    Intent adduserIntent = new Intent(SingleChatActivity.this, MeBaService.class);
                    adduserIntent.setAction(Constants.ACTION_ADDUSERTOCHAT);
                    adduserIntent.putExtra(Constants.CHATID, ChatID);
                    adduserIntent.putExtra(Constants.USERID, tmpuserid);
                    startService(adduserIntent);
                }
            }
        })
                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int id) {
                        //  Your code when user clicked on Cancel

                    }
                });

        AlertDialog alertDialog = alertDialogBuilder.create();
        // show alert
        alertDialog.show();
    }

    private void showPopup() {
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
            case R.id.option_adduser:
                openEnterUserName();
                return true;
            case R.id.option_deletechat:
                return true;
            case R.id.option_removeuser:
                return true;
        }
        Log.d(TAG, "end onOptionsItemSelected");
        return super.onOptionsItemSelected(item);
    }

    public class SingleChatReceiver extends BroadcastReceiver {

        public SingleChatReceiver() {
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
                        eh.CheckErrorText(Constants.ERROR_NO_CONNECTION_TO_SERVER);
                    } else {
                        if (res.getErrortext() != null && !res.getErrortext().isEmpty()) {
                            ErrorHelper eh = new ErrorHelper(SingleChatActivity.this);
                            eh.CheckErrorText(res.getErrortext());
                        } else {
                            if (res.getMessage() != null && !res.getMessage().isEmpty()) {
                                long newtime = System.currentTimeMillis() / 1000L;
                                Cursor newc = ldb.get(ChatID, newtime);
                                mAdapter.changeCursor(newc);
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
                        eh.CheckErrorText(Constants.ERROR_NO_CONNECTION_TO_SERVER);
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
            } else if (intent.getAction().equalsIgnoreCase(Constants.BROADCAST_ADDUSERTOCHAT)) {
                try {
                    String ret = intent.getStringExtra(Constants.BROADCAST_DATA);
                    Serializer serializer = new Persister();
                    Reader reader = new StringReader(ret);

                    OutListUser res = serializer.read(OutListUser.class, reader, false);
                    if (res == null) {
                        ErrorHelper eh = new ErrorHelper(SingleChatActivity.this);
                        eh.CheckErrorText(Constants.ERROR_NO_CONNECTION_TO_SERVER);
                    } else {
                        if (res.getErrortext() != null && !res.getErrortext().isEmpty()) {
                            ErrorHelper eh = new ErrorHelper(SingleChatActivity.this);
                            eh.CheckErrorText(res.getErrortext());
                        } else {
                            openSelectUserDialog(res);
                        }
                    }

                } catch (Exception e) {
                    e.printStackTrace();
                }
            }  else if (intent.getAction().equalsIgnoreCase(Constants.BROADCAST_USERADDEDTOCHAT)) {
                try {
                    String ret = intent.getStringExtra(Constants.BROADCAST_DATA);
                    Serializer serializer = new Persister();
                    Reader reader = new StringReader(ret);

                    OutAddUserToChat res = serializer.read(OutAddUserToChat.class, reader, false);
                    if (res == null) {
                        ErrorHelper eh = new ErrorHelper(SingleChatActivity.this);
                        eh.CheckErrorText(Constants.ERROR_NO_CONNECTION_TO_SERVER);
                    } else {
                        if (res.getErrortext() != null && !res.getErrortext().isEmpty()) {
                            ErrorHelper eh = new ErrorHelper(SingleChatActivity.this);
                            eh.CheckErrorText(res.getErrortext());
                        } else {
                            if (res.getResult().equalsIgnoreCase(Constants.RESULT_USER_ADDED_TO_CHAT)) {
                                Toast.makeText(SingleChatActivity.this.getBaseContext(), getString(R.string.user_added_to_chat), Toast.LENGTH_SHORT).show();
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
