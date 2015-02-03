package de.radiohacks.frinmean;

import java.util.Locale;

/**
 * Created by thomas on 19.08.14.
 */
public class Constants {
    /*
    Contants for Actions in the Communication Service
     */
    public static final String ACTION_SIGNUP = "signup";
    public static final String ACTION_AUTHENTICATE = "authenticate";
    public static final String ACTION_CREATECHAT = "createchat";
    public static final String ACTION_DELETECHAT = "deletechat";
    public static final String ACTION_ADDUSERTOCHAT = "addusertochat";
    public static final String ACTION_REMOVEUSERFROMCHAT = "removeuserfromchat";
    public static final String ACTION_LISTUSER = "listuser";
    public static final String ACTION_LISTCHAT = "listchat";
    public static final String ACTION_SENDTEXTMESSAGE = "sendtextmessage";
    public static final String ACTION_SENDIMAGEMESSAGE = "sendimagemessage";
    public static final String ACTION_SENDVIDEOMESSAGE = "sendvideomessage";
    public static final String ACTION_GETTEXTMESSAGE = "gettextmessage";
    public static final String ACTION_INSERTMESSAGEINTOCHAT = "insertmessageintochat";
    public static final String ACTION_GETMESSAGEFROMCHAT = "getmessagefromchat";
    public static final String ACTION_CHECKNEWMESSAGES = "checknewmessages";

    /*
    App own action without correspondance in the backend
     */
    public static final String ACTION_RELOAD_SETTING = "reloadsettings";
    public static final String ACTION_FULLSYNC = "fullsync";

    /*
    Contants for ErrorText
     */

    public final static String ERROR_NO_CONNECTION_TO_SERVER = "NO_CONNETION_TO_SERVER";
    public final static String ERROR_NONE_EXISTING_USER = "NONE_EXISTING_USER";
    public final static String ERROR_NONE_EXISTING_CHAT = "NONE_EXISTING_CHAT";
    public final static String ERROR_NO_TEXTMESSAGE_GIVEN = "NO_TEXTMESSAGE_GIVEN";
    public static final String ERROR_NO_IMAGEMESSAGE_GIVEN = "NO_IMAGEMESSAGE_GIVEN";
    public final static String ERROR_NONE_EXISTING_MESSAGE = "NONE_EXISTING_MESSAGE";
    public final static String ERROR_INVALID_MESSAGE_TYPE = "INVALID_MESSAGE_TYPE";
    public final static String ERROR_INVALID_EMAIL_ADRESS = "INVALID_EMAIL_ADRESS";
    public final static String ERROR_MISSING_CHATNAME = "MISSING_CHATNAME";
    public final static String ERROR_NONE_EXISTING_TEXT_MESSAGE = "NONE_EXISTING_TEXT_MESSAGE";
    public final static String ERROR_TYPE_NOT_FOUND = "TYPE_NOT_FOUND";
    public final static String ERROR_FILE_NOT_FOUND = "FILE_NOT_FOUND";
    public final static String ERROR_NOT_MESSAGE_OWNER = "NOT_MESSAGE_OWNER";
    public final static String ERROR_NOT_CHAT_OWNER = "NOT_CHAT_OWNER";
    public final static String ERROR_DB_ERROR = "DATABASE_ERROR";
    public final static String ERROR_USER_NOT_ACTIVE = "USER_NOT_ACTIVE";
    public final static String ERROR_WRONG_PASSWORD = "WRONG_PASSWORD";
    public final static String ERROR_NO_USERNAME_OR_PASSWORD = "NO_USERNAME_OR_PASSWORD";
    public final static String ERROR_USER_ALREADY_EXISTS = "USER_ALREADY_EXISTS";
    public final static String ERROR_NO_ACTIVE_CHATS = "NO_ACTIVE_CHATS";
    public final static String ERROR_USER_ALREADY_IN_CHAT = "USER_ALREADY_IN_CHAT";

        /*
    Contants for ResultText
     */

    public final static String RESULT_USER_ADDED = "USER_ADDED";
    public final static String RESULT_SIGNUP_SUCCESSFUL = "SIGNUP_SUCCESSFUL";

        /*
    Contants for AuthenticationText
     */

    public final static String AUTHENTICATE_TRUE = "TRUE";
    public final static String AUTHENTICATE_FALSE = "FALSE";

        /*
    Constants for Media Types
     */

    public final static String TYP_TEXT = "TEXT";
    public final static String TYP_IMAGE = "IMAGE";
    public final static String TYP_LOCATION = "LOCATION";
    public final static String TYP_CONTACT = "CONTACT";
    public final static String TYP_FILE = "FILE";
    public final static String TYP_VIDEO = "VIDEO";

    /* Where should the content go to on the Server */
    public static final String SERVER_UPLOAD_LOCATION_FOLDER = "/opt/frinme-data/";

    /* Constants for the query Parameter names */
    public static final String QPusername = "username";
    public static final String QPpassword = "password";
    public static final String QPemail = "email";
    public static final String QPchatname = "chatname";
    public static final String QPchatid = "chatid";
    public static final String QPuserid = "userid";
    public static final String QPsearch = "search";
    public static final String QPtextmessage = "textmessage";
    public static final String QPtextmessageid = "textmessageid";
    public static final String QPmessageid = "messageid";
    public static final String QPmessagetype = "messagetype";
    public static final String QPtimestamp = "timestamp";
    public static final String QPimageid = "imageid";
    public static final String QPvideoid = "videoid";


    /*
    Constants for Result Types
     */
    public final static String RESULT_USER_ADDED_TO_CHAT = "ADDED";

    /*
    Constants for Thumbnail Picutres
     */

    public final static String THUMBNAIL_TYPE = "THUMBNAIL_TYPE";
    public final static String THUMBNAIL_USER = "USER_THUMBNAIL";
    public final static String THUMBNAUL_CHAT = "CHAT_THUMBNAIL";


    /*
    Constants for Directories
     */

    public final static String IMAGEDIR = "images";
    public final static String IMAGEPREVIEWDIR = "images/preview";
    public final static String CHATIAMGEDIR = "images/chatimage";
    public final static String FILESDIR = "files";
    public final static String VIDEODIR = "videos";

    public final static String USERNAME = "username";
    public final static String PASSWORD = "password";
    public final static String CHATNAME = "chatname";
    public final static String EMAIL = "email";
    public final static String CHATID = "chatid";
    public final static String USERID = "userid";
    public final static String TIMESTAMP = "timestamp";
    public final static String TEXTMESSAGE = "textmessage";
    public final static String OWNINGUSERID = "owninguserid";
    public final static String OWNINGUSERNAME = "owningusername";
    public final static String MESSAGEID = "messageid";
    public final static String MESSAGETYPE = "messagetype";
    public final static String IMAGELOCATION = "imagelocation";
    public final static String VIDEOLOCATION = "videolocation";
    public final static String SEARCH = "search";

    /*
    Constants for Broadcasts
     */
    public static final String BROADCAST_ACTION = "de.radiohacks.frinmean.BROADCAST";
    public final static String BROADCAST_DATA = "de.radiohacks.frinmean.DATA";

    public final static String BROADCAST_SIGNUP = "de.radiohacks.frinmean.SIGNUP";
    public final static String BROADCAST_AUTHENTICATE = "de.radiohacks.frinmean.AUTHENTICATE";
    public static final String BROADCAST_CREATECHAT = "de.radiohacks.frinmean.CREATECHAT";
    public static final String BROADCAST_DELETECHAT = "de.radiohacks.frinmean.DELETECHAT";
    public static final String BROADCAST_ADDUSERTOCHAT = "de.radiohacks.frinmean.ADDUSERTOCHAT";
    public static final String BROADCAST_USERADDEDTOCHAT = "de.radiohacks.frinmean.USERADDEDTOCHAT";
    public static final String BROADCAST_REMOVEUSERFROMCHAT = "de.radiohacks.frinmean.REMOVEUSERFROMCHAT";
    public static final String BROADCAST_LISTUSER = "de.radiohacks.frinmean.LISTUSER";
    public final static String BROADCAST_LISTCHAT = "de.radiohacks.frinmean.LISTCHAT";
    public final static String BROADCAST_SENDTEXTMESSAGE = "de.radiohacks.frinmean.SENDTEXTMESSAGE";
    public static final String BROADCAST_GETTEXTMESSAGE = "de.radiohacks.frinmean.GETTEXTMESSAGE";
    public final static String BROADCAST_INSERTMESSAGEINTOCHAT = "de.radiohacks.frinmean.INSERTMESSAGEINTOCHAT";
    public final static String BROADCAST_GETMESSAGEFROMCHAT = "de.radiohacks.frinmean.GETMESSAGEFROMCHAT";
    public static final String BROADCAST_CHECKNEWMESSAGES = "de.radiohacks.frinmean.CHECKNEWMESSAGES";

    /*
     * A user-agent string that's sent to the HTTP site. It includes information about the device
     * and the build that the device is running.
     */
    public static final String USER_AGENT = "Mozilla/5.0 (Linux; U; Android "
            + android.os.Build.VERSION.RELEASE + ";"
            + Locale.getDefault().toString() + "; " + android.os.Build.DEVICE
            + "/" + android.os.Build.ID + ")";

    public static final String DATABASE_NAME = "Frinme.db";
    public static final String MESSAGES_TABLE_NAME = "frinme_messages";
    public static final String CHAT_TABLE_NAME = "frinme_chats";
    public static final String T_MESSAGES_ID = "_id"; // int(10)
    public static final String T_MESSAGES_BADBID = "ID"; // int(10)
    public static final String T_MESSAGES_OwningUserID = "OwningUserID"; //int(10)
    public static final String T_MESSAGES_OwningUserName = "OwingUserName"; // varchar(45)"// int(10)
    public static final String T_MESSAGES_ChatID = "ChatID";
    public static final String T_MESSAGES_MessageTyp = "MessageTyp";// varchar(10)
    public static final String T_MESSAGES_SendTimestamp = "SendTimeStamp"; // datetime NOT NULL,
    public static final String T_MESSAGES_ReadTimestamp = "ReadTimeStamp"; // datetime NOT NULL DEFAULT '0000-00-00 00:00:00',
    public static final String T_MESSAGES_TextMsgID = "TextMsgID"; // int(10) unsigned DEFAULT NULL,
    public static final String T_MESSAGES_TextMsgValue = "TextMsgValue";  // varchar(10000)
    public static final String T_MESSAGES_ImageMsgID = "ImageMsgID"; // int(10) unsigned DEFAULT NULL
    public static final String T_MESSAGES_ImageMsgValue = "ImageMsgValue"; // varchar(256)
    public static final String T_MESSAGES_VideoMsgID = "VideoMsgID"; // int(10) unsigned DEFAULT NULL
    public static final String T_MESSAGES_VideoMsgValue = "VideoMsgValue"; // varchar(256)
    public static final String T_MESSAGES_FileMsgID = "FileMsgID"; // int(10) unsigned DEFAULT NULL,
    public static final String T_MESSAGES_FileMsgValue = "FileMsgValue"; // varchar(256) Pfad zur lokalein DateULT NULL,
    public static final String T_MESSAGES_LocationMsgID = "LocationMsgID"; // int(10) unsigned DEFAULT NULL,
    public static final String T_MESSAGES_LocationMsgValue = "LocationMsgValue"; // varchar(50)
    public static final String T_MESSAGES_ContactMsgID = "ContactMsgID"; // int(10) unsgned DEFAULT NULL,
    public static final String T_MESSAGES_ContactMsgValue = "ContactMsgValue"; // varchar(250)
    /*
    String Arrays for the Content Provider
     */
    public static final String[] MESSAGES_DB_Columns = {
            T_MESSAGES_ID,
            T_MESSAGES_BADBID,
            T_MESSAGES_OwningUserID,
            T_MESSAGES_OwningUserName,
            T_MESSAGES_ChatID,
            T_MESSAGES_MessageTyp,
            T_MESSAGES_SendTimestamp,
            T_MESSAGES_ReadTimestamp,
            T_MESSAGES_TextMsgID,
            T_MESSAGES_TextMsgValue,
            T_MESSAGES_ImageMsgID,
            T_MESSAGES_ImageMsgValue,
            T_MESSAGES_FileMsgID,
            T_MESSAGES_FileMsgValue,
            T_MESSAGES_LocationMsgID,
            T_MESSAGES_LocationMsgValue,
            T_MESSAGES_ContactMsgID,
            T_MESSAGES_ContactMsgValue};
    public static final String T_CHAT_ID = "_id"; // int(10)
    public static final String T_CHAT_BADBID = "ID"; // int(10)
    public static final String T_CHAT_OwningUserID = "OwningUserID"; //int(10)
    public static final String T_CHAT_OwningUserName = "OwingUserName"; // varchar(45)"// int(10)
    public static final String T_CHAT_ChatName = "ChatName"; // varchar(50)
    public static final String[] CHAT_DB_Columns = {
            T_CHAT_ID,
            T_CHAT_BADBID,
            T_CHAT_OwningUserID,
            T_CHAT_OwningUserName,
            T_CHAT_ChatName};

    /*
    Integer-ID of the Database Rows
     */

    public static final int ID_MESSAGES__id = 0;
    public static final int ID_MESSAGES_BADBID = 1;
    public static final int ID_MESSAGES_OwningUserID = 2;
    public static final int ID_MESSAGES_OwningUserName = 3;
    public static final int ID_MESSAGES_ChatID = 4;
    public static final int ID_MESSAGES_MessageType = 5;
    public static final int ID_MESSAGES_SendTimeStamp = 6;
    public static final int ID_MESSAGES_ReadTimeStamp = 7;
    public static final int ID_MESSAGES_TextMsgID = 8;
    public static final int ID_MESSAGES_TextMsgValue = 9;
    public static final int ID_MESSAGES_ImageMsgID = 10;
    public static final int ID_MESSAGES_ImageMsgValue = 11;
    public static final int ID_MESSAGES_VideoMsgID = 12;
    public static final int ID_MESSAGES_VideoMsgValue = 13;
    public static final int ID_MESSAGES_FileMsgID = 14;
    public static final int ID_MESSAGES_FileMsgValue = 15;
    public static final int ID_MESSAGES_LocationMsgID = 16;
    public static final int ID_MESSAGES_LocationMsgValue = 17;
    public static final int ID_MESSAGES_ContactMsgID = 18;
    public static final int ID_MESSAGES_ContactMsgValue = 19;

    public static final int ID_CHAT__id = 0;
    public static final int ID_CHAT_BADBID = 1;
    public static final int ID_CHAT_OwningUserID = 2;
    public static final int ID_CHAT_OwningUserName = 3;
    public static final int ID_CHAT_ChatName = 4;

    /*
    Constants for Preferences
     */

    public static final String PrefServerport = "prefServerport";
    public static final String PrefServername = "prefServername";
    public static final String PrefHTTPSCommunication = "prefHTTPSCommunication";
    public static final String PrefUsername = "prefUsername";
    public static final String PrefPassword = "prefPassword";
    public static final String PrefDirectory = "prefDirectory";
    public static final String PrefSyncfrequency = "prefSyncfrequency";


    public static final String PREF_SETUP_COMPLETE = "setup_complete";
    // Value below must match the account type specified in res/xml/syncadapter.xml
    public static final String ACCOUNT_TYPE = "de.radiohacks.frinmean.account";
}


