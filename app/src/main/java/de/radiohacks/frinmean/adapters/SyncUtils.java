/*
 * Copyright 2013 Google Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package de.radiohacks.frinmean.adapters;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.annotation.TargetApi;
import android.content.ContentResolver;
import android.content.Context;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;

import de.radiohacks.frinmean.Constants;
import de.radiohacks.frinmean.account.GenericAccountService;
import de.radiohacks.frinmean.providers.FrinmeanContentProvider;

/**
 * Static helper methods for working with the sync framework.
 */
public class SyncUtils {
    private static Account mAccount;

    /**
     * Create an entry for this application in the system account list, if it isn't already there.
     *
     * @param context Context
     */
    @TargetApi(Build.VERSION_CODES.FROYO)
    public static void CreateSyncAccount(Context context) {
        boolean newAccount = false;
        boolean setupComplete = PreferenceManager
                .getDefaultSharedPreferences(context).getBoolean(Constants.PREF_SETUP_COMPLETE, false);

        // Create account, if it's missing. (Either first run, or user has deleted account.)
        mAccount = GenericAccountService.GetAccount(Constants.ACCOUNT_TYPE);
        AccountManager accountManager =
                (AccountManager) context.getSystemService(Context.ACCOUNT_SERVICE);
        if (accountManager.addAccountExplicitly(mAccount, null, null)) {
            // Inform the system that this account supports sync
            ContentResolver.setIsSyncable(mAccount, FrinmeanContentProvider.AUTHORITY, 1);
            // Inform the system that this account is eligible for auto sync when the network is up
            ContentResolver.setSyncAutomatically(mAccount, FrinmeanContentProvider.AUTHORITY, true);
            // Recommend a schedule for automatic synchronization. The system may modify this based
            // on other scheduled syncs and network utilization.
            // ContentResolver.addPeriodicSync(
            //        mAccount, FrinmeanContentProvider.AUTHORITY, new Bundle(), SYNC_FREQUENCY);
            newAccount = true;
        }

        // Schedule an initial sync if we detect problems with either our account or our local
        // data has been deleted. (Note that it's possible to clear app data WITHOUT affecting
        // the account list, so wee need to check both.)
        if (newAccount || !setupComplete) {
            PreferenceManager.getDefaultSharedPreferences(context).edit()
                    .putBoolean(Constants.PREF_SETUP_COMPLETE, true).commit();
        }
        TriggerRefresh();
    }

    public static void ChangeSyncFreq(long infreq) {
        ContentResolver.removePeriodicSync(mAccount, FrinmeanContentProvider.AUTHORITY, new Bundle());
        ContentResolver.addPeriodicSync(mAccount, FrinmeanContentProvider.AUTHORITY, new Bundle(), infreq);
    }

    /**
     * Helper method to trigger an immediate sync ("refresh").
     * <p/>
     * <p>This should only be used when we need to preempt the normal sync schedule. Typically, this
     * means the user has pressed the "refresh" button.
     * <p/>
     * Note that SYNC_EXTRAS_MANUAL will cause an immediate sync, without any optimization to
     * preserve battery life. If you know new data is available (perhaps via a GCM notification),
     * but the user is not actively waiting for that data, you should omit this flag; this will give
     * the OS additional freedom in scheduling your sync request.
     */
    public static void TriggerRefresh() {
        Bundle b = new Bundle();
        // Disable sync backoff and ignore sync preferences. In other words...perform sync NOW!
        b.putBoolean(ContentResolver.SYNC_EXTRAS_MANUAL, true);
        b.putBoolean(ContentResolver.SYNC_EXTRAS_EXPEDITED, true);
        ContentResolver.requestSync(
                GenericAccountService.GetAccount(Constants.ACCOUNT_TYPE), // Sync account
                FrinmeanContentProvider.AUTHORITY,                 // Content authority
                b);                                             // Extras
    }
}
