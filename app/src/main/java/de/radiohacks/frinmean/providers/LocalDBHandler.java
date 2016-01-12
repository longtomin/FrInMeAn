package de.radiohacks.frinmean.providers;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import de.radiohacks.frinmean.Constants;

public class LocalDBHandler extends SQLiteOpenHelper {

    private static final String TAG = LocalDBHandler.class.getSimpleName();

    private static final int DATABASE_VERSION = 1;

    private static final String TABLE_MESSAGE_DROP =
            "DROP TABLE IF EXISTS "
                    + Constants.MESSAGES_TABLE_NAME;

    private static final String TABLE_CHAT_DROP =
            "DROP TABLE IF EXISTS "
                    + Constants.CHAT_TABLE_NAME;

    private static final String TABLE_USER_DROP =
            "DROP TABLE IF EXISTS "
                    + Constants.USER_TABLE_NAME;

    private static final String TABLE_MESSAGES_CREATE
            = "CREATE TABLE " + Constants.MESSAGES_TABLE_NAME
            + " (" + Constants.T_MESSAGES_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
            + Constants.T_MESSAGES_BADBID + " INTEGER, "
            + Constants.T_MESSAGES_OwningUserID + " INTEGER, "
            + Constants.T_MESSAGES_OwningUserName + " VARCHAR(45), "
            + Constants.T_MESSAGES_ChatID + " INTEGER, "
            + Constants.T_MESSAGES_MessageTyp + " VARCHAR(10), "
            + Constants.T_MESSAGES_SendTimestamp + " LONG, "
            + Constants.T_MESSAGES_ReadTimestamp + " LONG, "
            + Constants.T_MESSAGES_ShowTimestamp + " LONG, "
            + Constants.T_MESSAGES_TextMsgID + " INTEGER, "
            + Constants.T_MESSAGES_TextMsgValue + " VARCHAR(10000), "
            + Constants.T_MESSAGES_ImageMsgID + " INTEGER, "
            + Constants.T_MESSAGES_ImageMsgValue + " VARCHAR(256), "
            + Constants.T_MESSAGES_VideoMsgID + " INTEGER, "
            + Constants.T_MESSAGES_VideoMsgValue + " VARCHAR(256), "
            + Constants.T_MESSAGES_FileMsgID + " INTEGER, "
            + Constants.T_MESSAGES_FileMsgValue + " VARCHAR(256), "
            + Constants.T_MESSAGES_LocationMsgID + " INTEGER, "
            + Constants.T_MESSAGES_LocationMsgValue + " VARCHAR(50), "
            + Constants.T_MESSAGES_ContactMsgID + " INTEGER, "
            + Constants.T_MESSAGES_ContactMsgValue + " VARCHAR(250));";
            /*+ Constants.T_MESSAGES_NumberAll + " INTEGER, "
            + Constants.T_MESSAGES_NumberRead + " INTEGER, "
            + Constants.T_MESSAGES_NumberShow + " INTEGER);"; */

    private static final String TABLE_CHAT_CREATE
            = "CREATE TABLE " + Constants.CHAT_TABLE_NAME
            + " (" + Constants.T_CHAT_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
            + Constants.T_CHAT_BADBID + " INTEGER, "
            + Constants.T_CHAT_OwningUserID + " INTEGER, "
            + Constants.T_CHAT_OwningUserName + " VARCHAR(45), "
            + Constants.T_CHAT_ChatName + " VARCHAR(50), "
            + Constants.T_CHAT_IconID + " INTEGER, "
            + Constants.T_CHAT_IconValue + " VARCHAR(256));";

    private static final String TABLE_USER_CREATE
            = "CREATE TABLE " + Constants.USER_TABLE_NAME
            + " (" + Constants.T_USER_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
            + Constants.T_USER_BADBID + " INTEGER, "
            + Constants.T_USER_PhoneUsername + " VARCHAR(100), "
            + Constants.T_USER_Username + " VARCHAR(45), "
            + Constants.T_USER_Email + " VARCHAR(100), "
            + Constants.T_USER_AuthenticationTime + " INTEGER, "
            + Constants.T_User_IconID + " INTEGER, "
            + Constants.T_User_IconValue + " VARCHAR(256));";

    private static final String TABLE_MESSAGE_TIME_CREATE
            = "CREATE TABLE " + Constants.MESSAGE_TIME_TABLE_NAME
            + " (" + Constants.T_MESSAGES_TIME_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
            + Constants.T_MESSAGES_TIME_BADBID + " INTEGER, "
            + Constants.T_MESSAGES_TIME_UserID + " INTEGER, "
            + Constants.T_MESSAGES_TIME_UserName + " VARCHAR(45), "
            + Constants.T_MESSAGES_TIME_SendTimestamp + " LONG DEFAULT 0, "
            + Constants.T_MESSAGES_TIME_ReadTimestamp + " LONG DEFAULT 0, "
            + Constants.T_MESSAGES_TIME_ShowTimestamp + " LONG DEFAULT 0);";

    private static final String MESSAGES_INDEX_NAME = "frinme_messages_idx";
    private static final String MESSAGES_TIME_INDEX_NAME = "frinme_messages_time_idx";

    private static final String MESSAGES_INDEX_CREATE = "CREATE INDEX " + MESSAGES_INDEX_NAME + " on " + Constants.MESSAGES_TABLE_NAME + " (" + Constants.T_MESSAGES_ChatID + ", " + Constants.T_MESSAGES_SendTimestamp + ");";
    private static final String MESSAGES_TIME_INDEX_CREATE = "CREATE INDEX " + MESSAGES_TIME_INDEX_NAME + " on " + Constants.MESSAGE_TIME_TABLE_NAME + " (" + Constants.T_MESSAGES_TIME_BADBID + ", " + Constants.T_MESSAGES_TIME_UserID + ");";

    public LocalDBHandler(Context context) {
        super(context, Constants.DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        Log.d(TAG, "start onCreate");
        db.execSQL(TABLE_MESSAGES_CREATE);
        db.execSQL(TABLE_CHAT_CREATE);
        db.execSQL(TABLE_USER_CREATE);
        db.execSQL(TABLE_MESSAGE_TIME_CREATE);
        db.execSQL(MESSAGES_INDEX_CREATE);
        db.execSQL(MESSAGES_TIME_INDEX_CREATE);
        Log.d(TAG, "end onCreate");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        Log.d(TAG, "start onUpgrade");
        Log.w(TAG, "Upgrade der DB von V: " + oldVersion + " zu V:" + newVersion + "; Alle Daten werden gelöscht!");
        db.execSQL(TABLE_MESSAGE_DROP);
        db.execSQL(TABLE_CHAT_DROP);
        db.execSQL(TABLE_USER_DROP);
        onCreate(db);
        Log.d(TAG, "end onUpgrade");
    }
}