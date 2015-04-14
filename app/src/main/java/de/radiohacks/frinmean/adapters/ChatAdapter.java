package de.radiohacks.frinmean.adapters;

import android.annotation.SuppressLint;
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


public class ChatAdapter extends CursorAdapter {

    private static final String TAG = ChatAdapter.class.getSimpleName();

    public ChatAdapter(Context context, Cursor cursor) {
        super(context, cursor, true);
        Log.d(TAG, "start & End ChatAdapter");
    }

    @SuppressLint("InflateParams")
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