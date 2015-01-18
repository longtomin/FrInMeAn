package de.radiohacks.frinmean.service;

import android.app.IntentService;
import android.app.LoaderManager;
import android.content.ContentProviderClient;
import android.content.ContentValues;
import android.content.Intent;
import android.content.Loader;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.ConnectivityManager;
import android.os.Binder;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.provider.MediaStore;
import android.util.Log;

import org.apache.commons.io.FileUtils;
import org.apache.http.HttpStatus;
import org.simpleframework.xml.Serializer;
import org.simpleframework.xml.core.Persister;

import java.io.File;
import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.List;

import de.radiohacks.frinmean.Constants;
import de.radiohacks.frinmean.model.Chat;
import de.radiohacks.frinmean.model.Message;
import de.radiohacks.frinmean.model.OutFetchImageMessage;
import de.radiohacks.frinmean.model.OutFetchMessageFromChat;
import de.radiohacks.frinmean.model.OutFetchTextMessage;
import de.radiohacks.frinmean.model.OutGetImageMessageMetaData;
import de.radiohacks.frinmean.model.OutInsertMessageIntoChat;
import de.radiohacks.frinmean.model.OutListChat;
import de.radiohacks.frinmean.model.OutSendImageMessage;
import de.radiohacks.frinmean.model.OutSendTextMessage;
import de.radiohacks.frinmean.providers.FrinmeanContentProvider;

public class MeBaService extends IntentService implements
        LoaderManager.LoaderCallbacks<Cursor> {

    private static final String TAG = MeBaService.class.getSimpleName();
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

    public MeBaService() {
        super("MeBaService");
    }

    @Override
    public void onCreate() {
        super.onCreate();
        Log.d(TAG, "start onCreate");
        conManager = (ConnectivityManager) getSystemService(CONNECTIVITY_SERVICE);
        mBroadcaster = new BroadcastNotifier(MeBaService.this);
        getPreferenceInfo();
        buildServerURL();
        Log.d(TAG, "end onCreate");
    }

    /*@Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }*/

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
            if (Constants.ACTION_SIGNUP.equalsIgnoreCase(action)) {
                final String email = intent.getStringExtra(Constants.EMAIL);
//                handleActionSignup(user, pw, email);
            } else if (Constants.ACTION_AUTHENTICATE.equalsIgnoreCase(action)) {
                handleActionAuthenticate();
            } else if (Constants.ACTION_CHECKNEWMESSAGES.equalsIgnoreCase(
                    action)) {
//                final String param = intent.getStringExtra(EXTRA_PARAM1);
//                handleActionCheckNewMessages(param);
            } else if (Constants.ACTION_GETMESSAGEFROMCHAT.equalsIgnoreCase(action)) {
                final String ChatName = intent.getStringExtra(Constants.CHATNAME);
                final int cid = intent.getIntExtra(Constants.CHATID, -1);
                final long readtime = intent.getLongExtra(Constants.TIMESTAMP, -1);
                if (cid > 0 && readtime > -1) {
                    handleActionGetMessageFromChat(cid, readtime, ChatName);
                }
            } else if (Constants.ACTION_LISTCHAT.equalsIgnoreCase(action)) {
                handleActionListChat();
            } else if (Constants.ACTION_LISTUSER.equalsIgnoreCase(action)) {
                final String search = intent.getStringExtra(Constants.SEARCH);
                handleActionListUser(search);
            } else if (Constants.ACTION_SENDTEXTMESSAGE.equalsIgnoreCase(action)) {
                final String ChatName = intent.getStringExtra(Constants.CHATNAME);
                final int cid = intent.getIntExtra(Constants.CHATID, -1);
                final String TextMessage = intent.getStringExtra(Constants.TEXTMESSAGE);
                handleActionSendTextMessage(ChatName, cid, TextMessage);
            } else if (Constants.ACTION_CREATECHAT.equalsIgnoreCase(action)) {
                final String ChatName = intent.getStringExtra(Constants.CHATNAME);
                handleActionCreateChat(ChatName);
            } else if (Constants.ACTION_SENDIMAGEMESSAGE.equalsIgnoreCase(action)) {
                final String ChatName = intent.getStringExtra(Constants.CHATNAME);
                final int cid = intent.getIntExtra(Constants.CHATID, -1);
                final String ImageLoc = intent.getStringExtra(Constants.IMAGELOCATION);
                handleActionSendImageMessage(ChatName, cid, ImageLoc);
            } else if (Constants.ACTION_ADDUSERTOCHAT.equalsIgnoreCase(action)) {
                final int cid = intent.getIntExtra(Constants.CHATID, -1);
                final int uid = intent.getIntExtra(Constants.USERID, -1);
                handleActionAddUserToChat(cid, uid);
            } else if (Constants.ACTION_SENDVIDEOMESSAGE.equalsIgnoreCase((action))) {
                final String ChatName = intent.getStringExtra(Constants.CHATNAME);
                final int cid = intent.getIntExtra(Constants.CHATID, -1);
                final String VideoLoc = intent.getStringExtra(Constants.VIDEOLOCATION);
                //handleActionSendVideoMessage(ChatName, cid, VideoLoc);
            }

        }
        Log.d(TAG, "start onHandleIntent");
    }

    private void handleActionAddUserToChat(int ChatID, int UserID) {
        Log.d(TAG, "start handleActionAddUserToChat");
        Integer tmpcid = ChatID;
        Integer tmpuid = UserID;
        if (checkServer()) {
            RestClient rc;
            rc = new RestClient(CommunicationURL + "user/addusertochat", https, port);
            try {
                rc.AddParam(Constants.USERNAME, username);
                rc.AddParam(Constants.PASSWORD, password);
                rc.AddParam(Constants.CHATID, tmpcid.toString());
                rc.AddParam(Constants.USERID, tmpuid.toString());

                String ret = rc.ExecuteRequestXML(rc.BevorExecuteGetQuery());
                if (rc.getResponseCode() == HttpStatus.SC_OK) {
                    mBroadcaster.notifyProgress(ret, Constants.BROADCAST_USERADDEDTOCHAT);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        Log.d(TAG, "end handleActionAddUserToChat");
    }


    private void handleActionSendImageMessage(String ChatName, int ChatID, String Message) {
        Log.d(TAG, "start handleActionSendImageMessage");

        // First Insert Message into local Chat
        String imgname = uploadImageMessage(ChatName, ChatID, Message);

        if (imgname != null && !imgname.isEmpty()) {
            // Second move the Image to the right Location
            moveFileToDestination(Message, Constants.IMAGEDIR, imgname);
        }
        Log.d(TAG, "end handleActionSendImageMessage");
    }

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
                                insertMessageIntoChatAndDB(ChatName, ressend.getImageID(), ChatID, Constants.TYP_IMAGE, ressend.getImageFileName());
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

    private void handleActionSendTextMessage(String ChatName, int ChatID, String TextMessage) {
        Log.d(TAG, "start handleActionSendTextMessage");
        if (checkServer()) {
            RestClient rcsend;
            RestClient rcinsert;
            rcsend = new RestClient(CommunicationURL + "user/sendtextmessage", https, port);
            try {
                rcsend.AddParam(Constants.USERNAME, username);
                rcsend.AddParam(Constants.PASSWORD, password);
                rcsend.AddParam(Constants.TEXTMESSAGE, TextMessage);

                String ret = rcsend.ExecuteRequestXML(rcsend.BevorExecuteGetQuery());
                if (rcsend.getResponseCode() == HttpStatus.SC_OK) {
                    Serializer sersendtxtmsg = new Persister();
                    Reader readersendtxtmsg = new StringReader(ret);

                    OutSendTextMessage ressend = sersendtxtmsg.read(OutSendTextMessage.class, readersendtxtmsg, false);

                    if (ressend != null) {
                        if (ressend.getErrortext() == null || ressend.getErrortext().isEmpty()) {
                            if (ressend.getTextID() != null && ressend.getTextID() > 0) {
                                // OutInsertMessageIntoChat ins = insertMessageIntoChatAndDB(ChatName, ressend.getTextID(), ChatID, Constants.TYP_TEXT, TextMessage);
                                insertMessageIntoChatAndDB(ChatName, ressend.getTextID(), ChatID, Constants.TYP_TEXT, TextMessage);
                            }
                        }
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        Log.d(TAG, "end handleActionSendTextMessage");
    }

    private void insertMessageIntoChatAndDB(String ChatName, int MsgID, int ChatID, String MessageType, String Message) {
        Log.d(TAG, "start insertMessageIntoChatAndDB");
        RestClient rcinsert;
//        OutInsertMessageIntoChat ret = null;

        try {
            rcinsert = new RestClient(CommunicationURL + "user/insertmessageintochat", https, port);
            Integer cid = ChatID;
            Integer mid = MsgID;
            rcinsert.AddParam(Constants.USERNAME, username);
            rcinsert.AddParam(Constants.PASSWORD, password);

            rcinsert.AddParam(Constants.CHATID, URLEncoder.encode(cid.toString(), "UTF-8"));
            rcinsert.AddParam(Constants.MESSAGEID, URLEncoder.encode(mid.toString(), "UTF-8"));
            rcinsert.AddParam(Constants.MESSAGETYPE, URLEncoder.encode(MessageType, "UTF-8"));

            String retinsert = rcinsert.ExecuteRequestXML(rcinsert.BevorExecuteGetQuery());
            if (rcinsert.getResponseCode() == HttpStatus.SC_OK) {
                Serializer serinserttxtmsg = new Persister();
                Reader readinserttxtmsg = new StringReader(retinsert);

                OutInsertMessageIntoChat resinsert = serinserttxtmsg.read(OutInsertMessageIntoChat.class, readinserttxtmsg, false);

                if (resinsert != null) {
                    if (resinsert.getErrortext() == null || resinsert.getErrortext().isEmpty()) {
                        ContentValues valuesins = new ContentValues();
                        valuesins.put(Constants.T_MESSAGES_BADBID, resinsert.getMessageID());
                        valuesins.put(Constants.T_MESSAGES_OwningUserID, userid);
                        valuesins.put(Constants.T_MESSAGES_OwningUserName, username);
                        valuesins.put(Constants.T_MESSAGES_ChatID, ChatID);
                        valuesins.put(Constants.T_MESSAGES_MessageTyp, MessageType);
                        valuesins.put(Constants.T_MESSAGES_SendTimestamp, resinsert.getSendTimestamp());
                        valuesins.put(Constants.T_MESSAGES_ReadTimestamp, resinsert.getSendTimestamp());
                        if (MessageType.equalsIgnoreCase(Constants.TYP_TEXT)) {
                            valuesins.put(Constants.T_MESSAGES_TextMsgID, MsgID);
                            valuesins.put(Constants.T_MESSAGES_TextMsgValue, Message);
                        } else if (MessageType.equalsIgnoreCase(Constants.TYP_IMAGE)) {
                            valuesins.put(Constants.T_MESSAGES_ImageMsgID, MsgID);
                            valuesins.put(Constants.T_MESSAGES_ImageMsgValue, Message);
                        } else if (MessageType.equalsIgnoreCase(Constants.TYP_LOCATION)) {
                            valuesins.put(Constants.T_MESSAGES_LocationMsgID, MsgID);
                            valuesins.put(Constants.T_MESSAGES_LocationMsgValue, Message);
                        } else if (MessageType.equalsIgnoreCase(Constants.TYP_CONTACT)) {
                            valuesins.put(Constants.T_MESSAGES_ContactMsgID, MsgID);
                            valuesins.put(Constants.T_MESSAGES_ContactMsgValue, Message);
                        } else if (MessageType.equalsIgnoreCase(Constants.TYP_FILE)) {
                            valuesins.put(Constants.T_MESSAGES_FileMsgID, MsgID);
                            valuesins.put(Constants.T_MESSAGES_FileMsgValue, Message);
                        } else if (MessageType.equalsIgnoreCase(Constants.TYP_VIDEO)) {
                            valuesins.put(Constants.T_MESSAGES_VideoMsgID, MsgID);
                            valuesins.put(Constants.T_MESSAGES_VideoMsgValue, Message);
                        }
                        ContentProviderClient client = getContentResolver().acquireContentProviderClient(FrinmeanContentProvider.MESSAES_CONTENT_URI);
                        ((FrinmeanContentProvider) client.getLocalContentProvider()).insertorupdate(FrinmeanContentProvider.MESSAES_CONTENT_URI, valuesins);
                    }
                }
            }
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
        Log.d(TAG, "end insertMessageIntoChatAndDB");
//        return ret;
    }

    private void handleActionCreateChat(String ChatName) {
        Log.d(TAG, "start handleActionCreateChate");
        if (checkServer()) {
            RestClient rc;
            rc = new RestClient(CommunicationURL + "user/createchat", https, port);
            try {
                rc.AddParam(Constants.USERNAME, username);
                rc.AddParam(Constants.PASSWORD, password);
                rc.AddParam(Constants.CHATNAME, ChatName);

                String ret = rc.ExecuteRequestXML(rc.BevorExecuteGetQuery());
                if (rc.getResponseCode() == HttpStatus.SC_OK) {
                    handleActionListChat();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        Log.d(TAG, "end handleActionCreateChat");
    }

    private void handleActionListChat() {
        Log.d(TAG, "start handleActionListChat");
        if (checkServer()) {
            RestClient rc;
            rc = new RestClient(CommunicationURL + "user/listchat", https, port);
            try {
                rc.AddParam(Constants.USERNAME, username);
                rc.AddParam(Constants.PASSWORD, password);

                String ret = rc.ExecuteRequestXML(rc.BevorExecuteGetQuery());
                if (rc.getResponseCode() == HttpStatus.SC_OK) {
                    Serializer serializer = new Persister();
                    Reader reader = new StringReader(ret);

                    OutListChat res = serializer.read(OutListChat.class, reader, false);
                    if (res != null) {
                        if (res.getErrortext() == null || res.getErrortext().isEmpty()) {
                            if (res.getChat() != null && !res.getChat().isEmpty()) {
                                SaveChatsToLDB(res.getChat());
                            }
                        }
                    }
                    // mBroadcaster.notifyProgress(ret, Constants.BROADCAST_LISTCHAT);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        Log.d(TAG, "end handleActionListChat");
    }

    private void handleActionAuthenticate() {
        Log.d(TAG, "start handleActionAuthenticate");
        if (checkServer()) {
            RestClient rc;
            rc = new RestClient(CommunicationURL + "user/authenticate", https, port);
            try {
                rc.AddParam(Constants.USERNAME, username);
                rc.AddParam(Constants.PASSWORD, password);

                String ret = rc.ExecuteRequestXML(rc.BevorExecuteGetQuery());
                if (rc.getResponseCode() == HttpStatus.SC_OK) {
                    mBroadcaster.notifyProgress(ret, Constants.BROADCAST_AUTHENTICATE);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        Log.d(TAG, "start handleActionAuthenticate");
    }

    private void handleActionListUser(String in) {
        Log.d(TAG, "start handleActionListUser");
        if (checkServer()) {
            RestClient rc;
            rc = new RestClient(CommunicationURL + "user/listuser", https, port);
            try {
                rc.AddParam(Constants.USERNAME, username);
                rc.AddParam(Constants.PASSWORD, password);
                rc.AddParam(Constants.SEARCH, in);

                String ret = rc.ExecuteRequestXML(rc.BevorExecuteGetQuery());
                if (rc.getResponseCode() == HttpStatus.SC_OK) {
                    mBroadcaster.notifyProgress(ret, Constants.BROADCAST_LISTUSER);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        Log.d(TAG, "end handleActionListUser");
    }

    private void handleActionGetMessageFromChat(int cid, long readtime, String CName) {
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
            ContentProviderClient client = getContentResolver().acquireContentProviderClient(FrinmeanContentProvider.CHAT_CONTENT_URI);
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
    }

    private OutFetchTextMessage fetchTextMessage(int TxtMsgID) {
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
    }

    private OutFetchImageMessage checkAndDownloadImageMessage(int ImgMsgID) {
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
    }

    private OutFetchImageMessage fetchImageMessage(int ImgMsgID) {
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
        Log.d(TAG, "start fetchImageMessage");
        return out;
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        return null;
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {

    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {

    }

    /*    public void Refresh() {

            OutCheckNewMessages outcheck = CheckNewMessages();

            if (outcheck.getErrortext() != null && !outcheck.getErrortext().isEmpty()) {
                //Do notthing we are in the Background working
            } else {
                if (outcheck.getChats() != null && outcheck.getChats().size() > 0) {
                    for (int i = 0; i < outcheck.getChats().size(); i++) {
                        Chats c = outcheck.getChats().get(i);
                        OutFetchMessageFromChat of = FetchMessageFromChat(c.getChatID());
                        if (of.getErrortext() != null && !of.getErrortext().isEmpty()) {
                            //Do notthing we are in the Background working
                        } else {
                            if (of.getMessage() != null && !of.getMessage().isEmpty()) {
                                for (int j = 0; j < of.getMessage().size(); j++) {
                                    Message m = of.getMessage().get(j);

                                    long readTime = System.currentTimeMillis() / 1000L;

                                    if (m.getTextMsgID() > 0) {
                                        ldb.insert(m.getOwningUser().getOwningUserID(), m.getOwningUser().getOwningUserName(), c.getChatID(), c.getChatname(), m.getMessageTyp(), m.getSendTimestamp(), readTime, m.getTextMsgID());
                                        OutFetchTextMessage oftm = FetchTextMessage(m.getTextMsgID());
                                        if (oftm.getErrortext() != null && !oftm.getErrortext().isEmpty()) {
                                            //Do notthing we are in the Background working
                                        } else {
                                            ldb.update(m.getMessageTyp(), m.getTextMsgID(), oftm.getTextMessage());
                                        }
                                    }
                                    if (m.getContactMsgID() > 0) {
                                        ldb.insert(m.getOwningUser().getOwningUserID(), m.getOwningUser().getOwningUserName(), c.getChatID(), c.getChatname(), m.getMessageTyp(), m.getSendTimestamp(), readTime, m.getContactMsgID());
                                    }
                                    if (m.getImageMsgID() > 0) {
                                        ldb.insert(m.getOwningUser().getOwningUserID(), m.getOwningUser().getOwningUserName(), c.getChatID(), c.getChatname(), m.getMessageTyp(), m.getSendTimestamp(), readTime, m.getImageMsgID());
                                    }
                                    if (m.getFileMsgID() > 0) {
                                        ldb.insert(m.getOwningUser().getOwningUserID(), m.getOwningUser().getOwningUserName(), c.getChatID(), c.getChatname(), m.getMessageTyp(), m.getSendTimestamp(), readTime, m.getFileMsgID());
                                    }
                                    if (m.getLocationMsgID() > 0) {
                                        ldb.insert(m.getOwningUser().getOwningUserID(), m.getOwningUser().getOwningUserName(), c.getChatID(), c.getChatname(), m.getMessageTyp(), m.getSendTimestamp(), readTime, m.getLocationMsgID());
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    */
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
    }*/

    public class LocalBinder extends Binder {
        public MeBaService getService() {
            // Return this instance of LocalService so clients can call public methods
            return MeBaService.this;
        }
    }
}