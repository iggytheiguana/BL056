package biz.softtechnics.qodeme.tests.model.connection.rest.cases;

import android.test.suitebuilder.annotation.MediumTest;
import android.test.suitebuilder.annotation.Suppress;

import biz.softtechnics.qodeme.core.connection.rest.responses.AccountContactsResponse;
import biz.softtechnics.qodeme.core.connection.rest.responses.AccountCreateResponse;
import biz.softtechnics.qodeme.core.connection.rest.responses.AccountLoginResponse;
import biz.softtechnics.qodeme.core.connection.rest.responses.ContactAddResponse;
import biz.softtechnics.qodeme.tests.model.connection.rest.BaseTestCase;
import biz.softtechnics.qodeme.tests.model.connection.rest.utils.RestSyncTestHelper;
import biz.softtechnics.qodeme.utils.RestUtils;

import static biz.softtechnics.qodeme.utils.LogUtils.makeLogTag;

/**
 * Created by Alex on 12/1/13.
 */
public class AccountContactsTest extends BaseTestCase {

    private static final String TAG = makeLogTag(AccountContactsTest.class);

    @MediumTest
    public void testAccountContacts() throws Throwable {

        RestSyncTestHelper rest = RestSyncTestHelper.getInstance(getContext());

        // Session 1 values
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

        String session2_password = RestUtils.getMd5("password_mdp5_2");
        String session2_qrcode = null;
        String session2_token = null;

        {
            // create account
            AccountCreateResponse accountCreateResponse = rest.accountCreate(session2_password);
            session2_qrcode = accountCreateResponse.getQrcode();

            // login
            AccountLoginResponse accountLoginResponse = rest.accountLogin(session2_qrcode, session2_password);
            session2_token = accountLoginResponse.getRestToken();
        }

        ContactAddResponse contactAddResponse = rest.contactAdd(session2_qrcode, "message", "public_name", "location", session1_token);
        assertTrue(contactAddResponse != null);

        {
            AccountContactsResponse accountContactsResponse = rest.accountContacts(session1_token);
        }
        {
            AccountContactsResponse accountContactsResponse = rest.accountContacts(session2_token);
        }
        assertTrue(true);

    }


    @Suppress
    public void testAndroidTestCaseSetupProperly() {
        super.testAndroidTestCaseSetupProperly();
    }


}
