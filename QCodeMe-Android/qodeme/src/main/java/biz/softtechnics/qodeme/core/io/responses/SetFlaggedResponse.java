package biz.softtechnics.qodeme.core.io.responses;

import org.json.JSONException;
import org.json.JSONObject;

import biz.softtechnics.qodeme.core.io.utils.RestKeyMap;

/**
 * Created by Alex on 8/16/13.
 */
public class SetFlaggedResponse extends BaseResponse {

    private long messageId;

    @Override
    public SetFlaggedResponse parse(JSONObject jsonObject) throws JSONException {
        messageId = jsonObject.getLong(RestKeyMap.MESSAGE_ID);
        return this;
    }

    public long getMessageId() {
        return messageId;
    }
}
