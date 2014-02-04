package biz.softtechnics.qodeme.tests.model.connection.rest;

import android.test.suitebuilder.annotation.MediumTest;
import android.test.suitebuilder.annotation.SmallTest;
import android.test.suitebuilder.annotation.Suppress;

import com.android.volley.VolleyError;

import java.util.concurrent.CountDownLatch;

import biz.softtechnics.qodeme.core.connection.rest.RestClient;
import biz.softtechnics.qodeme.core.connection.rest.RestError;
import biz.softtechnics.qodeme.core.connection.rest.RestErrorType;
import biz.softtechnics.qodeme.core.connection.rest.RestHelper;
import biz.softtechnics.qodeme.core.connection.rest.RestListener;
import biz.softtechnics.qodeme.core.connection.rest.responses.AccountCreateResponse;
import biz.softtechnics.qodeme.core.connection.rest.responses.AccountLoginResponse;
import biz.softtechnics.qodeme.tests.model.connection.rest.utils.RestTestUtils;
import biz.softtechnics.qodeme.utils.RestUtils;


/**
 * Created with IntelliJ IDEA.
 * User: Alex Vegner
 * Date: 8/28/13
 * Time: 1:02 AM
 */
@Suppress
public class AccountLoginTest extends BaseTestCase {

    @Suppress
    @MediumTest
    public void testAccountLoginPositive() throws Throwable {
        final RestClient restClient = RestHelper.getInstance();

        final String password = "password_mdp5_1";
        final String passwordMd5 = RestUtils.getMd5(password);
        final CountDownLatch signal = new CountDownLatch(1);

        restClient.accountCreate(passwordMd5, new RestListener<AccountCreateResponse>(){
            @Override
            public void onResponse (AccountCreateResponse response){
                RestTestUtils.logPassed(AccountLoginTest.this, "accountCreate");
                restClient.accountLogin(response.getQrcode(), passwordMd5, new RestListener<AccountLoginResponse>() {

                    @Override
                    public void onResponse(AccountLoginResponse response) {
                        assertTrue(true);
                        RestTestUtils.logPassed(AccountLoginTest.this, "accountLogin");
                        signal.countDown();
                    }

                    @Override
                    public void onServiceError(RestError error) {
                        assertFalse(true);
                        RestTestUtils.logFailed(AccountLoginTest.this, error);
                        signal.countDown();
                    }

                    @Override
                    public void onNetworkError(VolleyError error) {
                        super.onNetworkError(error);
                        assertFalse(true);
                        RestTestUtils.logFailed(AccountLoginTest.this, error);
                        signal.countDown();

                    }
                });
            }

            @Override
            public void onServiceError(RestError error) {
                assertFalse(true);
                RestTestUtils.logFailed(AccountLoginTest.this, error);
                signal.countDown();
            }
        });
        signal.await();
    }

    @Suppress
    @SmallTest
    public void testAccountLoginWithWrongCredentials() throws Throwable {
        final RestClient restClient = RestHelper.getInstance();

        final String password = "password_mdp5_1";
        final String passwordMd5 = RestUtils.getMd5(password);

        // create  a signal to let us know when our task is done.
        final CountDownLatch signal = new CountDownLatch(1);


        restClient.accountCreate(passwordMd5, new RestListener<AccountCreateResponse>(){
            @Override
            public void onResponse (AccountCreateResponse response){
                RestTestUtils.logPassed(AccountLoginTest.this, "accountCreate");
                restClient.accountLogin(response.getQrcode(), /*passwordMd5*/"sdf", new RestListener<AccountLoginResponse>() {

                    @Override
                    public void onResponse(AccountLoginResponse response) {
                        assertFalse(true); // Passed with wrong credentials
                        RestTestUtils.logPassed(AccountLoginTest.this, "accountCreate");
                        signal.countDown();
                    }

                    @Override
                    public void onServiceError(RestError error) {
                        assertEquals(error.getErrorType(), RestErrorType.REST_INCORRECT_CREDENTIALS);

                        //getInstrumentation().finish(1, getInstrumentation().getAllocCounts());
                        RestTestUtils.logFailed(AccountLoginTest.this, error);
                        signal.countDown();
                    }

                    @Override
                    public void onNetworkError(VolleyError error) {
                        super.onNetworkError(error);
                        assertFalse(true);
                        RestTestUtils.logFailed(AccountLoginTest.this, error);
                        signal.countDown();

                    }
                });
            }

            @Override
            public void onServiceError(RestError error) {
                assertFalse(true);
                RestTestUtils.logFailed(AccountLoginTest.this, error);
                signal.countDown();
            }

            @Override
            public void onNetworkError(VolleyError error) {
                super.onNetworkError(error);
                assertFalse(true);
                RestTestUtils.logFailed(AccountLoginTest.this, error);
                signal.countDown();

            }
        });
        signal.await();
    }


    @Suppress
    public void testAndroidTestCaseSetupProperly() {
        super.testAndroidTestCaseSetupProperly();
    }
}
