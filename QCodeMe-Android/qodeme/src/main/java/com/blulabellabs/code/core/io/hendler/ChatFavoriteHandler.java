package com.blulabellabs.code.core.io.hendler;

import static com.blulabellabs.code.core.provider.QodemeContract.addCallerIsSyncAdapterParameter;

import java.util.ArrayList;

import android.content.ContentProviderOperation;
import android.content.Context;

import com.blulabellabs.code.core.io.responses.SetFavoriteResponse;
import com.blulabellabs.code.core.io.responses.SetFlaggedResponse;
import com.blulabellabs.code.core.io.responses.VoidResponse;
import com.blulabellabs.code.core.provider.QodemeContract;
import com.blulabellabs.code.core.provider.QodemeContract.Contacts;
import com.blulabellabs.code.core.provider.QodemeContract.SyncColumns;
import com.blulabellabs.code.utils.DbUtils;
import com.google.common.collect.Lists;

/**
 * Created by Alex on 11/27/13.
 */
public class ChatFavoriteHandler extends BaseResponseHandler<SetFavoriteResponse> {

	private long mId;
	private int is_favorite = 0;

	public ChatFavoriteHandler(Context context, long _id, int favorite) {
		super(context);
		mId = _id;
		if (favorite == 2)
			this.is_favorite = 0;
		else
			this.is_favorite = favorite;
	}

	@Override
	public ArrayList<ContentProviderOperation> parse(SetFavoriteResponse response) {
		ArrayList<ContentProviderOperation> batch = Lists.newArrayList();
		return parse(response, batch);
	}

	public ArrayList<ContentProviderOperation> parse(SetFavoriteResponse response,
			ArrayList<ContentProviderOperation> batch) {
		if (batch == null)
			batch = Lists.newArrayList();
		prepareBatch(response, batch);
		return batch;
	}

	private void prepareBatch(SetFavoriteResponse response,
			ArrayList<ContentProviderOperation> batch) {
		ContentProviderOperation.Builder builder = ContentProviderOperation
				.newUpdate(addCallerIsSyncAdapterParameter(QodemeContract.Chats.CONTENT_URI));

		builder.withValue(QodemeContract.Chats.CHAT_IS_FAVORITE, is_favorite);
		builder.withSelection(DbUtils.getWhereClauseForId(), DbUtils.getWhereArgsForId(mId));
		batch.add(builder.build());
	}

}
