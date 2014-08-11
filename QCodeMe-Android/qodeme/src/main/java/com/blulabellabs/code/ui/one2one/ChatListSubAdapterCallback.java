package com.blulabellabs.code.ui.one2one;

import com.blulabellabs.code.core.io.model.ChatLoad;
import com.blulabellabs.code.core.io.model.Contact;


public interface ChatListSubAdapterCallback  {
    int getColor(String senderQrcode);
    Contact getContact(String senderQrcode);
    ChatLoad getChatLoad(long chatId);

}
