package de.radiohacks.frinmean.service;

import android.content.Context;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.preference.PreferenceManager;
import android.util.Base64;
import android.util.Log;

import com.google.common.hash.HashCode;
import com.google.common.hash.Hashing;
import com.google.common.io.Files;

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
import de.radiohacks.frinmean.modelshort.OAckCD;
import de.radiohacks.frinmean.modelshort.OAckMD;
import de.radiohacks.frinmean.modelshort.OAdUC;
import de.radiohacks.frinmean.modelshort.OAuth;
import de.radiohacks.frinmean.modelshort.OCN;
import de.radiohacks.frinmean.modelshort.OCrCh;
import de.radiohacks.frinmean.modelshort.ODMFC;
import de.radiohacks.frinmean.modelshort.ODeCh;
import de.radiohacks.frinmean.modelshort.OFMFC;
import de.radiohacks.frinmean.modelshort.OGImM;
import de.radiohacks.frinmean.modelshort.OGImMMD;
import de.radiohacks.frinmean.modelshort.OGMI;
import de.radiohacks.frinmean.modelshort.OGTeM;
import de.radiohacks.frinmean.modelshort.OGViM;
import de.radiohacks.frinmean.modelshort.OGViMMD;
import de.radiohacks.frinmean.modelshort.OIMIC;
import de.radiohacks.frinmean.modelshort.OLiCh;
import de.radiohacks.frinmean.modelshort.OLiUs;
import de.radiohacks.frinmean.modelshort.OReUC;
import de.radiohacks.frinmean.modelshort.OSImM;
import de.radiohacks.frinmean.modelshort.OSShT;
import de.radiohacks.frinmean.modelshort.OSTeM;
import de.radiohacks.frinmean.modelshort.OSViM;
import de.radiohacks.frinmean.modelshort.OSiUp;

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
    public OAuth AuthenticateUser(@QueryParam(Constants.QPusername) String User,
                                            @QueryParam(Constants.QPpassword) String Password); */

    public OAuth authenticate(String inuser, String inpassword) {
        Log.d(TAG, "start authenticate with user=" + inuser + " password=" + inpassword);
        OAuth out = null;
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

                    out = serializer.read(OAuth.class, reader, false);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        Log.d(TAG, "end authenticate");
        return out;
    }

    /* @GET
    @Produces(MediaType.APPLICATION_XML)
    @Path("/signup")
    public OSiUp SingUpUser(@QueryParam(Constants.QPusername) String User,
                                @QueryParam(Constants.QPpassword) String Password,
                                @QueryParam(Constants.QPemail) String Email); */

    public OSiUp signup(String inuser, String inpassword, String inemail) {
        Log.d(TAG, "start signup with user=" + inuser + " password=" + inpassword + "Email=" + inemail);
        OSiUp out = null;
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

                    out = serializer.read(OSiUp.class, reader, false);
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
    public OCrCh CreateChat(@QueryParam(Constants.QPusername) String User,
                                    @QueryParam(Constants.QPpassword) String Password,
                                    @QueryParam(Constants.QPchatname) String Chatname);*/

    public OCrCh createchat(String inuser, String inpassword, String inchatname) {
        Log.d(TAG, "start createchat with user=" + inuser + " password=" + inpassword);
        OCrCh out = null;
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

                    out = serializer.read(OCrCh.class, reader, false);
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
    public ODeCh DeleteChat(@QueryParam(Constants.QPusername) String User,
                                    @QueryParam(Constants.QPpassword) String Password,
                                    @QueryParam(Constants.QPchatid) int ChatID); */

    public ODeCh deletechat(String inuser, String inpassword, int inchatid) {
        Log.d(TAG, "start deletechat with user=" + inuser + " password=" + inpassword);
        ODeCh out = null;
        if (checkServer()) {
            RestClient rc;
            rc = new RestClient(CommunicationURL + "user/deletechat", https, port);
            try {
                rc.AddParam(Constants.QPusername, convertB64(inuser));
                rc.AddParam(Constants.QPpassword, convertB64(inpassword));
                rc.AddParam(Constants.QPchatid, Integer.toString(inchatid));

                String ret = rc.ExecuteRequestXML(rc.BevorExecuteDeleteQuery());
                if (rc.getResponseCode() == HttpStatus.SC_OK) {
                    Serializer serializer = new Persister();
                    Reader reader = new StringReader(ret);

                    out = serializer.read(ODeCh.class, reader, false);
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
    public OAdUC AddUserToChat(@QueryParam(Constants.QPusername) String User,
                                          @QueryParam(Constants.QPpassword) String Password,
                                          @QueryParam(Constants.QPuserid) int UserID,
                                          @QueryParam(Constants.QPchatid) int ChatID); */

    public OAdUC addusertochat(String inuser, String inpassword, int inuserid, int inchatid) {
        Log.d(TAG, "start addusertochat with user=" + inuser + " password=" + inpassword);
        OAdUC out = null;
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

                    out = serializer.read(OAdUC.class, reader, false);
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
    public OReUC RemoveUserFromChat(@QueryParam(Constants.QPusername) String User,
                                                    @QueryParam(Constants.QPpassword) String Password,
                                                    @QueryParam(Constants.QPchatid) int ChatID,
                                                    @QueryParam(Constants.QPuserid) int UserID); */

    public OReUC removeuserfromchat(String inuser, String inpassword, int inuserid, int inchatid) {
        Log.d(TAG, "start removeuserfromchat with user=" + inuser + " password=" + inpassword);
        OReUC out = null;
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

                    out = serializer.read(OReUC.class, reader, false);
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
    public ODMFC deleteMessageFromChat(
            @QueryParam(Constants.QPusername) String User,
            @QueryParam(Constants.QPpassword) String Password,
            @QueryParam(Constants.QPmessageid) int MessageID);*/

    public ODMFC deleteMessageFromChat(String inuser, String inpassword, int inmessageid) {
        Log.d(TAG, "start removeuserfromchat with user=" + inuser + " password=" + inpassword);
        ODMFC out = null;
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

                    out = serializer.read(ODMFC.class, reader, false);
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
    public OLiUs ListUsers(@QueryParam(Constants.QPusername) String User,
                                 @QueryParam(Constants.QPpassword) String Password,
                                 @QueryParam(Constants.QPsearch) String search); */

    public OLiUs listuser(String inuser, String inpassword, String insearch) {
        Log.d(TAG, "start listuser with user=" + inuser + " password=" + inpassword + " Search=" + insearch);
        OLiUs out = null;
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

                    out = serializer.read(OLiUs.class, reader, false);
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
    public OLiCh ListChats(@QueryParam(Constants.QPusername) String User,
                                 @QueryParam(Constants.QPpassword) String Password); */
    public OLiCh listchat(String inuser, String inpassword) {
        Log.d(TAG, "start listchat with user=" + inuser + " password=" + inpassword);
        OLiCh out = null;
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

                    out = serializer.read(OLiCh.class, reader, false);
                } else {
                    retcode = rc.getResponseCode();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        if (out != null) {
            Log.d(TAG, "end listchat Errortext" + out.getET() + "Chatsize = " + String.valueOf(out.getChat().size()));
        } else {
            Log.d(TAG, "end listchat Errortext out = null and Returncode =" + String.valueOf(retcode));
        }
        return out;
    }

    /* @GET
    @Produces(MediaType.APPLICATION_XML)
    @Path("/sendtextmessage")
    public OSTeM sendTextMessage(@QueryParam(Constants.QPusername) String User,
                                              @QueryParam(Constants.QPpassword) String Password,
                                              @QueryParam(Constants.QPtextmessage) String TextMessage); */

    public OSTeM sendtextmessage(String inuser, String inpassword, String intextmsg) {
        Log.d(TAG, "start sendtextmessage with user=" + inuser + " password=" + inpassword + "Message=" + intextmsg);
        OSTeM out = null;
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

                    out = serializer.read(OSTeM.class, reader, false);
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
    public OGTeM getTextMessage(@QueryParam(Constants.QPusername) String User,
                                              @QueryParam(Constants.QPpassword) String Password,
                                              @QueryParam(Constants.QPtextmessageid) int TextMessageID); */

    public OGTeM gettextmessage(String inuser, String inpassword, int intextmsgid) {
        Log.d(TAG, "start gettextmessage with user=" + inuser + " password=" + inpassword + "Message=" + String.valueOf(intextmsgid));
        OGTeM out = null;
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

                    out = serializer.read(OGTeM.class, reader, false);
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
    public OIMIC insertMessageIntoChat(@QueryParam(Constants.QPusername) String User,
                                                          @QueryParam(Constants.QPpassword) String Password,
                                                          @QueryParam(Constants.QPchatid) int ChatID,
                                                          @QueryParam(Constants.QPmessageid) int MessageID,
                                                          @QueryParam(Constants.QPmessagetype) String MessageType); */

    public OIMIC insertmessageintochat(String inuser, String inpassword, int inchatid, int inmsgid, String inmsgtype) {
        Log.d(TAG, "start insertmessageintochat with user=" + inuser + " password=" + inpassword + "ChatID=" + String.valueOf(inchatid) + " MessageID=" + String.valueOf(inmsgid) + "MessageType=" + inmsgtype);
        OIMIC out = null;
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

                    out = serializer.read(OIMIC.class, reader, false);
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
    public ODMFC deleteMessageFromChat(@QueryParam(Constants.QPusername) String User,
                                                          @QueryParam(Constants.QPpassword) String Password,
                                                          @QueryParam(Constants.QPmessageid) int MessageID); */

    public ODMFC deletemessagefromchat(String inuser, String inpassword, int inmsgid) {
        Log.d(TAG, "start deletemessagefromchat with user=" + inuser + " password=" + inpassword + "MessageID=" + String.valueOf(inmsgid));
        ODMFC out = null;
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

                    out = serializer.read(ODMFC.class, reader, false);
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
    public OFMFC getMessageFromChat(@QueryParam(Constants.QPusername) String User,
                                                      @QueryParam(Constants.QPpassword) String Password,
                                                      @QueryParam(Constants.QPchatid) int ChatID,
                                                      @QueryParam(Constants.QPtimestamp) int Timestamp); */

    public OFMFC getmessagefromchat(String inuser, String inpassword, int inchatid, long intimestamp) {
        Log.d(TAG, "start getmessagefromchat with user=" + inuser + " password=" + inpassword + "ChatID=" + String.valueOf(inchatid) + " Timestamp=" + String.valueOf(intimestamp));
        OFMFC out = null;
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

                    out = serializer.read(OFMFC.class, reader, false);
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
    public OCN checkNewMessages(@QueryParam(Constants.QPusername) String User,
                                                @QueryParam(Constants.QPpassword) String Password); */

    public OCN checknewmessages(String inuser, String inpassword) {
        Log.d(TAG, "start checknewmessages with user=" + inuser + " password=" + inpassword);
        OCN out = null;
        if (checkServer()) {
            RestClient rc;
            rc = new RestClient(CommunicationURL + "user/checknew", https, port);
            try {
                rc.AddParam(Constants.QPusername, convertB64(inuser));
                rc.AddParam(Constants.QPpassword, convertB64(inpassword));

                String ret = rc.ExecuteRequestXML(rc.BevorExecuteGetQuery());
                if (rc.getResponseCode() == HttpStatus.SC_OK) {
                    Serializer serializer = new Persister();
                    Reader reader = new StringReader(ret);

                    out = serializer.read(OCN.class, reader, false);
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
    public OSImM uploadImage(
            @QueryParam(Constants.QPusername) String User,
            @QueryParam(Constants.QPpassword) String Password,
            @FormDataParam("file") InputStream fileInputStream,
            @FormDataParam("file") FormDataContentDisposition contentDispositionHeader); */

    public OSImM sendImageMessage(String inuser, String inpassword, String Message) {
        Log.d(TAG, "start sendImageMessage with user=" + inuser + " password=" + inpassword + "Message=" + Message);
        OSImM out = null;
        if (checkServer()) {
            RestClient rc;
            rc = new RestClient(CommunicationURL + "image/upload", https, port);
            try {
                HashCode md5 = Files.hash(new File(Message),
                        Hashing.md5());
                rc.AddHeader("enctype", "multipart/form-data");
                rc.AddParam(Constants.QPusername, convertB64(inuser));
                rc.AddParam(Constants.QPpassword, convertB64(inpassword));
                rc.AddParam(Constants.QPacknowledge, convertB64(md5.toString()));
                rc.setFilename(Message);

                String ret = rc.ExecuteRequestUploadXML(rc.BevorExecutePost());

                if (rc.getResponseCode() == HttpStatus.SC_OK) {
                    Serializer serializer = new Persister();
                    Reader reader = new StringReader(ret);

                    out = serializer.read(OSImM.class, reader, false);
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
        public OGImMMD getimagemetadata(
                @QueryParam(Constants.QPusername) String User,
                @QueryParam(Constants.QPpassword) String Password,
                @QueryParam("imageid") int imageid); */

    public OGImMMD getImageMessageMetaData(String inuser, String inpassword, int ImgMsgID) {
        Log.d(TAG, "start getImageMessageMetaData with user=" + inuser + " password=" + inpassword + "ImageMessageID=" + String.valueOf(ImgMsgID));
        OGImMMD out = new OGImMMD();

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

                    out = serializer.read(OGImMMD.class, reader, false);
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

    public OGImM fetchImageMessage(String inuser, String inpassword, int ImgMsgID) {
        Log.d(TAG, "start fetchImageMessage with user=" + inuser + " password=" + inpassword + "ImageMessageID=" + String.valueOf(ImgMsgID));
        OGImM out = new OGImM();

        if (checkServer()) {

            RestClient rc;
            rc = new RestClient(CommunicationURL + "image/download", https, port);

            rc.AddHeader("Accept", "image/jpeg");
            rc.setSaveDirectory(directory + File.separator + Constants.IMAGEDIR + File.separator);

            try {
                String savedFilename = rc.ExecuteRequestImage(rc.BevorExecuteGetPath(inuser, inpassword, ImgMsgID));

                if (savedFilename != null && !savedFilename.isEmpty()) {
                    out.setIM(savedFilename);
                    //MediaStore.Images.Media.insertImage(FrinmeanApplication.getAppContext().getContentResolver(), file.getAbsolutePath(), file.getName(), file.getName());

                } else {
                    out.setET("ERROR_DOWNLOAD_IMAGE");
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
    public OSViM uploadVideo(
            @QueryParam(Constants.QPusername) String User,
            @QueryParam(Constants.QPpassword) String Password,
            @FormDataParam("file") InputStream fileInputStream,
            @FormDataParam("file") FormDataContentDisposition contentDispositionHeader); */

    public OSViM sendVideoMessage(String inuser, String inpassword, String Message) {
        Log.d(TAG, "start sendImageMessage with user=" + inuser + " password=" + inpassword + "Message=" + Message);
        OSViM out = null;
        if (checkServer()) {
            RestClient rc;
            rc = new RestClient(CommunicationURL + "video/upload", https, port);
            try {
                HashCode md5 = Files.hash(new File(Message),
                        Hashing.md5());
                rc.AddHeader("enctype", "multipart/form-data");
                rc.AddParam(Constants.QPusername, convertB64(inuser));
                rc.AddParam(Constants.QPpassword, convertB64(inpassword));
                rc.AddParam(Constants.QPacknowledge, convertB64(md5.toString()));
                rc.setFilename(Message);

                String ret = rc.ExecuteRequestUploadXML(rc.BevorExecutePost());

                if (rc.getResponseCode() == HttpStatus.SC_OK) {
                    Serializer serializer = new Persister();
                    Reader reader = new StringReader(ret);

                    out = serializer.read(OSViM.class, reader, false);
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
        public OGViMMD getvideometadata(
                @QueryParam(Constants.QPusername) String User,
                @QueryParam(Constants.QPpassword) String Password,
                @QueryParam("videoid") int videoid); */

    public OGViMMD getVideoMessageMetaData(String inuser, String inpassword, int VidMsgID) {
        Log.d(TAG, "start getVideoMessageMetaData with user=" + inuser + " password=" + inpassword + " VideoMessageID=" + String.valueOf(VidMsgID));
        OGViMMD out = new OGViMMD();

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

                    out = serializer.read(OGViMMD.class, reader, false);
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

    public OGViM fetchVideoMessage(String inuser, String inpassword, int VidMsgID) {
        Log.d(TAG, "start fetchVideoMessage with user=" + inuser + " password=" + inpassword + " VideoMessageID=" + String.valueOf(VidMsgID));
        OGViM out = new OGViM();

        if (checkServer()) {

            RestClient rc;
            rc = new RestClient(CommunicationURL + "video/download", https, port);

            rc.AddHeader("Accept", "video/mp4");
            rc.setSaveDirectory(directory + File.separator + Constants.VIDEODIR + File.separator);

            try {
                String savedFilename = rc.ExecuteRequestImage(rc.BevorExecuteGetPath(inuser, inpassword, VidMsgID));

                if (savedFilename != null && !savedFilename.isEmpty()) {
                    out.setVM(savedFilename);
                    // TODO Video muss im MediaStore noch registriert werden
                    //File file = new File(directory + File.separator + Constants.VIDEODIR + File.separator + savedFilename);
                    //MediaStore.Video.Media.(FrinmeanApplication.getAppContext().getContentResolver(), file.getAbsolutePath(), file.getName(), file.getName());

                } else {
                    out.setET("ERROR_DOWNLOAD_VIDEO");
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        Log.d(TAG, "end fetchVideoMessage");
        return out;
    }


    /* @GET
    @Produces(MediaType.APPLICATION_XML)
    @Path("/setshowtimestamp")
    public OSShT setShowTimeStamp(
            @QueryParam(Constants.QPusername) String User,
            @QueryParam(Constants.QPpassword) String Password,
            @QueryParam(Constants.QPmessageid) int MessageID); */

    public OSShT setshowtimestamp(String inuser, String inpassword, int msgid) {
        Log.d(TAG, "start setshowtimestamp with user=" + inuser + " password=" + inpassword + "MessageID=" + String.valueOf(msgid));
        OSShT out = null;
        if (checkServer()) {
            RestClient rc;
            rc = new RestClient(CommunicationURL + "user/setshowtimestamp", https, port);
            try {
                rc.AddParam(Constants.QPusername, convertB64(inuser));
                rc.AddParam(Constants.QPpassword, convertB64(inpassword));
                rc.AddParam(Constants.QPmessageid, Integer.toString(msgid));

                String ret = rc.ExecuteRequestXML(rc.BevorExecuteGetQuery());
                if (rc.getResponseCode() == HttpStatus.SC_OK) {
                    Serializer serializer = new Persister();
                    Reader reader = new StringReader(ret);

                    out = serializer.read(OSShT.class, reader, false);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        Log.d(TAG, "start setshowtimestamp");
        return out;
    }

    /* @GET
    @Produces(MediaType.APPLICATION_XML)
    @Path("/getmessageinformation")
    public OGMI getMessageInformation(
            @QueryParam(Constants.QPusername) String User,
            @QueryParam(Constants.QPpassword) String Password,
            @QueryParam(Constants.QPmessageid) int MessageID); */

    public OGMI getmessageinformation(String inuser, String inpassword, int msgid) {
        Log.d(TAG, "start getmessageinformation with user=" + inuser + " password=" + inpassword + "MessageID=" + String.valueOf(msgid));
        OGMI out = null;
        if (checkServer()) {
            RestClient rc;
            rc = new RestClient(CommunicationURL + "user/getmessageinformation", https, port);
            try {
                rc.AddParam(Constants.QPusername, convertB64(inuser));
                rc.AddParam(Constants.QPpassword, convertB64(inpassword));
                rc.AddParam(Constants.QPmessageid, Integer.toString(msgid));

                String ret = rc.ExecuteRequestXML(rc.BevorExecuteGetQuery());
                if (rc.getResponseCode() == HttpStatus.SC_OK) {
                    Serializer serializer = new Persister();
                    Reader reader = new StringReader(ret);

                    out = serializer.read(OGMI.class, reader, false);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        Log.d(TAG, "start getmessageinformation");
        return out;
    }

    /* @GET
    @Produces(MediaType.APPLICATION_XML)
    @Path("/acknowledgemessagedownload")
    public OAckMD acknowledgeMessageDownload(
            @QueryParam(Constants.QPusername) String User,
            @QueryParam(Constants.QPpassword) String Password,
            @QueryParam(Constants.QPmessageid) int MessageID,
            @QueryParam(Constants.QPacknowledge) String Acknowledge); */

    public OAckMD acknowledgemessagedownload(String inuser, String inpassword, int msgid, String inacknowledge) {
        Log.d(TAG, "start acknowledgemessagedownload with user=" + inuser + " password=" + inpassword + " MessageID=" + String.valueOf(msgid) + " Acknowledge=" + inacknowledge);
        OAckMD out = null;
        if (checkServer()) {
            RestClient rc;
            rc = new RestClient(CommunicationURL + "user/acknowledgemessagedownload", https, port);
            try {
                rc.AddParam(Constants.QPusername, convertB64(inuser));
                rc.AddParam(Constants.QPpassword, convertB64(inpassword));
                rc.AddParam(Constants.QPmessageid, Integer.toString(msgid));
                rc.AddParam(Constants.QPacknowledge, convertB64(inacknowledge));

                String ret = rc.ExecuteRequestXML(rc.BevorExecuteGetQuery());
                if (rc.getResponseCode() == HttpStatus.SC_OK) {
                    Serializer serializer = new Persister();
                    Reader reader = new StringReader(ret);

                    out = serializer.read(OAckMD.class, reader, false);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        Log.d(TAG, "start acknowledgemessagedownload");
        return out;
    }

    /* @GET
    @Produces(MediaType.APPLICATION_XML)
    @Path("/acknowledgechatdownload")
    public OAckCD acknowledgeMessageDownload(
            @QueryParam(Constants.QPusername) String User,
            @QueryParam(Constants.QPpassword) String Password,
            @QueryParam(Constants.QPchatid) int ChatID,
            @QueryParam(Constants.QPacknowledge) String Acknowledge); */

    public OAckCD acknowledgechatdownload(String inuser, String inpassword, int chatid, String inacknowledge) {
        Log.d(TAG, "start acknowledgechatdownload with user=" + inuser + " password=" + inpassword + " ChatID=" + String.valueOf(chatid) + " Acknowledge=" + inacknowledge);
        OAckCD out = null;
        if (checkServer()) {
            RestClient rc;
            rc = new RestClient(CommunicationURL + "user/acknowledgechatdownload", https, port);
            try {
                rc.AddParam(Constants.QPusername, convertB64(inuser));
                rc.AddParam(Constants.QPpassword, convertB64(inpassword));
                rc.AddParam(Constants.QPchatid, Integer.toString(chatid));
                rc.AddParam(Constants.QPacknowledge, convertB64(inacknowledge));

                String ret = rc.ExecuteRequestXML(rc.BevorExecuteGetQuery());
                if (rc.getResponseCode() == HttpStatus.SC_OK) {
                    Serializer serializer = new Persister();
                    Reader reader = new StringReader(ret);

                    out = serializer.read(OAckCD.class, reader, false);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        Log.d(TAG, "start acknowledgemessagedownload");
        return out;
    }
}
