package biz.softtechnics.qodeme.core.io.utils;

import android.content.Context;

/**
 * Created by Alex on 8/16/13.
 */
public class RestError extends Exception{

    private RestErrorType mErrorType;
    private String mServerMsg;

    public RestError(){}

    public RestError(RestErrorType errorType, String serverMsg){
        super(String.format("%d: %s", errorType.getValue(), serverMsg));
        mErrorType = errorType;
        mServerMsg = serverMsg;
    }

    public RestErrorType getErrorType() {
        return mErrorType;
    }

    public RestError setErrorType(RestErrorType errorType) {
        mErrorType = errorType;
        return this;
    }

    public String getServerMsg() {
        return mServerMsg;
    }

    public RestError setServerMsg(String serverMsg) {
        mServerMsg = serverMsg;
        return this;
    }

    public String toString(Context context) {
        return String.format("RestError: %s: %s", RestErrorType.getMessage(context, mErrorType), mServerMsg == null ? "" : mServerMsg);
    }
}
