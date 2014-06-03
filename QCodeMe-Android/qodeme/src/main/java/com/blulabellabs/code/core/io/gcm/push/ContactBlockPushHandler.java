package com.blulabellabs.code.core.io.gcm.push;

import com.blulabellabs.code.Application;
import com.blulabellabs.code.core.io.utils.RestKeyMap;
import com.blulabellabs.code.core.provider.QodemeContract;

import android.content.Context;
import android.os.Bundle;

/**
 * Created by Alex on 12/10/13.
 */
public class ContactBlockPushHandler extends BasePushHandler {

	private long mContactId;

	public ContactBlockPushHandler(Context context) {
		super(context);
	}

	@Override
	public void parse(Bundle bundle) {
		mContactId = Long.parseLong(bundle.getString(RestKeyMap.CONTACT_ID));
	}

	@Override
	public void handle() {
		if (!((Application) getContext().getApplicationContext()).isActive()) {
			// String msg = "A contact accepted an invitation";
			// sendNotification(msg, getContext(),
			// NOTIFICATION_REQUEST_NEW_CONTACT);
		}

		getContext().getContentResolver().update(QodemeContract.Contacts.CONTENT_URI,
				QodemeContract.Contacts.blockContactPushValues(),
				QodemeContract.Contacts.CONTACT_ID + " = " + String.valueOf(mContactId), null);
	}
}