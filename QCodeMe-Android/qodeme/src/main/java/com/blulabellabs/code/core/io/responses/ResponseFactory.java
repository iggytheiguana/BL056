package com.blulabellabs.code.core.io.responses;

import org.json.JSONException;
import org.json.JSONObject;

import com.blulabellabs.code.core.io.hendler.ChatImageUploadHandler;
import com.blulabellabs.code.core.io.utils.RequestType;
import com.blulabellabs.code.core.io.utils.RestKeyMap;


/**
 * Created by Alex on 8/16/13.
 *
 * Responses:
 * AccountCreateResponse
 * AccountLoginResponse
 * AccountLogoutResponse - VoidResponse
 * AccountContactsResponse
 * ChatCreateResponse
 * ChatSetInfoResponse - VoidResponse
 * ChatAddMemberResponse
 * ChatMessageResponse - VoidResponse
 * ChatLoadResponse
 * ContactAddResponse
 * ContactRemoveResponse - VoidResponse
 * ChatDropMemberResponse - VoidResponse
 * LookupResponse
 * RegisterToken - VoidResponse
 */
public class ResponseFactory {

    public static BaseResponse getResponse(RequestType requestType, JSONObject jsonObject) throws JSONException{
        switch (requestType) {
            case ACCOUNT_CREATE:
                return new AccountCreateResponse().parse(getResult(jsonObject));
            case ACCOUNT_LOGIN:
                return new AccountLoginResponse().parse(getResult(jsonObject));
            case ACCOUNT_LOGOUT:
                return new VoidResponse();
            case ACCOUNT_CONTACTS:
                return new AccountContactsResponse().parse(getResult(jsonObject));
            case CHAT_CREATE:
                return new ChatCreateResponse().parse(getResult(jsonObject));
            case CHAT_SET_INFO:
                return new VoidResponse();
            case CHAT_ADD_MEMBER:
                return new ChatAddMemberResponse().parse(getResult(jsonObject));
            case CHAT_MESSAGE:
                return new VoidResponse();
            case CHAT_LOAD:
                return new ChatLoadResponse().parse(getResult(jsonObject));
            case CONTACT_ADD:
                return new ContactAddResponse().parse(getResult(jsonObject));
            case CONTACT_REMOVE:
                return new VoidResponse();
            case CHAT_DROP_MEMBER:
                return new VoidResponse();
            case LOOKUP:
                return new LookupResponse().parse(getResult(jsonObject));
            case REGISTER_TOKEN:
                return new VoidResponse();
            case UPLOAD_IMAGE:
                return new UploadImageResponse1().parse(getResult(jsonObject));
            case SET_FLAGGED:
                return new SetFlaggedResponse().parse(getResult(jsonObject));
            case SET_FAVORITE:
                return new SetFavoriteResponse().parse(getResult(jsonObject));
            case CLEAR_SEARCH:
                return new ClearSearchResponse().parse(jsonObject);
            case SET_SEARCHABLE:
            	return new SetSearchableResponse().parse(jsonObject);
            case DELETE_MESSAGE:
            	return new DeleteMessageResponse().parse(jsonObject);
            default:
                throw new ResponseTypeError();
        }
    }

    private static JSONObject getResult(JSONObject jsonObject) throws JSONException {
        return jsonObject.getJSONObject(RestKeyMap.RESULT);
    }



    public static class ResponseTypeError extends ClassCastException{}

}
