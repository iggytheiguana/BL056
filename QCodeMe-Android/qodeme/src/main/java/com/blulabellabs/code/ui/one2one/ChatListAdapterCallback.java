package com.blulabellabs.code.ui.one2one;

import android.view.View;

import com.blulabellabs.code.core.io.model.ChatLoad;
import com.blulabellabs.code.core.io.model.Contact;
import com.blulabellabs.code.core.io.model.Message;
import com.blulabellabs.code.images.utils.ImageFetcher;
import com.blulabellabs.code.ui.common.ExAdapterCallback;

import java.util.List;

public interface ChatListAdapterCallback extends ExAdapterCallback {
    void onSingleTap(View view, int position, Contact c);
    void onSingleTap(View view, int position, ChatLoad c);
    void onDoubleTap(View view, int position, Contact c);
    void onDoubleTap(View view, int position, ChatLoad c);
    int getChatHeight(long chatId);
    void setChatHeight(long chatId, int height);
    void setDragModeEnabled(boolean value);
    void sendMessage(Contact c, String message,  String photoUrl, int hashPhoto, long replyTo_Id, double latitude, double longitude, String senderName, String localUrl);
    void sendMessage(long c, String message,  String photoUrl, int hashPhoto, long replyTo_Id, double latitude, double longitude, String senderName, String localUrl);
    List<Message> getMessages(Contact c);
    List<Message> getMessages(long chatId);
    int getNewMessagesCount(long chatId);
    void messageRead(long chatId);
    
    Contact getContact(String qrCode);
    ImageFetcher getImageFetcher();
    int getChatType(long chatId);
    ChatLoad getChatLoad(long chatId);
}
