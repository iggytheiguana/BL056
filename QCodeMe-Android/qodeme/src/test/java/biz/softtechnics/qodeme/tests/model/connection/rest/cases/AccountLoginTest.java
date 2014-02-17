package biz.softtechnics.qodeme.tests.model.connection.rest.cases;

import android.test.suitebuilder.annotation.SmallTest;
import android.test.suitebuilder.annotation.Suppress;

import biz.softtechnics.qodeme.core.connection.rest.RestError;
import biz.softtechnics.qodeme.core.connection.rest.RestErrorType;
import biz.softtechnics.qodeme.core.connection.rest.responses.AccountCreateResponse;
import biz.softtechnics.qodeme.core.connection.rest.responses.AccountLoginResponse;
import biz.softtechnics.qodeme.core.connection.rest.responses.VoidResponse;
import biz.softtechnics.qodeme.tests.model.connection.rest.BaseTestCase;
import biz.softtechnics.qodeme.tests.model.connection.rest.utils.RestSyncTestHelper;
import biz.softtechnics.qodeme.utils.RestUtils;

import static biz.softtechnics.qodeme.utils.LogUtils.makeLogTag;


/**
 * Created with IntelliJ IDEA.
 * User: Alex Vegner
 * Date: 8/28/13
 * Time: 1:02 AM
 */
public class AccountLoginTest extends BaseTestCase {

    private static final String TAG = makeLogTag(ContactAddTest.class);

    // Test session 1

    @SmallTest
    public void testAccountLoginPositive() throws Throwable {
        RestSyncTestHelper rest = RestSyncTestHelper.getInstance(getContext());

        // Session one values
        String session1_password = RestUtils.getMd5("password_mdp5_1");
        String session1_qrcode = null;
        String session1_token = null;

        {
            // create account
            AccountCreateResponse accountCreateResponse = rest.accountCreate(session1_password);
            session1_qrcode = accountCreateResponse.getQrcode();

            // login
            AccountLoginResponse accountLoginResponse = rest.accountLogin(session1_qrcode, session1_password);
            session1_token = accountLoginResponse.getRestToken();
        }
    }

    @SmallTest
    public void testAccountLoginWithWrongCredentials() throws Throwable {
        RestSyncTestHelper rest = RestSyncTestHelper.getInstance(getContext());

        String session1_password = RestUtils.getMd5("password_mdp5_1");
        String session1_qrcode = null;
        String session1_token = null;

        // create account
        AccountCreateResponse accountCreateResponse = rest.accountCreate(session1_password);
        session1_qrcode = accountCreateResponse.getQrcode();

        // login
        try {
            AccountLoginResponse accountLoginResponse = rest.accountLogin(session1_qrcode, "incorrect_password");
        } catch (RestError e) {
            assertEquals(e.getErrorType(), RestErrorType.REST_INCORRECT_CREDENTIALS);
        }
    }

    @SmallTest
    public void testAccountLogout() throws Throwable {
        RestSyncTestHelper rest = RestSyncTestHelper.getInstance(getContext());

        // Session one values
        String session1_password = RestUtils.getMd5("password_mdp5_1");
        String session1_qrcode = null;
        String session1_token = null;

        // create account
        AccountCreateResponse accountCreateResponse = rest.accountCreate(session1_password);
        session1_qrcode = accountCreateResponse.getQrcode();

        // login
        AccountLoginResponse accountLoginResponse = rest.accountLogin(session1_qrcode, session1_password);
        session1_token = accountLoginResponse.getRestToken();

        // logout

        VoidResponse voidResponse = rest.accountLogout(session1_token);

        assertTrue(voidResponse != null);
    }

    @Suppress
    public void testAndroidTestCaseSetupProperly() {
        super.testAndroidTestCaseSetupProperly();
    }
}
