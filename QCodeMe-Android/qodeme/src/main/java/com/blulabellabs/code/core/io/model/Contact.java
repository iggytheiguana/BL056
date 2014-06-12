package com.blulabellabs.code.core.io.model;

import android.os.Parcel;
import android.os.Parcelable;

import com.google.gson.annotations.SerializedName;

/**
 * Created by Alex on 11/27/13.
 */
public class Contact implements Parcelable {

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
	public int isArchive = 0;
	public String latitude = "0";
	public String longitude = "0";

	public Contact() {
	}

	public Contact(Parcel parcel) {
		this._id = parcel.readLong();
		this.updated = parcel.readInt();
		this.contactId = parcel.readLong();
		this.title = parcel.readString();
		this.qrCode = parcel.readString();
		this.color = parcel.readInt();
		this.chatId = parcel.readLong();
		this.state = parcel.readInt();
		this.message = parcel.readString();
		this.publicName = parcel.readString();
		this.date = parcel.readString();
		this.location = parcel.readString();
		this.isArchive = parcel.readInt();
		this.latitude = parcel.readString();
		this.longitude = parcel.readString();
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
		dest.writeLong(this.contactId);
		dest.writeString(this.title);
		dest.writeString(this.qrCode);
		dest.writeInt(this.color);
		dest.writeLong(this.chatId);
		dest.writeInt(this.state);
		dest.writeString(this.message);
		dest.writeString(this.publicName);
		dest.writeString(this.date);
		dest.writeString(this.location);
		dest.writeInt(this.isArchive);
		dest.writeString(this.latitude);
		dest.writeString(this.longitude);

	}

	public static final Parcelable.Creator<Contact> CREATOR = new Parcelable.Creator<Contact>() {

		@Override
		public Contact createFromParcel(Parcel source) {
			return new Contact(source); // RECREATE VENUE GIVEN SOURCE
		}

		@Override
		public Contact[] newArray(int size) {
			return new Contact[size]; // CREATING AN ARRAY OF VENUES
		}

	};
}
