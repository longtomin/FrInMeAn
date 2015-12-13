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

import android.annotation.TargetApi;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.preference.PreferenceActivity;
import android.preference.PreferenceCategory;
import android.preference.PreferenceFragment;
import android.util.Log;

import java.io.File;

import de.radiohacks.frinmean.service.CustomExceptionHandler;
import de.radiohacks.frinmean.service.MeBaService;

/**
 * A {@link PreferenceActivity} that presents a set of application settings. On
 * handset devices, settings are presented as a single list. On tablets,
 * settings are split by category, with category headers shown to the left of
 * the list of settings.
 * <p/>
 * See <a href="http://developer.android.com/design/patterns/settings.html">
 * Android Design: Settings</a> for design guidelines and the <a
 * href="http://developer.android.com/guide/topics/ui/settings.html">Settings
 * API Guide</a> for more information on developing a Settings UI.
 */
public class SettingsActivity extends PreferenceActivity {
    /**
     * Determines whether to always show the simplified settings UI, where
     * settings are presented in a single list. When false, settings are shown
     * as a master/detail two-pane view on tablets. When true, a single pane is
     * shown on tablets.
     */
    private static final String TAG = SettingsActivity.class.getSimpleName();
    private static final boolean ALWAYS_SIMPLE_PREFS = true;
    private static final int REQUEST_DIRECTORY = 0;
    private static final int REQUEST_USER_THUMBNAIL = 1;

    /**
     * Helper method to determine if the device has an extra-large screen. For
     * example, 10" tablets are extra-large.
     */
    private static boolean isXLargeTablet(Context context) {
        return (context.getResources().getConfiguration().screenLayout
                & Configuration.SCREENLAYOUT_SIZE_MASK) >= Configuration.SCREENLAYOUT_SIZE_XLARGE;
    }

    /**
     * A preference value change listener that updates the preference's summary
     * to reflect its new value.
     */
    /* private static Preference.OnPreferenceChangeListener sBindPreferenceSummaryToValueListener = new Preference.OnPreferenceChangeListener() {
        @Override
        public boolean onPreferenceChange(Preference preference, Object value) {
            String stringValue = value.toString();

            if (preference instanceof ListPreference) {
                // For list preferences, look up the correct display value in
                // the preference's 'entries' list.
                ListPreference listPreference = (ListPreference) preference;
                int index = listPreference.findIndexOfValue(stringValue);

                // Set the summary to reflect the new value.
                preference.setSummary(
                        index >= 0
                                ? listPreference.getEntries()[index]
                                : null
                );

            } else if (preference instanceof RingtonePreference) {
                // For ringtone preferences, look up the correct display value
                // using RingtoneManager.
                if (TextUtils.isEmpty(stringValue)) {
                    // Empty values correspond to 'silent' (no ringtone).
                    preference.setSummary(R.string.pref_ringtone_silent);

                } else {
                    Ringtone ringtone = RingtoneManager.getRingtone(
                            preference.getContext(), Uri.parse(stringValue));

                    if (ringtone == null) {
                        // Clear the summary if there was a lookup error.
                        preference.setSummary(null);
                    } else {
                        // Set the summary to reflect the new ringtone display
                        // name.
                        String name = ringtone.getTitle(preference.getContext());
                        preference.setSummary(name);
                    }
                }

            } else {
                // For all other preferences, set the summary to the value's
                // simple string representation.
                preference.setSummary(stringValue);
            }
            return true;
        }
    };*/

    /**
     * Determines whether the simplified settings UI should be shown. This is
     * true if this is forced via {@link #ALWAYS_SIMPLE_PREFS}, or the device
     * doesn't have newer APIs like {@link PreferenceFragment}, or the device
     * doesn't have an extra-large screen. In these cases, a single-pane
     * "simplified" settings UI should be shown.
     */
    private static boolean isSimplePreferences(Context context) {
        boolean ret1 = ALWAYS_SIMPLE_PREFS;
        boolean ret2 = Build.VERSION.SDK_INT < Build.VERSION_CODES.HONEYCOMB;
        boolean ret3 = !isXLargeTablet(context);

        return ALWAYS_SIMPLE_PREFS
                || Build.VERSION.SDK_INT < Build.VERSION_CODES.HONEYCOMB
                || !isXLargeTablet(context);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

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

//        SharedPreferences sharedPrefs = PreferenceManager
//                .getDefaultSharedPreferences(this);

//        String directory = sharedPrefs.getString(Constants.PrefDirectory, "NULL");
//        if (directory.equalsIgnoreCase("NULL")) {
//            if (!(Thread.getDefaultUncaughtExceptionHandler() instanceof CustomExceptionHandler)) {
//                Thread.setDefaultUncaughtExceptionHandler(new CustomExceptionHandler(Environment.getExternalStorageDirectory().toString()));
//            }
//        } else {
//            if (!(Thread.getDefaultUncaughtExceptionHandler() instanceof CustomExceptionHandler)) {
//                Thread.setDefaultUncaughtExceptionHandler(new CustomExceptionHandler(directory));
//            }
//        }

    }

    /**
     * Binds a preference's summary to its value. More specifically, when the
     * preference's value is changed, its summary (line of text below the
     * preference title) is updated to reflect the value. The summary is also
     * immediately updated upon calling this method. The exact display format is
     * dependent on the type of preference.
     *
     * @see #
     */
    /* private static void bindPreferenceSummaryToValue(Preference preference) {
        // Set the listener to watch for value changes.
        preference.setOnPreferenceChangeListener(sBindPreferenceSummaryToValueListener);

        // Trigger the listener immediately with the preference's
        // current value.
        sBindPreferenceSummaryToValueListener.onPreferenceChange(preference,
                PreferenceManager
                        .getDefaultSharedPreferences(preference.getContext())
                        .getString(preference.getKey(), "")
        );
    }*/
    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);

        setupSimplePreferencesScreen();
    }

    /**
     * Shows the simplified settings UI if the device configuration if the
     * device configuration dictates that a simplified, single-pane UI should be
     * shown.
     */
    private void setupSimplePreferencesScreen() {
//        if (!isSimplePreferences(this)) {
//            return;
//        }

        // In the simplified UI, fragments are not used at all and we instead
        // use the older PreferenceActivity APIs.

        // Add 'general' preferences.
        addPreferencesFromResource(R.xml.pref_general);
/*        Preference filePicker = findPreference(Constants.PrefDirectory);
        filePicker.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {

                final String m_chosenDir = "";
                boolean m_newFolderEnabled = true;
                // Create DirectoryChooserDialog and register a callback
                DirectoryChooserDialog directoryChooserDialog =
                        new DirectoryChooserDialog(SettingsActivity.this,
                                new DirectoryChooserDialog.ChosenDirectoryListener() {
                                    @Override
                                    public void onChosenDir(String chosenDir) {
                                        if (chosenDir != null && !chosenDir.isEmpty()) {
                                            SharedPreferences sharedPrefs = PreferenceManager
                                                    .getDefaultSharedPreferences(SettingsActivity.this);
                                            SharedPreferences.Editor editor = sharedPrefs.edit();
                                            editor.putString(Constants.PrefDirectory, chosenDir);
                                            editor.commit();

                                            // Jetzt erstellen wir die Unterverzeichnisse

                                            //File basedir = new File(Environment.getExternalStorageDirectory() + s);
                                            File basedir = new File(chosenDir);
                                            if (basedir.exists() && basedir.isDirectory()) {
                                                File imagedir = new File(chosenDir + File.separator + Constants.IMAGEDIR);
                                                if (!imagedir.exists() || !imagedir.isDirectory()) {
                                                    imagedir.mkdir();
                                                }
                                                File imagepreviewdir = new File(chosenDir + File.separator + Constants.IMAGEPREVIEWDIR);
                                                if (!imagepreviewdir.exists() || !imagepreviewdir.isDirectory()) {
                                                    imagepreviewdir.mkdir();
                                                }
                                                File imagechatdir = new File(chosenDir + File.separator + Constants.CHATIAMGEDIR);
                                                if (!imagechatdir.exists() || !imagechatdir.isDirectory()) {
                                                    imagechatdir.mkdir();
                                                }
                                                File videodir = new File(chosenDir + File.separator + Constants.VIDEODIR);
                                                if (!videodir.exists() || !videodir.isDirectory()) {
                                                    videodir.mkdir();
                                                }
                                                File filesdir = new File(chosenDir + File.separator + Constants.FILESDIR);
                                                if (!filesdir.exists() || !filesdir.isDirectory()) {
                                                    filesdir.mkdir();
                                                }
                                            }
                                        }
                                        Toast.makeText(
                                                SettingsActivity.this, "Chosen directory: " +
                                                        chosenDir, Toast.LENGTH_LONG).show();
                                    }
                                });
                // Toggle new folder button enabling
                directoryChooserDialog.setNewFolderEnabled(m_newFolderEnabled);
                // Load directory chooser dialog for initial 'm_chosenDir' directory.
                // The registered callback will be called upon final directory selection.
                directoryChooserDialog.chooseDirectory(m_chosenDir);
                m_newFolderEnabled = !m_newFolderEnabled;
                return true;
            }
        })*/


                /* Intent i = new Intent(SettingsActivity.this, FilePickerActivity.class);
                i.putExtra(FilePickerActivity.EXTRA_ALLOW_MULTIPLE, false);
                i.putExtra(FilePickerActivity.EXTRA_ALLOW_CREATE_DIR, true);
                i.putExtra(FilePickerActivity.EXTRA_MODE, FilePickerActivity.MODE_DIR);

                startActivityForResult(i, REQUEST_DIRECTORY);*/

        /*Preference ownPicture = (Preference) findPreference("prefOwnImage");
        ownPicture.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                final Intent chooserIntent = new Intent(
                        SettingsActivity.this,
                        ThumbnailActivity.class);
                chooserIntent.putExtra(Constants.THUMBNAIL_TYPE,Constants.THUMBNAIL_USER);
                startActivityForResult(chooserIntent, REQUEST_USER_THUMBNAIL);
                return true;
            }
        });*/


        // Add 'notifications' preferences, and a corresponding header.
        PreferenceCategory fakeHeader = new PreferenceCategory(this);
        fakeHeader.setTitle(R.string.pref_header_notifications);
        getPreferenceScreen().addPreference(fakeHeader);
        addPreferencesFromResource(R.xml.pref_notification);

        // Add 'data and sync' preferences, and a corresponding header.
        fakeHeader = new PreferenceCategory(this);
        fakeHeader.setTitle(R.string.pref_header_data_sync);
        getPreferenceScreen().addPreference(fakeHeader);
        addPreferencesFromResource(R.xml.pref_data_sync);

        // Bind the summaries of EditText/List/Dialog/Ringtone preferences to
        // their values. When their values change, their summaries are updated
        // to reflect the new value, per the Android Design guidelines.
        /*bindPreferenceSummaryToValue(findPreference("prefServername"));
        bindPreferenceSummaryToValue(findPreference("prefUsername"));
        bindPreferenceSummaryToValue(findPreference("prefPassword"));

        bindPreferenceSummaryToValue(findPreference("example_list"));
        bindPreferenceSummaryToValue(findPreference("notifications_new_message_ringtone"));
        bindPreferenceSummaryToValue(findPreference("sync_frequency")); */
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean onIsMultiPane() {
        return isXLargeTablet(this) && !isSimplePreferences(this);
    }

/*    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == REQUEST_DIRECTORY && resultCode == Activity.RESULT_OK) {

            Uri file_uri = data.getData();
            String real_path = file_uri.getPath();

            if (real_path != null && !real_path.isEmpty()) {
                SharedPreferences sharedPrefs = PreferenceManager
                        .getDefaultSharedPreferences(this);
                SharedPreferences.Editor editor = sharedPrefs.edit();
                editor.putString(Constants.PrefDirectory, real_path);
                editor.commit();

                // Jetzt erstellen wir die Unterverzeichnisse


                //File basedir = new File(Environment.getExternalStorageDirectory() + s);
                File basedir = new File(real_path);
                if (basedir.exists() && basedir.isDirectory()) {
                    File imagedir = new File(real_path + File.separator + Constants.IMAGEDIR);
                    if (!imagedir.exists() || !imagedir.isDirectory()) {
                        imagedir.mkdir();
                    }
                    File imagepreviewdir = new File(real_path + File.separator + Constants.IMAGEPREVIEWDIR);
                    if (!imagepreviewdir.exists() || !imagepreviewdir.isDirectory()) {
                        imagepreviewdir.mkdir();
                    }
                    File imagechatdir = new File(real_path + File.separator + Constants.CHATIAMGEDIR);
                    if (!imagechatdir.exists() || !imagechatdir.isDirectory()) {
                        imagechatdir.mkdir();
                    }
                    File videodir = new File(real_path + File.separator + Constants.VIDEODIR);
                    if (!videodir.exists() || !videodir.isDirectory()) {
                        videodir.mkdir();
                    }
                    File filesdir = new File(real_path + File.separator + Constants.FILESDIR);
                    if (!filesdir.exists() || !filesdir.isDirectory()) {
                        filesdir.mkdir();
                    }
                }
            }
        } else if (requestCode == REQUEST_USER_THUMBNAIL) {

        }
    }*/

    @Override
    protected void onStop() {

        Intent reload = new Intent(this, MeBaService.class);
        reload.setAction(Constants.ACTION_RELOAD_SETTING);
        startService(reload);
        super.onStop();
    }

    /**
     * This fragment shows general preferences only. It is used when the
     * activity is showing a two-pane settings UI.
     */
    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    public static class GeneralPreferenceFragment extends PreferenceFragment {
        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            addPreferencesFromResource(R.xml.pref_general);

            // Bind the summaries of EditText/List/Dialog/Ringtone preferences
            // to their values. When their values change, their summaries are
            // updated to reflect the new value, per the Android Design
            // guidelines.
//            bindPreferenceSummaryToValue(findPreference("prefServername"));
//            bindPreferenceSummaryToValue(findPreference("prefUsername"));
//            bindPreferenceSummaryToValue(findPreference("prefPassword"));
//            bindPreferenceSummaryToValue(findPreference("example_list"));
        }
    }

    /**
     * This fragment shows notification preferences only. It is used when the
     * activity is showing a two-pane settings UI.
     */
    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    public static class NotificationPreferenceFragment extends PreferenceFragment {
        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            addPreferencesFromResource(R.xml.pref_notification);

            // Bind the summaries of EditText/List/Dialog/Ringtone preferences
            // to their values. When their values change, their summaries are
            // updated to reflect the new value, per the Android Design
            // guidelines.
            // bindPreferenceSummaryToValue(findPreference("notifications_new_message_ringtone"));
        }
    }

    /**
     * This fragment shows data and sync preferences only. It is used when the
     * activity is showing a two-pane settings UI.
     */
    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    public static class DataSyncPreferenceFragment extends PreferenceFragment {
        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            addPreferencesFromResource(R.xml.pref_data_sync);

            // Bind the summaries of EditText/List/Dialog/Ringtone preferences
            // to their values. When their values change, their summaries are
            // updated to reflect the new value, per the Android Design
            // guidelines.
            // bindPreferenceSummaryToValue(findPreference("sync_frequency"));
        }
    }
}
