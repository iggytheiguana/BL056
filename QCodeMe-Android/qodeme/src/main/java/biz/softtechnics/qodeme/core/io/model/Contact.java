package biz.softtechnics.qodeme.core.io.model;

import com.google.gson.annotations.SerializedName;

/**
 * Created by Alex on 11/27/13.
 */
public class Contact {

    public long _id;
    public int updated;
    @SerializedName("id")
    public long contactId;
    public String title;
    @SerializedName("qrcode")
    public String qrCode;
    public int color;
    @SerializedName("private_chat_id")
    public long chatId;
    public int state;
    public String message;
    @SerializedName("public_name")
    public String publicName;
    @SerializedName("datetime")
    public String date;
    public String location;

}
