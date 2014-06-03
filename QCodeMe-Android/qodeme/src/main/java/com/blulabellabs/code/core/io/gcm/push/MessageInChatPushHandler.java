package com.blulabellabs.code.core.io.gcm.push;

import com.blulabellabs.code.Application;
import com.blulabellabs.code.core.io.model.Message;
import com.blulabellabs.code.core.provider.QodemeContract;
import com.blulabellabs.code.utils.DbUtils;

import android.R.menu;
import android.content.Context;
import android.database.Cursor;
import android.os.Bundle;

import static com.blulabellabs.code.utils.NotificationUtils.NOTIFICATION_REQUEST_NEW_MESSAGE;
import static com.blulabellabs.code.utils.NotificationUtils.sendNotification;

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
		mMessage.hasPhoto = Integer.parseInt(bundle.getString("has_photo"));
		// mMessage.is_flagged =
		// Integer.parseInt(bundle.getString("is_flagged"));
		mMessage.latitude = bundle.getString("latitude");
		mMessage.longitude = bundle.getString("longitude");
		mMessage.photoUrl = bundle.getString("photourl");
		mMessage.replyTo_id = Long.parseLong(bundle.getString("replyto_id"));
		mMessage.senderName = bundle.getString("sendername");

	}

	@Override
	public void handle() {
		if (!((Application) getContext().getApplicationContext()).isActive()) {

			int type = 0;
			String userName = "User";
			if (mMessage.senderName == null || mMessage.senderName.trim().equals(""))
				userName = "User";
			else
				userName = mMessage.senderName;
			try {
				Cursor cursorChat = getContext().getContentResolver().query(
						QodemeContract.Chats.CONTENT_URI,
						QodemeContract.Chats.ChatQuery.PROJECTION,
						QodemeContract.Chats.CHAT_ID + " = " + String.valueOf(mMessage.chatId),
						null, null);

				if (cursorChat != null && cursorChat.getCount() > 0) {
					if (cursorChat.moveToFirst()) {
						do {
							type = cursorChat.getInt(QodemeContract.Chats.ChatQuery.CHAT_TYPE);
						} while (cursorChat.moveToNext());
					}
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
			String msg = "New message from ";
			if (type == 0) {
				msg = "New message from " + userName;
			} else {
				msg = "New message in " + userName;
			}
			sendNotification(msg, getContext(), NOTIFICATION_REQUEST_NEW_MESSAGE);
		}
		getContext().getContentResolver().insert(QodemeContract.Messages.CONTENT_URI,
				QodemeContract.Messages.addNewMessagePushValues(mMessage));
		getContext().getContentResolver().update(QodemeContract.Contacts.CONTENT_URI,
				QodemeContract.Contacts.isArchiveValues(0),
				QodemeContract.Contacts.CONTACT_CHAT_ID + "=" + mMessage.chatId, null);
	}

}
