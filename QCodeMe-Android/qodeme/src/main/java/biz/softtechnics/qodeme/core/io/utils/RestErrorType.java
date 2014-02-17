package biz.softtechnics.qodeme.core.io.utils;

import android.content.Context;

import biz.softtechnics.qodeme.R;

/**
 * Created by Alex on 8/16/13.
 * This class contain all REST errors and errors which appear during parsing
 */
public enum RestErrorType {
    /*Parse errors*/
    REST_PARSE_STATUS(10001),
    REST_PARSE_RESULT(10002),

    /*REST errors*/
    REST_NOT_DOCUMENTED_ERROR(10000),
    REST_INVALID_TOKEN(99),
    REST_UNKNOWN_ERROR(100),
    REST_ACCOUNT_CREATE(101),
    REST_INCORRECT_CREDENTIALS(201),
    REST_ACCOUNT_WAS_TAKEN(203),

    REST_CHAT_SET_INFO_FIELD_VALIDATION(301),
    REST_CHAT_SET_INFO_CHAT_NOT_FOUND(302),
    REST_CHAT_ADD_MEMBER_FIELD_VALIDATION(401),
    REST_CHAT_ADD_MEMBER_ALREADY_MEMBER(402),
    REST_CHAT_ADD_MEMBER_PERMISSIONS_DENIED(403),
    REST_CHAT_ADD_MEMBER_CHAT_NOT_FOUND(404),
    REST_CHAT_MESSAGE_NOT_A_MEMBER(501),
    REST_CHAT_MESSAGE_CHAT_NOT_FOUND(502),
    REST_CHAT_LOAD_CHAT_NOT_A_MEMBER(601),
    REST_CHAT_LOAD_CHAT_NOT_FOUND(602),
    REST_CONTACT_SET_INFO_USER_NOT_FOUND(701),
    REST_CONTACT_SET_INFO_NO_PERMISSION(701),
    REST_CONTACT_REMOVE_CONTACT_NOT_FOUND(801),
    REST_CHAT_DROP_MEMBER_PERMISSIONS_DENIED(901),
    REST_CHAT_DROP_MEMBER_NOT_A_MEMBER(902),
    REST_CHAT_DROP_MEMBER_CHAT_NOT_FOUND(903);

    private final int value;

    private RestErrorType(int value) {
        this.value = value;
    }
    public int getValue() {
        return value;
    }

    public static RestErrorType gerRestError(int errorId){
        RestErrorType[] restErrors = REST_NOT_DOCUMENTED_ERROR.getDeclaringClass().getEnumConstants();
        for (int i = 0; i < restErrors.length; i++){
            if (restErrors[i].getValue() == errorId){
                return restErrors[i];
            }
        }
        return REST_NOT_DOCUMENTED_ERROR;
    }

    public static String getMessage(Context context, RestErrorType restError){
        switch (restError) {
            case REST_PARSE_STATUS:
                return context.getResources().getString(R.string.rest_parse_status);
            case REST_PARSE_RESULT:
                return context.getResources().getString(R.string.rest_parse_result);
            case REST_NOT_DOCUMENTED_ERROR:
                return context.getResources().getString(R.string.rest_not_documented_error);
            case REST_INVALID_TOKEN:
                return context.getResources().getString(R.string.rest_invalid_token);
            case REST_UNKNOWN_ERROR:
                return context.getResources().getString(R.string.rest_unknown_error);
            case REST_ACCOUNT_CREATE:
                return context.getResources().getString(R.string.rest_account_create);
            case REST_INCORRECT_CREDENTIALS:
                return context.getResources().getString(R.string.rest_incorrect_credentials);
            case REST_CHAT_SET_INFO_FIELD_VALIDATION:
                return context.getResources().getString(R.string.rest_chat_set_info_field_validation);
            case REST_CHAT_SET_INFO_CHAT_NOT_FOUND:
                return context.getResources().getString(R.string.rest_chat_set_info_chat_not_found);
            case REST_CHAT_ADD_MEMBER_FIELD_VALIDATION:
                return context.getResources().getString(R.string.rest_chat_add_member_field_validation);
            case REST_CHAT_ADD_MEMBER_ALREADY_MEMBER:
                return context.getResources().getString(R.string.rest_chat_add_member_already_member);
            case REST_CHAT_ADD_MEMBER_PERMISSIONS_DENIED:
                return context.getResources().getString(R.string.rest_chat_add_member_permissions_denied);
            case REST_CHAT_ADD_MEMBER_CHAT_NOT_FOUND:
                return context.getResources().getString(R.string.rest_chat_add_member_chat_not_found);
            case REST_CHAT_MESSAGE_NOT_A_MEMBER:
                return context.getResources().getString(R.string.rest_chat_message_not_a_member);
            case REST_CHAT_MESSAGE_CHAT_NOT_FOUND:
                return context.getResources().getString(R.string.rest_chat_message_chat_not_found);
            case REST_CHAT_LOAD_CHAT_NOT_A_MEMBER:
                return context.getResources().getString(R.string.rest_chat_load_chat_not_a_member);
            case REST_CHAT_LOAD_CHAT_NOT_FOUND:
                return context.getResources().getString(R.string.rest_chat_load_chat_not_found);
            case REST_CONTACT_SET_INFO_USER_NOT_FOUND:
                return context.getResources().getString(R.string.rest_contact_set_info_user_not_found);
            case REST_CONTACT_SET_INFO_NO_PERMISSION:
                return context.getResources().getString(R.string.rest_contact_set_info_no_permission);
            case REST_CONTACT_REMOVE_CONTACT_NOT_FOUND:
                return context.getResources().getString(R.string.rest_contact_remove_contact_not_found);
            case REST_CHAT_DROP_MEMBER_PERMISSIONS_DENIED:
                return context.getResources().getString(R.string.rest_chat_drop_member_permissions_denied);
            case REST_CHAT_DROP_MEMBER_NOT_A_MEMBER:
                return context.getResources().getString(R.string.rest_chat_drop_member_not_a_member);
            case REST_CHAT_DROP_MEMBER_CHAT_NOT_FOUND:
                return context.getResources().getString(R.string.rest_chat_drop_member_chat_not_found);
            default:
                return "No resource for the error, please define a resource in class";
        }

    }







}
