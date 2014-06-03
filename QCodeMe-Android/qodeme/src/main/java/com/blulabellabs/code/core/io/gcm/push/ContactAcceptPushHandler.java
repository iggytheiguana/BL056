package com.blulabellabs.code.core.io.gcm.push;

import com.blulabellabs.code.Application;
import com.blulabellabs.code.core.io.utils.RestKeyMap;
import com.blulabellabs.code.core.provider.QodemeContract;
import com.google.android.gms.internal.cu;

import android.content.Context;
import android.database.Cursor;
import android.os.Bundle;


import static com.blulabellabs.code.utils.NotificationUtils.NOTIFICATION_REQUEST_NEW_CONTACT;
import static com.blulabellabs.code.utils.NotificationUtils.sendNotification;

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
		if (!((Application) getContext().getApplicationContext()).isActive()) {
			String userName = "User";
			try {
				Cursor cursor = getContext().getContentResolver().query(
						QodemeContract.Contacts.CONTENT_URI,
						QodemeContract.Contacts.ContactQuery.PROJECTION,
						QodemeContract.Contacts.CONTACT_ID + " = " + String.valueOf(mContactId),
						null, null);
				if (cursor != null && cursor.getCount() > 0) {
					if (cursor.moveToFirst()) {
						do {
							String name = cursor
									.getString(QodemeContract.Contacts.ContactQuery.CONTACT_PUBLIC_NAME);
							String name1 = cursor
									.getString(QodemeContract.Contacts.ContactQuery.CONTACT_TITLE);
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
			String msg = userName + " has approved your contact request";
			sendNotification(msg, getContext(), NOTIFICATION_REQUEST_NEW_CONTACT);
		}

		getContext().getContentResolver().update(QodemeContract.Contacts.CONTENT_URI,
				QodemeContract.Contacts.acceptContactPushValues(),
				QodemeContract.Contacts.CONTACT_ID + " = " + String.valueOf(mContactId), null);
	}
}