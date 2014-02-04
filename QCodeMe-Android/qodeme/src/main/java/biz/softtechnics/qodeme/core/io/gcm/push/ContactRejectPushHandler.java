package biz.softtechnics.qodeme.core.io.gcm.push;

import android.content.Context;
import android.os.Bundle;

import biz.softtechnics.qodeme.Application;
import biz.softtechnics.qodeme.core.io.utils.RestKeyMap;
import biz.softtechnics.qodeme.core.provider.QodemeContract;

import static biz.softtechnics.qodeme.utils.NotificationUtils.NOTIFICATION_REQUEST_NEW_CONTACT;
import static biz.softtechnics.qodeme.utils.NotificationUtils.sendNotification;


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