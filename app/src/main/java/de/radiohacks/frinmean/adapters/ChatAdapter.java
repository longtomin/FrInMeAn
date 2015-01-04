package de.radiohacks.frinmean.adapters;

import android.content.Context;
import android.database.Cursor;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.widget.TextView;

import de.radiohacks.frinmean.Constants;
import de.radiohacks.frinmean.R;


/**
 * Created by thomas on 27.08.14.
 */
public class ChatAdapter extends CursorAdapter {

    private static final String TAG = ChatAdapter.class.getSimpleName();

    public ChatAdapter(Context context, Cursor cursor) {
        super(context, cursor, true);
        Log.d(TAG, "start & End ChatAdapter");
    }

/*    @Override
    public int getCount() {
        Log.d(TAG, "start getCount");
        if (itemList != null) {
            Integer ret = itemList.size();
            Log.d(TAG, "end  ChatAdapter " + ret.toString());
            return ret;
        } else {
            Log.d(TAG, "end ChatAdapter 0");
            return 0;
        }
    }

    @Override
    public Chat getItem(int position) {
        Log.d(TAG, "start getItem");

        if (itemList != null) {
            Log.d(TAG, "end getItem != null");
            return itemList.get(position);
        } else {
            Log.d(TAG, "end getItem = null");
            return null;
        }
    }

    @Override
    public long getItemId(int position) {
        Log.d(TAG, "start getItemId");
        if (itemList != null) {
            Integer ret = itemList.get(position).hashCode();
            Log.d(TAG, "start getItemId " + ret.toString());
            return ret;
        } else {
            Log.d(TAG, "end getItemId 0");
            return 0;
        }
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        Log.d(TAG, "start getView");
        View v = convertView;
        if (v == null) {
            LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            v = inflater.inflate(R.layout.chatitem, null);
        }

        Chat c = itemList.get(position);
        TextView text = (TextView) v.findViewById(R.id.chatName);
        String tmp = c.getChatname();
        text.setText(tmp);

        TextView text1 = (TextView) v.findViewById(R.id.owningUserName);
        text1.setText(c.getOwningUser().getOwningUserName());

        Log.d(TAG, "end getView");
        return v;

    } */

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        return inflater.inflate(R.layout.chatitem, null);
    }

    @Override
    public void bindView(View view, Context context, Cursor cursor) {
        TextView text = (TextView) view.findViewById(R.id.chatName);
        String tmp = cursor.getString(Constants.ID_CHAT_ChatName);
        text.setText(tmp);

        TextView text1 = (TextView) view.findViewById(R.id.owningUserName);
        text1.setText(cursor.getString(Constants.ID_CHAT_OwningUserName));
    }
}