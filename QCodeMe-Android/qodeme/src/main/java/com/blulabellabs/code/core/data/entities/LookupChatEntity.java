package com.blulabellabs.code.core.data.entities;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.blulabellabs.code.core.io.model.Message;
import com.blulabellabs.code.core.io.utils.RestKeyMap;
import com.blulabellabs.code.utils.Converter;
import com.google.gson.JsonObject;

/**
 * Created by Alex on 8/21/13.
 */
public class LookupChatEntity extends ChatEntity {
	private String title;
	private String tags;
	private String status;
	private int number_of_likes;
	private int number_of_member;
	private String description;
	private String longitude;
	private String latitude;
	private int is_favorite = 0;
	private String created = "";
	private Message[] messages;

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}

	public int getNumber_of_likes() {
		return number_of_likes;
	}

	public void setNumber_of_likes(int number_of_likes) {
		this.number_of_likes = number_of_likes;
	}

	public int getNumber_of_member() {
		return number_of_member;
	}

	public void setNumber_of_member(int number_of_member) {
		this.number_of_member = number_of_member;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public String getLongitude() {
		return longitude;
	}

	public void setLongitude(String longitude) {
		this.longitude = longitude;
	}

	public String getLatitude() {
		return latitude;
	}

	public void setLatitude(String latitude) {
		this.latitude = latitude;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public void setTags(String tags) {
		this.tags = tags;
	}

	@Override
	public LookupChatEntity parse(JSONObject jsonObject) throws JSONException {
		super.parse(jsonObject);
		title = jsonObject.getString(RestKeyMap.TITLE);
		tags = jsonObject.getString(RestKeyMap.TAGS);
		status = jsonObject.getString(RestKeyMap.STATUS);
		description = jsonObject.getString(RestKeyMap.DESCRIPTION);
		latitude = jsonObject.getString(RestKeyMap.LATITUDE);
		longitude = jsonObject.getString(RestKeyMap.LONGITUDE);
		is_favorite = jsonObject.getInt("is_favorite");
		created = jsonObject.getString("created");

		try {
			number_of_member = Integer.parseInt(jsonObject.getString("number_of_members"));
			number_of_likes = Integer.parseInt(jsonObject.getString("number_of_likes"));

			JSONArray array = jsonObject.getJSONArray("messages");
			setMessages(new Message[array.length()]);
			for (int i = 0; i < array.length(); i++) {
				JSONObject jsonObject2 = array.getJSONObject(i);
				getMessages()[i] = new Message();
				getMessages()[i].chatId = getId();
				getMessages()[i].created = jsonObject2.getString("created");
				getMessages()[i].hasPhoto = Integer.parseInt(jsonObject2.getString("has_photo"));
				getMessages()[i].is_flagged = Integer.parseInt(jsonObject2.getString("is_flagged"));
				getMessages()[i].latitude = jsonObject2.getString("latitude");
				getMessages()[i].longitude = jsonObject2.getString("longitude");
				getMessages()[i].message = jsonObject2.getString("message");
				getMessages()[i].photoUrl = jsonObject2.getString("photourl");
				getMessages()[i].qrcode = jsonObject2.getString("from_qrcode");
				getMessages()[i].replyTo_id = Long.parseLong(jsonObject2.getString("replyto_id"));
				getMessages()[i].senderName = jsonObject2.getString("sendername");
				getMessages()[i].state = Integer.parseInt(jsonObject2.getString("state"));
				getMessages()[i].messageId = Long.parseLong(jsonObject2.getString("id"));

				Long createdLong = Converter.getCrurentTimeFromTimestamp(getMessages()[i].created);
				getMessages()[i].timeStamp = createdLong;
			}

		} catch (Exception e) {

		}

		return this;
	}

	public String getTitle() {
		return title;
	}

	public String getTags() {
		return tags;
	}

	public void setIs_favorite(int is_favorite) {
		this.is_favorite = is_favorite;
	}

	public int getIs_favorite() {
		return is_favorite;
	}

	public void setCreated(String created) {
		this.created = created;
	}

	public String getCreated() {
		return created;
	}

	public void setMessages(Message[] messages) {
		this.messages = messages;
	}

	public Message[] getMessages() {
		return messages;
	}

	
}
