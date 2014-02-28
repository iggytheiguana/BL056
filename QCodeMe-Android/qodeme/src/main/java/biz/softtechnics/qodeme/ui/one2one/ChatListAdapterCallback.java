package biz.softtechnics.qodeme.ui.one2one;

import android.graphics.Typeface;
import android.view.View;

import java.util.List;

import biz.softtechnics.qodeme.core.io.model.Contact;
import biz.softtechnics.qodeme.core.io.model.Message;
import biz.softtechnics.qodeme.ui.common.ExAdapterCallback;
import biz.softtechnics.qodeme.utils.Fonts;

/**
 * Created by Alex on 10/25/13.
 */
public interface ChatListAdapterCallback extends ExAdapterCallback {
    void onSingleTap(View view, int position, Contact c);
    void onDoubleTap(View view, int position, Contact c);
    int getChatHeight(long chatId);
    void setChatHeight(long chatId, int height);
    void setDragModeEnabled(boolean value);
    void sendMessage(Contact c, String message);
    List<Message> getMessages(Contact c);
    Typeface getFont(Fonts font);
    void refreshUi();
    int getNewMessagesCount(long chatId);
    void messageRead(long chatId);
}