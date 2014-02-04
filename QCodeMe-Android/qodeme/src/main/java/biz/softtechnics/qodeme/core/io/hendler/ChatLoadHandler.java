package biz.softtechnics.qodeme.core.io.hendler;

import android.content.ContentProviderOperation;
import android.content.Context;

import java.util.ArrayList;
import java.util.Arrays;

import biz.softtechnics.qodeme.core.io.responses.ChatLoadResponse;
import biz.softtechnics.qodeme.core.io.model.Message;

import static biz.softtechnics.qodeme.core.provider.QodemeContract.Contacts;
import static biz.softtechnics.qodeme.core.provider.QodemeContract.Messages;
import static biz.softtechnics.qodeme.core.provider.QodemeContract.SyncColumns;
import static biz.softtechnics.qodeme.core.provider.QodemeContract.addCallerIsSyncAdapterParameter;

/**
 * Created by Alex on 11/27/13.
 */
public class ChatLoadHandler extends BaseResponseHandler<ChatLoadResponse> {

    public ChatLoadHandler(Context context) {
        super(context);
    }

    @Override
    public ArrayList<ContentProviderOperation> parse(ChatLoadResponse response, ArrayList<ContentProviderOperation> batch) {
        for (Message m: Arrays.asList(response.getChatLoad().messages)){
            m.chatId = response.getChatLoad().chatId;
            parseMessage(m, batch);
        }
        return batch;
    }

    private static void parseMessage(Message m,
                                          ArrayList<ContentProviderOperation> batch) {
        ContentProviderOperation.Builder builder = ContentProviderOperation
                .newInsert(addCallerIsSyncAdapterParameter(Messages.CONTENT_URI));
        builder.withValue(Messages.MESSAGE_ID, m.messageId);
        builder.withValue(Messages.MESSAGE_CHAT_ID, m.chatId);
        builder.withValue(Messages.MESSAGE_TEXT, m.message);
        builder.withValue(Messages.MESSAGE_QRCODE, m.qrcode);
        builder.withValue(Messages.MESSAGE_CREATED, m.created);
        builder.withValue(Messages.MESSAGE_STATE, m.state);
        builder.withValue(SyncColumns.UPDATED, Contacts.Sync.DONE);
        batch.add(builder.build());
    }

}
