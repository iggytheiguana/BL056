package biz.softtechnics.qodeme.tests.model.connection.rest;

import android.test.AndroidTestCase;
import android.test.suitebuilder.annotation.Suppress;

import com.android.volley.RequestQueue;
import com.android.volley.VolleyError;

import java.util.concurrent.CountDownLatch;

import biz.softtechnics.qodeme.core.connection.rest.RestError;
import biz.softtechnics.qodeme.core.connection.rest.RestHelper;
import biz.softtechnics.qodeme.core.data.preference.QodemePreferences;
import biz.softtechnics.qodeme.core.io.RequestQueueSingleton;
import biz.softtechnics.qodeme.tests.model.connection.rest.utils.RestTestUtils;

/**
 * Created with IntelliJ IDEA.
 * User: Alex Vegner
 * Date: 8/28/13
 * Time: 1:56 PM
 */
public abstract class BaseTestCase extends AndroidTestCase{


    @Override
    protected void setUp() throws Exception {
        super.setUp();
        QodemePreferences.initialize(getContext());
        RestHelper.initialize(getContext());
    }

    @Suppress
    public void testAndroidTestCaseSetupProperly() {
        super.testAndroidTestCaseSetupProperly();
    }

    protected void defaultServiceError(RestError error, CountDownLatch signal){
        assertFalse(true);
        RestTestUtils.logFailed(this, error);
        signal.countDown();
    }

    protected void defaultNetworkError(VolleyError error, CountDownLatch signal){
        assertFalse(true);
        RestTestUtils.logFailed(this, error);
        signal.countDown();
    }

    protected BaseTestCase getThisRoot(){
        return this;
    }

    protected RequestQueue getQueue(){
        return RequestQueueSingleton.getInstance(getContext());
    }

}
