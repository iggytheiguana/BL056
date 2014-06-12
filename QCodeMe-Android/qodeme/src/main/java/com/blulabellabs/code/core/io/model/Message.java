package com.blulabellabs.code.core.io.model;

import android.os.Parcel;
import android.os.Parcelable;

import com.google.gson.annotations.SerializedName;

/**
 * Created by Alex on 12/12/13.
 */
public class Message implements Parcelable{

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
	
	public Message() {
	}

	public Message(Parcel parcel) {
		this._id = parcel.readLong();
		this.updated = parcel.readInt();
		this.chatId = parcel.readLong();
		this.state = parcel.readInt();
		this.message = parcel.readString();
		this.latitude = parcel.readString();
		this.longitude = parcel.readString();
		this.messageId = parcel.readLong();
		this.created = parcel.readString();
		this.qrcode = parcel.readString();
		this.replyTo_id = parcel.readLong();
		this.hasPhoto = parcel.readInt();
		this.photoUrl = parcel.readString();
		this.localImgPath = parcel.readString();
		this.senderName = parcel.readString();
		this.is_flagged = parcel.readInt();
		this.is_deleted = parcel.readInt();
	}

	@Override
	public int describeContents() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public void writeToParcel(Parcel dest, int flags) {
		dest.writeLong(this._id);
		dest.writeInt(this.updated);
		dest.writeLong(this.chatId);
		dest.writeInt(this.state);
		dest.writeString(this.message);
		dest.writeString(this.latitude);
		dest.writeString(this.longitude);
		dest.writeLong(this.messageId);
		dest.writeString(this.created);
		dest.writeString(this.qrcode);
		dest.writeLong(this.replyTo_id);
		dest.writeInt(this.hasPhoto);
		dest.writeString(this.photoUrl);
		dest.writeString(this.localImgPath);
		dest.writeString(this.senderName);
		dest.writeInt(this.is_flagged);
		dest.writeInt(this.is_deleted);
	}

	public static final Parcelable.Creator<Message> CREATOR = new Parcelable.Creator<Message>() {

		@Override
		public Message createFromParcel(Parcel source) {
			return new Message(source); // RECREATE VENUE GIVEN SOURCE
		}

		@Override
		public Message[] newArray(int size) {
			return new Message[size]; // CREATING AN ARRAY OF VENUES
		}

	};
}
