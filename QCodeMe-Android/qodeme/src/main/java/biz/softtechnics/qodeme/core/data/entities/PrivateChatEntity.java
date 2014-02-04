package biz.softtechnics.qodeme.core.data.entities;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import biz.softtechnics.qodeme.core.io.utils.RestKeyMap;

/**
 * Created by Alex on 8/20/13.
 */
public class PrivateChatEntity implements ParseableEntity{

    public static final int TYPE_PRIVATE = 0;

    private long id;
    private String qrcode;
    private int type = TYPE_PRIVATE;
    private List<String> memberQrcodeList;


    @Override
    public PrivateChatEntity parse(JSONObject jsonObject) throws JSONException {
        id = jsonObject.getLong(RestKeyMap.ID);
        qrcode = jsonObject.getString(RestKeyMap.QRCODE);
        JSONArray jsonArray = jsonObject.getJSONArray(RestKeyMap.MEMBER_QRCODES);
        memberQrcodeList = new ArrayList<String>();
        for (int i = 0; i < jsonArray.length(); i++) {
            String qrcode = jsonArray.getString(i);
            memberQrcodeList.add(qrcode);
        }
        return this;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getQrcode() {
        return qrcode;
    }

    public void setQrcode(String qrcode) {
        this.qrcode = qrcode;
    }

}
