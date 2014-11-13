package de.radiohacks.frinmean.model;

import java.util.List;

/**
 * Created by thomas on 19.10.14.
 */
public class DBMessageList {

    public List<DBMessage> DBMessages;

    public DBMessageList() {
    }

    public List<DBMessage> getDBMessageList() {
        return this.DBMessages;
    }

    public void setDBMessageList(List<DBMessage> in) {
        this.DBMessages = in;
    }
}

