package biz.softtechnics.qodeme.tests.ui;

import android.content.Context;
import android.test.ActivityInstrumentationTestCase2;

import biz.softtechnics.qodeme.core.connection.rest.RestClient;
import biz.softtechnics.qodeme.core.connection.rest.RestHelper;
import biz.softtechnics.qodeme.ui.SplashScreen;


/**
 * This is a simple framework for a test of an Application.  See
 * {@link android.test.ApplicationTestCase ApplicationTestCase} for more information on
 * how to write and extend Application tests.
 * <p/>
 * To run this test, you can type:
 * adb shell am instrument -w \
 * -e class biz.softtechnics.qodeme.ui.test.SplashScreenTestd \
 * biz.softtechnics.qodeme.tests/android.test.InstrumentationTestRunner
 */
public class SplashScreenTest extends ActivityInstrumentationTestCase2<SplashScreen> {

    private Context context;
    private RestClient restClient;


    public SplashScreenTest() {
        super("biz.softtechnics.qodeme", SplashScreen.class);
        context = getInstrumentation().getTargetContext();
        restClient = RestHelper.getInstance();

    }

    //@SmallTest
    public void _testBlah(){
        assertEquals(1,1);
    }

    //@SmallTest
    /*public void testAccountCreate() throws Throwable {
        //Context context = getInstrumentation().getTargetContext();
        //RestClient restClient = RestHelper.getInstance(context);

        String password = "password_mdp5_1";
        String passwordMd5 = RestHelperTestUtils.getMd5(password);
        restClient.accountCreate(passwordMd5, new RestListener<AccountCreateResponse>() {
            @Override
            public void onResponse(AccountCreateResponse response) {
                assertTrue(true);
            }

            @Override
            public void onServiceError(RestError error) {
                assertFalse(true);
                RestHelperTestUtils.logFailed(SplashScreenTest.this, error);
            }

            @Override
            public void onNetworkError(VolleyError error) {
                super.onNetworkError(error);
                assertFalse(true);
                RestHelperTestUtils.logFailed(SplashScreenTest.this, error);

            }
        });

    }

    @SmallTest
    public void testAccountLogin() throws Throwable {
        Context context = getInstrumentation().getTargetContext();
        RestClient restClient = RestHelper.getInstance(context);

        String password = "password_mdp5_1";
        String passwordMd5 = RestHelperTestUtils.getMd5(password);
        restClient.accountCreate(passwordMd5, new RestListener<AccountCreateResponse>() {
            @Override
            public void onResponse(AccountCreateResponse response) {
                assertTrue(true);
            }

            @Override
            public void onServiceError(RestError error) {
                assertFalse(true);
                RestHelperTestUtils.logFailed(SplashScreenTest.this, error);
            }

            @Override
            public void onNetworkError(VolleyError error) {
                super.onNetworkError(error);
                assertFalse(true);
                RestHelperTestUtils.logFailed(SplashScreenTest.this, error);

            }
        });

    }*/
}
