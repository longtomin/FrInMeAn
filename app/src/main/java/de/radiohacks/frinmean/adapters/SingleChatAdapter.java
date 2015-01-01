package de.radiohacks.frinmean.adapters;

import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
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
    private int OID = 0;
    private String directory;

    public SingleChatAdapter(Context context, Cursor cursor, int InOID, String dir) {
        super(context, cursor, true);
        Log.d(TAG, "start SingleChatAdapter");
        this.OID = InOID;
        this.directory = dir;
        Log.d(TAG, "end SingleChatAdapter");
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
        }
        Log.d(TAG, "end findMsgType");
        return ret;
    }

    @Override
    public int getViewTypeCount() {
        Log.d(TAG, "start & end getViewTypeCount");
        return 5;
    }

    @Override
    public int getItemViewType(int position) {
        Log.d(TAG, "start getItemViewType");
        Cursor cursor = getCursor();
        if (cursor.moveToPosition(position)) {
            Integer ret = findMsgType(cursor.getString(cursor.getColumnIndex(Constants.T_MessageTyp)));
            Log.d(TAG, "end getItemViewType " + ret.toString());
            return ret;
        } else {
            Log.d(TAG, "start getItemViewType -1");
            return -1;
        }
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (!mDataValid) {
            throw new IllegalStateException("this should only be called when the cursor is valid");
        }
        if (!mCursor.moveToPosition(position)) {
            throw new IllegalStateException("couldn't move cursor to position " + position);
        }
        View v;
        if (convertView == null) {
            v = newView(mContext, mCursor, parent);
        } else {
            v = convertView;
        }
        bindView(v, mContext, mCursor);
        return v;
    }

    @Override
    public View newView(Context context, Cursor cur, ViewGroup parent) {
        Log.d(TAG, "start newView");
        View ret = null;

        LayoutInflater li = (LayoutInflater) context
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        String msgType = cur.getString(cur.getColumnIndex(Constants.T_MessageTyp));
        int msgOID = cur.getInt(cur.getColumnIndex(Constants.T_OwningUserID));

        switch (findMsgType(msgType)) {
            case TEXTMSG:
                ret = li.inflate(R.layout.textmsg, parent, false);
                if (msgOID == this.OID) {
                    ret.setBackgroundResource(R.drawable.bubble_green);
                    ret.setPadding(0, 0, 20, 0);
                    ((TableLayout) ret).setGravity(Gravity.END);
                } else {
                    ret.setBackgroundResource(R.drawable.bubble_yellow);
                    ret.setPadding(20, 0, 0, 0);
                    ((TableLayout) ret).setGravity(Gravity.START);
                }
                break;
            case IMAGEMSG:
                ret = li.inflate(R.layout.imagemsg, parent, false);
                if (msgOID == this.OID) {
                    ret.setBackgroundResource(R.drawable.bubble_green);
                    ret.setPadding(0, 0, 20, 0);
                    ((TableLayout) ret).setGravity(Gravity.END);
                } else {
                    ret.setBackgroundResource(R.drawable.bubble_yellow);
                    ret.setPadding(20, 0, 0, 0);
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
        String msgType = cur.getString(cur.getColumnIndex(Constants.T_MessageTyp));
        int msgOID = cur.getInt(cur.getColumnIndex(Constants.T_OwningUserID));
        long sstamp = cur.getLong(cur.getColumnIndex(Constants.T_SendTimestamp));
        Date sDate = new java.util.Date(sstamp * 1000);
        long rstamp = cur.getLong(cur.getColumnIndex(Constants.T_ReadTimestamp));
        Date rDate = new java.util.Date(rstamp * 1000);
        String OName = cur.getString(cur.getColumnIndex(Constants.T_OwningUserName));

        switch (findMsgType(msgType)) {
            case TEXTMSG:
                TextView TxtOwningUserName = (TextView) view.findViewById(R.id.TextOwningUserName);
                TxtOwningUserName.setText(OName);
                TextView TxtSendTimeStamp = (TextView) view.findViewById(R.id.TextSendTimeStamp);
                TxtSendTimeStamp.setText(new SimpleDateFormat("dd.MM.yyy HH:mm:ss").format(sDate));
                TextView TxtReadTimeStamp = (TextView) view.findViewById(R.id.TextReadTimeStamp);
                TxtReadTimeStamp.setText(new SimpleDateFormat("dd.MM.yyy HH:mm:ss").format(rDate));
                TextView TextMessage = (TextView) view.findViewById(R.id.TextTextMessage);
                TextMessage.setText(cur.getString(cur.getColumnIndex(Constants.T_TextMsgValue)));

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
                ImgSendTimeStamp.setText(new SimpleDateFormat("DD.MM.yyyy HH:mm:ss").format(sDate));
                TextView ImgReadTimeStamp = (TextView) view.findViewById(R.id.ImageReadTimeStamp);
                ImgReadTimeStamp.setText(new SimpleDateFormat("DD.MM.yyyy HH:mm:ss").format(rDate));
                ImageButton IButton = (ImageButton) view.findViewById(R.id.ImageImageButton);

                IButton.setOnClickListener(new View.OnClickListener() {

                    @Override
                    public void onClick(View v) {
                        //TODO Image anzeigen im fullscreen Modus
                    }
                });

                String imgfile = directory;
                if (imgfile.endsWith("/")) {
                    imgfile += Constants.IMAGEDIR + "/" + cur.getString(cur.getColumnIndex(Constants.T_ImageMsgValue));
                } else {
                    imgfile += "/" + Constants.IMAGEDIR + "/" + cur.getString(cur.getColumnIndex(Constants.T_ImageMsgValue));
                }

                File ifile = new File(imgfile);
                if (ifile.exists()) {
                    Bitmap bmp = BitmapFactory.decodeFile(imgfile);
                    int imgheight = bmp.getHeight();
                    int imgwidth = bmp.getWidth();
                    int IMG_SIZE = 200;

                    int zoom = 0;

                    if (imgheight > imgwidth) {
                        double faktor = imgheight / IMG_SIZE;
                        zoom = (int) ((int) imgwidth / faktor);
                        IButton.setImageBitmap(Bitmap.createScaledBitmap(bmp, zoom, 200, false));
                        IButton.setMinimumWidth(zoom);
                        IButton.setMinimumHeight(200);
                    } else {
                        double faktor = imgwidth / IMG_SIZE;
                        zoom = (int) ((int) imgheight / faktor);
                        IButton.setImageBitmap(Bitmap.createScaledBitmap(bmp, 200, zoom, false));
                        IButton.setMinimumWidth(200);
                        IButton.setMinimumHeight(zoom);
                    }
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
        }
        Log.d(TAG, "end bindView ");
    }
}
