package com.blulabellabs.code.core.data.entities;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.blulabellabs.code.core.io.utils.RestKeyMap;

import java.util.ArrayList;
import java.util.List;


/**
 * Created by Alex on 8/20/13.
 */
public class DetailChatEntity extends ChatEntity{

    public static final int TYPE_PRIVATE = 1;
    public static final int TYPE_PUBLIC = 2;

    private int type;
    private List<String> memberQrcodeList;

    @Override
    public DetailChatEntity parse(JSONObject jsonObject) throws JSONException {
        super.parse(jsonObject);
        type = jsonObject.getInt(RestKeyMap.CHAT_TYPE);
        JSONArray jsonArray = jsonObject.getJSONArray(RestKeyMap.MEMBER_QRCODES);
        memberQrcodeList = new ArrayList<String>();
        for (int i = 0; i < jsonArray.length(); i++) {
            String qrcode = jsonArray.getString(i);
            memberQrcodeList.add(qrcode);
        }
        return this;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public List<String> getMemberQrcodeList() {
        return memberQrcodeList;
    }

    public void setMemberQrcodeList(List<String> memberQrcodeList) {
        this.memberQrcodeList = memberQrcodeList;
    }

}
