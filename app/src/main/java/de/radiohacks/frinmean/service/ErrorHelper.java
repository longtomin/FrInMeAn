package de.radiohacks.frinmean.service;

import android.content.Context;
import android.widget.Toast;

import de.radiohacks.frinmean.Constants;
import de.radiohacks.frinmean.R;

/**
 * Created by thomas on 24.08.14.
 */
public class ErrorHelper {
    protected Context ctx;

    public ErrorHelper(Context in) {
        this.ctx = in;
    }

    /*
    Returns True if an error occured
     */
    public boolean CheckErrorText(String Text) {
        boolean ret = true;
        if (!Text.isEmpty()) {
            if (Text.equalsIgnoreCase(Constants.ERROR_NO_CONNECTION_TO_SERVER)) {
                Toast.makeText(ErrorHelper.this.ctx, ctx.getString(R.string.ERROR_NO_CONNECTION_TO_SERVER), Toast.LENGTH_SHORT).show();
            } else if (Text.equalsIgnoreCase(Constants.ERROR_USER_AUTHENTICATION_FAILED)) {
                Toast.makeText(ErrorHelper.this.ctx, ctx.getString(R.string.ERROR_USER_AUTHENTICATION_FAILED), Toast.LENGTH_SHORT).show();
            } else if (Text.equalsIgnoreCase(Constants.ERROR_NONE_EXISTING_USER)) {
                Toast.makeText(ErrorHelper.this.ctx, ctx.getString(R.string.ERROR_NONE_EXISTING_USER), Toast.LENGTH_SHORT).show();
            } else if (Text.equalsIgnoreCase(Constants.ERROR_NONE_EXISTING_CHAT)) {
                Toast.makeText(ErrorHelper.this.ctx, ctx.getString(R.string.ERROR_NONE_EXISTING_CHAT), Toast.LENGTH_SHORT).show();
            } else if (Text.equalsIgnoreCase(Constants.ERROR_NO_TEXTMESSAGE_GIVEN)) {
                Toast.makeText(ErrorHelper.this.ctx, ctx.getString(R.string.ERROR_NO_TEXTMESSAGE_GIVEN), Toast.LENGTH_SHORT).show();
            } else if (Text.equalsIgnoreCase(Constants.ERROR_NONE_EXISTING_MESSAGE)) {
                Toast.makeText(ErrorHelper.this.ctx, ctx.getString(R.string.ERROR_NONE_EXISTING_MESSAGE), Toast.LENGTH_SHORT).show();
            } else if (Text.equalsIgnoreCase(Constants.ERROR_INVALID_MESSAGE_TYPE)) {
                Toast.makeText(ErrorHelper.this.ctx, ctx.getString(R.string.ERROR_INVALID_MESSAGE_TYPE), Toast.LENGTH_SHORT).show();
            } else if (Text.equalsIgnoreCase(Constants.ERROR_INVALID_EMAIL_ADRESS)) {
                Toast.makeText(ErrorHelper.this.ctx, ctx.getString(R.string.ERROR_INVALID_EMAIL_ADRESS), Toast.LENGTH_SHORT).show();
            } else if (Text.equalsIgnoreCase(Constants.ERROR_MISSING_CHATNAME)) {
                Toast.makeText(ErrorHelper.this.ctx, ctx.getString(R.string.ERROR_MISSING_CHATNAME), Toast.LENGTH_SHORT).show();
            } else if (Text.equalsIgnoreCase(Constants.ERROR_NONE_EXISTING_TEXT_MESSAGE)) {
                Toast.makeText(ErrorHelper.this.ctx, ctx.getString(R.string.ERROR_NONE_EXISTING_TEXT_MESSAGE), Toast.LENGTH_SHORT).show();
            } else if (Text.equalsIgnoreCase(Constants.ERROR_DB_ERROR)) {
                Toast.makeText(ErrorHelper.this.ctx, ctx.getString(R.string.ERROR_DATABASE_ERROR), Toast.LENGTH_SHORT).show();
            } else if (Text.equalsIgnoreCase(Constants.ERROR_USER_NOT_ACTIVE)) {
                Toast.makeText(ErrorHelper.this.ctx, ctx.getString(R.string.ERROR_USER_NOT_ACTIVE), Toast.LENGTH_SHORT).show();
            } else if (Text.equalsIgnoreCase(Constants.ERROR_WRONG_PASSWORD)) {
                Toast.makeText(ErrorHelper.this.ctx, ctx.getString(R.string.ERROR_WRONG_PASSWORD), Toast.LENGTH_SHORT).show();
            } else if (Text.equalsIgnoreCase(Constants.ERROR_NO_USERNAME_OR_PASSWORD)) {
                Toast.makeText(ErrorHelper.this.ctx, ctx.getString(R.string.ERROR_NO_USERNAME_OR_PASSWORD), Toast.LENGTH_SHORT).show();
            } else if (Text.equalsIgnoreCase(Constants.ERROR_USER_ALREADY_EXISTS)) {
                Toast.makeText(ErrorHelper.this.ctx, ctx.getString(R.string.ERROR_USER_ALREADY_EXISTS), Toast.LENGTH_SHORT).show();
            } else if (Text.equalsIgnoreCase(Constants.ERROR_NO_ACTIVE_CHATS)) {
                Toast.makeText(ErrorHelper.this.ctx, ctx.getString(R.string.ERROR_NO_ACTIVE_CHATS), Toast.LENGTH_SHORT).show();
            } else if (Text.equalsIgnoreCase(Constants.ERROR_FILE_NOT_FOUND)) {
                Toast.makeText(ErrorHelper.this.ctx, ctx.getString(R.string.ERROR_FILE_NOT_FOUND), Toast.LENGTH_SHORT).show();
            }
        } else {
            ret = false;
        }
        return ret;
    }
}