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
	public Long timeStamp = 0l;// Added for sorting

	@SerializedName("replyto_id")
	public long replyTo_id;
	@SerializedName("has_photo")
	public int hasPhoto;
	@SerializedName("photourl")
	public String photoUrl;
	public String latitude;
	public String longitude;
	@SerializedName("sendername")
	public String senderName;
	public int is_flagged;
	public String localImgPath;
	
	public boolean isFirst = false;
	public boolean isLast = false;
	public int is_deleted;
	
}
