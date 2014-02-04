package biz.softtechnics.qodeme.tests.model.connection.rest.cases;

import android.test.suitebuilder.annotation.MediumTest;
import android.test.suitebuilder.annotation.Suppress;

import biz.softtechnics.qodeme.core.connection.rest.responses.AccountCreateResponse;
import biz.softtechnics.qodeme.core.connection.rest.responses.AccountLoginResponse;
import biz.softtechnics.qodeme.core.connection.rest.responses.ContactAddResponse;
import biz.softtechnics.qodeme.tests.model.connection.rest.BaseTestCase;
import biz.softtechnics.qodeme.tests.model.connection.rest.utils.RestSyncTestHelper;
import biz.softtechnics.qodeme.utils.RandomColorGenerator;
import biz.softtechnics.qodeme.utils.RestUtils;

import static biz.softtechnics.qodeme.utils.LogUtils.makeLogTag;

/**
 * Created by Alex on 12/1/13.
 */
public class ContactAddTest extends BaseTestCase {

    private static final String TAG = makeLogTag(ContactAddTest.class);

    @MediumTest
    public void testAddContact() throws Throwable {

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

        String session3_password = RestUtils.getMd5("password_mdp5_3");
        String session3_qrcode = null;
        String session3_token = null;

        {
            // create account
            AccountCreateResponse accountCreateResponse = rest.accountCreate(session3_password);
            session3_qrcode = accountCreateResponse.getQrcode();

            // login
            AccountLoginResponse accountLoginResponse = rest.accountLogin(session3_qrcode, session3_password);
            session3_token = accountLoginResponse.getRestToken();
        }
        {
            ContactAddResponse contactAddResponse = rest.contactAdd(session2_qrcode, "message", "public_name", "location", session1_token);
        }
        {
            ContactAddResponse contactAddResponse = rest.contactAdd(session3_qrcode, "message", "public_name", "location", session1_token);
            rest.chatSetInfo(contactAddResponse.getContact().chatId, "User", RandomColorGenerator.getInstance().nextColor(), null, session1_token);
        }
    }


    @MediumTest
    public void testAddContactWithSettings() throws Throwable {

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

        String session3_password = RestUtils.getMd5("password_mdp5_3");
        String session3_qrcode = null;
        String session3_token = null;

        {
            // create account
            AccountCreateResponse accountCreateResponse = rest.accountCreate(session3_password);
            session3_qrcode = accountCreateResponse.getQrcode();

            // login
            AccountLoginResponse accountLoginResponse = rest.accountLogin(session3_qrcode, session3_password);
            session3_token = accountLoginResponse.getRestToken();
        }

        rest.setUserSettings("Message", true, "public name", true, true, "sd", true, session1_token);
        rest.setUserSettings("Message", true, "public name", true, true, "sd", true, session2_token);
        rest.setUserSettings("Message", true, "public name", true, true, "sd", true, session3_token);

        //rest.setUserSettings("Message", true, "public name", true, true, "dsf", true, session1_token);
        //rest.setUserSettings("Message", true, "public name", true, true, "dsf", true, session2_token);


        {
            ContactAddResponse contactAddResponse = rest.contactAdd(session2_qrcode, "", "", "", session1_token);
        }
        {
            ContactAddResponse contactAddResponse = rest.contactAdd(session3_qrcode, "", "", "", session1_token);
            rest.chatSetInfo(contactAddResponse.getContact().chatId, "User", RandomColorGenerator.getInstance().nextColor(), null, session1_token);
        }
    }


    @Suppress
    public void testAndroidTestCaseSetupProperly() {
        super.testAndroidTestCaseSetupProperly();
    }


}
