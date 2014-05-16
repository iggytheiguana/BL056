package biz.softtechnics.qodeme.core.io.responses;

import org.json.JSONException;
import org.json.JSONObject;

import biz.softtechnics.qodeme.core.io.utils.RestKeyMap;

/**
 * Created by Alex on 8/16/13.
 */
public class UploadImageResponse extends BaseResponse {

    private long messageId;
    private String url = "";

    @Override
    public UploadImageResponse parse(JSONObject jsonObject) throws JSONException {
        messageId = jsonObject.getLong(RestKeyMap.MESSAGE_ID);
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
