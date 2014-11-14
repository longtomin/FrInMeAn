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
            if (Text.equalsIgnoreCase(Constants.NO_CONNECTION_TO_SERVER)) {
                Toast.makeText(ErrorHelper.this.ctx, ctx.getString(R.string.NO_CONNECTION_TO_SERVER), Toast.LENGTH_SHORT).show();
            }
            if (Text.equalsIgnoreCase(Constants.USER_AUTHENTICATION_FAILED)) {
                Toast.makeText(ErrorHelper.this.ctx, ctx.getString(R.string.USER_AUTHENTICATION_FAILED), Toast.LENGTH_SHORT).show();
            }
            if (Text.equalsIgnoreCase(Constants.NONE_EXISTING_USER)) {
                Toast.makeText(ErrorHelper.this.ctx, ctx.getString(R.string.NONE_EXISTING_USER), Toast.LENGTH_SHORT).show();
            }
            if (Text.equalsIgnoreCase(Constants.NONE_EXISTING_CHAT)) {
                Toast.makeText(ErrorHelper.this.ctx, ctx.getString(R.string.NONE_EXISTING_CHAT), Toast.LENGTH_SHORT).show();
            }
            if (Text.equalsIgnoreCase(Constants.NO_TEXTMESSAGE_GIVEN)) {
                Toast.makeText(ErrorHelper.this.ctx, ctx.getString(R.string.NO_TEXTMESSAGE_GIVEN), Toast.LENGTH_SHORT).show();
            }
            if (Text.equalsIgnoreCase(Constants.NONE_EXISTING_MESSAGE)) {
                Toast.makeText(ErrorHelper.this.ctx, ctx.getString(R.string.NONE_EXISTING_MESSAGE), Toast.LENGTH_SHORT).show();
            }
            if (Text.equalsIgnoreCase(Constants.INVALID_MESSAGE_TYPE)) {
                Toast.makeText(ErrorHelper.this.ctx, ctx.getString(R.string.INVALID_MESSAGE_TYPE), Toast.LENGTH_SHORT).show();
            }
            if (Text.equalsIgnoreCase(Constants.INVALID_EMAIL_ADRESS)) {
                Toast.makeText(ErrorHelper.this.ctx, ctx.getString(R.string.INVALID_EMAIL_ADRESS), Toast.LENGTH_SHORT).show();
            }
            if (Text.equalsIgnoreCase(Constants.MISSING_CHATNAME)) {
                Toast.makeText(ErrorHelper.this.ctx, ctx.getString(R.string.MISSING_CHATNAME), Toast.LENGTH_SHORT).show();
            }
            if (Text.equalsIgnoreCase(Constants.NONE_EXISTING_TEXT_MESSAGE)) {
                Toast.makeText(ErrorHelper.this.ctx, ctx.getString(R.string.NONE_EXISTING_TEXT_MESSAGE), Toast.LENGTH_SHORT).show();
            }
            if (Text.equalsIgnoreCase(Constants.DB_ERROR)) {
                Toast.makeText(ErrorHelper.this.ctx, ctx.getString(R.string.DATABASE_ERROR), Toast.LENGTH_SHORT).show();
            }
            if (Text.equalsIgnoreCase(Constants.USER_NOT_ACTIVE)) {
                Toast.makeText(ErrorHelper.this.ctx, ctx.getString(R.string.USER_NOT_ACTIVE), Toast.LENGTH_SHORT).show();
            }
            if (Text.equalsIgnoreCase(Constants.WRONG_PASSWORD)) {
                Toast.makeText(ErrorHelper.this.ctx, ctx.getString(R.string.WRONG_PASSWORD), Toast.LENGTH_SHORT).show();
            }
            if (Text.equalsIgnoreCase(Constants.NO_USERNAME_OR_PASSWORD)) {
                Toast.makeText(ErrorHelper.this.ctx, ctx.getString(R.string.NO_USERNAME_OR_PASSWORD), Toast.LENGTH_SHORT).show();
            }
            if (Text.equalsIgnoreCase(Constants.USER_ALREADY_EXISTS)) {
                Toast.makeText(ErrorHelper.this.ctx, ctx.getString(R.string.USER_ALREADY_EXISTS), Toast.LENGTH_SHORT).show();
            }
            if (Text.equalsIgnoreCase(Constants.NO_ACTIVE_CHATS)) {
                Toast.makeText(ErrorHelper.this.ctx, ctx.getString(R.string.NO_ACTIVE_CHATS), Toast.LENGTH_SHORT).show();
            }
        } else {
            ret = false;
        }
        return ret;
    }
}