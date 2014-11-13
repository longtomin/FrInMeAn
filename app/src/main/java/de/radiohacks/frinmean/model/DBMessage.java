package de.radiohacks.frinmean.model;

/**
 * Created by thomas on 07.09.14.
 */
public class DBMessage {

    private int ID;
    private int OwningUserID;
    private String OwingUserName;
    private int ChatID;
    private String ChatName;
    private String MessageTyp;
    private long SendTimeStamp;
    private long ReadTimeStamp;
    private int TextMsgID;
    private String TextMsgValue;
    private int ImageMsgID;
    private String ImageMsgValue;
    private int FileMsgID;
    private String FileMsgValue;
    private int LocationMsgID;
    private String LocationMsgValue;
    private int ContactMsgID;
    private String ContactMsgValue;

    public int getID() {
        return ID;
    }

    public void setID(int ID) {
        this.ID = ID;
    }

    public int getOwningUserID() {
        return OwningUserID;
    }

    public void setOwningUserID(int owningUserID) {
        OwningUserID = owningUserID;
    }

    public String getOwingUserName() {
        return OwingUserName;
    }

    public void setOwingUserName(String owingUserName) {
        OwingUserName = owingUserName;
    }

    public int getChatID() {
        return ChatID;
    }

    public void setChatID(int chatID) {
        ChatID = chatID;
    }

    public String getChatName() {
        return ChatName;
    }

    public void setChatName(String chatName) {
        ChatName = chatName;
    }

    public String getMessageTyp() {
        return MessageTyp;
    }

    public void setMessageTyp(String messageTyp) {
        MessageTyp = messageTyp;
    }

    public long getSendTimeStamp() {
        return SendTimeStamp;
    }

    public void setSendTimeStamp(long sendTimeStamp) {
        SendTimeStamp = sendTimeStamp;
    }

    public long getReadTimeStamp() {
        return ReadTimeStamp;
    }

    public void setReadTimeStamp(long readTimeStamp) {
        ReadTimeStamp = readTimeStamp;
    }

    public int getTextMsgID() {
        return TextMsgID;
    }

    public void setTextMsgID(int textMsgID) {
        TextMsgID = textMsgID;
    }

    public String getTextMsgValue() {
        return TextMsgValue;
    }

    public void setTextMsgValue(String textMsgValue) {
        TextMsgValue = textMsgValue;
    }

    public int getImageMsgID() {
        return ImageMsgID;
    }

    public void setImageMsgID(int imageMsgID) {
        ImageMsgID = imageMsgID;
    }

    public String getImageMsgValue() {
        return ImageMsgValue;
    }

    public void setImageMsgValue(String imageMsgValue) {
        ImageMsgValue = imageMsgValue;
    }

    public int getFileMsgID() {
        return FileMsgID;
    }

    public void setFileMsgID(int fileMsgID) {
        FileMsgID = fileMsgID;
    }

    public String getFileMsgValue() {
        return FileMsgValue;
    }

    public void setFileMsgValue(String fileMsgValue) {
        FileMsgValue = fileMsgValue;
    }

    public int getLocationMsgID() {
        return LocationMsgID;
    }

    public void setLocationMsgID(int locationMsgID) {
        LocationMsgID = locationMsgID;
    }

    public String getLocationMsgValue() {
        return LocationMsgValue;
    }

    public void setLocationMsgValue(String locationMsgValue) {
        LocationMsgValue = locationMsgValue;
    }

    public int getContactMsgID() {
        return ContactMsgID;
    }

    public void setContactMsgID(int contactMsgID) {
        ContactMsgID = contactMsgID;
    }

    public String getContactMsgValue() {
        return ContactMsgValue;
    }

    public void setContactMsgValue(String contactMsgValue) {
        ContactMsgValue = contactMsgValue;
    }
}
