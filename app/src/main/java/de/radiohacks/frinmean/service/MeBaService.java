package de.radiohacks.frinmean.service;

import android.app.IntentService;
import android.content.ContentProviderClient;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Environment;
import android.os.IBinder;
import android.os.RemoteException;
import android.preference.PreferenceManager;
import android.util.Log;

import com.google.common.hash.HashCode;
import com.google.common.hash.Hashing;
import com.google.common.io.Files;

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
import de.radiohacks.frinmean.modelshort.C;
import de.radiohacks.frinmean.modelshort.M;
import de.radiohacks.frinmean.modelshort.OAckMD;
import de.radiohacks.frinmean.modelshort.OAdUC;
import de.radiohacks.frinmean.modelshort.OAuth;
import de.radiohacks.frinmean.modelshort.OCrCh;
import de.radiohacks.frinmean.modelshort.ODMFC;
import de.radiohacks.frinmean.modelshort.ODeCh;
import de.radiohacks.frinmean.modelshort.OFMFC;
import de.radiohacks.frinmean.modelshort.OGImM;
import de.radiohacks.frinmean.modelshort.OGImMMD;
import de.radiohacks.frinmean.modelshort.OGTeM;
import de.radiohacks.frinmean.modelshort.OGViM;
import de.radiohacks.frinmean.modelshort.OGViMMD;
import de.radiohacks.frinmean.modelshort.OIMIC;
import de.radiohacks.frinmean.modelshort.OLiCh;
import de.radiohacks.frinmean.modelshort.OLiUs;
import de.radiohacks.frinmean.modelshort.OSShT;
import de.radiohacks.frinmean.providers.FrinmeanContentProvider;

import static de.radiohacks.frinmean.Constants.CHAT_DB_Columns;
import static de.radiohacks.frinmean.Constants.ID_MESSAGES_ImageMsgID;
import static de.radiohacks.frinmean.Constants.ID_MESSAGES_VideoMsgID;
import static de.radiohacks.frinmean.Constants.MESSAGES_DB_Columns;
import static de.radiohacks.frinmean.Constants.TYP_CONTACT;
import static de.radiohacks.frinmean.Constants.TYP_FILE;
import static de.radiohacks.frinmean.Constants.TYP_IMAGE;
import static de.radiohacks.frinmean.Constants.TYP_LOCATION;
import static de.radiohacks.frinmean.Constants.TYP_TEXT;
import static de.radiohacks.frinmean.Constants.TYP_VIDEO;
import static de.radiohacks.frinmean.Constants.T_CHAT_BADBID;
import static de.radiohacks.frinmean.Constants.T_CHAT_ChatName;
import static de.radiohacks.frinmean.Constants.T_CHAT_ID;
import static de.radiohacks.frinmean.Constants.T_CHAT_OwningUserID;
import static de.radiohacks.frinmean.Constants.T_CHAT_OwningUserName;
import static de.radiohacks.frinmean.Constants.T_MESSAGES_BADBID;
import static de.radiohacks.frinmean.Constants.T_MESSAGES_ChatID;
import static de.radiohacks.frinmean.Constants.T_MESSAGES_ContactMsgID;
import static de.radiohacks.frinmean.Constants.T_MESSAGES_FileMsgID;
import static de.radiohacks.frinmean.Constants.T_MESSAGES_ID;
import static de.radiohacks.frinmean.Constants.T_MESSAGES_ImageMsgID;
import static de.radiohacks.frinmean.Constants.T_MESSAGES_ImageMsgValue;
import static de.radiohacks.frinmean.Constants.T_MESSAGES_LocationMsgID;
import static de.radiohacks.frinmean.Constants.T_MESSAGES_MessageTyp;
import static de.radiohacks.frinmean.Constants.T_MESSAGES_NumberAll;
import static de.radiohacks.frinmean.Constants.T_MESSAGES_NumberRead;
import static de.radiohacks.frinmean.Constants.T_MESSAGES_NumberShow;
import static de.radiohacks.frinmean.Constants.T_MESSAGES_OwningUserID;
import static de.radiohacks.frinmean.Constants.T_MESSAGES_OwningUserName;
import static de.radiohacks.frinmean.Constants.T_MESSAGES_ReadTimestamp;
import static de.radiohacks.frinmean.Constants.T_MESSAGES_SendTimestamp;
import static de.radiohacks.frinmean.Constants.T_MESSAGES_ShowTimestamp;
import static de.radiohacks.frinmean.Constants.T_MESSAGES_TIME_BADBID;
import static de.radiohacks.frinmean.Constants.T_MESSAGES_TIME_UserID;
import static de.radiohacks.frinmean.Constants.T_MESSAGES_TextMsgID;
import static de.radiohacks.frinmean.Constants.T_MESSAGES_TextMsgValue;
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
    private boolean contentall;
    private boolean isWifi;
    private BroadcastNotifier mBroadcaster = null;
    private RestFunctions rf;

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
        File vidFile = new File(imgdir);
        if (!vidFile.exists()) {
            if (!vidFile.mkdir()) {
                Log.e(TAG, "Video Directory creation failed");
            }
        }
        fildir = basedir + File.separator + Constants.FILESDIR + File.separator;
        File filFile = new File(imgdir);
        if (!filFile.exists()) {
            if (!filFile.mkdir()) {
                Log.e(TAG, "File Directory creation failed");
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

    private void checkNetwork() {
        ConnectivityManager cm =
                (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);

        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();

        isWifi = contentall || activeNetwork != null && activeNetwork.getType() == ConnectivityManager.TYPE_WIFI;
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
//        this.directory = sharedPrefs.getString(Constants.PrefDirectory, "NULL");
        this.contentall = sharedPrefs.getBoolean(Constants.prefContentCommunication, false);
        this.userid = sharedPrefs.getInt(Constants.PrefUserID, -1);
        Log.d(TAG, "end getPferefenceInfo");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        Log.d(TAG, "start onHandleIntent");
        if (intent != null) {
            final String action = intent.getAction();
            checkNetwork();

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
            }
        }
        Log.d(TAG, "start onHandleIntent");
    }

    private boolean acknowledgeMessage(String msgType, String message, int msgid) {
        Log.d(TAG, "start acknowledgeMessage");

        boolean ret = false;

        if (msgType.equalsIgnoreCase(Constants.TYP_TEXT)) {
            /* Hasher hasher = Hashing.md5().newHasher();
            hasher.putBytes(message.getBytes());
            byte[] md5 = hasher.hash().asBytes(); */
            int hashCode = message.hashCode();

            OAckMD oack = rf.acknowledgemessagedownload(username, password, msgid, String.valueOf(hashCode));
            if (oack != null) {
                if (oack.getET() == null || oack.getET().isEmpty()) {
                    if (oack.getACK().equalsIgnoreCase(Constants.ACKNOWLEDGE_TRUE)) {
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
            assert md5 != null;
            OAckMD oack = rf.acknowledgemessagedownload(username, password, msgid, md5.toString());
            if (oack != null) {
                if (oack.getET() == null || oack.getET().isEmpty()) {
                    if (oack.getACK().equalsIgnoreCase(Constants.ACKNOWLEDGE_TRUE)) {
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
            assert md5 != null;
            OAckMD oack = rf.acknowledgemessagedownload(username, password, msgid, md5.toString());
            if (oack != null) {
                if (oack.getET() == null || oack.getET().isEmpty()) {
                    if (oack.getACK().equalsIgnoreCase(Constants.ACKNOWLEDGE_TRUE)) {
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

        if (msgType.equalsIgnoreCase(Constants.TYP_IMAGE)) {
            checkfilepath = imgdir + fname;
        } else if (msgType.equalsIgnoreCase(Constants.TYP_VIDEO)) {
            checkfilepath = viddir + fname;
        } else if (msgType.equalsIgnoreCase(Constants.TYP_FILE)) {
            checkfilepath = fildir + fname;
        }

        assert checkfilepath != null;
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
                assert md5 != null;
                if (md5.toString().equals(inmd5sumd)) {
                    // MD5Sum is equal File already exists
                    ret = true;
                }
            }
        }
        Log.d(TAG, "end checkfileexists");
        return ret;
    }

    private void handleActionRefresh(long intime) {
        Log.d(TAG, "start handleActionRefresh");

        try {
            OLiCh outListChat = rf.listchat(username, password);
            if (outListChat != null && outListChat.getET() == null) {
                ContentProviderClient clientChat = getContentResolver().acquireContentProviderClient(FrinmeanContentProvider.CHAT_CONTENT_URI);
                for (int j = 0; j < outListChat.getC().size(); j++) {
                    C c = outListChat.getC().get(j);
                    ContentValues valuesinschat = new ContentValues();
                    valuesinschat.put(T_CHAT_BADBID, c.getCID());
                    valuesinschat.put(T_CHAT_OwningUserID, c.getOU().getOUID());
                    valuesinschat.put(T_CHAT_OwningUserName, c.getOU().getOUN());
                    valuesinschat.put(T_CHAT_ChatName, c.getCN());
                    ((FrinmeanContentProvider) clientChat.getLocalContentProvider()).insertorupdate(FrinmeanContentProvider.CHAT_CONTENT_URI, valuesinschat);
                    OFMFC outFetchMessage = rf.getmessagefromchat(username, password, c.getCID(), intime);
                    if (outFetchMessage != null && outFetchMessage.getET() == null) {
                        for (int k = 0; k < outFetchMessage.getM().size(); k++) {
                            M m = outFetchMessage.getM().get(k);
                            ContentValues valuesinsmsg = new ContentValues();
                            valuesinsmsg.put(T_MESSAGES_BADBID, m.getMID());
                            valuesinsmsg.put(T_MESSAGES_OwningUserID, m.getOU().getOUID());
                            valuesinsmsg.put(T_MESSAGES_OwningUserName, m.getOU().getOUN());
                            valuesinsmsg.put(T_MESSAGES_ChatID, c.getCID());
                            valuesinsmsg.put(T_MESSAGES_MessageTyp, m.getMT());
                            valuesinsmsg.put(T_MESSAGES_SendTimestamp, m.getSdT());
                            valuesinsmsg.put(T_MESSAGES_ReadTimestamp, m.getRdT());
                            valuesinsmsg.put(T_MESSAGES_ShowTimestamp, m.getShT());
                            valuesinsmsg.put(T_MESSAGES_NumberAll, m.getNT());
                            valuesinsmsg.put(T_MESSAGES_NumberShow, m.getNS());
                            valuesinsmsg.put(T_MESSAGES_NumberRead, m.getNR());

                            if (m.getMT().equalsIgnoreCase(TYP_TEXT)) {
                                valuesinsmsg.put(T_MESSAGES_TextMsgID, m.getTMID());
                                OGTeM oftm = rf.gettextmessage(username, password, m.getTMID());
                                if (oftm != null) {
                                    if (oftm.getET() == null || oftm.getET().isEmpty()) {
                                        if (acknowledgeMessage(Constants.TYP_TEXT, oftm.getTM(), m.getMID())) {
                                            valuesinsmsg.put(T_MESSAGES_TextMsgValue, oftm.getTM());
                                            ContentProviderClient clientmsg = getContentResolver().acquireContentProviderClient(FrinmeanContentProvider.MESSAGES_CONTENT_URI);
                                            ((FrinmeanContentProvider) clientmsg.getLocalContentProvider()).insertorupdate(FrinmeanContentProvider.MESSAGES_CONTENT_URI, valuesinsmsg);
                                            clientmsg.release();
                                        }
                                    }
                                }
                            } else if (m.getMT().equalsIgnoreCase(TYP_IMAGE)) {
                                valuesinsmsg.put(T_MESSAGES_ImageMsgID, m.getIMID());
                                OGImMMD outmeta = rf.getImageMessageMetaData(username, password, m.getIMID());

                                if (outmeta != null) {
                                    if (outmeta.getET() == null || outmeta.getET().isEmpty()) {

                                        if (!checkfileexists(outmeta.getIM(), TYP_IMAGE, outmeta.getIS(), outmeta.getIMD5())) {
                                            if (isWifi) {
                                                OGImM ofim = rf.fetchImageMessage(username, password, m.getIMID());
                                                if (ofim != null) {
                                                    if (ofim.getET() == null || ofim.getET().isEmpty()) {
                                                        String checkfilepath;

                                                        checkfilepath = imgdir + ofim.getIM();
                                                        if (acknowledgeMessage(Constants.TYP_IMAGE, checkfilepath, m.getMID())) {
                                                            valuesinsmsg.put(T_MESSAGES_ImageMsgValue, checkfilepath);
                                                            ContentProviderClient client = getContentResolver().acquireContentProviderClient(FrinmeanContentProvider.MESSAGES_CONTENT_URI);
                                                            ((FrinmeanContentProvider) client.getLocalContentProvider()).insertorupdate(FrinmeanContentProvider.MESSAGES_CONTENT_URI, valuesinsmsg);
                                                            client.release();

                                                            Intent mediaScanIntent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
                                                            Uri contentUri = Uri.fromFile(new File(checkfilepath));
                                                            mediaScanIntent.setData(contentUri);
                                                            sendBroadcast(mediaScanIntent);
                                                        }
                                                    }
                                                }
                                            }
                                        } else {
                                            String checkfilepath;
                                            checkfilepath = imgdir + outmeta.getIM();
                                            if (acknowledgeMessage(Constants.TYP_IMAGE, checkfilepath, m.getMID())) {
                                                valuesinsmsg.put(T_MESSAGES_ImageMsgValue, checkfilepath);
                                                ContentProviderClient client = getContentResolver().acquireContentProviderClient(FrinmeanContentProvider.MESSAGES_CONTENT_URI);
                                                ((FrinmeanContentProvider) client.getLocalContentProvider()).insertorupdate(FrinmeanContentProvider.MESSAGES_CONTENT_URI, valuesinsmsg);
                                                client.release();
                                            }
                                        }
                                    }
                                }
                            } else if (m.getMT().equalsIgnoreCase(TYP_CONTACT)) {
                                valuesinsmsg.put(T_MESSAGES_ContactMsgID, m.getCMID());
                                ContentProviderClient client = getContentResolver().acquireContentProviderClient(FrinmeanContentProvider.MESSAGES_CONTENT_URI);
                                ((FrinmeanContentProvider) client.getLocalContentProvider()).insertorupdate(FrinmeanContentProvider.MESSAGES_CONTENT_URI, valuesinsmsg);
                                client.release();
//                OutFetchContactMessage ofcm = checkAndDownloadImageMessage(m.getImageMsgID());
//                if (ofcm.getET() == null || ofcm.getET().isEmpty()) {
//                    valuesinsmsg.put(Constants.T_MESSAGES_ContactMsgValue, ofcm.getImageMessage());
//                    fcp.insertorupdate(FrinmeanContentProvider.CONTENT_URI, valuesins);
//                }
                            } else if (m.getMT().equalsIgnoreCase(TYP_FILE)) {
                                valuesinsmsg.put(T_MESSAGES_FileMsgID, m.getFMID());
                                ContentProviderClient client = getContentResolver().acquireContentProviderClient(FrinmeanContentProvider.MESSAGES_CONTENT_URI);
                                ((FrinmeanContentProvider) client.getLocalContentProvider()).insertorupdate(FrinmeanContentProvider.MESSAGES_CONTENT_URI, valuesinsmsg);
                                client.release();
//                OutFetchFileMessage offm = checkAndDownloadImageMessage(m.getImageMsgID());
//                if (offm.getET() == null || offm.getET().isEmpty()) {
//                    valuesinsmsg.put(Constants.T_MESSAGES_ContactMsgValue, offm.getImageMessage());
//                    fcp.insertorupdate(FrinmeanContentProvider.CONTENT_URI, valuesins);
//                }
                            } else if (m.getMT().equalsIgnoreCase(TYP_LOCATION)) {
                                valuesinsmsg.put(T_MESSAGES_LocationMsgID, m.getLMID());
                                ContentProviderClient client = getContentResolver().acquireContentProviderClient(FrinmeanContentProvider.MESSAGES_CONTENT_URI);
                                ((FrinmeanContentProvider) client.getLocalContentProvider()).insertorupdate(FrinmeanContentProvider.MESSAGES_CONTENT_URI, valuesinsmsg);
                                client.release();
//                OutFetchLocationMessage oflm = checkAndDownloadImageMessage(m.getImageMsgID());
//                if (oflm.getET() == null || oflm.getET().isEmpty()) {
//                    valuesinsmsg.put(Constants.T_MESSAGES_ContactMsgValue, oflm.getImageMessage());
//                    fcp.insertorupdate(FrinmeanContentProvider.CONTENT_URI, valuesins);
//                }
                            } else if (m.getMT().equalsIgnoreCase(TYP_VIDEO)) {
                                valuesinsmsg.put(T_MESSAGES_VideoMsgID, m.getVMID());
                                OGViMMD outmeta = rf.getVideoMessageMetaData(username, password, m.getVMID());

                                if (outmeta != null) {
                                    if (outmeta.getET() == null || outmeta.getET().isEmpty()) {
                                        if (!checkfileexists(outmeta.getVM(), TYP_VIDEO, outmeta.getVS(), outmeta.getVMD5())) {
                                            if (isWifi) {
                                                OGViM ofvm = rf.fetchVideoMessage(username, password, m.getVMID());
                                                if (ofvm != null) {
                                                    if (ofvm.getET() == null || ofvm.getET().isEmpty()) {
                                                        String checkfilepath = viddir + ofvm.getVM();
                                                        if (acknowledgeMessage(Constants.TYP_VIDEO, checkfilepath, m.getMID())) {
                                                            valuesinsmsg.put(T_MESSAGES_VideoMsgValue, checkfilepath);
                                                            ContentProviderClient client = getContentResolver().acquireContentProviderClient(FrinmeanContentProvider.MESSAGES_CONTENT_URI);
                                                            ((FrinmeanContentProvider) client.getLocalContentProvider()).insertorupdate(FrinmeanContentProvider.MESSAGES_CONTENT_URI, valuesinsmsg);
                                                            client.release();
                                                        }
                                                    }
                                                }
                                            }
                                        } else {
                                            String checkfilepath = viddir + outmeta.getVM();
                                            if (acknowledgeMessage(Constants.TYP_IMAGE, checkfilepath, m.getMID())) {
                                                valuesinsmsg.put(T_MESSAGES_VideoMsgValue, checkfilepath);
                                                ContentProviderClient client = getContentResolver().acquireContentProviderClient(FrinmeanContentProvider.MESSAGES_CONTENT_URI);
                                                ((FrinmeanContentProvider) client.getLocalContentProvider()).insertorupdate(FrinmeanContentProvider.MESSAGES_CONTENT_URI, valuesinsmsg);
                                                client.release();
                                            }
                                        }
                                    }
                                }
                            }
                            inserIntoTimeTable(m.getMID(), userid);
                        }
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        // ToDo setze Preference wenn Backup durchgeführt ist. Wichtig für StartActivity im onResume.
        Log.d(TAG, "end handleActionRefresh");
    }

    private void inserIntoTimeTable(int inBAID, int inUSID) {
        ContentValues valuesins = new ContentValues();
        valuesins.put(T_MESSAGES_TIME_BADBID, inBAID);
        valuesins.put(T_MESSAGES_TIME_UserID, inUSID);
        ContentProviderClient client = getContentResolver().acquireContentProviderClient(FrinmeanContentProvider.MESSAGES_TIME_CONTENT_URI);
        ((FrinmeanContentProvider) client.getLocalContentProvider()).insertorupdate(FrinmeanContentProvider.MESSAGES_TIME_CONTENT_URI, valuesins);
        client.release();
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

        while (c.moveToNext()) {
            OSShT outsst = rf.setshowtimestamp(username, password, c.getInt(Constants.ID_MESSAGES_BADBID));
            if (outsst != null) {
                if (outsst.getET() == null || outsst.getET().isEmpty()) {
                    ContentValues valuesins = new ContentValues();
                    valuesins.put(Constants.T_MESSAGES_BADBID, outsst.getMID());
                    valuesins.put(Constants.T_MESSAGES_ShowTimestamp, outsst.getShT());
                    ((FrinmeanContentProvider) client.getLocalContentProvider()).insertorupdate(FrinmeanContentProvider.MESSAGES_CONTENT_URI, valuesins);
                }
            }
        }
        c.close();
        client.release();
        getContentResolver().notifyChange(FrinmeanContentProvider.CHAT_CONTENT_URI, null);
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
            OIMIC out = rf.insertmessageintochat(username, password, ChatID, ContentMsgID, msgType);
            insertFwdMsgIntoDB(ChatID, UserID, out.getMID(), out.getSdT(), msgType, ContentMessage, ContentMsgID);
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
                    ODMFC out = rf.deletemessagefromchat(username, password, c.getInt(Constants.ID_MESSAGES_BADBID));
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

        if (!orgFile.getAbsolutePath().equalsIgnoreCase(imgdir + orgFile.getName())) {
            // Copy file
            try {
                copy(orgFile, new File(imgdir + orgFile.getName()));
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        insertNewMsgIntoDB(ChatID, UserID, Constants.TYP_IMAGE, imgdir + orgFile.getName());
        Log.d(TAG, "end insertImageMesgIntoDB");
    }

    private void insertVideoMesgIntoDB(int ChatID, int UserID, String Message) {
        Log.d(TAG, "start insertVideoMesgIntoDB");

        // First check if File is already in the Image Folder with the right size
        // If not, copy File to the Image Directory with the given Name
        // Then insert Entry into DB for next sync

        File orgFile = new File(Message);

        if (!orgFile.getAbsolutePath().equalsIgnoreCase(viddir + orgFile.getName())) {
            // Copy file
            try {
                copy(orgFile, new File(viddir + orgFile.getName()));
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        insertNewMsgIntoDB(ChatID, UserID, Constants.TYP_VIDEO, viddir + orgFile.getName());
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
        ContentProviderClient client = getContentResolver().acquireContentProviderClient(FrinmeanContentProvider.MESSAGES_CONTENT_URI);
        client.getLocalContentProvider().insert(FrinmeanContentProvider.MESSAGES_CONTENT_URI, valuesins);
        client.release();
        SyncUtils.TriggerRefresh();

        Log.d(TAG, "end insertMsgIntoDB");
    }

    private void insertFwdMsgIntoDB(int ChatID, int UserID, int MsgID, long timeStamp, String MessageType, String ContentMessage, int ContentMsgID) {
        Log.d(TAG, "start insertFwdMsgIntoDB");

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
        ContentProviderClient client = getContentResolver().acquireContentProviderClient(FrinmeanContentProvider.MESSAGES_CONTENT_URI);
        client.getLocalContentProvider().insert(FrinmeanContentProvider.MESSAGES_CONTENT_URI, valuesins);
        client.release();

        inserIntoTimeTable(MsgID, UserID);
        SyncUtils.TriggerRefresh();

        Log.d(TAG, "end insertFwdMsgIntoDB");
    }
}