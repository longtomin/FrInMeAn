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
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.widget.TextView;

import de.radiohacks.frinmean.Constants;
import de.radiohacks.frinmean.R;
import de.radiohacks.frinmean.providers.FrinmeanContentProvider;


public class ChatAdapter extends CursorAdapter {

    private static final String TAG = ChatAdapter.class.getSimpleName();
    private Context mContext;

    public ChatAdapter(Context context, Cursor cursor) {
        super(context, cursor, true);
        mContext = context;
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

        int newMsg = count(FrinmeanContentProvider.MESSAES_CONTENT_URI, null, new String[]{Constants.T_MESSAGES_ShowTimestamp + "= 0"});
        if (newMsg > 0) {
            TextView nMsg = (TextView) view.findViewById(R.id.numbernewmessages);
            nMsg.setText(newMsg);
        }
    }

    public int count(Uri uri, String selection, String[] selectionArgs) {
        Cursor cursor = mContext.getContentResolver().query(uri, new String[]{"count(*)"},
                selection, selectionArgs, null);
        if (cursor.getCount() == 0) {
            cursor.close();
            return 0;
        } else {
            cursor.moveToFirst();
            int result = cursor.getInt(0);
            cursor.close();
            return result;
        }
    }

}