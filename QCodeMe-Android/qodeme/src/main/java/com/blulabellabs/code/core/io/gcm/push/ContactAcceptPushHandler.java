package com.blulabellabs.code.core.io.gcm.push;

import static com.blulabellabs.code.utils.NotificationUtils.NOTIFICATION_REQUEST_NEW_CONTACT;
import static com.blulabellabs.code.utils.NotificationUtils.sendNotification;
import android.content.Context;
import android.database.Cursor;
import android.os.Bundle;

import com.blulabellabs.code.Application;
import com.blulabellabs.code.core.io.utils.RestKeyMap;
import com.blulabellabs.code.core.provider.QodemeContract;

/**
 * Created by Alex on 12/10/13.
 */
public class ContactAcceptPushHandler extends BasePushHandler {

	private long mContactId;

	public ContactAcceptPushHandler(Context context) {
		super(context);
	}

	@Override
	public void parse(Bundle bundle) {
		mContactId = Long.parseLong(bundle.getString(RestKeyMap.CONTACT_ID));
	}

	@Override
	public void handle() {
		long chatId = -1;
		String latitude = "0";
		String longitude = "0";
		String userName = "User";
		try {
			Cursor cursor = getContext().getContentResolver().query(
					QodemeContract.Contacts.CONTENT_URI,
					QodemeContract.Contacts.ContactQuery.PROJECTION,
					QodemeContract.Contacts.CONTACT_ID + " = " + String.valueOf(mContactId), null,
					null);
			if (cursor != null && cursor.getCount() > 0) {
				if (cursor.moveToFirst()) {
					do {
						String name = cursor
								.getString(QodemeContract.Contacts.ContactQuery.CONTACT_PUBLIC_NAME);
						String name1 = cursor
								.getString(QodemeContract.Contacts.ContactQuery.CONTACT_TITLE);
						chatId = cursor
								.getLong(QodemeContract.Contacts.ContactQuery.CONTACT_CHAT_ID);
						latitude = cursor
								.getString(QodemeContract.Contacts.ContactQuery.CONTACT_LATITUDE);
						longitude = cursor
								.getString(QodemeContract.Contacts.ContactQuery.CONTACT_LONGITUDE);
						if (name.equals(""))
							if (name1.equals(""))
								userName = "User";
							else
								userName = name1;
						else
							userName = name;
					} while (cursor.moveToNext());
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		if (!((Application) getContext().getApplicationContext()).isActive()) {
			String msg = userName + " has approved your contact request";
			sendNotification(msg, getContext(), NOTIFICATION_REQUEST_NEW_CONTACT);
		}

		getContext().getContentResolver().update(QodemeContract.Contacts.CONTENT_URI,
				QodemeContract.Contacts.acceptContactPushValues(),
				QodemeContract.Contacts.CONTACT_ID + " = " + String.valueOf(mContactId), null);

		if (chatId != -1) {
			try {
				Cursor cursor = getContext().getContentResolver().query(
						QodemeContract.Chats.CONTENT_URI,
						QodemeContract.Chats.ChatQuery.PROJECTION,
						QodemeContract.Chats.CHAT_ID + " = " + chatId, null, null);
				if (cursor != null) {
					if (cursor.getCount() > 0) {
					} else {
						getContext().getContentResolver().insert(
								QodemeContract.Chats.CONTENT_URI,
								QodemeContract.Chats.addNewPushChatValues(chatId, 0, "", "",
										latitude, longitude, "", "", 0, 0, "", "", 0, ""));
					}
				} else {
					getContext().getContentResolver().insert(
							QodemeContract.Chats.CONTENT_URI,
							QodemeContract.Chats.addNewPushChatValues(chatId, 0, "", "", latitude,
									longitude, "", "", 0, 0, "", "", 0, ""));
				}
			} catch (Exception e) {
				// TODO: handle exception
			}
		}
	}
}