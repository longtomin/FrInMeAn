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
import java.text.SimpleDateFormat;
import java.util.Date;

import de.radiohacks.frinmean.Constants;
import de.radiohacks.frinmean.R;


public class UserAdapter extends CursorAdapter {

    private static final String TAG = UserAdapter.class.getSimpleName();
    //private ContentResolver mContentResolver;
    private Context mContext;

    public UserAdapter(Context context, Cursor cursor) {
        super(context, cursor, true);
        //this.mContentResolver = context.getContentResolver();
        this.mContext = context;
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
        return inflater.inflate(R.layout.useritem, null);
    }

    @Override
    public void bindView(View view, Context context, Cursor cursor) {

        long astamp = cursor.getLong(Constants.ID_USER_AuthenticationTime);
        Date aDate = new java.util.Date(astamp * 1000);
        TextView Authentication = (TextView) view.findViewById(R.id.UserItemLastAuthenticate);
        Authentication.setText(new SimpleDateFormat(Constants.DATETIMEFORMAT).format(aDate));

        TextView Username = (TextView) view.findViewById(R.id.UserItemUsername);
        String u1 = cursor.getString(Constants.ID_USER_Username);
        Username.setText(cursor.getString(Constants.ID_USER_Username));

        TextView UserPhonename = (TextView) view.findViewById(R.id.UserItemPhoneUsername);
        String p1 = cursor.getString(Constants.ID_USER_PhoneUsername);
        UserPhonename.setText(cursor.getString(Constants.ID_USER_PhoneUsername));

        final String UserImageFile = cursor.getString(Constants.ID_USER_IconValue);
        if ((UserImageFile != null) && (!UserImageFile.isEmpty())) {
            ImageView UserImage = (ImageView) view.findViewById(R.id.UserItemUserImage);
            UserImage.setOnClickListener(new View.OnClickListener() {

                @Override
                public void onClick(View v) {
                    Intent intent = new Intent();
                    intent.setAction(android.content.Intent.ACTION_VIEW);
                    File file = new File(UserImageFile);
                    intent.setDataAndType(Uri.fromFile(file), "image/*");
                    mContext.startActivity(intent);
                }
            });


            File ifileOwn = new File(UserImageFile);
            if (ifileOwn.exists()) {
                String fname = ifileOwn.getAbsolutePath();
                BitmapFactory.Options options = new BitmapFactory.Options();
                options.inJustDecodeBounds = true;
                BitmapFactory.decodeFile(fname, options);

                options.inSampleSize = calculateInSampleSize(options, 50, 50);

                // Decode bitmap with inSampleSize set
                options.inJustDecodeBounds = false;
                Bitmap bmp = BitmapFactory.decodeFile(fname, options);

                UserImage.setImageBitmap(bmp);
                UserImage.setMinimumWidth(options.outWidth);
                UserImage.setMinimumHeight(options.outHeight);
                UserImage.setMaxWidth(options.outWidth);
                UserImage.setMaxHeight(options.outHeight);
            }
        }
    }
}