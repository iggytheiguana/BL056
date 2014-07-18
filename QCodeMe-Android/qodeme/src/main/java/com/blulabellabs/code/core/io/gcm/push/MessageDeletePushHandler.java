package com.blulabellabs.code.core.io.gcm.push;

import com.blulabellabs.code.core.provider.QodemeContract;

import android.content.Context;
import android.os.Bundle;

/**
 * Created by Alex on 12/10/13.
 */
public class MessageDeletePushHandler extends BasePushHandler {

	private long mMessageId;

	// private int numberOfFlagged;
	// private int is_flagged = 1;

	public MessageDeletePushHandler(Context context) {
		super(context);
	}

	@Override
	public void parse(Bundle bundle) {
		mMessageId = Long.parseLong(bundle.getString("message_id"));
		// try {
		// is_flagged = Integer.parseInt(bundle.getString("is_flagged"));
		// } catch (Exception e) {
		// e.printStackTrace();
		// }
		// numberOfFlagged =
		// Integer.parseInt(bundle.getString("number_of_flagged"));

	}

	@Override
	public void handle() {
		// if(is_flagged == 1)
		getContext().getContentResolver().delete(QodemeContract.Messages.CONTENT_URI,
				QodemeContract.Messages.MESSAGE_ID + "=" + String.valueOf(mMessageId), null);
	}

}
