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
    Contants for ErrorText
     */
    public final static String ERROR_NO_CONNECTION_TO_SERVER = "NO_CONNETION_TO_SERVER";
    public final static String ERROR_USER_AUTHENTICATION_FAILED = "USER_AUTHENTICATION_FAILED";
    public final static String ERROR_NONE_EXISTING_USER = "NONE_EXISTING_USER";
    public final static String ERROR_NONE_EXISTING_CHAT = "NONE_EXISTING_CHAT";
    public final static String ERROR_NO_TEXTMESSAGE_GIVEN = "NO_TEXTMESSAGE_GIVEN";
    public final static String ERROR_NONE_EXISTING_MESSAGE = "NONE_EXISTING_MESSAGE";
    public final static String ERROR_INVALID_MESSAGE_TYPE = "INVALID_MESSAGE_TYPE";
    public final static String ERROR_INVALID_EMAIL_ADRESS = "INVALID_EMAIL_ADRESS";
    public final static String ERROR_MISSING_CHATNAME = "MISSING_CHATNAME";
    public final static String ERROR_NONE_EXISTING_TEXT_MESSAGE = "NONE_EXISTING_TEXT_MESSAGE";
    public final static String ERROR_DB_ERROR = "DATABASE_ERROR";
    public final static String ERROR_USER_NOT_ACTIVE = "USER_NOT_ACTIVE";
    public final static String ERROR_WRONG_PASSWORD = "WRONG_PASSWORD";
    public final static String ERROR_NO_USERNAME_OR_PASSWORD = "NO_USERNAME_OR_PASSWORD";
    public final static String ERROR_USER_ALREADY_EXISTS = "USER_ALREADY_EXISTS";
    public final static String ERROR_NO_ACTIVE_CHATS = "NO_ACTIVE_CHATS";
    public final static String ERROR_FILE_NOT_FOUND = "FILE_NOT_FOUND";

    /*
    Constants for Result Types
     */
    public final static String RESULT_USER_ADDED_TO_CHAT = "ADDED";

    /*
    Constants for Media Types
     */

    public final static String TYP_TEXT = "TEXT";
    public final static String TYP_IMAGE = "IMAGE";
    public final static String TYP_LOCATION = "LOCATION";
    public final static String TYP_CONTACT = "CONTACT";
    public final static String TYP_FILE = "FILE";
    public final static String TYP_VIDEO = "VIDEO";

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
    public static final String TABLE_NAME = "frinme_messages";
    public static final String T_ID = "_id"; // int(10)
    public static final String T_BADBID = "ID"; // int(10)
    public static final String T_OwningUserID = "OwningUserID"; //int(10)
    public static final String T_OwningUserName = "OwingUserName"; // varchar(45)"// int(10)
    public static final String T_ChatID = "ChatID";
    public static final String T_ChatName = "ChatName"; // varchar(50)
    public static final String T_MessageTyp = "MessageTyp";// varchar(10)
    public static final String T_SendTimestamp = "SendTimeStamp"; // datetime NOT NULL,
    public static final String T_ReadTimestamp = "ReadTimeStamp"; // datetime NOT NULL DEFAULT '0000-00-00 00:00:00',
    public static final String T_TextMsgID = "TextMsgID"; // int(10) unsigned DEFAULT NULL,
    public static final String T_TextMsgValue = "TextMsgValue";  // varchar(10000)
    public static final String T_ImageMsgID = "ImageMsgID"; // int(10) unsigned DEFAULT NULL
    public static final String T_ImageMsgValue = "ImageMsgValue"; // varchar(256)
    public static final String T_VideoMsgID = "VideoMsgID"; // int(10) unsigned DEFAULT NULL
    public static final String T_VideoMsgValue = "VideoMsgValue"; // varchar(256)
    public static final String T_FileMsgID = "FileMsgID"; // int(10) unsigned DEFAULT NULL,
    public static final String T_FileMsgValue = "FileMsgValue"; // varchar(256) Pfad zur lokalein DateULT NULL,
    public static final String T_LocationMsgID = "LocationMsgID"; // int(10) unsigned DEFAULT NULL,
    public static final String T_LocationMsgValue = "LocationMsgValue"; // varchar(50)
    public static final String T_ContactMsgID = "ContactMsgID"; // int(10) unsgned DEFAULT NULL,
    public static final String T_ContactMsgValue = "ContactMsgValue"; // varchar(250)

    /*
    String Arrays for the Content Provider
     */
    public static final String[] DB_Columns = {
            T_ID,
            T_BADBID,
            T_OwningUserID,
            T_OwningUserName,
            T_ChatID,
            T_ChatName,
            T_MessageTyp,
            T_SendTimestamp,
            T_ReadTimestamp,
            T_TextMsgID,
            T_TextMsgValue,
            T_ImageMsgID,
            T_ImageMsgValue,
            T_FileMsgID,
            T_FileMsgValue,
            T_LocationMsgID,
            T_LocationMsgValue,
            T_ContactMsgID,
            T_ContactMsgValue};
}


