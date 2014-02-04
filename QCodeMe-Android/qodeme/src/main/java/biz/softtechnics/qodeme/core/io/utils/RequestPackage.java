package biz.softtechnics.qodeme.core.io.utils;

/**
 * Created by Alex on 8/18/13.
 *
 */
public class RequestPackage {

    private int mothod;
    private RequestType requestType;
    private RestListener callback;
    private RequestParams params;
    private int tries;

    public RequestPackage(int mothod, RequestType requestType, RequestParams params, RestListener callback){
        this.mothod = mothod;
        this.requestType = requestType;
        this.params = params;
        this.callback = callback;
        this.tries = 0;
    }

    public int getMothod() {
        return mothod;
    }

    public RequestType getRequestType() {
        return requestType;
    }

    public RestListener getCallback() {
        return callback;
    }

    public RequestParams getParams() {
        return params;
    }

    public int getTries() {
        return tries;
    }

    public void countTries(){
        tries++;
    }

}
