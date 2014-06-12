package com.blulabellabs.code.core.io.hendler;

import java.util.ArrayList;

import android.content.ContentProviderOperation;
import android.content.Context;

import com.blulabellabs.code.core.io.responses.DeleteMessageResponse;
import com.blulabellabs.code.core.io.responses.VoidResponse;
import com.blulabellabs.code.core.provider.QodemeContract;
import com.blulabellabs.code.utils.DbUtils;
import com.google.common.collect.Lists;

/**
 * Created by Alex on 11/27/13.
 */
public class MessageDeleteHandler extends BaseResponseHandler<DeleteMessageResponse> {

	private long mId;

	public MessageDeleteHandler(Context context, long _id) {
		super(context);
		mId = _id;
	}

	@Override
	public ArrayList<ContentProviderOperation> parse(DeleteMessageResponse response) {
		ArrayList<ContentProviderOperation> batch = Lists.newArrayList();
		return parse(response, batch);
	}

	public ArrayList<ContentProviderOperation> parse(DeleteMessageResponse response,
			ArrayList<ContentProviderOperation> batch) {
		if (batch == null)
			batch = Lists.newArrayList();
		prepareBatch(response, batch);
		return batch;
	}

	private void prepareBatch(DeleteMessageResponse response,
			ArrayList<ContentProviderOperation> batch) {
		// ContentProviderOperation.Builder builder = ContentProviderOperation
		// .newUpdate(addCallerIsSyncAdapterParameter(QodemeContract.Messages.CONTENT_URI));
		// builder.withValue(SyncColumns.UPDATED, Contacts.Sync.DONE);
		// builder.withValue(QodemeContract.Messages.MESSAGE_STATE,
		// QodemeContract.Messages.State.WAS_READ);
		// builder.withSelection(DbUtils.getWhereClauseForId(),
		// DbUtils.getWhereArgsForId(mId));
		// batch.add(builder.build());

		ContentProviderOperation.Builder builder1 = ContentProviderOperation
				.newDelete(QodemeContract.Messages.CONTENT_URI);
		builder1.withSelection(DbUtils.getWhereClauseForId(), DbUtils.getWhereArgsForId(mId));
		batch.add(builder1.build());
	}

}
