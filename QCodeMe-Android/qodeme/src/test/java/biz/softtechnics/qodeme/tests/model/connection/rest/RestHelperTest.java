package biz.softtechnics.qodeme.tests.model.connection.rest;


import android.test.suitebuilder.annotation.SmallTest;
import android.test.suitebuilder.annotation.Suppress;
import android.util.Log;

import com.android.volley.VolleyError;

import java.util.concurrent.CountDownLatch;

import biz.softtechnics.qodeme.R;
import biz.softtechnics.qodeme.core.connection.rest.RestClient;
import biz.softtechnics.qodeme.core.connection.rest.RestError;
import biz.softtechnics.qodeme.core.connection.rest.RestHelper;
import biz.softtechnics.qodeme.core.connection.rest.RestListener;
import biz.softtechnics.qodeme.core.connection.rest.responses.AccountCreateResponse;
import biz.softtechnics.qodeme.core.data.preference.QodemePreferences;
import biz.softtechnics.qodeme.tests.model.connection.rest.utils.RestTestUtils;
import biz.softtechnics.qodeme.utils.RestUtils;

/**
 * Created with IntelliJ IDEA.
 * User: Alex Vegner
 * Date: 8/27/13
 * Time: 2:56 PM
 */
@Suppress
public class RestHelperTest extends BaseTestCase {

    @Suppress
    @SmallTest
    public void testCase(){
        String str = getContext().getResources().getString(R.string.rest_not_documented_error);
        Log.i("TAG", str);
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        QodemePreferences.initialize(getContext());
    }


    @Suppress
    @SmallTest
    public void testAccountCreate() throws InterruptedException {
        final RestClient restClient = RestHelper.getInstance();
        final CountDownLatch signal = new CountDownLatch(1);

        String password = "password_mdp5_13";
        String passwordMd5 = RestUtils.getMd5(password);
        restClient.accountCreate(passwordMd5, new RestListener<AccountCreateResponse>() {
            @Override
            public void onResponse(AccountCreateResponse response) {
                assertTrue(true);
                signal.countDown();
                Log.i("Test","passed");
            }

            @Override
            public void onServiceError(RestError error) {
                assertFalse("test",true);
                RestTestUtils.logFailed(RestHelperTest.this, error);
                signal.countDown();
            }

            @Override
            public void onNetworkError(VolleyError error) {
                super.onNetworkError(error);
                assertFalse(true);
                RestTestUtils.logFailed(RestHelperTest.this, error);
                signal.countDown();

            }
        });
        signal.await();
    }
}