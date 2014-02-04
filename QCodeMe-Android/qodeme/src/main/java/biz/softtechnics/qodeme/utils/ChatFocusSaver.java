package biz.softtechnics.qodeme.utils;

import java.util.HashMap;

/**
 * Created by kpv on 10.01.14.
 */
public class ChatFocusSaver {

    private static long focusedChat = -1;
    private static HashMap<Long, String> currentMessages = new HashMap<Long, String>();

    public static synchronized long getFocusedChatId() {
        return focusedChat;
    }

    public static synchronized void setFocusedChatId(long chatId) {
        focusedChat = chatId;
    }


    public static synchronized String getCurrentMessage(long chatId) {
        return currentMessages.get(chatId);
    }

    public static synchronized void setCurrentMessage(long chatId, String msg) {
        currentMessages.put(chatId, msg);
    }


}
