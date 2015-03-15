package de.radiohacks.frinmean;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ListFragment;
import android.app.LoaderManager;
import android.content.CursorLoader;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.Loader;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ListView;

import de.radiohacks.frinmean.adapters.ChatAdapter;
import de.radiohacks.frinmean.adapters.SyncUtils;
import de.radiohacks.frinmean.providers.FrinmeanContentProvider;
import de.radiohacks.frinmean.service.CustomExceptionHandler;
import de.radiohacks.frinmean.service.MeBaService;


public class ChatFragment extends ListFragment implements LoaderManager.LoaderCallbacks<Cursor> {

    private static final int CHAT_LOADER_FULL_ID = 2000;
    private static final int CHAT_LOADER_FORWARD_ID = 3000;
    private ChatAdapter mAdapter;
    private int syncFreq;
    private int userid;
    private String chatname;
    private String mode;
    private int sendChatID;
    private int MessageID;
    private String MessageType;
    private String MessageContent;

    /**
     * Options menu used to populate ActionBar.
     */
    private Menu mOptionsMenu;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Intent i = getActivity().getIntent();
        mode = i.getStringExtra(Constants.CHAT_ACTIVITY_MODE);
        userid = i.getIntExtra(Constants.USERID, -1);

        SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(getActivity());
        String directory = sharedPrefs.getString("prefDirectory", "NULL");

        if (mode.equalsIgnoreCase(Constants.CHAT_ACTIVITY_FULL)) {
            setHasOptionsMenu(true);

            syncFreq = i.getIntExtra(Constants.PrefSyncfrequency, -1);
            if (syncFreq != -1) {
                SyncUtils.ChangeSyncFreq(syncFreq);
            }


        } else {
            // Needed to show not the Chat where the Message is send from
            sendChatID = i.getIntExtra(Constants.SENDCHATID, -1);
            // MessageID und Message Type kommen vom original chat, chat ID kommt vom gewählten Chat
            MessageID = i.getIntExtra(Constants.FWDCONTENTMESSAGEID, -1);
            MessageType = i.getStringExtra(Constants.MESSAGETYPE);
            MessageContent = i.getStringExtra(Constants.FWDCONTENTMESSAGE);
        }

        if (!(Thread.getDefaultUncaughtExceptionHandler() instanceof CustomExceptionHandler)) {
            Thread.setDefaultUncaughtExceptionHandler(new CustomExceptionHandler(directory));
        }
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);

        // Create account, if needed
        // SyncUtils.CreateSyncAccount(activity);
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
            insertMsg.putExtra(Constants.MESSAGETYPE, MessageType);
            insertMsg.putExtra(Constants.USERID, userid);
            insertMsg.putExtra(Constants.FWDCONTENTMESSAGE, MessageContent);
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

    /* @Override
    public void onResume() {
        super.onResume();
        mSyncStatusObserver.onStatusChanged(0);

        // Watch for sync state changes
        final int mask = ContentResolver.SYNC_OBSERVER_TYPE_PENDING |
                ContentResolver.SYNC_OBSERVER_TYPE_ACTIVE;
        mSyncObserverHandle = ContentResolver.addStatusChangeListener(mask, mSyncStatusObserver);
    }

    @Override
    public void onPause() {
        super.onPause();
        if (mSyncObserverHandle != null) {
            ContentResolver.removeStatusChangeListener(mSyncObserverHandle);
            mSyncObserverHandle = null;
        }
    }*/

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
            mOptionsMenu = menu;
            inflater.inflate(R.menu.chat, menu);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.

        switch (item.getItemId()) {
            case R.id.action_createchat:
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

        alertDialogBuilder.setTitle(R.string.action_createchat);
        alertDialogBuilder.setMessage(R.string.chatname);

        final EditText input = new EditText(getActivity());
        alertDialogBuilder.setView(input);


        alertDialogBuilder.setPositiveButton(R.string.action_createchat
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
}
