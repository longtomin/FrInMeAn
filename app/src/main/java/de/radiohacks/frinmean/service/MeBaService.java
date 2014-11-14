package de.radiohacks.frinmean.service;

import android.app.IntentService;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.os.Binder;
import android.os.IBinder;
import android.preference.PreferenceManager;

import org.apache.http.HttpStatus;
import org.simpleframework.xml.Serializer;
import org.simpleframework.xml.core.Persister;

import java.io.Reader;
import java.io.StringReader;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.List;

import de.radiohacks.frinmean.Constants;
import de.radiohacks.frinmean.model.Message;
import de.radiohacks.frinmean.model.OutFetchImageMessage;
import de.radiohacks.frinmean.model.OutFetchMessageFromChat;
import de.radiohacks.frinmean.model.OutFetchTextMessage;
import de.radiohacks.frinmean.model.OutInsertMessageIntoChat;
import de.radiohacks.frinmean.model.OutSendTextMessage;

public class MeBaService extends IntentService {

    private final IBinder mBinder = new LocalBinder();
    public ConnectivityManager conManager = null;
    private String server;
    private String username;
    private String password;
    private int userid;
    // private int freq;
    private LocalDBHandler ldb = null;
    private BroadcastNotifier mBroadcaster = new BroadcastNotifier(this);

    public MeBaService() {
        super("MeBaService");
        ldb = new LocalDBHandler(this);
    }

    @Override
    public void onCreate() {
        super.onCreate();
        conManager = (ConnectivityManager) getSystemService(CONNECTIVITY_SERVICE);
        getPreferenceInfo();

    }

    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }

    public boolean isNetworkConnected() {
        return conManager.getActiveNetworkInfo().isConnected();
    }

    protected void getPreferenceInfo() {
        SharedPreferences sharedPrefs = PreferenceManager
                .getDefaultSharedPreferences(this);

        server = sharedPrefs.getString("prefServername", "NULL");
        username = sharedPrefs.getString("prefUsername", "NULL");
        password = sharedPrefs.getString("prefPassword", "NULL");
        userid = sharedPrefs.getInt("prefUserID", -1);
        // freq = sharedPrefs.getInt("prefSyncfrequency", -1);
    }

    protected boolean CheckServer() {
        boolean ret = false;
        if (this.server != null && !this.server.equalsIgnoreCase("NULL") && !this.server.isEmpty()) {
            if (this.username != null && !this.username.equalsIgnoreCase("NULL") && !this.username.isEmpty()) {
                if (this.password != null && !this.password.equalsIgnoreCase("NULL") && !this.password.isEmpty()) {
                    ret = true;
                }
            }
        }
        return ret;
    }

    // Helper Funktion um den Service im Hintergrund zu starten.
    /* public void startActionSignup(Context context, String user, String pw, String email) {
        Intent intent = new Intent(context, MeBaService.class);
        intent.setAction(Constants.ACTION_SIGNUP);
        intent.putExtra(Constants.USERNAME, user);
        intent.putExtra(Constants.PASSWORD, pw);
        intent.putExtra(Constants.EMAIL, email);
        context.startService(intent);
    }*/

    // Helper Funktion um den Service im Hintergrund zu starten.
    /* public void startActionAuthenticate(Context context, String user, String pw) {
        Intent intent = new Intent(context, MeBaService.class);
        intent.setAction(Constants.ACTION_AUTHENTICATE);
        intent.putExtra(Constants.USERNAME, user);
        intent.putExtra(Constants.PASSWORD, pw);
        context.startService(intent);
    } */

    @Override
    protected void onHandleIntent(Intent intent) {
        if (intent != null) {
            final String action = intent.getAction();
            if (Constants.ACTION_SIGNUP.equalsIgnoreCase(action)) {
                final String user = intent.getStringExtra(Constants.USERNAME);
                final String pw = intent.getStringExtra(Constants.PASSWORD);
                final String email = intent.getStringExtra(Constants.EMAIL);
//                handleActionSignup(user, pw, email);
            }
            if (Constants.ACTION_AUTHENTICATE.equalsIgnoreCase(action)) {
                handleActionAuthenticate();
            }
            if (Constants.ACTION_CHECKNEWMESSAGES.equalsIgnoreCase(
                    action)) {
//                final String param = intent.getStringExtra(EXTRA_PARAM1);
//                handleActionCheckNewMessages(param);
            }
            if (Constants.ACTION_GETMESSAGEFROMCHAT.equalsIgnoreCase(action)) {
                final String ChatName = intent.getStringExtra(Constants.CHATNAME);
                final int cid = intent.getIntExtra(Constants.CHATID, -1);
                final long readtime = intent.getLongExtra(Constants.TIMESTAMP, -1);
                if (cid > 0 && readtime > 0) {
                    handleActionGetMessageFromChat(cid, readtime, ChatName);
                }
            }
            if (Constants.ACTION_LISTCHAT.equalsIgnoreCase(action)) {
                handleActionListChat();
            }
            if (Constants.ACTION_LISTUSER.equalsIgnoreCase(action)) {
                handleActionListUser();
            }
            if (Constants.ACTION_SENDTEXTMESSAGE.equalsIgnoreCase(action)) {
                final String ChatName = intent.getStringExtra(Constants.CHATNAME);
                final int cid = intent.getIntExtra(Constants.CHATID, -1);
                final String TextMessage = intent.getStringExtra(Constants.TEXTMESSAGE);
                handleActionSendTextMessage(ChatName, cid, TextMessage);
            }
            if (Constants.ACTION_CREATECHAT.equalsIgnoreCase(action)) {
                final String ChatName = intent.getStringExtra(Constants.CHATNAME);
                handleActionCreateChat(ChatName);
            }
            if (Constants.ACTION_SENDIMAGEMESSAGE.equalsIgnoreCase(action)) {
                final String ImageLoc = intent.getStringExtra(Constants.IMAGELOCATION);
                //   handleActionSendImage(ImageLoc);
            }
        }
    }

    private void handleActionSendTextMessage(String ChatName, int ChatID, String TextMessage) {
        if (CheckServer()) {
            RestClient rcsend;
            RestClient rcinsert;
            if (!server.endsWith("/")) {
                rcsend = new RestClient(server + "/user/sendtextmessage");
            } else {
                rcsend = new RestClient(server + "user/sendtextmessage");
            }
            try {
                rcsend.AddParam(Constants.USERNAME, username);
                rcsend.AddParam(Constants.PASSWORD, password);
                rcsend.AddParam(Constants.TEXTMESSAGE, TextMessage);

                String ret = rcsend.ExecuteRequestXML(rcsend.BevorExecuteGetQuery());
                if (rcsend.getResponseCode() == HttpStatus.SC_OK) {
                    Serializer sersendtxtmsg = new Persister();
                    Reader readersendtxtmsg = new StringReader(ret);

                    OutSendTextMessage ressend = sersendtxtmsg.read(OutSendTextMessage.class, readersendtxtmsg, false);

                    if (ressend == null) {
                        ErrorHelper eh = new ErrorHelper(this);
                        eh.CheckErrorText(Constants.NO_CONNECTION_TO_SERVER);
                    } else {
                        if (ressend.getErrortext() != null && !ressend.getErrortext().isEmpty()) {
                            ErrorHelper eh = new ErrorHelper(this);
                            eh.CheckErrorText(ressend.getErrortext());
                        } else {
                            if (ressend.getTextID() != null && ressend.getTextID() > 0) {
                                OutInsertMessageIntoChat ins = insertMessageIntoChatAndDB(ChatName, ressend.getTextID(), ChatID, Constants.TYP_TEXT, TextMessage);
                            }
                        }
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private OutInsertMessageIntoChat insertMessageIntoChatAndDB(String ChatName, int MsgID, int ChatID, String MessageType, String Message) {

        RestClient rcinsert;
        OutInsertMessageIntoChat ret = null;

        try {
            if (!server.endsWith("/")) {
                rcinsert = new RestClient(server + "/user/insertmessageintochat");
            } else {
                rcinsert = new RestClient(server + "user/insertmessageintochat");
            }
            Integer cid = ChatID;
            Integer mid = MsgID;
            rcinsert.AddParam(Constants.USERNAME, username);
            rcinsert.AddParam(Constants.PASSWORD, password);

            rcinsert.AddParam(Constants.CHATNAME, URLEncoder.encode(cid.toString(), "UTF-8"));
            rcinsert.AddParam(Constants.MESSAGEID, URLEncoder.encode(mid.toString(), "UTF-8"));
            rcinsert.AddParam(Constants.MESSAGETYPE, URLEncoder.encode(MessageType, "UTF-8"));

            String retinsert = rcinsert.ExecuteRequestXML(rcinsert.BevorExecuteGetQuery());
            if (rcinsert.getResponseCode() == HttpStatus.SC_OK) {
                Serializer serinserttxtmsg = new Persister();
                Reader readinserttxtmsg = new StringReader(retinsert);

                OutInsertMessageIntoChat resinsert = serinserttxtmsg.read(OutInsertMessageIntoChat.class, readinserttxtmsg, false);

                if (resinsert == null) {
                    ErrorHelper eh = new ErrorHelper(this);
                    eh.CheckErrorText(Constants.NO_CONNECTION_TO_SERVER);
                } else {
                    if (resinsert.getErrortext() != null && !resinsert.getErrortext().isEmpty()) {
                        ErrorHelper eh = new ErrorHelper(this);
                        eh.CheckErrorText(resinsert.getErrortext());
                    } else {
                        ldb.insert(userid, username, ChatID, ChatName, Constants.TYP_TEXT, resinsert.getSendTimestamp(), resinsert.getSendTimestamp(), resinsert.getMessageID());
                        ldb.update(Constants.TYP_TEXT, resinsert.getMessageID(), Message);
                        ret = resinsert;
                    }
                }
            }
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return ret;
    }

    private void handleActionCreateChat(String ChatName) {
        if (CheckServer()) {
            RestClient rc;
            if (!server.endsWith("/")) {
                rc = new RestClient(server + "/user/createchat");
            } else {
                rc = new RestClient(server + "user/createchat");
            }
            try {
                rc.AddParam(Constants.USERNAME, username);
                rc.AddParam(Constants.PASSWORD, password);
                rc.AddParam(Constants.CHATNAME, ChatName);

                String ret = rc.ExecuteRequestXML(rc.BevorExecuteGetQuery());
                if (rc.getResponseCode() == HttpStatus.SC_OK) {
                    mBroadcaster.notifyProgress(ret, Constants.BROADCAST_CREATECHAT);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private void handleActionListChat() {
        if (CheckServer()) {
            RestClient rc;
            if (!server.endsWith("/")) {
                rc = new RestClient(server + "/user/listchat");
            } else {
                rc = new RestClient(server + "user/listchat");
            }
            try {
                rc.AddParam(Constants.USERNAME, username);
                rc.AddParam(Constants.PASSWORD, password);

                String ret = rc.ExecuteRequestXML(rc.BevorExecuteGetQuery());
                if (rc.getResponseCode() == HttpStatus.SC_OK) {
                    mBroadcaster.notifyProgress(ret, Constants.BROADCAST_LISTCHAT);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private void handleActionAuthenticate() {
        if (CheckServer()) {
            RestClient rc;
            if (!server.endsWith("/")) {
                rc = new RestClient(server + "/user/authenticate");
            } else {
                rc = new RestClient(server + "user/authenticate");
            }
            try {
                //rc.AddParam("username", user);
                //rc.AddParam("password", password);
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
    }

    private void handleActionListUser() {
        if (CheckServer()) {
            RestClient rc;
            if (!server.endsWith("/")) {
                rc = new RestClient(server + "/user/listuser");
            } else {
                rc = new RestClient(server + "user/listuser");
            }
            try {
                rc.AddParam(Constants.USERNAME, username);
                rc.AddParam(Constants.PASSWORD, password);

                String ret = rc.ExecuteRequestXML(rc.BevorExecuteGetQuery());
                if (rc.getResponseCode() == HttpStatus.SC_OK) {
                    mBroadcaster.notifyProgress(ret, Constants.BROADCAST_LISTUSER);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private void handleActionGetMessageFromChat(int cid, long readtime, String CName) {
        if (CheckServer()) {
            RestClient rc;
            if (!server.endsWith("/")) {
                rc = new RestClient(server + "/user/getmessagefromchat");
            } else {
                rc = new RestClient(server + "user/getmessagefromchat");
            }
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

                    if (res == null) {
                        ErrorHelper eh = new ErrorHelper(this);
                        eh.CheckErrorText(Constants.NO_CONNECTION_TO_SERVER);
                    } else {
                        if (res.getErrortext() != null && !res.getErrortext().isEmpty()) {
                            ErrorHelper eh = new ErrorHelper(this);
                            eh.CheckErrorText(res.getErrortext());
                        } else {
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
    }

    private void SaveMessageToLDB(List<Message> in, int ChatID, String ChatName) {
        for (int j = 0; j < in.size(); j++) {
            Message m = in.get(j);

            if (m.getTextMsgID() > 0) {
                ldb.insert(m.getOwningUser().getOwningUserID(), m.getOwningUser().getOwningUserName(), ChatID, ChatName, m.getMessageTyp(), m.getSendTimestamp(), m.getReadTimestamp(), m.getTextMsgID());
                OutFetchTextMessage oftm = FetchTextMessage(m.getTextMsgID());
                if (oftm.getErrortext() != null && !oftm.getErrortext().isEmpty()) {
                    //Do notthing we are in the Background working
                } else {
                    ldb.update(m.getMessageTyp(), m.getTextMsgID(), oftm.getTextMessage());
                }
            }
            if (m.getContactMsgID() > 0) {
                ldb.insert(m.getOwningUser().getOwningUserID(), m.getOwningUser().getOwningUserName(), ChatID, ChatName, m.getMessageTyp(), m.getSendTimestamp(), m.getReadTimestamp(), m.getContactMsgID());
            }
            if (m.getImageMsgID() > 0) {
                ldb.insert(m.getOwningUser().getOwningUserID(), m.getOwningUser().getOwningUserName(), ChatID, ChatName, m.getMessageTyp(), m.getSendTimestamp(), m.getReadTimestamp(), m.getImageMsgID());

                OutFetchImageMessage ofim = FetchImageMessage(m.getImageMsgID());
                if (ofim.getErrortext() != null && !ofim.getErrortext().isEmpty()) {
                    //Do notthing we are in the Background working
                } else {
                    ldb.update(m.getMessageTyp(), m.getTextMsgID(), ofim.getImageMessage());
                }


            }
            if (m.getFileMsgID() > 0) {
                ldb.insert(m.getOwningUser().getOwningUserID(), m.getOwningUser().getOwningUserName(), ChatID, ChatName, m.getMessageTyp(), m.getSendTimestamp(), m.getReadTimestamp(), m.getFileMsgID());
            }
            if (m.getLocationMsgID() > 0) {
                ldb.insert(m.getOwningUser().getOwningUserID(), m.getOwningUser().getOwningUserName(), ChatID, ChatName, m.getMessageTyp(), m.getSendTimestamp(), m.getReadTimestamp(), m.getLocationMsgID());
            }
        }
    }

    private OutFetchTextMessage FetchTextMessage(int TxtMsgID) {
        OutFetchTextMessage out = null;
        if (CheckServer()) {
            RestClient rc;
            if (!server.endsWith("/")) {
                rc = new RestClient(server + "/user/gettextmessage");
            } else {
                rc = new RestClient(server + "user/gettextmessage");
            }
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
        return out;
    }

    private OutFetchImageMessage FetchImageMessage(int ImgMsgID) {
        OutFetchImageMessage out = null;
        if (CheckServer()) {
            RestClient rc;
            if (!server.endsWith("/")) {
                rc = new RestClient(server + "/image/download");
            } else {
                rc = new RestClient(server + "image/download");
            }
            try {
                String ret = rc.ExecuteRequestXML(rc.BevorExecuteGetPath(username, password, ImgMsgID));
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return out;
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
    public String getServer() {
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
    }

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