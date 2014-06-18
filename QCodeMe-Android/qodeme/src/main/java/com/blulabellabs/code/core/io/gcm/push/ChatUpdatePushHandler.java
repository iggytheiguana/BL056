package com.blulabellabs.code.core.io.gcm.push;

import android.content.Context;
import android.os.Bundle;

import com.blulabellabs.code.core.io.model.ChatLoad;
import com.blulabellabs.code.core.provider.QodemeContract;

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
		mChatLoad = new ChatLoad();// new
									// Gson().fromJson(bundle.getString(RestKeyMap.CONTACT_OBJECT),
									// ChatLoad.class);
		mChatLoad.chatId = Long.parseLong(bundle.getString("id"));
		mChatLoad.number_of_flagged = Integer.parseInt(bundle.getString("number_of_flagged"));
		mChatLoad.status = bundle.getString("status");
		mChatLoad.is_locked = Integer.parseInt(bundle.getString("is_locked"));
		mChatLoad.number_of_members = Integer.parseInt(bundle.getString("number_of_members"));
		mChatLoad.description = bundle.getString("description");
		mChatLoad.title = bundle.getString("chat_title");
		mChatLoad.tag = bundle.getString("tags");
		mChatLoad.latitude = bundle.getString("latitude");
		mChatLoad.longitude = bundle.getString("longitude");
	}

	@Override
	public void handle() {
		// if (!((Application)getContext().getApplicationContext()).isActive())
		// {
		// String msg = null;
		// if (mContact.state == QodemeContract.Contacts.State.INVITED)
		// msg = "A new invitation:" + mContact.message != null ?
		// mContact.message : "" + "   " + mContact.publicName;
		// else
		// msg = "A new contact was added";
		// sendNotification(msg, getContext(),
		// NOTIFICATION_REQUEST_NEW_CONTACT);
		// }
		//
		// getContext().getContentResolver().insert(
		// QodemeContract.Contacts.CONTENT_URI,
		// QodemeContract.Contacts.addNewContactPushValues(mContact));
		// getContext().getContentResolver().update(QodemeContract.Chats.CONTENT_URI,
		// QodemeContract.Chats.updateChatInfoValues("",mChatLoad.color,
		// mChatLoad.description, mChatLoad.is_locked, mChatLoad.status,
		// mChatLoad.tag, mChatLoad.number_of_flagged,
		// mChatLoad.number_of_members),
		// QodemeContract.Chats.CHAT_ID+" = "+mChatLoad.chatId, null);
		
		
		
//		Cursor cursor = getContext().getContentResolver().query(QodemeContract.Chats.CONTENT_URI,
//				QodemeContract.Chats.ChatQuery.PROJECTION,
//				QodemeContract.Chats.CHAT_ID + "=" + mChatLoad.chatId, null, null);
//		if (cursor != null)
//			if (cursor.getCount() > 0 && cursor.moveToFirst()) {
//				String status = cursor.getString(QodemeContract.Chats.ChatQuery.CHAT_STATUS);
//				if(!status.equals(mChatLoad.status)){
//					QodemePreferences.getInstance().set("" + mChatLoad.chatId, mChatLoad.status);
////					String msg = "A new contact was added";
////					sendNotification(msg, getContext(),
////							 NOTIFICATION_REQUEST_NEW_CONTACT);
//							 
//				}
//			}
		
		
		
		getContext().getContentResolver().update(
				QodemeContract.Chats.CONTENT_URI,
				QodemeContract.Chats.updateChatInfoValuesAll(mChatLoad.title, mChatLoad.color,
						mChatLoad.description, mChatLoad.is_locked, mChatLoad.status,
						mChatLoad.tag, mChatLoad.number_of_flagged, mChatLoad.number_of_members,
						mChatLoad.latitude, mChatLoad.longitude),
				QodemeContract.Chats.CHAT_ID + " = " + mChatLoad.chatId, null);

		

		// SyncHelper.requestManualSync();
	}
}