package biz.softtechnics.qodeme.core.io.hendler;

import android.content.ContentProviderOperation;
import android.content.Context;

import com.google.common.collect.Lists;

import java.util.ArrayList;

import biz.softtechnics.qodeme.core.io.responses.ChatMessageResponse;
import biz.softtechnics.qodeme.core.provider.QodemeContract;
import biz.softtechnics.qodeme.utils.DbUtils;

import static biz.softtechnics.qodeme.core.provider.QodemeContract.Contacts;
import static biz.softtechnics.qodeme.core.provider.QodemeContract.SyncColumns;
import static biz.softtechnics.qodeme.core.provider.QodemeContract.addCallerIsSyncAdapterParameter;

/**
 * Created by Alex on 11/27/13.
 */
public class ChatMessageHandler extends BaseResponseHandler<ChatMessageResponse> {

    private long mId;

    public ChatMessageHandler(Context context, long _id) {
        super(context);
        mId = _id;
    }

    @Override
     public ArrayList<ContentProviderOperation> parse(ChatMessageResponse response) {
        ArrayList<ContentProviderOperation> batch = Lists.newArrayList();
        return parse(response, batch);
    }

    public ArrayList<ContentProviderOperation> parse(ChatMessageResponse response, ArrayList<ContentProviderOperation> batch) {
        if (batch == null)
            batch = Lists.newArrayList();
        prepareBatch(response, batch);
        return batch;
    }

    private void prepareBatch(ChatMessageResponse c, ArrayList<ContentProviderOperation> batch) {
        ContentProviderOperation.Builder builder = ContentProviderOperation
                .newUpdate(addCallerIsSyncAdapterParameter(QodemeContract.Messages.CONTENT_URI));
        builder.withValue(QodemeContract.Messages.MESSAGE_ID, c.getMessageId());
        builder.withValue(QodemeContract.Messages.MESSAGE_STATE, QodemeContract.Messages.State.SENT);
        builder.withValue(SyncColumns.UPDATED, Contacts.Sync.DONE);
        builder.withSelection(DbUtils.getWhereClauseForId(), DbUtils.getWhereArgsForId(mId));
        batch.add(builder.build());
    }

}
