package com.blulabellabs.code.core.io.responses;

import org.json.JSONException;
import org.json.JSONObject;

import com.blulabellabs.code.core.io.utils.RestKeyMap;


/**
 * Created by Alex on 8/16/13.
 */
public class UploadImageResponse1 extends BaseResponse {

	private long messageId;
	private String url = "";

	@Override
	public UploadImageResponse1 parse(JSONObject jsonObject) throws JSONException {
		// JSONObject response = jsonObject.getJSONObject("result");

		messageId = Long.parseLong(jsonObject.getString(RestKeyMap.MESSAGE_ID));
		url = jsonObject.getString(RestKeyMap.IMAGE_URL);
		return this;
	}

	public long getMessageId() {
		return messageId;
	}

	public String getUrl() {
		return url;
	}
}
