package de.radiohacks.frinmean.service;

import android.app.IntentService;
import android.content.ContentProviderClient;
import android.content.ContentValues;
import android.content.Intent;
import android.content.SharedPreferences;
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
import de.radiohacks.frinmean.model.OutAddUserToChat;
import de.radiohacks.frinmean.model.OutAuthenticate;
import de.radiohacks.frinmean.model.OutCreateChat;
import de.radiohacks.frinmean.model.OutListUser;
import de.radiohacks.frinmean.providers.FrinmeanContentProvider;

public class MeBaService extends IntentService {

    private static final String TAG = MeBaService.class.getSimpleName();
    private static final Object sSyncAdapterLock = new Object();
    private static SyncAdapter sSyncAdapter = null;
    public ConnectivityManager conManager = null;
    private String username;
    private String password;
    //private boolean https;
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

    public boolean isNetworkConnected() {
        return conManager.getActiveNetworkInfo().isConnected();
    }

    protected void getPreferenceInfo() {
        Log.d(TAG, "start getPreferenceInfo");
        SharedPreferences sharedPrefs = PreferenceManager
                .getDefaultSharedPreferences(this);

        //this.server = sharedPrefs.getString(Constants.PrefServername, "NULL");
        //this.https = sharedPrefs.getBoolean(Constants.PrefHTTPSCommunication, true);
        //if (this.https) {
        //    this.port = Integer.parseInt(sharedPrefs.getString(Constants.PrefServerport, "443"));
        //} else {
        //    this.port = Integer.parseInt(sharedPrefs.getString(Constants.PrefServerport, "80"));
        //}
        // this.userid = Integer.parseInt(sharedPrefs.getString(Constants.PrefUserID, "0"));
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
                final String ChatName = intent.getStringExtra(Constants.CHATNAME);
                final int cid = intent.getIntExtra(Constants.CHATID, -1);
                final int uid = intent.getIntExtra(Constants.USERID, -1);
                final String TextMessage = intent.getStringExtra(Constants.TEXTMESSAGE);
                insertMsgIntoDB(cid, uid, Constants.TYP_TEXT, TextMessage);
            } else if (Constants.ACTION_CREATECHAT.equalsIgnoreCase(action)) {
                final String ChatName = intent.getStringExtra(Constants.CHATNAME);
                handleActionCreateChat(ChatName);
            } else if (Constants.ACTION_SENDIMAGEMESSAGE.equalsIgnoreCase(action)) {
                final String ChatName = intent.getStringExtra(Constants.CHATNAME);
                final int cid = intent.getIntExtra(Constants.CHATID, -1);
                final int uid = intent.getIntExtra(Constants.USERID, -1);
                final String ImageLoc = intent.getStringExtra(Constants.IMAGELOCATION);
                insertImageMesgIntoDB(cid, uid, ImageLoc);
            } else if (Constants.ACTION_ADDUSERTOCHAT.equalsIgnoreCase(action)) {
                final int cid = intent.getIntExtra(Constants.CHATID, -1);
                final int uid = intent.getIntExtra(Constants.USERID, -1);
                handleActionAddUserToChat(cid, uid);
            } else if (Constants.ACTION_SENDVIDEOMESSAGE.equalsIgnoreCase(action)) {
                final String ChatName = intent.getStringExtra(Constants.CHATNAME);
                final int cid = intent.getIntExtra(Constants.CHATID, -1);
                final String VideoLoc = intent.getStringExtra(Constants.VIDEOLOCATION);
                //handleActionSendVideoMessage(ChatName, cid, VideoLoc);
            } else if (Constants.ACTION_RELOAD_SETTING.equalsIgnoreCase(action)) {
                getPreferenceInfo();
                //buildServerURL();
            } else if (Constants.ACTION_FULLSYNC.equalsIgnoreCase(action)) {
                final String ChatName = intent.getStringExtra(Constants.CHATNAME);
                final int cid = intent.getIntExtra(Constants.CHATID, -1);
                sSyncAdapter.syncGetMessageFromChat(cid, 0, ChatName);
            } else if (Constants.ACTION_AUTHENTICATE.equalsIgnoreCase(action)) {
                handleActionAuthenticateUser();
            }
        }
        Log.d(TAG, "start onHandleIntent");
    }

    private void handleActionAddUserToChat(int ChatID, int UserID) {
        Log.d(TAG, "start handleActionAddUserToChat");

        try {
            OutAddUserToChat out = rf.addusertochat(username, password, UserID, ChatID);
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

        OutCreateChat out = rf.createchat(username, password, ChatName);

        if (out.getErrortext() == null || out.getErrortext().isEmpty()) {
            if ((out.getChatname().equals(ChatName)) && (out.getChatID() > 0)) {
                // mBroadcaster.notifyProgress(Constants.BROADCAST_CREATECHAT, Constants.BROADCAST_CREATECHAT);
                SyncUtils.TriggerRefresh();
            }
        }
        Log.d(TAG, "end handleActionCreateChat");
    }

    private void handleActionListUser(String in) {
        Log.d(TAG, "start handleActionListUser");

        try {
            OutListUser out = rf.listuser(username, password, in);
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
            OutAuthenticate out = rf.authenticate(username, password);

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
        Log.d(TAG, "start handleActionSendImageMessage");

        // First check if File is already in the Image Folder with the right size
        // If not, copy File to the Image Directory with the given Name
        // Then insert Entry into DB for next sync

        File orgFile = new File(Message);

        String localfname = new String();
        if (directory.endsWith("/")) {
            localfname += directory + Constants.IMAGEDIR;
        } else {
            localfname += directory + "/" + Constants.IMAGEDIR;
        }

        if (!orgFile.getAbsolutePath().equalsIgnoreCase(localfname + "/" + orgFile.getName())) {
            // Copy file
            try {
                copy(orgFile, new File(localfname + "/" + orgFile.getName()));
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        insertMsgIntoDB(ChatID, UserID, Constants.TYP_IMAGE, orgFile.getName());
        Log.d(TAG, "end handleActionSendImageMessage");
    }

    private void insertMsgIntoDB(int ChatID, int UserID, String MessageType, String Message) {
        Log.d(TAG, "start insertMsgIntoDB");

        // Insert new Message into local DB and trigger Sync to upload the Information.
        // To find the not send messages the Backend ID musst be 0 and the
        // Sendtimestamp musst be 0
        // The Readtimestamp and the MessageIDs are supplied by the Server
        // The ChatID is needed to insert the Message into the right Chat afterwards

        ContentValues valuesins = new ContentValues();
        valuesins.put(Constants.T_MESSAGES_BADBID, 0);
        valuesins.put(Constants.T_MESSAGES_OwningUserID, UserID);
        valuesins.put(Constants.T_MESSAGES_OwningUserName, username);
        valuesins.put(Constants.T_MESSAGES_ChatID, ChatID);
        valuesins.put(Constants.T_MESSAGES_MessageTyp, MessageType);
        valuesins.put(Constants.T_MESSAGES_SendTimestamp, 0);
        valuesins.put(Constants.T_MESSAGES_ReadTimestamp, 0);
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

        Log.d(TAG, "end insertMsgIntoDB");
    }
}