package biz.softtechnics.qodeme.core.io.gcm.push;

import android.content.Context;
import android.os.Bundle;

import biz.softtechnics.qodeme.core.provider.QodemeContract;

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
