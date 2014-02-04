package biz.softtechnics.qodeme.core.io.utils;

import com.android.volley.VolleyError;

import biz.softtechnics.qodeme.core.io.responses.BaseResponse;

/**
 * Created by Alex on 8/16/13.
 */
public abstract class RestListener<T extends BaseResponse>{

    private boolean canceled;

    public abstract void onResponse(T response);

    public abstract void onServiceError(RestError error);

    public void onNetworkError(VolleyError error){}

    public boolean isCanceled() {
        return canceled;
    }

    public void cancel() {
        this.canceled = true;
    }
}
