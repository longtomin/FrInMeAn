package de.radiohacks.frinmean.providers;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;
import android.text.TextUtils;
import android.util.Log;

import java.util.Arrays;
import java.util.HashSet;

import de.radiohacks.frinmean.Constants;


public class FrinmeanContentProvider extends ContentProvider {

    private static final String TAG = FrinmeanContentProvider.class.getSimpleName();

    // Used for the UriMacher
    private static final int Frinmean_messages = 10;
    private static final int ID = 20;
    private static final int BADBID = 30;
    private static final int OwningUserID = 40;
    private static final int OwningUserName = 50;
    private static final int ChatID = 60;
    private static final int ChatName = 70;
    private static final int MessageTyp = 80;
    private static final int SendTimestamp = 90;
    private static final int ReadTimestamp = 100;
    private static final int TextMsgID = 110;
    private static final int TextMsgValue = 120;
    private static final int ImageMsgID = 130;
    private static final int ImageMsgValue = 140;
    private static final int FileMsgID = 150;
    private static final int FileMsgValue = 160;
    private static final int LocationMsgID = 170;
    private static final int LocationMsgValue = 180;
    private static final int ContactMsgID = 190;
    private static final int ContactMsgValue = 200;
    private static final String AUTHORITY = "de.radiohacks.frinmean.providers.FrinmeanContentProvider";
    public static final Uri CONTENT_URI = Uri.parse("content://" + AUTHORITY
            + "/" + Constants.TABLE_NAME);
    private static final UriMatcher sURIMatcher = new UriMatcher(
            UriMatcher.NO_MATCH);

    static {
        sURIMatcher.addURI(AUTHORITY, Constants.TABLE_NAME, Frinmean_messages);
        sURIMatcher.addURI(AUTHORITY, Constants.TABLE_NAME + "/#/" + Constants.T_ID, ID);
        sURIMatcher.addURI(AUTHORITY, Constants.TABLE_NAME + "/#/" + Constants.T_BADBID, BADBID);
        sURIMatcher.addURI(AUTHORITY, Constants.TABLE_NAME + "/#/" + Constants.T_OwningUserID, OwningUserID);
        sURIMatcher.addURI(AUTHORITY, Constants.TABLE_NAME + "/#/" + Constants.T_OwningUserName, OwningUserName);
        sURIMatcher.addURI(AUTHORITY, Constants.TABLE_NAME + "/#/" + Constants.T_ChatID, ChatID);
        sURIMatcher.addURI(AUTHORITY, Constants.TABLE_NAME + "/#/" + Constants.T_ChatName, ChatName);
        sURIMatcher.addURI(AUTHORITY, Constants.TABLE_NAME + "/#/" + Constants.T_MessageTyp, MessageTyp);
        sURIMatcher.addURI(AUTHORITY, Constants.TABLE_NAME + "/#/" + Constants.T_SendTimestamp, SendTimestamp);
        sURIMatcher.addURI(AUTHORITY, Constants.TABLE_NAME + "/#/" + Constants.T_ReadTimestamp, ReadTimestamp);
        sURIMatcher.addURI(AUTHORITY, Constants.TABLE_NAME + "/#/" + Constants.T_TextMsgID, TextMsgID);
        sURIMatcher.addURI(AUTHORITY, Constants.TABLE_NAME + "/#/" + Constants.T_TextMsgValue, TextMsgValue);
        sURIMatcher.addURI(AUTHORITY, Constants.TABLE_NAME + "/#/" + Constants.T_ImageMsgID, ImageMsgID);
        sURIMatcher.addURI(AUTHORITY, Constants.TABLE_NAME + "/#/" + Constants.T_ImageMsgValue, ImageMsgValue);
        sURIMatcher.addURI(AUTHORITY, Constants.TABLE_NAME + "/#/" + Constants.T_FileMsgID, FileMsgID);
        sURIMatcher.addURI(AUTHORITY, Constants.TABLE_NAME + "/#/" + Constants.T_FileMsgValue, FileMsgValue);
        sURIMatcher.addURI(AUTHORITY, Constants.TABLE_NAME + "/#/" + Constants.T_LocationMsgID, LocationMsgID);
        sURIMatcher.addURI(AUTHORITY, Constants.TABLE_NAME + "/#/" + Constants.T_LocationMsgValue, LocationMsgValue);
        sURIMatcher.addURI(AUTHORITY, Constants.TABLE_NAME + "/#/" + Constants.T_ContactMsgID, ContactMsgID);
        sURIMatcher.addURI(AUTHORITY, Constants.TABLE_NAME + "/#/" + Constants.T_ContactMsgValue, ContactMsgValue);
    }

    // database
    private LocalDBHandler database;

    @Override
    public boolean onCreate() {
        database = new LocalDBHandler(getContext());
        return false;
    }

    @Override
    public Cursor query(Uri uri, String[] projection, String selection,
                        String[] selectionArgs, String sortOrder) {

        // Uisng SQLiteQueryBuilder instead of query() method
        SQLiteQueryBuilder queryBuilder = new SQLiteQueryBuilder();

        // Check if the caller has requested a column which does not exists
        checkColumns(projection);

        // Set the table
        queryBuilder.setTables(Constants.TABLE_NAME);

        int uriType = sURIMatcher.match(uri);
        switch (uriType) {
            case Frinmean_messages:
                break;
            case ID:
                queryBuilder.appendWhere(Constants.T_ID + "="
                        + uri.getLastPathSegment());
                break;
            case BADBID:
                queryBuilder.appendWhere(Constants.T_BADBID + "="
                        + uri.getLastPathSegment());
                break;
            case OwningUserID:
                queryBuilder.appendWhere(Constants.T_OwningUserID + "="
                        + uri.getLastPathSegment());
                break;
            case OwningUserName:
                queryBuilder.appendWhere(Constants.T_OwningUserName + "="
                        + uri.getLastPathSegment());
                break;
            case ChatID:
                queryBuilder.appendWhere(Constants.T_ChatID + "="
                        + uri.getLastPathSegment());
                break;
            case ChatName:
                queryBuilder.appendWhere(Constants.T_ChatName + "="
                        + uri.getLastPathSegment());
                break;
            case MessageTyp:
                queryBuilder.appendWhere(Constants.T_MessageTyp + "="
                        + uri.getLastPathSegment());
                break;
            case SendTimestamp:
                queryBuilder.appendWhere(Constants.T_SendTimestamp + "="
                        + uri.getLastPathSegment());
                break;
            case ReadTimestamp:
                queryBuilder.appendWhere(Constants.T_ReadTimestamp + "="
                        + uri.getLastPathSegment());
                break;
            case TextMsgID:
                queryBuilder.appendWhere(Constants.T_TextMsgID + "="
                        + uri.getLastPathSegment());
                break;
            case TextMsgValue:
                queryBuilder.appendWhere(Constants.T_TextMsgValue + "="
                        + uri.getLastPathSegment());
                break;
            case ImageMsgID:
                queryBuilder.appendWhere(Constants.T_ImageMsgID + "="
                        + uri.getLastPathSegment());
                break;
            case ImageMsgValue:
                queryBuilder.appendWhere(Constants.T_ImageMsgValue + "="
                        + uri.getLastPathSegment());
                break;
            case FileMsgID:
                queryBuilder.appendWhere(Constants.T_FileMsgID + "="
                        + uri.getLastPathSegment());
                break;
            case FileMsgValue:
                queryBuilder.appendWhere(Constants.T_FileMsgValue + "="
                        + uri.getLastPathSegment());
                break;
            case LocationMsgID:
                queryBuilder.appendWhere(Constants.T_LocationMsgID + "="
                        + uri.getLastPathSegment());
                break;
            case LocationMsgValue:
                queryBuilder.appendWhere(Constants.T_LocationMsgValue + "="
                        + uri.getLastPathSegment());
                break;
            case ContactMsgID:
                queryBuilder.appendWhere(Constants.T_ContactMsgID + "="
                        + uri.getLastPathSegment());
                break;
            case ContactMsgValue:
                queryBuilder.appendWhere(Constants.T_ContactMsgValue + "="
                        + uri.getLastPathSegment());
                break;


            default:
                throw new IllegalArgumentException("Unknown URI: " + uri);
        }

        SQLiteDatabase db = database.getWritableDatabase();
        Cursor cursor = queryBuilder.query(db, projection, selection,
                selectionArgs, null, null, sortOrder);
        // Make sure that potential listeners are getting notified
        cursor.setNotificationUri(getContext().getContentResolver(), uri);

        return cursor;
    }

    @Override
    public String getType(Uri uri) {
        return null;
    }


    @Override
    public Uri insert(Uri uri, ContentValues values) {
        int uriType = sURIMatcher.match(uri);
        SQLiteDatabase sqlDB = database.getWritableDatabase();
        long id = 0;
        switch (uriType) {
            case Frinmean_messages:
                id = sqlDB.insert(Constants.TABLE_NAME, null, values);
                break;
            default:
                throw new IllegalArgumentException("Unknown URI: " + uri);
        }
        getContext().getContentResolver().notifyChange(uri, null);
        return Uri.parse(Constants.TABLE_NAME + "/" + id);
    }

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        int uriType = sURIMatcher.match(uri);
        SQLiteDatabase sqlDB = database.getWritableDatabase();
        int rowsDeleted = 0;
        switch (uriType) {
            case Frinmean_messages:
                rowsDeleted = sqlDB.delete(Constants.TABLE_NAME, selection,
                        selectionArgs);
                break;
            case ID:
                String id = uri.getLastPathSegment();
                if (TextUtils.isEmpty(selection)) {
                    rowsDeleted = sqlDB.delete(Constants.TABLE_NAME,
                            Constants.DATABASE_NAME + "." + Constants.T_ID + "=" + id, null);
                } else {
                    rowsDeleted = sqlDB.delete(Constants.TABLE_NAME,
                            Constants.TABLE_NAME + "." + Constants.T_ID + "=" + id + " and " + selection,
                            selectionArgs);
                }
                break;
            default:
                throw new IllegalArgumentException("Unknown URI: " + uri);
        }
        getContext().getContentResolver().notifyChange(uri, null);
        return rowsDeleted;
    }

    @Override
    public int update(Uri uri, ContentValues values, String selection,
                      String[] selectionArgs) {

        int uriType = sURIMatcher.match(uri);
        SQLiteDatabase sqlDB = database.getWritableDatabase();
        int rowsUpdated = 0;
        switch (uriType) {
            case Frinmean_messages:
                rowsUpdated = sqlDB.update(Constants.TABLE_NAME, values, selection,
                        selectionArgs);
                break;
            case ID:
                String id = uri.getLastPathSegment();
                if (TextUtils.isEmpty(selection)) {
                    rowsUpdated = sqlDB.update(Constants.TABLE_NAME, values,
                            Constants.T_ID + "=" + id, null);
                } else {
                    rowsUpdated = sqlDB.update(Constants.TABLE_NAME, values,
                            Constants.T_ID + "=" + id + " and " + selection,
                            selectionArgs);
                }
                break;
            default:
                throw new IllegalArgumentException("Unknown URI: " + uri);
        }
        getContext().getContentResolver().notifyChange(uri, null);
        return rowsUpdated;
    }

    public Uri insertorupdate(Uri uri, ContentValues values) {
        int uriType = sURIMatcher.match(uri);
        SQLiteDatabase sqlDB = database.getWritableDatabase();
        long id = 0;
        switch (uriType) {
            case Frinmean_messages:
                // First check if values has the BADBID (Backend-ID)
                if (values.containsKey(Constants.T_BADBID)) {
                    int foundID = getID(values.getAsInteger(Constants.T_BADBID));
                    if (foundID == -1) {
                        // Backend-ID found, make an update
                        id = sqlDB.insert(Constants.TABLE_NAME, null, values);
                    } else {
                        // Backend-ID not found so insert
                        id = sqlDB.update(Constants.TABLE_NAME, values,
                                Constants.T_BADBID + "=" + foundID, null);
                    }
                } else {
                    // No Beckend ID given, jsut insert.
                    id = sqlDB.insert(Constants.TABLE_NAME, null, values);
                }
                break;
            default:
                throw new IllegalArgumentException("Unknown URI: " + uri);
        }
        getContext().getContentResolver().notifyChange(uri, null);
        return Uri.parse(Constants.TABLE_NAME + "/" + id);
    }

    private void checkColumns(String[] projection) {

        if (projection != null) {
            HashSet<String> requestedColumns = new HashSet<String>(
                    Arrays.asList(projection));
            HashSet<String> availableColumns = new HashSet<String>(
                    Arrays.asList(Constants.DB_Columns));
            // Check if all columns which are requested are available
            if (!availableColumns.containsAll(requestedColumns)) {
                throw new IllegalArgumentException(
                        "Unknown columns in projection");
            }
        }
    }

    private int getID(int inid) {
        Log.d(TAG, "start getID");
        int ret = -1;
        SQLiteDatabase db = database.getReadableDatabase();
        Cursor c = db.query(Constants.TABLE_NAME, new String[]{Constants.T_BADBID}, Constants.T_BADBID + " =?", new String[]{Integer.toString(inid)}, null, null, null, null);
        if (c.moveToFirst()) {
            ret = c.getInt(c.getColumnIndex(Constants.T_BADBID));
        }
        Log.d(TAG, "end getID");
        return ret;
    }
}
