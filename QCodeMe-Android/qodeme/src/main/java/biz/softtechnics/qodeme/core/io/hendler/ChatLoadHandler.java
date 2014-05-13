package biz.softtechnics.qodeme.core.io.hendler;

import android.content.ContentProviderOperation;
import android.content.Context;

import java.util.ArrayList;
import java.util.Arrays;

import biz.softtechnics.qodeme.core.io.responses.ChatLoadResponse;
import biz.softtechnics.qodeme.core.io.model.ChatLoad;
import biz.softtechnics.qodeme.core.io.model.Message;
import biz.softtechnics.qodeme.core.provider.QodemeContract.Chats;

import static biz.softtechnics.qodeme.core.provider.QodemeContract.Contacts;
import static biz.softtechnics.qodeme.core.provider.QodemeContract.Messages;
import static biz.softtechnics.qodeme.core.provider.QodemeContract.SyncColumns;
import static biz.softtechnics.qodeme.core.provider.QodemeContract.addCallerIsSyncAdapterParameter;

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

		builder.withValue(SyncColumns.UPDATED, Contacts.Sync.DONE);
		batch.add(builder.build());
	}

	private static void parseChat(ChatLoad chatLoad,ArrayList<ContentProviderOperation> batch) {
		ContentProviderOperation.Builder builder = ContentProviderOperation
				.newInsert(addCallerIsSyncAdapterParameter(Chats.CONTENT_URI));
		builder.withValue(Chats.CHAT_ID, chatLoad.chatId);
		builder.withValue(Chats.CHAT_DESCRIPTION, chatLoad.description);
		builder.withValue(Chats.CHAT_COLOR, chatLoad.color);
		builder.withValue(Chats.CHAT_LATITUDE, chatLoad.latitude);
		builder.withValue(Chats.CHAT_LONGITUDE, chatLoad.longitude);
		builder.withValue(Chats.CHAT_QRCODE, chatLoad.qrcode);
		builder.withValue(Chats.CHAT_STATUS, chatLoad.status);
		builder.withValue(Chats.CHAT_TAGS, chatLoad.tag);
		builder.withValue(Chats.CHAT_TYPE, chatLoad.type);

		builder.withValue(SyncColumns.UPDATED, Contacts.Sync.DONE);
		batch.add(builder.build());
	}

}
