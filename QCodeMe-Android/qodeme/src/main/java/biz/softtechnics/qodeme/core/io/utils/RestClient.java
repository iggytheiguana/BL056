package biz.softtechnics.qodeme.core.io.utils;

import biz.softtechnics.qodeme.core.data.entities.ChatType;

/**
 * Created by Alex on 8/20/13.
 */
public interface RestClient {
    void accountCreate(String passwordMd5, RestListener callback);
    void accountLogin(String qrcode, String passwordMd5, RestListener callback);
    void accountLogin(RestListener callback);
    void accountLogout(RestListener callback);
    void accountContacts(RestListener callback);
    void chatCreate(ChatType chatType, String title, String tags, int color, RestListener callback);
    void chatSetInfo(long chatId, String title, int color, RestListener callback);
    void chatAddMember(long chatId, String qrcode, RestListener callback);
    void chatMessage(long chatId, String message, long date, RestListener callback);
    void chatLoad(long chatId, int page, int limit, RestListener callback);
    void contactAdd(String contactCqroce, RestListener callback);
    void contactRemove(long contactId, RestListener callback);
    void chatDropMember(long chatId, String memberQrcode, RestListener callback);
    void lookup(String searchQuery, RestListener callback);
    void registerToken(String gcmToken, RestListener callback);
}