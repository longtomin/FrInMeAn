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
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.util.Log;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import de.radiohacks.frinmean.Constants;
import de.radiohacks.frinmean.R;
import de.radiohacks.frinmean.SingleChatActivity;
import de.radiohacks.frinmean.modelshort.C;
import de.radiohacks.frinmean.modelshort.CNM;
import de.radiohacks.frinmean.modelshort.M;
import de.radiohacks.frinmean.modelshort.OCN;
import de.radiohacks.frinmean.modelshort.OFMFC;
import de.radiohacks.frinmean.modelshort.OGMI;
import de.radiohacks.frinmean.modelshort.OIMIC;
import de.radiohacks.frinmean.modelshort.OSImM;
import de.radiohacks.frinmean.modelshort.OSTeM;
import de.radiohacks.frinmean.modelshort.OSU;
import de.radiohacks.frinmean.modelshort.OSViM;
import de.radiohacks.frinmean.providers.DBHelper;
import de.radiohacks.frinmean.providers.FrinmeanContentProvider;
import de.radiohacks.frinmean.service.CustomExceptionHandler;
import de.radiohacks.frinmean.service.RestFunctions;

import static de.radiohacks.frinmean.Constants.CHAT_DB_Columns;
import static de.radiohacks.frinmean.Constants.ID_MESSAGES_ChatID;
import static de.radiohacks.frinmean.Constants.ID_MESSAGES_MessageType;
import static de.radiohacks.frinmean.Constants.ID_MESSAGES_TextMsgValue;
import static de.radiohacks.frinmean.Constants.ID_MESSAGES__id;
import static de.radiohacks.frinmean.Constants.MESSAGES_DB_Columns;
import static de.radiohacks.frinmean.Constants.MESSAGES_TIME_DB_Columns;
import static de.radiohacks.frinmean.Constants.TYP_CONTACT;
import static de.radiohacks.frinmean.Constants.TYP_FILE;
import static de.radiohacks.frinmean.Constants.TYP_ICON;
import static de.radiohacks.frinmean.Constants.TYP_IMAGE;
import static de.radiohacks.frinmean.Constants.TYP_LOCATION;
import static de.radiohacks.frinmean.Constants.TYP_TEXT;
import static de.radiohacks.frinmean.Constants.TYP_VIDEO;
import static de.radiohacks.frinmean.Constants.T_CHAT_BADBID;
import static de.radiohacks.frinmean.Constants.T_CHAT_ChatName;
import static de.radiohacks.frinmean.Constants.T_CHAT_OwningUserID;
import static de.radiohacks.frinmean.Constants.T_CHAT_OwningUserName;
import static de.radiohacks.frinmean.Constants.T_MESSAGES_BADBID;
import static de.radiohacks.frinmean.Constants.T_MESSAGES_ContactMsgID;
import static de.radiohacks.frinmean.Constants.T_MESSAGES_ContactMsgValue;
import static de.radiohacks.frinmean.Constants.T_MESSAGES_FileMsgID;
import static de.radiohacks.frinmean.Constants.T_MESSAGES_FileMsgValue;
import static de.radiohacks.frinmean.Constants.T_MESSAGES_ID;
import static de.radiohacks.frinmean.Constants.T_MESSAGES_ImageMsgID;
import static de.radiohacks.frinmean.Constants.T_MESSAGES_ImageMsgValue;
import static de.radiohacks.frinmean.Constants.T_MESSAGES_LocationMsgID;
import static de.radiohacks.frinmean.Constants.T_MESSAGES_LocationMsgValue;
import static de.radiohacks.frinmean.Constants.T_MESSAGES_ReadTimestamp;
import static de.radiohacks.frinmean.Constants.T_MESSAGES_SendTimestamp;
import static de.radiohacks.frinmean.Constants.T_MESSAGES_TIME_BADBID;
import static de.radiohacks.frinmean.Constants.T_MESSAGES_TIME_ReadTimestamp;
import static de.radiohacks.frinmean.Constants.T_MESSAGES_TIME_SendTimestamp;
import static de.radiohacks.frinmean.Constants.T_MESSAGES_TIME_ShowTimestamp;
import static de.radiohacks.frinmean.Constants.T_MESSAGES_TIME_UserID;
import static de.radiohacks.frinmean.Constants.T_MESSAGES_TIME_UserName;
import static de.radiohacks.frinmean.Constants.T_MESSAGES_TextMsgID;
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
    private boolean contentall;
    private boolean timesync;
    private int userid;
    private String ringtone;
    private boolean vibrate;
    private RestFunctions rf;
    private Context mContext;
    private String basedir;
    private String imgdir;
    private String viddir;
    private String fildir;
    private String icndir;
    private DBHelper dbh;

    /**
     * Constructor. Obtains handle to content resolver for later use.
     */
    public SyncAdapter(Context context, boolean autoInitialize) {
        super(context, autoInitialize);
        mContext = context;
        mContentResolver = mContext.getContentResolver();
        getPreferenceInfo();
        rf = new RestFunctions();
        dbh = new DBHelper(context);
        basedir = Environment.getExternalStorageDirectory().getAbsolutePath() + File.separator + Constants.BASEDIR;
        File baseFile = new File(basedir);
        if (!baseFile.exists()) {
            if (!baseFile.mkdirs()) {
                Log.e(TAG, "Base Directory creation failed");
            }
        }
        if (!(Thread.getDefaultUncaughtExceptionHandler() instanceof CustomExceptionHandler)) {
            Thread.setDefaultUncaughtExceptionHandler(new CustomExceptionHandler(baseFile.toString()));
        }
        imgdir = basedir + File.separator + Constants.IMAGEDIR;
        File imgFile = new File(imgdir);
        if (!imgFile.exists()) {
            if (!imgFile.mkdirs()) {
                Log.e(TAG, "Image Directory creation failed");
            }
        }
        viddir = basedir + File.separator + Constants.VIDEODIR;
        File vidFile = new File(viddir);
        if (!vidFile.exists()) {
            if (!vidFile.mkdirs()) {
                Log.e(TAG, "Video Directory creation failed");
            }
        }
        fildir = basedir + File.separator + Constants.FILESDIR;
        File filFile = new File(fildir);
        if (!filFile.exists()) {
            if (!filFile.mkdirs()) {
                Log.e(TAG, "File Directory creation failed");
            }
        }
        icndir = basedir + File.separator + Constants.ICONDIR;
        File icnFile = new File(icndir);
        if (!icnFile.exists()) {
            if (!icnFile.mkdirs()) {
                Log.e(TAG, "Icon Directory creation failed");
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
        String basedir = Environment.getExternalStorageDirectory().getAbsolutePath() + File.separator + Constants.BASEDIR;
        File baseFile = new File(basedir);
        if (!baseFile.exists()) {
            if (!baseFile.mkdirs()) {
                Log.e(TAG, "Base Directory creation failed");
            }
        }
        if (!(Thread.getDefaultUncaughtExceptionHandler() instanceof CustomExceptionHandler)) {
            Thread.setDefaultUncaughtExceptionHandler(new CustomExceptionHandler(baseFile.toString()));
        }

        /*        if (directory.equalsIgnoreCase("NULL")) {
            if (!(Thread.getDefaultUncaughtExceptionHandler() instanceof CustomExceptionHandler)) {
                Thread.setDefaultUncaughtExceptionHandler(new CustomExceptionHandler(Environment.getExternalStorageDirectory().toString()));
            }
        } else {
            if (!(Thread.getDefaultUncaughtExceptionHandler() instanceof CustomExceptionHandler)) {
                Thread.setDefaultUncaughtExceptionHandler(new CustomExceptionHandler(directory));
            }
        } */
    }

    protected void getPreferenceInfo() {
        Log.d(TAG, "start getPreferenceInfo");
        SharedPreferences sharedPrefs = PreferenceManager
                .getDefaultSharedPreferences(getContext());

        this.username = sharedPrefs.getString(Constants.PrefUsername, "NULL");
        this.password = sharedPrefs.getString(Constants.PrefPassword, "NULL");
//        this.directory = sharedPrefs.getString(Constants.PrefDirectory, "NULL");
        this.userid = sharedPrefs.getInt(Constants.PrefUserID, -1);
        this.ringtone = sharedPrefs.getString(Constants.prefRingtone, "DEFAULT_SOUND");
        this.vibrate = sharedPrefs.getBoolean(Constants.prefVibrate, true);
        this.contentall = sharedPrefs.getBoolean(Constants.prefContentCommunication, false);
        this.timesync = sharedPrefs.getBoolean(Constants.prefContentCommunication, false);
        Log.d(TAG, "end getPferefenceInfo");
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
        Log.d(TAG, "Beginning network synchronization");
        getPreferenceInfo();
        if (dbh.checkNetwork()) {
            syncCheckNewMessages();
            uploadUnsavedMessages();
            syncGetMessageInformation();
            syncUser();
        }
        Log.d(TAG, "Network synchronization complete");
    }

    private void syncCheckNewMessages() {
        Log.d(TAG, "start syncCheckNewMessages");

        OCN outcheck = rf.checknew();

        if (outcheck != null) {
            if (outcheck.getET() == null || outcheck.getET().isEmpty()) {
                if (outcheck.getC() != null && outcheck.getC().size() > 0) {
                    SaveChatsToLDB(outcheck.getC());
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


        OFMFC res = rf.getmessagefromchat(cid, readtime);

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

    private void SaveChatsToLDB(List<C> in) {
        Log.d(TAG, "start SaveChatsToLDB");
        for (int j = 0; j < in.size(); j++) {
            C c = in.get(j);
            ContentValues valuesins = new ContentValues();
            valuesins.put(T_CHAT_BADBID, c.getCID());
            valuesins.put(T_CHAT_OwningUserID, c.getOU().getOUID());
            valuesins.put(T_CHAT_OwningUserName, c.getOU().getOUN());
            if (c.getICID() > 0) {
                String filepath = dbh.downloadimage(c.getICID(), 0, TYP_ICON);
                if (filepath != null && !filepath.isEmpty()) {
                    valuesins.put(Constants.T_CHAT_IconID, c.getICID());
                    valuesins.put(Constants.T_CHAT_IconValue, filepath);
                }
            }
            valuesins.put(T_CHAT_ChatName, c.getCN());
            if (dbh.acknowledgeChat(c.getCN(), c.getCID())) {
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

        if (dbh.SaveMessagetoDB(in, ChatID)) {
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
                nb.setLights(1, 200, 100);
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
        }
        Log.d(TAG, "end saveMessageToLDB");
    }

    /*
    Update local Database with values returned from the server after the upload
     */
    public void updateUploadedMessagesDatabase(int id, int backendid, long send, long read, int msgid, String MessageType, String Message) {
        ContentValues valuesins = new ContentValues();
        valuesins.put(T_MESSAGES_BADBID, backendid);
        valuesins.put(T_MESSAGES_SendTimestamp, send);
        valuesins.put(T_MESSAGES_ReadTimestamp, read);
        if (MessageType.equalsIgnoreCase(TYP_TEXT)) {
            valuesins.put(T_MESSAGES_TextMsgID, msgid);
        } else if (MessageType.equalsIgnoreCase(TYP_IMAGE)) {
            valuesins.put(T_MESSAGES_ImageMsgID, msgid);
            String path = imgdir + File.separator + Message;
            valuesins.put(T_MESSAGES_ImageMsgValue, path);
        } else if (MessageType.equalsIgnoreCase(TYP_LOCATION)) {
            valuesins.put(T_MESSAGES_LocationMsgID, msgid);
            valuesins.put(T_MESSAGES_LocationMsgValue, Message);
        } else if (MessageType.equalsIgnoreCase(TYP_CONTACT)) {
            valuesins.put(T_MESSAGES_ContactMsgID, msgid);
            valuesins.put(T_MESSAGES_ContactMsgValue, Message);
        } else if (MessageType.equalsIgnoreCase(TYP_FILE)) {
            valuesins.put(T_MESSAGES_FileMsgID, msgid);
            String path = fildir + File.separator + Message;
            valuesins.put(T_MESSAGES_FileMsgValue, path);
        } else if (MessageType.equalsIgnoreCase(TYP_VIDEO)) {
            valuesins.put(T_MESSAGES_VideoMsgID, msgid);
            String path = viddir + File.separator + Message;
            valuesins.put(T_MESSAGES_VideoMsgValue, path);
        }
        ContentProviderClient client = mContentResolver.acquireContentProviderClient(FrinmeanContentProvider.MESSAGES_CONTENT_URI);
        client.getLocalContentProvider().update(FrinmeanContentProvider.MESSAGES_CONTENT_URI, valuesins, T_MESSAGES_ID + " = ?", new String[]{String.valueOf(id)});
        client.release();
    }

    /*
    Upload the Messages without a Backend ID
     */
    private void uploadUnsavedMessages() {

        ContentProviderClient client = mContentResolver.acquireContentProviderClient(FrinmeanContentProvider.MESSAGES_CONTENT_URI);
        Cursor c = client.getLocalContentProvider().query(FrinmeanContentProvider.MESSAGES_CONTENT_URI, MESSAGES_DB_Columns, T_MESSAGES_BADBID + " = ?", new String[]{"0"}, null);

        while (c.moveToNext()) {

            String msgtype = c.getString(ID_MESSAGES_MessageType);

            if (msgtype.equalsIgnoreCase(TYP_TEXT)) {
                OSTeM outtxt = rf.sendtextmessage(c.getString(ID_MESSAGES_TextMsgValue));
                if (outtxt != null) {
                    if (outtxt.getET() == null || outtxt.getET().isEmpty()) {
                        OIMIC outins = rf.insertmessageintochat(c.getInt(ID_MESSAGES_ChatID), outtxt.getTID(), TYP_TEXT);
                        if (outins != null) {
                            if (outins.getET() == null || outins.getET().isEmpty()) {
                                updateUploadedMessagesDatabase(c.getInt(ID_MESSAGES__id), outins.getMID(), outins.getSdT(), outins.getSdT(), outtxt.getTID(), TYP_TEXT, null);
                                dbh.inserIntoTimeTable(outins.getMID(), userid);
                            }
                        }
                    }
                }
            } else if (msgtype.equalsIgnoreCase(TYP_IMAGE)) {
                if ((!contentall && dbh.checkWIFI()) || (contentall && dbh.checkNetwork())) {
                    String imgfile = c.getString(Constants.ID_MESSAGES_ImageMsgValue);
                    OSImM outimg = rf.sendImageMessage(c.getString(Constants.ID_MESSAGES_ImageMsgValue));
                    if (outimg != null) {
                        if (outimg.getET() == null || outimg.getET().isEmpty()) {
                            OIMIC outins = rf.insertmessageintochat(c.getInt(ID_MESSAGES_ChatID), outimg.getImID(), TYP_IMAGE);
                            if (outins != null) {
                                if (outins.getET() == null || outins.getET().isEmpty()) {
                                    updateUploadedMessagesDatabase(c.getInt(ID_MESSAGES__id), outins.getMID(), outins.getSdT(), outins.getSdT(), outimg.getImID(), TYP_IMAGE, outimg.getImF());
                                    dbh.inserIntoTimeTable(outins.getMID(), userid);
                                    dbh.moveFileToDestination(imgfile, Constants.IMAGEDIR, outimg.getImF());
                                }
                            }
                        }
                    }
                }
            } else if (msgtype.equalsIgnoreCase(TYP_LOCATION)) {

            } else if (msgtype.equalsIgnoreCase(TYP_VIDEO)) {
                if ((!contentall && dbh.checkWIFI()) || (contentall && dbh.checkNetwork())) {
                    String vidfile = c.getString(Constants.ID_MESSAGES_VideoMsgValue);
                    OSViM outvid = rf.sendVideoMessage(c.getString(Constants.ID_MESSAGES_VideoMsgValue));
                    if (outvid != null) {
                        if (outvid.getET() == null || outvid.getET().isEmpty()) {
                            OIMIC outins = rf.insertmessageintochat(c.getInt(ID_MESSAGES_ChatID), outvid.getVID(), TYP_VIDEO);
                            if (outins != null) {
                                if (outins.getET() == null || outins.getET().isEmpty()) {
                                    updateUploadedMessagesDatabase(c.getInt(ID_MESSAGES__id), outins.getMID(), outins.getSdT(), outins.getSdT(), outvid.getVID(), TYP_VIDEO, outvid.getVF());
                                    dbh.inserIntoTimeTable(outins.getMID(), userid);
                                    dbh.moveFileToDestination(vidfile, Constants.VIDEODIR, outvid.getVF());
                                }
                            }
                        }

                    }
                }
            } else if (msgtype.equalsIgnoreCase(TYP_FILE)) {

            } else if (msgtype.equalsIgnoreCase(TYP_CONTACT)) {

            }
        }
        c.close();
        client.release();
    }


    /*
    Get the Message Information for the Messages
     */
    private void syncGetMessageInformation() {

        if ((!timesync && dbh.checkWIFI()) || (timesync && dbh.checkNetwork())) {
            ContentProviderClient clientdifferent = mContentResolver.acquireContentProviderClient(FrinmeanContentProvider.MESSAGES_TIME_CONTENT_URI);
            Cursor cd = clientdifferent.getLocalContentProvider().query(FrinmeanContentProvider.MESSAGES_TIME_CONTENT_URI, MESSAGES_TIME_DB_Columns,
                    Constants.T_MESSAGES_TIME_ReadTimestamp + " = ? OR " + Constants.T_MESSAGES_TIME_ShowTimestamp + " = ?", new String[]{"0", "0"}, null);

            // ToDo beim Senden und Empfangen muss ein Eintrag für die eigene oder fremde Nachricht hinterlegt werden.
            ArrayList<Integer> inputrf = new ArrayList<Integer>(1);
            int count = 0;
            while (cd.moveToNext()) {
                int val = cd.getInt(Constants.ID_MESSAGES_BADBID);
                if (!inputrf.contains(val)) {
                    inputrf.add(val);
                    count++;
                    if (count == 100) {
                        OGMI outgmi = rf.getmessageinformation(inputrf);
                        if (outgmi != null) {
                            if (outgmi.getET() == null || outgmi.getET().isEmpty()) {
                                for (int i = 0; i < outgmi.getMIB().size(); i++) {
                                    // int numall = outgmi.getMIB().get(i).getMI().size();
                                    // int numre = 0;
                                    // int numsh = 0;
                                    for (int j = 0; j < outgmi.getMIB().get(i).getMI().size(); j++) {
                                        ContentValues valuesins = new ContentValues();
                                        valuesins.put(T_MESSAGES_TIME_BADBID, outgmi.getMIB().get(i).getMID());
                                        valuesins.put(T_MESSAGES_TIME_UserID, outgmi.getMIB().get(i).getMI().get(j).getUID());
                                        valuesins.put(T_MESSAGES_TIME_ReadTimestamp, outgmi.getMIB().get(i).getMI().get(j).getRD());
                                        valuesins.put(T_MESSAGES_TIME_SendTimestamp, outgmi.getMIB().get(i).getSD());
                                        valuesins.put(T_MESSAGES_TIME_ShowTimestamp, outgmi.getMIB().get(i).getMI().get(j).getSH());
                                        valuesins.put(T_MESSAGES_TIME_UserName, outgmi.getMIB().get(i).getMI().get(j).getUN());

                                        ContentProviderClient clientupd = mContentResolver.acquireContentProviderClient(FrinmeanContentProvider.MESSAGES_TIME_CONTENT_URI);
                                        ((FrinmeanContentProvider) clientupd.getLocalContentProvider()).insertorupdate(FrinmeanContentProvider.MESSAGES_TIME_CONTENT_URI, valuesins);
                                        clientupd.release();

                                    /* if (outgmi.getMIB().get(i).getMI().get(j).getRD() != 0) {
                                        numre++;
                                    }
                                    if (outgmi.getMIB().get(i).getMI().get(j).getSH() != 0) {
                                        numsh++;
                                    } */
                                    }
                                }
                            }
                        }
                        // cd.close();
                        // clientdifferent.release();
                        count = 0;
                        inputrf.clear();
                    }
                }
            }
            if (inputrf.size() > 0) {
                OGMI outgmi = rf.getmessageinformation(inputrf);
                if (outgmi != null) {
                    if (outgmi.getET() == null || outgmi.getET().isEmpty()) {
                        for (int i = 0; i < outgmi.getMIB().size(); i++) {
                            // int numall = outgmi.getMIB().get(i).getMI().size();
                            // int numre = 0;
                            // int numsh = 0;
                            for (int j = 0; j < outgmi.getMIB().get(i).getMI().size(); j++) {
                                ContentValues valuesins = new ContentValues();
                                valuesins.put(T_MESSAGES_TIME_BADBID, outgmi.getMIB().get(i).getMID());
                                valuesins.put(T_MESSAGES_TIME_UserID, outgmi.getMIB().get(i).getMI().get(j).getUID());
                                valuesins.put(T_MESSAGES_TIME_ReadTimestamp, outgmi.getMIB().get(i).getMI().get(j).getRD());
                                valuesins.put(T_MESSAGES_TIME_SendTimestamp, outgmi.getMIB().get(i).getSD());
                                valuesins.put(T_MESSAGES_TIME_ShowTimestamp, outgmi.getMIB().get(i).getMI().get(j).getSH());
                                valuesins.put(T_MESSAGES_TIME_UserName, outgmi.getMIB().get(i).getMI().get(j).getUN());

                                ContentProviderClient clientupd = mContentResolver.acquireContentProviderClient(FrinmeanContentProvider.MESSAGES_TIME_CONTENT_URI);
                                ((FrinmeanContentProvider) clientupd.getLocalContentProvider()).insertorupdate(FrinmeanContentProvider.MESSAGES_TIME_CONTENT_URI, valuesins);
                                clientupd.release();

                            /* if (outgmi.getMIB().get(i).getMI().get(j).getRD() != 0) {
                                numre++;
                            }
                            if (outgmi.getMIB().get(i).getMI().get(j).getSH() != 0) {
                                numsh++;
                            }*/
                            }
                        }
                    }
                }
                cd.close();
                clientdifferent.release();
            }
        }
    }

    private void syncUser() {
        Log.d(TAG, "start hanleActionSyncUser");

        ContentProviderClient client = mContentResolver.acquireContentProviderClient(FrinmeanContentProvider.USER_CONTENT_URI);
        Cursor cur = client.getLocalContentProvider().query(FrinmeanContentProvider.USER_CONTENT_URI, Constants.USER_DB_Columns, null, null, null);
        ArrayList<Integer> inputuids = new ArrayList<>(1);
        while (cur.moveToNext()) {
            int val = cur.getInt(Constants.ID_USER_BADBID);
            if (!inputuids.contains(val)) {
                inputuids.add(val);
            }
        }
        cur.close();
        client.release();

        if (inputuids.size() > 0) {
            OSU out = rf.syncUser(inputuids);
            if (out != null) {
                if (out.getET() == null || out.getET().isEmpty()) {

                    for (int i = 0; i < out.getU().size(); i++) {
                        dbh.insertUserIntoDB(out.getU().get(i).getUID(), out.getU().get(i).getUN(), null, out.getU().get(i).getE(), out.getU().get(i).getLA(), 0, null);
                    }
                }
            }
        }
        Log.d(TAG, "end hanleActionSyncUser");
    }
}