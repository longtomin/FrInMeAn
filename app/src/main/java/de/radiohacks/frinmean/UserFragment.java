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

package de.radiohacks.frinmean;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ListFragment;
import android.app.LoaderManager;
import android.content.CursorLoader;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.DatePicker;
import android.widget.ListView;
import android.widget.TimePicker;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Calendar;
import java.util.GregorianCalendar;

import de.radiohacks.frinmean.adapters.UserAdapter;
import de.radiohacks.frinmean.providers.FrinmeanContentProvider;
import de.radiohacks.frinmean.service.CustomExceptionHandler;
import de.radiohacks.frinmean.service.MeBaService;


public class UserFragment extends ListFragment implements LoaderManager.LoaderCallbacks<Cursor> {

    private static final String TAG = UserFragment.class.getSimpleName();
    private static final int USER_LOADER = 1000;
    private static final int REQUEST_USER_THUMBNAIL = 1;
    private String icndir;
    private UserAdapter mAdapter;
    private int userid;
    private String chatname;
    private String mode;
    private int sendChatID;
    private long MessageID;
    private String MessageType;
    private String MessageContent;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Bundle args = getArguments();
        setHasOptionsMenu(true);

        String basedir = Environment.getExternalStorageDirectory().getAbsolutePath() + File.separator + Constants.BASEDIR;
        File baseFile = new File(basedir);
        if (!baseFile.exists()) {
            if (!baseFile.mkdirs()) {
                Log.e(TAG, "Base Directory creation failed");
            }
        }
        icndir = basedir + File.separator + Constants.ICONDIR + File.separator;
        File icnFile = new File(icndir);
        if (!icnFile.exists()) {
            if (!icnFile.mkdir()) {
                Log.e(TAG, "Icon Directory creation failed");
            }
        }
        if (!(Thread.getDefaultUncaughtExceptionHandler() instanceof CustomExceptionHandler)) {
            Thread.setDefaultUncaughtExceptionHandler(new CustomExceptionHandler(baseFile.toString()));
        }
    }

    @Override
    public void onListItemClick(ListView l, View v, int position, long id) {
        super.onListItemClick(l, v, position, id);

        Cursor c = (Cursor) mAdapter.getItem(position);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        mAdapter = new UserAdapter(getActivity(), null);

        setListAdapter(mAdapter);
        setEmptyText(getText(R.string.no_users));
        getLoaderManager().initLoader(USER_LOADER, null, this);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        CursorLoader cl = null;

        switch (id) {
            case USER_LOADER:
                cl = new CursorLoader(getActivity(), FrinmeanContentProvider.USER_CONTENT_URI,
                        Constants.USER_DB_Columns, null, null, null);
                break;
        }
        return cl;
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        switch (loader.getId()) {
            case USER_LOADER:
                // The asynchronous load is complete and the data
                // is now available for use. Only now can we associate
                // the queried Cursor with the SimpleCursorAdapter.
                mAdapter.swapCursor(data);
                break;
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        mAdapter.swapCursor(null);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);

            /*
      Options menu used to populate ActionBar.
     */
        Menu mOptionsMenu = menu;
        inflater.inflate(R.menu.user, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.

        switch (item.getItemId()) {
            case R.id.action_show_refresh:
                openRefresh();
                return true;
            case R.id.option_syncuser:
                Intent iRefresh = new Intent(getActivity(), MeBaService.class);
                iRefresh.setAction(Constants.ACTION_SYNCUSER);
                getActivity().startService(iRefresh);
                return true;
            case R.id.action_settings:
                Intent su = new Intent(getActivity(), SettingsActivity.class);
                startActivity(su);
                return true;
            case R.id.action_setusericon:
                final Intent chooserIntent = new Intent(
                        getActivity(),
                        SelectIconActivity.class);
                chooserIntent.putExtra(Constants.THUMBNAIL_TYPE, Constants.THUMBNAIL_USER);
                startActivityForResult(chooserIntent, REQUEST_USER_THUMBNAIL);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == Activity.RESULT_OK) {
            if (requestCode == REQUEST_USER_THUMBNAIL) {
                if (data != null) {
                    if (data.getExtras() != null) {
                        Bundle extras = data.getExtras();
                        Bitmap ownpic = extras.getParcelable("data");

                        File owniconfile = new File(icndir, "ownicon.png");
                        FileOutputStream fOut = null;
                        try {
                            fOut = new FileOutputStream(owniconfile);
                            ownpic.compress(Bitmap.CompressFormat.PNG, 85, fOut);
                            fOut.flush();
                            fOut.close();
                        } catch (FileNotFoundException e) {
                            e.printStackTrace();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                        Intent picintent = new Intent(getActivity(), MeBaService.class);
                        picintent.setAction(Constants.ACTION_SENDUSERICON);
                        picintent.putExtra(Constants.IMAGELOCATION, owniconfile.getAbsolutePath());
                        getActivity().startService(picintent);
                    }
                }
            }
        }
    }

    private void openRefresh() {
        final View dialogView = View.inflate(getActivity(), R.layout.date_time_picker, null);
        final AlertDialog alertDialog = new AlertDialog.Builder(getActivity()).create();

        dialogView.findViewById(R.id.date_time_set).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                DatePicker datePicker = (DatePicker) dialogView.findViewById(R.id.date_picker);
                TimePicker timePicker = (TimePicker) dialogView.findViewById(R.id.time_picker);

                Calendar calendar = new GregorianCalendar(datePicker.getYear(),
                        datePicker.getMonth(),
                        datePicker.getDayOfMonth(),
                        timePicker.getCurrentHour(),
                        timePicker.getCurrentMinute());

                long time = calendar.getTimeInMillis() / 1000;
                Intent iRefresh = new Intent(getActivity(), MeBaService.class);
                iRefresh.setAction(Constants.ACTION_REFRESH);
                iRefresh.putExtra(Constants.TIMESTAMP, time);
                getActivity().startService(iRefresh);

                alertDialog.dismiss();
            }
        });
        alertDialog.setView(dialogView);
        alertDialog.show();
    }
}
