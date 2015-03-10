package de.radiohacks.frinmean;

import android.app.AlertDialog;
import android.app.LoaderManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.CursorLoader;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.Loader;
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
import android.webkit.MimeTypeMap;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.PopupMenu;
import android.widget.Toast;

import org.simpleframework.xml.Serializer;
import org.simpleframework.xml.core.Persister;

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
import de.radiohacks.frinmean.providers.FrinmeanContentProvider;
import de.radiohacks.frinmean.service.CustomExceptionHandler;
import de.radiohacks.frinmean.service.ErrorHelper;
import de.radiohacks.frinmean.service.MeBaService;


public class SingleChatActivity extends ActionBarActivity implements
        LoaderManager.LoaderCallbacks<Cursor> {

    private static final String TAG = SingleChatActivity.class.getSimpleName();
    private static final int CAPTURE_IMAGE_ACTIVITY_REQUEST_CODE = 100;
    private static final int CAPTURE_VIDEO_ACTIVITY_REQUEST_CODE = 200;
    private static final int SELECT_IMAGE_ACTIVITY_REQUEST_CODE = 300;
    private static final int MESSAGE_LOADER_ID = 1000;

    private SingleChatReceiver mSingleChatReceiver = new SingleChatReceiver();
    private SingleChatAdapter mAdapter;
    private String username;
    private String directory;
    private int userid;
    private File m_imagefromcamera;
    private File m_videofromcamera;
    private int ChatID;
    private String ChatName;
    private int OwningUserID;
    private EditText Message;

    public static String getDataColumn(Context context, Uri uri, String selection,
                                       String[] selectionArgs) {
        Cursor cursor = null;
        final String column = "_data";
        final String[] projection = {
                column
        };
        try {
            cursor = context.getContentResolver().query(uri, projection, selection, selectionArgs,
                    null);
            if (cursor != null && cursor.moveToFirst()) {
                final int column_index = cursor.getColumnIndexOrThrow(column);
                return cursor.getString(column_index);
            }
        } finally {
            if (cursor != null)
                cursor.close();
        }
        return null;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.d(TAG, "start onCreate");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_single_chat);

        Intent i = getIntent();
        ChatID = i.getIntExtra(Constants.CHATID, 0);
        ChatName = i.getStringExtra(Constants.CHATNAME);
        OwningUserID = i.getIntExtra(Constants.OWNINGUSERID, -1);
        userid = i.getIntExtra(Constants.USERID, -1);

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

        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);

        actionBar.setTitle(ChatName);

        actionBar.setDisplayShowCustomEnabled(true);

        getLoaderManager().initLoader(MESSAGE_LOADER_ID, null, this);
        mAdapter = new SingleChatAdapter(this, null, userid, directory);


        ListView lv = (ListView) findViewById(R.id.singlechatlist);

        lv.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {

                // ToDo Dialog mit Loeschen oder weiterleiten
                return false;
            }
        });

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

        Message = (EditText) findViewById(R.id.chatText);
        Button send = (Button) findViewById(R.id.buttonSend);
        send.setOnClickListener(new View.OnClickListener() {
            public void onClick(View arg0) {
                if (Message.getText().toString().length() > 0) {

                    Intent iSendTextMsgService = new Intent(SingleChatActivity.this, MeBaService.class);

                    iSendTextMsgService.setAction(Constants.ACTION_SENDTEXTMESSAGE);
                    iSendTextMsgService.putExtra(Constants.CHATNAME, ChatName);
                    iSendTextMsgService.putExtra(Constants.CHATID, ChatID);
                    iSendTextMsgService.putExtra(Constants.USERID, userid);
                    String tmpmsg = Message.getText().toString();
                    iSendTextMsgService.putExtra(Constants.TEXTMESSAGE, tmpmsg);

                    startService(iSendTextMsgService);

                    Message.setText("");
                    Message.clearFocus();
                }
            }
        });

        /* Fuer Testzwecke um den Chat komplett neu zu laden */
        /* Intent picintent = new Intent(this, MeBaService.class);
        picintent.putExtra(Constants.CHATNAME, ChatName);
        picintent.putExtra(Constants.CHATID, ChatID);

        picintent.setAction(Constants.ACTION_FULLSYNC);
        startService(picintent); */

        Log.d(TAG, "end onCreate");
    }

    @Override
    protected void onResume() {
        Log.d(TAG, "start onResume");
        super.onResume();
        IntentFilter infi = new IntentFilter(Constants.BROADCAST_GETMESSAGEFROMCHAT);
        infi.addAction(Constants.BROADCAST_ADDUSERTOCHAT);
        infi.addAction(Constants.BROADCAST_LISTUSER);
        infi.addAction(Constants.BROADCAST_REMOVEUSERFROMCHAT);
        infi.addAction(Constants.BROADCAST_SENDTEXTMESSAGE);
        infi.addAction(Constants.BROADCAST_INSERTMESSAGEINTOCHAT);

        registerReceiver(mSingleChatReceiver, infi);
        Log.d(TAG, "end onResume");
    }

    @Override
    protected void onPause() {
        Log.d(TAG, "start onPause");
        super.onPause();
        if (mSingleChatReceiver != null) {
            unregisterReceiver(mSingleChatReceiver);
        }
        Log.d(TAG, "end onPause");
    }

    protected void getPreferenceInfo() {
        Log.d(TAG, "start getPreferenceInfo");
        SharedPreferences sharedPrefs = PreferenceManager
                .getDefaultSharedPreferences(this);

        //server = sharedPrefs.getString("prefServername", "NULL");
        username = sharedPrefs.getString("prefUsername", "NULL");
        //password = sharedPrefs.getString("prefPassword", "NULL");
        // userid = sharedPrefs.getInt("prefUserID", 0);
        directory = sharedPrefs.getString("prefDirectory", "NULL");
        Log.d(TAG, "end getPreferenceInfo");
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        Log.d(TAG, "start onActivityResult");

        if (resultCode == RESULT_OK) {

            //Start MeBaService
            Intent picintent = new Intent(this, MeBaService.class);

            picintent.putExtra(Constants.CHATID, ChatID);
            picintent.putExtra(Constants.USERID, userid);
            picintent.putExtra(Constants.CHATNAME, ChatName);

            if (data != null) {
                Uri selectedimage = data.getData();

                String mediaType = null;
                String filePath = null;
                if ("content".equalsIgnoreCase(selectedimage.getScheme())) {
                    filePath = getDataColumn(this, selectedimage, null, null);
                    String extension = MimeTypeMap.getFileExtensionFromUrl(filePath);
                    mediaType = MimeTypeMap.getSingleton().getMimeTypeFromExtension(extension);
                }

                if (mediaType.startsWith("image")) {
                    picintent.setAction(Constants.ACTION_SENDIMAGEMESSAGE);
                    picintent.putExtra(Constants.IMAGELOCATION, filePath);
                    startService(picintent);

                } else if (mediaType.startsWith("video")) {
                    picintent.setAction(Constants.ACTION_SENDVIDEOMESSAGE);
                    picintent.putExtra(Constants.VIDEOLOCATION, filePath);
                    startService(picintent);
                }
            }
        } else if (resultCode == RESULT_CANCELED) {
            // User cancelled the image capture
        } else {
            // Image capture failed, advise user
        }
        Log.d(TAG, "end onActivityResult");
    }

    private void SendCameraPicture() {
        Log.d(TAG, "start SendCameraPicture");

        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            startActivityForResult(takePictureIntent, CAPTURE_IMAGE_ACTIVITY_REQUEST_CODE);
        }
        Log.d(TAG, "end SendCameraPicture");
    }

    private void SendGalleryPicture() {
        Log.d(TAG, "start SendGalleryPicture");

        Intent photoPickerIntent = new Intent(Intent.ACTION_PICK);
        photoPickerIntent.setType("video/*, images/*");
        startActivityForResult(photoPickerIntent, SELECT_IMAGE_ACTIVITY_REQUEST_CODE);

        Log.d(TAG, "end SendGalleryPicture");
    }

    private void SendCameraVideo() {
        Log.d(TAG, "start SendCameraVideo");

        Intent takeVideoIntent = new Intent(MediaStore.ACTION_VIDEO_CAPTURE);
        if (takeVideoIntent.resolveActivity(getPackageManager()) != null) {
            startActivityForResult(takeVideoIntent, CAPTURE_VIDEO_ACTIVITY_REQUEST_CODE);
        }
        Log.d(TAG, "start SendCameraVideo");
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        Log.d(TAG, "start onCreateOptionsMenu");

        if (userid == OwningUserID) {
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
        Log.d(TAG, "start openEnterUserName");
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
        Log.d(TAG, "end openEnterUserName");
    }

    private void openSelectUserDialog(final OutListUser in) {
        Log.d(TAG, "start openSelectUserDialog");
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
        Log.d(TAG, "end openSelectUserDialog");
    }

    private void showPopup() {
        Log.d(TAG, "start showPopup");
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
                        SendGalleryPicture();
                        return true;
                    case R.id.action_sendvideo:
                        SendCameraVideo();
                        return true;
                    case R.id.action_sendcamera:
                        SendCameraPicture();
                        return true;

                }
                Log.d(TAG, "end Popup onMenuItemClick");
                return false;
            }
        });
        popup.show();
        Log.d(TAG, "end showPopup");
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        Log.d(TAG, "start onOptionsItemSelected");
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        // int id = item.getItemId();

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

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        long time = System.currentTimeMillis() / 1000L - (60 * 60 * 24 * 60);
        String select = "((" + Constants.T_MESSAGES_ChatID + " = " + String.valueOf(ChatID) + ") AND (" + Constants.T_MESSAGES_SendTimestamp + ">" + String.valueOf(time) + "))";
        String sort = Constants.T_MESSAGES_SendTimestamp + " ASC";

        return new CursorLoader(SingleChatActivity.this, FrinmeanContentProvider.MESSAES_CONTENT_URI,
                Constants.MESSAGES_DB_Columns, select, null, sort);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        switch (loader.getId()) {
            case MESSAGE_LOADER_ID:
                // The asynchronous load is complete and the data
                // is now available for use. Only now can we associate
                // the queried Cursor with the SimpleCursorAdapter.
                mAdapter.swapCursor(data);
                break;
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        mAdapter.swapCursor(null);
    }

    public class SingleChatReceiver extends BroadcastReceiver {

        private final String TAG = SingleChatReceiver.class.getSimpleName();

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
            Log.d(TAG, "start onReceive");

            /*
             * Gets the status from the Intent's extended data, and chooses the appropriate action
             */
            if (intent.getAction().equalsIgnoreCase(Constants.BROADCAST_GETMESSAGEFROMCHAT)) {
                Log.d(TAG, "start broadcast " + Constants.BROADCAST_GETMESSAGEFROMCHAT);
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
                                getLoaderManager().restartLoader(MESSAGE_LOADER_ID, null, SingleChatActivity.this);
                                //long newtime = System.currentTimeMillis() / 1000L;
                                //Cursor newc = ldb.get(ChatID, newtime);
                                //stopManagingCursor(mAdapter.getCursor());
                                //mAdapter.swapCursor(newc);
                            }
                        }
                    }

                } catch (Exception e) {
                    e.printStackTrace();
                }
                Log.d(TAG, "end broadcast " + Constants.BROADCAST_GETMESSAGEFROMCHAT);
            } else if (intent.getAction().equalsIgnoreCase(Constants.BROADCAST_INSERTMESSAGEINTOCHAT)) {
                Log.d(TAG, "start broadcast " + Constants.BROADCAST_INSERTMESSAGEINTOCHAT);
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
                                getLoaderManager().restartLoader(MESSAGE_LOADER_ID, null, SingleChatActivity.this);
                                //long newtime = System.currentTimeMillis() / 1000L;
                                //Cursor newc = ldb.get(ChatID, newtime);
                                //mAdapter.swapCursor(newc);
                            }
                        }
                    }

                } catch (Exception e) {
                    e.printStackTrace();
                }
                Log.d(TAG, "end broadcast " + Constants.BROADCAST_INSERTMESSAGEINTOCHAT);
            } else if (intent.getAction().equalsIgnoreCase(Constants.BROADCAST_LISTUSER)) {
                Log.d(TAG, "start broadcast " + Constants.BROADCAST_LISTUSER);
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
                Log.d(TAG, "end broadcast " + Constants.BROADCAST_LISTUSER);
            } else if (intent.getAction().equalsIgnoreCase(Constants.BROADCAST_USERADDEDTOCHAT)) {
                Log.d(TAG, "start broadcast " + Constants.BROADCAST_USERADDEDTOCHAT);
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
                Log.d(TAG, "end broadcast " + Constants.BROADCAST_USERADDEDTOCHAT);
            }
        }
    }
}
