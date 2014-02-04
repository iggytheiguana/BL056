package biz.softtechnics.qodeme.core.io.gcm.push;

import android.content.Context;
import android.os.Bundle;

/**
 * Created by Alex on 12/10/13.
 */
public abstract class BasePushHandler {

    private final Context mContext;

    public BasePushHandler(Context context){
        mContext = context;
    }

    protected Context getContext(){
        return mContext;
    }

    public abstract void parse(Bundle bundle);

    public abstract void handle();

}
