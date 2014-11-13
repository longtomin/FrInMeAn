package de.radiohacks.frinmean.adapters;

import android.content.Context;
import android.database.Cursor;
import android.support.v4.widget.CursorAdapter;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.text.SimpleDateFormat;
import java.util.Date;

import de.radiohacks.frinmean.Constants;
import de.radiohacks.frinmean.R;

/**
 * Created by thomas on 25.10.14.
 */
public class SingleChatAdapter extends CursorAdapter {

    public static final int LAYOUT_ID = R.layout.activity_single_chat;
    private static final int TEXTMSG = 0;
    private static final int IMAGEMSG = 1;
    private static final int FILEMSG = 2;
    private static final int CONTACTMSG = 3;
    private static final int LOCATIONMSG = 4;
    private int OID = 0;
    private Context mContext;
    private Cursor mCursor;

    public SingleChatAdapter(Context context, Cursor cursor, int InOID) {
        super(context, cursor, true);
        this.mContext = context;
        this.mCursor = cursor;
        this.OID = InOID;
    }

    private int findMsgType(String in) {
        int ret = -1;
        if (in.equalsIgnoreCase(Constants.TYP_TEXT)) {
            ret = TEXTMSG;
        }
        if (in.equalsIgnoreCase(Constants.TYP_IMAGE)) {
            ret = IMAGEMSG;
        }
        if (in.equalsIgnoreCase(Constants.TYP_FILE)) {
            ret = FILEMSG;
        }
        if (in.equalsIgnoreCase(Constants.TYP_CONTACT)) {
            ret = CONTACTMSG;
        }
        if (in.equalsIgnoreCase(Constants.TYP_LOCATION)) {
            ret = LOCATIONMSG;
        }

        return ret;
    }

    @Override
    public int getViewTypeCount() {
        return 5;
    }

    @Override
    public int getItemViewType(int position) {

        Cursor cursor = getCursor();
        if (cursor.moveToPosition(position)) {
            return findMsgType(cursor.getString(cursor.getColumnIndex(Constants.T_MessageTyp)));
        } else {
            return -1;
        }
    }

    @Override
    public View newView(Context context, Cursor cur, ViewGroup parent) {
        View ret = null;
        int msgTypeInt = -1;

        LayoutInflater li = (LayoutInflater) context
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        String msgType = new String(cur.getString(cur.getColumnIndex(Constants.T_MessageTyp)));
        int msgOID = cur.getInt(cur.getColumnIndex(Constants.T_OwningUserID));

        switch (findMsgType(msgType)) {
            case TEXTMSG:
                ret = li.inflate(R.layout.textmsg, parent, false);
                if (msgOID == this.OID) {
                    ret.setBackgroundResource(R.drawable.bubble_yellow);
                } else {
                    ret.setBackgroundResource(R.drawable.bubble_green);
                }
                break;
            case IMAGEMSG:
                ret = li.inflate(R.layout.imagemsg, parent, false);
                if (msgOID == this.OID) {
                    ret.setBackgroundResource(R.drawable.bubble_yellow);
                    ((RelativeLayout) ret).setGravity(Gravity.END);
                } else {
                    ret.setBackgroundResource(R.drawable.bubble_green);
                    ((RelativeLayout) ret).setGravity(Gravity.START);
                }
                break;
        }
        return ret;
    }

    @Override
    public void bindView(View view, Context context, Cursor cur) {

        String msgType = new String(cur.getString(cur.getColumnIndex(Constants.T_MessageTyp)));
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

                TxtOwningUserName.setGravity(Gravity.LEFT);
                TextMessage.setGravity(Gravity.LEFT);

                RelativeLayout bg = (RelativeLayout) view.findViewById(R.id.TextMsgLayout);
                if (msgOID == this.OID) {
                    bg.setHorizontalGravity(Gravity.END);
                    bg.setVerticalGravity(Gravity.END);
                } else {
                    // bg.setGravity(Gravity.LEFT);
                    bg.setHorizontalGravity(Gravity.START);
                    bg.setVerticalGravity(Gravity.START);
                }
                break;
            case IMAGEMSG:
                TextView ImgOwningUserName = (TextView) view.findViewById(R.id.ImageOwningUserName);
                ImgOwningUserName.setText(OName);
                TextView ImgSendTimeStamp = (TextView) view.findViewById(R.id.ImageSendTimeStamp);
                ImgSendTimeStamp.setText(new SimpleDateFormat("DD.MM.yyyy HH:mm:ss").format(sDate));
                TextView ImgReadTimeStamp = (TextView) view.findViewById(R.id.ImageReadTimeStamp);
                ImgReadTimeStamp.setText(rDate.toString());
                ImageButton IButton = (ImageButton) view.findViewById(R.id.ImageImageButton);

                if (msgOID == this.OID) {
                    ImgOwningUserName.setGravity(Gravity.RIGHT);
                    ImgReadTimeStamp.setGravity(Gravity.RIGHT);
                    ImgSendTimeStamp.setGravity(Gravity.RIGHT);
                } else {
                    ImgOwningUserName.setGravity(Gravity.LEFT);
                    ImgReadTimeStamp.setGravity(Gravity.LEFT);
                    ImgSendTimeStamp.setGravity(Gravity.LEFT);
                }
                break;
        }
    }
}
