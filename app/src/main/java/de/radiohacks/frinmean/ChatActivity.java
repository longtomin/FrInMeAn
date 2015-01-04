package de.radiohacks.frinmean;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.LoaderManager;
import android.content.CursorLoader;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.widget.SwipeRefreshLayout;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ListView;

import de.radiohacks.frinmean.adapters.ChatAdapter;
import de.radiohacks.frinmean.providers.FrinmeanContentProvider;
import de.radiohacks.frinmean.service.MeBaService;


public class ChatActivity extends Activity implements LoaderManager.LoaderCallbacks<Cursor> {

    private static final int CHAT_LOADER_ID = 2000;
    // private ListChatStateReceiver mListChatStateReceiver = new ListChatStateReceiver();
    private ChatAdapter mAdapter;
    //private String username;
    //private String password;
    //private String server;
    private int userid;
    private String chatname;
    private SwipeRefreshLayout swipeLayout;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        getLoaderManager().initLoader(CHAT_LOADER_ID, null, this);
        mAdapter = new ChatAdapter(this, null);
        ListView list = (ListView) findViewById(R.id.chatlist);
        list.setAdapter(mAdapter);

        list.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view,
                                    int position, long id) {
                Cursor cursor = (Cursor) parent.getItemAtPosition(position);

                Intent i = new Intent(ChatActivity.this, SingleChatActivity.class);
                i.putExtra(Constants.CHATID, cursor.getInt(Constants.ID_CHAT_BADBID));
                i.putExtra(Constants.CHATNAME, cursor.getString(Constants.ID_CHAT_ChatName));
                i.putExtra(Constants.OWNINGUSERID, cursor.getInt(Constants.ID_CHAT_OwningUserID));
                i.putExtra(Constants.OWNINGUSERNAME, cursor.getString(Constants.ID_CHAT_OwningUserName));
                i.putExtra(Constants.USERID, userid);
                startActivity(i);
            }
        });

        Intent i = getIntent();
        userid = i.getIntExtra(Constants.USERID, -1);
        //getPreferenceInfo();

        /*IntentFilter statusIntentFilter = new IntentFilter(
                Constants.BROADCAST_LISTCHAT);
        statusIntentFilter.addAction(Constants.BROADCAST_CREATECHAT);

        // Sets the filter's category to DEFAULT
        statusIntentFilter.addCategory(Intent.CATEGORY_DEFAULT);

        mListChatStateReceiver = new ListChatStateReceiver();

        // Registers the DownloadStateReceiver and its intent filters
        LocalBroadcastManager.getInstance(this).registerReceiver(
                mListChatStateReceiver,
                statusIntentFilter); */

        //Start MeBaService
        Intent intentMyIntentService = new Intent(this, MeBaService.class);
        intentMyIntentService.setAction(Constants.ACTION_LISTCHAT);
        startService(intentMyIntentService);

    }

    /*protected void getPreferenceInfo() {
        SharedPreferences sharedPrefs = PreferenceManager
                .getDefaultSharedPreferences(this);

        server = sharedPrefs.getString("prefServername", "NULL");
        username = sharedPrefs.getString("prefUsername", "NULL");
        password = sharedPrefs.getString("prefPassword", "NULL");
    }*/

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.chat, menu);
        return true;
    }

    /*@Override
    protected void onResume() {
        super.onResume();
        IntentFilter infi = new IntentFilter(Constants.BROADCAST_LISTCHAT);
        infi.addAction(Constants.BROADCAST_CREATECHAT);

        registerReceiver(mListChatStateReceiver, infi);
    } */

    /* @Override
    protected void onPause() {
        super.onPause();
        if (mListChatStateReceiver != null) {
            unregisterReceiver(mListChatStateReceiver);
        }
    }*/


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
                Intent su = new Intent(ChatActivity.this, SettingsActivity.class);
                startActivity(su);
                return true;

        }
        return super.onOptionsItemSelected(item);
    }

    private void openCreateChat() {
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(ChatActivity.this);

        alertDialogBuilder.setTitle(R.string.action_createchat);
        alertDialogBuilder.setMessage(R.string.chatname);

        final EditText input = new EditText(this);
        alertDialogBuilder.setView(input);


        alertDialogBuilder.setPositiveButton(R.string.action_createchat
                , new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {

                chatname = input.getText().toString();
                Intent iCreateChat = new Intent(ChatActivity.this, MeBaService.class);
                iCreateChat.setAction(Constants.ACTION_CREATECHAT);
                iCreateChat.putExtra(Constants.CHATNAME, chatname);
                startService(iCreateChat);
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

/*    @Override
    protected void onListItemClick(ListView list, View view, int position, long id) {
        super.onListItemClick(list, view, position, id);

        Cursor c = (Cursor) mAdapter.getItem(position);
        Intent i = new Intent(ChatActivity.this, SingleChatActivity.class);
        i.putExtra(Constants.CHATID, c.getInt(Constants.ID_CHAT_ChatID));
        i.putExtra(Constants.CHATNAME, c.getString(Constants.ID_CHAT_ChatName));
        i.putExtra(Constants.OWNINGUSERID, c.getInt(Constants.ID_CHAT_OwningUserID));
        i.putExtra(Constants.OWNINGUSERNAME, c.getString(Constants.ID_CHAT_OwningUserName));
        i.putExtra(Constants.USERID, userid);
        startActivity(i);
    } */

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        return new CursorLoader(ChatActivity.this, FrinmeanContentProvider.CHAT_CONTENT_URI,
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

    /* public class ListChatStateReceiver extends BroadcastReceiver {

        public ListChatStateReceiver() {
            super();

            // prevents instantiation by other packages.
        }

        @Override
        public void onReceive(Context context, Intent intent) {

            if (intent.getAction().equalsIgnoreCase(Constants.BROADCAST_LISTCHAT)) {
                try {
                    String ret = intent.getStringExtra(Constants.BROADCAST_DATA);
                    Serializer serializer = new Persister();
                    Reader reader = new StringReader(ret);

                    OutListChat res = serializer.read(OutListChat.class, reader, false);
                    if (res == null) {
                        ErrorHelper eh = new ErrorHelper(ChatActivity.this);
                        eh.CheckErrorText(Constants.ERROR_NO_CONNECTION_TO_SERVER);
                    } else {
                        if (res.getErrortext() != null && !res.getErrortext().isEmpty()) {
                            ErrorHelper eh = new ErrorHelper(ChatActivity.this);
                            eh.CheckErrorText(res.getErrortext());
                        } else {
                            if (res.getChat() != null && !res.getChat().isEmpty()) {
                                mAdapter.setItemList(res.getChat());
                                mAdapter.notifyDataSetChanged();
                            }
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            } else if (intent.getAction().equalsIgnoreCase(Constants.BROADCAST_CREATECHAT)) {
                try {
                    String ret = intent.getStringExtra(Constants.BROADCAST_DATA);
                    Serializer serializer = new Persister();
                    Reader reader = new StringReader(ret);

                    OutCreateChat res = serializer.read(OutCreateChat.class, reader, false);
                    if (res == null) {
                        ErrorHelper eh = new ErrorHelper(ChatActivity.this);
                        eh.CheckErrorText(Constants.ERROR_NO_CONNECTION_TO_SERVER);
                    } else {
                        if (res.getErrortext() != null && !res.getErrortext().isEmpty()) {
                            ErrorHelper eh = new ErrorHelper(ChatActivity.this);
                            eh.CheckErrorText(res.getErrortext());
                        } else {
                            if (res.getChatID() != null && res.getChatID() > 0) {
                                OwningUser own = new OwningUser();
                                own.setOwningUserID(userid);
                                own.setOwningUserName(username);
                                Chat c = new Chat();
                                c.setOwningUser(own);
                                c.setChatID(res.getChatID());
                                c.setChatname(res.getChatname());

                                List<Chat> lc = mAdapter.getItemList();
                                lc.add(c);

                                mAdapter.setItemList(lc);
                                mAdapter.notifyDataSetChanged();
                            }
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }*/
}