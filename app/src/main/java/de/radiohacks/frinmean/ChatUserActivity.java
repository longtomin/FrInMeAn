package de.radiohacks.frinmean;


import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentManager;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v13.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;

public class ChatUserActivity extends Activity {

    /**
     * The {@link android.support.v4.view.PagerAdapter} that will provide
     * fragments for each of the sections. We use a
     * {@link FragmentPagerAdapter} derivative, which will keep every
     * loaded fragment in memory. If this becomes too memory intensive, it
     * may be best to switch to a
     * {@link android.support.v13.app.FragmentStatePagerAdapter}.
     */
    private SectionsPagerAdapter mSectionsPagerAdapter;
    private String chatmode;
    private int userid;
    private int syncfreq;
    private int chatid;
    private int sendmsgid;
    private String chatname;
    private int owninguserid;

    /**
     * The {@link ViewPager} that will host the section contents.
     */
    private ViewPager mViewPager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat_user);
        getActionBar().setDisplayHomeAsUpEnabled(true);

        Intent i = getIntent();
        chatmode = i.getStringExtra(Constants.CHAT_ACTIVITY_MODE);
        userid = i.getIntExtra(Constants.USERID, -1);
        syncfreq = i.getIntExtra(Constants.PrefSyncfrequency, -1);
        chatid = i.getIntExtra(Constants.CHATID, -1);
        sendmsgid = i.getIntExtra(Constants.SENDMSGID, -1);
        owninguserid = i.getIntExtra(Constants.OWNINGUSERID, -1);
        // Create the adapter that will return a fragment for each of the three
        // primary sections of the activity.
        mSectionsPagerAdapter = new SectionsPagerAdapter(getFragmentManager());

        // Set up the ViewPager with the sections adapter.
        mViewPager = (ViewPager) findViewById(R.id.container);
        mViewPager.setAdapter(mSectionsPagerAdapter);
    }

    /**
     * A placeholder fragment containing a simple view.
     */
    public static class PlaceholderFragment extends Fragment {
        /**
         * The fragment argument representing the section number for this
         * fragment.
         */
        private static final String ARG_SECTION_NUMBER = "section_number";

        public PlaceholderFragment() {
        }
    }

    /**
     * A {@link FragmentPagerAdapter} that returns a fragment corresponding to
     * one of the sections/tabs/pages.
     */
    public class SectionsPagerAdapter extends FragmentPagerAdapter {

        public SectionsPagerAdapter(FragmentManager fm) {
            super(fm);
            getPreferenceInfo();
        }

        protected void getPreferenceInfo() {
            SharedPreferences sharedPrefs = PreferenceManager
                    .getDefaultSharedPreferences(FrinmeanApplication.getAppContext());

//            this.server = sharedPrefs.getString(Constants.PrefServername, "NULL");
//            boolean https = sharedPrefs.getBoolean(Constants.PrefHTTPSCommunication, true);
//            if (https) {
//                this.port = Integer.parseInt(sharedPrefs.getString(Constants.PrefServerport, "-1"));
//            } else {
//                this.port = Integer.parseInt(sharedPrefs.getString(Constants.PrefServerport, "-1"));
//            }
            userid = sharedPrefs.getInt(Constants.PrefUserID, -1);
//            this.username = sharedPrefs.getString(Constants.PrefUsername, "NULL");
//            this.password = sharedPrefs.getString(Constants.PrefPassword, "NULL");
            syncfreq = Integer.parseInt(sharedPrefs.getString(Constants.PrefSyncfrequency, "-1"));
//            this.askedrestore = sharedPrefs.getString(Constants.PrefAskedRestore, "NULL");
//            this.userAlreadyExists = sharedPrefs.getString(Constants.PrefUserAlreadExists, "NULL");
        }

        @Override
        public Fragment getItem(int position) {
            // getItem is called to instantiate the fragment for the given page.
            // Return a PlaceholderFragment (defined as a static inner class below).
            Fragment ret = null;
            switch (position) {
                case 0:
                    ret = new ChatFragment();
                    Bundle args = new Bundle();
                    args.putString(Constants.CHAT_ACTIVITY_MODE, chatmode);
                    args.putInt(Constants.USERID, userid);
                    args.putInt(Constants.PrefSyncfrequency, syncfreq);
                    //args.aaddFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT)
                    ret.setArguments(args);
                    break;
                case 1:
                    ret = new UserFragment();

                    break;
            }
            return ret;
        }

        @Override
        public int getCount() {
            if (chatmode.equalsIgnoreCase(Constants.CHAT_ACTIVITY_FULL)) {
                // We are in the Full Mode with a selection of a Chat
                // So we have two tabs that can be shown
                return 2;
            } else {
                // We are in the Forward mode so we only show the chatlist
                // for choosing a chat where the message should be send
                return 1;
            }
        }

        @Override
        public CharSequence getPageTitle(int position) {
            switch (position) {
                case 0:
                    return getString(R.string.title_tab_chat);
                case 1:
                    return getString(R.string.title_tab_user);
            }
            return null;
        }
    }
}
