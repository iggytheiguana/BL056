package biz.softtechnics.qodeme.core.io.responses;

import org.json.JSONException;
import org.json.JSONObject;

import biz.softtechnics.qodeme.core.io.utils.RestKeyMap;

/**
 * Created by Alex on 8/16/13.
 */
public class AccountCreateResponse extends BaseResponse {

    private long userId;
    private String qrcode;
    private String token;

    public AccountCreateResponse(){}

    @Override
    public AccountCreateResponse parse(JSONObject jsonObject) throws JSONException {
        userId = jsonObject.getLong(RestKeyMap.ID);
        qrcode = jsonObject.getString(RestKeyMap.QRCODE);
        token = jsonObject.getString(RestKeyMap.REST_TOKEN);
        return this;
    }

    public long getUserId() {
        return userId;
    }

    public String getQrcode() {
        return qrcode;
    }

    public String getToken() {
        return token;
    }
}
