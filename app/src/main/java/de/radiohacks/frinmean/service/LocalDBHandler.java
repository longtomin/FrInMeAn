package de.radiohacks.frinmean.service;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import de.radiohacks.frinmean.Constants;

/**
 * Created by thomas on 06.09.14.
 */
public class LocalDBHandler extends SQLiteOpenHelper {

    private static final String TAG = LocalDBHandler.class.getSimpleName();

    private static final int DATABASE_VERSION = 1;

    private static final String TABLE_NAME = "frinme_messages";
    private static final String TABLE_MESSAGE_DROP =
            "DROP TABLE IF EXISTS "
                    + TABLE_NAME;
    private static final String TABLE_CREATE
            = "CREATE TABLE " + TABLE_NAME
            + " (" + Constants.T_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
            + Constants.T_BADBID + " INTEGER, "
            + Constants.T_OwningUserID + " INTEGER, "
            + Constants.T_OwningUserName + " VARCHAR(45), "
            + Constants.T_ChatID + " INTEGER, "
            + Constants.T_ChatName + " VARCHAR(50), "
            + Constants.T_MessageTyp + " VARCHAR(10), "
            + Constants.T_SendTimestamp + " LONG, "
            + Constants.T_ReadTimestamp + " LONG, "
            + Constants.T_TextMsgID + " INTEGER, "
            + Constants.T_TextMsgValue + " VARCHAR(10000), "
            + Constants.T_ImageMsgID + " INTEGER, "
            + Constants.T_ImageMsgValue + " VARCHAR(256), "
            + Constants.T_FileMsgID + " INTEGER, "
            + Constants.T_FileMsgValue + " VARCHAR(256), "
            + Constants.T_LocationMsgID + " INTEGER, "
            + Constants.T_LocationMsgValue + " VARCHAR(50), "
            + Constants.T_ContactMsgID + " INTEGER, "
            + Constants.T_ContactMsgValue + " VARCHAR(250));";
    private static final String INDEX_NAME = "frinme_messages_idx";
    private static final String INDEX_CREATE = "CREATE INDEX " + INDEX_NAME + " on " + TABLE_NAME + " (" + Constants.T_ChatID + ", " + Constants.T_SendTimestamp + ");";


    public LocalDBHandler(Context context) {
        super(context, Constants.DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        Log.d(TAG, "start onCreate");
        db.execSQL(TABLE_CREATE);
        db.execSQL(INDEX_CREATE);
        Log.d(TAG, "end onCreate");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        Log.d(TAG, "start onUpgrade");
        Log.w(TAG, "Upgrade der DB von V: " + oldVersion + " zu V:" + newVersion + "; Alle Daten werden gelöscht!");
        db.execSQL(TABLE_MESSAGE_DROP);
        onCreate(db);
        Log.d(TAG, "end onUpgrade");
    }

    private int getID(int inid) {
        Log.d(TAG, "start getID");
        int ret = -1;
        SQLiteDatabase db = getWritableDatabase();
        Cursor c = db.query(TABLE_NAME, new String[]{Constants.T_BADBID}, Constants.T_BADBID + " =?", new String[]{Integer.toString(inid)}, null, null, null, null);
        if (c.moveToFirst()) {
            ret = c.getInt(c.getColumnIndex(Constants.T_BADBID));
        }
        Log.d(TAG, "end getID");
        return ret;
    }


    public long insert(int BackendID, int InOwningUserID, String InOwningUserName, int InChatID, String InChatName, String InMessageTyp, long InSendTimestamp,
                       long InReadTimestamp, int InMsgID) {
        Log.d(TAG, "start insert");

        long rowId = -1;
        try {

            SQLiteDatabase db = getWritableDatabase();
            ContentValues values = new ContentValues();
            values.put(Constants.T_BADBID, BackendID);
            values.put(Constants.T_OwningUserID, InOwningUserID);
            values.put(Constants.T_OwningUserName, InOwningUserName);
            values.put(Constants.T_ChatID, InChatID);
            values.put(Constants.T_ChatName, InChatName);
            values.put(Constants.T_MessageTyp, InMessageTyp);
            values.put(Constants.T_SendTimestamp, InSendTimestamp);
            values.put(Constants.T_ReadTimestamp, InReadTimestamp);
            if (InMessageTyp.equalsIgnoreCase(Constants.TYP_TEXT)) {
                values.put(Constants.T_TextMsgID, InMsgID);
            } else if (InMessageTyp.equalsIgnoreCase(Constants.TYP_IMAGE)) {
                values.put(Constants.T_ImageMsgID, InMsgID);
            } else if (InMessageTyp.equalsIgnoreCase(Constants.TYP_LOCATION)) {
                values.put(Constants.T_LocationMsgID, InMsgID);
            } else if (InMessageTyp.equalsIgnoreCase(Constants.TYP_CONTACT)) {
                values.put(Constants.T_ContactMsgID, InMsgID);
            } else if (InMessageTyp.equalsIgnoreCase(Constants.TYP_FILE)) {
                values.put(Constants.T_FileMsgID, InMsgID);
            }
            // Check if Message already exists, if not insert message else update message.
            int id = getID(BackendID);
            if (id == -1) {
                rowId = db.insert(TABLE_NAME, null, values);
            } else {
                rowId = db.update(TABLE_NAME, values, Constants.T_BADBID + "=?", new String[]{Integer.toString(id)});
            }

        } catch (SQLiteException e) {
            Log.e(TAG, "insert()", e);
        } finally {
            Log.d(TAG, "insert(): rowId=" + rowId);
        }
        Log.d(TAG, "end insert");
        return rowId;
    }

    public void update(String InMsgTyp, int InMsgID, String value) {
        Log.d(TAG, "start update");
        SQLiteDatabase db = getWritableDatabase();
        ContentValues values = new ContentValues();
        if (InMsgTyp.equalsIgnoreCase(Constants.TYP_TEXT)) {
            values.put(Constants.T_TextMsgValue, value);
        } else if (InMsgTyp.equalsIgnoreCase(Constants.TYP_IMAGE)) {
            values.put(Constants.T_ImageMsgValue, value);
        } else if (InMsgTyp.equalsIgnoreCase(Constants.TYP_LOCATION)) {
            values.put(Constants.T_LocationMsgValue, value);
        } else if (InMsgTyp.equalsIgnoreCase(Constants.TYP_CONTACT)) {
            values.put(Constants.T_ContactMsgValue, value);
        } else if (InMsgTyp.equalsIgnoreCase(Constants.TYP_FILE)) {
            values.put(Constants.T_FileMsgValue, value);
        }
        db.update(TABLE_NAME, values, Constants.T_BADBID + "=" + InMsgID, null);
        Log.d(TAG, "end update");
    }

    public long CalcDate(long start) {
        return start - (2 * 7 * 24 * 60 * 60);
    }

    public Cursor get(int chatid, long start) {
        Log.d(TAG, "start get");

        long timestamp = CalcDate(start);

        SQLiteDatabase db = getReadableDatabase();
        // SQLiteDatabase db = getWritableDatabase();
        /*String SELECT_QUERY = "SELECT " + Constants.T_ID + ", "
                + Constants.T_BADBID + ", "
                + Constants.T_OwningUserName + ", "
                + Constants.T_OwningUserID + ", "
                + Constants.T_MessageTyp + ", "
                + Constants.T_SendTimestamp + ", "
                + Constants.T_ReadTimestamp + ", "
                + Constants.T_TextMsgID + ", "
                + Constants.T_TextMsgValue + ", "
                + Constants.T_ImageMsgID + ", "
                + Constants.T_ImageMsgValue + ", "
                + Constants.T_FileMsgID + ", "
                + Constants.T_FileMsgValue + ", "
                + Constants.T_LocationMsgID + ", "
                + Constants.T_LocationMsgValue + ", "
                + Constants.T_ContactMsgID + ", "
                + Constants.T_ContactMsgValue +
                " FROM " + TABLE_NAME + " WHERE " + Constants.T_ChatID + " = " + chatid + " AND " + Constants.T_SendTimestamp +
                " > " + timestamp + " ORDER BY " + Constants.T_SendTimestamp + " ASC";*/
        Log.d(TAG, "end get");
        return db.rawQuery("Select * from " + TABLE_NAME + " WHERE " + Constants.T_ChatID + " = " + chatid + " AND " + Constants.T_SendTimestamp +
                " > " + timestamp + " ORDER BY " + Constants.T_SendTimestamp + " ASC", null);
        //return db.query(TABLE_NAME_MESSAGES, null, MESSAGE_SENDER + " LIKE ? OR " + MESSAGE_SENDER + " LIKE ?", sender , null, null, _ID + " ASC");
    }
}