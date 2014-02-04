package biz.softtechnics.qodeme.core.io.model;

import com.google.gson.annotations.SerializedName;

/**
 * Created by Alex on 12/12/13.
 */
public class Message {

    public long _id;
    public int updated;
    public long chatId;
    @SerializedName("id")
    public long messageId;
    public String message;
    public String created;
    @SerializedName("from_qrcode")
    public String qrcode;
    public int state;

}
