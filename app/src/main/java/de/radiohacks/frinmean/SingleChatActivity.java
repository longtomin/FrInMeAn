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
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.provider.MediaStore;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.InputMethodManager;
import android.webkit.MimeTypeMap;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.PopupMenu;
import android.widget.PopupWindow.OnDismissListener;
import android.widget.Toast;

import org.simpleframework.xml.Serializer;
import org.simpleframework.xml.core.Persister;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;

import de.radiohacks.frinmean.adapters.SingleChatAdapter;
import de.radiohacks.frinmean.emojicon.Emojicon;
import de.radiohacks.frinmean.emojicon.EmojiconEditText;
import de.radiohacks.frinmean.emojicon.EmojiconGridView;
import de.radiohacks.frinmean.emojicon.EmojiconsPopup;
import de.radiohacks.frinmean.modelshort.OAdUC;
import de.radiohacks.frinmean.modelshort.ODeCh;
import de.radiohacks.frinmean.modelshort.OFMFC;
import de.radiohacks.frinmean.modelshort.OIMIC;
import de.radiohacks.frinmean.modelshort.OLiUs;
import de.radiohacks.frinmean.providers.FrinmeanContentProvider;
import de.radiohacks.frinmean.providers.MediaConverter;
import de.radiohacks.frinmean.service.CustomExceptionHandler;
import de.radiohacks.frinmean.service.ErrorHelper;
import de.radiohacks.frinmean.service.MeBaService;

public class SingleChatActivity extends ActionBarActivity implements
        LoaderManager.LoaderCallbacks<Cursor> {

    private static final String TAG = SingleChatActivity.class.getSimpleName();
    private static final int CAPTURE_CONTENT = 100;
    private static final int REQUEST_USER_THUMBNAIL = 200;
    private static final int MESSAGE_LOADER_ID = 1000;

    private SingleChatReceiver mSingleChatReceiver = new SingleChatReceiver();
    private SingleChatAdapter mAdapter;
    //    private String directory;
    private int userid;
    private int ChatID;
    private int OwningUserID;
    private EmojiconEditText Message;
    private String icndir;

    private static String getDataColumn(Context context, Uri uri, String selection,
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
        String chatName = i.getStringExtra(Constants.CHATNAME);
        OwningUserID = i.getIntExtra(Constants.OWNINGUSERID, -1);
        userid = i.getIntExtra(Constants.USERID, -1);

        getPreferenceInfo();

        String basedir = Environment.getExternalStorageDirectory().getAbsolutePath() + File.separator + Constants.BASEDIR;
        File baseFile = new File(basedir);
        if (!baseFile.exists()) {
            if (!baseFile.mkdirs()) {
                Log.e(TAG, "Base Directory creation failed");
            }
        }
        icndir = basedir + File.separator + Constants.ICONDIR + File.separator;
        File icnFile = new File(icndir);
        if (!icnFile.exists()) {
            if (!icnFile.mkdir()) {
                Log.e(TAG, "Icon Directory creation failed");
            }
        }
        if (!(Thread.getDefaultUncaughtExceptionHandler() instanceof CustomExceptionHandler)) {
            Thread.setDefaultUncaughtExceptionHandler(new CustomExceptionHandler(baseFile.toString()));
        }

        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);

        actionBar.setTitle(chatName);

        actionBar.setDisplayShowCustomEnabled(true);

        getLoaderManager().initLoader(MESSAGE_LOADER_ID, null, this);
        mAdapter = new SingleChatAdapter(this, null, userid, ChatID);


        ListView lv = (ListView) findViewById(R.id.singlechatlist);

        lv.setAdapter(mAdapter);

        IntentFilter statusIntentFilter = new IntentFilter(
                Constants.BROADCAST_GETMESSAGEFROMCHAT);
        statusIntentFilter.addAction(Constants.BROADCAST_ADDUSERTOCHAT);
        statusIntentFilter.addAction(Constants.BROADCAST_LISTUSER);
        statusIntentFilter.addAction(Constants.BROADCAST_REMOVEUSERFROMCHAT);
        statusIntentFilter.addAction(Constants.BROADCAST_SENDTEXTMESSAGE);
        statusIntentFilter.addAction(Constants.BROADCAST_INSERTMESSAGEINTOCHAT);
        statusIntentFilter.addAction(Constants.BROADCAST_DELETECHAT);

        // Sets the filter's category to DEFAULT
        statusIntentFilter.addCategory(Intent.CATEGORY_DEFAULT);

        mSingleChatReceiver = new SingleChatReceiver();

        // Registers the DownloadStateReceiver and its intent filters
        LocalBroadcastManager.getInstance(this).registerReceiver(
                mSingleChatReceiver,
                statusIntentFilter);

        Message = (EmojiconEditText) findViewById(R.id.chatText);
        Button send = (Button) findViewById(R.id.buttonSend);
        send.setOnClickListener(new View.OnClickListener() {
            public void onClick(View arg0) {
                if (Message.getText().toString().length() > 0) {

                    Intent iSendTextMsgService = new Intent(SingleChatActivity.this, MeBaService.class);

                    iSendTextMsgService.setAction(Constants.ACTION_SENDTEXTMESSAGE);
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

        /* Now we have to set the ShowTimestamp for the messages in this chat */
        Intent iSetShowTime = new Intent(SingleChatActivity.this, MeBaService.class);

        iSetShowTime.setAction(Constants.ACTION_SETSHOWTIMESTAMP);
        iSetShowTime.putExtra(Constants.CHATID, ChatID);
        startService(iSetShowTime);

        final View rootView = findViewById(R.id.root_view);
        final ImageView emojiButton = (ImageView) findViewById(R.id.emoji_btn);

        // Give the topmost view of your activity layout hierarchy. This will be used to measure soft keyboard height
        final EmojiconsPopup popup = new EmojiconsPopup(rootView, this);

        //Will automatically set size according to the soft keyboard size
        popup.setSizeForSoftKeyboard();

        //If the emoji popup is dismissed, change emojiButton to smiley icon
        popup.setOnDismissListener(new OnDismissListener() {

            @Override
            public void onDismiss() {
                changeEmojiKeyboardIcon(emojiButton, R.drawable.smiley);
            }
        });

        //If the text keyboard closes, also dismiss the emoji popup
        popup.setOnSoftKeyboardOpenCloseListener(new EmojiconsPopup.OnSoftKeyboardOpenCloseListener() {

            @Override
            public void onKeyboardOpen(int keyBoardHeight) {

            }

            @Override
            public void onKeyboardClose() {
                if (popup.isShowing())
                    popup.dismiss();
            }
        });

        //On emoji clicked, add it to edittext
        popup.setOnEmojiconClickedListener(new EmojiconGridView.OnEmojiconClickedListener() {

            @Override
            public void onEmojiconClicked(Emojicon emojicon) {
                if (Message == null || emojicon == null) {
                    return;
                }

                int start = Message.getSelectionStart();
                int end = Message.getSelectionEnd();
                if (start < 0) {
                    Message.append(emojicon.getEmoji());
                } else {
                    Message.getText().replace(Math.min(start, end),
                            Math.max(start, end), emojicon.getEmoji(), 0,
                            emojicon.getEmoji().length());
                }
            }
        });

        //On backspace clicked, emulate the KEYCODE_DEL key event
        popup.setOnEmojiconBackspaceClickedListener(new EmojiconsPopup.OnEmojiconBackspaceClickedListener() {

            @Override
            public void onEmojiconBackspaceClicked(View v) {
                KeyEvent event = new KeyEvent(
                        0, 0, 0, KeyEvent.KEYCODE_DEL, 0, 0, 0, 0, KeyEvent.KEYCODE_ENDCALL);
                Message.dispatchKeyEvent(event);
            }
        });

        // To toggle between text keyboard and emoji keyboard keyboard(Popup)
        emojiButton.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {

                //If popup is not showing => emoji keyboard is not visible, we need to show it
                if (!popup.isShowing()) {

                    //If keyboard is visible, simply show the emoji popup
                    if (popup.isKeyBoardOpen()) {
                        popup.showAtBottom();
                        changeEmojiKeyboardIcon(emojiButton, R.drawable.ic_action_keyboard);
                    }

                    //else, open the text keyboard first and immediately after that show the emoji popup
                    else {
                        Message.setFocusableInTouchMode(true);
                        Message.requestFocus();
                        popup.showAtBottomPending();
                        final InputMethodManager inputMethodManager = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                        inputMethodManager.showSoftInput(Message, InputMethodManager.SHOW_IMPLICIT);
                        changeEmojiKeyboardIcon(emojiButton, R.drawable.ic_action_keyboard);
                    }
                }

                //If popup is showing, simply dismiss it to show the undelying text keyboard
                else {
                    popup.dismiss();
                }
            }
        });


        Log.d(TAG, "end onCreate");
    }

    private void changeEmojiKeyboardIcon(ImageView iconToBeChanged, int drawableResourceId) {
        iconToBeChanged.setImageResource(drawableResourceId);
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
        String username = sharedPrefs.getString(Constants.PrefUsername, "NULL");
        //password = sharedPrefs.getString("prefPassword", "NULL");
        // userid = sharedPrefs.getInt("prefUserID", 0);
        // directory = sharedPrefs.getString(Constants.PrefDirectory, "NULL");
        Log.d(TAG, "end getPreferenceInfo");
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        Log.d(TAG, "start onActivityResult");

        if (resultCode == RESULT_OK) {
            if (requestCode == REQUEST_USER_THUMBNAIL) {
                if (data != null) {
                    if (data.getExtras() != null) {
                        Bundle extras = data.getExtras();
                        Bitmap ownpic = extras.getParcelable("data");

                        File owniconfile = new File(icndir, "Chat" + String.valueOf(ChatID) + ".png");
                        FileOutputStream fOut = null;
                        try {
                            fOut = new FileOutputStream(owniconfile);
                            ownpic.compress(Bitmap.CompressFormat.PNG, 85, fOut);
                            fOut.flush();
                            fOut.close();
                        } catch (FileNotFoundException e) {
                            e.printStackTrace();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                        Intent picintent = new Intent(this, MeBaService.class);
                        picintent.setAction(Constants.ACTION_SENDCHATICON);
                        picintent.putExtra(Constants.IMAGELOCATION, owniconfile.getAbsolutePath());
                        picintent.putExtra(Constants.CHATID, ChatID);
                        startService(picintent);
                    }
                }
            } else if (requestCode == CAPTURE_CONTENT) {

                //Start MeBaService
                Intent picintent = new Intent(this, MeBaService.class);

                picintent.putExtra(Constants.CHATID, ChatID);
                picintent.putExtra(Constants.USERID, userid);

                if (data != null) {
                    Uri selectedimage = data.getData();

                    String mediaType = null;
                    String filePath = null;
                    if ("content".equalsIgnoreCase(selectedimage.getScheme())) {
                        filePath = getDataColumn(this, selectedimage, null, null);
                        String extension = MimeTypeMap.getFileExtensionFromUrl(filePath);
                        mediaType = MimeTypeMap.getSingleton().getMimeTypeFromExtension(extension.toLowerCase());
                    }

                    assert mediaType != null;
                    if (mediaType.startsWith("image")) {
                        String newfile = compressImage(filePath);
                        picintent.setAction(Constants.ACTION_SENDIMAGEMESSAGE);
                        picintent.putExtra(Constants.IMAGELOCATION, newfile);
                        startService(picintent);

                    } else if (mediaType.startsWith("video")) {
                        MediaConverter m = new MediaConverter();
                        try {
                            m.EncodeVideoToMp4();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                        picintent.setAction(Constants.ACTION_SENDVIDEOMESSAGE);
                        picintent.putExtra(Constants.VIDEOLOCATION, filePath);
                        startService(picintent);
                    }
                }
            }
        } else if (resultCode == RESULT_CANCELED) {
            // Benutzer cancelled the image capture
        } else {
            // Image capture failed, advise user
        }
        Log.d(TAG, "end onActivityResult");
    }

    private void SendCameraPicture() {
        Log.d(TAG, "start SendCameraPicture");

        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            startActivityForResult(takePictureIntent, CAPTURE_CONTENT);
        }
        Log.d(TAG, "end SendCameraPicture");
    }

    private void SendGalleryPicture() {
        Log.d(TAG, "start SendGalleryPicture");

        Intent photoPickerIntent = new Intent(Intent.ACTION_PICK);
        photoPickerIntent.setType("video/*, images/*");
        startActivityForResult(photoPickerIntent, CAPTURE_CONTENT);

        Log.d(TAG, "end SendGalleryPicture");
    }

    private void SendCameraVideo() {
        Log.d(TAG, "start SendCameraVideo");

        Intent takeVideoIntent = new Intent(MediaStore.ACTION_VIDEO_CAPTURE);
        if (takeVideoIntent.resolveActivity(getPackageManager()) != null) {
            startActivityForResult(takeVideoIntent, CAPTURE_CONTENT);
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

    private void openSelectUserDialog(final OLiUs in) {
        Log.d(TAG, "start openSelectUserDialog");
        final CharSequence users[];

        users = new String[in.getU().size()];

        for (int i = 0; i < in.getU().size(); i++) {
            users[i] = String.valueOf(in.getU().get(i).getUID()) + " | " + in.getU().get(i).getUN() + " | " + in.getU().get(i).getE();
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

    private void openDeleteChat(final int inChatID) {
        Log.d(TAG, "start openDeleteChat");

        final CharSequence[] items = {getText(R.string.delete_on_server), getText(R.string.delete_local_content)};
        // arraylist to keep the selected items
        final ArrayList seletedItems = new ArrayList();

        AlertDialog.Builder delbuilder = new AlertDialog.Builder(SingleChatActivity.this);
        delbuilder.setTitle(getText(R.string.delete_options));
        delbuilder.setMultiChoiceItems(items, null,
                new DialogInterface.OnMultiChoiceClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int indexSelected,
                                        boolean isChecked) {
                        if (isChecked) {
                            // If the user checked the item, add it to the selected items
                            seletedItems.add(indexSelected);
                        } else if (seletedItems.contains(indexSelected)) {
                            // Else, if the item is already in the array, remove it
                            seletedItems.remove(Integer.valueOf(indexSelected));
                        }
                    }
                })
                // Set the action buttons
                .setPositiveButton(getText(R.string.delete), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int id) {
                        Intent delintent = new Intent(SingleChatActivity.this, MeBaService.class);

                        boolean delserver = false;
                        boolean delcontent = false;
                        for (int j = 0; j < seletedItems.size(); j++) {
                            CharSequence option = items[j];
                            String stmpoption = option.toString();
                            if (stmpoption.equalsIgnoreCase(getText(R.string.delete_on_server).toString())) {
                                delserver = true;
                            }
                            if (stmpoption.equalsIgnoreCase(getText(R.string.delete_local_content).toString())) {
                                delcontent = true;
                            }
                        }
                        delintent.setAction(Constants.ACTION_DELETECHAT);
                        delintent.putExtra(Constants.DELETEONSERVER, delserver);
                        delintent.putExtra(Constants.DELETELOCALCONTENT, delcontent);
                        delintent.putExtra(Constants.CHATID, inChatID);
                        startService(delintent);
                    }
                })
                .setNegativeButton(getText(R.string.cancel), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int id) {
                        //  Your code when user clicked on Cancel

                    }
                });
        AlertDialog deldlg = delbuilder.create();
        deldlg.show();

        Log.d(TAG, "end openDeleteChat");
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

                switch (menuItem.getItemId()) {
                    case R.id.action_sendcontact:
                        return true;
                    case R.id.action_sendfile:
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
                openDeleteChat(this.ChatID);
                return true;
            case R.id.option_removeuser:
                return true;
            case R.id.action_setchaticon:
                final Intent chooserIntent = new Intent(this, SelectIconActivity.class);
                chooserIntent.putExtra(Constants.THUMBNAIL_TYPE, Constants.THUMBNAIL_USER);
                startActivityForResult(chooserIntent, REQUEST_USER_THUMBNAIL);
                return true;
        }
        Log.d(TAG, "end onOptionsItemSelected");
        return super.onOptionsItemSelected(item);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        //long time = System.currentTimeMillis() / 1000L - (60 * 60 * 24 * 60);
        //String select = "((" + Constants.T_MESSAGES_ChatID + " = " + String.valueOf(ChatID) + ") AND (" + Constants.T_MESSAGES_SendTimestamp + ">" + String.valueOf(time) + "))";
        String select = Constants.T_MESSAGES_ChatID + " = " + String.valueOf(ChatID);
        String sort = Constants.T_MESSAGES_SendTimestamp + " ASC";

        return new CursorLoader(SingleChatActivity.this, FrinmeanContentProvider.MESSAGES_CONTENT_URI,
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

    public String getFilename() {
        String uriSting = Environment.getExternalStorageDirectory().getAbsolutePath() + File.separator + Constants.BASEDIR + File.separator;
        uriSting += System.currentTimeMillis() + ".jpg";
        return uriSting;
    }

    public int calculateInSampleSize(BitmapFactory.Options options, int reqWidth, int reqHeight) {
        final int height = options.outHeight;
        final int width = options.outWidth;
        int inSampleSize = 1;

        if (height > reqHeight || width > reqWidth) {
            final int heightRatio = Math.round((float) height / (float) reqHeight);
            final int widthRatio = Math.round((float) width / (float) reqWidth);
            inSampleSize = heightRatio < widthRatio ? heightRatio : widthRatio;
        }
        final float totalPixels = width * height;
        final float totalReqPixelsCap = reqWidth * reqHeight * 2;
        while (totalPixels / (inSampleSize * inSampleSize) > totalReqPixelsCap) {
            inSampleSize++;
        }

        return inSampleSize;
    }

    private String getRealPathFromURI(String contentURI) {
        Uri contentUri = Uri.parse(contentURI);
        Cursor cursor = getContentResolver().query(contentUri, null, null, null, null);
        if (cursor == null) {
            return contentUri.getPath();
        } else {
            cursor.moveToFirst();
            int index = cursor.getColumnIndex(MediaStore.Images.ImageColumns.DATA);
            return cursor.getString(index);
        }
    }

    public String compressImage(String imageUri) {

        String filePath = getRealPathFromURI(imageUri);
        Bitmap scaledBitmap = null;

        BitmapFactory.Options options = new BitmapFactory.Options();

//      by setting this field as true, the actual bitmap pixels are not loaded in the memory. Just the bounds are loaded. If
//      you try the use the bitmap here, you will get null.
        options.inJustDecodeBounds = true;
        Bitmap bmp = BitmapFactory.decodeFile(filePath, options);

        int actualHeight = options.outHeight;
        int actualWidth = options.outWidth;

//      max Height and width values of the compressed image is taken as 816x612

        float maxHeight = 816.0f;
        float maxWidth = 612.0f;
        float imgRatio = actualWidth / actualHeight;
        float maxRatio = maxWidth / maxHeight;

//      width and height values are set maintaining the aspect ratio of the image

        if (actualHeight > maxHeight || actualWidth > maxWidth) {
            if (imgRatio < maxRatio) {
                imgRatio = maxHeight / actualHeight;
                actualWidth = (int) (imgRatio * actualWidth);
                actualHeight = (int) maxHeight;
            } else if (imgRatio > maxRatio) {
                imgRatio = maxWidth / actualWidth;
                actualHeight = (int) (imgRatio * actualHeight);
                actualWidth = (int) maxWidth;
            } else {
                actualHeight = (int) maxHeight;
                actualWidth = (int) maxWidth;
            }
        }

//      setting inSampleSize value allows to load a scaled down version of the original image

        options.inSampleSize = calculateInSampleSize(options, actualWidth, actualHeight);

//      inJustDecodeBounds set to false to load the actual bitmap
        options.inJustDecodeBounds = false;

//      this options allow android to claim the bitmap memory if it runs low on memory
        options.inPurgeable = true;
        options.inInputShareable = true;
        options.inTempStorage = new byte[16 * 1024];

        try {
//          load the bitmap from its path
            bmp = BitmapFactory.decodeFile(filePath, options);
        } catch (OutOfMemoryError exception) {
            exception.printStackTrace();

        }
        try {
            scaledBitmap = Bitmap.createBitmap(actualWidth, actualHeight, Bitmap.Config.ARGB_8888);
        } catch (OutOfMemoryError exception) {
            exception.printStackTrace();
        }

        float ratioX = actualWidth / (float) options.outWidth;
        float ratioY = actualHeight / (float) options.outHeight;
        float middleX = actualWidth / 2.0f;
        float middleY = actualHeight / 2.0f;

        Matrix scaleMatrix = new Matrix();
        scaleMatrix.setScale(ratioX, ratioY, middleX, middleY);

        Canvas canvas = new Canvas(scaledBitmap);
        canvas.setMatrix(scaleMatrix);
        canvas.drawBitmap(bmp, middleX - bmp.getWidth() / 2, middleY - bmp.getHeight() / 2, new Paint(Paint.FILTER_BITMAP_FLAG));

//      check the rotation of the image and display it properly
        ExifInterface exif;
        try {
            exif = new ExifInterface(filePath);

            int orientation = exif.getAttributeInt(
                    ExifInterface.TAG_ORIENTATION, 0);
            Log.d("EXIF", "Exif: " + orientation);
            Matrix matrix = new Matrix();
            if (orientation == 6) {
                matrix.postRotate(90);
                Log.d("EXIF", "Exif: " + orientation);
            } else if (orientation == 3) {
                matrix.postRotate(180);
                Log.d("EXIF", "Exif: " + orientation);
            } else if (orientation == 8) {
                matrix.postRotate(270);
                Log.d("EXIF", "Exif: " + orientation);
            }
            scaledBitmap = Bitmap.createBitmap(scaledBitmap, 0, 0,
                    scaledBitmap.getWidth(), scaledBitmap.getHeight(), matrix,
                    true);
        } catch (IOException e) {
            e.printStackTrace();
        }

        FileOutputStream out = null;
        String filename = getFilename();
        try {
            out = new FileOutputStream(filename);

//          write the compressed bitmap at the destination specified by filename.
            scaledBitmap.compress(Bitmap.CompressFormat.JPEG, 80, out);


        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        return filename;
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

                    OFMFC res = serializer.read(OFMFC.class, reader, false);
                    if (res == null) {
                        ErrorHelper eh = new ErrorHelper(SingleChatActivity.this);
                        eh.CheckErrorText(Constants.ERROR_NO_CONNECTION_TO_SERVER);
                    } else {
                        if (res.getET() != null && !res.getET().isEmpty()) {
                            ErrorHelper eh = new ErrorHelper(SingleChatActivity.this);
                            eh.CheckErrorText(res.getET());
                        } else {
                            if (res.getM() != null && !res.getM().isEmpty()) {
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

                    OIMIC res = serializer.read(OIMIC.class, reader, false);
                    if (res == null) {
                        ErrorHelper eh = new ErrorHelper(SingleChatActivity.this);
                        eh.CheckErrorText(Constants.ERROR_NO_CONNECTION_TO_SERVER);
                    } else {
                        if (res.getET() != null && !res.getET().isEmpty()) {
                            ErrorHelper eh = new ErrorHelper(SingleChatActivity.this);
                            eh.CheckErrorText(res.getET());
                        } else {
                            if (res.getMID() > 0) {
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

                    OLiUs res = serializer.read(OLiUs.class, reader, false);
                    if (res == null) {
                        ErrorHelper eh = new ErrorHelper(SingleChatActivity.this);
                        eh.CheckErrorText(Constants.ERROR_NO_CONNECTION_TO_SERVER);
                    } else {
                        if (res.getET() != null && !res.getET().isEmpty()) {
                            ErrorHelper eh = new ErrorHelper(SingleChatActivity.this);
                            eh.CheckErrorText(res.getET());
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

                    OAdUC res = serializer.read(OAdUC.class, reader, false);
                    if (res == null) {
                        ErrorHelper eh = new ErrorHelper(SingleChatActivity.this);
                        eh.CheckErrorText(Constants.ERROR_NO_CONNECTION_TO_SERVER);
                    } else {
                        if (res.getET() != null && !res.getET().isEmpty()) {
                            ErrorHelper eh = new ErrorHelper(SingleChatActivity.this);
                            eh.CheckErrorText(res.getET());
                        } else {
                            if (res.getR().equalsIgnoreCase(Constants.RESULT_USER_ADDED_TO_CHAT)) {
                                Toast.makeText(SingleChatActivity.this.getBaseContext(), getString(R.string.user_added_to_chat), Toast.LENGTH_SHORT).show();
                            }
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
                Log.d(TAG, "end broadcast " + Constants.BROADCAST_USERADDEDTOCHAT);
            } else if (intent.getAction().equalsIgnoreCase(Constants.BROADCAST_DELETECHAT)) {
                Log.d(TAG, "start broadcast " + Constants.BROADCAST_DELETECHAT);
                try {
                    String ret = intent.getStringExtra(Constants.BROADCAST_DATA);
                    Serializer serializer = new Persister();
                    Reader reader = new StringReader(ret);

                    ODeCh res = serializer.read(ODeCh.class, reader, false);
                    if (res == null) {
                        ErrorHelper eh = new ErrorHelper(SingleChatActivity.this);
                        eh.CheckErrorText(Constants.ERROR_NO_CONNECTION_TO_SERVER);
                    } else {
                        if (res.getET() != null && !res.getET().isEmpty()) {
                            ErrorHelper eh = new ErrorHelper(SingleChatActivity.this);
                            eh.CheckErrorText(res.getET());
                        } else {
                            if (res.getR().equalsIgnoreCase(Constants.RESULT_CHAT_DELETED)) {
                                Toast.makeText(SingleChatActivity.this.getBaseContext(), getString(R.string.chat_delete), Toast.LENGTH_SHORT).show();
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
