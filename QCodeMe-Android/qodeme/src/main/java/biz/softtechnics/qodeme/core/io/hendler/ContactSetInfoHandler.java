package biz.softtechnics.qodeme.core.io.hendler;

import android.content.ContentProviderOperation;
import android.content.Context;

import com.google.common.collect.Lists;

import java.util.ArrayList;

import biz.softtechnics.qodeme.core.io.responses.VoidResponse;
import biz.softtechnics.qodeme.utils.DbUtils;

import static biz.softtechnics.qodeme.core.provider.QodemeContract.Contacts;
import static biz.softtechnics.qodeme.core.provider.QodemeContract.SyncColumns;
import static biz.softtechnics.qodeme.core.provider.QodemeContract.addCallerIsSyncAdapterParameter;

/**
 * Created by Alex on 11/27/13.
 */
public class ContactSetInfoHandler extends BaseResponseHandler<VoidResponse> {

    private long mId;

    public ContactSetInfoHandler(Context context, long _id) {
        super(context);
        mId = _id;
    }

    @Override
     public ArrayList<ContentProviderOperation> parse(VoidResponse response) {
        ArrayList<ContentProviderOperation> batch = Lists.newArrayList();
        return parse(response, batch);
    }

    public ArrayList<ContentProviderOperation> parse(VoidResponse response, ArrayList<ContentProviderOperation> batch) {
        if (batch == null)
            batch = Lists.newArrayList();
        parseContact(batch);
        return batch;
    }

    private void parseContact(ArrayList<ContentProviderOperation> batch) {
        ContentProviderOperation.Builder builder = ContentProviderOperation
                .newUpdate(addCallerIsSyncAdapterParameter(Contacts.CONTENT_URI));
        builder.withValue(SyncColumns.UPDATED, Contacts.Sync.DONE);
        builder.withSelection(DbUtils.getWhereClauseForId(), DbUtils.getWhereArgsForId(mId));
        batch.add(builder.build());
    }

}
