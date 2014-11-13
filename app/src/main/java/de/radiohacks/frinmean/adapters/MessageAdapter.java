package de.radiohacks.frinmean.adapters;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;

import java.util.ArrayList;
import java.util.List;

import de.radiohacks.frinmean.R;
import de.radiohacks.frinmean.model.DBMessage;

/**
 * Created by thomas on 06.09.14.
 */
public class MessageAdapter extends ArrayAdapter<DBMessage> {

    private List<DBMessage> itemList = new ArrayList<DBMessage>();
    private Context context;
    private int ChatID;

    public MessageAdapter(Context ctx, int ChatId) {
        super(ctx, R.layout.activity_chat);
        this.context = ctx;
        this.ChatID = ChatId;
    }

    public int getCount() {
        if (itemList != null) {
            return itemList.size();
        } else {
            return 0;
        }
    }

    public DBMessage getItem(int position) {
        if (itemList != null) {
            return itemList.get(position);
        } else {
            return null;
        }
    }

    public long getItemId(int position) {
        if (itemList != null) {
            return itemList.get(position).hashCode();
        } else {
            return 0;
        }
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        DBMessage m = itemList.get(position);

        View v = convertView;
/*        if (v == null) {
            LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            // Prüfen um welchen Messagetyp es sich handelt.
            if (m.getMessageTyp().equalsIgnoreCase(Constants.TYP_TEXT)) {
                v = inflater.inflate(R.layout.messagetextitem, null);
                // Setzen der Werte aus dem Message Objekt in das Item
                TextView Ownertext = (TextView) v.findViewById(R.id.MessageTextOwner);
                Ownertext.setText(m.getOwingUserName());

                // TODO Umrechnen der Unix Zeit auf die lesbare Zeit.
                TextView Sendtext = (TextView) v.findViewById(R.id.MessageTextSendTime);
                //Sendtext.setText(m.getSendTimeStamp());

                TextView Messagetext = (TextView) v.findViewById(R.id.MessageTextValue);
                Messagetext.setText(m.getTextMsgValue());


            }
            if (m.getMessageTyp().equalsIgnoreCase(Constants.TYP_IMAGE)) {
                v = inflater.inflate(R.layout.messageimageitem, null);

                // Setzen der Werte aus dem Message Objekt in das Item
            }
        }*/
        return v;
    }

    public List<DBMessage> getItemList() {
        return itemList;
    }

    public void setItemList(List<DBMessage> itemList) {
        this.itemList = itemList;
    }

    /* protected void loadList() {
        long unixTime = System.currentTimeMillis() / 1000L;
        Cursor dbC = ldb.get(ChatID, unixTime);

        if (dbC.getCount() > 0) {
            int noOfScorer = 0;
            dbC.moveToFirst();
            while ((!dbC.isAfterLast()) && noOfScorer < dbC.getCount()) {
                noOfScorer++;

                DBMessage msg = new DBMessage();

                msg.setOwingUserName(dbC.getString(1));
                msg.setOwningUserID(dbC.getInt(2));
                msg.setMessageTyp(dbC.getString(3));
                msg.setSendTimeStamp(dbC.getLong(4));
                msg.setTextMsgID(dbC.getInt(5));
                msg.setTextMsgValue(dbC.getString(6));
                msg.setImageMsgID(dbC.getInt(7));
                msg.setImageMsgValue(dbC.getString(8));
                msg.setFileMsgID(dbC.getInt(9));
                msg.setFileMsgValue(dbC.getString(10));
                msg.setLocationMsgID(dbC.getInt(11));
                msg.setLocationMsgValue(dbC.getString(12));
                msg.setContactMsgID(dbC.getInt(13));
                msg.setContactMsgValue(dbC.getString(14));

                this.itemList.add(msg);

                dbC.moveToNext();
            }
        }
        ldb.close();
    } */
}
