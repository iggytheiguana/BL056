package biz.softtechnics.qodeme.core.io.hendler;

import android.content.ContentProviderOperation;
import android.content.Context;

import java.util.ArrayList;

import biz.softtechnics.qodeme.core.io.responses.AccountContactsResponse;
import biz.softtechnics.qodeme.core.data.entities.ChatEntity;
import biz.softtechnics.qodeme.core.io.model.Contact;

import static biz.softtechnics.qodeme.core.provider.QodemeContract.Contacts;
import static biz.softtechnics.qodeme.core.provider.QodemeContract.SyncColumns;
import static biz.softtechnics.qodeme.core.provider.QodemeContract.addCallerIsSyncAdapterParameter;

/**
 * Created by Alex on 11/27/13.
 */
public class AccountContactsHandler extends BaseResponseHandler<AccountContactsResponse> {

    public AccountContactsHandler(Context context) {
        super(context);
    }

    @Override
    public ArrayList<ContentProviderOperation> parse(AccountContactsResponse response, ArrayList<ContentProviderOperation> batch) {
        for (Contact contact: response.getContactList()){
            parseContact(contact, batch);
        }
        for (ChatEntity chatEntity: response.getChatList()){
            parseChat(chatEntity, batch);
        }
        return batch;
    }

    private static void parseContact(Contact c,
                                          ArrayList<ContentProviderOperation> batch) {
        ContentProviderOperation.Builder builder = ContentProviderOperation
                .newInsert(addCallerIsSyncAdapterParameter(Contacts.CONTENT_URI));
        builder.withValue(Contacts.CONTACT_ID, c.contactId);
        builder.withValue(Contacts.CONTACT_CHAT_ID, c.chatId);
        builder.withValue(Contacts.CONTACT_QRCODE, c.qrCode);
        builder.withValue(Contacts.CONTACT_TITLE, c.title != null ? c.title : "");
        builder.withValue(Contacts.CONTACT_COLOR, c.color);
        builder.withValue(Contacts.CONTACT_STATE, c.state);
        builder.withValue(Contacts.CONTACT_DATETIME, c.date);
        builder.withValue(Contacts.CONTACT_PUBLIC_NAME, c.publicName);
        builder.withValue(Contacts.CONTACT_LOCATION, c.location);
        builder.withValue(SyncColumns.UPDATED, Contacts.Sync.DONE);
        batch.add(builder.build());
    }

    private static void parseChat(ChatEntity ce,
                                     ArrayList<ContentProviderOperation> batch) {
        // TODO
    }

}
