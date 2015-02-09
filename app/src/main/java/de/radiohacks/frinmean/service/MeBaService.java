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

import org.apache.http.HttpStatus;
import org.simpleframework.xml.Serializer;
import org.simpleframework.xml.core.Persister;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Reader;
import java.io.StringReader;
import java.io.StringWriter;

import de.radiohacks.frinmean.Constants;
import de.radiohacks.frinmean.adapters.SyncAdapter;
import de.radiohacks.frinmean.adapters.SyncUtils;
import de.radiohacks.frinmean.model.OutAddUserToChat;
import de.radiohacks.frinmean.model.OutAuthenticate;
import de.radiohacks.frinmean.model.OutCreateChat;
import de.radiohacks.frinmean.model.OutListUser;
import de.radiohacks.frinmean.model.OutSendImageMessage;
import de.radiohacks.frinmean.providers.FrinmeanContentProvider;

public class MeBaService extends IntentService {

    private static final String TAG = MeBaService.class.getSimpleName();
    private static final Object sSyncAdapterLock = new Object();
    private static SyncAdapter sSyncAdapter = null;
    // private final IBinder mBinder = new LocalBinder();
    public ConnectivityManager conManager = null;
    private String server;
    private String username;
    private String password;
    private boolean https;
    private String CommunicationURL;
    private int port;
    private int userid;
    private String directory;
    // private int freq;
    // private LocalDBHandler ldb = null;
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

        buildServerURL();

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

        this.server = sharedPrefs.getString(Constants.PrefServername, "NULL");
        this.https = sharedPrefs.getBoolean(Constants.PrefHTTPSCommunication, true);
        if (this.https) {
            this.port = Integer.parseInt(sharedPrefs.getString(Constants.PrefServerport, "443"));
        } else {
            this.port = Integer.parseInt(sharedPrefs.getString(Constants.PrefServerport, "80"));
        }
        this.username = sharedPrefs.getString(Constants.PrefUsername, "NULL");
        this.password = sharedPrefs.getString(Constants.PrefPassword, "NULL");
        this.directory = sharedPrefs.getString(Constants.PrefDirectory, "NULL");
        Log.d(TAG, "end getPferefenceInfo");
    }

    protected void buildServerURL() {
        this.CommunicationURL = "";
        if (this.https) {
            this.CommunicationURL += "https://";
        } else {
            this.CommunicationURL += "http://";
        }
        this.CommunicationURL += server + ":" + port + "/frinmeba/";
    }

    protected boolean checkServer() {
        Log.d(TAG, "start checkserver");
        boolean ret = false;
        if (this.server != null && !this.server.equalsIgnoreCase("NULL") && !this.server.isEmpty()) {
            if (this.username != null && !this.username.equalsIgnoreCase("NULL") && !this.username.isEmpty()) {
                if (this.password != null && !this.password.equalsIgnoreCase("NULL") && !this.password.isEmpty()) {
                    ret = true;
                }
            }
        }
        Log.d(TAG, "end checkServer");
        return ret;
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
                final String TextMessage = intent.getStringExtra(Constants.TEXTMESSAGE);
                insertMsgIntoDB(cid, Constants.TYP_TEXT, TextMessage);
                // handleActionSendTextMessage(ChatName, cid, TextMessage);
                SyncUtils.TriggerRefresh();
            } else if (Constants.ACTION_CREATECHAT.equalsIgnoreCase(action)) {
                final String ChatName = intent.getStringExtra(Constants.CHATNAME);
                handleActionCreateChat(ChatName);
            } else if (Constants.ACTION_SENDIMAGEMESSAGE.equalsIgnoreCase(action)) {
                final String ChatName = intent.getStringExtra(Constants.CHATNAME);
                final int cid = intent.getIntExtra(Constants.CHATID, -1);
                final String ImageLoc = intent.getStringExtra(Constants.IMAGELOCATION);
                insertImageMesgIntoDB(cid, ImageLoc);
                SyncUtils.TriggerRefresh();
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
                buildServerURL();
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

        OutAddUserToChat out = rf.addusertochat(username, password, UserID, ChatID);
        mBroadcaster.notifyProgress(out.getResult(), Constants.BROADCAST_USERADDEDTOCHAT);

        Log.d(TAG, "end handleActionAddUserToChat");
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

    private void insertImageMesgIntoDB(int ChatID, String Message) {
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

        if (!orgFile.getAbsolutePath().equalsIgnoreCase(localfname)) {
            // Copy file
            try {
                copy(orgFile, new File(localfname + "/" + orgFile.getName()));
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        insertMsgIntoDB(ChatID, Constants.TYP_IMAGE, orgFile.getName());
        Log.d(TAG, "end handleActionSendImageMessage");
    }

    // ToDo Umschreiben, RC nicht ausführen, Nachricht lokal speichern ohne SendTime und BackendID
    private String uploadImageMessage(String ChatName, int ChatID, String Message) {
        Log.d(TAG, "start uploadImageMessage");
        String serverfilename = null;
        if (checkServer()) {
            RestClient rcsend;
            rcsend = new RestClient(CommunicationURL + "image/upload", https, port);
            try {
                rcsend.AddHeader("enctype", "multipart/form-data");
                rcsend.AddParam(Constants.USERNAME, username);
                rcsend.AddParam(Constants.PASSWORD, password);
                rcsend.setFilename(Message);

                String ret = rcsend.ExecuteRequestUploadXML(rcsend.BevorExecutePost());

                if (rcsend.getResponseCode() == HttpStatus.SC_OK) {
                    Serializer sersendtxtmsg = new Persister();
                    Reader readersendimgmsg = new StringReader(ret);

                    OutSendImageMessage ressend = sersendtxtmsg.read(OutSendImageMessage.class, readersendimgmsg, false);

                    if (ressend != null) {
                        if (ressend.getErrortext() == null || ressend.getErrortext().isEmpty()) {
                            if (ressend.getImageID() != null && ressend.getImageID() > 0) {
                                serverfilename = ressend.getImageFileName();
                                // OutInsertMessageIntoChat ins = insertMessageIntoChatAndDB(ChatName, ressend.getImageID(), ChatID, Constants.TYP_IMAGE, ressend.getImageFileName());
                                // insertMessageIntoChatAndDB(ChatName, ressend.getImageID(), ChatID, Constants.TYP_IMAGE, ressend.getImageFileName());
                            }
                        }
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        Log.d(TAG, "end uploadImageMessage");
        return serverfilename;
    }

    private void insertMsgIntoDB(int ChatID, String MessageType, String Message) {
        Log.d(TAG, "start insertMsgIntoDB");

        // Insert new Message into local DB and trigger Sync to upload the Information.
        // To find the not send messages the Backend ID musst be 0 and the
        // Sendtimestamp musst be 0
        // The Readtimestamp and the MessageIDs are supplied by the Server
        // The ChatID is needed to insert the Message into the right Chat afterwards

        ContentValues valuesins = new ContentValues();
        valuesins.put(Constants.T_MESSAGES_BADBID, 0);
        valuesins.put(Constants.T_MESSAGES_OwningUserID, userid);
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
        ((FrinmeanContentProvider) client.getLocalContentProvider()).insert(FrinmeanContentProvider.MESSAES_CONTENT_URI, valuesins);

        Log.d(TAG, "end insertMsgIntoDB");
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

        OutListUser out = rf.listuser(username, password, in);

        mBroadcaster.notifyProgress(out.toString(), Constants.BROADCAST_LISTUSER);

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

/*    private void handleActionGetMessageFromChat(int cid, long readtime, String CName) {
        Log.d(TAG, "start handleActionGetMessageFromChat");
        if (checkServer()) {
            RestClient rc;
            rc = new RestClient(CommunicationURL + "user/getmessagefromchat", https, port);
            rc.AddParam(Constants.USERNAME, username);
            rc.AddParam(Constants.PASSWORD, password);
            rc.AddParam(Constants.CHATID, String.valueOf(cid));
            rc.AddParam(Constants.TIMESTAMP, String.valueOf(readtime));

            try {
                String ret = rc.ExecuteRequestXML(rc.BevorExecuteGetQuery());
                if (rc.getResponseCode() == HttpStatus.SC_OK) {
                    Serializer serializer = new Persister();
                    Reader reader = new StringReader(ret);

                    OutFetchMessageFromChat res = serializer.read(OutFetchMessageFromChat.class, reader, false);

                    if (res != null) {
                        if (res.getErrortext() == null || res.getErrortext().isEmpty()) {
                            if (res.getMessage() != null && res.getMessage().size() > 0) {
                                SaveMessageToLDB(res.getMessage(), cid, CName);
                            }
                        }
                    }
                    mBroadcaster.notifyProgress(ret, Constants.BROADCAST_GETMESSAGEFROMCHAT);
                }

            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        Log.d(TAG, "end handleActionGetMessageFromChat");
    } */

    // ToDo Umschreiben, wird im SyncService gemacht, nicht hier
    /* private void SaveChatsToLDB(List<Chat> in) {
        Log.d(TAG, "start SaveChatsToLDB");
        for (int j = 0; j < in.size(); j++) {
            Chat c = in.get(j);
            ContentValues valuesins = new ContentValues();
            valuesins.put(Constants.T_CHAT_BADBID, c.getChatID());
            valuesins.put(Constants.T_CHAT_OwningUserID, c.getOwningUser().getOwningUserID());
            valuesins.put(Constants.T_CHAT_OwningUserName, c.getOwningUser().getOwningUserName());
            valuesins.put(Constants.T_CHAT_ChatName, c.getChatname());
            ContentProviderClient client = getContentResolver().acquireContentProviderClient(FrinmeanContentProvider.CHAT_CONTENT_URI);
            ((FrinmeanContentProvider) client.getLocalContentProvider()).insertorupdate(FrinmeanContentProvider.CHAT_CONTENT_URI, valuesins);
            client.release();
        }
        Log.d(TAG, "end saveChatsToLDB");
    } */

/*    private void SaveMessageToLDB(List<Message> in, int ChatID, String ChatName) {
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
                OutFetchTextMessage oftm = fetchTextMessage(m.getTextMsgID());
                if (oftm.getErrortext() == null || oftm.getErrortext().isEmpty()) {
                    valuesins.put(Constants.T_MESSAGES_TextMsgValue, oftm.getTextMessage());
                    ContentProviderClient client = getContentResolver().acquireContentProviderClient(FrinmeanContentProvider.MESSAES_CONTENT_URI);
                    ((FrinmeanContentProvider) client.getLocalContentProvider()).insertorupdate(FrinmeanContentProvider.MESSAES_CONTENT_URI, valuesins);
                    client.release();
                }
            } else if (m.getMessageTyp().equalsIgnoreCase(Constants.TYP_IMAGE)) {
                valuesins.put(Constants.T_MESSAGES_ImageMsgID, m.getTextMsgID());
                OutFetchImageMessage ofim = checkAndDownloadImageMessage(m.getImageMsgID());
                if (ofim.getErrortext() == null || ofim.getErrortext().isEmpty()) {
                    valuesins.put(Constants.T_MESSAGES_ImageMsgValue, ofim.getImageMessage());
                    ContentProviderClient client = getContentResolver().acquireContentProviderClient(FrinmeanContentProvider.MESSAES_CONTENT_URI);
                    ((FrinmeanContentProvider) client.getLocalContentProvider()).insertorupdate(FrinmeanContentProvider.MESSAES_CONTENT_URI, valuesins);
                    client.release();
                }
            } else if (m.getMessageTyp().equalsIgnoreCase(Constants.TYP_CONTACT)) {
                valuesins.put(Constants.T_MESSAGES_ContactMsgID, m.getTextMsgID());
                ContentProviderClient client = getContentResolver().acquireContentProviderClient(FrinmeanContentProvider.MESSAES_CONTENT_URI);
                ((FrinmeanContentProvider) client.getLocalContentProvider()).insertorupdate(FrinmeanContentProvider.MESSAES_CONTENT_URI, valuesins);
                client.release();
//                OutFetchContactMessage ofcm = checkAndDownloadImageMessage(m.getImageMsgID());
//                if (ofcm.getErrortext() == null || ofcm.getErrortext().isEmpty()) {
//                    valuesins.put(Constants.T_MESSAGES_ContactMsgValue, ofcm.getImageMessage());
//                    fcp.insertorupdate(FrinmeanContentProvider.CONTENT_URI, valuesins);
//                }
            } else if (m.getMessageTyp().equalsIgnoreCase(Constants.TYP_FILE)) {
                valuesins.put(Constants.T_MESSAGES_FileMsgID, m.getTextMsgID());
                ContentProviderClient client = getContentResolver().acquireContentProviderClient(FrinmeanContentProvider.MESSAES_CONTENT_URI);
                ((FrinmeanContentProvider) client.getLocalContentProvider()).insertorupdate(FrinmeanContentProvider.MESSAES_CONTENT_URI, valuesins);
                client.release();
//                OutFetchFileMessage offm = checkAndDownloadImageMessage(m.getImageMsgID());
//                if (offm.getErrortext() == null || offm.getErrortext().isEmpty()) {
//                    valuesins.put(Constants.T_MESSAGES_ContactMsgValue, offm.getImageMessage());
//                    fcp.insertorupdate(FrinmeanContentProvider.CONTENT_URI, valuesins);
//                }
            } else if (m.getMessageTyp().equalsIgnoreCase(Constants.TYP_LOCATION)) {
                valuesins.put(Constants.T_MESSAGES_LocationMsgID, m.getTextMsgID());
                ContentProviderClient client = getContentResolver().acquireContentProviderClient(FrinmeanContentProvider.MESSAES_CONTENT_URI);
                ((FrinmeanContentProvider) client.getLocalContentProvider()).insertorupdate(FrinmeanContentProvider.MESSAES_CONTENT_URI, valuesins);
                client.release();
//                OutFetchLocationMessage oflm = checkAndDownloadImageMessage(m.getImageMsgID());
//                if (oflm.getErrortext() == null || oflm.getErrortext().isEmpty()) {
//                    valuesins.put(Constants.T_MESSAGES_ContactMsgValue, oflm.getImageMessage());
//                    fcp.insertorupdate(FrinmeanContentProvider.CONTENT_URI, valuesins);
//                }
            } else if (m.getMessageTyp().equalsIgnoreCase(Constants.TYP_VIDEO)) {
                valuesins.put(Constants.T_MESSAGES_VideoMsgID, m.getTextMsgID());
                ContentProviderClient client = getContentResolver().acquireContentProviderClient(FrinmeanContentProvider.MESSAES_CONTENT_URI);
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
    } */

    /* private OutFetchTextMessage fetchTextMessage(int TxtMsgID) {
        Log.d(TAG, "start fetchTestMessage");
        OutFetchTextMessage out = null;
        if (checkServer()) {
            RestClient rc;
            rc = new RestClient(CommunicationURL + "user/gettextmessage", https, port);
            try {
                Integer mid = TxtMsgID;
                rc.AddParam("username", username);
                rc.AddParam("password", password);
                rc.AddParam("textmessageid", URLEncoder.encode(mid.toString(), "UTF-8"));

                String ret = rc.ExecuteRequestXML(rc.BevorExecuteGetQuery());
                Serializer serializer = new Persister();
                Reader reader = new StringReader(ret);

                out = serializer.read(OutFetchTextMessage.class, reader, false);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        Log.d(TAG, "end fetchTextMessage");
        return out;
    } */

/*    private OutFetchImageMessage checkAndDownloadImageMessage(int ImgMsgID) {
        Log.d(TAG, "start fetchImageMessage");
        OutFetchImageMessage out = new OutFetchImageMessage();
        OutGetImageMessageMetaData outmeta;

        if (checkServer()) {
            // First get MetaData from Server to check if File already exists
            try {
                RestClient rcmeta;
                rcmeta = new RestClient(CommunicationURL + "image/getimagemetadata", https, port);
                Integer imgid = ImgMsgID;
                rcmeta.AddParam("username", username);
                rcmeta.AddParam("password", password);
                rcmeta.AddParam("imageid", URLEncoder.encode(imgid.toString(), "UTF-8"));

                String ret = rcmeta.ExecuteRequestXML(rcmeta.BevorExecuteGetQuery());
                Serializer serializer = new Persister();
                Reader reader = new StringReader(ret);

                outmeta = serializer.read(OutGetImageMessageMetaData.class, reader, false);

                if (outmeta != null) {
                    if (outmeta.getErrortext() == null || outmeta.getErrortext().isEmpty()) {
                        // No error occured no we can check if the file exists and has the right size
                        File checkfile = new File(directory + "/" + "images/" + outmeta.getImageMessage());
                        if (checkfile.exists()) {
                            if (checkfile.length() != outmeta.getImageSize()) {
                                // Download File we have the wrong size
                                out = fetchImageMessage(ImgMsgID);
                            } else {
                                // File is already here an has the right size
                                out.setImageMessage(outmeta.getImageMessage());
                            }
                        } else {
                            // Download File, it is not existing
                            out = fetchImageMessage(ImgMsgID);
                        }
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        Log.d(TAG, "start fetchImageMessage");
        return out;
    } */

    /* private OutFetchImageMessage fetchImageMessage(int ImgMsgID) {
        Log.d(TAG, "start fetchImageMessage");
        OutFetchImageMessage out = new OutFetchImageMessage();

        if (checkServer()) {

            RestClient rc;
            rc = new RestClient(CommunicationURL + "image/download", https, port);

            rc.AddHeader("Accept", "image/jpeg");
            rc.setSaveDirectory(directory + "/" + "images/");

            try {
                String savedFilename = rc.ExecuteRequestImage(rc.BevorExecuteGetPath(username, password, ImgMsgID));

                if (savedFilename != null && !savedFilename.isEmpty()) {
                    out.setImageMessage(savedFilename);
                    File file = new File(directory + "/" + "images/" + savedFilename);
                    MediaStore.Images.Media.insertImage(getContentResolver(), file.getAbsolutePath(), file.getName(), file.getName());

                } else {
                    out.setErrortext("ERROR_DOWNLOAD_IMAGE");
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        Log.d(TAG, "end fetchImageMessage");
        return out;
    } */

    /* public String getServer() {
        return server;
    }

    public void setServer(String server) {
        this.server = server;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    } */

    /* public int getFreq() {
        return freq;
    } */

    /* public void setFreq(int freq) {
        this.freq = freq;
    }

    public class LocalBinder extends Binder {
        public MeBaService getService() {
            // Return this instance of LocalService so clients can call public methods
            return MeBaService.this;
        }
    }*/
}