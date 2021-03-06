package com.blulabellabs.code.core.io.hendler;

import static com.blulabellabs.code.core.provider.QodemeContract.addCallerIsSyncAdapterParameter;

import java.util.ArrayList;
import java.util.Arrays;

import android.content.ContentProviderOperation;
import android.content.Context;

import com.blulabellabs.code.core.data.preference.QodemePreferences;
import com.blulabellabs.code.core.io.model.ChatLoad;
import com.blulabellabs.code.core.io.model.Message;
import com.blulabellabs.code.core.io.responses.ChatLoadResponse;
import com.blulabellabs.code.core.provider.QodemeContract.Chats;
import com.blulabellabs.code.core.provider.QodemeContract.Contacts;
import com.blulabellabs.code.core.provider.QodemeContract.Messages;
import com.blulabellabs.code.core.provider.QodemeContract.SyncColumns;
import com.google.android.gms.internal.bu;

/**
 * Created by Alex on 11/27/13.
 */
public class ChatLoadHandler extends BaseResponseHandler<ChatLoadResponse> {

	public ChatLoadHandler(Context context) {
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
		
		ContentProviderOperation.Builder builder = ContentProviderOperation
				.newInsert(addCallerIsSyncAdapterParameter(Chats.CONTENT_URI));
		builder.withValue(Chats.CHAT_ID, chatLoad.chatId);
		builder.withValue(Chats.CHAT_DESCRIPTION, chatLoad.description);
		builder.withValue(Chats.CHAT_COLOR, chatLoad.chat_color);
		builder.withValue(Chats.CHAT_LATITUDE, chatLoad.latitude);
		builder.withValue(Chats.CHAT_LONGITUDE, chatLoad.longitude);
		builder.withValue(Chats.CHAT_QRCODE, chatLoad.qrcode);
		builder.withValue(Chats.CHAT_STATUS, chatLoad.chat_status);
		builder.withValue(Chats.CHAT_TAGS, chatLoad.tag);
		builder.withValue(Chats.CHAT_TYPE, chatLoad.type);
		builder.withValue(Chats.CHAT_ADMIN_QRCODE, chatLoad.user_qrcode);
		builder.withValue(Chats.CHAT_TITLE, chatLoad.title);
		builder.withValue(Chats.CHAT_IS_LOCKED, chatLoad.is_locked);
		builder.withValue(Chats.CHAT_NUMBER_OF_FLAGGED, chatLoad.number_of_flagged);
		builder.withValue(Chats.CHAT_NUMBER_OF_MEMBER, chatLoad.number_of_members);
		builder.withValue(Chats.CHAT_MEMBER_QRCODES, memberQR);
		builder.withValue(Chats.CHAT_CREATED_DATE, chatLoad.created);
		builder.withValue(Chats.CHAT_NUMBER_OF_FAVORITE, chatLoad.number_of_likes);
		builder.withValue(Chats.CHAT_IS_FAVORITE, chatLoad.is_favorite);

		builder.withValue(SyncColumns.UPDATED, Contacts.Sync.DONE);
		batch.add(builder.build());

//		ContentProviderOperation.Builder builder1 = ContentProviderOperation
//				.newUpdate(addCallerIsSyncAdapterParameter(Contacts.CONTENT_URI));
//		builder1.withValue(SyncColumns.UPDATED, Contacts.Sync.DONE);
//		builder.withValue(Contacts.CONTACT_STATUS, chatLoad.status);
//		builder1.withSelection(Contacts.CONTACT_CHAT_ID+"=?", new String[]{chatLoad.chatId+""});
//		batch.add(builder1.build());
	}
	

}
