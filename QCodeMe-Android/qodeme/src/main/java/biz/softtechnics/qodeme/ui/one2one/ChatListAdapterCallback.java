package biz.softtechnics.qodeme.ui.one2one;

import android.graphics.Typeface;
import android.view.View;

import java.util.List;

import biz.softtechnics.qodeme.core.io.model.ChatLoad;
import biz.softtechnics.qodeme.core.io.model.Contact;
import biz.softtechnics.qodeme.core.io.model.Message;
import biz.softtechnics.qodeme.images.utils.ImageFetcher;
import biz.softtechnics.qodeme.ui.common.ExAdapterCallback;
import biz.softtechnics.qodeme.utils.Fonts;

/**
 * Created by Alex on 10/25/13.
 */
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
    Typeface getFont(Fonts font);
    void refreshUi();
    int getNewMessagesCount(long chatId);
    void messageRead(long chatId);
    
    Contact getContact(String qrCode);
    ImageFetcher getImageFetcher();
}
