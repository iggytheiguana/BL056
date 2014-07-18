package com.blulabellabs.code.core.io.gcm.push;

import static com.blulabellabs.code.utils.NotificationUtils.NOTIFICATION_REQUEST_NEW_CONTACT;
import static com.blulabellabs.code.utils.NotificationUtils.sendNotification;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;

import com.blulabellabs.code.Application;
import com.blulabellabs.code.core.data.preference.QodemePreferences;
import com.blulabellabs.code.core.io.model.ChatAdd;
import com.blulabellabs.code.core.provider.QodemeContract;
import com.blulabellabs.code.core.sync.SyncHelper;
import com.blulabellabs.code.ui.MainActivity;

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
		mChatLoad.title = bundle.getString("chat_title");
		mChatLoad.status = bundle.getString("chat_status");
		mChatLoad.description = bundle.getString("description");
		mChatLoad.latitude = bundle.getString("latitude");
		mChatLoad.longitude = bundle.getString("longitude");
		try {
			mChatLoad.number_of_flagged = Integer.parseInt(bundle.getString("number_of_flagged"));
			mChatLoad.number_of_members = Integer.parseInt(bundle.getString("number_of_members"));
		} catch (Exception e) {
			e.printStackTrace();
		}
		mChatLoad.tag = bundle.getString("tags");
		mChatLoad.created = bundle.getString("created");
		try {
			mChatLoad.is_locked = Integer.parseInt(bundle.getString("is_locked"));
		} catch (Exception e) {
			e.printStackTrace();
		}

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
			msg = "You have been added to " + mChatLoad.title;
			sendNotification(msg, getContext(), NOTIFICATION_REQUEST_NEW_CONTACT);
		}
		//

		Cursor cursor = getContext().getContentResolver().query(QodemeContract.Chats.CONTENT_URI,
				QodemeContract.Chats.ChatQuery.PROJECTION,
				QodemeContract.Chats.CHAT_ID + " = " + mChatLoad.chatId, null, null);
		// addNewPushChatValues(long chatId, int type, String qrCode,
		// String admin, String latitude, String longitude, String desc, String
		// status, int no_flagged, int no_member, String title, String tag, int
		// is_locked)
		if (cursor != null) {
			if (cursor.getCount() > 0) {
			} else {
				getContext().getContentResolver().insert(
						QodemeContract.Chats.CONTENT_URI,
						QodemeContract.Chats.addNewPushChatValues(mChatLoad.chatId, mChatLoad.type,
								mChatLoad.qrcode, "", mChatLoad.latitude, mChatLoad.longitude,
								mChatLoad.description, mChatLoad.status,
								mChatLoad.number_of_flagged, mChatLoad.number_of_members,
								mChatLoad.title, mChatLoad.tag, mChatLoad.is_locked,
								mChatLoad.created));
			}
		} else {
			getContext().getContentResolver().insert(
					QodemeContract.Chats.CONTENT_URI,
					QodemeContract.Chats.addNewPushChatValues(mChatLoad.chatId, mChatLoad.type,
							mChatLoad.qrcode, "", mChatLoad.latitude, mChatLoad.longitude,
							mChatLoad.description, mChatLoad.status, mChatLoad.number_of_flagged,
							mChatLoad.number_of_members, mChatLoad.title, mChatLoad.tag,
							mChatLoad.is_locked, mChatLoad.created));
		}
		String id = QodemePreferences.getInstance().get("AddMemberId", "");
		if (id.trim().equals(""))
			id = mChatLoad.chatId + "";
		else
			id = id + "," + mChatLoad.chatId + "";

//		getContext().getContentResolver().delete(QodemeContract.Messages.CONTENT_URI,
//				QodemeContract.Messages.MESSAGE_CHAT_ID + "=" + mChatLoad.chatId, null);
		QodemePreferences.getInstance().set("AddMemberId", id);
//		Intent intent = new Intent(MainActivity.CHAT_ADDED_BRODCAST_ACTION);
//		intent.putExtra("chat_id", mChatLoad.chatId);
//		getContext().sendBroadcast(intent);
		// try {
		// ArrayList<ContentProviderOperation> batch = Lists.newArrayList();
		// RestSyncHelper rest = RestSyncHelper.getInstance(getContext());
		// ChatLoadResponse chatLoadResponse = rest.chatLoad(mChatLoad.chatId,
		// 0, 1000);
		// new ChatLoadHandler(getContext()).parse(chatLoadResponse, batch);
		// QodemeContract.applyBatch(getContext(), batch);

		// } catch (Exception e) {
		// e.printStackTrace();
		// }
		// getContext().getContentResolver().update(QodemeContract.Chats.CONTENT_URI,
		// QodemeContract.Chats.updateChatInfoValues("",mChatLoad.color,
		// mChatLoad.description, mChatLoad.is_locked, mChatLoad.status,
		// mChatLoad.tag, mChatLoad.number_of_flagged,
		// mChatLoad.number_of_members),
		// QodemeContract.Chats.CHAT_ID+" = "+mChatLoad.chatId, null);
		SyncHelper.requestManualSync();
	}
}