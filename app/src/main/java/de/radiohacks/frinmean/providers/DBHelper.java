package de.radiohacks.frinmean.providers;

import android.content.ContentProviderClient;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.util.Log;

import com.google.common.hash.HashCode;
import com.google.common.hash.Hashing;
import com.google.common.io.Files;

import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;

import de.radiohacks.frinmean.Constants;
import de.radiohacks.frinmean.adapters.SyncUtils;
import de.radiohacks.frinmean.modelshort.M;
import de.radiohacks.frinmean.modelshort.OAckCD;
import de.radiohacks.frinmean.modelshort.OAckMD;
import de.radiohacks.frinmean.modelshort.OGImM;
import de.radiohacks.frinmean.modelshort.OGImMMD;
import de.radiohacks.frinmean.modelshort.OGTeM;
import de.radiohacks.frinmean.modelshort.OGViM;
import de.radiohacks.frinmean.modelshort.OGViMMD;
import de.radiohacks.frinmean.service.RestFunctions;

import static de.radiohacks.frinmean.Constants.TYP_CONTACT;
import static de.radiohacks.frinmean.Constants.TYP_FILE;
import static de.radiohacks.frinmean.Constants.TYP_IMAGE;
import static de.radiohacks.frinmean.Constants.TYP_LOCATION;
import static de.radiohacks.frinmean.Constants.TYP_TEXT;
import static de.radiohacks.frinmean.Constants.TYP_VIDEO;
import static de.radiohacks.frinmean.Constants.T_MESSAGES_BADBID;
import static de.radiohacks.frinmean.Constants.T_MESSAGES_ChatID;
import static de.radiohacks.frinmean.Constants.T_MESSAGES_ContactMsgID;
import static de.radiohacks.frinmean.Constants.T_MESSAGES_FileMsgID;
import static de.radiohacks.frinmean.Constants.T_MESSAGES_ImageMsgID;
import static de.radiohacks.frinmean.Constants.T_MESSAGES_ImageMsgValue;
import static de.radiohacks.frinmean.Constants.T_MESSAGES_LocationMsgID;
import static de.radiohacks.frinmean.Constants.T_MESSAGES_MessageTyp;
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

/**
 * Created by thomas on 18.01.16.
 */
public class DBHelper {

    private static final String TAG = DBHelper.class.getSimpleName();
    private int userid;
    private String basedir;
    private String imgdir;
    private String icndir;
    private String viddir;
    private String fildir;
    private String username;
    private boolean contentall;
    private Context mContext;

    public DBHelper(Context ctx) {

        this.mContext = ctx;
        getPreferenceInfo();
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

    protected void getPreferenceInfo() {
        Log.d(TAG, "start getPreferenceInfo");
        SharedPreferences sharedPrefs = PreferenceManager
                .getDefaultSharedPreferences(mContext);

        this.username = sharedPrefs.getString(Constants.PrefUsername, "NULL");
        this.contentall = sharedPrefs.getBoolean(Constants.prefContentCommunication, false);
        this.userid = sharedPrefs.getInt(Constants.PrefUserID, -1);
        Log.d(TAG, "end getPferefenceInfo");
    }

    public boolean checkNetwork() {
        ConnectivityManager cm =
                (ConnectivityManager) mContext.getSystemService(Context.CONNECTIVITY_SERVICE);

        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();

        return activeNetwork.isAvailable();
    }

    public boolean checkWIFI() {
        ConnectivityManager cm =
                (ConnectivityManager) mContext.getSystemService(Context.CONNECTIVITY_SERVICE);

        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();

        return (contentall || activeNetwork != null && activeNetwork.getType() == ConnectivityManager.TYPE_WIFI);
    }

    public void SaveMessagetoDB(List<M> in, int c) {
        RestFunctions rf = new RestFunctions();
        for (int k = 0; k < in.size(); k++) {
            M m = in.get(k);
            ContentValues valuesinsmsg = new ContentValues();
            valuesinsmsg.put(T_MESSAGES_BADBID, m.getMID());
            valuesinsmsg.put(T_MESSAGES_OwningUserID, m.getOU().getOUID());
            valuesinsmsg.put(T_MESSAGES_OwningUserName, m.getOU().getOUN());
            valuesinsmsg.put(T_MESSAGES_ChatID, c);
            valuesinsmsg.put(T_MESSAGES_MessageTyp, m.getMT());
            valuesinsmsg.put(T_MESSAGES_SendTimestamp, m.getSdT());
            valuesinsmsg.put(T_MESSAGES_ReadTimestamp, m.getRdT());
            valuesinsmsg.put(T_MESSAGES_ShowTimestamp, m.getShT());

            if (m.getMT().equalsIgnoreCase(TYP_TEXT)) {
                valuesinsmsg.put(T_MESSAGES_TextMsgID, m.getTMID());
                OGTeM oftm = rf.gettextmessage(m.getTMID());
                if (oftm != null) {
                    if (oftm.getET() == null || oftm.getET().isEmpty()) {
                        if (acknowledgeMessage(Constants.TYP_TEXT, oftm.getTM(), m.getMID())) {
                            valuesinsmsg.put(T_MESSAGES_TextMsgValue, oftm.getTM());
                            ContentProviderClient clientmsg = mContext.getContentResolver().acquireContentProviderClient(FrinmeanContentProvider.MESSAGES_CONTENT_URI);
                            ((FrinmeanContentProvider) clientmsg.getLocalContentProvider()).insertorupdate(FrinmeanContentProvider.MESSAGES_CONTENT_URI, valuesinsmsg);
                            clientmsg.release();
                        }
                    }
                }
            } else if (m.getMT().equalsIgnoreCase(TYP_IMAGE)) {
                valuesinsmsg.put(T_MESSAGES_ImageMsgID, m.getIMID());

                String imgfile = downloadimage(m.getIMID(), m.getMID(), Constants.TYP_IMAGE);

                valuesinsmsg.put(T_MESSAGES_ImageMsgValue, imgfile);
                ContentProviderClient client = mContext.getContentResolver().acquireContentProviderClient(FrinmeanContentProvider.MESSAGES_CONTENT_URI);
                ((FrinmeanContentProvider) client.getLocalContentProvider()).insertorupdate(FrinmeanContentProvider.MESSAGES_CONTENT_URI, valuesinsmsg);
                client.release();

                Intent mediaScanIntent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
                Uri contentUri = Uri.fromFile(new File(imgfile));
                mediaScanIntent.setData(contentUri);
                mContext.sendBroadcast(mediaScanIntent);

            } else if (m.getMT().equalsIgnoreCase(TYP_CONTACT)) {
                valuesinsmsg.put(T_MESSAGES_ContactMsgID, m.getCMID());
                ContentProviderClient client = mContext.getContentResolver().acquireContentProviderClient(FrinmeanContentProvider.MESSAGES_CONTENT_URI);
                ((FrinmeanContentProvider) client.getLocalContentProvider()).insertorupdate(FrinmeanContentProvider.MESSAGES_CONTENT_URI, valuesinsmsg);
                client.release();
//                OutFetchContactMessage ofcm = checkAndDownloadImageMessage(m.getImageMsgID());
//                if (ofcm.getET() == null || ofcm.getET().isEmpty()) {
//                    valuesinsmsg.put(Constants.T_MESSAGES_ContactMsgValue, ofcm.getImageMessage());
//                    fcp.insertorupdate(FrinmeanContentProvider.CONTENT_URI, valuesins);
//                }
            } else if (m.getMT().equalsIgnoreCase(TYP_FILE)) {
                valuesinsmsg.put(T_MESSAGES_FileMsgID, m.getFMID());
                ContentProviderClient client = mContext.getContentResolver().acquireContentProviderClient(FrinmeanContentProvider.MESSAGES_CONTENT_URI);
                ((FrinmeanContentProvider) client.getLocalContentProvider()).insertorupdate(FrinmeanContentProvider.MESSAGES_CONTENT_URI, valuesinsmsg);
                client.release();
//                OutFetchFileMessage offm = checkAndDownloadImageMessage(m.getImageMsgID());
//                if (offm.getET() == null || offm.getET().isEmpty()) {
//                    valuesinsmsg.put(Constants.T_MESSAGES_ContactMsgValue, offm.getImageMessage());
//                    fcp.insertorupdate(FrinmeanContentProvider.CONTENT_URI, valuesins);
//                }
            } else if (m.getMT().equalsIgnoreCase(TYP_LOCATION)) {
                valuesinsmsg.put(T_MESSAGES_LocationMsgID, m.getLMID());
                ContentProviderClient client = mContext.getContentResolver().acquireContentProviderClient(FrinmeanContentProvider.MESSAGES_CONTENT_URI);
                ((FrinmeanContentProvider) client.getLocalContentProvider()).insertorupdate(FrinmeanContentProvider.MESSAGES_CONTENT_URI, valuesinsmsg);
                client.release();
//                OutFetchLocationMessage oflm = checkAndDownloadImageMessage(m.getImageMsgID());
//                if (oflm.getET() == null || oflm.getET().isEmpty()) {
//                    valuesinsmsg.put(Constants.T_MESSAGES_ContactMsgValue, oflm.getImageMessage());
//                    fcp.insertorupdate(FrinmeanContentProvider.CONTENT_URI, valuesins);
//                }
            } else if (m.getMT().equalsIgnoreCase(TYP_VIDEO)) {
                valuesinsmsg.put(T_MESSAGES_VideoMsgID, m.getVMID());
                OGViMMD outmeta = rf.getVideoMessageMetaData(m.getVMID());

                if (outmeta != null) {
                    if (outmeta.getET() == null || outmeta.getET().isEmpty()) {
                        if (!checkfileexists(outmeta.getVM(), TYP_VIDEO, outmeta.getVS(), outmeta.getVMD5())) {
                            if (checkWIFI()) {
                                OGViM ofvm = rf.fetchVideoMessage(m.getVMID());
                                if (ofvm != null) {
                                    if (ofvm.getET() == null || ofvm.getET().isEmpty()) {
                                        String checkfilepath = viddir + ofvm.getVM();
                                        if (acknowledgeMessage(Constants.TYP_VIDEO, checkfilepath, m.getMID())) {
                                            valuesinsmsg.put(T_MESSAGES_VideoMsgValue, checkfilepath);
                                            ContentProviderClient client = mContext.getContentResolver().acquireContentProviderClient(FrinmeanContentProvider.MESSAGES_CONTENT_URI);
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
                                ContentProviderClient client = mContext.getContentResolver().acquireContentProviderClient(FrinmeanContentProvider.MESSAGES_CONTENT_URI);
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

    public String downloadimage(int inImgID, int inMsgID, String ImageType) {

        String ret = null;
        RestFunctions rf = new RestFunctions();
        OGImMMD outmeta = rf.getImageMessageMetaData(inImgID);

        if (outmeta != null) {
            if (outmeta.getET() == null || outmeta.getET().isEmpty()) {
                if (!checkfileexists(outmeta.getIM(), ImageType, outmeta.getIS(), outmeta.getIMD5())) {
                    if (checkWIFI()) {
                        OGImM ofim = rf.fetchImageMessage(inImgID, ImageType);
                        if (ofim != null) {
                            if (ofim.getET() == null || ofim.getET().isEmpty()) {
                                String checkfilepath;
                                if (ImageType.equalsIgnoreCase(Constants.TYP_IMAGE)) {
                                    checkfilepath = imgdir + File.separator + ofim.getIM();
                                    if (acknowledgeMessage(Constants.TYP_IMAGE, checkfilepath, inMsgID)) {
                                        ret = checkfilepath;
                                    }
                                } else if (ImageType.equalsIgnoreCase(Constants.TYP_ICON)) {
                                    ret = icndir + File.separator + ofim.getIM();
                                }
                            }
                        }
                    }
                } else {
                    String checkfilepath;
                    if (ImageType.equalsIgnoreCase(Constants.TYP_IMAGE)) {
                        checkfilepath = imgdir + File.separator + outmeta.getIM();
                        if (acknowledgeMessage(Constants.TYP_IMAGE, checkfilepath, inMsgID)) {
                            ret = checkfilepath;
                        }
                    } else if (ImageType.equalsIgnoreCase(Constants.TYP_ICON)) {
                        ret = icndir + outmeta.getIM();
                    }
                }
            }
        }
        return ret;
    }

    public String downloadvideo(int inVidID, int inMsgID) {

        String ret = null;
        RestFunctions rf = new RestFunctions();
        OGViMMD outmeta = rf.getVideoMessageMetaData(inVidID);

        if (outmeta != null) {
            if (outmeta.getET() == null || outmeta.getET().isEmpty()) {
                if (!checkfileexists(outmeta.getVM(), Constants.TYP_VIDEO, outmeta.getVS(), outmeta.getVMD5())) {
                    if (checkWIFI()) {
                        OGViM ovim = rf.fetchVideoMessage(inVidID);
                        if (ovim != null) {
                            if (ovim.getET() == null || ovim.getET().isEmpty()) {
                                String checkfilepath;
                                checkfilepath = viddir + File.separator + ovim.getVM();
                                if (acknowledgeMessage(Constants.TYP_VIDEO, checkfilepath, inMsgID)) {
                                    ret = checkfilepath;
                                }
                            }
                        }
                    }
                } else {
                    String checkfilepath;
                    checkfilepath = viddir + File.separator + outmeta.getVM();
                    if (acknowledgeMessage(Constants.TYP_VIDEO, checkfilepath, inMsgID)) {
                        ret = checkfilepath;
                    }
                }
            }
        }
        return ret;
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


    public void insertImageMesgIntoDB(int ChatID, int UserID, String Message) {
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

    public void insertVideoMesgIntoDB(int ChatID, int UserID, String Message) {
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


    public void insertNewMsgIntoDB(int ChatID, int UserID, String MessageType, String Message) {
        Log.d(TAG, "start insertMsgIntoDB");

        // Insert new Message into local DB and trigger Sync to upload the Information.
        // To find the not send messages the Backend ID musst be 0 and the
        // Sendtimestamp musst be 0
        // The Readtimestamp and the MessageIDs are supplied by the Server
        // The ChatID is needed to insert the Message into the right Chat afterwards

        long time = System.currentTimeMillis() / 1000L;

        ContentValues valuesins = new ContentValues();
        valuesins.put(Constants.T_MESSAGES_BADBID, 0);
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
        ContentProviderClient client = mContext.getContentResolver().acquireContentProviderClient(FrinmeanContentProvider.MESSAGES_CONTENT_URI);
        client.getLocalContentProvider().insert(FrinmeanContentProvider.MESSAGES_CONTENT_URI, valuesins);
        client.release();
        SyncUtils.TriggerRefresh();

        Log.d(TAG, "end insertMsgIntoDB");
    }

    public void insertFwdMsgIntoDB(int ChatID, int UserID, int MsgID, long timeStamp, String MessageType, String ContentMessage, int ContentMsgID) {
        Log.d(TAG, "start insertFwdMsgIntoDB");

        // Insert new Message into local DB and trigger Sync to upload the Information.
        // To find the not send messages the Backend ID musst be 0 and the
        // Sendtimestamp musst be 0
        // The Readtimestamp and the MessageIDs are supplied by the Server
        // The ChatID is needed to insert the Message into the right Chat afterwards

        ContentValues valuesins = new ContentValues();
        valuesins.put(Constants.T_MESSAGES_BADBID, MsgID);
//        valuesins.put(Constants.T_MESSAGES_NumberAll, 0);
//        valuesins.put(Constants.T_MESSAGES_NumberRead, 0);
//        valuesins.put(Constants.T_MESSAGES_NumberShow, 0);
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
        ContentProviderClient client = mContext.getContentResolver().acquireContentProviderClient(FrinmeanContentProvider.MESSAGES_CONTENT_URI);
        client.getLocalContentProvider().insert(FrinmeanContentProvider.MESSAGES_CONTENT_URI, valuesins);
        client.release();

        inserIntoTimeTable(MsgID, UserID);
        SyncUtils.TriggerRefresh();

        Log.d(TAG, "end insertFwdMsgIntoDB");
    }

    public void insertUserIntoDB(int uid, String uname, String puname, String email, long auth, int iconid, String iconvalue) {
        Log.d(TAG, "start insertUserIntoDB");

        if (uid > 0) {
            ContentValues valuesins = new ContentValues();
            valuesins.put(Constants.T_USER_BADBID, uid);
            if (uname != null && !uname.isEmpty()) {
                valuesins.put(Constants.T_USER_Username, uname);
            }
            if (puname != null && !puname.isEmpty()) {
                valuesins.put(Constants.T_USER_PhoneUsername, puname);
            }
            if (email != null && !email.isEmpty()) {
                valuesins.put(Constants.T_USER_Email, email);
            }
            if (auth > 0) {
                valuesins.put(Constants.T_USER_AuthenticationTime, auth);
            }
            if (iconid > 0) {
                valuesins.put(Constants.T_User_IconID, iconid);
            }
            if (iconvalue != null && !iconvalue.isEmpty()) {
                valuesins.put(Constants.T_User_IconValue, iconvalue);
            }

            ContentProviderClient client = mContext.getContentResolver().acquireContentProviderClient(FrinmeanContentProvider.USER_CONTENT_URI);
            ((FrinmeanContentProvider) client.getLocalContentProvider()).insertorupdate(FrinmeanContentProvider.USER_CONTENT_URI, valuesins);
            client.release();
        }
        Log.d(TAG, "end insertUserIntoDB");
    }

    public void inserIntoTimeTable(int inBAID, int inUSID) {
        ContentValues valuesins = new ContentValues();
        valuesins.put(T_MESSAGES_TIME_BADBID, inBAID);
        valuesins.put(T_MESSAGES_TIME_UserID, inUSID);
        ContentProviderClient client = mContext.getContentResolver().acquireContentProviderClient(FrinmeanContentProvider.MESSAGES_TIME_CONTENT_URI);
        ((FrinmeanContentProvider) client.getLocalContentProvider()).insertorupdate(FrinmeanContentProvider.MESSAGES_TIME_CONTENT_URI, valuesins);
        client.release();
    }

    public boolean checkfileexists(String fname, String msgType, long fsize, String inmd5sumd) {
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
        } else if (msgType.equalsIgnoreCase(Constants.TYP_ICON)) {
            checkfilepath = icndir + fname;
        }

        // To
        // Hier fehlt das Icon!!!!

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

    public void moveFileToDestination(String origFile, String subdir, String serverfilename) {
        Log.d(TAG, "start moveFileToDestination");
        File source = new File(origFile);

        // Where to store it.
        String destFile = basedir;
        // Add SubDir for Images, videos or files
        if (destFile.endsWith(File.separator)) {
            destFile += subdir + File.separator + serverfilename;
        } else {
            destFile += File.separator + subdir + File.separator + serverfilename;
        }

        File destination = new File(destFile);
        try {
            FileUtils.moveFile(source, destination);
        } catch (IOException e) {
            e.printStackTrace();
        }
        boolean X = destination.exists();
        Log.d(TAG, "end moveFileToDestination");
    }

    public boolean acknowledgeMessage(String msgType, String message, int msgid) {
        Log.d(TAG, "start acknowledgeMessage");

        boolean ret = false;
        RestFunctions rf = new RestFunctions();

        if (msgType.equalsIgnoreCase(Constants.TYP_TEXT)) {
            /* Hasher hasher = Hashing.md5().newHasher();
            hasher.putBytes(message.getBytes());
            byte[] md5 = hasher.hash().asBytes(); */
            int hashCode = message.hashCode();

            OAckMD oack = rf.acknowledgemessagedownload(msgid, String.valueOf(hashCode));
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
            OAckMD oack = rf.acknowledgemessagedownload(msgid, md5.toString());
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
            OAckMD oack = rf.acknowledgemessagedownload(msgid, md5.toString());
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

    public boolean acknowledgeChat(String Chatname, int chatid) {
        Log.d(TAG, "start acknowledgeMessage");

        boolean ret = false;
        RestFunctions rf = new RestFunctions();

        int hashCode = Chatname.hashCode();

        OAckCD oack = rf.acknowledgechatdownload(chatid, String.valueOf(hashCode));
        if (oack != null) {
            if (oack.getET() == null || oack.getET().isEmpty()) {
                if (oack.getACK().equalsIgnoreCase(Constants.ACKNOWLEDGE_TRUE)) {
                    ret = true;
                }
            }
        }
        Log.d(TAG, "end acknowledgeMessage");
        return ret;
    }
}
