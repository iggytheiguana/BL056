package biz.softtechnics.qodeme.core.io.gcm.push;

import android.content.Context;
import android.database.Cursor;
import android.os.Bundle;
import biz.softtechnics.qodeme.core.io.model.ChatAdd;
import biz.softtechnics.qodeme.core.io.utils.RestKeyMap;
import biz.softtechnics.qodeme.core.provider.QodemeContract;
import biz.softtechnics.qodeme.core.sync.SyncHelper;
import biz.softtechnics.qodeme.Application;
import com.google.android.gms.internal.cu;
import com.google.gson.Gson;

/**
 * Created by Alex on 12/10/13.
 */
public class ChatAddMemberPushHandler extends BasePushHandler {

	private ChatAdd mChatLoad;

	public ChatAddMemberPushHandler(Context context) {
		super(context);
	}

	@Override
	public void parse(Bundle bundle) {
		mChatLoad = new ChatAdd();
		// new Gson().fromJson(bundle.get, ChatAdd.class);
		mChatLoad.chatId = Long.parseLong(bundle.getString("chat_id"));
		mChatLoad.type = Integer.parseInt(bundle.getString("chat_type"));

	}

	@Override
	public void handle() {
		if (!((Application) getContext().getApplicationContext()).isActive()) {
			String msg = null;
			// if (mContact.state == QodemeContract.Contacts.State.INVITED)
			// msg = "A new invitation:" + mContact.message != null ?
			// mContact.message : ""
			// + "   " + mContact.publicName;
			// else
			// msg = "A new contact was added";
			// sendNotification(msg, getContext(),
			// NOTIFICATION_REQUEST_NEW_CONTACT);
		}
		//

		Cursor cursor = getContext().getContentResolver().query(QodemeContract.Chats.CONTENT_URI,
				QodemeContract.Chats.ChatQuery.PROJECTION,
				QodemeContract.Chats.CHAT_ID + " = " + mChatLoad.chatId, null, null);
		if (cursor != null) {
			if (cursor.getCount() > 0) {
			} else {
				getContext().getContentResolver().insert(
						QodemeContract.Chats.CONTENT_URI,
						QodemeContract.Chats.addNewChatValues(mChatLoad.chatId, mChatLoad.type,
								mChatLoad.qrcode, "",0,0));
			}
		} else {
			getContext().getContentResolver().insert(
					QodemeContract.Chats.CONTENT_URI,
					QodemeContract.Chats.addNewChatValues(mChatLoad.chatId, mChatLoad.type,
							mChatLoad.qrcode, "",0,0));
		}

		// getContext().getContentResolver().update(QodemeContract.Chats.CONTENT_URI,
		// QodemeContract.Chats.updateChatInfoValues("",mChatLoad.color,
		// mChatLoad.description, mChatLoad.is_locked, mChatLoad.status,
		// mChatLoad.tag, mChatLoad.number_of_flagged,
		// mChatLoad.number_of_members),
		// QodemeContract.Chats.CHAT_ID+" = "+mChatLoad.chatId, null);
		// SyncHelper.requestManualSync();
	}
}