package com.blulabellabs.code.core.io.utils;

import com.blulabellabs.code.core.data.entities.ChatType;

/**
 * Created by Alex on 8/20/13.
 */
public interface RestClient {
    void accountCreate(String passwordMd5, RestListener callback);
    void accountLogin(String qrcode, String passwordMd5, RestListener callback);
    void accountLogin(RestListener callback);
    void accountLogout(RestListener callback);
    void accountContacts(RestListener callback);
    void chatCreate(ChatType chatType, String title, String tags, int color,String description, int is_locked, String status, double latitude, double longitude, RestListener callback);
    void chatSetInfo(long chatId, String title, Integer color,String tags,String description, Integer is_locked, String status, String chat_title, String latitude, String longitude, RestListener callback);
    void chatAddMember(long chatId, String qrcode, RestListener callback);
    void chatMessage(long chatId, String message, long date,String photoUrl, int hasPhoto,int replyToId,int isFlagged,String senderName, double latitude, double longitude, RestListener callback);
    void chatLoad(long chatId, int page, int limit, RestListener callback);
    void contactAdd(String contactCqroce,double latitude, double longitude, RestListener callback);
    void contactRemove(String qr, long contactId, RestListener callback);
    void chatDropMember(long chatId, String memberQrcode, RestListener callback);
    void lookup(String searchQuery, int type, int pageNo, RestListener callback);
    void registerToken(String gcmToken, RestListener callback);
    void chatImage(long messageId, String imageString, RestListener callback);
    void setFavorite(String date, int is_favorite, long chatId, RestListener callback);
    void setSearchable(int is_searchable, long chatId, RestListener callback);
    void clearSearchChats(int type, RestListener callback);
    void deleteChat(long chatId, RestListener callback);
}
