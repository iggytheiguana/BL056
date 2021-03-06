package com.blulabellabs.code.core.io.model;

import com.google.gson.annotations.SerializedName;

/**
 * Created by Alex on 12/12/13.
 */
public class ChatLoad {

    public long _id;
    public int updated;
    @SerializedName("id")
    public long chatId;
    public int type;
    public String[] members;
    public String qrcode;
    public Message[] messages;
    public String latitude;
    public String longitude;
    public String description;
    public String status;
    public String chat_status;
    public int color;
    public int chat_color;
    public String tag;
    public int number_of_flagged;
    public int is_locked;
    public int number_of_likes;
    public int number_of_members;
    public int number_of_dislikes;
    @SerializedName("chat_title")
    public String title;
    public String user_qrcode;
    public String created;
    public int is_favorite;
    public int is_searchable;
    public int is_deleted;
    public boolean isSearchResult = false;
    public boolean isTyping = false;
    public boolean isCreated = true;
}
