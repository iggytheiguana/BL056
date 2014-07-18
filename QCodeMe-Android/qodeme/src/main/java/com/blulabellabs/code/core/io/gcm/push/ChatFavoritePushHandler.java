package com.blulabellabs.code.core.io.gcm.push;

import com.blulabellabs.code.core.provider.QodemeContract;

import android.content.ContentValues;
import android.content.Context;
import android.os.Bundle;

/**
 * Created by Alex on 12/10/13.
 */
public class ChatFavoritePushHandler extends BasePushHandler {

	private long mChatId;
	private int numberOfFavorite;

	public ChatFavoritePushHandler(Context context) {
		super(context);
	}

	@Override
	public void parse(Bundle bundle) {
		mChatId = Long.parseLong(bundle.getString("chat_id"));
		numberOfFavorite = Integer.parseInt(bundle.getString("number_of_favorites"));
	}

	@Override
	public void handle() {
		ContentValues contentValues = new ContentValues();
		contentValues.put(QodemeContract.Chats.CHAT_NUMBER_OF_FAVORITE, numberOfFavorite);
		getContext().getContentResolver().update(QodemeContract.Chats.CONTENT_URI, contentValues,
				QodemeContract.Chats.CHAT_ID + "=" + String.valueOf(mChatId), null);
	}

}
