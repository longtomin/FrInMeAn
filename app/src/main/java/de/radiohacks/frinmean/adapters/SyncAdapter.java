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
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.util.Log;

import com.google.common.hash.HashCode;
import com.google.common.hash.Hashing;
import com.google.common.io.Files;

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
import de.radiohacks.frinmean.model.OutAcknowledgeMessageDownload;
import de.radiohacks.frinmean.model.OutCheckNewMessages;
import de.radiohacks.frinmean.model.OutFetchImageMessage;
import de.radiohacks.frinmean.model.OutFetchMessageFromChat;
import de.radiohacks.frinmean.model.OutFetchTextMessage;
import de.radiohacks.frinmean.model.OutFetchVideoMessage;
import de.radiohacks.frinmean.model.OutGetImageMessageMetaData;
import de.radiohacks.frinmean.model.OutGetMessageInformation;
import de.radiohacks.frinmean.model.OutGetVideoMessageMetaData;
import de.radiohacks.frinmean.model.OutInsertMessageIntoChat;
import de.radiohacks.frinmean.model.OutListChat;
import de.radiohacks.frinmean.model.OutSendImageMessage;
import de.radiohacks.frinmean.model.OutSendTextMessage;
import de.radiohacks.frinmean.model.OutSendVideoMessage;
import de.radiohacks.frinmean.providers.FrinmeanContentProvider;
import de.radiohacks.frinmean.service.CustomExceptionHandler;
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
import static de.radiohacks.frinmean.Constants.T_MESSAGES_NumberAll;
import static de.radiohacks.frinmean.Constants.T_MESSAGES_NumberRead;
import static de.radiohacks.frinmean.Constants.T_MESSAGES_NumberShow;
import static de.radiohacks.frinmean.Constants.T_MESSAGES_OwningUserID;
import static de.radiohacks.frinmean.Constants.T_MESSAGES_OwningUserName;
import static de.radiohacks.frinmean.Constants.T_MESSAGES_ReadTimestamp;
import static de.radiohacks.frinmean.Constants.T_MESSAGES_SendTimestamp;
import static de.radiohacks.frinmean.Constants.T_MESSAGES_ShowTimestamp;
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
    private String ringtone;
    private boolean vibrate;
    private RestFunctions rf;

    /**
     * Constructor. Obtains handle to content resolver for later use.
     */
    public SyncAdapter(Context context, boolean autoInitialize) {
        super(context, autoInitialize);
        mContentResolver = context.getContentResolver();
        getPreferenceInfo();
        rf = new RestFunctions();
        if (directory.equalsIgnoreCase("NULL")) {
            if (!(Thread.getDefaultUncaughtExceptionHandler() instanceof CustomExceptionHandler)) {
                Thread.setDefaultUncaughtExceptionHandler(new CustomExceptionHandler(Environment.getExternalStorageDirectory().toString()));
            }
        } else {
            if (!(Thread.getDefaultUncaughtExceptionHandler() instanceof CustomExceptionHandler)) {
                Thread.setDefaultUncaughtExceptionHandler(new CustomExceptionHandler(directory));
            }
        }
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
        if (directory.equalsIgnoreCase("NULL")) {
            if (!(Thread.getDefaultUncaughtExceptionHandler() instanceof CustomExceptionHandler)) {
                Thread.setDefaultUncaughtExceptionHandler(new CustomExceptionHandler(Environment.getExternalStorageDirectory().toString()));
            }
        } else {
            if (!(Thread.getDefaultUncaughtExceptionHandler() instanceof CustomExceptionHandler)) {
                Thread.setDefaultUncaughtExceptionHandler(new CustomExceptionHandler(directory));
            }
        }
    }

    protected void getPreferenceInfo() {
        Log.d(TAG, "start getPreferenceInfo");
        SharedPreferences sharedPrefs = PreferenceManager
                .getDefaultSharedPreferences(getContext());

        this.username = sharedPrefs.getString(Constants.PrefUsername, "NULL");
        this.password = sharedPrefs.getString(Constants.PrefPassword, "NULL");
        this.directory = sharedPrefs.getString(Constants.PrefDirectory, "NULL");
        this.userid = sharedPrefs.getInt(Constants.PrefUserID, -1);
        this.ringtone = sharedPrefs.getString(Constants.prefRingtone, "DEFAULT_SOUND");
        this.vibrate = sharedPrefs.getBoolean(Constants.prefVibrate, true);
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
        getPreferenceInfo();
        syncListChats();
        syncCheckNewMessages();
        uploadUnsavedMessages();
        syncGetMessageInformation();
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
            valuesins.put(T_MESSAGES_ShowTimestamp, m.getShowTimestamp());
            valuesins.put(T_MESSAGES_NumberAll, m.getNumberTotal());
            valuesins.put(T_MESSAGES_NumberShow, m.getNumberShow());
            valuesins.put(T_MESSAGES_NumberRead, m.getNumberRead());

            if (m.getMessageTyp().equalsIgnoreCase(TYP_TEXT)) {
                valuesins.put(T_MESSAGES_TextMsgID, m.getTextMsgID());
                OutFetchTextMessage oftm = rf.gettextmessage(username, password, m.getTextMsgID());
                if (oftm != null) {
                    if (oftm.getErrortext() == null || oftm.getErrortext().isEmpty()) {
                        if (acknowledgeMessage(Constants.TYP_TEXT, oftm.getTextMessage(), m.getMessageID())) {
                            valuesins.put(T_MESSAGES_TextMsgValue, oftm.getTextMessage());
                            ContentProviderClient client = mContentResolver.acquireContentProviderClient(FrinmeanContentProvider.MESSAES_CONTENT_URI);
                            ((FrinmeanContentProvider) client.getLocalContentProvider()).insertorupdate(FrinmeanContentProvider.MESSAES_CONTENT_URI, valuesins);
                            client.release();
                        }
                    }
                }
            } else if (m.getMessageTyp().equalsIgnoreCase(TYP_IMAGE)) {
                valuesins.put(T_MESSAGES_ImageMsgID, m.getImageMsgID());
                OutGetImageMessageMetaData outmeta = rf.getImageMessageMetaData(username, password, m.getImageMsgID());

                if (outmeta != null) {
                    if (outmeta.getErrortext() == null || outmeta.getErrortext().isEmpty()) {
                        if (!checkfileexists(outmeta.getImageMessage(), TYP_IMAGE, outmeta.getImageSize(), outmeta.getImageMD5Hash())) {
                            OutFetchImageMessage ofim = rf.fetchImageMessage(username, password, m.getImageMsgID());
                            if (ofim != null) {
                                if (ofim.getErrortext() == null || ofim.getErrortext().isEmpty()) {
                                    String checkfilepath;

                                    if (directory.endsWith("/")) {
                                        checkfilepath = directory + Constants.IMAGEDIR + "/" + ofim.getImageMessage();
                                    } else {
                                        checkfilepath = directory + "/" + Constants.IMAGEDIR + "/" + ofim.getImageMessage();
                                    }
                                    if (acknowledgeMessage(Constants.TYP_IMAGE, checkfilepath, m.getMessageID())) {
                                        valuesins.put(T_MESSAGES_ImageMsgValue, ofim.getImageMessage());
                                        ContentProviderClient client = mContentResolver.acquireContentProviderClient(FrinmeanContentProvider.MESSAES_CONTENT_URI);
                                        ((FrinmeanContentProvider) client.getLocalContentProvider()).insertorupdate(FrinmeanContentProvider.MESSAES_CONTENT_URI, valuesins);
                                        client.release();
                                    }
                                }
                            }
                        } else {
                            String checkfilepath;
                            if (directory.endsWith("/")) {
                                checkfilepath = directory + Constants.IMAGEDIR + "/" + outmeta.getImageMessage();
                            } else {
                                checkfilepath = directory + "/" + Constants.IMAGEDIR + "/" + outmeta.getImageMessage();
                            }
                            if (acknowledgeMessage(Constants.TYP_IMAGE, checkfilepath, m.getMessageID())) {
                                valuesins.put(T_MESSAGES_ImageMsgValue, outmeta.getImageMessage());
                                ContentProviderClient client = mContentResolver.acquireContentProviderClient(FrinmeanContentProvider.MESSAES_CONTENT_URI);
                                ((FrinmeanContentProvider) client.getLocalContentProvider()).insertorupdate(FrinmeanContentProvider.MESSAES_CONTENT_URI, valuesins);
                                client.release();
                            }
                        }
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
                OutGetVideoMessageMetaData outmeta = rf.getVideoMessageMetaData(username, password, m.getVideoMsgID());

                if (outmeta != null) {
                    if (outmeta.getErrortext() == null || outmeta.getErrortext().isEmpty()) {
                        if (!checkfileexists(outmeta.getVideoMessage(), TYP_VIDEO, outmeta.getVideoSize(), outmeta.getVideoMD5Hash())) {
                            OutFetchVideoMessage ofvm = rf.fetchVideoMessage(username, password, m.getVideoMsgID());
                            if (ofvm != null) {
                                if (ofvm.getErrortext() == null || ofvm.getErrortext().isEmpty()) {
                                    String checkfilepath;

                                    if (directory.endsWith("/")) {
                                        checkfilepath = directory + Constants.VIDEODIR + "/" + ofvm.getVideoMessage();
                                    } else {
                                        checkfilepath = directory + "/" + Constants.VIDEODIR + "/" + ofvm.getVideoMessage();
                                    }
                                    if (acknowledgeMessage(Constants.TYP_IMAGE, checkfilepath, m.getMessageID())) {
                                        valuesins.put(T_MESSAGES_VideoMsgValue, ofvm.getVideoMessage());
                                        ContentProviderClient client = mContentResolver.acquireContentProviderClient(FrinmeanContentProvider.MESSAES_CONTENT_URI);
                                        ((FrinmeanContentProvider) client.getLocalContentProvider()).insertorupdate(FrinmeanContentProvider.MESSAES_CONTENT_URI, valuesins);
                                        client.release();
                                    }
                                }
                            }
                        } else {
                            String checkfilepath;

                            if (directory.endsWith("/")) {
                                checkfilepath = directory + Constants.VIDEODIR + "/" + outmeta.getVideoMessage();
                            } else {
                                checkfilepath = directory + "/" + Constants.VIDEODIR + "/" + outmeta.getVideoMessage();
                            }
                            if (acknowledgeMessage(Constants.TYP_IMAGE, checkfilepath, m.getMessageID())) {
                                valuesins.put(T_MESSAGES_VideoMsgValue, outmeta.getVideoMessage());
                                ContentProviderClient client = mContentResolver.acquireContentProviderClient(FrinmeanContentProvider.MESSAES_CONTENT_URI);
                                ((FrinmeanContentProvider) client.getLocalContentProvider()).insertorupdate(FrinmeanContentProvider.MESSAES_CONTENT_URI, valuesins);
                                client.release();
                            }
                        }
                    }
                }
            }
        }

        // Now we do the Notification for the User
        // Get needed Information from ContentProvider
        ContentProviderClient client = mContentResolver.acquireContentProviderClient(FrinmeanContentProvider.CHAT_CONTENT_URI);
        Cursor c = client.getLocalContentProvider().query(FrinmeanContentProvider.CHAT_CONTENT_URI, CHAT_DB_Columns, T_CHAT_BADBID + " = ?", new String[]{String.valueOf(ChatID)}, null);

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

            long[] vibpattern = {500, 100, 500};
            // Build notification
            // Actions are just fake
            Notification.Builder nb = new Notification.Builder(this.getContext());
            nb.setContentTitle("FrInMeAn");
            nb.setContentText(String.valueOf(in.size()) + " neue Nachrichten im Chat " + ChatName).setSmallIcon(R.drawable.ic_stat_frinmean);
            nb.setSound(Uri.parse(ringtone));
            nb.setContentIntent(pIntent);

            if (vibrate) {
                nb.setVibrate(vibpattern);
            }

            Notification noti = nb.build();

            NotificationManager notificationManager = (NotificationManager) this.getContext().getSystemService(Context.NOTIFICATION_SERVICE);
            // hide the notification after its selected
            noti.flags |= Notification.FLAG_AUTO_CANCEL;

            notificationManager.notify(0, noti);
        }
        Log.d(TAG, "end saveMessageToLDB");
    }

    private boolean acknowledgeMessage(String msgType, String message, int msgid) {
        Log.d(TAG, "start acknowledgeMessage");

        boolean ret = false;

        if (msgType.equalsIgnoreCase(Constants.TYP_TEXT)) {
            /* Hasher hasher = Hashing.md5().newHasher();
            hasher.putBytes(message.getBytes());
            byte[] md5 = hasher.hash().asBytes(); */
            int hashCode = message.hashCode();

            OutAcknowledgeMessageDownload oack = rf.acknowledgemessagedownload(username, password, msgid, String.valueOf(hashCode));
            if (oack != null) {
                if (oack.getErrortext() == null || oack.getErrortext().isEmpty()) {
                    if (oack.getAcknowledge().equalsIgnoreCase(Constants.ACKNOWLEDGE_TRUE)) {
                        ret = true;
                    }
                }
            }
        } else if (msgType.equalsIgnoreCase(Constants.TYP_IMAGE)) {
            HashCode md5 = null;
            try {
                md5 = Files.hash(new File(message),
                        Hashing.md5());
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
            OutAcknowledgeMessageDownload oack = rf.acknowledgemessagedownload(username, password, msgid, md5.toString());
            if (oack != null) {
                if (oack.getErrortext() == null || oack.getErrortext().isEmpty()) {
                    if (oack.getAcknowledge().equalsIgnoreCase(Constants.ACKNOWLEDGE_TRUE)) {
                        ret = true;
                    }
                }
            }
        } else if (msgType.equalsIgnoreCase(Constants.TYP_VIDEO)) {
            HashCode md5 = null;
            try {
                md5 = Files.hash(new File(message),
                        Hashing.md5());
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
            OutAcknowledgeMessageDownload oack = rf.acknowledgemessagedownload(username, password, msgid, md5.toString());
            if (oack != null) {
                if (oack.getErrortext() == null || oack.getErrortext().isEmpty()) {
                    if (oack.getAcknowledge().equalsIgnoreCase(Constants.ACKNOWLEDGE_TRUE)) {
                        ret = true;
                    }
                }
            }
        }

        Log.d(TAG, "end acknowledgeMessage");
        return ret;
    }

    private boolean checkfileexists(String fname, String msgType, long fsize, String inmd5sumd) {
        Log.d(TAG, "start checkfileexists");

        boolean ret = false;
        File checkfile;
        String checkfilepath = null;

        if (directory.endsWith("/")) {
            if (msgType.equalsIgnoreCase(Constants.TYP_IMAGE)) {
                checkfilepath = directory + Constants.IMAGEDIR + "/" + fname;
            } else if (msgType.equalsIgnoreCase(Constants.TYP_VIDEO)) {
                checkfilepath = directory + Constants.VIDEODIR + "/" + fname;
            } else if (msgType.equalsIgnoreCase(Constants.TYP_FILE)) {
                checkfilepath = directory + Constants.FILESDIR + "/" + fname;
            }
        } else {
            if (msgType.equalsIgnoreCase(Constants.TYP_IMAGE)) {
                checkfilepath = directory + "/" + Constants.IMAGEDIR + "/" + fname;
            } else if (msgType.equalsIgnoreCase(Constants.TYP_VIDEO)) {
                checkfilepath = directory + "/" + Constants.VIDEODIR + "/" + fname;
            } else if (msgType.equalsIgnoreCase(Constants.TYP_FILE)) {
                checkfilepath = directory + "/" + Constants.FILESDIR + "/" + fname;
            }
        }
        checkfile = new File(checkfilepath);

        if (checkfile.exists()) {
            if (checkfile.length() == fsize) {
                // File exists an has right size
                HashCode md5 = null;
                try {
                    md5 = Files.hash(new File(checkfilepath),
                            Hashing.md5());
                } catch (IOException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
                if (md5.toString().equals(inmd5sumd)) {
                    // MD5Sum is equal File already exists
                    ret = true;
                }
            }
        }
        Log.d(TAG, "end checkfileexists");
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
    Update local Database with values returned from the server after the upload
     */
    public void updateUploadedNessagesDatabase(int id, int backendid, long send, long read, int msgid, String MessageType, String Message) {
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

        while (c.moveToNext()) {

            String msgtype = c.getString(ID_MESSAGES_MessageType);

            if (msgtype.equalsIgnoreCase(TYP_TEXT)) {
                OutSendTextMessage outtxt = rf.sendtextmessage(username, password, c.getString(ID_MESSAGES_TextMsgValue));
                if (outtxt != null) {
                    if (outtxt.getErrortext() == null || outtxt.getErrortext().isEmpty()) {
                        OutInsertMessageIntoChat outins = rf.insertmessageintochat(username, password, c.getInt(ID_MESSAGES_ChatID), outtxt.getTextID(), TYP_TEXT);
                        if (outins != null) {
                            if (outins.getErrortext() == null || outins.getErrortext().isEmpty()) {
                                updateUploadedNessagesDatabase(c.getInt(ID_MESSAGES__id), outins.getMessageID(), outins.getSendTimestamp(), outins.getSendTimestamp(), outtxt.getTextID(), TYP_TEXT, null);
                            }
                        }
                    }
                }
            } else if (msgtype.equalsIgnoreCase(TYP_IMAGE)) {
                String imgfile = directory;
                if (imgfile.endsWith("/")) {
                    imgfile += Constants.IMAGEDIR + "/" + c.getString(Constants.ID_MESSAGES_ImageMsgValue);
                } else {
                    imgfile += "/" + Constants.IMAGEDIR + "/" + c.getString(Constants.ID_MESSAGES_ImageMsgValue);
                }
                OutSendImageMessage outimg = rf.sendImageMessage(username, password, imgfile);
                if (outimg != null) {
                    if (outimg.getErrortext() == null || outimg.getErrortext().isEmpty()) {
                        OutInsertMessageIntoChat outins = rf.insertmessageintochat(username, password, c.getInt(ID_MESSAGES_ChatID), outimg.getImageID(), TYP_IMAGE);
                        if (outins != null) {
                            if (outins.getErrortext() == null || outins.getErrortext().isEmpty()) {
                                updateUploadedNessagesDatabase(c.getInt(ID_MESSAGES__id), outins.getMessageID(), outins.getSendTimestamp(), outins.getSendTimestamp(), outimg.getImageID(), TYP_IMAGE, outimg.getImageFileName());
                                moveFileToDestination(imgfile, Constants.IMAGEDIR, outimg.getImageFileName());
                            }
                        }
                    }

                }
            } else if (msgtype.equalsIgnoreCase(TYP_LOCATION)) {

            } else if (msgtype.equalsIgnoreCase(TYP_VIDEO)) {
                String vidfile = directory;
                if (vidfile.endsWith("/")) {
                    vidfile += Constants.VIDEODIR + "/" + c.getString(Constants.ID_MESSAGES_VideoMsgValue);
                } else {
                    vidfile += "/" + Constants.VIDEODIR + "/" + c.getString(Constants.ID_MESSAGES_VideoMsgValue);
                }
                OutSendVideoMessage outvid = rf.sendVideoMessage(username, password, vidfile);
                if (outvid != null) {
                    if (outvid.getErrortext() == null || outvid.getErrortext().isEmpty()) {
                        OutInsertMessageIntoChat outins = rf.insertmessageintochat(username, password, c.getInt(ID_MESSAGES_ChatID), outvid.getVideoID(), TYP_VIDEO);
                        if (outins != null) {
                            if (outins.getErrortext() == null || outins.getErrortext().isEmpty()) {
                                updateUploadedNessagesDatabase(c.getInt(ID_MESSAGES__id), outins.getMessageID(), outins.getSendTimestamp(), outins.getSendTimestamp(), outvid.getVideoID(), TYP_VIDEO, outvid.getVideoFileName());
                                moveFileToDestination(vidfile, Constants.VIDEODIR, outvid.getVideoFileName());
                            }
                        }
                    }

                }
            } else if (msgtype.equalsIgnoreCase(TYP_FILE)) {

            } else if (msgtype.equalsIgnoreCase(TYP_CONTACT)) {

            }
        }
    }

    /*
Update local Database with values returned from the server after the upload
 */
    public void updateGetInformationMessagesDatabase(int id, int innumall, int innumread, int innumshow) {
        ContentValues valuesins = new ContentValues();
        valuesins.put(T_MESSAGES_NumberAll, innumall);
        valuesins.put(T_MESSAGES_NumberRead, innumread);
        valuesins.put(T_MESSAGES_NumberShow, innumshow);

        ContentProviderClient client = mContentResolver.acquireContentProviderClient(FrinmeanContentProvider.MESSAES_CONTENT_URI);
        ((FrinmeanContentProvider) client.getLocalContentProvider()).update(FrinmeanContentProvider.MESSAES_CONTENT_URI, valuesins, T_MESSAGES_ID + " = ?", new String[]{String.valueOf(id)});
    }


    /*
    Get the Message Information for the Messages
     */
    private void syncGetMessageInformation() {

        /* This if for new uploaded Messages to get teh first Status*/
        /* ContentProviderClient clienttotalnull = mContentResolver.acquireContentProviderClient(FrinmeanContentProvider.MESSAES_CONTENT_URI);
        Cursor ctn = ((FrinmeanContentProvider) clienttotalnull.getLocalContentProvider()).query(FrinmeanContentProvider.MESSAES_CONTENT_URI, MESSAGES_DB_Columns, T_MESSAGES_NumberAll + " = ?", new String[]{"0"}, null);

        while (ctn.moveToNext()) {
            OutGetMessageInformation outgmi = rf.getmessageinformation(username, password, ctn.getInt(Constants.ID_MESSAGES_BADBID));
            if (outgmi != null) {
                if (outgmi.getErrortext() == null || outgmi.getErrortext().isEmpty()) {
                    ContentValues valuesins = new ContentValues();
                    valuesins.put(Constants.T_MESSAGES_BADBID, outgmi.getMessageID());
                    valuesins.put(Constants.T_MESSAGES_NumberAll, outgmi.getNumberTotal());
                    valuesins.put(Constants.T_MESSAGES_NumberRead, outgmi.getNumberRead());
                    valuesins.put(Constants.T_MESSAGES_NumberShow, outgmi.getNumberShow());
                    ((FrinmeanContentProvider) clienttotalnull.getLocalContentProvider()).insertorupdate(FrinmeanContentProvider.MESSAES_CONTENT_URI, valuesins);
                }
            }
        }*/

        /* This is to identify changes in the status, if a message is totally downloaded and shown nothing can change anymore */
        String selectdifferent = "((" + Constants.T_MESSAGES_NumberAll + " = ?) OR (" + Constants.T_MESSAGES_NumberAll + " != " + Constants.T_MESSAGES_NumberRead + ") OR  ("
                + Constants.T_MESSAGES_NumberAll + " != " + Constants.T_MESSAGES_NumberShow + "))";

        ContentProviderClient clientdifferent = mContentResolver.acquireContentProviderClient(FrinmeanContentProvider.MESSAES_CONTENT_URI);
        Cursor cd = ((FrinmeanContentProvider) clientdifferent.getLocalContentProvider()).query(FrinmeanContentProvider.MESSAES_CONTENT_URI, MESSAGES_DB_Columns, selectdifferent, new String[]{"0"}, null);

        while (cd.moveToNext()) {
            OutGetMessageInformation outgmi = rf.getmessageinformation(username, password, cd.getInt(Constants.ID_MESSAGES_BADBID));
            if (outgmi != null) {
                if (outgmi.getErrortext() == null || outgmi.getErrortext().isEmpty()) {
                    if (outgmi.getNumberTotal() != cd.getInt(Constants.ID_MESSAGES_NumberAll) ||
                            (outgmi.getNumberRead() != cd.getInt(Constants.ID_MESSAGES_NumberRead)) ||
                            (outgmi.getNumberShow() != cd.getInt(Constants.ID_MESSAGES_NumberShow))) {
                        ContentValues valuesins = new ContentValues();
                        valuesins.put(Constants.T_MESSAGES_BADBID, outgmi.getMessageID());
                        valuesins.put(Constants.T_MESSAGES_NumberAll, outgmi.getNumberTotal());
                        valuesins.put(Constants.T_MESSAGES_NumberRead, outgmi.getNumberRead());
                        valuesins.put(Constants.T_MESSAGES_NumberShow, outgmi.getNumberShow());
                        ((FrinmeanContentProvider) clientdifferent.getLocalContentProvider()).insertorupdate(FrinmeanContentProvider.MESSAES_CONTENT_URI, valuesins);
                    }
                }
            }
        }
    }
}

