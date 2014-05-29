package biz.softtechnics.qodeme.core.io.hendler;

import static biz.softtechnics.qodeme.core.provider.QodemeContract.addCallerIsSyncAdapterParameter;

import java.util.ArrayList;

import android.content.ContentProviderOperation;
import android.content.Context;
import biz.softtechnics.qodeme.core.io.responses.SetFlaggedResponse;
import biz.softtechnics.qodeme.core.io.responses.VoidResponse;
import biz.softtechnics.qodeme.core.provider.QodemeContract;
import biz.softtechnics.qodeme.core.provider.QodemeContract.Contacts;
import biz.softtechnics.qodeme.core.provider.QodemeContract.SyncColumns;
import biz.softtechnics.qodeme.utils.DbUtils;

import com.google.common.collect.Lists;

/**
 * Created by Alex on 11/27/13.
 */
public class MessageFlaggedHandler extends BaseResponseHandler<SetFlaggedResponse> {

    private long mId;

    public MessageFlaggedHandler(Context context, long _id) {
        super(context);
        mId = _id;
    }

    @Override
     public ArrayList<ContentProviderOperation> parse(SetFlaggedResponse response) {
        ArrayList<ContentProviderOperation> batch = Lists.newArrayList();
        return parse(response, batch);
    }

    public ArrayList<ContentProviderOperation> parse(SetFlaggedResponse response, ArrayList<ContentProviderOperation> batch) {
        if (batch == null)
            batch = Lists.newArrayList();
        prepareBatch(response, batch);
        return batch;
    }

    private void prepareBatch(SetFlaggedResponse response, ArrayList<ContentProviderOperation> batch) {
        ContentProviderOperation.Builder builder = ContentProviderOperation
                .newUpdate(addCallerIsSyncAdapterParameter(QodemeContract.Messages.CONTENT_URI));
        builder.withValue(SyncColumns.UPDATED, Contacts.Sync.DONE);
        builder.withSelection(DbUtils.getWhereClauseForId(), DbUtils.getWhereArgsForId(mId));
        batch.add(builder.build());
    }

}
