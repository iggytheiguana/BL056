package biz.softtechnics.qodeme.tests.model.connection.rest.cases;

import android.test.suitebuilder.annotation.SmallTest;
import android.test.suitebuilder.annotation.Suppress;

import biz.softtechnics.qodeme.core.connection.rest.RestError;
import biz.softtechnics.qodeme.core.connection.rest.RestErrorType;
import biz.softtechnics.qodeme.core.connection.rest.responses.AccountContactsResponse;
import biz.softtechnics.qodeme.core.connection.rest.responses.AccountCreateResponse;
import biz.softtechnics.qodeme.core.connection.rest.responses.AccountLoginResponse;
import biz.softtechnics.qodeme.core.connection.rest.responses.ContactAddResponse;
import biz.softtechnics.qodeme.tests.model.connection.rest.BaseTestCase;
import biz.softtechnics.qodeme.tests.model.connection.rest.utils.RestSyncTestHelper;
import biz.softtechnics.qodeme.utils.RestUtils;

import static biz.softtechnics.qodeme.utils.LogUtils.makeLogTag;

/**
 * Created by Alex on 12/2/13.
 */
public class ContactAcceptRejectBlockTest extends BaseTestCase {

    private static final String TAG = makeLogTag(AccountContactsTest.class);

    @SmallTest
    public void testContactAccept() throws Throwable {
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
        //assertTrue(contactAddResponse != null);

        // send message
        //rest.chatMessage(contactAddResponse.getContact().chatId, "Message 1 ", session1_token);
        //rest.chatMessage(contactAddResponse.getContact().chatId, "Message 2 ", session1_token);

        // get message
        rest.chatLoad(contactAddResponse.getContact().chatId, 0, 25, session1_token);

        AccountContactsResponse accountContactsResponse = rest.accountContacts(session2_token);
        // accept contact
        rest.contactAccept(session1_qrcode, session2_token);
    }

    @SmallTest
    public void testContactAcceptNegative() throws Throwable {
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
        //assertTrue(contactAddResponse != null);

        // send message
        //rest.chatMessage(contactAddResponse.getContact().chatId, "Message 1 ", session1_token);
        //rest.chatMessage(contactAddResponse.getContact().chatId, "Message 2 ", session1_token);

        // get message
        rest.chatLoad(contactAddResponse.getContact().chatId, 0, 25, session1_token);

        AccountContactsResponse accountContactsResponse = rest.accountContacts(session2_token);

        // accept contact
        try {
            rest.contactAccept(session1_qrcode, session1_token);
        } catch (RestError e) {
            assertEquals(e.getErrorType(), RestErrorType.REST_UNKNOWN_ERROR);
        }

    }

    @SmallTest
    public void testContactReject() throws Throwable {
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
        //assertTrue(contactAddResponse != null);

        // send message
        //rest.chatMessage(contactAddResponse.getContact().chatId, "Message 1 ", session1_token);
        //rest.chatMessage(contactAddResponse.getContact().chatId, "Message 2 ", session1_token);

        // get message
        rest.chatLoad(contactAddResponse.getContact().chatId, 0, 25, session1_token);

        // reject contact
        rest.contactReject(session1_qrcode, session2_token);

        //  AccountContactsResponse accountContactsResponse = rest.accountContacts(session2_token);
    }


    @SmallTest
    public void testContactRejectNegative() throws Throwable {
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
        //assertTrue(contactAddResponse != null);

        // send message
        //rest.chatMessage(contactAddResponse.getContact().chatId, "Message 1 ", session1_token);
        //rest.chatMessage(contactAddResponse.getContact().chatId, "Message 2 ", session1_token);

        // get message
        rest.chatLoad(contactAddResponse.getContact().chatId, 0, 25, session1_token);

        // reject contact
        try {
            rest.contactReject(session1_qrcode, session1_token);
        } catch (RestError e) {
            assertEquals(e.getErrorType(), RestErrorType.REST_UNKNOWN_ERROR);
        }

        //  AccountContactsResponse accountContactsResponse = rest.accountContacts(session2_token);
    }


    @SmallTest
    public void testContactBlock() throws Throwable {
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
        rest.chatLoad(contactAddResponse.getContact().chatId, 0, 25, session1_token);
        // block contact
        rest.contactReject(session1_qrcode, session2_token);
    }

    @SmallTest
    public void testContactBlockNegative() throws Throwable {
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
        rest.chatLoad(contactAddResponse.getContact().chatId, 0, 25, session1_token);
        // block contact

        //rest.contactBlock(session1_qrcode, session1_token);

        try {
            rest.contactBlock(session1_qrcode, session1_token);
        } catch (RestError e) {
            assertEquals(e.getErrorType(), RestErrorType.REST_UNKNOWN_ERROR);
        }
    }


    @Suppress
    public void testAndroidTestCaseSetupProperly() {
        super.testAndroidTestCaseSetupProperly();
    }


}



