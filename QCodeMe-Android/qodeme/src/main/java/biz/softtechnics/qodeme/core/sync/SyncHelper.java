/*
 * Copyright 2012 Google Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package biz.softtechnics.qodeme.core.sync;

import android.accounts.Account;
import android.content.ContentProviderOperation;
import android.content.ContentResolver;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.SyncResult;
import android.database.Cursor;
import android.os.Bundle;
import android.preference.PreferenceManager;

import com.bugsense.trace.BugSenseHandler;
import com.google.common.collect.Lists;

import org.json.JSONException;

import java.io.IOException;
import java.util.ArrayList;
import java.util.concurrent.ExecutionException;

import biz.softtechnics.qodeme.core.accounts.GenericAccountService;
import biz.softtechnics.qodeme.core.data.preference.QodemePreferences;
import biz.softtechnics.qodeme.core.io.RestSyncHelper;
import biz.softtechnics.qodeme.core.io.hendler.AccountContactsHandler;
import biz.softtechnics.qodeme.core.io.hendler.ChatLoadHandler;
import biz.softtechnics.qodeme.core.io.hendler.ChatMessageHandler;
import biz.softtechnics.qodeme.core.io.hendler.ContactAcceptHandler;
import biz.softtechnics.qodeme.core.io.hendler.ContactAddHandler;
import biz.softtechnics.qodeme.core.io.hendler.ContactBlockHandler;
import biz.softtechnics.qodeme.core.io.hendler.ContactRejectHandler;
import biz.softtechnics.qodeme.core.io.hendler.ContactSetInfoHandler;
import biz.softtechnics.qodeme.core.io.hendler.MessageReadHandler;
import biz.softtechnics.qodeme.core.io.model.Contact;
import biz.softtechnics.qodeme.core.io.responses.AccountContactsResponse;
import biz.softtechnics.qodeme.core.io.responses.ChatLoadResponse;
import biz.softtechnics.qodeme.core.io.responses.ChatMessageResponse;
import biz.softtechnics.qodeme.core.io.responses.ContactAddResponse;
import biz.softtechnics.qodeme.core.io.responses.UserSettingsResponse;
import biz.softtechnics.qodeme.core.io.responses.VoidResponse;
import biz.softtechnics.qodeme.core.io.utils.RestError;
import biz.softtechnics.qodeme.core.provider.QodemeContract;
import biz.softtechnics.qodeme.utils.Converter;

import static biz.softtechnics.qodeme.utils.LogUtils.LOGE;
import static biz.softtechnics.qodeme.utils.LogUtils.LOGI;
import static biz.softtechnics.qodeme.utils.LogUtils.makeLogTag;


/**
 * A helper class for dealing with sync and other remote persistence operations.
 * All operations occur on the thread they're called from, so it's best to wrap
 * calls in an {@link android.os.AsyncTask}, or better yet, a
 * {@link android.app.Service}.
 */
public class SyncHelper {
    private static final String TAG = makeLogTag(SyncHelper.class);

    public static final String SYNC_EXTRAS_AFTER_LOGIN = "sync_extras_after_login";


    public static final int FLAG_SYNC_LOCAL = 0x1;
    public static final int FLAG_SYNC_REMOTE = 0x2;

    private static final int LOCAL_VERSION_CURRENT = 25;
    private static final String LOCAL_MAPVERSION_CURRENT = "\"vlh7Ig\"";

    private Context mContext;

    public SyncHelper(Context context) {
        mContext = context;
    }

    public static void requestManualSync() {
        Account account = GenericAccountService.GetAccount();
        Bundle b = new Bundle();
        b.putBoolean(ContentResolver.SYNC_EXTRAS_MANUAL, true);
        ContentResolver.requestSync(
                account,
                QodemeContract.CONTENT_AUTHORITY, b);
    }

    public static void requestAfterLoginSync() {
        Account account = GenericAccountService.GetAccount();
        Bundle b = new Bundle();
        b.putBoolean(ContentResolver.SYNC_EXTRAS_MANUAL, true);
        b.putBoolean(SYNC_EXTRAS_AFTER_LOGIN, true);
        ContentResolver.requestSync(account, QodemeContract.CONTENT_AUTHORITY, b);
    }

    /**
     * Loads conference information (sessions, rooms, tracks, speakers, etc.)
     * from a local static cache data and then syncs down data from the
     * Conference API.
     *
     * @param syncResult Optional {@link android.content.SyncResult} object to populate.
     * @throws java.io.IOException
     */
    public void performSync(SyncResult syncResult, int flags) throws IOException {

        final SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(mContext);
        final int localVersion = prefs.getInt("local_data_version", 0);

        // Bulk of sync work, performed by executing several fetches from
        // local and online sources.
        final ContentResolver resolver = mContext.getContentResolver();
        ArrayList<ContentProviderOperation> batch = new ArrayList<ContentProviderOperation>();

        LOGI(TAG, "Performing sync");
    }

    public static void doInitialSync(Context context, ContentResolver contentResolver){
        LOGI(TAG, "Initial sync");
        ArrayList<ContentProviderOperation> batch = Lists.newArrayList();
        RestSyncHelper rest = RestSyncHelper.getInstance(context);
        contentResolver.delete(QodemeContract.BASE_CONTENT_URI, null, null);
        try {
            AccountContactsResponse accountContactsResponse = rest.accountContacts();
            AccountContactsHandler accountContactHandler = new AccountContactsHandler(context);
            accountContactHandler.parseAndApply(accountContactsResponse);
            UserSettingsResponse userSettingsResponse = rest.getUserSettings();
            if (userSettingsResponse.getSettings() != null){
                QodemePreferences.getInstance().setUserSettingsResponse(userSettingsResponse);
            } else {
                QodemePreferences pref = QodemePreferences.getInstance();
                pref.initUserSettings();
                rest.setUserSettings();
                pref.setUserSettingsUpToDate(true);
            }
            loadAllChats(context, batch, rest, accountContactsResponse);
            QodemeContract.applyBatch(context, batch);
        } catch (RestError e) {
            LOGE(TAG, e.toString(context), e);
        } catch (InterruptedException e) {
            LOGE(TAG, "doInitialSync", e);
        } catch (ExecutionException e) {
            LOGE(TAG, "doInitialSync", e);
        } catch (JSONException e) {
            LOGE(TAG, "doInitialSync", e);
        }
    }

    private static void loadAllChats(Context context, ArrayList<ContentProviderOperation> batch, RestSyncHelper rest, AccountContactsResponse accountContactsResponse) throws InterruptedException, ExecutionException, JSONException, RestError {
        // For One2One chats
        for (Contact c: accountContactsResponse.getContactList()){
            long chatId = c.chatId;
            ChatLoadResponse chatLoadResponse = rest.chatLoad(chatId, 0, 1000);
            new ChatLoadHandler(context).parse(chatLoadResponse, batch);
        }
        // For group chats
        // TODO
    }

    public static void doContactsSync(Context context, ContentResolver contentResolver) {
        LOGI(TAG, "Contacts sync");
        ArrayList<ContentProviderOperation> batch = Lists.newArrayList();
        RestSyncHelper rest = RestSyncHelper.getInstance(context);
        Cursor cursor = contentResolver.query(QodemeContract.Contacts.CONTENT_URI, QodemeContract.Contacts.ContactQuery.PROJECTION, QodemeContract.Contacts.UPDATED + " != " + QodemeContract.Contacts.Sync.DONE, null, null);
        if (cursor.moveToFirst())
            do {
                int update = cursor.getInt(QodemeContract.Contacts.ContactQuery.UPDATED);
                long id = cursor.getLong(QodemeContract.Contacts.ContactQuery._ID);
                String qrCode = cursor.getString(QodemeContract.Contacts.ContactQuery.CONTACT_QRCODE);
                if ((update & QodemeContract.Contacts.Sync.NEW)  == QodemeContract.Contacts.Sync.NEW){
                    try {
                        String publicName = cursor.getString(QodemeContract.Contacts.ContactQuery.CONTACT_PUBLIC_NAME);
                        String location = cursor.getString(QodemeContract.Contacts.ContactQuery.CONTACT_LOCATION);
                        String message = cursor.getString(QodemeContract.Contacts.ContactQuery.CONTACT_MESSAGE);
                        ContactAddResponse contactAddResponse = rest.contactAdd(qrCode, publicName, message, location);
                        new ContactAddHandler(context, id).parse(contactAddResponse, batch);
                        long chatId = contactAddResponse.getContact().chatId;
                        String title = cursor.getString(QodemeContract.Contacts.ContactQuery.CONTACT_TITLE);
                        int color = cursor.getInt(QodemeContract.Contacts.ContactQuery.CONTACT_COLOR);
                        VoidResponse response = rest.chatSetInfo(chatId, title, color, null);
                        new ContactSetInfoHandler(context, id).parse(response, batch);
                    } catch (RestError e) {
                        BugSenseHandler.sendExceptionMessage(TAG, "catch exception", e);
                        LOGE(TAG, e.toString(context), e);
                    } catch (InterruptedException e) {
                        LOGE(TAG, "doInitialSync", e);
                    } catch (ExecutionException e) {
                        LOGE(TAG, "doInitialSync", e);
                    } catch (JSONException e) {
                        LOGE(TAG, "doInitialSync", e);
                    }
                } else {
                    if ((update & QodemeContract.Contacts.Sync.STATE_UPDATED) == QodemeContract.Contacts.Sync.STATE_UPDATED){
                        try {
                            int state = cursor.getInt(QodemeContract.Contacts.ContactQuery.CONTACT_STATE);
                            switch (state){
                                case QodemeContract.Contacts.State.APPRUVED:{
                                    VoidResponse response = rest.contactAccept(qrCode);
                                    new ContactAcceptHandler(context, id).parse(response, batch);
                                    break;
                                }
                                case QodemeContract.Contacts.State.REJECTED:{
                                    VoidResponse response = rest.contactReject(qrCode);
                                    new ContactRejectHandler(context, id).parse(response, batch);
                                    update = QodemeContract.Contacts.Sync.DONE;
                                    break;
                                }
                                case QodemeContract.Contacts.State.BLOCKED_BY:{
                                    VoidResponse response = rest.contactBlock(qrCode);
                                    new ContactBlockHandler(context, id).parse(response, batch);
                                    update = QodemeContract.Contacts.Sync.DONE;
                                    break;
                                }
                            }
                        } catch (RestError e) {
                            BugSenseHandler.sendExceptionMessage(TAG, "catch exception", e);
                            LOGE(TAG, e.toString(context), e);
                        } catch (InterruptedException e) {
                            LOGE(TAG, "doInitialSync", e);
                        } catch (ExecutionException e) {
                            LOGE(TAG, "doInitialSync", e);
                        } catch (JSONException e) {
                            LOGE(TAG, "doInitialSync", e);
                        }
                    }
                    if ((update & QodemeContract.Contacts.Sync.UPDATED) == QodemeContract.Contacts.Sync.UPDATED){
                        //update & (QodemeContract.Contacts.Sync.UPDATED ^ 0xff)
                        long chatId = cursor.getLong(QodemeContract.Contacts.ContactQuery.CONTACT_CHAT_ID);
                        String title = cursor.getString(QodemeContract.Contacts.ContactQuery.CONTACT_TITLE);
                        int color = cursor.getInt(QodemeContract.Contacts.ContactQuery.CONTACT_COLOR);
                        try {
                            VoidResponse response = rest.chatSetInfo(chatId, title, color, null);
                            new ContactSetInfoHandler(context, id).parse(response, batch);
                        } catch (RestError e) {
                            BugSenseHandler.sendExceptionMessage(TAG, "catch exception", e);
                            LOGE(TAG, e.toString(context), e);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        } catch (ExecutionException e) {
                            e.printStackTrace();
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                }


            } while (cursor.moveToNext());

        QodemeContract.applyBatch(context, batch);
    }

    public static void doSettingsSync(Context context) {
        LOGI(TAG, "User settings sync");
        QodemePreferences pref = QodemePreferences.getInstance();
        RestSyncHelper rest = RestSyncHelper.getInstance(context);
        if (!QodemePreferences.getInstance().isUserSettingsUpToDate()){
            try {
                rest.setUserSettings();
                pref.setUserSettingsUpToDate(true);
            } catch (RestError e) {
                BugSenseHandler.sendExceptionMessage(TAG, "catch exception", e);
                LOGE(TAG, e.toString(context), e);
            } catch (InterruptedException e) {
                e.printStackTrace();
            } catch (ExecutionException e) {
                e.printStackTrace();
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }

    public static void doMessageSync(Context context, ContentResolver contentResolver) {
        RestSyncHelper rest = RestSyncHelper.getInstance(context);
        Cursor cursor = contentResolver.query(QodemeContract.Messages.CONTENT_URI, QodemeContract.Messages.Query.PROJECTION, QodemeContract.Messages.UPDATED + " != " + QodemeContract.Messages.Sync.DONE, null, null);
        if (cursor.moveToFirst()) do {
            long id = cursor.getLong(QodemeContract.Messages.Query._ID);
            int state = cursor.getInt(QodemeContract.Messages.Query.MESSAGE_STATE);
            int update = cursor.getInt(QodemeContract.Messages.Query.UPDATED);
            if (update == (update & QodemeContract.Sync.NEW) &&  state == QodemeContract.Messages.State.LOCAL){
                try {
                    long chatId = cursor.getLong(QodemeContract.Messages.Query.MESSAGE_CHAT_ID);
                    String message = cursor.getString(QodemeContract.Messages.Query.MESSAGE_TEXT);
                    long created = (long)(Converter.getCrurentTimeFromTimestamp(cursor.getString(QodemeContract.Messages.Query.MESSAGE_CREATED)) / 1E3);
                    ChatMessageResponse response = rest.chatMessage(chatId, message, created);
                    new ChatMessageHandler(context, id).parseAndApply(response);
                } catch (RestError e) {
                    BugSenseHandler.sendExceptionMessage(TAG, "catch exception", e);
                    LOGE(TAG, e.toString(context), e);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                } catch (ExecutionException e) {
                    e.printStackTrace();
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            } else if (update == (update & QodemeContract.Sync.UPDATED) &&  state == QodemeContract.Messages.State.READ_LOCAL){
                try {
                    long messageId = cursor.getLong(QodemeContract.Messages.Query.MESSAGE_ID);
                    VoidResponse response = rest.messageRead(messageId);
                    new MessageReadHandler(context, id).parseAndApply(response);
                } catch (RestError e) {
                    BugSenseHandler.sendExceptionMessage(TAG, "catch exception", e);
                    LOGE(TAG, e.toString(context), e);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                } catch (ExecutionException e) {
                    e.printStackTrace();
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        } while (cursor.moveToNext());
    }
}