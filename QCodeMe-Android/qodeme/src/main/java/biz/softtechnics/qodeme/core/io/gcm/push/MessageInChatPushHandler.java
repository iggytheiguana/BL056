package biz.softtechnics.qodeme.core.io.gcm.push;

import android.content.Context;
import android.os.Bundle;

import biz.softtechnics.qodeme.Application;
import biz.softtechnics.qodeme.core.io.model.Message;
import biz.softtechnics.qodeme.core.provider.QodemeContract;

import static biz.softtechnics.qodeme.utils.NotificationUtils.NOTIFICATION_REQUEST_NEW_MESSAGE;
import static biz.softtechnics.qodeme.utils.NotificationUtils.sendNotification;

/**
 * Created by Alex on 12/10/13.
 */
public class MessageInChatPushHandler extends BasePushHandler {

    private Message mMessage;

    public MessageInChatPushHandler(Context context) {
        super(context);
    }

    @Override
    public void parse(Bundle bundle) {
        mMessage = new Message();
        mMessage.chatId = Long.parseLong(bundle.getString("chat_id"));
        mMessage.messageId = Long.parseLong(bundle.getString("id"));
        mMessage.message = bundle.getString("message");
        mMessage.created = bundle.getString("created");
        mMessage.qrcode = bundle.getString("from_qrcode");
    }

    @Override
    public void handle() {
        if (!((Application)getContext().getApplicationContext()).isActive()) {
            String msg = "New messages available";
            sendNotification(msg, getContext(), NOTIFICATION_REQUEST_NEW_MESSAGE);
        }
        getContext().getContentResolver().insert(
                QodemeContract.Messages.CONTENT_URI,
                QodemeContract.Messages.addNewMessagePushValues(mMessage));
    }



}
