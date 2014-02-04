package biz.softtechnics.qodeme.core.io.hendler;

import android.content.ContentProviderOperation;
import android.content.Context;
import android.text.TextUtils;

import com.google.common.collect.Lists;

import java.util.ArrayList;

import biz.softtechnics.qodeme.core.io.model.Contact;
import biz.softtechnics.qodeme.core.io.responses.ContactAddResponse;
import biz.softtechnics.qodeme.utils.DbUtils;

import static biz.softtechnics.qodeme.core.provider.QodemeContract.Contacts;
import static biz.softtechnics.qodeme.core.provider.QodemeContract.SyncColumns;
import static biz.softtechnics.qodeme.core.provider.QodemeContract.addCallerIsSyncAdapterParameter;

/**
 * Created by Alex on 11/27/13.
 */
public class ContactAddHandler extends BaseResponseHandler<ContactAddResponse> {

    private long mId;

    public ContactAddHandler(Context context, long _id) {
        super(context);
        mId = _id;
    }

    @Override
     public ArrayList<ContentProviderOperation> parse(ContactAddResponse response) {
        ArrayList<ContentProviderOperation> batch = Lists.newArrayList();
        return parse(response, batch);
    }

    public ArrayList<ContentProviderOperation> parse(ContactAddResponse response, ArrayList<ContentProviderOperation> batch) {
        if (batch == null)
            batch = Lists.newArrayList();
        parseContact(response.getContact(), batch);
        return batch;
    }

    private void parseContact(Contact c, ArrayList<ContentProviderOperation> batch) {
        ContentProviderOperation.Builder builder = ContentProviderOperation
                .newUpdate(addCallerIsSyncAdapterParameter(Contacts.CONTENT_URI));
        builder.withValue(Contacts.CONTACT_ID, c.contactId);
        builder.withValue(Contacts.CONTACT_CHAT_ID, c.chatId);
        builder.withValue(Contacts.CONTACT_STATE, c.state);
//        builder.withValue(Contacts.CONTACT_QRCODE, c.qrCode);
//        builder.withValue(Contacts.CONTACT_TITLE, c.title);
//        builder.withValue(Contacts.CONTACT_COLOR, c.color);
//        builder.withValue(Contacts.CONTACT_LOCATION, c.location);
        builder.withValue(Contacts.CONTACT_PUBLIC_NAME, c.publicName);
        builder.withValue(Contacts.CONTACT_TITLE, TextUtils.isEmpty(c.publicName) ? "User" : c.publicName);
//        builder.withValue(Contacts.CONTACT_DATETIME, c.date);
        builder.withValue(SyncColumns.UPDATED, Contacts.Sync.UPDATED);
        builder.withSelection(DbUtils.getWhereClauseForId(), DbUtils.getWhereArgsForId(mId));
        batch.add(builder.build());
    }

}
