package com.blulabellabs.code.core.io.model;

import android.os.Parcel;
import android.os.Parcelable;

public class GroupMembers implements Parcelable {

	private long chatId;
	private String date;
	private String title;
	private int isBlocked;
	private String latitude;
	private String longitude;

	public long getChatId() {
		return chatId;
	}

	public void setChatId(long chatId) {
		this.chatId = chatId;
	}

	public String getDate() {
		return date;
	}

	public void setDate(String date) {
		this.date = date;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public int getIsBlocked() {
		return isBlocked;
	}

	public void setIsBlocked(int isBlocked) {
		this.isBlocked = isBlocked;
	}

	public String getLatitude() {
		return latitude;
	}

	public void setLatitude(String latitude) {
		this.latitude = latitude;
	}

	public String getLongitude() {
		return longitude;
	}

	public void setLongitude(String longitude) {
		this.longitude = longitude;
	}

	public GroupMembers(Parcel parcel) {
		this.chatId = parcel.readLong();
		this.title = parcel.readString();
		this.date = parcel.readString();
		this.isBlocked = parcel.readInt();
		this.latitude = parcel.readString();
		this.longitude = parcel.readString();
	}

	public GroupMembers() {
	}

	@Override
	public int describeContents() {
		return 0;
	}

	@Override
	public void writeToParcel(Parcel dest, int flags) {
		dest.writeLong(this.chatId);
		dest.writeString(this.title);
		dest.writeString(this.date);
		dest.writeInt(this.isBlocked);
		dest.writeString(latitude);
		dest.writeString(longitude);
	}

	public static final Parcelable.Creator<GroupMembers> CREATOR = new Parcelable.Creator<GroupMembers>() {

		@Override
		public GroupMembers createFromParcel(Parcel source) {
			return new GroupMembers(source); // RECREATE VENUE GIVEN SOURCE
		}

		@Override
		public GroupMembers[] newArray(int size) {
			return new GroupMembers[size]; // CREATING AN ARRAY OF VENUES
		}

	};
}
