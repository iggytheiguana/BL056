package com.blulabellabs.code.core.io.gcm.push;

import com.blulabellabs.code.core.provider.QodemeContract;
import com.blulabellabs.code.ui.MainActivity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

/**
 * Created by Alex on 12/10/13.
 */
public class ChatDeletePushHandler extends BasePushHandler {

	private long mChatId;

	// private int numberOfFlagged;
	// private int is_flagged = 1;

	public ChatDeletePushHandler(Context context) {
		super(context);
	}

	@Override
	public void parse(Bundle bundle) {
		mChatId = Long.parseLong(bundle.getString("chat_id"));

	}

	@Override
	public void handle() {
		// if(is_flagged == 1)
		// getContext().getContentResolver().delete(QodemeContract.Messages.CONTENT_URI,
		// QodemeContract.Messages.MESSAGE_ID + "=" + String.valueOf(mChatId),
		// null);
		// getContext().getContentResolver().update(QodemeContract.Chats.CONTENT_URI,
		// QodemeContract.Chats.deleteChat(), QodemeContract.Chats.CHAT_ID +
		// " = " + mChatId,
		// null);

		getContext().getContentResolver().delete(QodemeContract.Chats.CONTENT_URI,
				QodemeContract.Chats.CHAT_ID + "=" + String.valueOf(mChatId), null);
		Intent intent = new Intent(MainActivity.DELETE_BRODCAST_ACTION);
		intent.putExtra("chat_id", mChatId);
		getContext().sendBroadcast(intent);

	}

}
