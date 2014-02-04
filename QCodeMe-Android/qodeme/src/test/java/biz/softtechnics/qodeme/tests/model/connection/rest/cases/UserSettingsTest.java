package biz.softtechnics.qodeme.tests.model.connection.rest.cases;

import android.test.suitebuilder.annotation.SmallTest;
import android.test.suitebuilder.annotation.Suppress;

import biz.softtechnics.qodeme.core.connection.rest.responses.AccountCreateResponse;
import biz.softtechnics.qodeme.core.connection.rest.responses.AccountLoginResponse;
import biz.softtechnics.qodeme.core.connection.rest.responses.UserSettingsResponse;
import biz.softtechnics.qodeme.tests.model.connection.rest.BaseTestCase;
import biz.softtechnics.qodeme.tests.model.connection.rest.utils.RestSyncTestHelper;
import biz.softtechnics.qodeme.utils.RestUtils;

import static biz.softtechnics.qodeme.utils.LogUtils.LOGI;
import static biz.softtechnics.qodeme.utils.LogUtils.makeLogTag;

/**
 * Created by Alex on 12/4/13.
 */
public class UserSettingsTest extends BaseTestCase {


    private static final String TAG = makeLogTag(AccountContactsTest.class);

    @SmallTest
    public void testUserSettings() throws Throwable {
        LOGI(TAG,"testUserSettings");
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

        UserSettingsResponse userSettingsResponse = rest.getUserSettings(session1_token);

        rest.setUserSettings("Message", true, "public name", true, true, "location", true, session1_token);

        UserSettingsResponse userSettingsResponseAfterChanges = rest.getUserSettings(session1_token);

/*
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

        // accept contact
        rest.contactAccept(session1_qrcode, session2_token);

        // send message user 1 to user 2
        long unixTimeStamp = System.currentTimeMillis()/1000L;
        rest.chatMessage(contactAddResponse.getContact().chatId, "Message 1 ", unixTimeStamp, session1_token);
        rest.chatMessage(contactAddResponse.getContact().chatId, "Message 2 ", unixTimeStamp, session2_token);

        // get message
        rest.chatLoad(contactAddResponse.getContact().chatId, 0, 25, session1_token);
        rest.chatLoad(contactAddResponse.getContact().chatId, 0, 25, session2_token);


        *//*AccountContactsResponse accountContactsResponse = rest.accountContacts(session2_token);
        // accept contact
        rest.contactAccept(session1_qrcode, session2_token);*//*

        *//* }
        {
            AccountContactsResponse accountContactsResponse = rest.accountContacts(session2_token);
        }*/

    }


    @Suppress
    public void testAndroidTestCaseSetupProperly() {
        super.testAndroidTestCaseSetupProperly();
    }

}
