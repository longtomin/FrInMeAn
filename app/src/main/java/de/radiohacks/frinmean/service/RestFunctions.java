package de.radiohacks.frinmean.service;

import android.content.Context;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.util.Base64;
import android.util.Log;

import com.google.common.hash.HashCode;
import com.google.common.hash.Hashing;
import com.google.common.io.Files;

import org.simpleframework.xml.Serializer;
import org.simpleframework.xml.core.Persister;

import java.io.File;
import java.io.Reader;
import java.io.StringReader;
import java.io.StringWriter;
import java.io.UnsupportedEncodingException;
import java.net.FileNameMap;
import java.net.HttpURLConnection;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.util.ArrayList;

import javax.ws.rs.core.MediaType;

import de.radiohacks.frinmean.Constants;
import de.radiohacks.frinmean.FrinmeanApplication;
import de.radiohacks.frinmean.modelshort.IAckCD;
import de.radiohacks.frinmean.modelshort.IAckMD;
import de.radiohacks.frinmean.modelshort.IAdUC;
import de.radiohacks.frinmean.modelshort.ICrCh;
import de.radiohacks.frinmean.modelshort.IICIc;
import de.radiohacks.frinmean.modelshort.IIMIC;
import de.radiohacks.frinmean.modelshort.IIUIc;
import de.radiohacks.frinmean.modelshort.ISShT;
import de.radiohacks.frinmean.modelshort.ISTeM;
import de.radiohacks.frinmean.modelshort.ISiUp;
import de.radiohacks.frinmean.modelshort.OAckCD;
import de.radiohacks.frinmean.modelshort.OAckMD;
import de.radiohacks.frinmean.modelshort.OAdUC;
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
import de.radiohacks.frinmean.modelshort.OICIc;
import de.radiohacks.frinmean.modelshort.OIMIC;
import de.radiohacks.frinmean.modelshort.OIUIc;
import de.radiohacks.frinmean.modelshort.OLiCh;
import de.radiohacks.frinmean.modelshort.OLiUs;
import de.radiohacks.frinmean.modelshort.OReUC;
import de.radiohacks.frinmean.modelshort.OSIcM;
import de.radiohacks.frinmean.modelshort.OSImM;
import de.radiohacks.frinmean.modelshort.OSShT;
import de.radiohacks.frinmean.modelshort.OSTeM;
import de.radiohacks.frinmean.modelshort.OSU;
import de.radiohacks.frinmean.modelshort.OSViM;
import de.radiohacks.frinmean.modelshort.OSiUp;

/**
 * Created by thomas on 19.01.15.
 */
public class RestFunctions {

    private static final String TAG = RestFunctions.class.getSimpleName();
    private ConnectivityManager conManager = null;
    private Context mContext = null;
    private String server;
    private boolean https;
    private String CommunicationURL;
    private int port;
    private String contextroot;
    private String imgdir;
    private String viddir;
    private String fildir;
    private String icndir;

    public RestFunctions() {
        conManager = (ConnectivityManager) FrinmeanApplication.getAppContext().getSystemService(Context.CONNECTIVITY_SERVICE);
        mContext = FrinmeanApplication.getAppContext();
        getPreferenceInfo();
        buildServerURL();
        imgdir = Environment.getExternalStorageDirectory().getAbsolutePath() + File.separator + Constants.BASEDIR + File.separator + Constants.IMAGEDIR + File.separator;
        File imgFile = new File(imgdir);
        if (!imgFile.exists()) {
            if (!imgFile.mkdirs()) {
                Log.e(TAG, "Image Directory creation failed");
            }
        }
        viddir = Environment.getExternalStorageDirectory().getAbsolutePath() + File.separator + Constants.BASEDIR + File.separator + Constants.VIDEODIR + File.separator;
        File vidFile = new File(viddir);
        if (!vidFile.exists()) {
            if (!vidFile.mkdirs()) {
                Log.e(TAG, "Video Directory creation failed");
            }
        }
        fildir = Environment.getExternalStorageDirectory().getAbsolutePath() + File.separator + Constants.BASEDIR + File.separator + Constants.FILESDIR + File.separator;
        File filFile = new File(fildir);
        if (!filFile.exists()) {
            if (!filFile.mkdirs()) {
                Log.e(TAG, "File Directory creation failed");
            }
        }
        icndir = Environment.getExternalStorageDirectory().getAbsolutePath() + File.separator + Constants.BASEDIR + File.separator + Constants.ICONDIR + File.separator;
        File icnFile = new File(icndir);
        if (!icnFile.exists()) {
            if (!icnFile.mkdirs()) {
                Log.e(TAG, "Icon Directory creation failed");
            }
        }
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
        this.contextroot = sharedPrefs.getString(Constants.PrefContextRoot, "NULL");
        Log.d(TAG, "end getPferefenceInfo");
    }

    protected void buildServerURL() {
        this.CommunicationURL = "";
        if (this.https) {
            this.CommunicationURL += "https://";
        } else {
            this.CommunicationURL += "http://";
        }
        this.CommunicationURL += server + ":" + port + "/" + contextroot + "/";
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
        byte[] datauser = in.getBytes(Constants.CHARSET);
        return Base64.encodeToString(datauser, Base64.NO_WRAP);
    }

    /* @PUT
    @Produces(MediaType.APPLICATION_XML)
	@Consumes(MediaType.APPLICATION_XML)
	@Path("/signup")
	public OSiUp SingUpUser(ISiUp in); */

    public OSiUp signup(String inuser, String inpassword, String inemail) {
        Log.d(TAG, "start signup with user=" + inuser + " password=" + inpassword + "Email=" + inemail);
        OSiUp out = null;
        if (checkServer()) {
            RestClient rc;
            rc = new RestClient(CommunicationURL + "user/signup");
            try {
                ISiUp in = new ISiUp();
                in.setUN(convertB64(inuser));
                in.setPW(convertB64(inpassword));
                in.setE(convertB64(inemail));

                Serializer serializer = new Persister();
                StringWriter InString = new StringWriter();

                serializer.write(in, InString);
                rc.setPutContent(String.valueOf(InString));
                rc.AddHeader("Content-Type", MediaType.APPLICATION_XML);
                rc.AddHeader("Accept", MediaType.APPLICATION_XML);

                String ret;
                if (https) {
                    ret = rc.ExecuteHTTPSXML("PUT");
                } else {
                    ret = rc.ExecuteHTTPXML("PUT");
                }
                if (rc.getResponseCode() == HttpURLConnection.HTTP_OK) {
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

    /* @PUT
    @Produces(MediaType.APPLICATION_XML)
	@Consumes(MediaType.APPLICATION_XML)
	@Path("/createchat")
	public OCrCh CreateChat(ICrCh in);*/

    public OCrCh createchat(String inchatname) {
        Log.d(TAG, "start createchat with chatname =" + inchatname);
        OCrCh out = null;
        if (checkServer()) {
            RestClient rc;
            rc = new RestClient(CommunicationURL + "user/createchat");
            try {
                ICrCh in = new ICrCh();
                in.setCN(convertB64(inchatname));

                Serializer serializer = new Persister();
                StringWriter InString = new StringWriter();

                serializer.write(in, InString);
                rc.setPutContent(String.valueOf(InString));
                rc.AddHeader("Content-Type", MediaType.APPLICATION_XML);

                String ret;
                if (https) {
                    ret = rc.ExecuteHTTPSXML("PUT");
                } else {
                    ret = rc.ExecuteHTTPXML("PUT");
                }
                if (rc.getResponseCode() == HttpURLConnection.HTTP_OK) {
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

    public ODeCh deletechat(int inchatid) {
        Log.d(TAG, "start deletechat with chatid=" + inchatid);
        ODeCh out = null;
        if (checkServer()) {
            RestClient rc;
            rc = new RestClient(CommunicationURL + "user/deletechat");
            try {
                rc.AddParam(Constants.QPchatid, Integer.toString(inchatid));
                String ret;
                if (https) {
                    ret = rc.ExecuteHTTPSXML("DELETE");
                } else {
                    ret = rc.ExecuteHTTPXML("DELETE");
                }
                if (rc.getResponseCode() == HttpURLConnection.HTTP_OK) {
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

    /* @PUT
    @Produces(MediaType.APPLICATION_XML)
	@Consumes(MediaType.APPLICATION_XML)
	@Path("/addusertochat")
	public OAdUC AddUserToChat(IAdUC in); */

    public OAdUC addusertochat(int inuserid, int inchatid) {
        Log.d(TAG, "start addusertochat with userid=" + inuserid + " chatid=" + inchatid);
        OAdUC out = null;
        if (checkServer()) {
            RestClient rc;
            rc = new RestClient(CommunicationURL + "user/addusertochat");
            try {
                IAdUC in = new IAdUC();
                in.setCID(inchatid);
                in.setUID(inuserid);

                Serializer serializer = new Persister();
                StringWriter InString = new StringWriter();

                serializer.write(in, InString);
                rc.setPutContent(String.valueOf(InString));
                rc.AddHeader("Content-Type", MediaType.APPLICATION_XML);

                String ret;
                if (https) {
                    ret = rc.ExecuteHTTPSXML("PUT");
                } else {
                    ret = rc.ExecuteHTTPXML("PUT");
                }
                if (rc.getResponseCode() == HttpURLConnection.HTTP_OK) {
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
	public OReUC RemoveUserFromChat(
			@QueryParam(Constants.QPusername) String User,
			@QueryParam(Constants.QPpassword) String Password,
			@QueryParam(Constants.QPuserid) int UserID,
			@QueryParam(Constants.QPchatid) int ChatID); */

    public OReUC removeuserfromchat(int inuserid, int inchatid) {
        Log.d(TAG, "start removeuserfromchat with userid=" + inuserid + " chatid=" + inchatid);
        OReUC out = null;
        if (checkServer()) {
            RestClient rc;
            rc = new RestClient(CommunicationURL + "user/removeuserfromchat");
            try {
                rc.AddParam(Constants.QPchatid, Integer.toString(inchatid));
                rc.AddParam(Constants.QPuserid, Integer.toString(inuserid));
                String ret;
                if (https) {
                    ret = rc.ExecuteHTTPSXML("DELETE");
                } else {
                    ret = rc.ExecuteHTTPXML("DELETE");
                }
                if (rc.getResponseCode() == HttpURLConnection.HTTP_OK) {
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

    /* @GET
    @Produces(MediaType.APPLICATION_XML)
	@Path("/listuser")
	public OLiUs ListUsers(@QueryParam(Constants.QPusername) String User,
			@QueryParam(Constants.QPpassword) String Password,
			@QueryParam(Constants.QPsearch) String search); */

    public OLiUs listuser(String insearch) {
        Log.d(TAG, "start listuser with Search=" + insearch);
        OLiUs out = null;
        if (checkServer()) {
            RestClient rc;
            rc = new RestClient(CommunicationURL + "user/listuser");
            try {
                rc.AddParam(Constants.QPsearch, convertB64(insearch));
                String ret;
                if (https) {
                    ret = rc.ExecuteHTTPSXML("GET");
                } else {
                    ret = rc.ExecuteHTTPXML("GET");
                }
                if (rc.getResponseCode() == HttpURLConnection.HTTP_OK) {
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
    public OLiCh listchat() {
        Log.d(TAG, "start listchat");
        OLiCh out = null;
        int retcode = 0;
        if (checkServer()) {
            RestClient rc;
            rc = new RestClient(CommunicationURL + "user/listchat");
            try {
                String ret;
                if (https) {
                    ret = rc.ExecuteHTTPSXML("GET");
                } else {
                    ret = rc.ExecuteHTTPXML("GET");
                }
                if (rc.getResponseCode() == HttpURLConnection.HTTP_OK) {
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
            Log.d(TAG, "end listchat Errortext" + out.getET() + "Chatsize = " + String.valueOf(out.getC().size()));
        } else {
            Log.d(TAG, "end listchat Errortext out = null and Returncode =" + String.valueOf(retcode));
        }
        return out;
    }

    /* @PUT
    @Produces(MediaType.APPLICATION_XML)
	@Consumes(MediaType.APPLICATION_XML)
	@Path("/sendtextmessage")
	public OSTeM sendTextMessage(ISTeM in); */

    public OSTeM sendtextmessage(String intextmsg) {
        Log.d(TAG, "start sendtextmessage with message=" + intextmsg);
        OSTeM out = null;
        if (checkServer()) {
            RestClient rc;
            rc = new RestClient(CommunicationURL + "user/sendtextmessage");
            try {
                ISTeM in = new ISTeM();
                in.setTM(convertB64(intextmsg));

                Serializer serializer = new Persister();
                StringWriter InString = new StringWriter();

                serializer.write(in, InString);
                rc.setPutContent(String.valueOf(InString));
                rc.AddHeader("Content-Type", MediaType.APPLICATION_XML);
                rc.AddHeader("Accept", MediaType.APPLICATION_XML);

                String ret;
                if (https) {
                    ret = rc.ExecuteHTTPSXML("PUT");
                } else {
                    ret = rc.ExecuteHTTPXML("PUT");
                }
                if (rc.getResponseCode() == HttpURLConnection.HTTP_OK) {
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

    public OGTeM gettextmessage(int intextmsgid) {
        Log.d(TAG, "start gettextmessage with messageid=" + String.valueOf(intextmsgid));
        OGTeM out = null;
        if (checkServer()) {
            RestClient rc;
            rc = new RestClient(CommunicationURL + "user/gettextmessage");
            try {
                rc.AddParam(Constants.QPtextmessageid, Integer.toString(intextmsgid));
                String ret;
                if (https) {
                    ret = rc.ExecuteHTTPSXML("GET");
                } else {
                    ret = rc.ExecuteHTTPXML("GET");
                }
                if (rc.getResponseCode() == HttpURLConnection.HTTP_OK) {
                    Serializer serializer = new Persister();
                    Reader reader = new StringReader(ret);

                    out = serializer.read(OGTeM.class, reader, false);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        Log.d(TAG, "end gettextmessage");
        return out;
    }

    /* @PUT
    @Produces(MediaType.APPLICATION_XML)
	@Consumes(MediaType.APPLICATION_XML)
	@Path("/insertmessageintochat")
	public OIMIC insertMessageIntoChat(IIMIC in); */

    public OIMIC insertmessageintochat(int inchatid, int inmsgid, String inmsgtype) {
        Log.d(TAG, "start insertmessageintochat with ChatID=" + String.valueOf(inchatid) + " MessageID=" + String.valueOf(inmsgid) + "MessageType=" + inmsgtype);
        OIMIC out = null;
        if (checkServer()) {
            RestClient rc;
            rc = new RestClient(CommunicationURL + "user/insertmessageintochat");
            try {
                IIMIC in = new IIMIC();
                in.setCID(inchatid);
                in.setMID(inmsgid);
                in.setMT(convertB64(inmsgtype));

                Serializer serializer = new Persister();
                StringWriter InString = new StringWriter();

                serializer.write(in, InString);
                rc.setPutContent(String.valueOf(InString));
                rc.AddHeader("Content-Type", MediaType.APPLICATION_XML);

                String ret;
                if (https) {
                    ret = rc.ExecuteHTTPSXML("PUT");
                } else {
                    ret = rc.ExecuteHTTPXML("PUT");
                }
                if (rc.getResponseCode() == HttpURLConnection.HTTP_OK) {
                    Reader reader = new StringReader(ret);

                    out = serializer.read(OIMIC.class, reader, false);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        Log.d(TAG, "end insertmessageintochat");
        return out;
    }

    /* @DELETE
    @Produces(MediaType.APPLICATION_XML)
	@Path("/deletemessagefromchat")
	public ODMFC deleteMessageFromChat(
			@QueryParam(Constants.QPusername) String User,
			@QueryParam(Constants.QPpassword) String Password,
			@QueryParam(Constants.QPmessageid) int MessageID); */

    public ODMFC deletemessagefromchat(int inmsgid) {
        Log.d(TAG, "start deletemessagefromchat with MessageID=" + String.valueOf(inmsgid));
        ODMFC out = null;
        if (checkServer()) {
            RestClient rc;
            rc = new RestClient(CommunicationURL + "user/deletemessagefromchat");
            try {
                rc.AddParam(Constants.QPchatid, Integer.toString(inmsgid));
                String ret;
                if (https) {
                    ret = rc.ExecuteHTTPSXML("DELETE");
                } else {
                    ret = rc.ExecuteHTTPXML("DELETE");
                }
                if (rc.getResponseCode() == HttpURLConnection.HTTP_OK) {
                    Serializer serializer = new Persister();
                    Reader reader = new StringReader(ret);

                    out = serializer.read(ODMFC.class, reader, false);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        Log.d(TAG, "end deletemessagefromchat");
        return out;
    }

    /* 	@GET
    @Produces(MediaType.APPLICATION_XML)
	@Path("/getmessagefromchat")
	public OFMFC getMessageFromChat(
			@QueryParam(Constants.QPusername) String User,
			@QueryParam(Constants.QPpassword) String Password,
			@QueryParam(Constants.QPchatid) int ChatID,
			@QueryParam(Constants.QPtimestamp) int Timestamp); */

    public OFMFC getmessagefromchat(int inchatid, long intimestamp) {
        Log.d(TAG, "start getmessagefromchat with ChatID=" + String.valueOf(inchatid) + " Timestamp=" + String.valueOf(intimestamp));
        OFMFC out = null;
        if (checkServer()) {
            RestClient rc;
            rc = new RestClient(CommunicationURL + "user/getmessagefromchat");
            try {
                rc.AddParam(Constants.QPchatid, Integer.toString(inchatid));
                rc.AddParam(Constants.QPtimestamp, String.valueOf(intimestamp));
                String ret;
                if (https) {
                    ret = rc.ExecuteHTTPSXML("GET");
                } else {
                    ret = rc.ExecuteHTTPXML("GET");
                }
                if (rc.getResponseCode() == HttpURLConnection.HTTP_OK) {
                    Serializer serializer = new Persister();
                    Reader reader = new StringReader(ret);

                    out = serializer.read(OFMFC.class, reader, false);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        Log.d(TAG, "end getmessagefromchat");
        return out;
    }

    /* @GET
    @Produces(MediaType.APPLICATION_XML)
	@Path("/checknew")
	public OCN checkNew(@QueryParam(Constants.QPusername) String User,
			@QueryParam(Constants.QPpassword) String Password); */

    public OCN checknew() {
        Log.d(TAG, "start checknew");
        OCN out = null;
        if (checkServer()) {
            RestClient rc;
            rc = new RestClient(CommunicationURL + "user/checknew");
            try {
                String ret;
                if (https) {
                    ret = rc.ExecuteHTTPSXML("GET");
                } else {
                    ret = rc.ExecuteHTTPXML("GET");
                }
                if (rc.getResponseCode() == HttpURLConnection.HTTP_OK) {
                    Serializer serializer = new Persister();
                    Reader reader = new StringReader(ret);

                    out = serializer.read(OCN.class, reader, false);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        Log.d(TAG, "end checknew");
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

    public OSImM sendImageMessage(String Message) {
        Log.d(TAG, "start sendImageMessage with Message=" + Message);
        OSImM out = null;
        if (checkServer()) {
            RestClient rc;
            rc = new RestClient(CommunicationURL + "image/upload");
            try {
                HashCode md5 = Files.hash(new File(Message),
                        Hashing.md5());
                String boundary = "*****" + Long.toString(System.currentTimeMillis()) + "*****";
                rc.setBoundary(boundary);

                FileNameMap fileNameMap = URLConnection.getFileNameMap();
                String mime = fileNameMap.getContentTypeFor("file://" + Message);
                String[] q = Message.split("/");
                int idx = q.length - 1;

                rc.AddParam(Constants.QPacknowledge, convertB64(md5.toString()));

                String ret;
                if (https) {
                    ret = rc.ExecuteHTTPSPostXMLMultipart("FileName=" + q[idx], Message, "file", mime);
                } else {
                    ret = rc.ExecuteHTTPPostXMLMultipart("FileName=" + q[idx], Message, "file", mime);
                }

                if (rc.getResponseCode() == HttpURLConnection.HTTP_OK) {
                    Serializer serializer = new Persister();
                    Reader reader = new StringReader(ret);

                    out = serializer.read(OSImM.class, reader, false);
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

    public OGImMMD getImageMessageMetaData(int ImgMsgID) {
        Log.d(TAG, "start getImageMessageMetaData with ImageMessageID=" + String.valueOf(ImgMsgID));
        OGImMMD out = new OGImMMD();

        if (checkServer()) {
            try {
                RestClient rc;
                rc = new RestClient(CommunicationURL + "image/getimagemetadata");
                Integer imgid = ImgMsgID;
                rc.AddParam(Constants.QPimageid, URLEncoder.encode(imgid.toString(), Constants.CHARSET));
                String ret;
                if (https) {
                    ret = rc.ExecuteHTTPSXML("GET");
                } else {
                    ret = rc.ExecuteHTTPXML("GET");
                }
                if (rc.getResponseCode() == HttpURLConnection.HTTP_OK) {
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

    public OGImM fetchImageMessage(int ImgMsgID, String ImageType) {
        Log.d(TAG, "start fetchImageMessage with ImageMessageID=" + String.valueOf(ImgMsgID));
        OGImM out = new OGImM();

        if (checkServer()) {

            try {
                RestClient rc;
                rc = new RestClient(CommunicationURL + "image/download/" + URLEncoder.encode(String.valueOf(ImgMsgID), Constants.CHARSET));

                rc.AddHeader("Accept", "image/jpeg");
                if (ImageType.equalsIgnoreCase(Constants.TYP_IMAGE)) {
                    rc.setSaveDirectory(imgdir);
                } else if (ImageType.equalsIgnoreCase(Constants.TYP_ICON)) {
                    rc.setSaveDirectory(icndir);
                } else {
                    rc.setSaveDirectory(imgdir);
                }
                String savedFilename = null;
                if (https) {
                    savedFilename = rc.ExecuteHTTPSContent("GET");
                } else {
                    savedFilename = rc.ExecuteHTTPContent("GET");
                }

                if (savedFilename != null && !savedFilename.isEmpty()) {
                    out.setIM(savedFilename);
                    //MediaStore.Images.Media.insertImage(FrinmeanApplication.getAppContext().getContentResolver(), file.getAbsolutePath(), file.getName(), file.getName());
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

    public OSViM sendVideoMessage(String Message) {
        Log.d(TAG, "start sendVideoMessage with Message=" + Message);
        OSViM out = null;
        if (checkServer()) {
            RestClient rc;
            rc = new RestClient(CommunicationURL + "video/upload");

            try {
                HashCode md5 = Files.hash(new File(Message),
                        Hashing.md5());
                String boundary = "*****" + Long.toString(System.currentTimeMillis()) + "*****";
                rc.setBoundary(boundary);

                FileNameMap fileNameMap = URLConnection.getFileNameMap();
                String mime = fileNameMap.getContentTypeFor("file://" + Message);
                String[] q = Message.split("/");
                int idx = q.length - 1;

                rc.AddParam(Constants.QPacknowledge, convertB64(md5.toString()));

                String ret = "";
                if (https) {
                    ret = rc.ExecuteHTTPSPostXMLMultipart("FileName=" + q[idx], Message, "file", mime);
                } else {
                    ret = rc.ExecuteHTTPPostXMLMultipart("FileName=" + q[idx], Message, "file", mime);
                }

                if (rc.getResponseCode() == HttpURLConnection.HTTP_OK) {
                    Serializer serializer = new Persister();
                    Reader reader = new StringReader(ret);

                    out = serializer.read(OSViM.class, reader, false);
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

    public OGViMMD getVideoMessageMetaData(int VidMsgID) {
        Log.d(TAG, "start getVideoMessageMetaData with VideoMessageID=" + String.valueOf(VidMsgID));
        OGViMMD out = new OGViMMD();

        if (checkServer()) {
            try {
                RestClient rc;
                rc = new RestClient(CommunicationURL + "video/getvideometadata");
                Integer vidid = VidMsgID;
                rc.AddParam(Constants.QPvideoid, URLEncoder.encode(vidid.toString(), Constants.CHARSET));
                String ret;
                if (https) {
                    ret = rc.ExecuteHTTPSXML("GET");
                } else {
                    ret = rc.ExecuteHTTPXML("GET");
                }
                if (rc.getResponseCode() == HttpURLConnection.HTTP_OK) {
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

    public OGViM fetchVideoMessage(int VidMsgID) {
        Log.d(TAG, "start fetchVideoMessage with VideoMessageID=" + String.valueOf(VidMsgID));
        OGViM out = new OGViM();

        if (checkServer()) {
            try {
                RestClient rc;
                rc = new RestClient(CommunicationURL + "video/download/" + URLEncoder.encode(String.valueOf(VidMsgID), Constants.CHARSET));

                rc.AddHeader("Accept", "video/mp4");
                rc.setSaveDirectory(viddir);

                String savedFilename = null;
                if (https) {
                    savedFilename = rc.ExecuteHTTPSContent("GET");
                } else {
                    savedFilename = rc.ExecuteHTTPContent("GET");
                }

                if (savedFilename != null && !savedFilename.isEmpty()) {
                    out.setVM(savedFilename);
                    // TODO Video muss im MediaStore noch registriert werden
                    //File file = new File(directory + File.separator + Constants.VIDEODIR + File.separator + savedFilename);
                    //MediaStore.Video.Media.(FrinmeanApplication.getAppContext().getContentResolver(), file.getAbsolutePath(), file.getName(), file.getName());

                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        Log.d(TAG, "end fetchVideoMessage");
        return out;
    }

    /* @POST
    @Produces(MediaType.APPLICATION_XML)
	@Consumes(MediaType.APPLICATION_XML)
	@Path("/setshowtimestamp") */

    public OSShT setshowtimestamp(ArrayList<Integer> msgids) {
        Log.d(TAG, "start setshowtimestamp with Number of Messages=" + String.valueOf(msgids.size()));
        OSShT out = null;
        if (checkServer()) {
            RestClient rc;
            rc = new RestClient(CommunicationURL + "user/setshowtimestamp");
            try {
                ISShT in = new ISShT();
                for (int i = 0; i < msgids.size(); i++) {
                    in.getMID().add(msgids.get(i));
                }

                Serializer serializer = new Persister();
                StringWriter InString = new StringWriter();

                serializer.write(in, InString);
                rc.setPutContent(String.valueOf(InString));
                rc.AddHeader("Content-Type", MediaType.APPLICATION_XML);

                String ret;
                if (https) {
                    ret = rc.ExecuteHTTPSXML("POST");
                } else {
                    ret = rc.ExecuteHTTPXML("POST");
                }
                if (rc.getResponseCode() == HttpURLConnection.HTTP_OK) {
                    Reader reader = new StringReader(ret);
                    out = serializer.read(OSShT.class, reader, false);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        Log.d(TAG, "end setshowtimestamp");
        return out;
    }

    /* @GET
    @Produces(MediaType.APPLICATION_XML)
	@Path("/getmessageinformation")
	public OGMI getMessageInformation(
			@QueryParam(Constants.QPusername) String User,
			@QueryParam(Constants.QPpassword) String Password,
			@QueryParam(Constants.QPmessageid) List<Integer> MessageID); */

    public OGMI getmessageinformation(ArrayList<Integer> msgids) {
        Log.d(TAG, "start getmessageinformation with Number of Messages=" + msgids.size());
        OGMI out = null;
        if (checkServer()) {
            RestClient rc;
            rc = new RestClient(CommunicationURL + "user/getmessageinformation");
            try {
                for (int i = 0; i < msgids.size(); i++) {
                    rc.AddParam(Constants.QPmessageid, Integer.toString(msgids.get(i)));
                }
                String ret;
                if (https) {
                    ret = rc.ExecuteHTTPSXML("GET");
                } else {
                    ret = rc.ExecuteHTTPXML("GET");
                }
                if (rc.getResponseCode() == HttpURLConnection.HTTP_OK) {
                    Serializer serializer = new Persister();
                    Reader reader = new StringReader(ret);

                    out = serializer.read(OGMI.class, reader, false);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        Log.d(TAG, "end getmessageinformation");
        return out;
    }

    /* @POST
    @Produces(MediaType.APPLICATION_XML)
	@Consumes(MediaType.APPLICATION_XML)
	@Path("/acknowledgemessagedownload") */

    public OAckMD acknowledgemessagedownload(int msgid, String inacknowledge) {
        Log.d(TAG, "start acknowledgemessagedownload with MessageID=" + String.valueOf(msgid) + " Acknowledge=" + inacknowledge);
        OAckMD out = null;
        if (checkServer()) {
            RestClient rc;
            rc = new RestClient(CommunicationURL + "user/acknowledgemessagedownload");
            try {
                IAckMD in = new IAckMD();
                in.setACK(convertB64(inacknowledge));
                in.setMID(msgid);

                Serializer serializer = new Persister();
                StringWriter InString = new StringWriter();

                serializer.write(in, InString);
                rc.setPutContent(String.valueOf(InString));
                rc.AddHeader("Content-Type", MediaType.APPLICATION_XML);

                String ret;
                if (https) {
                    ret = rc.ExecuteHTTPSXML("POST");
                } else {
                    ret = rc.ExecuteHTTPXML("POST");
                }
                if (rc.getResponseCode() == HttpURLConnection.HTTP_OK) {
                    Reader reader = new StringReader(ret);

                    out = serializer.read(OAckMD.class, reader, false);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        Log.d(TAG, "end acknowledgemessagedownload");
        return out;
    }

    /* @POST
    @Produces(MediaType.APPLICATION_XML)
	@Consumes(MediaType.APPLICATION_XML)
	@Path("/acknowledgechatdownload")
	public OAckCD acknowledgeChatDownload(IAckCD in); */

    public OAckCD acknowledgechatdownload(int chatid, String inacknowledge) {
        Log.d(TAG, "start acknowledgechatdownload with ChatID=" + String.valueOf(chatid) + " Acknowledge=" + inacknowledge);
        OAckCD out = null;
        if (checkServer()) {
            RestClient rc;
            rc = new RestClient(CommunicationURL + "user/acknowledgechatdownload");
            try {
                IAckCD in = new IAckCD();
                in.setACK(convertB64(inacknowledge));
                in.setCID(chatid);

                Serializer serializer = new Persister();
                StringWriter InString = new StringWriter();

                serializer.write(in, InString);
                rc.setPutContent(String.valueOf(InString));
                rc.AddHeader("Content-Type", MediaType.APPLICATION_XML);

                String ret;
                if (https) {
                    ret = rc.ExecuteHTTPSXML("POST");
                } else {
                    ret = rc.ExecuteHTTPXML("POST");
                }
                if (rc.getResponseCode() == HttpURLConnection.HTTP_OK) {
//                    Serializer serializer = new Persister();
                    Reader reader = new StringReader(ret);

                    out = serializer.read(OAckCD.class, reader, false);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        Log.d(TAG, "end acknowledgemessagedownload");
        return out;
    }

    /* @POST
    @Path("/uploadicon")
    @Produces(MediaType.APPLICATION_XML)
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    public OSIcM uploadIcon(
            @QueryParam(Constants.QPusername) String User,
            @QueryParam(Constants.QPpassword) String Password,
            @QueryParam(Constants.QPacknowledge) String Acknowledge,
            @FormDataParam("file") InputStream fileInputStream,
            @FormDataParam("file") FormDataContentDisposition contentDispositionHeader); */

    public OSIcM sendIconMessage(String Message) {
        Log.d(TAG, "start sendIconMessage with Message=" + Message);
        OSIcM out = null;
        if (checkServer()) {
            RestClient rc;
            rc = new RestClient(CommunicationURL + "image/uploadicon");
            try {
                HashCode md5 = Files.hash(new File(Message),
                        Hashing.md5());
                String boundary = "*****" + Long.toString(System.currentTimeMillis()) + "*****";
                rc.setBoundary(boundary);

                FileNameMap fileNameMap = URLConnection.getFileNameMap();
                String mime = fileNameMap.getContentTypeFor("file://" + Message);
                String[] q = Message.split("/");
                int idx = q.length - 1;

                rc.AddParam(Constants.QPacknowledge, convertB64(md5.toString()));

                String ret;
                if (https) {
                    ret = rc.ExecuteHTTPSPostXMLMultipart("FileName=" + q[idx], Message, "file", mime);
                } else {
                    ret = rc.ExecuteHTTPPostXMLMultipart("FileName=" + q[idx], Message, "file", mime);
                }

                if (rc.getResponseCode() == HttpURLConnection.HTTP_OK) {
                    Serializer serializer = new Persister();
                    Reader reader = new StringReader(ret);

                    out = serializer.read(OSIcM.class, reader, false);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        Log.d(TAG, "end sendImageMessage");
        return out;
    }

    /* @PUT
    @Produces(MediaType.APPLICATION_XML)
    @Consumes(MediaType.APPLICATION_XML)
    @Path("/insertusericon")
    public OIUIc insertusericon(IIUIc in); */

    public OIUIc insertusericon(int iniconid) {
        Log.d(TAG, "start insertusericon with IconID=" + String.valueOf(iniconid));
        OIUIc out = null;
        if (checkServer()) {
            RestClient rc;
            rc = new RestClient(CommunicationURL + "user/insertusericon");
            try {
                IIUIc in = new IIUIc();
                in.setIcID(iniconid);

                Serializer serializer = new Persister();
                StringWriter InString = new StringWriter();

                serializer.write(in, InString);
                rc.setPutContent(String.valueOf(InString));
                rc.AddHeader("Content-Type", MediaType.APPLICATION_XML);

                String ret;
                if (https) {
                    ret = rc.ExecuteHTTPSXML("PUT");
                } else {
                    ret = rc.ExecuteHTTPXML("PUT");
                }
                if (rc.getResponseCode() == HttpURLConnection.HTTP_OK) {
                    Reader reader = new StringReader(ret);

                    out = serializer.read(OIUIc.class, reader, false);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        Log.d(TAG, "end insertusericon");
        return out;
    }

    /* @PUT
    @Produces(MediaType.APPLICATION_XML)
    @Consumes(MediaType.APPLICATION_XML)
    @Path("/insertchaticon")
    public OICIc insertchaticon(IICIc in); */

    public OICIc insertchaticon(int iniconid, int inchatid) {
        Log.d(TAG, "start insertchaticon with IconID=" + String.valueOf(iniconid) + " ChatID=" + String.valueOf(inchatid));
        OICIc out = null;
        if (checkServer()) {
            RestClient rc;
            rc = new RestClient(CommunicationURL + "user/insertchaticon");
            try {
                IICIc in = new IICIc();
                in.setIcID(iniconid);
                in.setCID(inchatid);

                Serializer serializer = new Persister();
                StringWriter InString = new StringWriter();

                serializer.write(in, InString);
                rc.setPutContent(String.valueOf(InString));
                rc.AddHeader("Content-Type", MediaType.APPLICATION_XML);

                String ret;
                if (https) {
                    ret = rc.ExecuteHTTPSXML("PUT");
                } else {
                    ret = rc.ExecuteHTTPXML("PUT");
                }
                if (rc.getResponseCode() == HttpURLConnection.HTTP_OK) {
                    Reader reader = new StringReader(ret);

                    out = serializer.read(OICIc.class, reader, false);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        Log.d(TAG, "end insertusericon");
        return out;
    }

    /*@GET
    @Produces(MediaType.APPLICATION_XML)
    @Path("/syncuser")
    public OSU syncuser(@QueryParam(Constants.QPusername) String User,
                        @QueryParam(Constants.QPpassword) String Password,
                        @QueryParam(Constants.QPuserid) List<Integer> UserID); */

    public OSU syncUser(ArrayList<Integer> userids) {
        Log.d(TAG, "start syncUser with Number of Messages=" + userids.size());
        OSU out = null;
        if (checkServer()) {
            RestClient rc;
            rc = new RestClient(CommunicationURL + "user/syncuser");
            try {

                for (int i = 0; i < userids.size(); i++) {
                    rc.AddParam(Constants.QPuserid, Integer.toString(userids.get(i)));
                }
                String ret;
                if (https) {
                    ret = rc.ExecuteHTTPSXML("GET");
                } else {
                    ret = rc.ExecuteHTTPXML("GET");
                }
                if (rc.getResponseCode() == HttpURLConnection.HTTP_OK) {
                    Serializer serializer = new Persister();
                    Reader reader = new StringReader(ret);

                    out = serializer.read(OSU.class, reader, false);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        Log.d(TAG, "end syncUser");
        return out;
    }
}