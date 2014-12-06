package de.radiohacks.frinmean.adapters;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import de.radiohacks.frinmean.R;
import de.radiohacks.frinmean.model.Chat;


/**
 * Created by thomas on 27.08.14.
 */
public class ChatAdapter extends ArrayAdapter<Chat> {

    private static final String TAG = ChatAdapter.class.getSimpleName();
    private List<Chat> itemList = new ArrayList<Chat>();
    private Context context;

    public ChatAdapter(Context ctx) {
        super(ctx, R.layout.activity_chat);
        Log.d(TAG, "start ChatAdapter");
        this.context = ctx;
        Log.d(TAG, "end ChatAdapter");
    }

    @Override
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

    }

    public List<Chat> getItemList() {
        return itemList;
    }

    public void setItemList(List<Chat> itemList) {
        this.itemList = itemList;
    }
}