package biz.softtechnics.qodeme.core.io.gcm.push;

import android.content.Context;
import android.os.Bundle;

import com.google.gson.Gson;

import biz.softtechnics.qodeme.Application;
import biz.softtechnics.qodeme.core.io.model.ChatLoad;
import biz.softtechnics.qodeme.core.io.model.Contact;
import biz.softtechnics.qodeme.core.io.utils.RestKeyMap;
import biz.softtechnics.qodeme.core.provider.QodemeContract;
import biz.softtechnics.qodeme.core.sync.SyncHelper;

import static biz.softtechnics.qodeme.utils.NotificationUtils.NOTIFICATION_REQUEST_NEW_CONTACT;
import static biz.softtechnics.qodeme.utils.NotificationUtils.sendNotification;


/**
 * Created by Alex on 12/10/13.
 */
public class ChatUpdatePushHandler extends BasePushHandler {

    private ChatLoad mChatLoad;

    public ChatUpdatePushHandler(Context context) {
        super(context);
    }

    @Override
    public void parse(Bundle bundle) {
        mChatLoad = new Gson().fromJson(bundle.getString(RestKeyMap.CONTACT_OBJECT), ChatLoad.class);
    }

    @Override
    public void handle() {
//        if (!((Application)getContext().getApplicationContext()).isActive()) {
//            String msg = null;
//            if (mContact.state == QodemeContract.Contacts.State.INVITED)
//                msg = "A new invitation:" + mContact.message != null ? mContact.message : "" + "   " + mContact.publicName;
//            else
//                msg = "A new contact was added";
//            sendNotification(msg, getContext(), NOTIFICATION_REQUEST_NEW_CONTACT);
//        }
//
//        getContext().getContentResolver().insert(
//                QodemeContract.Contacts.CONTENT_URI,
//                QodemeContract.Contacts.addNewContactPushValues(mContact));
    	getContext().getContentResolver().update(QodemeContract.Chats.CONTENT_URI, QodemeContract.Chats.updateChatInfoValues("",mChatLoad.color, mChatLoad.description, mChatLoad.is_locked, mChatLoad.status, mChatLoad.tag, mChatLoad.number_of_flagged, mChatLoad.number_of_members), QodemeContract.Chats.CHAT_ID+" = "+mChatLoad.chatId, null);
        SyncHelper.requestManualSync();
    }
}