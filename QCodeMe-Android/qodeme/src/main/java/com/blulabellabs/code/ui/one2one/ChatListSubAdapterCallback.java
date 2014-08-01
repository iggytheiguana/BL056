package com.blulabellabs.code.ui.one2one;

import com.blulabellabs.code.core.io.model.ChatLoad;
import com.blulabellabs.code.core.io.model.Contact;
import com.blulabellabs.code.ui.common.ExAdapterCallback;
import com.blulabellabs.code.utils.Fonts;

import android.graphics.Typeface;


public interface ChatListSubAdapterCallback extends ExAdapterCallback {
    int getColor(String senderQrcode);
    Contact getContact(String senderQrcode);
    ChatLoad getChatLoad(long chatId);

}
