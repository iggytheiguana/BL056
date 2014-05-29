package biz.softtechnics.qodeme.core.io.gcm.push;

import android.content.Context;
import android.os.Bundle;

import biz.softtechnics.qodeme.core.provider.QodemeContract;

/**
 * Created by Alex on 12/10/13.
 */
public class MessageFlaggedPushHandler extends BasePushHandler {

    private long mMessageId;
//    private int numberOfFlagged;

    public MessageFlaggedPushHandler(Context context) {
        super(context);
    }

    @Override
    public void parse(Bundle bundle) {
        mMessageId = Long.parseLong(bundle.getString("message_id"));
        //numberOfFlagged = Integer.parseInt(bundle.getString("number_of_flagged"));
        
    }

    @Override
    public void handle() {
        getContext().getContentResolver().update(
                QodemeContract.Messages.CONTENT_URI,
                QodemeContract.Messages.updatePushMessageFlagged(),
                QodemeContract.Messages.MESSAGE_ID + "=" + String.valueOf(mMessageId), null);
    }



}
