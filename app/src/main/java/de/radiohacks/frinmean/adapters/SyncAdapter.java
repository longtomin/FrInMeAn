package de.radiohacks.frinmean.adapters;

import android.accounts.Account;
import android.annotation.TargetApi;
import android.content.AbstractThreadedSyncAdapter;
import android.content.ContentProviderClient;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.SyncResult;
import android.database.Cursor;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;

import java.io.File;
import java.util.List;

import de.radiohacks.frinmean.Constants;
import de.radiohacks.frinmean.model.Chat;
import de.radiohacks.frinmean.model.Chats;
import de.radiohacks.frinmean.model.Message;
import de.radiohacks.frinmean.model.OutCheckNewMessages;
import de.radiohacks.frinmean.model.OutFetchImageMessage;
import de.radiohacks.frinmean.model.OutFetchMessageFromChat;
import de.radiohacks.frinmean.model.OutFetchTextMessage;
import de.radiohacks.frinmean.model.OutGetImageMessageMetaData;
import de.radiohacks.frinmean.model.OutListChat;
import de.radiohacks.frinmean.providers.FrinmeanContentProvider;
import de.radiohacks.frinmean.service.RestFunctions;

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
        Log.i(TAG, "Network synchronization complete");
    }

    private void syncListChats() {
        Log.d(TAG, "start syncListChats");

        OutListChat outlistchat = rf.listchat(username, password);

        if (outlistchat.getErrortext() == null || outlistchat.getErrortext().isEmpty()) {
            if (outlistchat.getChat() != null && !outlistchat.getChat().isEmpty()) {
                SaveChatsToLDB(outlistchat.getChat());
            }
        }
        Log.d(TAG, "end syncListChats");
    }

    private void syncCheckNewMessages() {
        Log.d(TAG, "start syncCheckNewMessages");

        OutCheckNewMessages outcheck = rf.checknewmessages(username, password);

        if (outcheck.getErrortext() == null || outcheck.getErrortext().isEmpty()) {
            if (outcheck.getChats() != null && outcheck.getChats().size() > 0) {
                for (int i = 0; i < outcheck.getChats().size(); i++) {
                    Chats c = outcheck.getChats().get(i);
                    syncGetMessageFromChat(c.getChatID(), 0, c.getChatname());
                }
            }
        }
        Log.d(TAG, "end syncCheckNewMessages");
    }


    private void syncGetMessageFromChat(int cid, long readtime, String CName) {
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
            valuesins.put(Constants.T_CHAT_BADBID, c.getChatID());
            valuesins.put(Constants.T_CHAT_OwningUserID, c.getOwningUser().getOwningUserID());
            valuesins.put(Constants.T_CHAT_OwningUserName, c.getOwningUser().getOwningUserName());
            valuesins.put(Constants.T_CHAT_ChatName, c.getChatname());
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
            valuesins.put(Constants.T_MESSAGES_BADBID, m.getMessageID());
            valuesins.put(Constants.T_MESSAGES_OwningUserID, m.getOwningUser().getOwningUserID());
            valuesins.put(Constants.T_MESSAGES_OwningUserName, m.getOwningUser().getOwningUserName());
            valuesins.put(Constants.T_MESSAGES_ChatID, ChatID);
            valuesins.put(Constants.T_MESSAGES_MessageTyp, m.getMessageTyp());
            valuesins.put(Constants.T_MESSAGES_SendTimestamp, m.getSendTimestamp());
            valuesins.put(Constants.T_MESSAGES_ReadTimestamp, m.getReadTimestamp());
            if (m.getMessageTyp().equalsIgnoreCase(Constants.TYP_TEXT)) {
                valuesins.put(Constants.T_MESSAGES_TextMsgID, m.getTextMsgID());
                OutFetchTextMessage oftm = rf.gettextmessage(username, password, m.getTextMsgID());
                if (oftm.getErrortext() == null || oftm.getErrortext().isEmpty()) {
                    valuesins.put(Constants.T_MESSAGES_TextMsgValue, oftm.getTextMessage());
                    ContentProviderClient client = mContentResolver.acquireContentProviderClient(FrinmeanContentProvider.MESSAES_CONTENT_URI);
                    ((FrinmeanContentProvider) client.getLocalContentProvider()).insertorupdate(FrinmeanContentProvider.MESSAES_CONTENT_URI, valuesins);
                    client.release();
                }
            } else if (m.getMessageTyp().equalsIgnoreCase(Constants.TYP_IMAGE)) {
                valuesins.put(Constants.T_MESSAGES_ImageMsgID, m.getTextMsgID());
                OutGetImageMessageMetaData outmeta = rf.getImageMessageMetaData(username, password, m.getImageMsgID());

                if (outmeta.getErrortext() == null || outmeta.getErrortext().isEmpty()) {
                    if (!checkfileexists(outmeta.getImageMessage(), outmeta.getImageSize())) {
                        OutFetchImageMessage ofim = rf.fetchImageMessage(username, password, m.getImageMsgID());

                        if (ofim.getErrortext() == null || ofim.getErrortext().isEmpty()) {
                            valuesins.put(Constants.T_MESSAGES_ImageMsgValue, ofim.getImageMessage());
                            ContentProviderClient client = mContentResolver.acquireContentProviderClient(FrinmeanContentProvider.MESSAES_CONTENT_URI);
                            ((FrinmeanContentProvider) client.getLocalContentProvider()).insertorupdate(FrinmeanContentProvider.MESSAES_CONTENT_URI, valuesins);
                            client.release();
                        }
                    }
                }
            } else if (m.getMessageTyp().equalsIgnoreCase(Constants.TYP_CONTACT)) {
                valuesins.put(Constants.T_MESSAGES_ContactMsgID, m.getTextMsgID());
                ContentProviderClient client = mContentResolver.acquireContentProviderClient(FrinmeanContentProvider.MESSAES_CONTENT_URI);
                ((FrinmeanContentProvider) client.getLocalContentProvider()).insertorupdate(FrinmeanContentProvider.MESSAES_CONTENT_URI, valuesins);
                client.release();
//                OutFetchContactMessage ofcm = checkAndDownloadImageMessage(m.getImageMsgID());
//                if (ofcm.getErrortext() == null || ofcm.getErrortext().isEmpty()) {
//                    valuesins.put(Constants.T_MESSAGES_ContactMsgValue, ofcm.getImageMessage());
//                    fcp.insertorupdate(FrinmeanContentProvider.CONTENT_URI, valuesins);
//                }
            } else if (m.getMessageTyp().equalsIgnoreCase(Constants.TYP_FILE)) {
                valuesins.put(Constants.T_MESSAGES_FileMsgID, m.getTextMsgID());
                ContentProviderClient client = mContentResolver.acquireContentProviderClient(FrinmeanContentProvider.MESSAES_CONTENT_URI);
                ((FrinmeanContentProvider) client.getLocalContentProvider()).insertorupdate(FrinmeanContentProvider.MESSAES_CONTENT_URI, valuesins);
                client.release();
//                OutFetchFileMessage offm = checkAndDownloadImageMessage(m.getImageMsgID());
//                if (offm.getErrortext() == null || offm.getErrortext().isEmpty()) {
//                    valuesins.put(Constants.T_MESSAGES_ContactMsgValue, offm.getImageMessage());
//                    fcp.insertorupdate(FrinmeanContentProvider.CONTENT_URI, valuesins);
//                }
            } else if (m.getMessageTyp().equalsIgnoreCase(Constants.TYP_LOCATION)) {
                valuesins.put(Constants.T_MESSAGES_LocationMsgID, m.getTextMsgID());
                ContentProviderClient client = mContentResolver.acquireContentProviderClient(FrinmeanContentProvider.MESSAES_CONTENT_URI);
                ((FrinmeanContentProvider) client.getLocalContentProvider()).insertorupdate(FrinmeanContentProvider.MESSAES_CONTENT_URI, valuesins);
                client.release();
//                OutFetchLocationMessage oflm = checkAndDownloadImageMessage(m.getImageMsgID());
//                if (oflm.getErrortext() == null || oflm.getErrortext().isEmpty()) {
//                    valuesins.put(Constants.T_MESSAGES_ContactMsgValue, oflm.getImageMessage());
//                    fcp.insertorupdate(FrinmeanContentProvider.CONTENT_URI, valuesins);
//                }
            } else if (m.getMessageTyp().equalsIgnoreCase(Constants.TYP_VIDEO)) {
                valuesins.put(Constants.T_MESSAGES_VideoMsgID, m.getTextMsgID());
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
        Log.d(TAG, "end saveMessageToLDB");
    }

    private boolean checkfileexists(String fname, long fsize) {

        boolean ret = false;

        File checkfile = new File(directory + "/" + "images/" + fname);
        if (checkfile.exists()) {
            if (checkfile.length() == fsize) {
                // File exists an has right size
                ret = true;
            }
        }
        return ret;
    }

    /*
    Upload the Messages without a Backend ID
     */
    private void uploadUnsavedMessages() {


        Cursor c = mContentResolver.query(FrinmeanContentProvider.MESSAES_CONTENT_URI, Constants.MESSAGES_DB_Columns, "where " + Constants.T_MESSAGES_BADBID + " = ?", new String[]{"0"}, null);
        c.moveToFirst();
        while (c.moveToNext()) {

            String msgtype = c.getString(Constants.ID_MESSAGES_MessageType);

            if (msgtype.equalsIgnoreCase(Constants.TYP_TEXT)) {

            } else if (msgtype.equalsIgnoreCase(Constants.TYP_IMAGE)) {

            } else if (msgtype.equalsIgnoreCase(Constants.TYP_LOCATION)) {

            } else if (msgtype.equalsIgnoreCase(Constants.TYP_VIDEO)) {

            } else if (msgtype.equalsIgnoreCase(Constants.TYP_FILE)) {

            } else if (msgtype.equalsIgnoreCase(Constants.TYP_CONTACT)) {

            }
        }
    }
}

