package de.radiohacks.frinmean.service;

import android.app.IntentService;
import android.content.ContentProviderClient;
import android.content.ContentValues;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.ConnectivityManager;
import android.os.Environment;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.util.Log;

import org.simpleframework.xml.Serializer;
import org.simpleframework.xml.core.Persister;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.StringWriter;

import de.radiohacks.frinmean.Constants;
import de.radiohacks.frinmean.adapters.SyncAdapter;
import de.radiohacks.frinmean.adapters.SyncUtils;
import de.radiohacks.frinmean.modelshort.OAdUC;
import de.radiohacks.frinmean.modelshort.OAuth;
import de.radiohacks.frinmean.modelshort.OCrCh;
import de.radiohacks.frinmean.modelshort.ODMFC;
import de.radiohacks.frinmean.modelshort.ODeCh;
import de.radiohacks.frinmean.modelshort.OIMIC;
import de.radiohacks.frinmean.modelshort.OLiUs;
import de.radiohacks.frinmean.modelshort.OSShT;
import de.radiohacks.frinmean.providers.FrinmeanContentProvider;

import static de.radiohacks.frinmean.Constants.CHAT_DB_Columns;
import static de.radiohacks.frinmean.Constants.MESSAGES_DB_Columns;
import static de.radiohacks.frinmean.Constants.T_CHAT_BADBID;
import static de.radiohacks.frinmean.Constants.T_CHAT_ID;
import static de.radiohacks.frinmean.Constants.T_MESSAGES_ChatID;
import static de.radiohacks.frinmean.Constants.T_MESSAGES_ID;
import static de.radiohacks.frinmean.Constants.T_MESSAGES_ShowTimestamp;

public class MeBaService extends IntentService {

    private static final String TAG = MeBaService.class.getSimpleName();
    private static final Object sSyncAdapterLock = new Object();
    private static SyncAdapter sSyncAdapter = null;
    public ConnectivityManager conManager = null;
    private String username;
    private String password;
    private String directory;
    private BroadcastNotifier mBroadcaster = null;
    private RestFunctions rf;

    public MeBaService() {
        super("MeBaService");
    }

    @Override
    public void onCreate() {
        super.onCreate();
        Log.d(TAG, "start onCreate");

        conManager = (ConnectivityManager) getSystemService(CONNECTIVITY_SERVICE);
        mBroadcaster = new BroadcastNotifier(MeBaService.this);
        rf = new RestFunctions();

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

        synchronized (sSyncAdapterLock) {
            if (sSyncAdapter == null) {
                sSyncAdapter = new SyncAdapter(getApplicationContext(), true);
            }
        }
        Log.d(TAG, "end onCreate");
    }

    @Override
    public IBinder onBind(Intent intent) {
        return sSyncAdapter.getSyncAdapterBinder();
    }

/*    protected boolean isNetworkConnected() {
        if (conManager != null) {
            if (conManager.getActiveNetworkInfo() != null) {
                return conManager.getActiveNetworkInfo().isConnected();
            } else {
                return false;
            }
        } else {
            return false;
        }
    } */

    protected void getPreferenceInfo() {
        Log.d(TAG, "start getPreferenceInfo");
        SharedPreferences sharedPrefs = PreferenceManager
                .getDefaultSharedPreferences(this);

        this.username = sharedPrefs.getString(Constants.PrefUsername, "NULL");
        this.password = sharedPrefs.getString(Constants.PrefPassword, "NULL");
        this.directory = sharedPrefs.getString(Constants.PrefDirectory, "NULL");
        Log.d(TAG, "end getPferefenceInfo");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        Log.d(TAG, "start onHandleIntent");
        if (intent != null) {
            final String action = intent.getAction();
            if (Constants.ACTION_LISTUSER.equalsIgnoreCase(action)) {
                final String search = intent.getStringExtra(Constants.SEARCH);
                handleActionListUser(search);
            } else if (Constants.ACTION_SENDTEXTMESSAGE.equalsIgnoreCase(action)) {
                final int cid = intent.getIntExtra(Constants.CHATID, -1);
                final int uid = intent.getIntExtra(Constants.USERID, -1);
                final String TextMessage = intent.getStringExtra(Constants.TEXTMESSAGE);
                insertNewMsgIntoDB(cid, uid, Constants.TYP_TEXT, TextMessage);
            } else if (Constants.ACTION_CREATECHAT.equalsIgnoreCase(action)) {
                final String ChatName = intent.getStringExtra(Constants.CHATNAME);
                handleActionCreateChat(ChatName);
            } else if (Constants.ACTION_SENDIMAGEMESSAGE.equalsIgnoreCase(action)) {
                final int cid = intent.getIntExtra(Constants.CHATID, -1);
                final int uid = intent.getIntExtra(Constants.USERID, -1);
                final String ImageLoc = intent.getStringExtra(Constants.IMAGELOCATION);
                insertImageMesgIntoDB(cid, uid, ImageLoc);
            } else if (Constants.ACTION_ADDUSERTOCHAT.equalsIgnoreCase(action)) {
                final int cid = intent.getIntExtra(Constants.CHATID, -1);
                final int uid = intent.getIntExtra(Constants.USERID, -1);
                handleActionAddUserToChat(cid, uid);
            } else if (Constants.ACTION_SENDVIDEOMESSAGE.equalsIgnoreCase(action)) {
                final int cid = intent.getIntExtra(Constants.CHATID, -1);
                final int uid = intent.getIntExtra(Constants.USERID, -1);
                final String VideoLoc = intent.getStringExtra(Constants.VIDEOLOCATION);
                insertVideoMesgIntoDB(cid, uid, VideoLoc);
            } else if (Constants.ACTION_RELOAD_SETTING.equalsIgnoreCase(action)) {
                getPreferenceInfo();
            } else if (Constants.ACTION_FULLSYNC.equalsIgnoreCase(action)) {
                final String ChatName = intent.getStringExtra(Constants.CHATNAME);
                final int cid = intent.getIntExtra(Constants.CHATID, -1);
                sSyncAdapter.syncGetMessageFromChat(cid, 0, ChatName);
            } else if (Constants.ACTION_AUTHENTICATE.equalsIgnoreCase(action)) {
                handleActionAuthenticateUser();
            } else if (Constants.ACTION_INSERTMESSAGEINTOCHAT.equalsIgnoreCase(action)) {
                final int cid = intent.getIntExtra(Constants.CHATID, -1);
                final long cntmid = intent.getLongExtra(Constants.FWDCONTENTMESSAGEID, -1);
                final int uid = intent.getIntExtra(Constants.USERID, -1);
                handleActionInsertFwdMsgIntoChat(cid, uid, cntmid);
            } else if (Constants.ACTION_DELETEMESSAGEFROMCHAT.equalsIgnoreCase(action)) {
                final long viewid = intent.getLongExtra(Constants.MESSAGEID, -1);
                final boolean delsvr = intent.getBooleanExtra(Constants.DELETEONSERVER, false);
                final boolean delcontent = intent.getBooleanExtra(Constants.DELETELOCALCONTENT, false);
                handleActionDeleteMsgFromChat(viewid, delsvr, delcontent);
            } else if (Constants.ACTION_SETSHOWTIMESTAMP.equalsIgnoreCase(action)) {
                final int cid = intent.getIntExtra(Constants.CHATID, -1);
                handleActionSetShowTimestamp(cid);
            } else if (Constants.ACTION_DELETECHAT.equalsIgnoreCase(action)) {
                final int cid = intent.getIntExtra(Constants.CHATID, -1);
                final boolean delsvr = intent.getBooleanExtra(Constants.DELETEONSERVER, false);
                final boolean delcontent = intent.getBooleanExtra(Constants.DELETELOCALCONTENT, false);
                handleActionDeleteChat(cid, delsvr, delcontent);
            }
        }
        Log.d(TAG, "start onHandleIntent");
    }

    private void handleActionDeleteChat(int ChatID, boolean inDelSvr, boolean inDelContent) {
        Log.d(TAG, "start handleActionDeleteChat");

        if (inDelSvr) {
            try {
                ODeCh out = rf.deletechat(username, password, ChatID);
                Serializer serializer = new Persister();
                StringWriter OutString = new StringWriter();

                serializer.write(out, OutString);

                mBroadcaster.notifyProgress(OutString.toString(), Constants.BROADCAST_DELETECHAT);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        if (inDelContent) {
            ContentProviderClient client = getContentResolver().acquireContentProviderClient(FrinmeanContentProvider.MESSAES_CONTENT_URI);
            Cursor cmsg = client.getLocalContentProvider().query(FrinmeanContentProvider.MESSAES_CONTENT_URI, MESSAGES_DB_Columns, T_MESSAGES_ChatID + " = ?", new String[]{String.valueOf(ChatID)}, null);

            while (cmsg.moveToNext()) {

                String filename;
                if (cmsg.getString(Constants.ID_MESSAGES_MessageType).equalsIgnoreCase(Constants.TYP_IMAGE)) {
                    if (directory.endsWith(File.separator)) {
                        filename = directory + Constants.IMAGEDIR + File.separator + cmsg.getString(Constants.ID_MESSAGES_ImageMsgValue);
                    } else {
                        filename = directory + File.separator + Constants.IMAGEDIR + File.separator + cmsg.getString(Constants.ID_MESSAGES_ImageMsgValue);
                    }
                    File delfile = new File(filename);
                    if (delfile.exists()) {
                        delfile.delete();
                    }
                } else if (cmsg.getString(Constants.ID_MESSAGES_MessageType).equalsIgnoreCase(Constants.TYP_VIDEO)) {
                    if (directory.endsWith(File.separator)) {
                        filename = directory + Constants.VIDEODIR + File.separator + cmsg.getString(Constants.ID_MESSAGES_VideoMsgValue);
                    } else {
                        filename = directory + File.separator + Constants.VIDEODIR + File.separator + cmsg.getString(Constants.ID_MESSAGES_VideoMsgValue);
                    }
                    File delfile = new File(filename);
                    if (delfile.exists()) {
                        delfile.delete();
                    }
                }
                int msgid = cmsg.getInt(Constants.ID_MESSAGES__id);
                client.getLocalContentProvider().delete(FrinmeanContentProvider.MESSAES_CONTENT_URI, T_MESSAGES_ID + " = ?", new String[]{String.valueOf(msgid)});
            }
            cmsg.close();

            Cursor cchat = client.getLocalContentProvider().query(FrinmeanContentProvider.CHAT_CONTENT_URI
                    , CHAT_DB_Columns, T_CHAT_BADBID + " = ?", new String[]{String.valueOf(ChatID)}, null);

            while (cchat.moveToNext()) {
                int chatid = cchat.getInt(Constants.ID_CHAT__id);
                client.getLocalContentProvider().delete(FrinmeanContentProvider.CHAT_CONTENT_URI, T_CHAT_ID + " = ?", new String[]{String.valueOf(chatid)});
            }
            cchat.close();
            client.release();
        }
        Log.d(TAG, "end handleActionDeleteChat");
    }

    private void handleActionSetShowTimestamp(int ChatID) {
        Log.d(TAG, "start handleActionSetShowTimestamp");

        ContentProviderClient client = getContentResolver().acquireContentProviderClient(FrinmeanContentProvider.MESSAES_CONTENT_URI);
        Cursor c = client.getLocalContentProvider().query(FrinmeanContentProvider.MESSAES_CONTENT_URI, MESSAGES_DB_Columns, T_MESSAGES_ShowTimestamp + " = ? AND " + T_MESSAGES_ChatID + " = ?", new String[]{"0", String.valueOf(ChatID)}, null);

        while (c.moveToNext()) {
            OSShT outsst = rf.setshowtimestamp(username, password, c.getInt(Constants.ID_MESSAGES_BADBID));
            if (outsst != null) {
                if (outsst.getET() == null || outsst.getET().isEmpty()) {
                    ContentValues valuesins = new ContentValues();
                    valuesins.put(Constants.T_MESSAGES_BADBID, outsst.getMID());
                    valuesins.put(Constants.T_MESSAGES_ShowTimestamp, outsst.getShT());
                    ((FrinmeanContentProvider) client.getLocalContentProvider()).insertorupdate(FrinmeanContentProvider.MESSAES_CONTENT_URI, valuesins);
                }
            }
        }
        c.close();
        client.release();
        Log.d(TAG, "end handleActionSetShowTimestamp");
    }

    private void handleActionInsertFwdMsgIntoChat(int ChatID, int UserID, long ViewID) {
        Log.d(TAG, "start handleActionInsertMsgIntoChat");

        String selectid = Constants.T_MESSAGES_ID + " = ?";
        ContentProviderClient client = getContentResolver().acquireContentProviderClient(FrinmeanContentProvider.MESSAES_CONTENT_URI);
        Cursor c = client.getLocalContentProvider().query(FrinmeanContentProvider.MESSAES_CONTENT_URI, MESSAGES_DB_Columns, selectid, new String[]{Long.toString(ViewID)}, null);
        c.moveToFirst();
        int ContentMsgID = 0;
        String ContentMessage = "";

        String msgType = c.getString(Constants.ID_MESSAGES_MessageType);
        if (msgType.equalsIgnoreCase(Constants.TYP_TEXT)) {
            ContentMsgID = c.getInt(Constants.ID_MESSAGES_TextMsgID);
            ContentMessage = c.getString(Constants.ID_MESSAGES_TextMsgValue);
        } else if (msgType.equalsIgnoreCase(Constants.TYP_IMAGE)) {
            ContentMsgID = c.getInt(Constants.ID_MESSAGES_ImageMsgID);
            ContentMessage = c.getString(Constants.ID_MESSAGES_ImageMsgValue);
        } else if (msgType.equalsIgnoreCase(Constants.TYP_FILE)) {
            ContentMsgID = c.getInt(Constants.ID_MESSAGES_FileMsgID);
            ContentMessage = c.getString(Constants.ID_MESSAGES_FileMsgValue);
        } else if (msgType.equalsIgnoreCase(Constants.TYP_CONTACT)) {
            ContentMsgID = c.getInt(Constants.ID_MESSAGES_ContactMsgID);
            ContentMessage = c.getString(Constants.ID_MESSAGES_ContactMsgValue);
        } else if (msgType.equalsIgnoreCase(Constants.TYP_LOCATION)) {
            ContentMsgID = c.getInt(Constants.ID_MESSAGES_LocationMsgID);
            ContentMessage = c.getString(Constants.ID_MESSAGES_LocationMsgValue);
        } else if (msgType.equalsIgnoreCase(Constants.TYP_VIDEO)) {
            ContentMsgID = c.getInt(Constants.ID_MESSAGES_VideoMsgID);
            ContentMessage = c.getString(Constants.ID_MESSAGES_VideoMsgValue);
        }
        c.close();
        client.release();

        try {
            OIMIC out = rf.insertmessageintochat(username, password, ChatID, ContentMsgID, msgType);
            insertFwdMsgIntoDB(ChatID, UserID, out.getMID(), out.getSdT(), msgType, ContentMessage, ContentMsgID);
        } catch (Exception e) {
            e.printStackTrace();
        }
        Log.d(TAG, "end handleActionInsertMsgIntoChat");
    }

    private void handleActionDeleteMsgFromChat(long inviewid, boolean indelsvr, boolean indellocal) {
        Log.d(TAG, "start handleActionInsertMsgIntoChat");

        ContentProviderClient client = getContentResolver().acquireContentProviderClient(FrinmeanContentProvider.MESSAES_CONTENT_URI);
        Cursor c = client.getLocalContentProvider().query(FrinmeanContentProvider.MESSAES_CONTENT_URI, MESSAGES_DB_Columns, T_MESSAGES_ID + " = ?", new String[]{String.valueOf(inviewid)}, null);
        //c.moveToFirst();

        while (c.moveToNext()) {
            if (indelsvr) {
                try {
                    ODMFC out = rf.deleteMessageFromChat(username, password, c.getInt(Constants.ID_MESSAGES_BADBID));
                    Serializer serializer = new Persister();
                    StringWriter OutString = new StringWriter();

                    serializer.write(out, OutString);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            if (indellocal) {
                String filename;
                if (c.getString(Constants.ID_MESSAGES_MessageType).equalsIgnoreCase(Constants.TYP_IMAGE)) {
                    if (directory.endsWith(File.separator)) {
                        filename = directory + Constants.IMAGEDIR + File.separator + c.getString(Constants.ID_MESSAGES_ImageMsgValue);
                    } else {
                        filename = directory + File.separator + Constants.IMAGEDIR + File.separator + c.getString(Constants.ID_MESSAGES_ImageMsgValue);
                    }
                    File delfile = new File(filename);
                    if (delfile.exists()) {
                        delfile.delete();
                    }
                } else if (c.getString(Constants.ID_MESSAGES_MessageType).equalsIgnoreCase(Constants.TYP_VIDEO)) {
                    if (directory.endsWith(File.separator)) {
                        filename = directory + Constants.VIDEODIR + File.separator + c.getString(Constants.ID_MESSAGES_VideoMsgValue);
                    } else {
                        filename = directory + File.separator + Constants.VIDEODIR + File.separator + c.getString(Constants.ID_MESSAGES_VideoMsgValue);
                    }
                    File delfile = new File(filename);
                    if (delfile.exists()) {
                        delfile.delete();
                    }
                }
                client.getLocalContentProvider().delete(FrinmeanContentProvider.MESSAES_CONTENT_URI, T_MESSAGES_ID + " = ?", new String[]{String.valueOf(inviewid)});
            }
        }
        c.close();
        client.release();
        Log.d(TAG, "end handleActionInsertMsgIntoChat");
    }

    private void handleActionAddUserToChat(int ChatID, int UserID) {
        Log.d(TAG, "start handleActionAddUserToChat");

        try {
            OAdUC out = rf.addusertochat(username, password, UserID, ChatID);
            Serializer serializer = new Persister();
            StringWriter OutString = new StringWriter();

            serializer.write(out, OutString);

            mBroadcaster.notifyProgress(OutString.toString(), Constants.BROADCAST_USERADDEDTOCHAT);
        } catch (Exception e) {
            e.printStackTrace();
        }
        Log.d(TAG, "end handleActionAddUserToChat");
    }

    private void handleActionCreateChat(String ChatName) {
        Log.d(TAG, "start handleActionCreateChate");

        OCrCh out = rf.createchat(username, password, ChatName);

        if (out.getET() == null || out.getET().isEmpty()) {
            if ((out.getCN().equals(ChatName)) && (out.getCID() > 0)) {
                // mBroadcaster.notifyProgress(Constants.BROADCAST_CREATECHAT, Constants.BROADCAST_CREATECHAT);
                SyncUtils.TriggerRefresh();
            }
        }
        Log.d(TAG, "end handleActionCreateChat");
    }

    private void handleActionListUser(String in) {
        Log.d(TAG, "start handleActionListUser");

        try {
            OLiUs out = rf.listuser(username, password, in);
            Serializer serializer = new Persister();
            StringWriter OutString = new StringWriter();

            serializer.write(out, OutString);

            mBroadcaster.notifyProgress(OutString.toString(), Constants.BROADCAST_LISTUSER);
        } catch (Exception e) {
            e.printStackTrace();
        }
        Log.d(TAG, "end handleActionListUser");
    }

    private void handleActionAuthenticateUser() {
        Log.d(TAG, "start handleActionListUser");

        try {
            OAuth out = rf.authenticate(username, password);

            Serializer serializer = new Persister();
            StringWriter OutString = new StringWriter();

            serializer.write(out, OutString);

            mBroadcaster.notifyProgress(OutString.toString(), Constants.BROADCAST_AUTHENTICATE);
        } catch (Exception e) {
            e.printStackTrace();
        }
        Log.d(TAG, "end handleActionListUser");
    }

    public void copy(File src, File dst) throws IOException {
        InputStream in = new FileInputStream(src);
        OutputStream out = new FileOutputStream(dst);

        // Transfer bytes from in to out
        byte[] buf = new byte[1024];
        int len;
        while ((len = in.read(buf)) > 0) {
            out.write(buf, 0, len);
        }
        in.close();
        out.close();
    }

    private void insertImageMesgIntoDB(int ChatID, int UserID, String Message) {
        Log.d(TAG, "start insertImageMesgIntoDB");

        // First check if File is already in the Image Folder with the right size
        // If not, copy File to the Image Directory with the given Name
        // Then insert Entry into DB for next sync

        File orgFile = new File(Message);

        String localfname = new String();
        if (directory.endsWith(File.separator)) {
            localfname += directory + Constants.IMAGEDIR;
        } else {
            localfname += directory + File.separator + Constants.IMAGEDIR;
        }

        if (!orgFile.getAbsolutePath().equalsIgnoreCase(localfname + File.separator + orgFile.getName())) {
            // Copy file
            try {
                copy(orgFile, new File(localfname + File.separator + orgFile.getName()));
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        insertNewMsgIntoDB(ChatID, UserID, Constants.TYP_IMAGE, orgFile.getName());
        Log.d(TAG, "end insertImageMesgIntoDB");
    }

    private void insertVideoMesgIntoDB(int ChatID, int UserID, String Message) {
        Log.d(TAG, "start insertVideoMesgIntoDB");

        // First check if File is already in the Image Folder with the right size
        // If not, copy File to the Image Directory with the given Name
        // Then insert Entry into DB for next sync

        File orgFile = new File(Message);

        String localfname = new String();
        if (directory.endsWith(File.separator)) {
            localfname += directory + Constants.VIDEODIR;
        } else {
            localfname += directory + File.separator + Constants.VIDEODIR;
        }

        if (!orgFile.getAbsolutePath().equalsIgnoreCase(localfname + File.separator + orgFile.getName())) {
            // Copy file
            try {
                copy(orgFile, new File(localfname + File.separator + orgFile.getName()));
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        insertNewMsgIntoDB(ChatID, UserID, Constants.TYP_VIDEO, orgFile.getName());
        Log.d(TAG, "end insertVideoMesgIntoDB");
    }


    private void insertNewMsgIntoDB(int ChatID, int UserID, String MessageType, String Message) {
        Log.d(TAG, "start insertMsgIntoDB");

        // Insert new Message into local DB and trigger Sync to upload the Information.
        // To find the not send messages the Backend ID musst be 0 and the
        // Sendtimestamp musst be 0
        // The Readtimestamp and the MessageIDs are supplied by the Server
        // The ChatID is needed to insert the Message into the right Chat afterwards

        long time = System.currentTimeMillis() / 1000L;

        ContentValues valuesins = new ContentValues();
        valuesins.put(Constants.T_MESSAGES_BADBID, 0);
        valuesins.put(Constants.T_MESSAGES_NumberAll, 0);
        valuesins.put(Constants.T_MESSAGES_NumberRead, 0);
        valuesins.put(Constants.T_MESSAGES_NumberShow, 0);
        valuesins.put(Constants.T_MESSAGES_OwningUserID, UserID);
        valuesins.put(Constants.T_MESSAGES_OwningUserName, username);
        valuesins.put(Constants.T_MESSAGES_ChatID, ChatID);
        valuesins.put(Constants.T_MESSAGES_MessageTyp, MessageType);
        valuesins.put(Constants.T_MESSAGES_SendTimestamp, time);
        valuesins.put(Constants.T_MESSAGES_ReadTimestamp, time);
        if (MessageType.equalsIgnoreCase(Constants.TYP_TEXT)) {
            valuesins.put(Constants.T_MESSAGES_TextMsgID, 0);
            valuesins.put(Constants.T_MESSAGES_TextMsgValue, Message);
        } else if (MessageType.equalsIgnoreCase(Constants.TYP_IMAGE)) {
            valuesins.put(Constants.T_MESSAGES_ImageMsgID, 0);
            valuesins.put(Constants.T_MESSAGES_ImageMsgValue, Message);
        } else if (MessageType.equalsIgnoreCase(Constants.TYP_LOCATION)) {
            valuesins.put(Constants.T_MESSAGES_LocationMsgID, 0);
            valuesins.put(Constants.T_MESSAGES_LocationMsgValue, Message);
        } else if (MessageType.equalsIgnoreCase(Constants.TYP_CONTACT)) {
            valuesins.put(Constants.T_MESSAGES_ContactMsgID, 0);
            valuesins.put(Constants.T_MESSAGES_ContactMsgValue, Message);
        } else if (MessageType.equalsIgnoreCase(Constants.TYP_FILE)) {
            valuesins.put(Constants.T_MESSAGES_FileMsgID, 0);
            valuesins.put(Constants.T_MESSAGES_FileMsgValue, Message);
        } else if (MessageType.equalsIgnoreCase(Constants.TYP_VIDEO)) {
            valuesins.put(Constants.T_MESSAGES_VideoMsgID, 0);
            valuesins.put(Constants.T_MESSAGES_VideoMsgValue, Message);
        }
        ContentProviderClient client = getContentResolver().acquireContentProviderClient(FrinmeanContentProvider.MESSAES_CONTENT_URI);
        client.getLocalContentProvider().insert(FrinmeanContentProvider.MESSAES_CONTENT_URI, valuesins);
        client.release();

        Log.d(TAG, "end insertMsgIntoDB");
    }

    private void insertFwdMsgIntoDB(int ChatID, int UserID, int MsgID, long timeStamp, String MessageType, String ContentMessage, int ContentMsgID) {
        Log.d(TAG, "start insertMsgIntoDB");

        // Insert new Message into local DB and trigger Sync to upload the Information.
        // To find the not send messages the Backend ID musst be 0 and the
        // Sendtimestamp musst be 0
        // The Readtimestamp and the MessageIDs are supplied by the Server
        // The ChatID is needed to insert the Message into the right Chat afterwards

        ContentValues valuesins = new ContentValues();
        valuesins.put(Constants.T_MESSAGES_BADBID, MsgID);
        valuesins.put(Constants.T_MESSAGES_NumberAll, 0);
        valuesins.put(Constants.T_MESSAGES_NumberRead, 0);
        valuesins.put(Constants.T_MESSAGES_NumberShow, 0);
        valuesins.put(Constants.T_MESSAGES_OwningUserID, UserID);
        valuesins.put(Constants.T_MESSAGES_OwningUserName, username);
        valuesins.put(Constants.T_MESSAGES_ChatID, ChatID);
        valuesins.put(Constants.T_MESSAGES_MessageTyp, MessageType);
        valuesins.put(Constants.T_MESSAGES_SendTimestamp, timeStamp);
        valuesins.put(Constants.T_MESSAGES_ReadTimestamp, timeStamp);
        if (MessageType.equalsIgnoreCase(Constants.TYP_TEXT)) {
            valuesins.put(Constants.T_MESSAGES_TextMsgID, ContentMsgID);
            valuesins.put(Constants.T_MESSAGES_TextMsgValue, ContentMessage);
        } else if (MessageType.equalsIgnoreCase(Constants.TYP_IMAGE)) {
            valuesins.put(Constants.T_MESSAGES_ImageMsgID, ContentMsgID);
            valuesins.put(Constants.T_MESSAGES_ImageMsgValue, ContentMessage);
        } else if (MessageType.equalsIgnoreCase(Constants.TYP_LOCATION)) {
            valuesins.put(Constants.T_MESSAGES_LocationMsgID, ContentMsgID);
            valuesins.put(Constants.T_MESSAGES_LocationMsgValue, ContentMessage);
        } else if (MessageType.equalsIgnoreCase(Constants.TYP_CONTACT)) {
            valuesins.put(Constants.T_MESSAGES_ContactMsgID, ContentMsgID);
            valuesins.put(Constants.T_MESSAGES_ContactMsgValue, ContentMessage);
        } else if (MessageType.equalsIgnoreCase(Constants.TYP_FILE)) {
            valuesins.put(Constants.T_MESSAGES_FileMsgID, ContentMsgID);
            valuesins.put(Constants.T_MESSAGES_FileMsgValue, ContentMessage);
        } else if (MessageType.equalsIgnoreCase(Constants.TYP_VIDEO)) {
            valuesins.put(Constants.T_MESSAGES_VideoMsgID, ContentMsgID);
            valuesins.put(Constants.T_MESSAGES_VideoMsgValue, ContentMessage);
        }
        ContentProviderClient client = getContentResolver().acquireContentProviderClient(FrinmeanContentProvider.MESSAES_CONTENT_URI);
        client.getLocalContentProvider().insert(FrinmeanContentProvider.MESSAES_CONTENT_URI, valuesins);
        client.release();

        Log.d(TAG, "end insertMsgIntoDB");
    }
}