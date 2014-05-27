package biz.softtechnics.qodeme.core.io.gcm.push;

import android.content.Context;
import android.os.Bundle;

import com.google.gson.Gson;

import biz.softtechnics.qodeme.Application;
import biz.softtechnics.qodeme.core.io.model.Contact;
import biz.softtechnics.qodeme.core.io.utils.RestKeyMap;
import biz.softtechnics.qodeme.core.provider.QodemeContract;
import biz.softtechnics.qodeme.core.sync.SyncHelper;

import static biz.softtechnics.qodeme.utils.NotificationUtils.NOTIFICATION_REQUEST_NEW_CONTACT;
import static biz.softtechnics.qodeme.utils.NotificationUtils.sendNotification;

/**
 * Created by Alex on 12/10/13.
 */
public class ContactAddPushHandler extends BasePushHandler {

	private Contact mContact;

	public ContactAddPushHandler(Context context) {
		super(context);
	}

	@Override
	public void parse(Bundle bundle) {
		mContact = new Gson().fromJson(bundle.getString(RestKeyMap.CONTACT_OBJECT), Contact.class);
	}

	@Override
	public void handle() {
		if (!((Application) getContext().getApplicationContext()).isActive()) {
			String msg = null;
			if (mContact.state == QodemeContract.Contacts.State.INVITED) {
				// msg = "A new invitation:" + mContact.message != null ?
				// mContact.message : "" + "   " + mContact.publicName;
				String name = "User";
				if (mContact.title == null || mContact.title.equals(""))
					if (mContact.publicName == null || mContact.publicName.equals(""))
						name = "User";
					else
						name = mContact.publicName;
				else
					name = mContact.title;

				msg = name + " would like to add you as a contact";
			} else
				msg = "A new contact was added";
			sendNotification(msg, getContext(), NOTIFICATION_REQUEST_NEW_CONTACT);
		}

		getContext().getContentResolver().insert(QodemeContract.Contacts.CONTENT_URI,
				QodemeContract.Contacts.addNewContactPushValues(mContact));
		SyncHelper.requestManualSync();
	}
}