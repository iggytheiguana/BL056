package biz.softtechnics.qodeme.tests.model.connection.rest;

import android.test.suitebuilder.annotation.Suppress;

/**
 * Created with IntelliJ IDEA.
 * User: Alex Vegner
 * Date: 8/28/13
 * Time: 12:35 PM
 */
@Suppress
public class PushAMassageTest extends BaseTestCase {

    private static final String TAG = PushAMassageTest.class.getSimpleName();
/*

    @Suppress
    @LargeTest
    public void testUpdateGcmToken() throws Throwable {
        final RestClient restClient = RestHelper.getInstance();

        final String u1Password = "password_mdp5_1";
        final String u1PasswordMd5 = RestUtils.getMd5(u1Password);
        final CountDownLatch signal = new CountDownLatch(1);
        restClient.accountCreate(u1PasswordMd5, new RestListener<AccountCreateResponse>(){
            @Override
            public void onResponse (AccountCreateResponse response){
                RestTestUtils.logPassed(getThisRoot(), "accountCreate");
                final String u1QrCode = response.getQrcode();
                restClient.accountLogin(u1QrCode, u1PasswordMd5, new RestListener<AccountLoginResponse>() {

                    @Override
                    public void onResponse(AccountLoginResponse response) {
                        RestTestUtils.logPassed(getThisRoot(), "accountLogin");

                        new AsyncTask<Void, Void, String>() {

                            @Override
                            protected String doInBackground(Void... params) {
                                Log.e(TAG, "getting_registration_id");
                                try {
                                    GoogleCloudMessaging gcm = GoogleCloudMessaging.getInstance(getContext());
                                    return gcm.register(ApplicationConstants.GCM_SENDER_ID);
                                } catch (final IOException ex) {
                                    Log.e(TAG, ex.getMessage());
                                    return null;
                                }
                            }

                            @Override
                            protected void onPostExecute(String gcmToken) {
                                Log.i(TAG, (String)(gcmToken == null ? "null" : gcmToken));
                                if (gcmToken == null)
                                    assertFalse(true);
                                else {
                                    restClient.registerToken(gcmToken, new RestListener<VoidResponse>() {

                                        @Override
                                        public void onResponse(VoidResponse response) {
                                            RestTestUtils.logPassed(getThisRoot(), "registerToken");
                                            // Create second account


                                            final String u2Password = "password_mdp5_1";
                                            final String u2PasswordMd5 = RestUtils.getMd5(u2Password);

                                            restClient.accountCreate(u2PasswordMd5, new RestListener<AccountCreateResponse>(){
                                                @Override
                                                public void onResponse (AccountCreateResponse response){
                                                    RestTestUtils.logPassed(getThisRoot(), "accountCreateU2");
                                                    restClient.accountLogin(response.getQrcode(), u2PasswordMd5, new RestListener<AccountLoginResponse>() {

                                                        @Override
                                                        public void onResponse(AccountLoginResponse response) {
                                                            RestTestUtils.logPassed(getThisRoot(), "accountLoginU2");
                                                            //restClient.contactAdd(u1QrCode, new RestListener<ContactAddResponse>(){

                                                                */
/*@Override
                                                                public void onResponse(ContactAddResponse response) {
                                                                    RestTestUtils.logPassed(getThisRoot(), "U2_AddContact_U1");
                                                                    restClient.chatMessage(response.getContact().chatId, "My message", new RestListener<VoidResponse>(){

                                                                        @Override
                                                                        public void onResponse(VoidResponse response) {
                                                                            RestTestUtils.logPassed(getThisRoot(), "U2_chatMessage_U1");
                                                                            assertTrue(true);
                                                                            signal.countDown();
                                                                        }

                                                                        @Override
                                                                        public void onServiceError(RestError error) {
                                                                            defaultServiceError(error, signal);
                                                                        }

                                                                        @Override
                                                                        public void onNetworkError(VolleyError error) {
                                                                            defaultNetworkError(error, signal);
                                                                        }
                                                                    });
                                                                }*//*


                                                                @Override
                                                                public void onServiceError(RestError error) {
                                                                    defaultServiceError(error, signal);
                                                                }

                                                                @Override
                                                                public void onNetworkError(VolleyError error) {
                                                                    defaultNetworkError(error, signal);
                                                                }
                                                            });

                                                        }

                                                        @Override
                                                        public void onServiceError(RestError error) {
                                                            defaultServiceError(error, signal);
                                                        }

                                                        @Override
                                                        public void onNetworkError(VolleyError error) {
                                                            defaultNetworkError(error, signal);

                                                        }
                                                    });
                                                }

                                                @Override
                                                public void onServiceError(RestError error) {
                                                    defaultServiceError(error, signal);
                                                }

                                                @Override
                                                public void onNetworkError(VolleyError error) {
                                                    defaultNetworkError(error, signal);

                                                }
                                            });
                                        }

                                        @Override
                                        public void onServiceError(RestError error) {
                                            defaultServiceError(error, signal);
                                        }

                                        @Override
                                        public void onNetworkError(VolleyError error) {
                                            defaultNetworkError(error, signal);
                                        }

                                    });
                                }
                            }
                        }.execute(null, null, null);
                    }

                    @Override
                    public void onServiceError (RestError error){
                        defaultServiceError(error, signal);
                    }

                    @Override
                    public void onNetworkError (VolleyError error){
                        defaultNetworkError(error, signal);
                    }
                }

                );
            }

            @Override
            public void onServiceError(RestError error) {
                defaultServiceError(error, signal);
            }

            @Override
            public void onNetworkError(VolleyError error) {
                defaultNetworkError(error, signal);
            }
        });

        signal.await();
    }
*/

}
