package com.blulabellabs.code.core.io.responses;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by Alex on 8/16/13.
 */
public class ClearSearchResponse extends BaseResponse {

	// private long messageId;

	@Override
	public ClearSearchResponse parse(JSONObject jsonObject) throws JSONException {
		// messageId = jsonObject.getLong(RestKeyMap.MESSAGE_ID);
		return this;
	}

	// public long getMessageId() {
	// return messageId;
	// }
}
