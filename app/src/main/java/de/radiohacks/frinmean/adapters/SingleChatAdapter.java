package de.radiohacks.frinmean.adapters;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
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
import java.util.Date;

import de.radiohacks.frinmean.Constants;
import de.radiohacks.frinmean.R;

/**
 * Created by thomas on 25.10.14.
 */
public class SingleChatAdapter extends CursorAdapter {

    private static final String TAG = SingleChatAdapter.class.getSimpleName();
    private static final int TEXTMSG = 0;
    private static final int IMAGEMSG = 1;
    private static final int FILEMSG = 2;
    private static final int CONTACTMSG = 3;
    private static final int LOCATIONMSG = 4;
    private static final int VIDEOMSG = 5;

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

    private int findMsgType(String in) {
        Log.d(TAG, "start findMsgType");
        int ret = -1;
        if (in.equalsIgnoreCase(Constants.TYP_TEXT)) {
            ret = TEXTMSG;
        } else if (in.equalsIgnoreCase(Constants.TYP_IMAGE)) {
            ret = IMAGEMSG;
        } else if (in.equalsIgnoreCase(Constants.TYP_FILE)) {
            ret = FILEMSG;
        } else if (in.equalsIgnoreCase(Constants.TYP_CONTACT)) {
            ret = CONTACTMSG;
        } else if (in.equalsIgnoreCase(Constants.TYP_LOCATION)) {
            ret = LOCATIONMSG;
        } else if (in.equalsIgnoreCase(Constants.TYP_VIDEO)) {
            ret = VIDEOMSG;
        }
        Log.d(TAG, "end findMsgType");
        return ret;
    }

    @Override
    public int getViewTypeCount() {
        Log.d(TAG, "start & end getViewTypeCount");
        return 6;
    }

    @Override
    public int getItemViewType(int position) {
        Log.d(TAG, "start getItemViewType");
        Cursor cursor = getCursor();
        if (cursor.moveToPosition(position)) {
            Integer ret = findMsgType(cursor.getString(Constants.ID_MESSAGES_MessageType));
            Log.d(TAG, "end getItemViewType " + ret.toString());
            return ret;
        } else {
            Log.d(TAG, "start getItemViewType -1");
            return -1;
        }
    }

    @Override
    public View newView(Context context, Cursor cur, ViewGroup parent) {
        Log.d(TAG, "start newView");
        View ret = null;

        String msgType = cur.getString(Constants.ID_MESSAGES_MessageType);
        String msgOID = cur.getString(Constants.ID_MESSAGES_OwningUserID);
        boolean mine = msgOID.equalsIgnoreCase(String.valueOf(OID));
        LayoutInflater li = (LayoutInflater) context
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        switch (findMsgType(msgType)) {
            case TEXTMSG:
                if (mine) {
                    ret = li.inflate(R.layout.rellay_textownmsg, parent, false);
                } else {
                    ret = li.inflate(R.layout.rellay_textforeignmsg, parent, false);
                }
                break;
            case IMAGEMSG:
                ret = li.inflate(R.layout.imagemsg, parent, false);
                if (mine) {
                    ret.setBackgroundResource(R.drawable.bubble_green);
                    ret.setPadding(10, 5, 20, 20);
                    ((TableLayout) ret).setGravity(Gravity.END);
                } else {
                    ret.setBackgroundResource(R.drawable.bubble_yellow);
                    ret.setPadding(20, 5, 10, 20);
                    ((TableLayout) ret).setGravity(Gravity.START);
                }
                break;
            case VIDEOMSG:
                ret = li.inflate(R.layout.videomsg, parent, false);
                if (mine) {
                    ret.setBackgroundResource(R.drawable.bubble_green);
                    ret.setPadding(10, 5, 20, 20);
                    ((TableLayout) ret).setGravity(Gravity.END);
                } else {
                    ret.setBackgroundResource(R.drawable.bubble_yellow);
                    ret.setPadding(20, 5, 10, 20);
                    ((TableLayout) ret).setGravity(Gravity.START);
                }
                break;
        }
        Log.d(TAG, "end newView");
        return ret;
    }

    @Override
    public void bindView(View view, Context context, Cursor cur) {
        Log.d(TAG, "start bindView");
        String msgType = cur.getString(Constants.ID_MESSAGES_MessageType);
        int msgOID = cur.getInt(Constants.ID_MESSAGES_OwningUserID);
        long sstamp = cur.getLong(Constants.ID_MESSAGES_SendTimeStamp);
        Date sDate = new java.util.Date(sstamp * 1000);
        long rstamp = cur.getLong(Constants.ID_MESSAGES_ReadTimeStamp);
        Date rDate = new java.util.Date(rstamp * 1000);
        String OName = cur.getString(Constants.ID_MESSAGES_OwningUserName);

        switch (findMsgType(msgType)) {
            case TEXTMSG:
                TextView TxtOwningUserName = (TextView) view.findViewById(R.id.TextOwningUserName);
                TxtOwningUserName.setText(OName);
                TextView TxtSendTimeStamp = (TextView) view.findViewById(R.id.TextSendTimeStamp);
                TxtSendTimeStamp.setText(new SimpleDateFormat("dd.MM.yyy HH:mm:ss").format(sDate));
                TextView TxtReadTimeStamp = (TextView) view.findViewById(R.id.TextReadTimeStamp);
                TxtReadTimeStamp.setText(new SimpleDateFormat("dd.MM.yyy HH:mm:ss").format(rDate));
                TextView TextMessage = (TextView) view.findViewById(R.id.TextTextMessage);
                TextMessage.setText(cur.getString(Constants.ID_MESSAGES_TextMsgValue));

                TxtOwningUserName.setGravity(Gravity.START);
                TextMessage.setGravity(Gravity.START);

                // RelativeLayout bg = (RelativeLayout) view.findViewById(R.id.TextMsgLayout);
                if (msgOID == this.OID) {
                    TxtOwningUserName.setGravity(Gravity.END);
                    TxtReadTimeStamp.setGravity(Gravity.END);
                    TxtSendTimeStamp.setGravity(Gravity.END);
                    TextMessage.setGravity(Gravity.END);
                } else {
                    TxtOwningUserName.setGravity(Gravity.START);
                    TxtReadTimeStamp.setGravity(Gravity.START);
                    TxtSendTimeStamp.setGravity(Gravity.START);
                    TextMessage.setGravity(Gravity.START);
                }
                break;
            case IMAGEMSG:
                TextView ImgOwningUserName = (TextView) view.findViewById(R.id.ImageOwningUserName);
                ImgOwningUserName.setText(OName);
                TextView ImgSendTimeStamp = (TextView) view.findViewById(R.id.ImageSendTimeStamp);
                ImgSendTimeStamp.setText(new SimpleDateFormat("dd.MM.yyy HH:mm:ss").format(sDate));
                TextView ImgReadTimeStamp = (TextView) view.findViewById(R.id.ImageReadTimeStamp);
                ImgReadTimeStamp.setText(new SimpleDateFormat("dd.MM.yyy HH:mm:ss").format(rDate));
                ImageButton IButton = (ImageButton) view.findViewById(R.id.ImageImageButton);

                String tmpimg;
                if (directory.endsWith("/")) {
                    tmpimg = Constants.IMAGEDIR + "/" + cur.getString(Constants.ID_MESSAGES_ImageMsgValue);
                } else {
                    tmpimg = "/" + Constants.IMAGEDIR + "/" + cur.getString(Constants.ID_MESSAGES_ImageMsgValue);
                }

                final String imgfile = directory + tmpimg;
                IButton.setOnClickListener(new View.OnClickListener() {

                    @Override
                    public void onClick(View v) {
                        Intent intent = new Intent();
                        intent.setAction(android.content.Intent.ACTION_VIEW);
                        intent.setDataAndType(Uri.parse(imgfile), "image/*");

                        mContext.startActivity(intent);
                    }
                });


                File ifile = new File(imgfile);
                if (ifile.exists()) {


                    String fname = ifile.getAbsolutePath();
                    BitmapFactory.Options options = new BitmapFactory.Options();
                    options.inJustDecodeBounds = true;
                    BitmapFactory.decodeFile(fname, options);
                    int imageHeight = options.outHeight;
                    int imageWidth = options.outWidth;
                    String imageType = options.outMimeType;

                    options.inSampleSize = calculateInSampleSize(options, 200, 200);

                    // Decode bitmap with inSampleSize set
                    options.inJustDecodeBounds = false;
                    Bitmap bmp = BitmapFactory.decodeFile(fname, options);

                    IButton.setImageBitmap(bmp);
                    IButton.setMaxWidth(options.outWidth);
                    IButton.setMaxHeight(options.outHeight);

                    //Bitmap bmp = BitmapFactory.decodeFile(fname);
                    /* if (bmp != null) {
                        int imgheight = bmp.getHeight();
                        int imgwidth = bmp.getWidth();
                        int IMG_SIZE = 200;

                        int zoom = 0;

                        if (imgheight > imgwidth) {
                            double faktor = imgheight / IMG_SIZE;
                            zoom = (int) ((int) imgwidth / faktor);
                            IButton.setImageBitmap(Bitmap.createScaledBitmap(bmp, zoom, 200, false));
                            IButton.setMaxWidth(zoom);
                            IButton.setMaxHeight(200);
                        } else {
                            double faktor = imgwidth / IMG_SIZE;
                            zoom = (int) ((int) imgheight / faktor);
                            IButton.setImageBitmap(Bitmap.createScaledBitmap(bmp, 200, zoom, false));
                            IButton.setMaxWidth(200);
                            IButton.setMaxHeight(zoom);
                        }
                    } */
                }
                if (msgOID == this.OID) {
                    ImgOwningUserName.setGravity(Gravity.END);
                    ImgSendTimeStamp.setGravity(Gravity.END);
                } else {
                    ImgOwningUserName.setGravity(Gravity.START);
                    ImgReadTimeStamp.setGravity(Gravity.START);
                    ImgSendTimeStamp.setGravity(Gravity.START);
                }
                break;
            case VIDEOMSG:
                TextView VidOwningUserName = (TextView) view.findViewById(R.id.VideoOwningUserName);
                VidOwningUserName.setText(OName);
                TextView VidSendTimeStamp = (TextView) view.findViewById(R.id.VideoSendTimeStamp);
                VidSendTimeStamp.setText(new SimpleDateFormat("dd.MM.yyy HH:mm:ss").format(sDate));
                TextView VidReadTimeStamp = (TextView) view.findViewById(R.id.VideoReadTimeStamp);
                VidReadTimeStamp.setText(new SimpleDateFormat("dd.MM.yyy HH:mm:ss").format(rDate));
                ImageButton VButton = (ImageButton) view.findViewById(R.id.VideoImageButton);

                String tmpvid;
                if (directory.endsWith("/")) {
                    tmpvid = Constants.VIDEODIR + "/" + cur.getString(Constants.ID_MESSAGES_VideoMsgValue);
                } else {
                    tmpvid = "/" + Constants.VIDEODIR + "/" + cur.getString(Constants.ID_MESSAGES_VideoMsgValue);
                }

                final String vidfile = directory + tmpvid;
                VButton.setOnClickListener(new View.OnClickListener() {

                    @Override
                    public void onClick(View v) {
                        Intent intent = new Intent();
                        intent.setAction(android.content.Intent.ACTION_VIEW);
                        intent.setDataAndType(Uri.parse(vidfile), "video/*");

                        mContext.startActivity(intent);
                    }
                });

                if (msgOID == this.OID) {
                    VidOwningUserName.setGravity(Gravity.END);
                    VidSendTimeStamp.setGravity(Gravity.END);
                } else {
                    VidOwningUserName.setGravity(Gravity.START);
                    VidReadTimeStamp.setGravity(Gravity.START);
                    VidSendTimeStamp.setGravity(Gravity.START);
                }
                break;
        }
        Log.d(TAG, "end bindView ");
    }

}
