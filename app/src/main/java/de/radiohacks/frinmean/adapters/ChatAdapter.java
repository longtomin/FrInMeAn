package de.radiohacks.frinmean.adapters;

import android.content.Context;
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

    private List<Chat> itemList = new ArrayList<Chat>();
    private Context context;

    public ChatAdapter(Context ctx) {
        super(ctx, R.layout.activity_chat);
        this.context = ctx;
    }

    @Override
    public int getCount() {
        if (itemList != null) {
            return itemList.size();
        } else {
            return 0;
        }
    }

    @Override
    public Chat getItem(int position) {
        if (itemList != null) {
            return itemList.get(position);
        } else {
            return null;
        }
    }

    @Override
    public long getItemId(int position) {
        if (itemList != null) {
            return itemList.get(position).hashCode();
        } else {
            return 0;
        }
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

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

        return v;

    }

    public List<Chat> getItemList() {
        return itemList;
    }

    public void setItemList(List<Chat> itemList) {
        this.itemList = itemList;
    }
}