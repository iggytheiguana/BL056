package com.blulabellabs.code.core.io.hendler;

import android.content.ContentProviderOperation;
import android.content.Context;

import com.blulabellabs.code.core.io.responses.VoidResponse;
import com.blulabellabs.code.utils.DbUtils;
import com.google.common.collect.Lists;

import java.util.ArrayList;


import static com.blulabellabs.code.core.provider.QodemeContract.Contacts;
import static com.blulabellabs.code.core.provider.QodemeContract.addCallerIsSyncAdapterParameter;

/**
 * Created by Alex on 11/27/13.
 */
public class ContactRejectHandler extends BaseResponseHandler<VoidResponse> {

    private long mId;

    public ContactRejectHandler(Context context, long _id) {
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
                .newDelete(addCallerIsSyncAdapterParameter(Contacts.CONTENT_URI));
        //builder.withValue(SyncColumns.UPDATED, Contacts.Sync.DONE);
        //builder.withValue(Contacts.CONTACT_STATE, Contacts.State.REJECTED);
        builder.withSelection(DbUtils.getWhereClauseForId(), DbUtils.getWhereArgsForId(mId));
        batch.add(builder.build());
    }

}
