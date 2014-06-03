package com.blulabellabs.code.core.io.gcm.push;

import com.blulabellabs.code.core.provider.QodemeContract;

import android.content.Context;
import android.os.Bundle;


/**
 * Created by Alex on 12/10/13.
 */
public class MessageWasReadPushHandler extends BasePushHandler {

    private long mMessageId;

    public MessageWasReadPushHandler(Context context) {
        super(context);
    }

    @Override
    public void parse(Bundle bundle) {
        mMessageId = Long.parseLong(bundle.getString("message_id"));
    }

    @Override
    public void handle() {
        getContext().getContentResolver().update(
                QodemeContract.Messages.CONTENT_URI,
                QodemeContract.Messages.addNewMessageWasReadValues(),
                QodemeContract.Messages.MESSAGE_ID + "=" + String.valueOf(mMessageId), null);
    }



}
