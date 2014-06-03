package com.blulabellabs.code.core.io.hendler;

import android.content.ContentProviderOperation;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;

import java.util.ArrayList;
import java.util.Arrays;

import com.blulabellabs.code.core.data.preference.QodemePreferences;
import com.blulabellabs.code.core.io.model.ChatLoad;
import com.blulabellabs.code.core.io.model.Message;
import com.blulabellabs.code.core.io.responses.ChatLoadResponse;
import com.blulabellabs.code.core.provider.QodemeContract;
import com.blulabellabs.code.core.provider.QodemeContract.Chats;
import com.blulabellabs.code.core.provider.QodemeContract.Contacts;
import com.blulabellabs.code.core.provider.QodemeContract.SyncColumns;
import com.blulabellabs.code.utils.DbUtils;
import com.flurry.sdk.ch;
import com.google.android.gms.internal.bu;

import static com.blulabellabs.code.core.provider.QodemeContract.Messages;
import static com.blulabellabs.code.core.provider.QodemeContract.addCallerIsSyncAdapterParameter;

/**
 * Created by Alex on 11/27/13.
 */
public class ChatLoadAddMemberHandler extends BaseResponseHandler<ChatLoadResponse> {

	public ChatLoadAddMemberHandler(Context context) {
		super(context);
	}

	@Override
	public ArrayList<ContentProviderOperation> parse(ChatLoadResponse response,
			ArrayList<ContentProviderOperation> batch) {

		parseChat(response.getChatLoad(), batch);
		for (Message m : Arrays.asList(response.getChatLoad().messages)) {
			m.chatId = response.getChatLoad().chatId;
			parseMessage(m, batch);
		}
		return batch;
	}

	private static void parseMessage(Message m, ArrayList<ContentProviderOperation> batch) {
		ContentProviderOperation.Builder builder = ContentProviderOperation
				.newInsert(addCallerIsSyncAdapterParameter(Messages.CONTENT_URI));
		builder.withValue(Messages.MESSAGE_ID, m.messageId);
		builder.withValue(Messages.MESSAGE_CHAT_ID, m.chatId);
		builder.withValue(Messages.MESSAGE_TEXT, m.message);
		builder.withValue(Messages.MESSAGE_QRCODE, m.qrcode);
		builder.withValue(Messages.MESSAGE_CREATED, m.created);
		builder.withValue(Messages.MESSAGE_STATE, m.state);
		builder.withValue(Messages.MESSAGE_PHOTO_URL, m.photoUrl);
		builder.withValue(Messages.MESSAGE_HASH_PHOTO, m.hasPhoto);
		builder.withValue(Messages.MESSAGE_REPLY_TO_ID, m.replyTo_id);
		builder.withValue(Messages.MESSAGE_LATITUDE, m.latitude);
		builder.withValue(Messages.MESSAGE_LONGITUDE, m.longitude);
		builder.withValue(Messages.MESSAGE_SENDERNAME, m.senderName);
		builder.withValue(Messages.MESSAGE_HAS_FLAGGED, m.is_flagged);

		builder.withValue(SyncColumns.UPDATED, Contacts.Sync.DONE);
		batch.add(builder.build());
	}

	private static void parseChat(ChatLoad chatLoad, ArrayList<ContentProviderOperation> batch) {
		// ContentProviderOperation.Builder builder = ContentProviderOperation
		// .newInsert(addCallerIsSyncAdapterParameter(Chats.CONTENT_URI));

		String[] members = chatLoad.members;
		String memberQR = "";
		if (members != null && members.length > 0) {
			for (String qr : members) {
				if (!QodemePreferences.getInstance().getQrcode().equals(qr.trim())) {
					if (memberQR.equals(""))
						memberQR = qr;
					else
						memberQR += "," + qr;
				}
			}
		}

		ContentValues builder = new ContentValues();
		builder.put(Chats.CHAT_ID, chatLoad.chatId);
		builder.put(Chats.CHAT_DESCRIPTION, chatLoad.description);
		builder.put(Chats.CHAT_COLOR, chatLoad.color);
		builder.put(Chats.CHAT_LATITUDE, chatLoad.latitude);
		builder.put(Chats.CHAT_LONGITUDE, chatLoad.longitude);
		builder.put(Chats.CHAT_QRCODE, chatLoad.qrcode);
		builder.put(Chats.CHAT_STATUS, chatLoad.chat_status);
		builder.put(Chats.CHAT_TAGS, chatLoad.tag);
		builder.put(Chats.CHAT_TYPE, chatLoad.type);
		builder.put(Chats.CHAT_ADMIN_QRCODE, chatLoad.user_qrcode);
		builder.put(Chats.CHAT_TITLE, chatLoad.title);
		builder.put(Chats.CHAT_IS_LOCKED, chatLoad.is_locked);
		builder.put(Chats.CHAT_NUMBER_OF_FLAGGED, chatLoad.number_of_flagged);
		builder.put(Chats.CHAT_NUMBER_OF_MEMBER, chatLoad.number_of_members);
		builder.put(Chats.CHAT_MEMBER_QRCODES, memberQR);

		builder.put(SyncColumns.UPDATED, Contacts.Sync.DONE);
		// batch.add(builder.build());
		ContentResolver resolver = mContext.getContentResolver();
		resolver.update(QodemeContract.Chats.CONTENT_URI, builder, QodemeContract.Chats.CHAT_ID
				+ " = " + chatLoad.chatId, null);
		// ContentProviderOperation.Builder builder1 = ContentProviderOperation
		// .newUpdate(addCallerIsSyncAdapterParameter(Contacts.CONTENT_URI));
		// builder1.withValue(SyncColumns.UPDATED, Contacts.Sync.DONE);
		// builder.withValue(Contacts.CONTACT_STATUS, chatLoad.status);
		// builder1.withSelection(Contacts.CONTACT_CHAT_ID+"=?", new
		// String[]{chatLoad.chatId+""});
		// batch.add(builder1.build());
	}

}
