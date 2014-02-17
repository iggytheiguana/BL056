package biz.softtechnics.qodeme.core.io.hendler;

import android.content.ContentProviderOperation;
import android.content.ContentResolver;
import android.content.Context;
import android.content.OperationApplicationException;
import android.os.RemoteException;

import com.google.common.collect.Lists;

import java.util.ArrayList;

import biz.softtechnics.qodeme.core.io.responses.BaseResponse;
import biz.softtechnics.qodeme.core.provider.QodemeContract;

/**
 * Created by Alex on 12/5/13.
 */
public abstract class BaseResponseHandler<T extends BaseResponse> {

    protected static Context mContext;

    public BaseResponseHandler(Context context) {
        mContext = context;
    }

    public ArrayList<ContentProviderOperation> parse(T response){
        final ArrayList<ContentProviderOperation> batch = Lists.newArrayList();
        return parse(response, batch);
    }

    public abstract ArrayList<ContentProviderOperation> parse(T response, ArrayList<ContentProviderOperation> batch);


    public final void parseAndApply(T response) {
        try {
            final ContentResolver resolver = mContext.getContentResolver();
            ArrayList<ContentProviderOperation> batch = parse(response);
            resolver.applyBatch(QodemeContract.CONTENT_AUTHORITY, batch);
        } catch (RemoteException e) {
            throw new RuntimeException("Problem applying batch operation", e);
        } catch (OperationApplicationException e) {
            throw new RuntimeException("Problem applying batch operation", e);
        }
    }


}
