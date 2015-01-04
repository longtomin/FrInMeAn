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
    private static final int Frinmean_messages = 1000;
    private static final int MESSAGES_ID = 1010;
    private static final int MESSAGES_BADBID = 1020;
    private static final int MESSAGES_OwningUserID = 1030;
    private static final int MESSAGES_OwningUserName = 1040;
    private static final int MESSAGES_ChatID = 1050;
    private static final int MESSAGES_MessageTyp = 1060;
    private static final int MESSAGES_SendTimestamp = 1070;
    private static final int MESSAGES_ReadTimestamp = 1080;
    private static final int MESSAGES_TextMsgID = 1090;
    private static final int MESSAGES_TextMsgValue = 1100;
    private static final int MESSAGES_ImageMsgID = 1110;
    private static final int MESSAGES_ImageMsgValue = 1120;
    private static final int MESSAGES_FileMsgID = 1130;
    private static final int MESSAGES_FileMsgValue = 1140;
    private static final int MESSAGES_LocationMsgID = 1150;
    private static final int MESSAGES_LocationMsgValue = 1160;
    private static final int MESSAGES_ContactMsgID = 1170;
    private static final int MESSAGES_ContactMsgValue = 1180;

    private static final int Frinmean_chats = 2000;
    private static final int CHAT_ID = 2010;
    private static final int CHAT_BADBID = 2020;
    private static final int CHAT_OwningUserID = 2030;
    private static final int CHAT_OwningUserName = 2040;
    private static final int CHAT_ChatName = 2050;


    private static final String AUTHORITY = "de.radiohacks.frinmean.providers.FrinmeanContentProvider";
    public static final Uri MESSAES_CONTENT_URI = Uri.parse("content://" + AUTHORITY
            + "/" + Constants.MESSAGES_TABLE_NAME);
    public static final Uri CHAT_CONTENT_URI = Uri.parse("content://" + AUTHORITY
            + "/" + Constants.CHAT_TABLE_NAME);
    private static final UriMatcher sURIMatcher = new UriMatcher(
            UriMatcher.NO_MATCH);

    static {
        sURIMatcher.addURI(AUTHORITY, Constants.MESSAGES_TABLE_NAME, Frinmean_messages);
        sURIMatcher.addURI(AUTHORITY, Constants.MESSAGES_TABLE_NAME + "/#/" + Constants.T_MESSAGES_ID, MESSAGES_ID);
        sURIMatcher.addURI(AUTHORITY, Constants.MESSAGES_TABLE_NAME + "/#/" + Constants.T_MESSAGES_BADBID, MESSAGES_BADBID);
        sURIMatcher.addURI(AUTHORITY, Constants.MESSAGES_TABLE_NAME + "/#/" + Constants.T_MESSAGES_OwningUserID, MESSAGES_OwningUserID);
        sURIMatcher.addURI(AUTHORITY, Constants.MESSAGES_TABLE_NAME + "/#/" + Constants.T_MESSAGES_OwningUserName, MESSAGES_OwningUserName);
        sURIMatcher.addURI(AUTHORITY, Constants.MESSAGES_TABLE_NAME + "/#/" + Constants.T_MESSAGES_ChatID, MESSAGES_ChatID);
        sURIMatcher.addURI(AUTHORITY, Constants.MESSAGES_TABLE_NAME + "/#/" + Constants.T_MESSAGES_MessageTyp, MESSAGES_MessageTyp);
        sURIMatcher.addURI(AUTHORITY, Constants.MESSAGES_TABLE_NAME + "/#/" + Constants.T_MESSAGES_SendTimestamp, MESSAGES_SendTimestamp);
        sURIMatcher.addURI(AUTHORITY, Constants.MESSAGES_TABLE_NAME + "/#/" + Constants.T_MESSAGES_ReadTimestamp, MESSAGES_ReadTimestamp);
        sURIMatcher.addURI(AUTHORITY, Constants.MESSAGES_TABLE_NAME + "/#/" + Constants.T_MESSAGES_TextMsgID, MESSAGES_TextMsgID);
        sURIMatcher.addURI(AUTHORITY, Constants.MESSAGES_TABLE_NAME + "/#/" + Constants.T_MESSAGES_TextMsgValue, MESSAGES_TextMsgValue);
        sURIMatcher.addURI(AUTHORITY, Constants.MESSAGES_TABLE_NAME + "/#/" + Constants.T_MESSAGES_ImageMsgID, MESSAGES_ImageMsgID);
        sURIMatcher.addURI(AUTHORITY, Constants.MESSAGES_TABLE_NAME + "/#/" + Constants.T_MESSAGES_ImageMsgValue, MESSAGES_ImageMsgValue);
        sURIMatcher.addURI(AUTHORITY, Constants.MESSAGES_TABLE_NAME + "/#/" + Constants.T_MESSAGES_FileMsgID, MESSAGES_FileMsgID);
        sURIMatcher.addURI(AUTHORITY, Constants.MESSAGES_TABLE_NAME + "/#/" + Constants.T_MESSAGES_FileMsgValue, MESSAGES_FileMsgValue);
        sURIMatcher.addURI(AUTHORITY, Constants.MESSAGES_TABLE_NAME + "/#/" + Constants.T_MESSAGES_LocationMsgID, MESSAGES_LocationMsgID);
        sURIMatcher.addURI(AUTHORITY, Constants.MESSAGES_TABLE_NAME + "/#/" + Constants.T_MESSAGES_LocationMsgValue, MESSAGES_LocationMsgValue);
        sURIMatcher.addURI(AUTHORITY, Constants.MESSAGES_TABLE_NAME + "/#/" + Constants.T_MESSAGES_ContactMsgID, MESSAGES_ContactMsgID);
        sURIMatcher.addURI(AUTHORITY, Constants.MESSAGES_TABLE_NAME + "/#/" + Constants.T_MESSAGES_ContactMsgValue, MESSAGES_ContactMsgValue);
    }

    static {
        sURIMatcher.addURI(AUTHORITY, Constants.CHAT_TABLE_NAME, Frinmean_chats);
        sURIMatcher.addURI(AUTHORITY, Constants.CHAT_TABLE_NAME + "/#/" + Constants.T_CHAT_ID, CHAT_ID);
        sURIMatcher.addURI(AUTHORITY, Constants.CHAT_TABLE_NAME + "/#/" + Constants.T_CHAT_BADBID, CHAT_BADBID);
        sURIMatcher.addURI(AUTHORITY, Constants.CHAT_TABLE_NAME + "/#/" + Constants.T_CHAT_OwningUserID, CHAT_OwningUserID);
        sURIMatcher.addURI(AUTHORITY, Constants.CHAT_TABLE_NAME + "/#/" + Constants.T_CHAT_OwningUserName, CHAT_OwningUserName);
        sURIMatcher.addURI(AUTHORITY, Constants.CHAT_TABLE_NAME + "/#/" + Constants.T_CHAT_ChatName, CHAT_ChatName);
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


        // Set the table
        int uriType = sURIMatcher.match(uri);
        if (uriType >= 1000 && uriType <= 1180) {
            // Check if the caller has requested a column which does not exists
            checkColumns(projection, Constants.MESSAGES_TABLE_NAME);

            queryBuilder.setTables(Constants.MESSAGES_TABLE_NAME);
            switch (uriType) {
                case Frinmean_messages:
                    break;
                case MESSAGES_ID:
                    queryBuilder.appendWhere(Constants.T_MESSAGES_ID + "="
                            + uri.getLastPathSegment());
                    break;
                case MESSAGES_BADBID:
                    queryBuilder.appendWhere(Constants.T_MESSAGES_BADBID + "="
                            + uri.getLastPathSegment());
                    break;
                case MESSAGES_OwningUserID:
                    queryBuilder.appendWhere(Constants.T_MESSAGES_OwningUserID + "="
                            + uri.getLastPathSegment());
                    break;
                case MESSAGES_OwningUserName:
                    queryBuilder.appendWhere(Constants.T_MESSAGES_OwningUserName + "="
                            + uri.getLastPathSegment());
                    break;
                case MESSAGES_ChatID:
                    queryBuilder.appendWhere(Constants.T_MESSAGES_ChatID + "="
                            + uri.getLastPathSegment());
                    break;
                case MESSAGES_MessageTyp:
                    queryBuilder.appendWhere(Constants.T_MESSAGES_MessageTyp + "="
                            + uri.getLastPathSegment());
                    break;
                case MESSAGES_SendTimestamp:
                    queryBuilder.appendWhere(Constants.T_MESSAGES_SendTimestamp + "="
                            + uri.getLastPathSegment());
                    break;
                case MESSAGES_ReadTimestamp:
                    queryBuilder.appendWhere(Constants.T_MESSAGES_ReadTimestamp + "="
                            + uri.getLastPathSegment());
                    break;
                case MESSAGES_TextMsgID:
                    queryBuilder.appendWhere(Constants.T_MESSAGES_TextMsgID + "="
                            + uri.getLastPathSegment());
                    break;
                case MESSAGES_TextMsgValue:
                    queryBuilder.appendWhere(Constants.T_MESSAGES_TextMsgValue + "="
                            + uri.getLastPathSegment());
                    break;
                case MESSAGES_ImageMsgID:
                    queryBuilder.appendWhere(Constants.T_MESSAGES_ImageMsgID + "="
                            + uri.getLastPathSegment());
                    break;
                case MESSAGES_ImageMsgValue:
                    queryBuilder.appendWhere(Constants.T_MESSAGES_ImageMsgValue + "="
                            + uri.getLastPathSegment());
                    break;
                case MESSAGES_FileMsgID:
                    queryBuilder.appendWhere(Constants.T_MESSAGES_FileMsgID + "="
                            + uri.getLastPathSegment());
                    break;
                case MESSAGES_FileMsgValue:
                    queryBuilder.appendWhere(Constants.T_MESSAGES_FileMsgValue + "="
                            + uri.getLastPathSegment());
                    break;
                case MESSAGES_LocationMsgID:
                    queryBuilder.appendWhere(Constants.T_MESSAGES_LocationMsgID + "="
                            + uri.getLastPathSegment());
                    break;
                case MESSAGES_LocationMsgValue:
                    queryBuilder.appendWhere(Constants.T_MESSAGES_LocationMsgValue + "="
                            + uri.getLastPathSegment());
                    break;
                case MESSAGES_ContactMsgID:
                    queryBuilder.appendWhere(Constants.T_MESSAGES_ContactMsgID + "="
                            + uri.getLastPathSegment());
                    break;
                case MESSAGES_ContactMsgValue:
                    queryBuilder.appendWhere(Constants.T_MESSAGES_ContactMsgValue + "="
                            + uri.getLastPathSegment());
                    break;
                default:
                    throw new IllegalArgumentException("Unknown URI: " + uri);
            }
        } else {
            // Check if the caller has requested a column which does not exists
            checkColumns(projection, Constants.CHAT_TABLE_NAME);

            queryBuilder.setTables(Constants.CHAT_TABLE_NAME);
            switch (uriType) {
                case Frinmean_chats:
                    break;
                case CHAT_ID:
                    queryBuilder.appendWhere(Constants.T_CHAT_ID + "="
                            + uri.getLastPathSegment());
                    break;
                case CHAT_BADBID:
                    queryBuilder.appendWhere(Constants.T_CHAT_BADBID + "="
                            + uri.getLastPathSegment());
                    break;
                case CHAT_OwningUserID:
                    queryBuilder.appendWhere(Constants.T_CHAT_OwningUserID + "="
                            + uri.getLastPathSegment());
                    break;
                case CHAT_OwningUserName:
                    queryBuilder.appendWhere(Constants.T_CHAT_OwningUserName + "="
                            + uri.getLastPathSegment());
                    break;
                case CHAT_ChatName:
                    queryBuilder.appendWhere(Constants.T_CHAT_ChatName + "="
                            + uri.getLastPathSegment());
                    break;
                default:
                    throw new IllegalArgumentException("Unknown URI: " + uri);
            }
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
        Uri ret;
        long id;
        switch (uriType) {
            case Frinmean_messages:
                id = sqlDB.insert(Constants.MESSAGES_TABLE_NAME, null, values);
                ret = Uri.parse(Constants.MESSAGES_TABLE_NAME + "/" + id);
                break;
            case Frinmean_chats:
                id = sqlDB.insert(Constants.CHAT_TABLE_NAME, null, values);
                ret = Uri.parse(Constants.CHAT_TABLE_NAME + "/" + id);
                break;
            default:
                throw new IllegalArgumentException("Unknown URI: " + uri);
        }
        getContext().getContentResolver().notifyChange(uri, null);
        return ret;
    }

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        int uriType = sURIMatcher.match(uri);
        SQLiteDatabase sqlDB = database.getWritableDatabase();
        int rowsDeleted;
        switch (uriType) {
            case Frinmean_messages:
                rowsDeleted = sqlDB.delete(Constants.MESSAGES_TABLE_NAME, selection,
                        selectionArgs);
                break;
            case Frinmean_chats:
                rowsDeleted = sqlDB.delete(Constants.CHAT_TABLE_NAME, selection,
                        selectionArgs);
                break;
            case MESSAGES_ID:
                String msgid = uri.getLastPathSegment();
                if (TextUtils.isEmpty(selection)) {
                    rowsDeleted = sqlDB.delete(Constants.MESSAGES_TABLE_NAME,
                            Constants.DATABASE_NAME + "." + Constants.T_MESSAGES_ID + "=" + msgid, null);
                } else {
                    rowsDeleted = sqlDB.delete(Constants.MESSAGES_TABLE_NAME,
                            Constants.MESSAGES_TABLE_NAME + "." + Constants.T_MESSAGES_ID + "=" + msgid + " and " + selection,
                            selectionArgs);
                }
                break;
            case CHAT_ID:
                String chatid = uri.getLastPathSegment();
                if (TextUtils.isEmpty(selection)) {
                    rowsDeleted = sqlDB.delete(Constants.CHAT_TABLE_NAME,
                            Constants.DATABASE_NAME + "." + Constants.T_CHAT_ID + "=" + chatid, null);
                } else {
                    rowsDeleted = sqlDB.delete(Constants.CHAT_TABLE_NAME,
                            Constants.CHAT_TABLE_NAME + "." + Constants.T_CHAT_ID + "=" + chatid + " and " + selection,
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
        int rowsUpdated;
        switch (uriType) {
            case Frinmean_messages:
                rowsUpdated = sqlDB.update(Constants.MESSAGES_TABLE_NAME, values, selection,
                        selectionArgs);
                break;
            case Frinmean_chats:
                rowsUpdated = sqlDB.update(Constants.CHAT_TABLE_NAME, values, selection,
                        selectionArgs);
                break;
            case MESSAGES_ID:
                String msgid = uri.getLastPathSegment();
                if (TextUtils.isEmpty(selection)) {
                    rowsUpdated = sqlDB.update(Constants.MESSAGES_TABLE_NAME, values,
                            Constants.T_MESSAGES_ID + "=" + msgid, null);
                } else {
                    rowsUpdated = sqlDB.update(Constants.MESSAGES_TABLE_NAME, values,
                            Constants.T_MESSAGES_ID + "=" + msgid + " and " + selection,
                            selectionArgs);
                }
                break;
            case CHAT_ID:
                String chatid = uri.getLastPathSegment();
                if (TextUtils.isEmpty(selection)) {
                    rowsUpdated = sqlDB.update(Constants.CHAT_TABLE_NAME, values,
                            Constants.T_CHAT_ID + "=" + chatid, null);
                } else {
                    rowsUpdated = sqlDB.update(Constants.CHAT_TABLE_NAME, values,
                            Constants.T_CHAT_ID + "=" + chatid + " and " + selection,
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
        Uri ret;
        long id;
        switch (uriType) {
            case Frinmean_messages:
                // First check if values has the BADBID (Backend-ID)
                if (values.containsKey(Constants.T_MESSAGES_BADBID)) {
                    int foundID = getID(values.getAsInteger(Constants.T_MESSAGES_BADBID), Constants.MESSAGES_TABLE_NAME);
                    if (foundID == -1) {
                        // Backend-ID found, make an update
                        id = sqlDB.insert(Constants.MESSAGES_TABLE_NAME, null, values);
                    } else {
                        // Backend-ID not found so insert
                        id = sqlDB.update(Constants.MESSAGES_TABLE_NAME, values,
                                Constants.T_MESSAGES_BADBID + "=" + foundID, null);
                    }
                } else {
                    // No Beckend ID given, jsut insert.
                    id = sqlDB.insert(Constants.MESSAGES_TABLE_NAME, null, values);
                }
                ret = Uri.parse(Constants.MESSAGES_TABLE_NAME + "/" + id);
                break;
            case Frinmean_chats:
                // First check if values has the BADBID (Backend-ID)
                if (values.containsKey(Constants.T_CHAT_BADBID)) {
                    int foundID = getID(values.getAsInteger(Constants.T_CHAT_BADBID), Constants.CHAT_TABLE_NAME);
                    if (foundID == -1) {
                        // Backend-ID found, make an update
                        id = sqlDB.insert(Constants.CHAT_TABLE_NAME, null, values);
                    } else {
                        // Backend-ID not found so insert
                        id = sqlDB.update(Constants.CHAT_TABLE_NAME, values,
                                Constants.T_CHAT_BADBID + "=" + foundID, null);
                    }
                } else {
                    // No Beckend ID given, jsut insert.
                    id = sqlDB.insert(Constants.CHAT_TABLE_NAME, null, values);
                }
                ret = Uri.parse(Constants.CHAT_TABLE_NAME + "/" + id);
                break;
            default:
                throw new IllegalArgumentException("Unknown URI: " + uri);
        }
        getContext().getContentResolver().notifyChange(uri, null);
        return ret;
    }

    private void checkColumns(String[] projection, String tablename) {

        if (projection != null) {
            HashSet<String> requestedColumns = new HashSet<String>(
                    Arrays.asList(projection));
            HashSet<String> availableColumns = null;
            if (tablename.equalsIgnoreCase(Constants.MESSAGES_TABLE_NAME)) {
                availableColumns = new HashSet<String>(
                        Arrays.asList(Constants.MESSAGES_DB_Columns));
            } else if (tablename.equalsIgnoreCase(Constants.CHAT_TABLE_NAME)) {
                availableColumns = new HashSet<String>(
                        Arrays.asList(Constants.CHAT_DB_Columns));
            }
            // Check if all columns which are requested are available
            if (!availableColumns.containsAll(requestedColumns)) {
                throw new IllegalArgumentException(
                        "Unknown columns in projection");
            }
        }
    }

    private int getID(int inid, String tablename) {
        Log.d(TAG, "start getID");
        int ret = -1;
        SQLiteDatabase db = database.getReadableDatabase();
        if (tablename.equalsIgnoreCase(Constants.MESSAGES_TABLE_NAME)) {
            Cursor c = db.query(tablename, new String[]{Constants.T_MESSAGES_BADBID}, Constants.T_MESSAGES_BADBID + " =?", new String[]{Integer.toString(inid)}, null, null, null, null);
            if (c.moveToFirst()) {
                ret = c.getInt(c.getColumnIndex(Constants.T_MESSAGES_BADBID));
            }
        } else if (tablename.equalsIgnoreCase(Constants.CHAT_TABLE_NAME)) {
            Cursor c = db.query(tablename, new String[]{Constants.T_CHAT_BADBID}, Constants.T_CHAT_BADBID + " =?", new String[]{Integer.toString(inid)}, null, null, null, null);
            if (c.moveToFirst()) {
                ret = c.getInt(c.getColumnIndex(Constants.T_CHAT_BADBID));
            }
        }
        Log.d(TAG, "end getID");
        return ret;
    }
}
