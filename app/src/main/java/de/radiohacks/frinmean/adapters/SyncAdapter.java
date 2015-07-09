/*
 * Copyright © 2015, Thomas Schreiner, thomas1.schreiner@googlemail.com
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * 1. Redistributions of source code must retain the above copyright notice, this
 *    list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR
 * ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 *
 * The views and conclusions contained in the software and documentation are those
 * of the authors and should not be interpreted as representing official policies,
 * either expressed or implied, of the FreeBSD Project.
 */

package de.radiohacks.frinmean.adapters;

import android.accounts.Account;
import android.annotation.TargetApi;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.AbstractThreadedSyncAdapter;
import android.content.ContentProviderClient;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SyncResult;
import android.database.Cursor;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.util.Log;

import com.google.common.hash.HashCode;
import com.google.common.hash.Hashing;
import com.google.common.io.Files;

import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;
import java.util.List;

import de.radiohacks.frinmean.Constants;
import de.radiohacks.frinmean.R;
import de.radiohacks.frinmean.SingleChatActivity;
import de.radiohacks.frinmean.modelshort.CNC;
import de.radiohacks.frinmean.modelshort.CNM;
import de.radiohacks.frinmean.modelshort.M;
import de.radiohacks.frinmean.modelshort.OAckCD;
import de.radiohacks.frinmean.modelshort.OAckMD;
import de.radiohacks.frinmean.modelshort.OCN;
import de.radiohacks.frinmean.modelshort.OFMFC;
import de.radiohacks.frinmean.modelshort.OGImM;
import de.radiohacks.frinmean.modelshort.OGImMMD;
import de.radiohacks.frinmean.modelshort.OGMI;
import de.radiohacks.frinmean.modelshort.OGTeM;
import de.radiohacks.frinmean.modelshort.OGViM;
import de.radiohacks.frinmean.modelshort.OGViMMD;
import de.radiohacks.frinmean.modelshort.OIMIC;
import de.radiohacks.frinmean.modelshort.OSImM;
import de.radiohacks.frinmean.modelshort.OSTeM;
import de.radiohacks.frinmean.modelshort.OSViM;
import de.radiohacks.frinmean.providers.FrinmeanContentProvider;
import de.radiohacks.frinmean.service.CustomExceptionHandler;
import de.radiohacks.frinmean.service.RestFunctions;

import static de.radiohacks.frinmean.Constants.CHAT_DB_Columns;
import static de.radiohacks.frinmean.Constants.ID_MESSAGES_ChatID;
import static de.radiohacks.frinmean.Constants.ID_MESSAGES_MessageType;
import static de.radiohacks.frinmean.Constants.ID_MESSAGES_TextMsgValue;
import static de.radiohacks.frinmean.Constants.ID_MESSAGES__id;
import static de.radiohacks.frinmean.Constants.MESSAGES_DB_Columns;
import static de.radiohacks.frinmean.Constants.TYP_CONTACT;
import static de.radiohacks.frinmean.Constants.TYP_FILE;
import static de.radiohacks.frinmean.Constants.TYP_IMAGE;
import static de.radiohacks.frinmean.Constants.TYP_LOCATION;
import static de.radiohacks.frinmean.Constants.TYP_TEXT;
import static de.radiohacks.frinmean.Constants.TYP_VIDEO;
import static de.radiohacks.frinmean.Constants.T_CHAT_BADBID;
import static de.radiohacks.frinmean.Constants.T_CHAT_ChatName;
import static de.radiohacks.frinmean.Constants.T_CHAT_OwningUserID;
import static de.radiohacks.frinmean.Constants.T_CHAT_OwningUserName;
import static de.radiohacks.frinmean.Constants.T_MESSAGES_BADBID;
import static de.radiohacks.frinmean.Constants.T_MESSAGES_ChatID;
import static de.radiohacks.frinmean.Constants.T_MESSAGES_ContactMsgID;
import static de.radiohacks.frinmean.Constants.T_MESSAGES_ContactMsgValue;
import static de.radiohacks.frinmean.Constants.T_MESSAGES_FileMsgID;
import static de.radiohacks.frinmean.Constants.T_MESSAGES_FileMsgValue;
import static de.radiohacks.frinmean.Constants.T_MESSAGES_ID;
import static de.radiohacks.frinmean.Constants.T_MESSAGES_ImageMsgID;
import static de.radiohacks.frinmean.Constants.T_MESSAGES_ImageMsgValue;
import static de.radiohacks.frinmean.Constants.T_MESSAGES_LocationMsgID;
import static de.radiohacks.frinmean.Constants.T_MESSAGES_LocationMsgValue;
import static de.radiohacks.frinmean.Constants.T_MESSAGES_MessageTyp;
import static de.radiohacks.frinmean.Constants.T_MESSAGES_NumberAll;
import static de.radiohacks.frinmean.Constants.T_MESSAGES_NumberRead;
import static de.radiohacks.frinmean.Constants.T_MESSAGES_NumberShow;
import static de.radiohacks.frinmean.Constants.T_MESSAGES_OwningUserID;
import static de.radiohacks.frinmean.Constants.T_MESSAGES_OwningUserName;
import static de.radiohacks.frinmean.Constants.T_MESSAGES_ReadTimestamp;
import static de.radiohacks.frinmean.Constants.T_MESSAGES_SendTimestamp;
import static de.radiohacks.frinmean.Constants.T_MESSAGES_ShowTimestamp;
import static de.radiohacks.frinmean.Constants.T_MESSAGES_TextMsgID;
import static de.radiohacks.frinmean.Constants.T_MESSAGES_TextMsgValue;
import static de.radiohacks.frinmean.Constants.T_MESSAGES_VideoMsgID;
import static de.radiohacks.frinmean.Constants.T_MESSAGES_VideoMsgValue;

public class SyncAdapter extends AbstractThreadedSyncAdapter {
    public static final String TAG = "SyncAdapter";
    /**
     * Content resolver, for performing database operations.
     */
    private final ContentResolver mContentResolver;
    private String username;
    private String password;
    private String directory;
    private int userid;
    private String ringtone;
    private boolean vibrate;
    private RestFunctions rf;
    private Context mContext;
    private boolean networtConnected = false;
    private boolean isWifi = false;
    private boolean contentall = false;

    /**
     * Constructor. Obtains handle to content resolver for later use.
     */
    public SyncAdapter(Context context, boolean autoInitialize) {
        super(context, autoInitialize);
        mContext = context;
        mContentResolver = mContext.getContentResolver();
        getPreferenceInfo();
        rf = new RestFunctions();
        if (directory.equalsIgnoreCase("NULL")) {
            if (!(Thread.getDefaultUncaughtExceptionHandler() instanceof CustomExceptionHandler)) {
                Thread.setDefaultUncaughtExceptionHandler(new CustomExceptionHandler(Environment.getExternalStorageDirectory().toString()));
            }
        } else {
            if (!(Thread.getDefaultUncaughtExceptionHandler() instanceof CustomExceptionHandler)) {
                Thread.setDefaultUncaughtExceptionHandler(new CustomExceptionHandler(directory));
            }
        }
    }

    /**
     * Constructor. Obtains handle to content resolver for later use.
     */
    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    public SyncAdapter(Context context, boolean autoInitialize, boolean allowParallelSyncs) {
        super(context, autoInitialize, allowParallelSyncs);
        mContentResolver = context.getContentResolver();
        getPreferenceInfo();
        rf = new RestFunctions();
        if (directory.equalsIgnoreCase("NULL")) {
            if (!(Thread.getDefaultUncaughtExceptionHandler() instanceof CustomExceptionHandler)) {
                Thread.setDefaultUncaughtExceptionHandler(new CustomExceptionHandler(Environment.getExternalStorageDirectory().toString()));
            }
        } else {
            if (!(Thread.getDefaultUncaughtExceptionHandler() instanceof CustomExceptionHandler)) {
                Thread.setDefaultUncaughtExceptionHandler(new CustomExceptionHandler(directory));
            }
        }
    }

    protected void getPreferenceInfo() {
        Log.d(TAG, "start getPreferenceInfo");
        SharedPreferences sharedPrefs = PreferenceManager
                .getDefaultSharedPreferences(getContext());

        this.username = sharedPrefs.getString(Constants.PrefUsername, "NULL");
        this.password = sharedPrefs.getString(Constants.PrefPassword, "NULL");
        this.directory = sharedPrefs.getString(Constants.PrefDirectory, "NULL");
        this.userid = sharedPrefs.getInt(Constants.PrefUserID, -1);
        this.ringtone = sharedPrefs.getString(Constants.prefRingtone, "DEFAULT_SOUND");
        this.vibrate = sharedPrefs.getBoolean(Constants.prefVibrate, true);
        this.contentall = sharedPrefs.getBoolean(Constants.prefContentCommunication, false);
        Log.d(TAG, "end getPferefenceInfo");
    }

    private void checkNetwork() {
        ConnectivityManager cm =
                (ConnectivityManager) mContext.getSystemService(Context.CONNECTIVITY_SERVICE);

        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
        networtConnected = activeNetwork != null &&
                activeNetwork.isConnectedOrConnecting();

        isWifi = contentall || activeNetwork != null && activeNetwork.getType() == ConnectivityManager.TYPE_WIFI;
    }

    /**
     * Called by the Android system in response to a request to run the sync adapter. The work
     * required to read data from the network, parse it, and store it in the content provider is
     * done here. Extending AbstractThreadedSyncAdapter ensures that all methods within SyncAdapter
     * run on a background thread. For this reason, blocking I/O and other long-running tasks can be
     * run <em>in situ</em>, and you don't have to set up a separate thread for them.
     * .
     * <p/>
     * <p>This is where we actually perform any work required to perform a sync.
     * {@link android.content.AbstractThreadedSyncAdapter} guarantees that this will be called on a non-UI thread,
     * so it is safe to peform blocking I/O here.
     * <p/>
     * <p>The syncResult argument allows you to pass information back to the method that triggered
     * the sync.
     */
    @Override
    public void onPerformSync(Account account, Bundle extras, String authority,
                              ContentProviderClient provider, SyncResult syncResult) {
        Log.i(TAG, "Beginning network synchronization");
        getPreferenceInfo();
        checkNetwork();
        if (networtConnected) {
            syncCheckNewMessages();
            uploadUnsavedMessages();
            syncGetMessageInformation();
        }
        Log.i(TAG, "Network synchronization complete");
    }

    private void syncCheckNewMessages() {
        Log.d(TAG, "start syncCheckNewMessages");

        OCN outcheck = rf.checknewmessages(username, password);

        if (outcheck != null) {
            if (outcheck.getET() == null || outcheck.getET().isEmpty()) {
                    if (outcheck.getCNC() != null && outcheck.getCNC().size() > 0) {
                        SaveChatsToLDB(outcheck.getCNC());
                    }
                    if (outcheck.getCNM() != null && outcheck.getCNM().size() > 0) {
                        for (int i = 0; i < outcheck.getCNM().size(); i++) {
                            CNM c = outcheck.getCNM().get(i);
                            syncGetMessageFromChat(c.getCID(), 0, c.getCN());
                        }
                    }
            }
        }
        Log.d(TAG, "end syncCheckNewMessages");
    }


    public void syncGetMessageFromChat(int cid, long readtime, String CName) {
        Log.d(TAG, "start syncGetMessageFromChat");


        OFMFC res = rf.getmessagefromchat(username, password, cid, readtime);

        if (res != null) {
            if (res.getET() == null || res.getET().isEmpty()) {
                if (res.getM() != null && res.getM().size() > 0) {
                    SaveMessageToLDB(res.getM(), cid, CName);
                }
            }
        }
        // TODO Check if App is running, the display nothing else make a Notofication
        // mBroadcaster.notifyProgress(ret, Constants.BROADCAST_GETMESSAGEFROMCHAT);
        Log.d(TAG, "end syncGetMessageFromChat");
    }

    private void SaveChatsToLDB(List<CNC> in) {
        Log.d(TAG, "start SaveChatsToLDB");
        for (int j = 0; j < in.size(); j++) {
            CNC c = in.get(j);
            ContentValues valuesins = new ContentValues();
            valuesins.put(T_CHAT_BADBID, c.getCID());
            valuesins.put(T_CHAT_OwningUserID, c.getOU().getOUID());
            valuesins.put(T_CHAT_OwningUserName, c.getOU().getOUN());
            valuesins.put(T_CHAT_ChatName, c.getCN());
            if (acknowledgeChat(c.getCN(), c.getCID())) {
                ContentProviderClient client = mContentResolver.acquireContentProviderClient(FrinmeanContentProvider.CHAT_CONTENT_URI);
                ((FrinmeanContentProvider) client.getLocalContentProvider()).insertorupdate(FrinmeanContentProvider.CHAT_CONTENT_URI, valuesins);
                client.release();
            }
        }
        Log.d(TAG, "end saveChatsToLDB");
    }

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
    private void SaveMessageToLDB(List<M> in, int ChatID, String ChatName) {
        Log.d(TAG, "start SaveMessageToLDB");
        for (int j = 0; j < in.size(); j++) {
            M m = in.get(j);
            ContentValues valuesins = new ContentValues();
            valuesins.put(T_MESSAGES_BADBID, m.getMID());
            valuesins.put(T_MESSAGES_OwningUserID, m.getOU().getOUID());
            valuesins.put(T_MESSAGES_OwningUserName, m.getOU().getOUN());
            valuesins.put(T_MESSAGES_ChatID, ChatID);
            valuesins.put(T_MESSAGES_MessageTyp, m.getMT());
            valuesins.put(T_MESSAGES_SendTimestamp, m.getSdT());
            valuesins.put(T_MESSAGES_ReadTimestamp, m.getRdT());
            valuesins.put(T_MESSAGES_ShowTimestamp, m.getShT());
            valuesins.put(T_MESSAGES_NumberAll, m.getNT());
            valuesins.put(T_MESSAGES_NumberShow, m.getNS());
            valuesins.put(T_MESSAGES_NumberRead, m.getNR());

            if (m.getMT().equalsIgnoreCase(TYP_TEXT)) {
                valuesins.put(T_MESSAGES_TextMsgID, m.getTMID());
                OGTeM oftm = rf.gettextmessage(username, password, m.getTMID());
                if (oftm != null) {
                    if (oftm.getET() == null || oftm.getET().isEmpty()) {
                        if (acknowledgeMessage(Constants.TYP_TEXT, oftm.getTM(), m.getMID())) {
                            valuesins.put(T_MESSAGES_TextMsgValue, oftm.getTM());
                            ContentProviderClient client = mContentResolver.acquireContentProviderClient(FrinmeanContentProvider.MESSAES_CONTENT_URI);
                            ((FrinmeanContentProvider) client.getLocalContentProvider()).insertorupdate(FrinmeanContentProvider.MESSAES_CONTENT_URI, valuesins);
                            client.release();
                        }
                    }
                }
            } else if (m.getMT().equalsIgnoreCase(TYP_IMAGE)) {
                valuesins.put(T_MESSAGES_ImageMsgID, m.getIMID());
                OGImMMD outmeta = rf.getImageMessageMetaData(username, password, m.getIMID());

                if (outmeta != null) {
                    if (outmeta.getET() == null || outmeta.getET().isEmpty()) {
                        if (!checkfileexists(outmeta.getIM(), TYP_IMAGE, outmeta.getIS(), outmeta.getIMD5())) {
                            if (isWifi) {
                                OGImM ofim = rf.fetchImageMessage(username, password, m.getIMID());
                                if (ofim != null) {
                                    if (ofim.getET() == null || ofim.getET().isEmpty()) {
                                        String checkfilepath;

                                        if (directory.endsWith(File.separator)) {
                                            checkfilepath = directory + Constants.IMAGEDIR + File.separator + ofim.getIM();
                                        } else {
                                            checkfilepath = directory + File.separator + Constants.IMAGEDIR + File.separator + ofim.getIM();
                                        }
                                        if (acknowledgeMessage(Constants.TYP_IMAGE, checkfilepath, m.getMID())) {
                                            valuesins.put(T_MESSAGES_ImageMsgValue, ofim.getIM());
                                            ContentProviderClient client = mContentResolver.acquireContentProviderClient(FrinmeanContentProvider.MESSAES_CONTENT_URI);
                                            ((FrinmeanContentProvider) client.getLocalContentProvider()).insertorupdate(FrinmeanContentProvider.MESSAES_CONTENT_URI, valuesins);
                                            client.release();

                                            Intent mediaScanIntent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
                                            Uri contentUri = Uri.fromFile(new File(checkfilepath));
                                            mediaScanIntent.setData(contentUri);
                                            mContext.sendBroadcast(mediaScanIntent);
                                        }
                                    }
                                }
                            }
                        } else {
                            String checkfilepath;
                            if (directory.endsWith(File.separator)) {
                                checkfilepath = directory + Constants.IMAGEDIR + File.separator + outmeta.getIM();
                            } else {
                                checkfilepath = directory + File.separator + Constants.IMAGEDIR + File.separator + outmeta.getIM();
                            }
                            if (acknowledgeMessage(Constants.TYP_IMAGE, checkfilepath, m.getMID())) {
                                valuesins.put(T_MESSAGES_ImageMsgValue, outmeta.getIM());
                                ContentProviderClient client = mContentResolver.acquireContentProviderClient(FrinmeanContentProvider.MESSAES_CONTENT_URI);
                                ((FrinmeanContentProvider) client.getLocalContentProvider()).insertorupdate(FrinmeanContentProvider.MESSAES_CONTENT_URI, valuesins);
                                client.release();
                            }
                        }
                    }
                }
            } else if (m.getMT().equalsIgnoreCase(TYP_CONTACT)) {
                valuesins.put(T_MESSAGES_ContactMsgID, m.getCMID());
                ContentProviderClient client = mContentResolver.acquireContentProviderClient(FrinmeanContentProvider.MESSAES_CONTENT_URI);
                ((FrinmeanContentProvider) client.getLocalContentProvider()).insertorupdate(FrinmeanContentProvider.MESSAES_CONTENT_URI, valuesins);
                client.release();
//                OutFetchContactMessage ofcm = checkAndDownloadImageMessage(m.getImageMsgID());
//                if (ofcm.getET() == null || ofcm.getET().isEmpty()) {
//                    valuesins.put(Constants.T_MESSAGES_ContactMsgValue, ofcm.getImageMessage());
//                    fcp.insertorupdate(FrinmeanContentProvider.CONTENT_URI, valuesins);
//                }
            } else if (m.getMT().equalsIgnoreCase(TYP_FILE)) {
                valuesins.put(T_MESSAGES_FileMsgID, m.getFMID());
                ContentProviderClient client = mContentResolver.acquireContentProviderClient(FrinmeanContentProvider.MESSAES_CONTENT_URI);
                ((FrinmeanContentProvider) client.getLocalContentProvider()).insertorupdate(FrinmeanContentProvider.MESSAES_CONTENT_URI, valuesins);
                client.release();
//                OutFetchFileMessage offm = checkAndDownloadImageMessage(m.getImageMsgID());
//                if (offm.getET() == null || offm.getET().isEmpty()) {
//                    valuesins.put(Constants.T_MESSAGES_ContactMsgValue, offm.getImageMessage());
//                    fcp.insertorupdate(FrinmeanContentProvider.CONTENT_URI, valuesins);
//                }
            } else if (m.getMT().equalsIgnoreCase(TYP_LOCATION)) {
                valuesins.put(T_MESSAGES_LocationMsgID, m.getLMID());
                ContentProviderClient client = mContentResolver.acquireContentProviderClient(FrinmeanContentProvider.MESSAES_CONTENT_URI);
                ((FrinmeanContentProvider) client.getLocalContentProvider()).insertorupdate(FrinmeanContentProvider.MESSAES_CONTENT_URI, valuesins);
                client.release();
//                OutFetchLocationMessage oflm = checkAndDownloadImageMessage(m.getImageMsgID());
//                if (oflm.getET() == null || oflm.getET().isEmpty()) {
//                    valuesins.put(Constants.T_MESSAGES_ContactMsgValue, oflm.getImageMessage());
//                    fcp.insertorupdate(FrinmeanContentProvider.CONTENT_URI, valuesins);
//                }
            } else if (m.getMT().equalsIgnoreCase(TYP_VIDEO)) {
                valuesins.put(T_MESSAGES_VideoMsgID, m.getVMID());
                OGViMMD outmeta = rf.getVideoMessageMetaData(username, password, m.getVMID());

                if (outmeta != null) {
                    if (outmeta.getET() == null || outmeta.getET().isEmpty()) {
                        if (!checkfileexists(outmeta.getVM(), TYP_VIDEO, outmeta.getVS(), outmeta.getVMD5())) {
                            if (isWifi) {
                                OGViM ofvm = rf.fetchVideoMessage(username, password, m.getVMID());
                                if (ofvm != null) {
                                    if (ofvm.getET() == null || ofvm.getET().isEmpty()) {
                                        String checkfilepath;

                                        if (directory.endsWith(File.separator)) {
                                            checkfilepath = directory + Constants.VIDEODIR + File.separator + ofvm.getVM();
                                        } else {
                                            checkfilepath = directory + File.separator + Constants.VIDEODIR + File.separator + ofvm.getVM();
                                        }
                                        if (acknowledgeMessage(Constants.TYP_VIDEO, checkfilepath, m.getMID())) {
                                            valuesins.put(T_MESSAGES_VideoMsgValue, ofvm.getVM());
                                            ContentProviderClient client = mContentResolver.acquireContentProviderClient(FrinmeanContentProvider.MESSAES_CONTENT_URI);
                                            ((FrinmeanContentProvider) client.getLocalContentProvider()).insertorupdate(FrinmeanContentProvider.MESSAES_CONTENT_URI, valuesins);
                                            client.release();
                                        }
                                    }
                                }
                            }
                        } else {
                            String checkfilepath;

                            if (directory.endsWith(File.separator)) {
                                checkfilepath = directory + Constants.VIDEODIR + File.separator + outmeta.getVM();
                            } else {
                                checkfilepath = directory + File.separator + Constants.VIDEODIR + File.separator + outmeta.getVM();
                            }
                            if (acknowledgeMessage(Constants.TYP_IMAGE, checkfilepath, m.getMID())) {
                                valuesins.put(T_MESSAGES_VideoMsgValue, outmeta.getVM());
                                ContentProviderClient client = mContentResolver.acquireContentProviderClient(FrinmeanContentProvider.MESSAES_CONTENT_URI);
                                ((FrinmeanContentProvider) client.getLocalContentProvider()).insertorupdate(FrinmeanContentProvider.MESSAES_CONTENT_URI, valuesins);
                                client.release();
                            }
                        }
                    }
                }
            }
        }

        // Now we do the Notification for the User
        // Get needed Information from ContentProvider
        ContentProviderClient client = mContentResolver.acquireContentProviderClient(FrinmeanContentProvider.CHAT_CONTENT_URI);
        Cursor c = client.getLocalContentProvider().query(FrinmeanContentProvider.CHAT_CONTENT_URI, CHAT_DB_Columns, T_CHAT_BADBID + " = ?", new String[]{String.valueOf(ChatID)}, null);

        if (c.moveToFirst()) {
            // Prepare intent which is triggered if the
            // notification is selected

            Intent resultIntent = new Intent(this.getContext(),
                    SingleChatActivity.class);
            resultIntent.putExtra(Constants.CHATID, ChatID);
            resultIntent.putExtra(Constants.CHATNAME, ChatName);
            resultIntent.putExtra(Constants.OWNINGUSERID, c.getInt(Constants.ID_CHAT_OwningUserID));
            resultIntent.putExtra(Constants.USERID, userid);

            PendingIntent pIntent = PendingIntent.getActivity(this.getContext(), 0, resultIntent, 0);

            long[] vibpattern = {500, 100, 500};
            // Build notification
            Notification.Builder nb = new Notification.Builder(this.getContext());
            nb.setContentTitle(ChatName);
            nb.setContentText(String.valueOf(in.size()) + " neue Nachrichten im Chat").setSmallIcon(R.drawable.ic_stat_frinmean);
            nb.setSound(Uri.parse(ringtone));
            nb.setContentIntent(pIntent);

            if (vibrate) {
                nb.setVibrate(vibpattern);
            }

            Notification noti = nb.build();

            NotificationManager notificationManager = (NotificationManager) this.getContext().getSystemService(Context.NOTIFICATION_SERVICE);
            // hide the notification after its selected
            noti.flags |= Notification.FLAG_AUTO_CANCEL;

            notificationManager.notify(0, noti);
        }
        c.close();
        client.release();
        Log.d(TAG, "end saveMessageToLDB");
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

    private boolean acknowledgeChat(String Chatname, int chatid) {
        Log.d(TAG, "start acknowledgeMessage");

        boolean ret = false;

        int hashCode = Chatname.hashCode();

        OAckCD oack = rf.acknowledgechatdownload(username, password, chatid, String.valueOf(hashCode));
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

    private boolean checkfileexists(String fname, String msgType, long fsize, String inmd5sumd) {
        Log.d(TAG, "start checkfileexists");

        boolean ret = false;
        File checkfile;
        String checkfilepath = null;

        if (directory.endsWith(File.separator)) {
            if (msgType.equalsIgnoreCase(Constants.TYP_IMAGE)) {
                checkfilepath = directory + Constants.IMAGEDIR + File.separator + fname;
            } else if (msgType.equalsIgnoreCase(Constants.TYP_VIDEO)) {
                checkfilepath = directory + Constants.VIDEODIR + File.separator + fname;
            } else if (msgType.equalsIgnoreCase(Constants.TYP_FILE)) {
                checkfilepath = directory + Constants.FILESDIR + File.separator + fname;
            }
        } else {
            if (msgType.equalsIgnoreCase(Constants.TYP_IMAGE)) {
                checkfilepath = directory + File.separator + Constants.IMAGEDIR + File.separator + fname;
            } else if (msgType.equalsIgnoreCase(Constants.TYP_VIDEO)) {
                checkfilepath = directory + File.separator + Constants.VIDEODIR + File.separator + fname;
            } else if (msgType.equalsIgnoreCase(Constants.TYP_FILE)) {
                checkfilepath = directory + File.separator + Constants.FILESDIR + File.separator + fname;
            }
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

    private void moveFileToDestination(String origFile, String subdir, String serverfilename) {
        Log.d(TAG, "start moveFileToDestination");
        File source = new File(origFile);

        // Where to store it.
        String destFile = directory;
        // Add SubDir for Images, videos or files
        if (destFile.endsWith(File.separator)) {
            destFile += subdir;
        } else {
            destFile += File.separator + subdir;
        }

        if (destFile.endsWith(File.separator)) {
            destFile += serverfilename;
        } else {
            destFile += File.separator + serverfilename;
        }

        File destination = new File(destFile);
        try {
            FileUtils.moveFile(source, destination);
        } catch (IOException e) {
            e.printStackTrace();
        }
        Log.d(TAG, "end moveFileToDestination");
    }

    /*
    Update local Database with values returned from the server after the upload
     */
    public void updateUploadedNessagesDatabase(int id, int backendid, long send, long read, int msgid, String MessageType, String Message) {
        ContentValues valuesins = new ContentValues();
        valuesins.put(T_MESSAGES_BADBID, backendid);
        valuesins.put(T_MESSAGES_SendTimestamp, send);
        valuesins.put(T_MESSAGES_ReadTimestamp, read);
        if (MessageType.equalsIgnoreCase(TYP_TEXT)) {
            valuesins.put(T_MESSAGES_TextMsgID, msgid);
        } else if (MessageType.equalsIgnoreCase(TYP_IMAGE)) {
            valuesins.put(T_MESSAGES_ImageMsgID, msgid);
            valuesins.put(T_MESSAGES_ImageMsgValue, Message);
        } else if (MessageType.equalsIgnoreCase(TYP_LOCATION)) {
            valuesins.put(T_MESSAGES_LocationMsgID, msgid);
            valuesins.put(T_MESSAGES_LocationMsgValue, Message);
        } else if (MessageType.equalsIgnoreCase(TYP_CONTACT)) {
            valuesins.put(T_MESSAGES_ContactMsgID, msgid);
            valuesins.put(T_MESSAGES_ContactMsgValue, Message);
        } else if (MessageType.equalsIgnoreCase(TYP_FILE)) {
            valuesins.put(T_MESSAGES_FileMsgID, msgid);
            valuesins.put(T_MESSAGES_FileMsgValue, Message);
        } else if (MessageType.equalsIgnoreCase(TYP_VIDEO)) {
            valuesins.put(T_MESSAGES_VideoMsgID, msgid);
            valuesins.put(T_MESSAGES_VideoMsgValue, Message);
        }
        ContentProviderClient client = mContentResolver.acquireContentProviderClient(FrinmeanContentProvider.MESSAES_CONTENT_URI);
        client.getLocalContentProvider().update(FrinmeanContentProvider.MESSAES_CONTENT_URI, valuesins, T_MESSAGES_ID + " = ?", new String[]{String.valueOf(id)});
        client.release();
    }

    /*
    Upload the Messages without a Backend ID
     */
    private void uploadUnsavedMessages() {

        ContentProviderClient client = mContentResolver.acquireContentProviderClient(FrinmeanContentProvider.MESSAES_CONTENT_URI);
        Cursor c = client.getLocalContentProvider().query(FrinmeanContentProvider.MESSAES_CONTENT_URI, MESSAGES_DB_Columns, T_MESSAGES_BADBID + " = ?", new String[]{"0"}, null);

        while (c.moveToNext()) {

            String msgtype = c.getString(ID_MESSAGES_MessageType);

            if (msgtype.equalsIgnoreCase(TYP_TEXT)) {
                OSTeM outtxt = rf.sendtextmessage(username, password, c.getString(ID_MESSAGES_TextMsgValue));
                if (outtxt != null) {
                    if (outtxt.getET() == null || outtxt.getET().isEmpty()) {
                        OIMIC outins = rf.insertmessageintochat(username, password, c.getInt(ID_MESSAGES_ChatID), outtxt.getTID(), TYP_TEXT);
                        if (outins != null) {
                            if (outins.getET() == null || outins.getET().isEmpty()) {
                                updateUploadedNessagesDatabase(c.getInt(ID_MESSAGES__id), outins.getMID(), outins.getSdT(), outins.getSdT(), outtxt.getTID(), TYP_TEXT, null);
                            }
                        }
                    }
                }
            } else if (msgtype.equalsIgnoreCase(TYP_IMAGE)) {
                if (isWifi) {
                    String imgfile = directory;
                    if (imgfile.endsWith(File.separator)) {
                        imgfile += Constants.IMAGEDIR + File.separator + c.getString(Constants.ID_MESSAGES_ImageMsgValue);
                    } else {
                        imgfile += File.separator + Constants.IMAGEDIR + File.separator + c.getString(Constants.ID_MESSAGES_ImageMsgValue);
                    }
                    OSImM outimg = rf.sendImageMessage(username, password, imgfile);
                    if (outimg != null) {
                        if (outimg.getET() == null || outimg.getET().isEmpty()) {
                            OIMIC outins = rf.insertmessageintochat(username, password, c.getInt(ID_MESSAGES_ChatID), outimg.getIID(), TYP_IMAGE);
                            if (outins != null) {
                                if (outins.getET() == null || outins.getET().isEmpty()) {
                                    updateUploadedNessagesDatabase(c.getInt(ID_MESSAGES__id), outins.getMID(), outins.getSdT(), outins.getSdT(), outimg.getIID(), TYP_IMAGE, outimg.getIF());
                                    moveFileToDestination(imgfile, Constants.IMAGEDIR, outimg.getIF());
                                }
                            }
                        }

                    }
                }
            } /*else if (msgtype.equalsIgnoreCase(TYP_LOCATION)) {

            } */ else if (msgtype.equalsIgnoreCase(TYP_VIDEO)) {
                if (isWifi) {
                    String vidfile = directory;
                    if (vidfile.endsWith(File.separator)) {
                        vidfile += Constants.VIDEODIR + File.separator + c.getString(Constants.ID_MESSAGES_VideoMsgValue);
                    } else {
                        vidfile += File.separator + Constants.VIDEODIR + File.separator + c.getString(Constants.ID_MESSAGES_VideoMsgValue);
                    }
                    OSViM outvid = rf.sendVideoMessage(username, password, vidfile);
                    if (outvid != null) {
                        if (outvid.getET() == null || outvid.getET().isEmpty()) {
                            OIMIC outins = rf.insertmessageintochat(username, password, c.getInt(ID_MESSAGES_ChatID), outvid.getVID(), TYP_VIDEO);
                            if (outins != null) {
                                if (outins.getET() == null || outins.getET().isEmpty()) {
                                    updateUploadedNessagesDatabase(c.getInt(ID_MESSAGES__id), outins.getMID(), outins.getSdT(), outins.getSdT(), outvid.getVID(), TYP_VIDEO, outvid.getVF());
                                    moveFileToDestination(vidfile, Constants.VIDEODIR, outvid.getVF());
                                }
                            }
                        }

                    }
                }
            } /* else if (msgtype.equalsIgnoreCase(TYP_FILE)) {

            } else if (msgtype.equalsIgnoreCase(TYP_CONTACT)) {

            } */
        }
        c.close();
        client.release();
    }


    /*
    Get the Message Information for the Messages
     */
    private void syncGetMessageInformation() {

        /* This is to identify changes in the status, if a message is totally downloaded and shown nothing can change anymore */
        String selectdifferent = "((" + Constants.T_MESSAGES_NumberAll + " = ?) OR (" + Constants.T_MESSAGES_NumberAll + " != " + Constants.T_MESSAGES_NumberRead + ") OR  ("
                + Constants.T_MESSAGES_NumberAll + " != " + Constants.T_MESSAGES_NumberShow + "))";

        ContentProviderClient clientdifferent = mContentResolver.acquireContentProviderClient(FrinmeanContentProvider.MESSAES_CONTENT_URI);
        Cursor cd = clientdifferent.getLocalContentProvider().query(FrinmeanContentProvider.MESSAES_CONTENT_URI, MESSAGES_DB_Columns, selectdifferent, new String[]{"0"}, null);

        while (cd.moveToNext()) {
            OGMI outgmi = rf.getmessageinformation(username, password, cd.getInt(Constants.ID_MESSAGES_BADBID));
            if (outgmi != null) {
                if (outgmi.getET() == null || outgmi.getET().isEmpty()) {
                    if (outgmi.getNT() != cd.getInt(Constants.ID_MESSAGES_NumberAll) ||
                            (outgmi.getNR() != cd.getInt(Constants.ID_MESSAGES_NumberRead)) ||
                            (outgmi.getNS() != cd.getInt(Constants.ID_MESSAGES_NumberShow))) {
                        ContentValues valuesins = new ContentValues();
                        valuesins.put(Constants.T_MESSAGES_BADBID, outgmi.getMID());
                        valuesins.put(Constants.T_MESSAGES_NumberAll, outgmi.getNT());
                        valuesins.put(Constants.T_MESSAGES_NumberRead, outgmi.getNR());
                        valuesins.put(Constants.T_MESSAGES_NumberShow, outgmi.getNS());
                        ((FrinmeanContentProvider) clientdifferent.getLocalContentProvider()).insertorupdate(FrinmeanContentProvider.MESSAES_CONTENT_URI, valuesins);
                    }
                }
            }
        }
        cd.close();
        clientdifferent.release();
    }
}

