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

import android.app.AlertDialog;
import android.app.ListFragment;
import android.app.LoaderManager;
import android.content.CursorLoader;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TimePicker;

import java.io.File;
import java.util.Calendar;
import java.util.GregorianCalendar;

import de.radiohacks.frinmean.adapters.ChatAdapter;
import de.radiohacks.frinmean.adapters.SyncUtils;
import de.radiohacks.frinmean.providers.FrinmeanContentProvider;
import de.radiohacks.frinmean.service.CustomExceptionHandler;
import de.radiohacks.frinmean.service.MeBaService;


public class ChatFragment extends ListFragment implements LoaderManager.LoaderCallbacks<Cursor> {

    private static final String TAG = ChatFragment.class.getSimpleName();
    private static final int CHAT_LOADER_FULL_ID = 2000;
    private static final int CHAT_LOADER_FORWARD_ID = 3000;
    private ChatAdapter mAdapter;
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
        mode = args.getString(Constants.CHAT_ACTIVITY_MODE);
        userid = args.getInt(Constants.USERID, -1);

        if (mode.equalsIgnoreCase(Constants.CHAT_ACTIVITY_FULL)) {
            setHasOptionsMenu(true);
            int syncFreq = args.getInt(Constants.PrefSyncfrequency, -1);
            if (syncFreq != -1) {
                SyncUtils.StartSyncFreq(syncFreq);
            }
        } else {
            setHasOptionsMenu(false);
            // Needed to show not the Chat where the Message is send from
            sendChatID = args.getInt(Constants.SENDCHATID, -1);
            MessageID = args.getInt(Constants.SENDMSGID, -1);
        }

//        SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(getActivity());
//        String directory = sharedPrefs.getString(Constants.PrefDirectory, "NULL");
        String basedir = Environment.getExternalStorageDirectory().getAbsolutePath() + File.separator + Constants.BASEDIR;
        File baseFile = new File(basedir);
        if (!baseFile.exists()) {
            if (!baseFile.mkdirs()) {
                Log.e(TAG, "Base Directory creation failed");
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

        if (mode.equalsIgnoreCase(Constants.CHAT_ACTIVITY_FULL)) {

            Intent startSingleChat = new Intent(getActivity(), SingleChatActivity.class);
            startSingleChat.putExtra(Constants.CHATID, c.getInt(Constants.ID_CHAT_BADBID));
            startSingleChat.putExtra(Constants.CHATNAME, c.getString(Constants.ID_CHAT_ChatName));
            startSingleChat.putExtra(Constants.OWNINGUSERID, c.getInt(Constants.ID_CHAT_OwningUserID));
            startSingleChat.putExtra(Constants.USERID, userid);
            startActivity(startSingleChat);
        } else {
            Intent insertMsg = new Intent(getActivity(), MeBaService.class);
            insertMsg.setAction(Constants.ACTION_INSERTMESSAGEINTOCHAT);
            insertMsg.putExtra(Constants.CHATID, c.getInt(Constants.ID_CHAT_BADBID));
            insertMsg.putExtra(Constants.FWDCONTENTMESSAGEID, MessageID);
            insertMsg.putExtra(Constants.USERID, userid);
            getActivity().startService(insertMsg);
            getActivity().finish();
        }
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        mAdapter = new ChatAdapter(getActivity(), null);

        setListAdapter(mAdapter);
        setEmptyText(getText(R.string.no_chats));
        if (mode.equalsIgnoreCase(Constants.CHAT_ACTIVITY_FULL)) {
            getLoaderManager().initLoader(CHAT_LOADER_FULL_ID, null, this);
        } else {
            getLoaderManager().initLoader(CHAT_LOADER_FORWARD_ID, null, this);
        }
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        CursorLoader cl = null;

        switch (id) {
            case CHAT_LOADER_FULL_ID:
                cl = new CursorLoader(getActivity(), FrinmeanContentProvider.CHAT_CONTENT_URI,
                        Constants.CHAT_DB_Columns, null, null, null);
                break;
            case CHAT_LOADER_FORWARD_ID:
                cl = new CursorLoader(getActivity(), FrinmeanContentProvider.CHAT_CONTENT_URI, Constants.CHAT_DB_Columns, Constants.T_CHAT_BADBID + " != ?", new String[]{String.valueOf(sendChatID)}, null);
                break;
        }
        return cl;
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        switch (loader.getId()) {
            case CHAT_LOADER_FULL_ID:
                // The asynchronous load is complete and the data
                // is now available for use. Only now can we associate
                // the queried Cursor with the SimpleCursorAdapter.
                mAdapter.swapCursor(data);
                break;
            case CHAT_LOADER_FORWARD_ID:
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

        if (mode.equalsIgnoreCase(Constants.CHAT_ACTIVITY_FULL)) {
            /*
      Options menu used to populate ActionBar.
     */
            Menu mOptionsMenu = menu;
            inflater.inflate(R.menu.chat, menu);
        }
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
            case R.id.option_createchat:
                openCreateChat();
                return true;
            case R.id.action_settings:
                Intent su = new Intent(getActivity(), SettingsActivity.class);
                startActivity(su);
                return true;

        }
        return super.onOptionsItemSelected(item);
    }

    private void openCreateChat() {
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(getActivity());

        alertDialogBuilder.setTitle(R.string.option_createchat);
        alertDialogBuilder.setMessage(R.string.chatname);

        final EditText input = new EditText(getActivity());
        alertDialogBuilder.setView(input);


        alertDialogBuilder.setPositiveButton(R.string.option_createchat
                , new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {

                chatname = input.getText().toString();
                Intent iCreateChat = new Intent(getActivity(), MeBaService.class);
                iCreateChat.setAction(Constants.ACTION_CREATECHAT);
                iCreateChat.putExtra(Constants.CHATNAME, chatname);
                getActivity().startService(iCreateChat);
            }
        });
        alertDialogBuilder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                // cancel the alert box and put a Toast to the user
                dialog.cancel();
            }
        });

        AlertDialog alertDialog = alertDialogBuilder.create();
        // show alert
        alertDialog.show();
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
