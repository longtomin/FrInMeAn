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

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.ContentProviderClient;
import android.content.ContentResolver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.media.MediaMetadataRetriever;
import android.net.Uri;
import android.preference.PreferenceManager;
import android.support.v4.widget.CursorAdapter;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TableLayout;
import android.widget.TextView;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import de.radiohacks.frinmean.ChatUserActivity;
import de.radiohacks.frinmean.Constants;
import de.radiohacks.frinmean.R;
import de.radiohacks.frinmean.providers.FrinmeanContentProvider;
import de.radiohacks.frinmean.service.MeBaService;

import static de.radiohacks.frinmean.Constants.ID_USER_BADBID;
import static de.radiohacks.frinmean.Constants.ID_USER_IconValue;
import static de.radiohacks.frinmean.Constants.MESSAGES_TIME_DB_Columns;

public class SingleChatAdapter extends CursorAdapter {

    private static final String TAG = SingleChatAdapter.class.getSimpleName();
    private static final int TEXTMSG_OWN = 0;
    private static final int TEXTMSG_FOREIGN = 1;
    private static final int IMAGEMSG_OWN = 2;
    private static final int IMAGEMSG_FOREIGN = 3;
    private static final int FILEMSG_OWN = 4;
    private static final int FILEMSG_FOREIGN = 5;
    private static final int CONTACTMSG_OWN = 6;
    private static final int CONTACTMSG_FOREIGN = 7;
    private static final int LOCATIONMSG_OWN = 8;
    private static final int LOCATIONMSG_FOREIGN = 9;
    private static final int VIDEOMSG_OWN = 10;
    private static final int VIDEOMSG_FOREIGN = 11;

    private int OID = 0;
    private ContentResolver mContentResolver;
    private Context mContext;
    private HashMap<Integer, Bitmap> userImages;

    public SingleChatAdapter(Context context, Cursor cursor, int InOID, int InchatID) {
        super(context, cursor, true);
        Log.d(TAG, "start SingleChatAdapter");
        this.OID = InOID;
        this.mContext = context;
        this.mContentResolver = context.getContentResolver();
        this.userImages = new HashMap<>(1);
        ContentProviderClient clientuserid = mContentResolver.acquireContentProviderClient(FrinmeanContentProvider.MESSAGES_CONTENT_URI);
        Cursor cuid = clientuserid.getLocalContentProvider().query(FrinmeanContentProvider.MESSAGES_CONTENT_URI, new String[]{"Distinct " + Constants.T_MESSAGES_OwningUserID},
                Constants.T_MESSAGES_ChatID + " = ?", new String[]{String.valueOf(InchatID)}, Constants.T_MESSAGES_OwningUserID);
        String sel = "";
        List uids = new ArrayList<String>(1);
        int count = cuid.getCount();
        if (cuid.getCount() > 0) {
            cuid.moveToFirst();
            for (int i = 0; i < count; i++) {
                if (i == count - 1) {
                    sel += Constants.T_USER_BADBID + " = ?";
                } else {
                    sel += Constants.T_USER_BADBID + " = ? OR ";
                }
                uids.add(String.valueOf(cuid.getInt(0)));
                cuid.moveToNext();
            }
        }
        cuid.close();
        clientuserid.release();

        String[] uids2 = (String[]) uids.toArray(new String[uids.size()]);
        ContentProviderClient clientuserimg = mContentResolver.acquireContentProviderClient(FrinmeanContentProvider.USER_CONTENT_URI);
        Cursor cuim = clientuserimg.getLocalContentProvider().query(FrinmeanContentProvider.USER_CONTENT_URI, Constants.USER_DB_Columns,
                sel, uids2, null);

        while (cuim.moveToNext()) {
            String UserImgFile = cuim.getString(ID_USER_IconValue);
            int UserId = cuim.getInt(ID_USER_BADBID);
            if (UserImgFile != null && !UserImgFile.isEmpty()) {
                File ifileOwn = new File(UserImgFile);
                if (ifileOwn.exists()) {
                    String filename = ifileOwn.getAbsolutePath();
                    BitmapFactory.Options options = new BitmapFactory.Options();
                    options.inJustDecodeBounds = true;
                    BitmapFactory.decodeFile(UserImgFile, options);

                    options.inSampleSize = calculateInSampleSize(options, 50, 50);

                    // Decode bitmap with inSampleSize set
                    options.inJustDecodeBounds = false;
                    Bitmap bmp = BitmapFactory.decodeFile(filename, options);
                    userImages.put(UserId, bmp);
                }
            } else {
                userImages.put(UserId, BitmapFactory.decodeResource(mContext.getResources(), R.drawable.iconuser));
            }
        }
        SharedPreferences sharedPrefs = PreferenceManager
                .getDefaultSharedPreferences(mContext);

        String ownusericon = sharedPrefs.getString(Constants.prefUserIcon, "NULL");
        if (ownusericon != "NULL") {
            File ifileOwn = new File(ownusericon);
            if (ifileOwn.exists()) {
                String filename = ifileOwn.getAbsolutePath();
                BitmapFactory.Options options = new BitmapFactory.Options();
                options.inJustDecodeBounds = true;
                BitmapFactory.decodeFile(ownusericon, options);

                options.inSampleSize = calculateInSampleSize(options, 50, 50);

                // Decode bitmap with inSampleSize set
                options.inJustDecodeBounds = false;
                Bitmap bmp = BitmapFactory.decodeFile(filename, options);
                userImages.put(InOID, bmp);
            }
        } else {
            userImages.put(InOID, BitmapFactory.decodeResource(mContext.getResources(), R.drawable.iconuser));
        }

        Log.d(TAG, "end SingleChatAdapter");
    }

    private static int calculateInSampleSize(
            BitmapFactory.Options options, int reqWidth, int reqHeight) {
        // Raw height and width of image
        final int height = options.outHeight;
        final int width = options.outWidth;
        int inSampleSize = 1;

        if (height > reqHeight || width > reqWidth) {

            final int halfHeight = height / 2;
            final int halfWidth = width / 2;

            // Calculate the largest inSampleSize value that is a power of 2 and keeps both
            // height and width larger than the requested height and width.
            while ((halfHeight / inSampleSize) > reqHeight
                    && (halfWidth / inSampleSize) > reqWidth) {
                inSampleSize *= 2;
            }
        }

        return inSampleSize;
    }

    private int findMsgType(String in, boolean inmine) {
        Log.d(TAG, "start findMsgType");
        int ret = -1;
        if (in.equalsIgnoreCase(Constants.TYP_TEXT) && inmine) {
            ret = TEXTMSG_OWN;
        } else if (in.equalsIgnoreCase(Constants.TYP_TEXT) && !inmine) {
            ret = TEXTMSG_FOREIGN;
        } else if (in.equalsIgnoreCase(Constants.TYP_IMAGE) && inmine) {
            ret = IMAGEMSG_OWN;
        } else if (in.equalsIgnoreCase(Constants.TYP_IMAGE) && !inmine) {
            ret = IMAGEMSG_FOREIGN;
        } else if (in.equalsIgnoreCase(Constants.TYP_FILE) && inmine) {
            ret = FILEMSG_OWN;
        } else if (in.equalsIgnoreCase(Constants.TYP_FILE) && !inmine) {
            ret = FILEMSG_FOREIGN;
        } else if (in.equalsIgnoreCase(Constants.TYP_CONTACT) && inmine) {
            ret = CONTACTMSG_OWN;
        } else if (in.equalsIgnoreCase(Constants.TYP_CONTACT) && !inmine) {
            ret = CONTACTMSG_FOREIGN;
        } else if (in.equalsIgnoreCase(Constants.TYP_LOCATION) && inmine) {
            ret = LOCATIONMSG_OWN;
        } else if (in.equalsIgnoreCase(Constants.TYP_LOCATION) && !inmine) {
            ret = LOCATIONMSG_FOREIGN;
        } else if (in.equalsIgnoreCase(Constants.TYP_VIDEO) && inmine) {
            ret = VIDEOMSG_OWN;
        } else if (in.equalsIgnoreCase(Constants.TYP_VIDEO) && !inmine) {
            ret = VIDEOMSG_FOREIGN;
        }
        Log.d(TAG, "end findMsgType");
        return ret;
    }

    @Override
    public int getViewTypeCount() {
        Log.d(TAG, "start & end getViewTypeCount");
        return 12;
    }

    @Override
    public int getItemViewType(int position) {
        Log.d(TAG, "start getItemViewType");
        Cursor cursor = getCursor();
        if (cursor.moveToPosition(position)) {
            String msgOID = cursor.getString(Constants.ID_MESSAGES_OwningUserID);
            boolean mine = msgOID.equalsIgnoreCase(String.valueOf(OID));
            Integer ret = findMsgType(cursor.getString(Constants.ID_MESSAGES_MessageType), mine);
            Log.d(TAG, "end getItemViewType " + ret.toString());
            return ret;
        } else {
            Log.d(TAG, "start getItemViewType -1");
            return -1;
        }
    }

    @Override
    public View newView(final Context context, Cursor cur, ViewGroup parent) {
        Log.d(TAG, "start newView");
        View ret = null;

        final String msgType = cur.getString(Constants.ID_MESSAGES_MessageType);
        final int chatID = cur.getInt(Constants.ID_MESSAGES_ChatID);
        final int vid = cur.getInt(Constants.ID_MESSAGES__id);
        final int baid = cur.getInt(Constants.ID_MESSAGES_BADBID);
        String msgOID = cur.getString(Constants.ID_MESSAGES_OwningUserID);

        final boolean mine = msgOID.equalsIgnoreCase(String.valueOf(OID));
        LayoutInflater li = (LayoutInflater) context
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        switch (findMsgType(msgType, mine)) {
            case TEXTMSG_OWN:
                ret = li.inflate(R.layout.rellay_textownmsg, parent, false);
                break;
            case TEXTMSG_FOREIGN:
                ret = li.inflate(R.layout.rellay_textforeignmsg, parent, false);
                break;
            case IMAGEMSG_OWN:
                ret = li.inflate(R.layout.rellay_imageownmsg, parent, false);
                break;
            case IMAGEMSG_FOREIGN:
                ret = li.inflate(R.layout.rellay_imageforeignmsg, parent, false);
                break;
            case VIDEOMSG_OWN:
                ret = li.inflate(R.layout.rellay_videoownmsg, parent, false);
                break;
            case VIDEOMSG_FOREIGN:
                ret = li.inflate(R.layout.rellay_videoforeignmsg, parent, false);
                break;
        }

        if (ret != null) ret.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                AlertDialog.Builder builder = new AlertDialog.Builder(context);
                builder.setTitle(R.string.message_option);
                builder.setItems(R.array.message_options, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        // The 'which' argument contains the index position
                        // of the selected item
                        switch (which) {
                            case 0:
                                // Löschen
                                final CharSequence[] items = {mContext.getText(R.string.delete_on_server), mContext.getText(R.string.delete_local_content)};
                                // arraylist to keep the selected items
                                final ArrayList seletedItems = new ArrayList();

                                AlertDialog.Builder delbuilder = new AlertDialog.Builder(context);
                                delbuilder.setTitle(context.getText(R.string.delete_options));
                                delbuilder.setMultiChoiceItems(items, null,
                                        new DialogInterface.OnMultiChoiceClickListener() {
                                            @Override
                                            public void onClick(DialogInterface dialog, int indexSelected,
                                                                boolean isChecked) {
                                                if (isChecked) {
                                                    // If the user checked the item, add it to the selected items
                                                    seletedItems.add(indexSelected);
                                                } else if (seletedItems.contains(indexSelected)) {
                                                    // Else, if the item is already in the array, remove it
                                                    seletedItems.remove(Integer.valueOf(indexSelected));
                                                }
                                            }
                                        })
                                        // Set the action buttons
                                        .setPositiveButton(context.getText(R.string.delete), new DialogInterface.OnClickListener() {
                                            @Override
                                            public void onClick(DialogInterface dialog, int id) {
                                                Intent delintent = new Intent(context, MeBaService.class);

                                                boolean delserver = false;
                                                boolean delcontent = false;
                                                for (int j = 0; j < seletedItems.size(); j++) {
                                                    CharSequence option = items[j];
                                                    String stmpoption = option.toString();
                                                    if (stmpoption.equalsIgnoreCase(context.getText(R.string.delete_on_server).toString())) {
                                                        delserver = true;
                                                    }
                                                    if (stmpoption.equalsIgnoreCase(context.getText(R.string.delete_local_content).toString())) {
                                                        delcontent = true;
                                                    }
                                                }
                                                delintent.setAction(Constants.ACTION_DELETEMESSAGEFROMCHAT);
                                                delintent.putExtra(Constants.DELETEONSERVER, delserver);
                                                delintent.putExtra(Constants.DELETELOCALCONTENT, delcontent);
                                                delintent.putExtra(Constants.MESSAGEID, vid);
                                                context.startService(delintent);
                                            }
                                        })
                                        .setNegativeButton(context.getText(R.string.cancel), new DialogInterface.OnClickListener() {
                                            @Override
                                            public void onClick(DialogInterface dialog, int id) {
                                                //  Your code when user clicked on Cancel

                                            }
                                        });
                                AlertDialog deldlg = delbuilder.create();
                                deldlg.show();
                                break;
                            case 1:
                                // Weiterleiten
                                Intent startchat = new Intent(context, ChatUserActivity.class);
                                startchat.putExtra(Constants.USERID, OID);
                                startchat.putExtra(Constants.CHAT_ACTIVITY_MODE, Constants.CHAT_ACTIVITY_FORWARD);
                                startchat.putExtra(Constants.SENDCHATID, chatID);
                                startchat.putExtra(Constants.SENDMSGID, vid);
                                context.startActivity(startchat);
                                break;
                            case 2:
                                // Zeit-Informationen
                                Dialog progresDialog = new Dialog(mContext);

                                LayoutInflater inflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

                                progresDialog.setTitle(R.string.timeinformation);
                                progresDialog.setCanceledOnTouchOutside(true);
                                //progresDialog.setContentView(R.layout.time_information);

                                View dialogview = inflater.inflate(R.layout.time_information, null);
                                progresDialog.setContentView(dialogview);
                                TableLayout table_dialog = (TableLayout) dialogview.findViewById(R.id.time_table);
                                table_dialog.setVerticalScrollBarEnabled(true);
                                //TableLayout table_dialog = (TableLayout)progresDialog.findViewById(R.id.time_table);
                                progresDialog.setContentView(dialogview);

//                                ContentProviderClient clientmsg = mContext.getContentResolver().acquireContentProviderClient(FrinmeanContentProvider.MESSAGES_CONTENT_URI);
//                                Cursor mid = clientmsg.getLocalContentProvider().query(FrinmeanContentProvider.MESSAGES_CONTENT_URI, MESSAGES_DB_Columns,
//                                        Constants.T_MESSAGES_ID + " = ?", new String[]{String.valueOf(vid)}, null);
//                                int baenid = mid.getInt(Constants.ID_MESSAGES_BADBID);
//                                mid.close();
//                                clientmsg.release();

                                long send = 0L;

                                ContentProviderClient clientdifferent = mContext.getContentResolver().acquireContentProviderClient(FrinmeanContentProvider.MESSAGES_TIME_CONTENT_URI);
                                Cursor cd = clientdifferent.getLocalContentProvider().query(FrinmeanContentProvider.MESSAGES_TIME_CONTENT_URI, MESSAGES_TIME_DB_Columns,
                                        Constants.T_MESSAGES_TIME_BADBID + " = ?", new String[]{String.valueOf(baid)}, null);

                                while (cd.moveToNext()) {

                                    //TableRow row = new TableRow(mContext);
                                    //row.setLayoutParams(new TableRow.LayoutParams(TableRow.LayoutParams.MATCH_PARENT, TableRow.LayoutParams.WRAP_CONTENT));

                                    TextView username = new TextView(mContext);
                                    username.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));
                                    username.setTextSize(18);
                                    username.setPadding(5, 5, 5, 5);
                                    username.setText(mContext.getResources().getString(R.string.username) + cd.getString(Constants.ID_MESSAGES_TIME_UserName));
                                    table_dialog.addView(username);

                                    TextView readtime = new TextView(mContext);
                                    readtime.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));
                                    readtime.setTextSize(14);
                                    readtime.setPadding(5, 5, 5, 5);
                                    long read = cd.getLong(Constants.ID_MESSAGES_TIME_ReadTimestamp);
                                    if (read == 0) {
                                        readtime.setText(mContext.getResources().getString(R.string.notread));
                                    } else {
                                        Date rDate = new java.util.Date(read * 1000);
                                        readtime.setText(mContext.getResources().getString(R.string.read) + new SimpleDateFormat(Constants.DATETIMEFORMAT).format(rDate));
                                    }
                                    table_dialog.addView(readtime);

                                    TextView showtime = new TextView(mContext);
                                    showtime.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));
                                    showtime.setTextSize(14);
                                    showtime.setPadding(5, 5, 5, 5);
                                    long show = cd.getLong(Constants.ID_MESSAGES_TIME_ShowTimestamp);
                                    if (show == 0) {
                                        showtime.setText(mContext.getResources().getString(R.string.notshow));
                                    } else {
                                        Date sDate = new java.util.Date(show * 1000);
                                        showtime.setText(mContext.getResources().getString(R.string.show) + new SimpleDateFormat(Constants.DATETIMEFORMAT).format(sDate));
                                    }
                                    table_dialog.addView(showtime);

                                    if (send == 0) {
                                        send = cd.getLong(Constants.ID_MESSAGES_TIME_ShowTimestamp);
                                    }
                                }
                                TextView sendtime = new TextView(mContext);
                                sendtime.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));
                                sendtime.setTextSize(16);
                                sendtime.setPadding(5, 5, 5, 5);
                                sendtime.setTextColor(Color.RED);
                                sendtime.setGravity(Gravity.CENTER);
                                Date seDate = new java.util.Date(send * 1000);
                                sendtime.setText(mContext.getResources().getString(R.string.send) + new SimpleDateFormat(Constants.DATETIMEFORMAT).format(seDate));
                                table_dialog.addView(sendtime);
                                cd.close();
                                clientdifferent.release();
                                progresDialog.show();
                                break;
                        }
                    }
                });
                AlertDialog dlg = builder.create();
                dlg.show();
                return false;
            }
        });
        Log.d(TAG, "end newView");
        return ret;
    }

    @SuppressLint("SimpleDateFormat")
    @Override
    public void bindView(View view, Context context, Cursor cur) {
        Log.d(TAG, "start bindView");
        String msgType = cur.getString(Constants.ID_MESSAGES_MessageType);
        int msgOID = cur.getInt(Constants.ID_MESSAGES_OwningUserID);
        boolean mine = msgOID == OID;
        long sstamp = cur.getLong(Constants.ID_MESSAGES_SendTimeStamp);
        Date sDate = new java.util.Date(sstamp * 1000);
        long rstamp = cur.getLong(Constants.ID_MESSAGES_ReadTimeStamp);
        Date rDate = new java.util.Date(rstamp * 1000);
        String OName = cur.getString(Constants.ID_MESSAGES_OwningUserName);

        ContentProviderClient clienttime = mContentResolver.acquireContentProviderClient(FrinmeanContentProvider.MESSAGES_TIME_CONTENT_URI);
        Cursor cto = clienttime.getLocalContentProvider().query(FrinmeanContentProvider.MESSAGES_TIME_CONTENT_URI, MESSAGES_TIME_DB_Columns,
                Constants.T_MESSAGES_TIME_BADBID + " = ?", new String[]{String.valueOf(cur.getInt(Constants.ID_MESSAGES_BADBID))}, null);
        int NumTotal = cto.getCount();
        Cursor crd = clienttime.getLocalContentProvider().query(FrinmeanContentProvider.MESSAGES_TIME_CONTENT_URI, MESSAGES_TIME_DB_Columns,
                Constants.T_MESSAGES_TIME_BADBID + " = ? and " + Constants.T_MESSAGES_TIME_ReadTimestamp + " != ?", new String[]{String.valueOf(cur.getInt(Constants.ID_MESSAGES_BADBID)), "0"}, null);
        int NumRead = crd.getCount();
        Cursor csh = clienttime.getLocalContentProvider().query(FrinmeanContentProvider.MESSAGES_TIME_CONTENT_URI, MESSAGES_TIME_DB_Columns,
                Constants.T_MESSAGES_TIME_BADBID + " = ? and " + Constants.T_MESSAGES_TIME_ShowTimestamp + " != ?", new String[]{String.valueOf(cur.getInt(Constants.ID_MESSAGES_BADBID)), "0"}, null);
        int NumShow = csh.getCount();
        cto.close();
        crd.close();
        csh.close();
        clienttime.release();

        switch (findMsgType(msgType, mine)) {
            case TEXTMSG_OWN:
                TextView TxtOwningUserNameOwn = (TextView) view.findViewById(R.id.OwnTextOwningUserName);
                TxtOwningUserNameOwn.setText(OName);
                TextView TxtSendTimeStampOwn = (TextView) view.findViewById(R.id.OwnTextSendTimeStamp);
                TxtSendTimeStampOwn.setText(new SimpleDateFormat(Constants.DATETIMEFORMAT).format(sDate));
                TextView TxtReadTimeStampOwn = (TextView) view.findViewById(R.id.OwnTextReadTimeStamp);
                TxtReadTimeStampOwn.setText(new SimpleDateFormat(Constants.DATETIMEFORMAT).format(rDate));
                TextView TextMessageOwn = (TextView) view.findViewById(R.id.OwnTextTextMessage);
                TextMessageOwn.setText(cur.getString(Constants.ID_MESSAGES_TextMsgValue));
                TextView TextStatusOwn = (TextView) view.findViewById(R.id.OwnTextStatus);
                TextStatusOwn.setText(NumTotal + "/" + NumRead + "/" + NumShow);
                ImageButton TOusericon = (ImageButton) view.findViewById(R.id.OwnTextUserIcon);
                if (userImages.containsKey(msgOID)) {
                    TOusericon.setImageBitmap(userImages.get(msgOID));
                } else {
                    TOusericon.setImageBitmap(BitmapFactory.decodeResource(mContext.getResources(), R.drawable.iconuser));
                }
                break;
            case TEXTMSG_FOREIGN:
                TextView TxtOwningUserNameForeign = (TextView) view.findViewById(R.id.ForTextOwningUserName);
                TxtOwningUserNameForeign.setText(OName);
                TextView TxtSendTimeStampForeign = (TextView) view.findViewById(R.id.ForTextSendTimeStamp);
                TxtSendTimeStampForeign.setText(new SimpleDateFormat(Constants.DATETIMEFORMAT).format(sDate));
                TextView TxtReadTimeStampForeign = (TextView) view.findViewById(R.id.ForTextReadTimeStamp);
                TxtReadTimeStampForeign.setText(new SimpleDateFormat(Constants.DATETIMEFORMAT).format(rDate));
                TextView TextMessageForeign = (TextView) view.findViewById(R.id.ForTextTextMessage);
                TextMessageForeign.setText(cur.getString(Constants.ID_MESSAGES_TextMsgValue));
                TextView TextStatusForeign = (TextView) view.findViewById(R.id.ForTextStatus);
                TextStatusForeign.setText(NumTotal + "/" + NumRead + "/" + NumShow);
                ImageButton TFusericon = (ImageButton) view.findViewById(R.id.ForTextUserIcon);
                if (userImages.containsKey(msgOID)) {
                    TFusericon.setImageBitmap(userImages.get(msgOID));
                } else {
                    TFusericon.setImageBitmap(BitmapFactory.decodeResource(mContext.getResources(), R.drawable.iconuser));
                }
                break;
            case IMAGEMSG_OWN:
                TextView ImgOwningUserNameOwn = (TextView) view.findViewById(R.id.OwnImageOwningUserName);
                ImgOwningUserNameOwn.setText(OName);
                TextView ImgSendTimeStampOwn = (TextView) view.findViewById(R.id.OwnImageSendTimeStamp);
                ImgSendTimeStampOwn.setText(new SimpleDateFormat(Constants.DATETIMEFORMAT).format(sDate));
                TextView ImgReadTimeStampOwn = (TextView) view.findViewById(R.id.OwnImageReadTimeStamp);
                ImgReadTimeStampOwn.setText(new SimpleDateFormat(Constants.DATETIMEFORMAT).format(rDate));
                TextView ImageStatusOwn = (TextView) view.findViewById(R.id.OwnImageStatus);
                ImageStatusOwn.setText(NumTotal + "/" + NumRead + "/" + NumShow);
                final String imgfileOwn = cur.getString(Constants.ID_MESSAGES_ImageMsgValue);
                if (imgfileOwn != null && !imgfileOwn.isEmpty()) {
                    ImageButton IButtonOwn = (ImageButton) view.findViewById(R.id.OwnImageImageButton);
                    setImageView(IButtonOwn, imgfileOwn, 200, 200, "image/*");
                }
                ImageButton IOusericon = (ImageButton) view.findViewById(R.id.OwnImageUserIcon);
                if (userImages.containsKey(msgOID)) {
                    IOusericon.setImageBitmap(userImages.get(msgOID));
                } else {
                    IOusericon.setImageBitmap(BitmapFactory.decodeResource(mContext.getResources(), R.drawable.iconuser));
                }
                break;
            case IMAGEMSG_FOREIGN:
                TextView ImgOwningUserNameForeign = (TextView) view.findViewById(R.id.ForImageOwningUserName);
                ImgOwningUserNameForeign.setText(OName);
                TextView ImgSendTimeStampForeign = (TextView) view.findViewById(R.id.ForImageSendTimeStamp);
                ImgSendTimeStampForeign.setText(new SimpleDateFormat(Constants.DATETIMEFORMAT).format(sDate));
                TextView ImgReadTimeStampForeign = (TextView) view.findViewById(R.id.ForImageReadTimeStamp);
                ImgReadTimeStampForeign.setText(new SimpleDateFormat(Constants.DATETIMEFORMAT).format(rDate));
                TextView ImageStatusForeign = (TextView) view.findViewById(R.id.ForImageStatus);
                ImageStatusForeign.setText(NumTotal + "/" + NumRead + "/" + NumShow);

                final String imgfileForeign = cur.getString(Constants.ID_MESSAGES_ImageMsgValue);
                if (imgfileForeign != null && !imgfileForeign.isEmpty()) {
                    ImageButton IButtonForeign = (ImageButton) view.findViewById(R.id.ForImageImageButton);
                    setImageView(IButtonForeign, imgfileForeign, 200, 200, "image/*");
                }
                ImageButton IFusericon = (ImageButton) view.findViewById(R.id.ForImageUserIcon);
                if (userImages.containsKey(msgOID)) {
                    IFusericon.setImageBitmap(userImages.get(msgOID));
                } else {
                    IFusericon.setImageBitmap(BitmapFactory.decodeResource(mContext.getResources(), R.drawable.iconuser));
                }
                break;
            case VIDEOMSG_OWN:

                TextView VidOwningUserNameOwn = (TextView) view.findViewById(R.id.OwnVideoOwningUserName);
                VidOwningUserNameOwn.setText(OName);
                TextView VidSendTimeStampOwn = (TextView) view.findViewById(R.id.OwnVideoSendTimeStamp);
                VidSendTimeStampOwn.setText(new SimpleDateFormat(Constants.DATETIMEFORMAT).format(sDate));
                TextView VidReadTimeStampOwn = (TextView) view.findViewById(R.id.OwnVideoReadTimeStamp);
                VidReadTimeStampOwn.setText(new SimpleDateFormat(Constants.DATETIMEFORMAT).format(rDate));
                TextView VideoStatusOwn = (TextView) view.findViewById(R.id.OwnVideoStatus);
                VideoStatusOwn.setText(NumTotal + "/" + NumRead + "/" + NumShow);
                final String vidfileOwn = cur.getString(Constants.ID_MESSAGES_VideoMsgValue);
                ImageButton VButtonOwn = (ImageButton) view.findViewById(R.id.OwnVideoImageButton);
                File vfileOwn = new File(vidfileOwn);
                if (vfileOwn.exists()) {
                    Bitmap Preview = getVideoFrame(vidfileOwn);
                    Bitmap thumbnailOwn;
                    if (Preview != null) {
                        thumbnailOwn = scaleBitmap(Preview, 600);
                    } else {
                        thumbnailOwn = BitmapFactory.decodeResource(mContext.getResources(), R.drawable.ic_play_video);
                    }
                    VButtonOwn.setImageBitmap(thumbnailOwn);
                    VButtonOwn.setMaxWidth(thumbnailOwn.getWidth());
                    VButtonOwn.setMaxHeight(thumbnailOwn.getHeight());
                    VButtonOwn.setOnClickListener(new View.OnClickListener() {

                        @Override
                        public void onClick(View v) {
                            Intent intent = new Intent();
                            intent.setAction(android.content.Intent.ACTION_VIEW);
                            File file = new File(vidfileOwn);
                            intent.setDataAndType(Uri.fromFile(file), "video/*");
                            mContext.startActivity(intent);
                        }
                    });
                } else {
                    Bitmap download = BitmapFactory.decodeResource(mContext.getResources(), R.drawable.ic_action_download);
                    VButtonOwn.setImageBitmap(download);
                    VButtonOwn.setMaxWidth(download.getWidth());
                    VButtonOwn.setMaxHeight(download.getHeight());
                    final int vidid = cur.getInt(Constants.ID_MESSAGES_VideoMsgID);
                    final int msgid = cur.getInt(Constants.ID_MESSAGES_BADBID);
                    VButtonOwn.setOnClickListener(new View.OnClickListener() {

                        @Override
                        public void onClick(View v) {
                            Intent downloadintent = new Intent(v.getContext(), MeBaService.class);

                            downloadintent.setAction(Constants.ACTION_GETVIDEOMESSAGE);
                            downloadintent.putExtra(Constants.MESSAGEID, msgid);
                            downloadintent.putExtra(Constants.VIDEOID, vidid);
                            v.getContext().startService(downloadintent);
                        }
                    });
                }
                ImageButton VOusericon = (ImageButton) view.findViewById(R.id.OwnVideoUserIcon);
                if (userImages.containsKey(msgOID)) {
                    VOusericon.setImageBitmap(userImages.get(msgOID));
                } else {
                    VOusericon.setImageBitmap(BitmapFactory.decodeResource(mContext.getResources(), R.drawable.iconuser));
                }
                break;
            case VIDEOMSG_FOREIGN:
                TextView VidOwningUserNameForeign = (TextView) view.findViewById(R.id.ForVideoOwningUserName);
                VidOwningUserNameForeign.setText(OName);
                TextView VidSendTimeStampForeign = (TextView) view.findViewById(R.id.ForVideoSendTimeStamp);
                VidSendTimeStampForeign.setText(new SimpleDateFormat(Constants.DATETIMEFORMAT).format(sDate));
                TextView VidReadTimeStampForeign = (TextView) view.findViewById(R.id.ForVideoReadTimeStamp);
                VidReadTimeStampForeign.setText(new SimpleDateFormat(Constants.DATETIMEFORMAT).format(rDate));
                TextView VideoStatusForeign = (TextView) view.findViewById(R.id.ForVideoStatus);
                VideoStatusForeign.setText(NumTotal + "/" + NumRead + "/" + NumShow);

                final String vidfileForeign = cur.getString(Constants.ID_MESSAGES_VideoMsgValue);
                ImageButton VButtonForeign = (ImageButton) view.findViewById(R.id.ForVideoImageButton);

                File vfileForeign = new File(vidfileForeign);
                if (vfileForeign.exists()) {
                    Bitmap Preview = getVideoFrame(vidfileForeign);
                    Bitmap thumbnailForeign;
                    if (Preview != null) {
                        thumbnailForeign = scaleBitmap(Preview, 600);
                    } else {
                        thumbnailForeign = BitmapFactory.decodeResource(mContext.getResources(), R.drawable.ic_play_video);
                    }
                    VButtonForeign.setImageBitmap(thumbnailForeign);
                    VButtonForeign.setMaxWidth(thumbnailForeign.getWidth());
                    VButtonForeign.setMaxHeight(thumbnailForeign.getHeight());
                    VButtonForeign.setOnClickListener(new View.OnClickListener() {

                        @Override
                        public void onClick(View v) {
                            Intent intent = new Intent();
                            intent.setAction(android.content.Intent.ACTION_VIEW);
                            File file = new File(vidfileForeign);
                            intent.setDataAndType(Uri.fromFile(file), "video/*");
                            mContext.startActivity(intent);
                        }
                    });
                } else {
                    Bitmap download = BitmapFactory.decodeResource(mContext.getResources(), R.drawable.ic_action_download);
                    VButtonForeign.setImageBitmap(download);
                    VButtonForeign.setMaxWidth(download.getWidth());
                    VButtonForeign.setMaxHeight(download.getHeight());
                    final int vidid = cur.getInt(Constants.ID_MESSAGES_VideoMsgID);
                    final int msgid = cur.getInt(Constants.ID_MESSAGES_BADBID);
                    VButtonForeign.setOnClickListener(new View.OnClickListener() {

                        @Override
                        public void onClick(View v) {
                            Intent downloadintent = new Intent(v.getContext(), MeBaService.class);

                            downloadintent.setAction(Constants.ACTION_GETVIDEOMESSAGE);
                            downloadintent.putExtra(Constants.MESSAGEID, msgid);
                            downloadintent.putExtra(Constants.VIDEOID, vidid);
                            v.getContext().startService(downloadintent);
                        }
                    });
                }
                ImageButton VFusericon = (ImageButton) view.findViewById(R.id.ForVideoUserIcon);
                if (userImages.containsKey(msgOID)) {
                    VFusericon.setImageBitmap(userImages.get(msgOID));
                } else {
                    VFusericon.setImageBitmap(BitmapFactory.decodeResource(mContext.getResources(), R.drawable.iconuser));
                }
                break;
        }
        Log.d(TAG, "end bindView ");
    }

    private Bitmap getVideoFrame(String file) {
        MediaMetadataRetriever retriever = new MediaMetadataRetriever();
        try {
            retriever.setDataSource(file);
            return retriever.getFrameAtTime(2000);
        } catch (IllegalArgumentException ex) {
            ex.printStackTrace();
        } catch (RuntimeException ex) {
            ex.printStackTrace();
        } finally {
            try {
                retriever.release();
            } catch (RuntimeException ex) {
            }
        }
        return null;
    }

    private Bitmap scaleBitmap(Bitmap bm, int newSize) {
        int width = bm.getWidth();
        int height = bm.getHeight();

        Log.v("Pictures", "Width and height are " + width + "--" + height);

        if (width > newSize && height > newSize) {
            if (width > height) {
                // landscape
                int ratio = width / newSize;
                width = newSize;
                height = height / ratio;
            } else if (height > width) {
                // portrait
                int ratio = height / newSize;
                height = newSize;
                width = width / ratio;
            } else {
                // square
                height = newSize;
                width = newSize;
            }
            Log.v("Pictures", "after scaling Width and height are " + width + "--" + height);
            bm = Bitmap.createScaledBitmap(bm, width, height, true);
        }
        return bm;
    }

    private void setImageView(ImageButton iv, final String fname, int width, int height, final String mediatype) {
        iv.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                Intent intent = new Intent();
                intent.setAction(android.content.Intent.ACTION_VIEW);
                File file = new File(fname);
                intent.setDataAndType(Uri.fromFile(file), mediatype);
                mContext.startActivity(intent);
            }
        });


        File ifileOwn = new File(fname);
        if (ifileOwn.exists()) {
            String filename = ifileOwn.getAbsolutePath();
            BitmapFactory.Options options = new BitmapFactory.Options();
            options.inJustDecodeBounds = true;
            BitmapFactory.decodeFile(fname, options);

            options.inSampleSize = calculateInSampleSize(options, width, height);

            // Decode bitmap with inSampleSize set
            options.inJustDecodeBounds = false;
            Bitmap bmp = BitmapFactory.decodeFile(filename, options);

            iv.setImageBitmap(bmp);
            iv.setMinimumWidth(options.outWidth);
            iv.setMinimumHeight(options.outHeight);
            iv.setMaxWidth(options.outWidth);
            iv.setMaxHeight(options.outHeight);
        }
    }
}