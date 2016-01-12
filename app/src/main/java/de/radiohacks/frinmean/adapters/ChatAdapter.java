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
import android.content.ContentProviderClient;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.io.File;

import de.radiohacks.frinmean.Constants;
import de.radiohacks.frinmean.R;
import de.radiohacks.frinmean.providers.FrinmeanContentProvider;

import static de.radiohacks.frinmean.Constants.MESSAGES_DB_Columns;


public class ChatAdapter extends CursorAdapter {

    private static final String TAG = ChatAdapter.class.getSimpleName();
    private ContentResolver mContentResolver;
    private Context mContext;

    public ChatAdapter(Context context, Cursor cursor) {
        super(context, cursor, true);
        this.mContext = context;
        this.mContentResolver = context.getContentResolver();
        Log.d(TAG, "start & End ChatAdapter");
    }

    private static int calculateInSampleSize(
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

    @SuppressLint("InflateParams")
    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        return inflater.inflate(R.layout.chatitem, null);
    }

    @Override
    public void bindView(View view, Context context, Cursor cursor) {

        ContentProviderClient client = mContentResolver.acquireContentProviderClient(FrinmeanContentProvider.MESSAGES_CONTENT_URI);
        Cursor csh = client.getLocalContentProvider().query(FrinmeanContentProvider.MESSAGES_CONTENT_URI, MESSAGES_DB_Columns,
                Constants.T_MESSAGES_ChatID + " = ? and " + Constants.T_MESSAGES_ShowTimestamp + " = ?", new String[]{String.valueOf(cursor.getInt(Constants.ID_CHAT_BADBID)), "0"}, null);
        int NumShow = csh.getCount();
        csh.close();

        if (NumShow > 0) {
            TextView text2 = (TextView) view.findViewById(R.id.ChatItemUnreadMessages);
            text2.setText(String.valueOf(NumShow));
        }

        TextView text = (TextView) view.findViewById(R.id.ChatItemChatName);
        String tmp = cursor.getString(Constants.ID_CHAT_ChatName);
        text.setText(tmp);

        TextView text1 = (TextView) view.findViewById(R.id.ChatItemowningUserName);
        text1.setText(cursor.getString(Constants.ID_CHAT_OwningUserName));

        final String ChatImageFile = cursor.getString(Constants.ID_CHAT_IconValue);
        if ((ChatImageFile != null) && (!ChatImageFile.isEmpty())) {
            ImageView ChatItem = (ImageView) view.findViewById(R.id.ChatItemChatImage);
            ChatItem.setOnClickListener(new View.OnClickListener() {

                @Override
                public void onClick(View v) {
                    Intent intent = new Intent();
                    intent.setAction(android.content.Intent.ACTION_VIEW);
                    File file = new File(ChatImageFile);
                    intent.setDataAndType(Uri.fromFile(file), "image/*");
                    mContext.startActivity(intent);
                }
            });


            File ifileOwn = new File(ChatImageFile);
            if (ifileOwn.exists()) {
                String fname = ifileOwn.getAbsolutePath();
                BitmapFactory.Options options = new BitmapFactory.Options();
                options.inJustDecodeBounds = true;
                BitmapFactory.decodeFile(fname, options);

                options.inSampleSize = calculateInSampleSize(options, 50, 50);

                // Decode bitmap with inSampleSize set
                options.inJustDecodeBounds = false;
                Bitmap bmp = BitmapFactory.decodeFile(fname, options);

                ChatItem.setImageBitmap(bmp);
                ChatItem.setMinimumWidth(options.outWidth);
                ChatItem.setMinimumHeight(options.outHeight);
                ChatItem.setMaxWidth(options.outWidth);
                ChatItem.setMaxHeight(options.outHeight);
            }
        }
    }
}