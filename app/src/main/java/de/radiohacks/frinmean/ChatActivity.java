package de.radiohacks.frinmean;

import android.app.AlertDialog;
import android.app.ListActivity;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.widget.SwipeRefreshLayout;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ListView;

import org.simpleframework.xml.Serializer;
import org.simpleframework.xml.core.Persister;

import java.io.Reader;
import java.io.StringReader;
import java.util.List;

import de.radiohacks.frinmean.adapters.ChatAdapter;
import de.radiohacks.frinmean.model.Chat;
import de.radiohacks.frinmean.model.OutCreateChat;
import de.radiohacks.frinmean.model.OutListChat;
import de.radiohacks.frinmean.model.OwningUser;
import de.radiohacks.frinmean.service.ErrorHelper;
import de.radiohacks.frinmean.service.MeBaService;
import de.radiohacks.frinmean.service.RestClient;


public class ChatActivity extends ListActivity implements SwipeRefreshLayout.OnRefreshListener {
    //implements SwipeRefreshLayout.OnRefreshListener {

    private ListChatStateReceiver mListChatStateReceiver = new ListChatStateReceiver();
    private ChatAdapter mAdapter;
    private String username;
    private String password;
    private String server;
    private int userid;
    private String chatname;
    private SwipeRefreshLayout swipeLayout;


    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        mAdapter = new ChatAdapter(this);
        setListAdapter(mAdapter);

        getPreferenceInfo();

        IntentFilter statusIntentFilter = new IntentFilter(
                Constants.BROADCAST_LISTCHAT);
        statusIntentFilter.addAction(Constants.BROADCAST_CREATECHAT);

        // Sets the filter's category to DEFAULT
        statusIntentFilter.addCategory(Intent.CATEGORY_DEFAULT);

        mListChatStateReceiver = new ListChatStateReceiver();

        // Registers the DownloadStateReceiver and its intent filters
        LocalBroadcastManager.getInstance(this).registerReceiver(
                mListChatStateReceiver,
                statusIntentFilter);

        //Start MeBaService
        Intent intentMyIntentService = new Intent(this, MeBaService.class);
        intentMyIntentService.setAction(Constants.ACTION_LISTCHAT);
        startService(intentMyIntentService);

        swipeLayout = (SwipeRefreshLayout) findViewById(R.id.swipe_container);
        swipeLayout.setOnRefreshListener(this);

        swipeLayout.setColorScheme(android.R.color.holo_blue_bright,
                android.R.color.holo_green_light,
                android.R.color.holo_orange_light,
                android.R.color.holo_red_light);
    }

    protected void getPreferenceInfo() {
        SharedPreferences sharedPrefs = PreferenceManager
                .getDefaultSharedPreferences(this);

        server = sharedPrefs.getString("prefServername", "NULL");
        username = sharedPrefs.getString("prefUsername", "NULL");
        password = sharedPrefs.getString("prefPassword", "NULL");
        userid = sharedPrefs.getInt("prefUserID", -1);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.chat, menu);
        return true;
    }

    @Override
    protected void onDestroy() {
        //un-register BroadcastReceiver
        unregisterReceiver(mListChatStateReceiver);
        super.onDestroy();
    }

    @Override
    protected void onResume() {
        super.onResume();
        IntentFilter infi = new IntentFilter(Constants.BROADCAST_LISTCHAT);
        infi.addAction(Constants.BROADCAST_CREATECHAT);

        registerReceiver(mListChatStateReceiver, infi);
    }

    @Override
    protected void onPause() {
        super.onPause();
        unregisterReceiver(mListChatStateReceiver);
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
                Intent su = new Intent(ChatActivity.this, SettingsActivity.class);
                startActivity(su);
                return true;

        }
        return super.onOptionsItemSelected(item);
    }

    private void openCreateChat() {
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(ChatActivity.this);

        alertDialogBuilder.setTitle(this.getTitle());
        alertDialogBuilder.setMessage(R.string.username);

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

    @Override
    protected void onListItemClick(ListView list, View view, int position, long id) {
        super.onListItemClick(list, view, position, id);

        Chat c = mAdapter.getItem(position);
        Intent i = new Intent(ChatActivity.this, SingleChatActivity.class);
        i.putExtra(Constants.CHATID, c.getChatID());
        i.putExtra(Constants.CHATNAME, c.getChatname());
        startActivity(i);
    }

    @Override
    public void onRefresh() {
        swipeLayout.setRefreshing(true);
        Intent iRefresh = new Intent(this, MeBaService.class);
        iRefresh.setAction(Constants.ACTION_LISTCHAT);
        startService(iRefresh);
        swipeLayout.setRefreshing(false);
    }

    private class ListChatStateReceiver extends BroadcastReceiver {

        private ListChatStateReceiver() {
            super();

            // prevents instantiation by other packages.
        }

        /**
         * This method is called by the system when a broadcast Intent is matched by this class'
         * intent filters
         *
         * @param context An Android context
         * @param intent  The incoming broadcast Intent
         */
        @Override
        public void onReceive(Context context, Intent intent) {

            /*
             * Gets the status from the Intent's extended data, and chooses the appropriate action
             */
            if (intent.getAction().equalsIgnoreCase(Constants.BROADCAST_LISTCHAT)) {
                try {
                    String ret = intent.getStringExtra(Constants.BROADCAST_DATA);
                    Serializer serializer = new Persister();
                    Reader reader = new StringReader(ret);

                    OutListChat res = serializer.read(OutListChat.class, reader, false);
                    if (res == null) {
                        ErrorHelper eh = new ErrorHelper(ChatActivity.this);
                        eh.CheckErrorText(Constants.NO_CONNECTION_TO_SERVER);
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
                        eh.CheckErrorText(Constants.NO_CONNECTION_TO_SERVER);
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
    }

/*    private class ChatLoader extends AsyncTask<String, Void, OutListChat> {

        private final ProgressDialog dialog = new ProgressDialog(ChatActivity.this);

        @Override
        protected void onPostExecute(OutListChat result) {
            super.onPostExecute(result);
            dialog.dismiss();

            if (result == null) {
                ErrorHelper eh = new ErrorHelper(ChatActivity.this);
                eh.CheckErrorText(Constants.NO_CONNECTION_TO_SERVER);
            } else {
                if (result.getErrortext() != null && !result.getErrortext().isEmpty()) {
                    ErrorHelper eh = new ErrorHelper(ChatActivity.this);
                    eh.CheckErrorText(result.getErrortext());
                } else {
                    if (result.getChat() != null && !result.getChat().isEmpty()) {
                        mAdapter.setItemList(result.getChat());
                        mAdapter.notifyDataSetChanged();
                    }
                }
            }
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            dialog.setMessage(getString(R.string.fetch_chats));
            dialog.show();
        }

        @Override
        protected OutListChat doInBackground(String... params) {
            OutListChat res = null;

            RestClient rc = new RestClient(params[0]);
            rc.AddParam("username", username);
            rc.AddParam("password", password);
            try {
                String ret = rc.ExecuteRequestXML(rc.BevorExecuteGet());
                Serializer serializer = new Persister();
                Reader reader = new StringReader(ret);

                res = serializer.read(OutListChat.class, reader, false);
            } catch (Exception e) {
                e.printStackTrace();
            }
            return res;
        }
    }*/

/*    private class ChatCreateLoader extends AsyncTask<String, Void, OutCreateChat> {

        private final ProgressDialog dialog = new ProgressDialog(ChatActivity.this);

        @Override
        protected void onPostExecute(OutCreateChat result) {
            super.onPostExecute(result);
            dialog.dismiss();

            if (result == null) {
                ErrorHelper eh = new ErrorHelper(ChatActivity.this);
                eh.CheckErrorText(Constants.NO_CONNECTION_TO_SERVER);
            } else {
                if (result.getErrortext() != null && !result.getErrortext().isEmpty()) {
                    ErrorHelper eh = new ErrorHelper(ChatActivity.this);
                    eh.CheckErrorText(result.getErrortext());
                } else {
                    if (result.getChatname() != null && !result.getChatname().isEmpty()) {

                    }
                }
            }
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            dialog.setMessage(getString(R.string.action_createchat));
            dialog.show();
        }

        @Override
        protected OutCreateChat doInBackground(String... params) {
            OutCreateChat res = null;

            RestClient rc = new RestClient(params[0]);
            rc.AddParam("username", username);
            rc.AddParam("password", password);
            rc.AddParam("chatname", chatname);
            try {
                String ret = rc.ExecuteRequestXML(rc.BevorExecuteGet());
                Serializer serializer = new Persister();
                Reader reader = new StringReader(ret);

                res = serializer.read(OutCreateChat.class, reader, false);
            } catch (Exception e) {
                e.printStackTrace();
            }
            return res;
        }
    }*/
}