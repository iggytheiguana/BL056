package biz.softtechnics.qodeme.core.data.entities;

import org.json.JSONException;
import org.json.JSONObject;

import java.sql.Timestamp;

import biz.softtechnics.qodeme.core.io.utils.RestKeyMap;

/**
 * Created by Alex on 8/20/13.
 */
public class MessageEntity implements ParseableEntity {

    private long id;
    private long created; //time
    private String senderQrcode;
    private String message;

    @Override
    public MessageEntity parse(JSONObject jsonObject) throws JSONException {
        id = jsonObject.getLong(RestKeyMap.ID);
        created = Timestamp.valueOf(jsonObject.getString(RestKeyMap.CREATED)).getTime();
        senderQrcode = jsonObject.getString(RestKeyMap.FROM_QRCODE);
        message = jsonObject.getString(RestKeyMap.MESSAGE);
        return this;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public long getCreated() {
        return created;
    }

    public void setCreated(long created) {
        this.created = created;
    }

    public String getSenderQrcode() {
        return senderQrcode;
    }

    public void setSenderQrcode(String senderQrcode) {
        this.senderQrcode = senderQrcode;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
