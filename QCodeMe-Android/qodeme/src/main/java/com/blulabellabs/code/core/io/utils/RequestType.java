package com.blulabellabs.code.core.io.utils;

/**
 * Created by Alex on 8/16/13.
 */
public enum RequestType{
    ACCOUNT_CREATE("account_create"),
    ACCOUNT_LOGIN("account_login"),
    ACCOUNT_LOGOUT("account_logout"),
    ACCOUNT_CONTACTS("account_contacts"),
    CHAT_CREATE("chat_create"),
    CHAT_SET_INFO("chat_set_info"),
    CHAT_ADD_MEMBER("chat_add_member"),
    CHAT_MESSAGE("chat_message"),
    CHAT_LOAD("chat_load"),
    CONTACT_ADD("contact_add"),
    //CONTACT_SET_INFO("contact_set_info"), deprecated, removed
    CONTACT_REMOVE("contact_remove"),
    CHAT_DROP_MEMBER("chat_drop_member"),
    LOOKUP("lookup"),
    REGISTER_TOKEN("register_token"),
    CONTACT_ACCEPT("contact_accept"),
    CONTACT_REJECT("contact_reject"),
    CONTACT_BLOCK("contact_block"),
    SET_USER_SETTINGS("set_user_settings"),
    GET_USER_SETTINGS("get_user_settings"),
    MESSAGE_READ("messageRead"),
    UPLOAD_IMAGE("uploadImage"),
    SET_FLAGGED("setFlagged"),
    SET_FAVORITE("setFavorite"),
    CLEAR_SEARCH("clear_search");
    



    private final String text;

    private RequestType(final String text) {
        this.text = text;
    }

    @Override
    public String toString() {
        return text;
    }
}
