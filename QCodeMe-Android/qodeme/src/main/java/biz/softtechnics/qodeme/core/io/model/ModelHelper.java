package biz.softtechnics.qodeme.core.io.model;

import android.annotation.SuppressLint;
import android.database.Cursor;
import android.text.TextUtils;

import com.google.android.gms.internal.cu;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import biz.softtechnics.qodeme.core.provider.QodemeContract;
import biz.softtechnics.qodeme.utils.Converter;
import biz.softtechnics.qodeme.utils.NullHelper;

/**
 * Created by Alex on 11/27/13.
 */
public class ModelHelper {

	public static List<Contact> getContactList(Cursor cursor) {
		List<Contact> contactList = Lists.newArrayList();
		if (cursor.moveToFirst())
			do {
				Contact c = new Contact();
				c._id = cursor.getLong(QodemeContract.Contacts.ContactQuery._ID);
				c.updated = cursor.getInt(QodemeContract.Contacts.ContactQuery.UPDATED);
				c.contactId = cursor.getLong(QodemeContract.Contacts.ContactQuery.CONTACT_ID);
				c.title = cursor.getString(QodemeContract.Contacts.ContactQuery.CONTACT_TITLE);
				c.qrCode = cursor.getString(QodemeContract.Contacts.ContactQuery.CONTACT_QRCODE);
				c.color = cursor.getInt(QodemeContract.Contacts.ContactQuery.CONTACT_COLOR);
				c.chatId = cursor.getLong(QodemeContract.Contacts.ContactQuery.CONTACT_CHAT_ID);
				c.state = cursor.getInt(QodemeContract.Contacts.ContactQuery.CONTACT_STATE);
				c.date = cursor.getString(QodemeContract.Contacts.ContactQuery.CONTACT_DATETIME);
				c.publicName = cursor
						.getString(QodemeContract.Contacts.ContactQuery.CONTACT_PUBLIC_NAME);
				c.location = cursor
						.getString(QodemeContract.Contacts.ContactQuery.CONTACT_LOCATION);
				contactList.add(c);

			} while (cursor.moveToNext());
		return contactList;
	}
	public static List<ChatLoad> getChatList(Cursor cursor) {
		List<ChatLoad> contactList = Lists.newArrayList();
		if (cursor.moveToFirst())
			do {
				ChatLoad c = new ChatLoad();
				c._id = cursor.getLong(QodemeContract.Chats.ChatQuery._ID);
				c.updated = cursor.getInt(QodemeContract.Chats.ChatQuery.UPDATED);
				c.chatId = cursor.getLong(QodemeContract.Chats.ChatQuery.CHAT_ID);
				c.color = cursor.getInt(QodemeContract.Chats.ChatQuery.CHAT_COLOR);
				c.description = cursor.getString(QodemeContract.Chats.ChatQuery.CHAT_DESCRIPTION);
				c.latitude = cursor.getString(QodemeContract.Chats.ChatQuery.CHAT_LATITUDE);
				c.longitude = cursor.getString(QodemeContract.Chats.ChatQuery.CHAT_LONGITUDE);
				c.qrcode = cursor.getString(QodemeContract.Chats.ChatQuery.CHAT_QRCODE);
				c.status = cursor.getString(QodemeContract.Chats.ChatQuery.CHAT_STATUS);
				c.tag = cursor
						.getString(QodemeContract.Chats.ChatQuery.CHAT_TAGS);
				c.type = cursor.getInt(QodemeContract.Chats.ChatQuery.CHAT_TYPE);
				contactList.add(c);

			} while (cursor.moveToNext());
		return contactList;
	}

	public static class MessageStructure {
		private Map<Long, List<Message>> messageMap;
		private Map<Long, Integer> newMassageMap;
		private Map<Long, Long> lastMessageInChatMap;

		public Map<Long, List<Message>> getMessageMap() {
			return messageMap;
		}

		public void setMessageMap(Map<Long, List<Message>> messageMap) {
			this.messageMap = messageMap;
		}

		public Map<Long, Integer> getNewMassageMap() {
			return newMassageMap;
		}

		public void setNewMassageMap(Map<Long, Integer> newMassageMap) {
			this.newMassageMap = newMassageMap;
		}

		public Map<Long, Long> getLastMessageInChatMap() {
			return lastMessageInChatMap;
		}

		public void setLastMessageInChatMap(Map<Long, Long> lastMessageInChatMap) {
			this.lastMessageInChatMap = lastMessageInChatMap;
		}
	}

	@SuppressLint("NewApi")
	public static MessageStructure getChatMessagesMap(Cursor cursor) {
		Map<Long, List<Message>> messageMap = Maps.newHashMap();
		Map<Long, Integer> newMessageMap = Maps.newHashMap();
		Map<Long, Long> lastMessageInChatMap = Maps.newHashMap();
		MessageStructure result = new MessageStructure();
		result.setMessageMap(messageMap);
		result.setNewMassageMap(newMessageMap);
		result.setLastMessageInChatMap(lastMessageInChatMap);

		if (cursor.moveToFirst())
			do {
				Message m = new Message();
				m._id = cursor.getLong(QodemeContract.Messages.Query._ID);
				m.updated = cursor.getInt(QodemeContract.Messages.Query.UPDATED);
				m.messageId = cursor.getLong(QodemeContract.Messages.Query.MESSAGE_ID);
				m.chatId = cursor.getLong(QodemeContract.Messages.Query.MESSAGE_CHAT_ID);
				m.qrcode = cursor.getString(QodemeContract.Messages.Query.MESSAGE_QRCODE);
				m.message = cursor.getString(QodemeContract.Messages.Query.MESSAGE_TEXT);
				m.created = cursor.getString(QodemeContract.Messages.Query.MESSAGE_CREATED);
				m.state = cursor.getInt(QodemeContract.Messages.Query.MESSAGE_STATE);
				m.replyTo_id = cursor.getLong(QodemeContract.Messages.Query.MESSAGE_REPLY_TO_ID);
				m.hasPhoto = cursor.getInt(QodemeContract.Messages.Query.MESSAGE_HAS_PHOTO);
				m.photoUrl = cursor.getString(QodemeContract.Messages.Query.MESSAGE_PHOTO_URL);
				m.latitude = cursor.getString(QodemeContract.Messages.Query.MESSAGE_LATITUDE);
				m.longitude = cursor.getString(QodemeContract.Messages.Query.MESSAGE_LONGITUDE);
				m.senderName = cursor.getString(QodemeContract.Messages.Query.MESSAGE_SENDERNAME);

				Long createdLong = Converter.getCrurentTimeFromTimestamp(m.created);
				m.timeStamp = createdLong;

				Long timeMarker = lastMessageInChatMap.get(m.chatId);
				if (m.state == QodemeContract.Messages.State.NOT_READ) {
					// Last new message time in chat map
					if (timeMarker == null
							|| (timeMarker + TimeUnit.MINUTES.toMillis(1)) < createdLong) {
						lastMessageInChatMap.put(m.chatId, createdLong);
					}
					// Count of new messages
					Integer index = NullHelper.notNull(newMessageMap.get(m.chatId), 0);
					index++;
					newMessageMap.put(m.chatId, index);
				}

				List<Message> messages = messageMap.get(m.chatId);
				if (messages == null) {
					messages = Lists.newArrayList();
					messageMap.put(m.chatId, messages);
				}
				// Clean created field in case previous message has same time
				if (!messages.isEmpty()) {
					// removeTimeMarkerFoeMessage(messages, m);
				}

				messages.add(m);

			} while (cursor.moveToNext());
		return result;
	}

	@SuppressLint("NewApi")
	private static void removeTimeMarkerFoeMessage(List<Message> messages, Message mL) {
		for (int i = messages.size() - 1; i >= 0; i--) {
			Message mR = messages.get(i);
			String created = mR.created;
			if (!mR.qrcode.contentEquals(mL.qrcode))
				return;

			if (!TextUtils.isEmpty(created)) {
				Long tLl = Converter.getCrurentTimeFromTimestamp(mL.created);
				Long tRl = Converter.getCrurentTimeFromTimestamp(mR.created);
				if (tLl == null || tLl == null
						|| Math.abs(tLl - tRl) > TimeUnit.MINUTES.toMillis(1)) {
					return;
				} else {
					if (mL._id > mR._id) {
						mL.created = "";
					} else {
						mR.created = "";
					}

				}
			}
		}
		return;
	}

}
