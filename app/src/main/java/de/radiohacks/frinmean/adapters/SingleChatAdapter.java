package de.radiohacks.frinmean.adapters;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.support.v4.widget.CursorAdapter;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import de.radiohacks.frinmean.ChatActivity;
import de.radiohacks.frinmean.Constants;
import de.radiohacks.frinmean.R;
import de.radiohacks.frinmean.service.MeBaService;

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
    private String directory;

    public SingleChatAdapter(Context context, Cursor cursor, int InOID, String dir) {
        super(context, cursor, true);
        Log.d(TAG, "start SingleChatAdapter");
        this.OID = InOID;
        this.directory = dir;
        Log.d(TAG, "end SingleChatAdapter");
    }

    public static int calculateInSampleSize(
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
        final int msgID = cur.getInt(Constants.ID_MESSAGES_BADBID);
        String msgOID = cur.getString(Constants.ID_MESSAGES_OwningUserID);

        // Needed for the innerclass in the dilaog.
        final int imageid = cur.getInt(Constants.ID_MESSAGES_ImageMsgID);
        final int textid = cur.getInt(Constants.ID_MESSAGES_TextMsgID);
        final int locationid = cur.getInt(Constants.ID_MESSAGES_LocationMsgID);
        final int fileid = cur.getInt(Constants.ID_MESSAGES_FileMsgID);
        final int contactid = cur.getInt(Constants.ID_MESSAGES_ContactMsgID);
        final int videoid = cur.getInt(Constants.ID_MESSAGES_VideoMsgID);
        final String imageval = cur.getString(Constants.ID_MESSAGES_ImageMsgValue);
        final String textval = cur.getString(Constants.ID_MESSAGES_TextMsgValue);
        final String locationoval = cur.getString(Constants.ID_MESSAGES_LocationMsgValue);
        final String fileval = cur.getString(Constants.ID_MESSAGES_FileMsgValue);
        final String contactval = cur.getString(Constants.ID_MESSAGES_ContactMsgValue);
        final String videoval = cur.getString(Constants.ID_MESSAGES_VideoMsgValue);


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
                                                delintent.putExtra(Constants.MESSAGEID, msgID);
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
                                Intent startchat = new Intent(context, ChatActivity.class);
                                startchat.putExtra(Constants.USERID, OID);
                                startchat.putExtra(Constants.CHAT_ACTIVITY_MODE, Constants.CHAT_ACTIVITY_FORWARD);
                                startchat.putExtra(Constants.SENDCHATID, chatID);

                                startchat.putExtra(Constants.MESSAGETYPE, msgType);
                                switch (findMsgType(msgType, mine)) {
                                    case TEXTMSG_OWN:
                                        startchat.putExtra(Constants.FWDCONTENTMESSAGEID, textid);
                                        startchat.putExtra(Constants.FWDCONTENTMESSAGE, textval);
                                        break;
                                    case TEXTMSG_FOREIGN:
                                        startchat.putExtra(Constants.FWDCONTENTMESSAGEID, textid);
                                        startchat.putExtra(Constants.FWDCONTENTMESSAGE, textval);
                                        break;
                                    case IMAGEMSG_OWN:
                                        startchat.putExtra(Constants.FWDCONTENTMESSAGEID, imageid);
                                        startchat.putExtra(Constants.FWDCONTENTMESSAGE, imageval);
                                        break;
                                    case IMAGEMSG_FOREIGN:
                                        startchat.putExtra(Constants.FWDCONTENTMESSAGEID, imageid);
                                        startchat.putExtra(Constants.FWDCONTENTMESSAGE, imageval);
                                        break;
                                    case FILEMSG_OWN:
                                        startchat.putExtra(Constants.FWDCONTENTMESSAGEID, fileid);
                                        startchat.putExtra(Constants.FWDCONTENTMESSAGE, fileval);
                                        break;
                                    case FILEMSG_FOREIGN:
                                        startchat.putExtra(Constants.FWDCONTENTMESSAGEID, fileid);
                                        startchat.putExtra(Constants.FWDCONTENTMESSAGE, fileval);
                                        break;
                                    case CONTACTMSG_OWN:
                                        startchat.putExtra(Constants.FWDCONTENTMESSAGEID, contactid);
                                        startchat.putExtra(Constants.FWDCONTENTMESSAGE, contactval);
                                        break;
                                    case CONTACTMSG_FOREIGN:
                                        startchat.putExtra(Constants.FWDCONTENTMESSAGEID, contactid);
                                        startchat.putExtra(Constants.FWDCONTENTMESSAGE, contactval);
                                        break;
                                    case LOCATIONMSG_OWN:
                                        startchat.putExtra(Constants.FWDCONTENTMESSAGEID, locationid);
                                        startchat.putExtra(Constants.FWDCONTENTMESSAGE, locationoval);
                                        break;
                                    case LOCATIONMSG_FOREIGN:
                                        startchat.putExtra(Constants.FWDCONTENTMESSAGEID, locationid);
                                        startchat.putExtra(Constants.FWDCONTENTMESSAGE, locationoval);
                                        break;
                                    case VIDEOMSG_OWN:
                                        startchat.putExtra(Constants.FWDCONTENTMESSAGEID, videoid);
                                        startchat.putExtra(Constants.FWDCONTENTMESSAGE, videoval);
                                        break;
                                    case VIDEOMSG_FOREIGN:
                                        startchat.putExtra(Constants.FWDCONTENTMESSAGEID, videoid);
                                        startchat.putExtra(Constants.FWDCONTENTMESSAGE, videoval);
                                        break;
                                }

                                context.startActivity(startchat);
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
        int NumTotal = cur.getInt(Constants.ID_MESSAGES_NumberAll);
        int NumRead = cur.getInt(Constants.ID_MESSAGES_NumberRead);
        int NumShow = cur.getInt(Constants.ID_MESSAGES_NumberShow);

        switch (findMsgType(msgType, mine)) {
            case TEXTMSG_OWN:
                TextView TxtOwningUserNameOwn = (TextView) view.findViewById(R.id.TextOwningUserName);
                TxtOwningUserNameOwn.setText(OName);
                TextView TxtSendTimeStampOwn = (TextView) view.findViewById(R.id.TextSendTimeStamp);
                TxtSendTimeStampOwn.setText(new SimpleDateFormat(Constants.DATETIMEFORMAT).format(sDate));
                TextView TxtReadTimeStampOwn = (TextView) view.findViewById(R.id.TextReadTimeStamp);
                TxtReadTimeStampOwn.setText(new SimpleDateFormat(Constants.DATETIMEFORMAT).format(rDate));
                TextView TextMessageOwn = (TextView) view.findViewById(R.id.TextTextMessage);
                TextMessageOwn.setText(cur.getString(Constants.ID_MESSAGES_TextMsgValue));
                TextView TextStatusOwn = (TextView) view.findViewById(R.id.TextStatus);
                TextStatusOwn.setText(NumTotal + "/" + NumRead + "/" + NumShow);
                break;
            case TEXTMSG_FOREIGN:
                TextView TxtOwningUserNameForeign = (TextView) view.findViewById(R.id.TextOwningUserName);
                TxtOwningUserNameForeign.setText(OName);
                TextView TxtSendTimeStampForeign = (TextView) view.findViewById(R.id.TextSendTimeStamp);
                TxtSendTimeStampForeign.setText(new SimpleDateFormat(Constants.DATETIMEFORMAT).format(sDate));
                TextView TxtReadTimeStampForeign = (TextView) view.findViewById(R.id.TextReadTimeStamp);
                TxtReadTimeStampForeign.setText(new SimpleDateFormat(Constants.DATETIMEFORMAT).format(rDate));
                TextView TextMessageForeign = (TextView) view.findViewById(R.id.TextTextMessage);
                TextMessageForeign.setText(cur.getString(Constants.ID_MESSAGES_TextMsgValue));
                TextView TextStatusForeign = (TextView) view.findViewById(R.id.TextStatus);
                TextStatusForeign.setText(NumTotal + "/" + NumRead + "/" + NumShow);
                break;
            case IMAGEMSG_OWN:
                TextView ImgOwningUserNameOwn = (TextView) view.findViewById(R.id.ImageOwningUserName);
                ImgOwningUserNameOwn.setText(OName);
                TextView ImgSendTimeStampOwn = (TextView) view.findViewById(R.id.ImageSendTimeStamp);
                ImgSendTimeStampOwn.setText(new SimpleDateFormat(Constants.DATETIMEFORMAT).format(sDate));
                TextView ImgReadTimeStampOwn = (TextView) view.findViewById(R.id.ImageReadTimeStamp);
                ImgReadTimeStampOwn.setText(new SimpleDateFormat(Constants.DATETIMEFORMAT).format(rDate));
                TextView ImageStatusOwn = (TextView) view.findViewById(R.id.ImageStatus);
                ImageStatusOwn.setText(NumTotal + "/" + NumRead + "/" + NumShow);
                ImageButton IButtonOwn = (ImageButton) view.findViewById(R.id.ImageImageButton);

                String tmpimgOwn;
                if (directory.endsWith("/")) {
                    tmpimgOwn = Constants.IMAGEDIR + "/" + cur.getString(Constants.ID_MESSAGES_ImageMsgValue);
                } else {
                    tmpimgOwn = "/" + Constants.IMAGEDIR + "/" + cur.getString(Constants.ID_MESSAGES_ImageMsgValue);
                }

                final String imgfileOwn = directory + tmpimgOwn;
                IButtonOwn.setOnClickListener(new View.OnClickListener() {

                    @Override
                    public void onClick(View v) {
                        Intent intent = new Intent();
                        intent.setAction(android.content.Intent.ACTION_VIEW);
                        intent.setDataAndType(Uri.parse(imgfileOwn), "image/*");

                        mContext.startActivity(intent);
                    }
                });


                File ifileOwn = new File(imgfileOwn);
                if (ifileOwn.exists()) {
                    String fname = ifileOwn.getAbsolutePath();
                    BitmapFactory.Options options = new BitmapFactory.Options();
                    options.inJustDecodeBounds = true;
                    BitmapFactory.decodeFile(fname, options);

                    options.inSampleSize = calculateInSampleSize(options, 200, 200);

                    // Decode bitmap with inSampleSize set
                    options.inJustDecodeBounds = false;
                    Bitmap bmp = BitmapFactory.decodeFile(fname, options);

                    IButtonOwn.setImageBitmap(bmp);
                    IButtonOwn.setMaxWidth(options.outWidth);
                    IButtonOwn.setMaxHeight(options.outHeight);

                }
                break;
            case IMAGEMSG_FOREIGN:
                TextView ImgOwningUserNameForeign = (TextView) view.findViewById(R.id.ImageOwningUserName);
                ImgOwningUserNameForeign.setText(OName);
                TextView ImgSendTimeStampForeign = (TextView) view.findViewById(R.id.ImageSendTimeStamp);
                ImgSendTimeStampForeign.setText(new SimpleDateFormat(Constants.DATETIMEFORMAT).format(sDate));
                TextView ImgReadTimeStampForeign = (TextView) view.findViewById(R.id.ImageReadTimeStamp);
                ImgReadTimeStampForeign.setText(new SimpleDateFormat(Constants.DATETIMEFORMAT).format(rDate));
                TextView ImageStatusForeign = (TextView) view.findViewById(R.id.ImageStatus);
                ImageStatusForeign.setText(NumTotal + "/" + NumRead + "/" + NumShow);
                ImageButton IButtonForeign = (ImageButton) view.findViewById(R.id.ImageImageButton);

                String tmpimgForeign;
                if (directory.endsWith("/")) {
                    tmpimgForeign = Constants.IMAGEDIR + "/" + cur.getString(Constants.ID_MESSAGES_ImageMsgValue);
                } else {
                    tmpimgForeign = "/" + Constants.IMAGEDIR + "/" + cur.getString(Constants.ID_MESSAGES_ImageMsgValue);
                }

                final String imgfileForeign = directory + tmpimgForeign;
                IButtonForeign.setOnClickListener(new View.OnClickListener() {

                    @Override
                    public void onClick(View v) {
                        Intent intent = new Intent();
                        intent.setAction(android.content.Intent.ACTION_VIEW);
                        intent.setDataAndType(Uri.parse(imgfileForeign), "image/*");

                        mContext.startActivity(intent);
                    }
                });


                File ifileForeign = new File(imgfileForeign);
                if (ifileForeign.exists()) {
                    String fname = ifileForeign.getAbsolutePath();
                    BitmapFactory.Options options = new BitmapFactory.Options();
                    options.inJustDecodeBounds = true;
                    BitmapFactory.decodeFile(fname, options);

                    options.inSampleSize = calculateInSampleSize(options, 200, 200);

                    // Decode bitmap with inSampleSize set
                    options.inJustDecodeBounds = false;
                    Bitmap bmp = BitmapFactory.decodeFile(fname, options);

                    IButtonForeign.setImageBitmap(bmp);
                    IButtonForeign.setMaxWidth(options.outWidth);
                    IButtonForeign.setMaxHeight(options.outHeight);

                }
                break;
            case VIDEOMSG_OWN:
                TextView VidOwningUserNameOwn = (TextView) view.findViewById(R.id.VideoOwningUserName);
                VidOwningUserNameOwn.setText(OName);
                TextView VidSendTimeStampOwn = (TextView) view.findViewById(R.id.VideoSendTimeStamp);
                VidSendTimeStampOwn.setText(new SimpleDateFormat(Constants.DATETIMEFORMAT).format(sDate));
                TextView VidReadTimeStampOwn = (TextView) view.findViewById(R.id.VideoReadTimeStamp);
                VidReadTimeStampOwn.setText(new SimpleDateFormat(Constants.DATETIMEFORMAT).format(rDate));
                TextView VideoStatusOwn = (TextView) view.findViewById(R.id.VideoStatus);
                VideoStatusOwn.setText(NumTotal + "/" + NumRead + "/" + NumShow);
                ImageButton VButtonOwn = (ImageButton) view.findViewById(R.id.VideoImageButton);

                String tmpvidOwn;
                if (directory.endsWith("/")) {
                    tmpvidOwn = Constants.VIDEODIR + "/" + cur.getString(Constants.ID_MESSAGES_VideoMsgValue);
                } else {
                    tmpvidOwn = "/" + Constants.VIDEODIR + "/" + cur.getString(Constants.ID_MESSAGES_VideoMsgValue);
                }

                final String vidfileOwn = directory + tmpvidOwn;
                VButtonOwn.setOnClickListener(new View.OnClickListener() {

                    @Override
                    public void onClick(View v) {
                        Intent intent = new Intent();
                        intent.setAction(android.content.Intent.ACTION_VIEW);
                        intent.setDataAndType(Uri.parse(vidfileOwn), "video/*");

                        mContext.startActivity(intent);
                    }
                });
                break;
            case VIDEOMSG_FOREIGN:
                TextView VidOwningUserNameForeign = (TextView) view.findViewById(R.id.VideoOwningUserName);
                VidOwningUserNameForeign.setText(OName);
                TextView VidSendTimeStampForeign = (TextView) view.findViewById(R.id.VideoSendTimeStamp);
                VidSendTimeStampForeign.setText(new SimpleDateFormat(Constants.DATETIMEFORMAT).format(sDate));
                TextView VidReadTimeStampForeign = (TextView) view.findViewById(R.id.VideoReadTimeStamp);
                VidReadTimeStampForeign.setText(new SimpleDateFormat(Constants.DATETIMEFORMAT).format(rDate));
                TextView VideoStatusForeign = (TextView) view.findViewById(R.id.VideoStatus);
                VideoStatusForeign.setText(NumTotal + "/" + NumRead + "/" + NumShow);
                ImageButton VButtonForeign = (ImageButton) view.findViewById(R.id.VideoImageButton);

                String tmpvidForeign;
                if (directory.endsWith("/")) {
                    tmpvidForeign = Constants.VIDEODIR + "/" + cur.getString(Constants.ID_MESSAGES_VideoMsgValue);
                } else {
                    tmpvidForeign = "/" + Constants.VIDEODIR + "/" + cur.getString(Constants.ID_MESSAGES_VideoMsgValue);
                }

                final String vidfileForeign = directory + tmpvidForeign;
                VButtonForeign.setOnClickListener(new View.OnClickListener() {

                    @Override
                    public void onClick(View v) {
                        Intent intent = new Intent();
                        intent.setAction(android.content.Intent.ACTION_VIEW);
                        intent.setDataAndType(Uri.parse(vidfileForeign), "video/*");

                        mContext.startActivity(intent);
                    }
                });
                break;
        }
        Log.d(TAG, "end bindView ");
    }

}
