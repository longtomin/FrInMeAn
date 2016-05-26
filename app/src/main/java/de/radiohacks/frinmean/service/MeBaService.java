package de.radiohacks.frinmean.service;

import android.app.IntentService;
import android.content.ContentProviderClient;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.ConnectivityManager;
import android.net.Uri;
import android.os.Environment;
import android.os.IBinder;
import android.os.RemoteException;
import android.preference.PreferenceManager;
import android.provider.ContactsContract;
import android.util.Log;
import android.widget.Toast;

import org.simpleframework.xml.Serializer;
import org.simpleframework.xml.core.Persister;

import java.io.File;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.HashMap;

import de.radiohacks.frinmean.Constants;
import de.radiohacks.frinmean.R;
import de.radiohacks.frinmean.adapters.SyncAdapter;
import de.radiohacks.frinmean.adapters.SyncUtils;
import de.radiohacks.frinmean.modelshort.C;
import de.radiohacks.frinmean.modelshort.OAdUC;
import de.radiohacks.frinmean.modelshort.OCrCh;
import de.radiohacks.frinmean.modelshort.ODMFC;
import de.radiohacks.frinmean.modelshort.ODeCh;
import de.radiohacks.frinmean.modelshort.OFMFC;
import de.radiohacks.frinmean.modelshort.OGImM;
import de.radiohacks.frinmean.modelshort.OGImMMD;
import de.radiohacks.frinmean.modelshort.OIMIC;
import de.radiohacks.frinmean.modelshort.OIUIc;
import de.radiohacks.frinmean.modelshort.OLiCh;
import de.radiohacks.frinmean.modelshort.OLiUs;
import de.radiohacks.frinmean.modelshort.OSIcM;
import de.radiohacks.frinmean.modelshort.OSShT;
import de.radiohacks.frinmean.providers.DBHelper;
import de.radiohacks.frinmean.providers.FrinmeanContentProvider;

import static de.radiohacks.frinmean.Constants.CHAT_DB_Columns;
import static de.radiohacks.frinmean.Constants.ID_MESSAGES_ImageMsgID;
import static de.radiohacks.frinmean.Constants.ID_MESSAGES_VideoMsgID;
import static de.radiohacks.frinmean.Constants.MESSAGES_DB_Columns;
import static de.radiohacks.frinmean.Constants.T_CHAT_BADBID;
import static de.radiohacks.frinmean.Constants.T_CHAT_ChatName;
import static de.radiohacks.frinmean.Constants.T_CHAT_ID;
import static de.radiohacks.frinmean.Constants.T_CHAT_OwningUserID;
import static de.radiohacks.frinmean.Constants.T_CHAT_OwningUserName;
import static de.radiohacks.frinmean.Constants.T_MESSAGES_BADBID;
import static de.radiohacks.frinmean.Constants.T_MESSAGES_ChatID;
import static de.radiohacks.frinmean.Constants.T_MESSAGES_ID;
import static de.radiohacks.frinmean.Constants.T_MESSAGES_ImageMsgID;
import static de.radiohacks.frinmean.Constants.T_MESSAGES_ShowTimestamp;
import static de.radiohacks.frinmean.Constants.T_MESSAGES_VideoMsgID;
import static de.radiohacks.frinmean.Constants.T_MESSAGES_VideoMsgValue;

public class MeBaService extends IntentService {

    private static final String TAG = MeBaService.class.getSimpleName();
    private static final Object sSyncAdapterLock = new Object();
    private static SyncAdapter sSyncAdapter = null;
    public ConnectivityManager conManager = null;
    private String username;
    private String password;
    private int userid;
    private String basedir;
    private String imgdir;
    private String viddir;
    private String fildir;
    private String icndir;
    private BroadcastNotifier mBroadcaster = null;
    private RestFunctions rf;
    private DBHelper dbh;

    public MeBaService() {
        super("MeBaService");
        basedir = Environment.getExternalStorageDirectory().getAbsolutePath() + File.separator + Constants.BASEDIR;
        File baseFile = new File(basedir);
        if (!baseFile.exists()) {
            if (!baseFile.mkdir()) {
                Log.e(TAG, "Base Directory creation failed");
            }
        }
        imgdir = basedir + File.separator + Constants.IMAGEDIR + File.separator;
        File imgFile = new File(imgdir);
        if (!imgFile.exists()) {
            if (!imgFile.mkdir()) {
                Log.e(TAG, "Image Directory creation failed");
            }
        }
        viddir = basedir + File.separator + Constants.VIDEODIR + File.separator;
        File vidFile = new File(viddir);
        if (!vidFile.exists()) {
            if (!vidFile.mkdir()) {
                Log.e(TAG, "Video Directory creation failed");
            }
        }
        fildir = basedir + File.separator + Constants.FILESDIR + File.separator;
        File filFile = new File(fildir);
        if (!filFile.exists()) {
            if (!filFile.mkdir()) {
                Log.e(TAG, "File Directory creation failed");
            }
        }
        icndir = basedir + File.separator + Constants.ICONDIR + File.separator;
        File icnFile = new File(icndir);
        if (!icnFile.exists()) {
            if (!icnFile.mkdir()) {
                Log.e(TAG, "Icon Directory creation failed");
            }
        }
    }

    @Override
    public void onCreate() {
        super.onCreate();
        Log.d(TAG, "start onCreate");

        conManager = (ConnectivityManager) getSystemService(CONNECTIVITY_SERVICE);
        mBroadcaster = new BroadcastNotifier(MeBaService.this);
        rf = new RestFunctions();

        getPreferenceInfo();
        dbh = new DBHelper(MeBaService.this);

        if (!(Thread.getDefaultUncaughtExceptionHandler() instanceof CustomExceptionHandler)) {
            Thread.setDefaultUncaughtExceptionHandler(new CustomExceptionHandler(basedir));
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

    protected void getPreferenceInfo() {
        Log.d(TAG, "start getPreferenceInfo");
        SharedPreferences sharedPrefs = PreferenceManager
                .getDefaultSharedPreferences(this);

        this.username = sharedPrefs.getString(Constants.PrefUsername, "NULL");
        this.password = sharedPrefs.getString(Constants.PrefPassword, "NULL");
//        this.directory = sharedPrefs.getString(Constants.PrefDirectory, "NULL");
//        this.contentall = sharedPrefs.getBoolean(Constants.prefContentCommunication, false);
        this.userid = sharedPrefs.getInt(Constants.PrefUserID, -1);
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
                dbh.insertNewMsgIntoDB(cid, uid, Constants.TYP_TEXT, TextMessage);
            } else if (Constants.ACTION_CREATECHAT.equalsIgnoreCase(action)) {
                final String ChatName = intent.getStringExtra(Constants.CHATNAME);
                handleActionCreateChat(ChatName);
            } else if (Constants.ACTION_SENDIMAGEMESSAGE.equalsIgnoreCase(action)) {
                final int cid = intent.getIntExtra(Constants.CHATID, -1);
                final int uid = intent.getIntExtra(Constants.USERID, -1);
                final String ImageLoc = intent.getStringExtra(Constants.IMAGELOCATION);
                dbh.insertImageMesgIntoDB(cid, uid, ImageLoc);
            } else if (Constants.ACTION_ADDUSERTOCHAT.equalsIgnoreCase(action)) {
                final int cid = intent.getIntExtra(Constants.CHATID, -1);
                final int uid = intent.getIntExtra(Constants.USERID, -1);
                handleActionAddUserToChat(cid, uid);
            } else if (Constants.ACTION_SENDVIDEOMESSAGE.equalsIgnoreCase(action)) {
                final int cid = intent.getIntExtra(Constants.CHATID, -1);
                final int uid = intent.getIntExtra(Constants.USERID, -1);
                final String VideoLoc = intent.getStringExtra(Constants.VIDEOLOCATION);
                dbh.insertVideoMesgIntoDB(cid, uid, VideoLoc);
            } else if (Constants.ACTION_RELOAD_SETTING.equalsIgnoreCase(action)) {
                getPreferenceInfo();
            } else if (Constants.ACTION_FULLSYNC.equalsIgnoreCase(action)) {
                final String ChatName = intent.getStringExtra(Constants.CHATNAME);
                final int cid = intent.getIntExtra(Constants.CHATID, -1);
                sSyncAdapter.syncGetMessageFromChat(cid, 0, ChatName);
            /*} else if (Constants.ACTION_AUTHENTICATE.equalsIgnoreCase(action)) {
                handleActionAuthenticateUser();*/
            } else if (Constants.ACTION_INSERTMESSAGEINTOCHAT.equalsIgnoreCase(action)) {
                final int cid = intent.getIntExtra(Constants.CHATID, -1);
                final long cntmid = intent.getLongExtra(Constants.FWDCONTENTMESSAGEID, -1);
                final int uid = intent.getIntExtra(Constants.USERID, -1);
                handleActionInsertFwdMsgIntoChat(cid, uid, cntmid);
            } else if (Constants.ACTION_DELETEMESSAGEFROMCHAT.equalsIgnoreCase(action)) {
                final long viewid = intent.getIntExtra(Constants.MESSAGEID, -1);
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
            } else if (Constants.ACTION_REFRESH.equalsIgnoreCase(action)) {
                final long time = intent.getLongExtra(Constants.TIMESTAMP, -1);
                handleActionRefresh(time);
            } else if (Constants.ACTION_SYNCUSER.equalsIgnoreCase(action)) {
                handleActionSyncUser();
            } else if (Constants.ACTION_SENDUSERICON.equalsIgnoreCase(action)) {
                final String message = intent.getStringExtra(Constants.IMAGELOCATION);
                handleSendUserIcon(message);
            } else if (Constants.ACTION_SENDCHATICON.equalsIgnoreCase(action)) {
                final String message = intent.getStringExtra(Constants.IMAGELOCATION);
                final int chatid = intent.getIntExtra(Constants.CHATID, -1);
                handleSendChatIcon(message, chatid);
            } else if (Constants.ACTION_GETVIDEOMESSAGE.equalsIgnoreCase(action)) {
                final int msgID = intent.getIntExtra(Constants.MESSAGEID, -1);
                final int vidID = intent.getIntExtra(Constants.VIDEOID, -1);
                handleActionDownloadVideo(vidID, msgID);
            }
        }
        Log.d(TAG, "start onHandleIntent");
    }

    private void handleActionRefresh(long intime) {
        Log.d(TAG, "start handleActionRefresh");

        try {
            OLiCh outListChat = rf.listchat();
            if (outListChat != null && outListChat.getET() == null) {
                ContentProviderClient clientChat = getContentResolver().acquireContentProviderClient(FrinmeanContentProvider.CHAT_CONTENT_URI);
                for (int j = 0; j < outListChat.getC().size(); j++) {
                    C c = outListChat.getC().get(j);
                    ContentValues valuesinschat = new ContentValues();
                    valuesinschat.put(T_CHAT_BADBID, c.getCID());
                    valuesinschat.put(T_CHAT_OwningUserID, c.getOU().getOUID());
                    valuesinschat.put(T_CHAT_OwningUserName, c.getOU().getOUN());
                    valuesinschat.put(T_CHAT_ChatName, c.getCN());
                    if (c.getICID() > 0) {
                        String filepath = dbh.downloadimage(c.getICID(), 0, Constants.TYP_ICON);
                        if (filepath != null && !filepath.isEmpty()) {
                            valuesinschat.put(Constants.T_CHAT_IconID, c.getICID());
                            valuesinschat.put(Constants.T_CHAT_IconValue, filepath);
                        }
                    }
                    ((FrinmeanContentProvider) clientChat.getLocalContentProvider()).insertorupdate(FrinmeanContentProvider.CHAT_CONTENT_URI, valuesinschat);
                    OFMFC outFetchMessage = rf.getmessagefromchat(c.getCID(), intime);
                    if (outFetchMessage != null && outFetchMessage.getET() == null) {
                        dbh.SaveMessagetoDB(outFetchMessage.getM(), c.getCID());
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        // ToDo setze Preference wenn Backup durchgeführt ist. Wichtig für StartActivity im onResume.
        Log.d(TAG, "end handleActionRefresh");
    }

    private void handleActionDownloadVideo(int vidID, int msgID) {
        ContentValues valuesins = new ContentValues();
        valuesins.put(T_MESSAGES_VideoMsgID, vidID);
        valuesins.put(T_MESSAGES_BADBID, msgID);
        String vidfile = dbh.downloadvideo(vidID, msgID);

        valuesins.put(T_MESSAGES_VideoMsgValue, vidfile);
        ContentProviderClient client = getContentResolver().acquireContentProviderClient(FrinmeanContentProvider.MESSAGES_CONTENT_URI);
        ((FrinmeanContentProvider) client.getLocalContentProvider()).insertorupdate(FrinmeanContentProvider.MESSAGES_CONTENT_URI, valuesins);
        client.release();

        Intent mediaScanIntent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
        Uri contentUri = Uri.fromFile(new File(vidfile));
        mediaScanIntent.setData(contentUri);
        sendBroadcast(mediaScanIntent);

        // Insert MSG-ID into Time Table
        dbh.inserIntoTimeTable(msgID, userid);
    }

    private void handleActionDeleteChat(int ChatID, boolean inDelSvr, boolean inDelContent) {
        Log.d(TAG, "start handleActionDeleteChat");

        if (inDelSvr) {
            try {
                ODeCh out = rf.deletechat(ChatID);
                Serializer serializer = new Persister();
                StringWriter OutString = new StringWriter();

                serializer.write(out, OutString);

                mBroadcaster.notifyProgress(OutString.toString(), Constants.BROADCAST_DELETECHAT);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        if (inDelContent) {
            ContentProviderClient client = getContentResolver().acquireContentProviderClient(FrinmeanContentProvider.MESSAGES_CONTENT_URI);
            Cursor cmsg = client.getLocalContentProvider().query(FrinmeanContentProvider.MESSAGES_CONTENT_URI, MESSAGES_DB_Columns, T_MESSAGES_ChatID + " = ?", new String[]{String.valueOf(ChatID)}, null);

            while (cmsg.moveToNext()) {

                String filename;
                if (cmsg.getString(Constants.ID_MESSAGES_MessageType).equalsIgnoreCase(Constants.TYP_IMAGE)) {
                    try {
                        Cursor cresueimg = client.query(FrinmeanContentProvider.MESSAGES_CONTENT_URI, MESSAGES_DB_Columns, T_MESSAGES_ImageMsgID + " = ?", new String[]{String.valueOf(cmsg.getInt(ID_MESSAGES_ImageMsgID))}, null);
                        if (cresueimg.getCount() == 1) {
                            File delfile = new File(cmsg.getString(Constants.ID_MESSAGES_ImageMsgValue));
                            if (delfile.exists()) {
                                delfile.delete();
                            }
                        }
                        cresueimg.close();
                    } catch (RemoteException e) {
                        e.printStackTrace();
                    }
                } else if (cmsg.getString(Constants.ID_MESSAGES_MessageType).equalsIgnoreCase(Constants.TYP_VIDEO)) {
                    try {
                        Cursor cresuevid = client.query(FrinmeanContentProvider.MESSAGES_CONTENT_URI, MESSAGES_DB_Columns, T_MESSAGES_VideoMsgID + " = ?", new String[]{String.valueOf(cmsg.getInt(ID_MESSAGES_VideoMsgID))}, null);
                        if (cresuevid.getCount() == 1) {
                            File delfile = new File(cmsg.getString(Constants.ID_MESSAGES_VideoMsgValue));
                            if (delfile.exists()) {
                                delfile.delete();
                            }
                        }
                        cresuevid.close();
                    } catch (RemoteException e) {
                        e.printStackTrace();
                    }
                }
                int msgid = cmsg.getInt(Constants.ID_MESSAGES__id);
                client.getLocalContentProvider().delete(FrinmeanContentProvider.MESSAGES_CONTENT_URI, T_MESSAGES_ID + " = ?", new String[]{String.valueOf(msgid)});
            }
            cmsg.close();

            Cursor cchat = client.getLocalContentProvider().query(FrinmeanContentProvider.CHAT_CONTENT_URI
                    , CHAT_DB_Columns, T_CHAT_BADBID + " = ?", new String[]{String.valueOf(ChatID)}, null);

            while (cchat.moveToNext()) {
                int chatid = cchat.getInt(Constants.ID_CHAT_ID);
                client.getLocalContentProvider().delete(FrinmeanContentProvider.CHAT_CONTENT_URI, T_CHAT_ID + " = ?", new String[]{String.valueOf(chatid)});
            }
            cchat.close();
            client.release();
        }
        Log.d(TAG, "end handleActionDeleteChat");
    }

    private void handleActionSetShowTimestamp(int ChatID) {
        Log.d(TAG, "start handleActionSetShowTimestamp");

        ContentProviderClient client = getContentResolver().acquireContentProviderClient(FrinmeanContentProvider.MESSAGES_CONTENT_URI);
        Cursor c = client.getLocalContentProvider().query(FrinmeanContentProvider.MESSAGES_CONTENT_URI, MESSAGES_DB_Columns, T_MESSAGES_ShowTimestamp + " = ? AND " + T_MESSAGES_ChatID + " = ?", new String[]{"0", String.valueOf(ChatID)}, null);

        if (c.getCount() > 0) {
            ArrayList<Integer> ids = new ArrayList<>(1);
            while (c.moveToNext()) {
                int val = c.getInt(Constants.ID_MESSAGES_BADBID);
                if (!ids.contains(val)) {
                    ids.add(val);
                }
            }
            c.close();

            OSShT outsst = rf.setshowtimestamp(ids);
            if (outsst != null) {
                if (outsst.getET() == null || outsst.getET().isEmpty()) {
                    for (int i = 0; i < outsst.getShT().size(); i++) {
                        ContentValues valuesins = new ContentValues();
                        valuesins.put(Constants.T_MESSAGES_BADBID, outsst.getShT().get(i).getMID());
                        valuesins.put(Constants.T_MESSAGES_ShowTimestamp, outsst.getShT().get(i).getT());
                        ((FrinmeanContentProvider) client.getLocalContentProvider()).insertorupdate(FrinmeanContentProvider.MESSAGES_CONTENT_URI, valuesins);
                    }
                }
            }

            client.release();
            getContentResolver().notifyChange(FrinmeanContentProvider.CHAT_CONTENT_URI, null);
        }
        Log.d(TAG, "end handleActionSetShowTimestamp");
    }

    private void handleActionInsertFwdMsgIntoChat(int ChatID, int UserID, long ViewID) {
        Log.d(TAG, "start handleActionInsertFwdMsgIntoChat");

        String selectid = Constants.T_MESSAGES_ID + " = ?";
        ContentProviderClient client = getContentResolver().acquireContentProviderClient(FrinmeanContentProvider.MESSAGES_CONTENT_URI);
        Cursor c = client.getLocalContentProvider().query(FrinmeanContentProvider.MESSAGES_CONTENT_URI, MESSAGES_DB_Columns, selectid, new String[]{Long.toString(ViewID)}, null);
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
            OIMIC out = rf.insertmessageintochat(ChatID, ContentMsgID, msgType);
            dbh.insertFwdMsgIntoDB(ChatID, UserID, out.getMID(), out.getSdT(), msgType, ContentMessage, ContentMsgID);
        } catch (Exception e) {
            e.printStackTrace();
        }
        Log.d(TAG, "end handleActionInsertFwdMsgIntoChat");
    }

    private void handleActionDeleteMsgFromChat(long inviewid, boolean indelsvr, boolean indellocal) {
        Log.d(TAG, "start handleActionDeleteMsgFromChat");

        ContentProviderClient client = getContentResolver().acquireContentProviderClient(FrinmeanContentProvider.MESSAGES_CONTENT_URI);
        Cursor c = client.getLocalContentProvider().query(FrinmeanContentProvider.MESSAGES_CONTENT_URI, MESSAGES_DB_Columns, T_MESSAGES_ID + " = ?", new String[]{String.valueOf(inviewid)}, null);
        //c.moveToFirst();

        while (c.moveToNext()) {
            if (indelsvr) {
                try {
                    int x = c.getInt(Constants.ID_MESSAGES_BADBID);
                    ODMFC out = rf.deletemessagefromchat(c.getInt(Constants.ID_MESSAGES_BADBID));
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
                    try {
                        Cursor cresueimg = client.query(FrinmeanContentProvider.MESSAGES_CONTENT_URI, MESSAGES_DB_Columns, T_MESSAGES_ImageMsgID + " = ?", new String[]{String.valueOf(c.getInt(ID_MESSAGES_ImageMsgID))}, null);
                        if (cresueimg.getCount() == 1) {
                            File delfile = new File(c.getString(Constants.ID_MESSAGES_ImageMsgValue));
                            if (delfile.exists()) {
                                delfile.delete();
                            }
                        }
                        cresueimg.close();
                    } catch (RemoteException e) {
                        e.printStackTrace();
                    }
                } else if (c.getString(Constants.ID_MESSAGES_MessageType).equalsIgnoreCase(Constants.TYP_VIDEO)) {
                    try {
                        Cursor cresuevid = client.query(FrinmeanContentProvider.MESSAGES_CONTENT_URI, MESSAGES_DB_Columns, T_MESSAGES_VideoMsgID + " = ?", new String[]{String.valueOf(c.getInt(ID_MESSAGES_VideoMsgID))}, null);
                        if (cresuevid.getCount() == 1) {
                            File delfile = new File(c.getString(Constants.ID_MESSAGES_VideoMsgValue));
                            if (delfile.exists()) {
                                delfile.delete();
                            }
                        }
                        cresuevid.close();
                    } catch (RemoteException e) {
                        e.printStackTrace();
                    }
                }
                client.getLocalContentProvider().delete(FrinmeanContentProvider.MESSAGES_CONTENT_URI, T_MESSAGES_ID + " = ?", new String[]{String.valueOf(inviewid)});
            }
        }
        c.close();
        client.release();
        Log.d(TAG, "end handleActionDeleteMsgFromChat");
    }

    private void handleActionAddUserToChat(int ChatID, int UserID) {
        Log.d(TAG, "start handleActionAddUserToChat");

        try {
            OAdUC out = rf.addusertochat(UserID, ChatID);
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

        OCrCh out = rf.createchat(ChatName);

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
            OLiUs out = rf.listuser(in);
            Serializer serializer = new Persister();
            StringWriter OutString = new StringWriter();

            serializer.write(out, OutString);

            mBroadcaster.notifyProgress(OutString.toString(), Constants.BROADCAST_LISTUSER);
        } catch (Exception e) {
            e.printStackTrace();
        }
        Log.d(TAG, "end handleActionListUser");
    }

/*    private void handleActionAuthenticateUser() {
        Log.d(TAG, "start handleActionAuthenticateUser");

        try {
            OAuth out = rf.authenticate(username, password);

            Serializer serializer = new Persister();
            StringWriter OutString = new StringWriter();

            serializer.write(out, OutString);

            mBroadcaster.notifyProgress(OutString.toString(), Constants.BROADCAST_AUTHENTICATE);
        } catch (Exception e) {
            e.printStackTrace();
        }
        Log.d(TAG, "end handleActionAuthenticateUser");
    } */

    private void handleActionSyncUser() {
        Log.d(TAG, "start hanleActionSyncUser");

        OLiUs out = rf.listuser("");
        if (out != null) {
            if (out.getET() == null || out.getET().isEmpty()) {

                HashMap<String, String> nameemail = new HashMap<>(1);
                final String[] PROJECTION = new String[]{
                        ContactsContract.CommonDataKinds.Email.CONTACT_ID,
                        ContactsContract.Contacts.DISPLAY_NAME,
                        ContactsContract.CommonDataKinds.Email.DATA};

                ContentResolver cr = getContentResolver();
                Cursor cursor = cr.query(ContactsContract.CommonDataKinds.Email.CONTENT_URI, PROJECTION, null, null, null);
                if (cursor != null) {
                    try {
                        final int contactIdIndex = cursor.getColumnIndex(ContactsContract.CommonDataKinds.Email.CONTACT_ID);
                        final int displayNameIndex = cursor.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME);
                        final int emailIndex = cursor.getColumnIndex(ContactsContract.CommonDataKinds.Email.DATA);
                        String displayName, address;
                        while (cursor.moveToNext()) {
                            displayName = cursor.getString(displayNameIndex);
                            address = cursor.getString(emailIndex).toLowerCase();
                            nameemail.put(address, displayName);
                        }
                    } finally {
                        cursor.close();
                    }
                }
                if (nameemail.size() > 0) {
                    for (int i = 0; i < out.getU().size(); i++) {
                        if (nameemail.containsKey(out.getU().get(i).getE().toLowerCase().trim())) {
                            int icnid = 0;
                            String icnvalue = null;
                            if (out.getU().get(i).getICID() > 0) {
                                OGImMMD outmeta = rf.getImageMessageMetaData(out.getU().get(i).getICID());

                                if (outmeta != null) {
                                    if (outmeta.getET() == null || outmeta.getET().isEmpty()) {

                                        if (!dbh.checkfileexists(outmeta.getIM(), Constants.TYP_ICON, outmeta.getIS(), outmeta.getIMD5())) {
                                            if (dbh.checkWIFI()) {
                                                OGImM ofim = rf.fetchImageMessage(out.getU().get(i).getICID(), Constants.TYP_ICON);
                                                if (ofim != null) {
                                                    if (ofim.getET() == null || ofim.getET().isEmpty()) {
                                                        icnvalue = icndir + File.separator + ofim.getIM();
                                                        icnid = out.getU().get(i).getICID();
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                            dbh.insertUserIntoDB(out.getU().get(i).getUID(), out.getU().get(i).getUN(), nameemail.get(out.getU().get(i).getE()), out.getU().get(i).getE(), out.getU().get(i).getLA(), icnid, icnvalue);
                        }
                    }
                }
            }
        }
        Log.d(TAG, "end hanleActionSyncUser");
    }

    private void handleSendUserIcon(String message) {
        Log.d(TAG, "start handleSendUserIcon");

        OSIcM outsendicon = rf.sendIconMessage(message);
        if (outsendicon != null) {
            if (outsendicon.getET() == null || outsendicon.getET().isEmpty()) {
                OIUIc outinsusericon = rf.insertusericon(outsendicon.getIcID());
                if (outinsusericon != null) {
                    if (outinsusericon.getET() == null || outinsusericon.getET().isEmpty()) {
                        dbh.moveFileToDestination(message, Constants.ICONDIR, outsendicon.getIcF());
                        File checkexists = new File(icndir + outsendicon.getIcF());
                        if (checkexists.exists()) {
                            SharedPreferences shP = PreferenceManager
                                    .getDefaultSharedPreferences(MeBaService.this);
                            SharedPreferences.Editor ed = shP.edit();
                            ed.putString(Constants.prefUserIcon, checkexists.getAbsolutePath());
                            ed.commit();
                            Toast.makeText(getApplicationContext(), R.string.user_icon_set, Toast.LENGTH_LONG).show();
                        }
                    }
                }

            }
        }
        Log.d(TAG, "end handleSendUserIcon");
    }

    private void handleSendChatIcon(String message, int chatid) {
        Log.d(TAG, "start handleSendUserIcon");

        if (chatid != -1) {
            OSIcM outsendicon = rf.sendIconMessage(message);
            if (outsendicon != null) {
                if (outsendicon.getET() == null || outsendicon.getET().isEmpty()) {
                    rf.insertchaticon(outsendicon.getIcID(), chatid);
                }
            }
            Log.d(TAG, "end handleSendUserIcon");
        }
    }
}