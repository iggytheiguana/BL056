package com.blulabellabs.code.core.io.gcm.push;

import com.blulabellabs.code.Application;
import com.blulabellabs.code.core.io.utils.RestKeyMap;
import com.blulabellabs.code.core.provider.QodemeContract;

import android.content.Context;
import android.os.Bundle;


import static com.blulabellabs.code.utils.NotificationUtils.NOTIFICATION_REQUEST_NEW_CONTACT;
import static com.blulabellabs.code.utils.NotificationUtils.sendNotification;


/**
 * Created by Alex on 12/10/13.
 */
public class ContactRejectPushHandler extends BasePushHandler{

    private long mContactId;

    public ContactRejectPushHandler(Context context) {
        super(context);
    }

    @Override
    public void parse(Bundle bundle) {
        mContactId = Long.parseLong(bundle.getString(RestKeyMap.CONTACT_ID));
    }

    @Override
    public void handle() {
        if (!((Application)getContext().getApplicationContext()).isActive()) {
            String msg = "Contact rejected an invitation";
            sendNotification(msg, getContext(), NOTIFICATION_REQUEST_NEW_CONTACT);
        }

        getContext().getContentResolver().delete(
                QodemeContract.Contacts.CONTENT_URI,
                QodemeContract.Contacts.CONTACT_ID + " = " + String.valueOf(mContactId),
                null);
    }
}