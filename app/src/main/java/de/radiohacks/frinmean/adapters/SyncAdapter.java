package de.radiohacks.frinmean.adapters;

import android.accounts.Account;
import android.annotation.TargetApi;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.AbstractThreadedSyncAdapter;
import android.content.ContentProviderClient;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SyncResult;
import android.database.Cursor;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;

import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;
import java.util.List;

import de.radiohacks.frinmean.Constants;
import de.radiohacks.frinmean.R;
import de.radiohacks.frinmean.SingleChatActivity;
import de.radiohacks.frinmean.model.Chat;
import de.radiohacks.frinmean.model.Chats;
import de.radiohacks.frinmean.model.Message;
import de.radiohacks.frinmean.model.OutCheckNewMessages;
import de.radiohacks.frinmean.model.OutFetchImageMessage;
import de.radiohacks.frinmean.model.OutFetchMessageFromChat;
import de.radiohacks.frinmean.model.OutFetchTextMessage;
import de.radiohacks.frinmean.model.OutGetImageMessageMetaData;
import de.radiohacks.frinmean.model.OutInsertMessageIntoChat;
import de.radiohacks.frinmean.model.OutListChat;
import de.radiohacks.frinmean.model.OutSendImageMessage;
import de.radiohacks.frinmean.model.OutSendTextMessage;
import de.radiohacks.frinmean.providers.FrinmeanContentProvider;
import de.radiohacks.frinmean.service.RestFunctions;

import static de.radiohacks.frinmean.Constants.CHAT_DB_Columns;
import static de.radiohacks.frinmean.Constants.ID_MESSAGES_ChatID;
import static de.radiohacks.frinmean.Constants.ID_MESSAGES_MessageType;
import static de.radiohacks.frinmean.Constants.ID_MESSAGES_TextMsgValue;
import static de.radiohacks.frinmean.Constants.ID_MESSAGES__id;
import static de.radiohacks.frinmean.Constants.MESSAGES_DB_Columns;
import static de.radiohacks.frinmean.Constants.TYP_CONTACT;
import static de.radiohacks.frinmean.Constants.TYP_FILE;
import static de.radiohacks.frinmean.Constants.TYP_IMAGE;
import static de.radiohacks.frinmean.Constants.TYP_LOCATION;
import static de.radiohacks.frinmean.Constants.TYP_TEXT;
import static de.radiohacks.frinmean.Constants.TYP_VIDEO;
import static de.radiohacks.frinmean.Constants.T_CHAT_BADBID;
import static de.radiohacks.frinmean.Constants.T_CHAT_ChatName;
import static de.radiohacks.frinmean.Constants.T_CHAT_OwningUserID;
import static de.radiohacks.frinmean.Constants.T_CHAT_OwningUserName;
import static de.radiohacks.frinmean.Constants.T_MESSAGES_BADBID;
import static de.radiohacks.frinmean.Constants.T_MESSAGES_ChatID;
import static de.radiohacks.frinmean.Constants.T_MESSAGES_ContactMsgID;
import static de.radiohacks.frinmean.Constants.T_MESSAGES_ContactMsgValue;
import static de.radiohacks.frinmean.Constants.T_MESSAGES_FileMsgID;
import static de.radiohacks.frinmean.Constants.T_MESSAGES_FileMsgValue;
import static de.radiohacks.frinmean.Constants.T_MESSAGES_ID;
import static de.radiohacks.frinmean.Constants.T_MESSAGES_ImageMsgID;
import static de.radiohacks.frinmean.Constants.T_MESSAGES_ImageMsgValue;
import static de.radiohacks.frinmean.Constants.T_MESSAGES_LocationMsgID;
import static de.radiohacks.frinmean.Constants.T_MESSAGES_LocationMsgValue;
import static de.radiohacks.frinmean.Constants.T_MESSAGES_MessageTyp;
import static de.radiohacks.frinmean.Constants.T_MESSAGES_OwningUserID;
import static de.radiohacks.frinmean.Constants.T_MESSAGES_OwningUserName;
import static de.radiohacks.frinmean.Constants.T_MESSAGES_ReadTimestamp;
import static de.radiohacks.frinmean.Constants.T_MESSAGES_SendTimestamp;
import static de.radiohacks.frinmean.Constants.T_MESSAGES_TextMsgID;
import static de.radiohacks.frinmean.Constants.T_MESSAGES_TextMsgValue;
import static de.radiohacks.frinmean.Constants.T_MESSAGES_VideoMsgID;
import static de.radiohacks.frinmean.Constants.T_MESSAGES_VideoMsgValue;

/**
 * Created by thomas on 19.01.15.
 */
public class SyncAdapter extends AbstractThreadedSyncAdapter {
    public static final String TAG = "SyncAdapter";
    /**
     * Content resolver, for performing database operations.
     */
    private final ContentResolver mContentResolver;
    private String username;
    private String password;
    private String directory;
    private int userid;
    private RestFunctions rf;

    /**
     * Constructor. Obtains handle to content resolver for later use.
     */
    public SyncAdapter(Context context, boolean autoInitialize) {
        super(context, autoInitialize);
        mContentResolver = context.getContentResolver();
        getPreferenceInfo();
        rf = new RestFunctions();
    }

    /**
     * Constructor. Obtains handle to content resolver for later use.
     */
    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    public SyncAdapter(Context context, boolean autoInitialize, boolean allowParallelSyncs) {
        super(context, autoInitialize, allowParallelSyncs);
        mContentResolver = context.getContentResolver();
        getPreferenceInfo();
        rf = new RestFunctions();
    }

    protected void getPreferenceInfo() {
        Log.d(TAG, "start getPreferenceInfo");
        SharedPreferences sharedPrefs = PreferenceManager
                .getDefaultSharedPreferences(getContext());

        this.username = sharedPrefs.getString(Constants.PrefUsername, "NULL");
        this.password = sharedPrefs.getString(Constants.PrefPassword, "NULL");
        this.directory = sharedPrefs.getString(Constants.PrefDirectory, "NULL");
        this.userid = sharedPrefs.getInt(Constants.PrefUserID, -1);
//        this.userid = Integer.parseInt(sharedPrefs.getString(Constants.PrefUserID, "-1"));
        Log.d(TAG, "end getPferefenceInfo");
    }


    /**
     * Called by the Android system in response to a request to run the sync adapter. The work
     * required to read data from the network, parse it, and store it in the content provider is
     * done here. Extending AbstractThreadedSyncAdapter ensures that all methods within SyncAdapter
     * run on a background thread. For this reason, blocking I/O and other long-running tasks can be
     * run <em>in situ</em>, and you don't have to set up a separate thread for them.
     * .
     * <p/>
     * <p>This is where we actually perform any work required to perform a sync.
     * {@link android.content.AbstractThreadedSyncAdapter} guarantees that this will be called on a non-UI thread,
     * so it is safe to peform blocking I/O here.
     * <p/>
     * <p>The syncResult argument allows you to pass information back to the method that triggered
     * the sync.
     */
    @Override
    public void onPerformSync(Account account, Bundle extras, String authority,
                              ContentProviderClient provider, SyncResult syncResult) {
        Log.i(TAG, "Beginning network synchronization");
        syncListChats();
        syncCheckNewMessages();
        uploadUnsavedMessages();
        Log.i(TAG, "Network synchronization complete");
    }

    private void syncListChats() {
        Log.d(TAG, "start syncListChats");

        OutListChat outlistchat = rf.listchat(username, password);

        if (outlistchat != null) {
            if (outlistchat.getErrortext() == null || outlistchat.getErrortext().isEmpty()) {
                if (outlistchat.getChat() != null && !outlistchat.getChat().isEmpty()) {
                    SaveChatsToLDB(outlistchat.getChat());
                }
            }
        }
        Log.d(TAG, "end syncListChats");
    }

    private void syncCheckNewMessages() {
        Log.d(TAG, "start syncCheckNewMessages");

        OutCheckNewMessages outcheck = rf.checknewmessages(username, password);

        if (outcheck != null) {
        if (outcheck.getErrortext() == null || outcheck.getErrortext().isEmpty()) {
            if (outcheck.getChats() != null && outcheck.getChats().size() > 0) {
                for (int i = 0; i < outcheck.getChats().size(); i++) {
                    Chats c = outcheck.getChats().get(i);
                    if (c.getNumberOfMessages() > 0) {
                        syncGetMessageFromChat(c.getChatID(), 0, c.getChatname());
                    }
                }
            }
        }
        }
        Log.d(TAG, "end syncCheckNewMessages");
    }


    public void syncGetMessageFromChat(int cid, long readtime, String CName) {
        Log.d(TAG, "start syncGetMessageFromChat");


        OutFetchMessageFromChat res = rf.getmessagefromchat(username, password, cid, readtime);

        if (res != null) {
            if (res.getErrortext() == null || res.getErrortext().isEmpty()) {
                if (res.getMessage() != null && res.getMessage().size() > 0) {
                    SaveMessageToLDB(res.getMessage(), cid, CName);
                }
            }
        }
        // TODO Check if App is running, the display nothing else make a Notofication
        // mBroadcaster.notifyProgress(ret, Constants.BROADCAST_GETMESSAGEFROMCHAT);
        Log.d(TAG, "end syncGetMessageFromChat");
    }

    private void SaveChatsToLDB(List<Chat> in) {
        Log.d(TAG, "start SaveChatsToLDB");
        for (int j = 0; j < in.size(); j++) {
            Chat c = in.get(j);
            ContentValues valuesins = new ContentValues();
            valuesins.put(T_CHAT_BADBID, c.getChatID());
            valuesins.put(T_CHAT_OwningUserID, c.getOwningUser().getOwningUserID());
            valuesins.put(T_CHAT_OwningUserName, c.getOwningUser().getOwningUserName());
            valuesins.put(T_CHAT_ChatName, c.getChatname());
            ContentProviderClient client = mContentResolver.acquireContentProviderClient(FrinmeanContentProvider.CHAT_CONTENT_URI);
            ((FrinmeanContentProvider) client.getLocalContentProvider()).insertorupdate(FrinmeanContentProvider.CHAT_CONTENT_URI, valuesins);
            client.release();
        }
        Log.d(TAG, "end saveChatsToLDB");
    }

    private void SaveMessageToLDB(List<Message> in, int ChatID, String ChatName) {
        Log.d(TAG, "start SaveMessageToLDB");
        for (int j = 0; j < in.size(); j++) {
            Message m = in.get(j);
            ContentValues valuesins = new ContentValues();
            valuesins.put(T_MESSAGES_BADBID, m.getMessageID());
            valuesins.put(T_MESSAGES_OwningUserID, m.getOwningUser().getOwningUserID());
            valuesins.put(T_MESSAGES_OwningUserName, m.getOwningUser().getOwningUserName());
            valuesins.put(T_MESSAGES_ChatID, ChatID);
            valuesins.put(T_MESSAGES_MessageTyp, m.getMessageTyp());
            valuesins.put(T_MESSAGES_SendTimestamp, m.getSendTimestamp());
            valuesins.put(T_MESSAGES_ReadTimestamp, m.getReadTimestamp());
            if (m.getMessageTyp().equalsIgnoreCase(TYP_TEXT)) {
                valuesins.put(T_MESSAGES_TextMsgID, m.getTextMsgID());
                OutFetchTextMessage oftm = rf.gettextmessage(username, password, m.getTextMsgID());
                if (oftm.getErrortext() == null || oftm.getErrortext().isEmpty()) {
                    valuesins.put(T_MESSAGES_TextMsgValue, oftm.getTextMessage());
                    ContentProviderClient client = mContentResolver.acquireContentProviderClient(FrinmeanContentProvider.MESSAES_CONTENT_URI);
                    ((FrinmeanContentProvider) client.getLocalContentProvider()).insertorupdate(FrinmeanContentProvider.MESSAES_CONTENT_URI, valuesins);
                    client.release();
                }
            } else if (m.getMessageTyp().equalsIgnoreCase(TYP_IMAGE)) {
                valuesins.put(T_MESSAGES_ImageMsgID, m.getImageMsgID());
                OutGetImageMessageMetaData outmeta = rf.getImageMessageMetaData(username, password, m.getImageMsgID());

                if (outmeta.getErrortext() == null || outmeta.getErrortext().isEmpty()) {
                    if (!checkfileexists(outmeta.getImageMessage(), outmeta.getImageSize())) {
                        OutFetchImageMessage ofim = rf.fetchImageMessage(username, password, m.getImageMsgID());

                        if (ofim.getErrortext() == null || ofim.getErrortext().isEmpty()) {
                            valuesins.put(T_MESSAGES_ImageMsgValue, ofim.getImageMessage());
                            ContentProviderClient client = mContentResolver.acquireContentProviderClient(FrinmeanContentProvider.MESSAES_CONTENT_URI);
                            ((FrinmeanContentProvider) client.getLocalContentProvider()).insertorupdate(FrinmeanContentProvider.MESSAES_CONTENT_URI, valuesins);
                            client.release();
                        }
                    } else {
                        valuesins.put(T_MESSAGES_ImageMsgValue, outmeta.getImageMessage());
                        ContentProviderClient client = mContentResolver.acquireContentProviderClient(FrinmeanContentProvider.MESSAES_CONTENT_URI);
                        ((FrinmeanContentProvider) client.getLocalContentProvider()).insertorupdate(FrinmeanContentProvider.MESSAES_CONTENT_URI, valuesins);
                        client.release();
                    }
                }
            } else if (m.getMessageTyp().equalsIgnoreCase(TYP_CONTACT)) {
                valuesins.put(T_MESSAGES_ContactMsgID, m.getContactMsgID());
                ContentProviderClient client = mContentResolver.acquireContentProviderClient(FrinmeanContentProvider.MESSAES_CONTENT_URI);
                ((FrinmeanContentProvider) client.getLocalContentProvider()).insertorupdate(FrinmeanContentProvider.MESSAES_CONTENT_URI, valuesins);
                client.release();
//                OutFetchContactMessage ofcm = checkAndDownloadImageMessage(m.getImageMsgID());
//                if (ofcm.getErrortext() == null || ofcm.getErrortext().isEmpty()) {
//                    valuesins.put(Constants.T_MESSAGES_ContactMsgValue, ofcm.getImageMessage());
//                    fcp.insertorupdate(FrinmeanContentProvider.CONTENT_URI, valuesins);
//                }
            } else if (m.getMessageTyp().equalsIgnoreCase(TYP_FILE)) {
                valuesins.put(T_MESSAGES_FileMsgID, m.getFileMsgID());
                ContentProviderClient client = mContentResolver.acquireContentProviderClient(FrinmeanContentProvider.MESSAES_CONTENT_URI);
                ((FrinmeanContentProvider) client.getLocalContentProvider()).insertorupdate(FrinmeanContentProvider.MESSAES_CONTENT_URI, valuesins);
                client.release();
//                OutFetchFileMessage offm = checkAndDownloadImageMessage(m.getImageMsgID());
//                if (offm.getErrortext() == null || offm.getErrortext().isEmpty()) {
//                    valuesins.put(Constants.T_MESSAGES_ContactMsgValue, offm.getImageMessage());
//                    fcp.insertorupdate(FrinmeanContentProvider.CONTENT_URI, valuesins);
//                }
            } else if (m.getMessageTyp().equalsIgnoreCase(TYP_LOCATION)) {
                valuesins.put(T_MESSAGES_LocationMsgID, m.getLocationMsgID());
                ContentProviderClient client = mContentResolver.acquireContentProviderClient(FrinmeanContentProvider.MESSAES_CONTENT_URI);
                ((FrinmeanContentProvider) client.getLocalContentProvider()).insertorupdate(FrinmeanContentProvider.MESSAES_CONTENT_URI, valuesins);
                client.release();
//                OutFetchLocationMessage oflm = checkAndDownloadImageMessage(m.getImageMsgID());
//                if (oflm.getErrortext() == null || oflm.getErrortext().isEmpty()) {
//                    valuesins.put(Constants.T_MESSAGES_ContactMsgValue, oflm.getImageMessage());
//                    fcp.insertorupdate(FrinmeanContentProvider.CONTENT_URI, valuesins);
//                }
            } else if (m.getMessageTyp().equalsIgnoreCase(TYP_VIDEO)) {
                valuesins.put(T_MESSAGES_VideoMsgID, m.getVideoMsgID());
                ContentProviderClient client = mContentResolver.acquireContentProviderClient(FrinmeanContentProvider.MESSAES_CONTENT_URI);
                ((FrinmeanContentProvider) client.getLocalContentProvider()).insertorupdate(FrinmeanContentProvider.MESSAES_CONTENT_URI, valuesins);
                client.release();
//                OutFetchVideoMessage ofvm = checkAndDownloadImageMessage(m.getImageMsgID());
//                if (ofvm.getErrortext() == null || ofvm.getErrortext().isEmpty()) {
//                    valuesins.put(Constants.T_ContactMsgValue, ofvm.getImageMessage());
//                    fcp.insertorupdate(FrinmeanContentProvider.CONTENT_URI, valuesins);
//                }
            }
        }

        // Now we do the Notification for the User
        // Get needed Information from ContentProvider
        ContentProviderClient client = mContentResolver.acquireContentProviderClient(FrinmeanContentProvider.CHAT_CONTENT_URI);
        Cursor c = ((FrinmeanContentProvider) client.getLocalContentProvider()).query(FrinmeanContentProvider.CHAT_CONTENT_URI, CHAT_DB_Columns, T_CHAT_BADBID + " = ?", new String[]{String.valueOf(ChatID)}, null);

        if (c.moveToFirst()) {
            // Prepare intent which is triggered if the
            // notification is selected

            Intent resultIntent = new Intent(this.getContext(),
                    SingleChatActivity.class);
            resultIntent.putExtra(Constants.CHATID, ChatID);
            resultIntent.putExtra(Constants.CHATNAME, ChatName);
            resultIntent.putExtra(Constants.OWNINGUSERID, c.getInt(Constants.ID_CHAT_OwningUserID));
            resultIntent.putExtra(Constants.USERID, userid);

            PendingIntent pIntent = PendingIntent.getActivity(this.getContext(), 0, resultIntent, 0);

            // Build notification
            // Actions are just fake
            Notification noti = new Notification.Builder(this.getContext())
                    .setContentTitle("FrInMeAn")
                    .setContentText(String.valueOf(in.size()) + " neue Nachrichten im Chat " + ChatName).setSmallIcon(R.drawable.ic_stat_chat)
                    .setContentIntent(pIntent).build();
            NotificationManager notificationManager = (NotificationManager) this.getContext().getSystemService(Context.NOTIFICATION_SERVICE);
            // hide the notification after its selected
            noti.flags |= Notification.FLAG_AUTO_CANCEL;

            notificationManager.notify(0, noti);
        }
        Log.d(TAG, "end saveMessageToLDB");
    }

    private boolean checkfileexists(String fname, long fsize) {

        boolean ret = false;
        File checkfile = null;

        if (directory.endsWith("/")) {
            checkfile = new File(directory + Constants.IMAGEDIR + "/" + fname);
        } else {
            checkfile = new File(directory + "/" + Constants.IMAGEDIR + "/" + fname);
        }

        if (checkfile.exists()) {
            long x = checkfile.length();
            if (checkfile.length() == fsize) {
                // File exists an has right size
                ret = true;
            }
        }
        return ret;
    }

    private void moveFileToDestination(String origFile, String subdir, String serverfilename) {
        Log.d(TAG, "start moveFileToDestination");
        File source = new File(origFile);

        // Where to store it.
        String destFile = directory;
        // Add SubDir for Images, videos or files
        if (destFile.endsWith("/")) {
            destFile += subdir;
        } else {
            destFile += "/" + subdir;
        }

        if (destFile.endsWith("/")) {
            destFile += serverfilename;
        } else {
            destFile += "/" + serverfilename;
        }

        File destination = new File(destFile);
        try {
            FileUtils.moveFile(source, destination);
        } catch (IOException e) {
            e.printStackTrace();
        }
        Log.d(TAG, "end moveFileToDestination");
    }

    /*
    Update local Database with values returned from the server
     */
    public void updateDatabase(int id, int backendid, long send, long read, int msgid, String MessageType, String Message) {
        ContentValues valuesins = new ContentValues();
        valuesins.put(T_MESSAGES_BADBID, backendid);
        valuesins.put(T_MESSAGES_SendTimestamp, send);
        valuesins.put(T_MESSAGES_ReadTimestamp, read);
        if (MessageType.equalsIgnoreCase(TYP_TEXT)) {
            valuesins.put(T_MESSAGES_TextMsgID, msgid);
        } else if (MessageType.equalsIgnoreCase(TYP_IMAGE)) {
            valuesins.put(T_MESSAGES_ImageMsgID, msgid);
            valuesins.put(T_MESSAGES_ImageMsgValue, Message);
        } else if (MessageType.equalsIgnoreCase(TYP_LOCATION)) {
            valuesins.put(T_MESSAGES_LocationMsgID, msgid);
            valuesins.put(T_MESSAGES_LocationMsgValue, Message);
        } else if (MessageType.equalsIgnoreCase(TYP_CONTACT)) {
            valuesins.put(T_MESSAGES_ContactMsgID, msgid);
            valuesins.put(T_MESSAGES_ContactMsgValue, Message);
        } else if (MessageType.equalsIgnoreCase(TYP_FILE)) {
            valuesins.put(T_MESSAGES_FileMsgID, msgid);
            valuesins.put(T_MESSAGES_FileMsgValue, Message);
        } else if (MessageType.equalsIgnoreCase(TYP_VIDEO)) {
            valuesins.put(T_MESSAGES_VideoMsgID, msgid);
            valuesins.put(T_MESSAGES_VideoMsgValue, Message);
        }
        ContentProviderClient client = mContentResolver.acquireContentProviderClient(FrinmeanContentProvider.MESSAES_CONTENT_URI);
        ((FrinmeanContentProvider) client.getLocalContentProvider()).update(FrinmeanContentProvider.MESSAES_CONTENT_URI, valuesins, T_MESSAGES_ID + " = ?", new String[]{String.valueOf(id)});
    }

    /*
    Upload the Messages without a Backend ID
     */
    private void uploadUnsavedMessages() {

        ContentProviderClient client = mContentResolver.acquireContentProviderClient(FrinmeanContentProvider.MESSAES_CONTENT_URI);
        Cursor c = ((FrinmeanContentProvider) client.getLocalContentProvider()).query(FrinmeanContentProvider.MESSAES_CONTENT_URI, MESSAGES_DB_Columns, T_MESSAGES_BADBID + " = ?", new String[]{"0"}, null);

        int x = c.getCount();

        //c.moveToFirst();

        while (c.moveToNext()) {

            String msgtype = c.getString(ID_MESSAGES_MessageType);

            if (msgtype.equalsIgnoreCase(TYP_TEXT)) {
                OutSendTextMessage outtxt = rf.sendtextmessage(username, password, c.getString(ID_MESSAGES_TextMsgValue));
                if (outtxt.getErrortext() == null || outtxt.getErrortext().isEmpty()) {
                    OutInsertMessageIntoChat outins = rf.insertmessageintochat(username, password, c.getInt(ID_MESSAGES_ChatID), outtxt.getTextID(), TYP_TEXT);
                    updateDatabase(c.getInt(ID_MESSAGES__id), outins.getMessageID(), outins.getSendTimestamp(), outins.getSendTimestamp(), outtxt.getTextID(), TYP_TEXT, null);
                }
            } else if (msgtype.equalsIgnoreCase(TYP_IMAGE)) {
                String imgfile = directory;
                if (imgfile.endsWith("/")) {
                    imgfile += Constants.IMAGEDIR + "/" + c.getString(Constants.ID_MESSAGES_ImageMsgValue);
                } else {
                    imgfile += "/" + Constants.IMAGEDIR + "/" + c.getString(Constants.ID_MESSAGES_ImageMsgValue);
                }
                OutSendImageMessage outimg = rf.sendImageMessage(username, password, imgfile);
                if (outimg.getErrortext() == null || outimg.getErrortext().isEmpty()) {
                    OutInsertMessageIntoChat outins = rf.insertmessageintochat(username, password, c.getInt(ID_MESSAGES_ChatID), outimg.getImageID(), TYP_IMAGE);
                    updateDatabase(c.getInt(ID_MESSAGES__id), outins.getMessageID(), outins.getSendTimestamp(), outins.getSendTimestamp(), outimg.getImageID(), TYP_IMAGE, outimg.getImageFileName());
                    moveFileToDestination(imgfile, Constants.IMAGEDIR, outimg.getImageFileName());
                }
            } else if (msgtype.equalsIgnoreCase(TYP_LOCATION)) {

            } else if (msgtype.equalsIgnoreCase(TYP_VIDEO)) {

            } else if (msgtype.equalsIgnoreCase(TYP_FILE)) {

            } else if (msgtype.equalsIgnoreCase(TYP_CONTACT)) {

            }
        }
    }
}

