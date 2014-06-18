package com.blulabellabs.code.core.io.gcm.push;

import android.content.Context;
import android.database.Cursor;
import android.os.Bundle;

import com.blulabellabs.code.core.data.preference.QodemePreferences;
import com.blulabellabs.code.core.provider.QodemeContract;
import com.blulabellabs.code.core.sync.SyncHelper;

/**
 * Created by Alex on 12/10/13.
 */
public class ContactDeletePushHandler extends BasePushHandler {

	private long mChatId;
	private String qrCode = "";

	// private int numberOfFlagged;
	// private int is_flagged = 1;

	public ContactDeletePushHandler(Context context) {
		super(context);
	}

	@Override
	public void parse(Bundle bundle) {
		mChatId = Long.parseLong(bundle.getString("chat_id"));
		qrCode = bundle.getString("qrcode");
	}

	@Override
	public void handle() {
		// if(is_flagged == 1)
		// getContext().getContentResolver().delete(QodemeContract.Messages.CONTENT_URI,
		// QodemeContract.Messages.MESSAGE_ID + "=" + String.valueOf(mChatId),
		// null);
		getContext().getContentResolver().update(QodemeContract.Chats.CONTENT_URI,
				QodemeContract.Chats.deleteChat(), QodemeContract.Chats.CHAT_ID + " = " + mChatId,
				null);
		if (!QodemePreferences.getInstance().getQrcode().equals(qrCode)) {

			Cursor cursor = getContext().getContentResolver().query(
					QodemeContract.Contacts.CONTENT_URI,
					QodemeContract.Contacts.ContactQuery.PROJECTION,
					QodemeContract.Contacts.CONTACT_QRCODE + "= '" + qrCode + "'", null, null);

			int is_deleted = 0;
			if (cursor != null && cursor.moveToFirst()) {
				long contactId;
				do {
					contactId = cursor.getLong(QodemeContract.Contacts.ContactQuery.CONTACT_ID);
					is_deleted = cursor
							.getInt(QodemeContract.Contacts.ContactQuery.CONTACT_IS_DELETED);
				} while (cursor.moveToNext());
				String contactlist = QodemePreferences.getInstance().get("RemoveContact", "");
				if (contactlist.trim().equals(""))
					contactlist = contactId + "";
				else
					contactlist += "," + contactId + "";

//				if (is_deleted != 1)
//					QodemePreferences.getInstance().set("RemoveContact", contactlist);
			}

			if (is_deleted != 1)
				getContext().getContentResolver().update(QodemeContract.Contacts.CONTENT_URI,
						QodemeContract.Contacts.deleteContact(),
						QodemeContract.Contacts.CONTACT_QRCODE + "= '" + qrCode + "'", null);

		} else {
			getContext().getContentResolver().update(QodemeContract.Contacts.CONTENT_URI,
					QodemeContract.Contacts.deleteContact(),
					QodemeContract.Contacts.CONTACT_QRCODE + "= '" + qrCode + "'", null);
		}
		SyncHelper.requestManualSync();

	}

}
