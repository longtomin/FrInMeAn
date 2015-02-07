package de.radiohacks.frinmean;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ListFragment;
import android.app.LoaderManager;
import android.content.CursorLoader;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ListView;

import de.radiohacks.frinmean.adapters.ChatAdapter;
import de.radiohacks.frinmean.adapters.SyncUtils;
import de.radiohacks.frinmean.providers.FrinmeanContentProvider;
import de.radiohacks.frinmean.service.MeBaService;


public class ChatFragment extends ListFragment implements LoaderManager.LoaderCallbacks<Cursor> {

    private static final int CHAT_LOADER_ID = 2000;
    private ChatAdapter mAdapter;
    private int syncFreq;
    private int userid;
    private String chatname;

    /**
     * Options menu used to populate ActionBar.
     */
    private Menu mOptionsMenu;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);

        Intent i = getActivity().getIntent();
        userid = i.getIntExtra(Constants.USERID, -1);
        syncFreq = i.getIntExtra(Constants.PrefSyncfrequency, -1);
        if (syncFreq != -1) {
            SyncUtils.ChangeSyncFreq(syncFreq);
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
        // TODO Auto-generated method stub
        super.onListItemClick(l, v, position, id);

        Cursor c = (Cursor) mAdapter.getItem(position);

        Intent i = new Intent(getActivity(), SingleChatActivity.class);
        i.putExtra(Constants.CHATID, c.getInt(Constants.ID_CHAT_BADBID));
        i.putExtra(Constants.CHATNAME, c.getString(Constants.ID_CHAT_ChatName));
        i.putExtra(Constants.OWNINGUSERID, c.getInt(Constants.ID_CHAT_OwningUserID));
        i.putExtra(Constants.USERID, userid);
        startActivity(i);

    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        mAdapter = new ChatAdapter(getActivity(), null);

        setListAdapter(mAdapter);
        setEmptyText(getText(R.string.no_chats));
        getLoaderManager().initLoader(CHAT_LOADER_ID, null, this);
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
        return new CursorLoader(getActivity(), FrinmeanContentProvider.CHAT_CONTENT_URI,
                Constants.CHAT_DB_Columns, null, null, null);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        switch (loader.getId()) {
            case CHAT_LOADER_ID:
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
        mOptionsMenu = menu;
        inflater.inflate(R.menu.chat, menu);
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

    /**
     * Crfate a new anonymous SyncStatusObserver. It's attached to the app's ContentResolver in
     * onResume(), and removed in onPause(). If status changes, it sets the state of the Refresh
     * button. If a sync is active or pending, the Refresh button is replaced by an indeterminate
     * ProgressBar; otherwise, the button itself is displayed.
     */
/*    private SyncStatusObserver mSyncStatusObserver = new SyncStatusObserver() {
        @Override
        public void onStatusChanged(int which) {
            getActivity().runOnUiThread(new Runnable() {

                @Override
                public void run() {
                    // Create a handle to the account that was created by
                    // SyncService.CreateSyncAccount(). This will be used to query the system to
                    // see how the sync status has changed.
                    Account account = GenericAccountService.GetAccount(SyncUtils.ACCOUNT_TYPE);
                    if (account == null) {
                        // GetAccount() returned an invalid value. This shouldn't happen, but
                        // we'll set the status to "not refreshing".
                        setRefreshActionButtonState(false);
                        return;
                    }

                    // Test the ContentResolver to see if the sync adapter is active or pending.
                    // Set the state of the refresh button accordingly.
                    boolean syncActive = ContentResolver.isSyncActive(
                            account, FeedContract.CONTENT_AUTHORITY);
                    boolean syncPending = ContentResolver.isSyncPending(
                            account, FeedContract.CONTENT_AUTHORITY);
                    setRefreshActionButtonState(syncActive || syncPending);
                }
            });
        }
    }; */
}
