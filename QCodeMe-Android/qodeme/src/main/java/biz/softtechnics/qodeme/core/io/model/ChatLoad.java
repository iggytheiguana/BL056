package biz.softtechnics.qodeme.core.io.model;

import com.google.gson.annotations.SerializedName;

/**
 * Created by Alex on 12/12/13.
 */
public class ChatLoad {

    public long _id;
    public int updated;
    @SerializedName("id")
    public long chatId;
    public int type;
    public String[] members;
    public String qrcode;
    public Message[] messages;
    public String latitude;
    public String longitude;
    public String description;
    public String status;

}
