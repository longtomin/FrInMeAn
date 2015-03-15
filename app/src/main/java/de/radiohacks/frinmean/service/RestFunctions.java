package de.radiohacks.frinmean.service;

import android.content.Context;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.preference.PreferenceManager;
import android.util.Base64;
import android.util.Log;

import org.apache.http.HttpStatus;
import org.simpleframework.xml.Serializer;
import org.simpleframework.xml.core.Persister;

import java.io.File;
import java.io.Reader;
import java.io.StringReader;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

import de.radiohacks.frinmean.Constants;
import de.radiohacks.frinmean.FrinmeanApplication;
import de.radiohacks.frinmean.model.OutAddUserToChat;
import de.radiohacks.frinmean.model.OutAuthenticate;
import de.radiohacks.frinmean.model.OutCheckNewMessages;
import de.radiohacks.frinmean.model.OutCreateChat;
import de.radiohacks.frinmean.model.OutDeleteChat;
import de.radiohacks.frinmean.model.OutDeleteMessageFromChat;
import de.radiohacks.frinmean.model.OutFetchImageMessage;
import de.radiohacks.frinmean.model.OutFetchMessageFromChat;
import de.radiohacks.frinmean.model.OutFetchTextMessage;
import de.radiohacks.frinmean.model.OutFetchVideoMessage;
import de.radiohacks.frinmean.model.OutGetImageMessageMetaData;
import de.radiohacks.frinmean.model.OutGetVideoMessageMetaData;
import de.radiohacks.frinmean.model.OutInsertMessageIntoChat;
import de.radiohacks.frinmean.model.OutListChat;
import de.radiohacks.frinmean.model.OutListUser;
import de.radiohacks.frinmean.model.OutRemoveUserFromChat;
import de.radiohacks.frinmean.model.OutSendImageMessage;
import de.radiohacks.frinmean.model.OutSendTextMessage;
import de.radiohacks.frinmean.model.OutSendVideoMessage;
import de.radiohacks.frinmean.model.OutSignUp;

/**
 * Created by thomas on 19.01.15.
 */
public class RestFunctions {

    private static final String TAG = RestFunctions.class.getSimpleName();
    public ConnectivityManager conManager = null;
    private String server;
    private boolean https;
    private String CommunicationURL;
    private int port;
    private String directory;

    public RestFunctions() {
        conManager = (ConnectivityManager) FrinmeanApplication.getAppContext().getSystemService(Context.CONNECTIVITY_SERVICE);
        getPreferenceInfo();
        buildServerURL();
    }

    protected boolean isNetworkConnected() {
        if (conManager != null) {
            if (conManager.getActiveNetworkInfo() != null) {
                return conManager.getActiveNetworkInfo().isConnected();
            } else {
                return false;
            }
        } else {
            return false;
        }
    }

    protected void getPreferenceInfo() {
        Log.d(TAG, "start getPreferenceInfo");
        SharedPreferences sharedPrefs = PreferenceManager
                .getDefaultSharedPreferences(FrinmeanApplication.getAppContext());

        this.server = sharedPrefs.getString(Constants.PrefServername, "NULL");
        this.https = sharedPrefs.getBoolean(Constants.PrefHTTPSCommunication, true);
        if (this.https) {
            this.port = Integer.parseInt(sharedPrefs.getString(Constants.PrefServerport, "443"));
        } else {
            this.port = Integer.parseInt(sharedPrefs.getString(Constants.PrefServerport, "80"));
        }
//        this.username = sharedPrefs.getString(Constants.PrefUsername, "NULL");
//        this.password = sharedPrefs.getString(Constants.PrefPassword, "NULL");
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
        if (this.CommunicationURL != null && !this.CommunicationURL.equalsIgnoreCase("NULL") && !this.CommunicationURL.isEmpty()) {
            if (isNetworkConnected()) {
                ret = true;
            }
        }
        Log.d(TAG, "end checkServer");
        return ret;
    }

    private String convertB64(String in) throws UnsupportedEncodingException {
        byte[] datauser = in.getBytes("UTF-8");
        return Base64.encodeToString(datauser, Base64.NO_WRAP);
    }

    /* @GET
    @Produces(MediaType.APPLICATION_XML)
    @Path("/authenticate")
    public OutAuthenticate AuthenticateUser(@QueryParam(Constants.QPusername) String User,
                                            @QueryParam(Constants.QPpassword) String Password); */

    public OutAuthenticate authenticate(String inuser, String inpassword) {
        Log.d(TAG, "start authenticate with user=" + inuser + " password=" + inpassword);
        OutAuthenticate out = null;
        if (checkServer()) {
            RestClient rc;
            rc = new RestClient(CommunicationURL + "user/authenticate", https, port);
            try {
                rc.AddParam(Constants.QPusername, convertB64(inuser));
                rc.AddParam(Constants.QPpassword, convertB64(inpassword));

                String ret = rc.ExecuteRequestXML(rc.BevorExecuteGetQuery());
                if (rc.getResponseCode() == HttpStatus.SC_OK) {
                    Serializer serializer = new Persister();
                    Reader reader = new StringReader(ret);

                    out = serializer.read(OutAuthenticate.class, reader, false);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        Log.d(TAG, "end authenticate =" + out.getAuthenticated());
        return out;
    }

    /* @GET
    @Produces(MediaType.APPLICATION_XML)
    @Path("/signup")
    public OutSignUp SingUpUser(@QueryParam(Constants.QPusername) String User,
                                @QueryParam(Constants.QPpassword) String Password,
                                @QueryParam(Constants.QPemail) String Email); */

    public OutSignUp signup(String inuser, String inpassword, String inemail) {
        Log.d(TAG, "start signup with user=" + inuser + " password=" + inpassword + "Email=" + inemail);
        OutSignUp out = null;
        if (checkServer()) {
            RestClient rc;
            rc = new RestClient(CommunicationURL + "user/signup", https, port);
            try {
                rc.AddParam(Constants.QPusername, convertB64(inuser));
                rc.AddParam(Constants.QPpassword, convertB64(inpassword));
                rc.AddParam(Constants.QPemail, convertB64(inemail));

                String ret = rc.ExecuteRequestXML(rc.BevorExecuteGetQuery());
                if (rc.getResponseCode() == HttpStatus.SC_OK) {
                    Serializer serializer = new Persister();
                    Reader reader = new StringReader(ret);

                    out = serializer.read(OutSignUp.class, reader, false);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        Log.d(TAG, "end signup");
        return out;
    }

    /* @GET
    @Produces(MediaType.APPLICATION_XML)
    @Path("/createchat")
    public OutCreateChat CreateChat(@QueryParam(Constants.QPusername) String User,
                                    @QueryParam(Constants.QPpassword) String Password,
                                    @QueryParam(Constants.QPchatname) String Chatname);*/

    public OutCreateChat createchat(String inuser, String inpassword, String inchatname) {
        Log.d(TAG, "start createchat with user=" + inuser + " password=" + inpassword);
        OutCreateChat out = null;
        if (checkServer()) {
            RestClient rc;
            rc = new RestClient(CommunicationURL + "user/createchat", https, port);
            try {
                rc.AddParam(Constants.QPusername, convertB64(inuser));
                rc.AddParam(Constants.QPpassword, convertB64(inpassword));
                rc.AddParam(Constants.QPchatname, convertB64(inchatname));

                String ret = rc.ExecuteRequestXML(rc.BevorExecuteGetQuery());
                if (rc.getResponseCode() == HttpStatus.SC_OK) {
                    Serializer serializer = new Persister();
                    Reader reader = new StringReader(ret);

                    out = serializer.read(OutCreateChat.class, reader, false);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        Log.d(TAG, "end createchat");
        return out;
    }

    /* @DELETE
    @Produces(MediaType.APPLICATION_XML)
    @Path("/deletechat")
    public OutDeleteChat DeleteChat(@QueryParam(Constants.QPusername) String User,
                                    @QueryParam(Constants.QPpassword) String Password,
                                    @QueryParam(Constants.QPchatid) int ChatID); */

    public OutDeleteChat deletechat(String inuser, String inpassword, int inchatid) {
        Log.d(TAG, "start deletechat with user=" + inuser + " password=" + inpassword);
        OutDeleteChat out = null;
        if (checkServer()) {
            RestClient rc;
            rc = new RestClient(CommunicationURL + "user/deletechat", https, port);
            try {
                rc.AddParam(Constants.QPusername, convertB64(inuser));
                rc.AddParam(Constants.QPpassword, convertB64(inpassword));
                rc.AddParam(Constants.QPchatid, Integer.toString(inchatid));

                String ret = rc.ExecuteRequestXML(rc.BevorExecuteGetQuery());
                if (rc.getResponseCode() == HttpStatus.SC_OK) {
                    Serializer serializer = new Persister();
                    Reader reader = new StringReader(ret);

                    out = serializer.read(OutDeleteChat.class, reader, false);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        Log.d(TAG, "end deletechat");
        return out;
    }

    /* @GET
    @Produces(MediaType.APPLICATION_XML)
    @Path("/addusertochat")
    public OutAddUserToChat AddUserToChat(@QueryParam(Constants.QPusername) String User,
                                          @QueryParam(Constants.QPpassword) String Password,
                                          @QueryParam(Constants.QPuserid) int UserID,
                                          @QueryParam(Constants.QPchatid) int ChatID); */

    public OutAddUserToChat addusertochat(String inuser, String inpassword, int inuserid, int inchatid) {
        Log.d(TAG, "start addusertochat with user=" + inuser + " password=" + inpassword);
        OutAddUserToChat out = null;
        if (checkServer()) {
            RestClient rc;
            rc = new RestClient(CommunicationURL + "user/addusertochat", https, port);
            try {
                rc.AddParam(Constants.QPusername, convertB64(inuser));
                rc.AddParam(Constants.QPpassword, convertB64(inpassword));
                rc.AddParam(Constants.QPchatid, Integer.toString(inchatid));
                rc.AddParam(Constants.QPuserid, Integer.toString(inuserid));

                String ret = rc.ExecuteRequestXML(rc.BevorExecuteGetQuery());
                if (rc.getResponseCode() == HttpStatus.SC_OK) {
                    Serializer serializer = new Persister();
                    Reader reader = new StringReader(ret);

                    out = serializer.read(OutAddUserToChat.class, reader, false);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        Log.d(TAG, "end addusertochat");
        return out;
    }

    /* @DELETE
    @Produces(MediaType.APPLICATION_XML)
    @Path("/removeuserfromchat")
    public OutRemoveUserFromChat RemoveUserFromChat(@QueryParam(Constants.QPusername) String User,
                                                    @QueryParam(Constants.QPpassword) String Password,
                                                    @QueryParam(Constants.QPchatid) int ChatID,
                                                    @QueryParam(Constants.QPuserid) int UserID); */

    public OutRemoveUserFromChat removeuserfromchat(String inuser, String inpassword, int inuserid, int inchatid) {
        Log.d(TAG, "start removeuserfromchat with user=" + inuser + " password=" + inpassword);
        OutRemoveUserFromChat out = null;
        if (checkServer()) {
            RestClient rc;
            rc = new RestClient(CommunicationURL + "user/removeuserfromchat", https, port);
            try {
                rc.AddParam(Constants.QPusername, convertB64(inuser));
                rc.AddParam(Constants.QPpassword, convertB64(inpassword));
                rc.AddParam(Constants.QPchatid, Integer.toString(inchatid));
                rc.AddParam(Constants.QPuserid, Integer.toString(inuserid));

                String ret = rc.ExecuteRequestXML(rc.BevorExecuteDeleteQuery());
                if (rc.getResponseCode() == HttpStatus.SC_OK) {
                    Serializer serializer = new Persister();
                    Reader reader = new StringReader(ret);

                    out = serializer.read(OutRemoveUserFromChat.class, reader, false);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        Log.d(TAG, "end removeuserfromchat");
        return out;
    }

    /* @DELETE
    @Produces(MediaType.APPLICATION_XML)
    @Path("/deletemessagefromchat")
    public OutDeleteMessageFromChat deleteMessageFromChat(
            @QueryParam(Constants.QPusername) String User,
            @QueryParam(Constants.QPpassword) String Password,
            @QueryParam(Constants.QPmessageid) int MessageID);*/

    public OutDeleteMessageFromChat deleteMessageFromChat(String inuser, String inpassword, int inmessageid) {
        Log.d(TAG, "start removeuserfromchat with user=" + inuser + " password=" + inpassword);
        OutDeleteMessageFromChat out = null;
        if (checkServer()) {
            RestClient rc;
            rc = new RestClient(CommunicationURL + "user/deletemessagefromchat", https, port);
            try {
                rc.AddParam(Constants.QPusername, convertB64(inuser));
                rc.AddParam(Constants.QPpassword, convertB64(inpassword));
                rc.AddParam(Constants.QPmessageid, Integer.toString(inmessageid));

                String ret = rc.ExecuteRequestXML(rc.BevorExecuteDeleteQuery());
                if (rc.getResponseCode() == HttpStatus.SC_OK) {
                    Serializer serializer = new Persister();
                    Reader reader = new StringReader(ret);

                    out = serializer.read(OutDeleteMessageFromChat.class, reader, false);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        Log.d(TAG, "end removeuserfromchat");
        return out;
    }


    /* @GET
    @Produces(MediaType.APPLICATION_XML)
    @Path("/listuser")
    public OutListUser ListUsers(@QueryParam(Constants.QPusername) String User,
                                 @QueryParam(Constants.QPpassword) String Password,
                                 @QueryParam(Constants.QPsearch) String search); */

    public OutListUser listuser(String inuser, String inpassword, String insearch) {
        Log.d(TAG, "start listuser with user=" + inuser + " password=" + inpassword + " Search=" + insearch);
        OutListUser out = null;
        if (checkServer()) {
            RestClient rc;
            rc = new RestClient(CommunicationURL + "user/listuser", https, port);
            try {
                rc.AddParam(Constants.QPusername, convertB64(inuser));
                rc.AddParam(Constants.QPpassword, convertB64(inpassword));
                rc.AddParam(Constants.QPsearch, convertB64(insearch));

                String ret = rc.ExecuteRequestXML(rc.BevorExecuteGetQuery());
                if (rc.getResponseCode() == HttpStatus.SC_OK) {
                    Serializer serializer = new Persister();
                    Reader reader = new StringReader(ret);

                    out = serializer.read(OutListUser.class, reader, false);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        Log.d(TAG, "end listuser");
        return out;
    }

    /* @GET
    @Produces(MediaType.APPLICATION_XML)
    @Path("/listchat")
    public OutListChat ListChats(@QueryParam(Constants.QPusername) String User,
                                 @QueryParam(Constants.QPpassword) String Password); */
    public OutListChat listchat(String inuser, String inpassword) {
        Log.d(TAG, "start listchat with user=" + inuser + " password=" + inpassword);
        OutListChat out = null;
        int retcode = 0;
        if (checkServer()) {
            RestClient rc;
            rc = new RestClient(CommunicationURL + "user/listchat", https, port);
            try {
                rc.AddParam(Constants.QPusername, convertB64(inuser));
                rc.AddParam(Constants.QPpassword, convertB64(inpassword));

                String ret = rc.ExecuteRequestXML(rc.BevorExecuteGetQuery());
                if (rc.getResponseCode() == HttpStatus.SC_OK) {
                    Serializer serializer = new Persister();
                    Reader reader = new StringReader(ret);

                    out = serializer.read(OutListChat.class, reader, false);
                } else {
                    retcode = rc.getResponseCode();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        if (out != null) {
            Log.d(TAG, "end listchat Errortext" + out.getErrortext() + "Chatsize = " + String.valueOf(out.getChat().size()));
        } else {
            Log.d(TAG, "end listchat Errortext out = null and Returncode =" + String.valueOf(retcode));
        }
        return out;
    }

    /* @GET
    @Produces(MediaType.APPLICATION_XML)
    @Path("/sendtextmessage")
    public OutSendTextMessage sendTextMessage(@QueryParam(Constants.QPusername) String User,
                                              @QueryParam(Constants.QPpassword) String Password,
                                              @QueryParam(Constants.QPtextmessage) String TextMessage); */

    public OutSendTextMessage sendtextmessage(String inuser, String inpassword, String intextmsg) {
        Log.d(TAG, "start sendtextmessage with user=" + inuser + " password=" + inpassword + "Message=" + intextmsg);
        OutSendTextMessage out = null;
        if (checkServer()) {
            RestClient rc;
            rc = new RestClient(CommunicationURL + "user/sendtextmessage", https, port);
            try {
                rc.AddParam(Constants.QPusername, convertB64(inuser));
                rc.AddParam(Constants.QPpassword, convertB64(inpassword));
                rc.AddParam(Constants.QPtextmessage, convertB64(intextmsg));

                String ret = rc.ExecuteRequestXML(rc.BevorExecuteGetQuery());
                if (rc.getResponseCode() == HttpStatus.SC_OK) {
                    Serializer serializer = new Persister();
                    Reader reader = new StringReader(ret);

                    out = serializer.read(OutSendTextMessage.class, reader, false);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        Log.d(TAG, "end sendtextmessage");
        return out;
    }

    /* @GET
    @Produces(MediaType.APPLICATION_XML)
    @Path("/gettextmessage")
    public OutFetchTextMessage getTextMessage(@QueryParam(Constants.QPusername) String User,
                                              @QueryParam(Constants.QPpassword) String Password,
                                              @QueryParam(Constants.QPtextmessageid) int TextMessageID); */

    public OutFetchTextMessage gettextmessage(String inuser, String inpassword, int intextmsgid) {
        Log.d(TAG, "start gettextmessage with user=" + inuser + " password=" + inpassword + "Message=" + String.valueOf(intextmsgid));
        OutFetchTextMessage out = null;
        if (checkServer()) {
            RestClient rc;
            rc = new RestClient(CommunicationURL + "user/gettextmessage", https, port);
            try {
                rc.AddParam(Constants.QPusername, convertB64(inuser));
                rc.AddParam(Constants.QPpassword, convertB64(inpassword));
                rc.AddParam(Constants.QPtextmessageid, Integer.toString(intextmsgid));

                String ret = rc.ExecuteRequestXML(rc.BevorExecuteGetQuery());
                if (rc.getResponseCode() == HttpStatus.SC_OK) {
                    Serializer serializer = new Persister();
                    Reader reader = new StringReader(ret);

                    out = serializer.read(OutFetchTextMessage.class, reader, false);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        Log.d(TAG, "start gettextmessage");
        return out;
    }

    /* @GET
    @Produces(MediaType.APPLICATION_XML)
    @Path("/insertmessageintochat")
    public OutInsertMessageIntoChat insertMessageIntoChat(@QueryParam(Constants.QPusername) String User,
                                                          @QueryParam(Constants.QPpassword) String Password,
                                                          @QueryParam(Constants.QPchatid) int ChatID,
                                                          @QueryParam(Constants.QPmessageid) int MessageID,
                                                          @QueryParam(Constants.QPmessagetype) String MessageType); */

    public OutInsertMessageIntoChat insertmessageintochat(String inuser, String inpassword, int inchatid, int inmsgid, String inmsgtype) {
        Log.d(TAG, "start insertmessageintochat with user=" + inuser + " password=" + inpassword + "ChatID=" + String.valueOf(inchatid) + " MessageID=" + String.valueOf(inmsgid) + "MessageType=" + inmsgtype);
        OutInsertMessageIntoChat out = null;
        if (checkServer()) {
            RestClient rc;
            rc = new RestClient(CommunicationURL + "user/insertmessageintochat", https, port);
            try {
                rc.AddParam(Constants.QPusername, convertB64(inuser));
                rc.AddParam(Constants.QPpassword, convertB64(inpassword));
                rc.AddParam(Constants.QPchatid, Integer.toString(inchatid));
                rc.AddParam(Constants.QPmessageid, Integer.toString(inmsgid));
                rc.AddParam(Constants.QPmessagetype, convertB64(inmsgtype));

                String ret = rc.ExecuteRequestXML(rc.BevorExecuteGetQuery());
                if (rc.getResponseCode() == HttpStatus.SC_OK) {
                    Serializer serializer = new Persister();
                    Reader reader = new StringReader(ret);

                    out = serializer.read(OutInsertMessageIntoChat.class, reader, false);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        Log.d(TAG, "start insertmessageintochat");
        return out;
    }

    /* @DELETE
    @Produces(MediaType.APPLICATION_XML)
    @Path("/deletemessagefromchat")
    public OutDeleteMessageFromChat deleteMessageFromChat(@QueryParam(Constants.QPusername) String User,
                                                          @QueryParam(Constants.QPpassword) String Password,
                                                          @QueryParam(Constants.QPmessageid) int MessageID); */

    public OutDeleteMessageFromChat deletemessagefromchat(String inuser, String inpassword, int inmsgid) {
        Log.d(TAG, "start deletemessagefromchat with user=" + inuser + " password=" + inpassword + "MessageID=" + String.valueOf(inmsgid));
        OutDeleteMessageFromChat out = null;
        if (checkServer()) {
            RestClient rc;
            rc = new RestClient(CommunicationURL + "user/deletemessagefromchat", https, port);
            try {
                rc.AddParam(Constants.QPusername, convertB64(inuser));
                rc.AddParam(Constants.QPpassword, convertB64(inpassword));
                rc.AddParam(Constants.QPchatid, Integer.toString(inmsgid));

                String ret = rc.ExecuteRequestXML(rc.BevorExecuteDeleteQuery());
                if (rc.getResponseCode() == HttpStatus.SC_OK) {
                    Serializer serializer = new Persister();
                    Reader reader = new StringReader(ret);

                    out = serializer.read(OutDeleteMessageFromChat.class, reader, false);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        Log.d(TAG, "start deletemessagefromchat");
        return out;
    }

    /* @GET
    @Produces(MediaType.APPLICATION_XML)
    @Path("/getmessagefromchat")
    public OutFetchMessageFromChat getMessageFromChat(@QueryParam(Constants.QPusername) String User,
                                                      @QueryParam(Constants.QPpassword) String Password,
                                                      @QueryParam(Constants.QPchatid) int ChatID,
                                                      @QueryParam(Constants.QPtimestamp) int Timestamp); */

    public OutFetchMessageFromChat getmessagefromchat(String inuser, String inpassword, int inchatid, long intimestamp) {
        Log.d(TAG, "start getmessagefromchat with user=" + inuser + " password=" + inpassword + "ChatID=" + String.valueOf(inchatid) + " Timestamp=" + String.valueOf(intimestamp));
        OutFetchMessageFromChat out = null;
        if (checkServer()) {
            RestClient rc;
            rc = new RestClient(CommunicationURL + "user/getmessagefromchat", https, port);
            try {
                rc.AddParam(Constants.QPusername, convertB64(inuser));
                rc.AddParam(Constants.QPpassword, convertB64(inpassword));
                rc.AddParam(Constants.QPchatid, Integer.toString(inchatid));
                rc.AddParam(Constants.QPtimestamp, String.valueOf(intimestamp));

                String ret = rc.ExecuteRequestXML(rc.BevorExecuteGetQuery());
                if (rc.getResponseCode() == HttpStatus.SC_OK) {
                    Serializer serializer = new Persister();
                    Reader reader = new StringReader(ret);

                    out = serializer.read(OutFetchMessageFromChat.class, reader, false);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        Log.d(TAG, "start getmessagefromchat");
        return out;
    }

    /* @GET
    @Produces(MediaType.APPLICATION_XML)
    @Path("/checknewmessages")
    public OutCheckNewMessages checkNewMessages(@QueryParam(Constants.QPusername) String User,
                                                @QueryParam(Constants.QPpassword) String Password); */

    public OutCheckNewMessages checknewmessages(String inuser, String inpassword) {
        Log.d(TAG, "start checknewmessages with user=" + inuser + " password=" + inpassword);
        OutCheckNewMessages out = null;
        if (checkServer()) {
            RestClient rc;
            rc = new RestClient(CommunicationURL + "user/checknewmessages", https, port);
            try {
                rc.AddParam(Constants.QPusername, convertB64(inuser));
                rc.AddParam(Constants.QPpassword, convertB64(inpassword));

                String ret = rc.ExecuteRequestXML(rc.BevorExecuteGetQuery());
                if (rc.getResponseCode() == HttpStatus.SC_OK) {
                    Serializer serializer = new Persister();
                    Reader reader = new StringReader(ret);

                    out = serializer.read(OutCheckNewMessages.class, reader, false);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        Log.d(TAG, "start checknewmessages");
        return out;
    }

    /* @POST
    @Path("/upload")
    @Produces(MediaType.APPLICATION_XML)
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    public OutSendImageMessage uploadImage(
            @QueryParam(Constants.QPusername) String User,
            @QueryParam(Constants.QPpassword) String Password,
            @FormDataParam("file") InputStream fileInputStream,
            @FormDataParam("file") FormDataContentDisposition contentDispositionHeader); */

    public OutSendImageMessage sendImageMessage(String inuser, String inpassword, String Message) {
        Log.d(TAG, "start sendImageMessage with user=" + inuser + " password=" + inpassword + "Message=" + Message);
        OutSendImageMessage out = null;
        if (checkServer()) {
            RestClient rc;
            rc = new RestClient(CommunicationURL + "image/upload", https, port);
            try {
                rc.AddHeader("enctype", "multipart/form-data");
                rc.AddParam(Constants.USERNAME, convertB64(inuser));
                rc.AddParam(Constants.PASSWORD, convertB64(inpassword));
                rc.setFilename(Message);

                String ret = rc.ExecuteRequestUploadXML(rc.BevorExecutePost());

                if (rc.getResponseCode() == HttpStatus.SC_OK) {
                    Serializer serializer = new Persister();
                    Reader reader = new StringReader(ret);

                    out = serializer.read(OutSendImageMessage.class, reader, false);
                } else {
                    ErrorHelper eh = new ErrorHelper(FrinmeanApplication.getAppContext());
                    eh.CheckErrorText(Constants.ERROR_NO_CONNECTION_TO_SERVER);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        Log.d(TAG, "end sendImageMessage");
        return out;
    }

    /* @GET
        @Path("/getimagemetadata")
        @Produces(MediaType.APPLICATION_XML)
        public OutGetImageMessageMetaData getimagemetadata(
                @QueryParam(Constants.QPusername) String User,
                @QueryParam(Constants.QPpassword) String Password,
                @QueryParam("imageid") int imageid); */

    public OutGetImageMessageMetaData getImageMessageMetaData(String inuser, String inpassword, int ImgMsgID) {
        Log.d(TAG, "start getImageMessageMetaData with user=" + inuser + " password=" + inpassword + "ImageMessageID=" + String.valueOf(ImgMsgID));
        OutGetImageMessageMetaData out = new OutGetImageMessageMetaData();

        if (checkServer()) {
            try {
                RestClient rc;
                rc = new RestClient(CommunicationURL + "image/getimagemetadata", https, port);
                Integer imgid = ImgMsgID;
                rc.AddParam("username", convertB64(inuser));
                rc.AddParam("password", convertB64(inpassword));
                rc.AddParam("imageid", URLEncoder.encode(imgid.toString(), "UTF-8"));
                String ret = rc.ExecuteRequestXML(rc.BevorExecuteGetQuery());

                if (rc.getResponseCode() == HttpStatus.SC_OK) {
                    Serializer serializer = new Persister();
                    Reader reader = new StringReader(ret);

                    out = serializer.read(OutGetImageMessageMetaData.class, reader, false);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        Log.d(TAG, "end getImageMessageMetaData");
        return out;
    }

    /* @GET
    @Path("/download/{username}/{password}/{imageid}")
    @Produces("image/*")
    public Response downloadImage(@PathParam(Constants.QPusername) String User,
                                  @PathParam(Constants.QPpassword) String Password,
                                  @PathParam(Constants.QPimageid) int imageid); */

    public OutFetchImageMessage fetchImageMessage(String inuser, String inpassword, int ImgMsgID) {
        Log.d(TAG, "start fetchImageMessage with user=" + inuser + " password=" + inpassword + "ImageMessageID=" + String.valueOf(ImgMsgID));
        OutFetchImageMessage out = new OutFetchImageMessage();

        if (checkServer()) {

            RestClient rc;
            rc = new RestClient(CommunicationURL + "image/download", https, port);

            rc.AddHeader("Accept", "image/jpeg");
            rc.setSaveDirectory(directory + "/" + "images/");

            try {
                String savedFilename = rc.ExecuteRequestImage(rc.BevorExecuteGetPath(inuser, inpassword, ImgMsgID));

                if (savedFilename != null && !savedFilename.isEmpty()) {
                    out.setImageMessage(savedFilename);
                    File file = new File(directory + "/" + "images/" + savedFilename);
                    //MediaStore.Images.Media.insertImage(FrinmeanApplication.getAppContext().getContentResolver(), file.getAbsolutePath(), file.getName(), file.getName());

                } else {
                    out.setErrortext("ERROR_DOWNLOAD_IMAGE");
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        Log.d(TAG, "end fetchImageMessage");
        return out;
    }

    /* @POST
    @Path("/upload")
    @Produces(MediaType.APPLICATION_XML)
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    public OutSendVideoMessage uploadVideo(
            @QueryParam(Constants.QPusername) String User,
            @QueryParam(Constants.QPpassword) String Password,
            @FormDataParam("file") InputStream fileInputStream,
            @FormDataParam("file") FormDataContentDisposition contentDispositionHeader); */

    public OutSendVideoMessage sendVideoMessage(String inuser, String inpassword, String Message) {
        Log.d(TAG, "start sendImageMessage with user=" + inuser + " password=" + inpassword + "Message=" + Message);
        OutSendVideoMessage out = null;
        if (checkServer()) {
            RestClient rc;
            rc = new RestClient(CommunicationURL + "video/upload", https, port);
            try {
                rc.AddHeader("enctype", "multipart/form-data");
                rc.AddParam(Constants.USERNAME, convertB64(inuser));
                rc.AddParam(Constants.PASSWORD, convertB64(inpassword));
                rc.setFilename(Message);

                String ret = rc.ExecuteRequestUploadXML(rc.BevorExecutePost());

                if (rc.getResponseCode() == HttpStatus.SC_OK) {
                    Serializer serializer = new Persister();
                    Reader reader = new StringReader(ret);

                    out = serializer.read(OutSendVideoMessage.class, reader, false);
                } else {
                    ErrorHelper eh = new ErrorHelper(FrinmeanApplication.getAppContext());
                    eh.CheckErrorText(Constants.ERROR_NO_CONNECTION_TO_SERVER);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        Log.d(TAG, "end sendVideoMessage");
        return out;
    }

    /* @GET
        @Path("/getvideometadata")
        @Produces(MediaType.APPLICATION_XML)
        public OutGetVideoMessageMetaData getvideometadata(
                @QueryParam(Constants.QPusername) String User,
                @QueryParam(Constants.QPpassword) String Password,
                @QueryParam("videoid") int videoid); */

    public OutGetVideoMessageMetaData getVideoMessageMetaData(String inuser, String inpassword, int VidMsgID) {
        Log.d(TAG, "start getVideoMessageMetaData with user=" + inuser + " password=" + inpassword + " VideoMessageID=" + String.valueOf(VidMsgID));
        OutGetVideoMessageMetaData out = new OutGetVideoMessageMetaData();

        if (checkServer()) {
            try {
                RestClient rc;
                rc = new RestClient(CommunicationURL + "video/getvideometadata", https, port);
                Integer vidid = VidMsgID;
                rc.AddParam("username", convertB64(inuser));
                rc.AddParam("password", convertB64(inpassword));
                rc.AddParam("videoid", URLEncoder.encode(vidid.toString(), "UTF-8"));
                String ret = rc.ExecuteRequestXML(rc.BevorExecuteGetQuery());

                if (rc.getResponseCode() == HttpStatus.SC_OK) {
                    Serializer serializer = new Persister();
                    Reader reader = new StringReader(ret);

                    out = serializer.read(OutGetVideoMessageMetaData.class, reader, false);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        Log.d(TAG, "end getVideoMessageMetaData");
        return out;
    }

    /* @GET
    @Path("/download/{username}/{password}/{videoeid}")
    @Produces("image/*")
    public Response downloadImage(@PathParam(Constants.QPusername) String User,
                                  @PathParam(Constants.QPpassword) String Password,
                                  @PathParam(Constants.QPvideoid) int videoid); */

    public OutFetchVideoMessage fetchVideoMessage(String inuser, String inpassword, int VidMsgID) {
        Log.d(TAG, "start fetchVideoMessage with user=" + inuser + " password=" + inpassword + " VideoMessageID=" + String.valueOf(VidMsgID));
        OutFetchVideoMessage out = new OutFetchVideoMessage();

        if (checkServer()) {

            RestClient rc;
            rc = new RestClient(CommunicationURL + "video/download", https, port);

            rc.AddHeader("Accept", "video/mp4");
            rc.setSaveDirectory(directory + "/" + Constants.VIDEODIR + "/");

            try {
                String savedFilename = rc.ExecuteRequestImage(rc.BevorExecuteGetPath(inuser, inpassword, VidMsgID));

                if (savedFilename != null && !savedFilename.isEmpty()) {
                    out.setVideoMessage(savedFilename);
                    // TODO Video muss im MediaStore noch registriert werden
                    //File file = new File(directory + "/" + Constants.VIDEODIR + "/" + savedFilename);
                    //MediaStore.Video.Media.(FrinmeanApplication.getAppContext().getContentResolver(), file.getAbsolutePath(), file.getName(), file.getName());

                } else {
                    out.setErrortext("ERROR_DOWNLOAD_VIDEO");
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        Log.d(TAG, "end fetchVideoMessage");
        return out;
    }
}
