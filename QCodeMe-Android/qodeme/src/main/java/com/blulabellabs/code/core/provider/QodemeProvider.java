package com.blulabellabs.code.core.provider;

import android.content.ContentProvider;
import android.content.ContentProviderOperation;
import android.content.ContentProviderResult;
import android.content.ContentValues;
import android.content.Context;
import android.content.OperationApplicationException;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;

import java.util.ArrayList;
import java.util.Arrays;

import com.blulabellabs.code.core.data.preference.QodemePreferences;
import com.blulabellabs.code.utils.SelectionBuilder;


import static com.blulabellabs.code.core.provider.QodemeContract.Chats;
import static com.blulabellabs.code.core.provider.QodemeContract.Contacts;
import static com.blulabellabs.code.core.provider.QodemeContract.Messages;
import static com.blulabellabs.code.core.provider.QodemeDatabase.Tables;
import static com.blulabellabs.code.utils.LogUtils.LOGV;
import static com.blulabellabs.code.utils.LogUtils.makeLogTag;

/**
 * Created by Alex on 10/31/13.
 */
public class QodemeProvider extends ContentProvider{


    private static final String TAG = makeLogTag(QodemeProvider.class);

    private QodemeDatabase mOpenHelper;

    private static final UriMatcher sUriMatcher = buildUriMatcher();

    private static final int CONTACTS = 100;
    private static final int CONTACTS_ID = 101;
    private static final int CONTACTS_SEARCH = 102;
    private static final int CONTACTS_QR_CODE = 103;

    private static final int CHATS = 200;
    private static final int CHATS_ID  = 201;
    private static final int CHATS_QR_CODE = 202;
    private static final int CHATS_SEARCH  = 203;
    private static final int CHATS_SETTING_HEIGHT = 204;

    private static final int MESSAGES = 300;
    private static final int MESSAGE_ID  = 301;

    /**
     * Build and return a {@link android.content.UriMatcher} that catches all {@link android.net.Uri}
     * variations supported by this {@link android.content.ContentProvider}.
     */
    private static UriMatcher buildUriMatcher() {
        final UriMatcher matcher = new UriMatcher(UriMatcher.NO_MATCH);
        final String authority = QodemeContract.CONTENT_AUTHORITY;

        matcher.addURI(authority, "contacts", CONTACTS);
        matcher.addURI(authority, "contacts/*", CONTACTS_ID);
        matcher.addURI(authority, "contacts/search/*", CONTACTS_SEARCH);
        matcher.addURI(authority, "contacts/qrcode/*", CONTACTS_QR_CODE);

        matcher.addURI(authority, "chats", CHATS);
        matcher.addURI(authority, "chats/*", CHATS_ID);
        matcher.addURI(authority, "chats/qrcode/*", CHATS_QR_CODE);
        matcher.addURI(authority, "chats/*/height", CHATS_SETTING_HEIGHT);
        matcher.addURI(authority, "chats/search/*", CHATS_SEARCH);

        matcher.addURI(authority, "messages", MESSAGES);
        matcher.addURI(authority, "messaged/*", MESSAGE_ID);

        return matcher;
    }

    @Override
    public boolean onCreate() {
        if (QodemePreferences.getInstance() == null)
            QodemePreferences.initialize(getContext().getApplicationContext());
        mOpenHelper = new QodemeDatabase(getContext());
        return true;
    }

    private void deleteDatabase() {
        // TODO: wait for content provider operations to finish, then tear down
        mOpenHelper.close();
        Context context = getContext();
        QodemeDatabase.deleteDatabase(context);
        mOpenHelper = new QodemeDatabase(getContext());
    }

    /** {@inheritDoc} */
    @Override
    public String getType(Uri uri) {
        final int match = sUriMatcher.match(uri);
        switch (match) {
            case CONTACTS:
                return Contacts.CONTENT_TYPE;
            case CONTACTS_ID:
                return Contacts.CONTENT_ITEM_TYPE;
            case CONTACTS_QR_CODE:
                return Contacts.CONTENT_ITEM_TYPE;
            case CONTACTS_SEARCH:
                return Contacts.CONTENT_TYPE;
            case CHATS:
                return Chats.CONTENT_TYPE;
            case CHATS_ID:
                return Chats.CONTENT_ITEM_TYPE;
            case CHATS_QR_CODE:
                return Chats.CONTENT_ITEM_TYPE;
            case CHATS_SEARCH:
                return Chats.CONTENT_TYPE;
            case CHATS_SETTING_HEIGHT:
                return Chats.CONTENT_ITEM_SETTINGS_TYPE;
            case MESSAGES:
                return Messages.CONTENT_TYPE;
            case MESSAGE_ID:
                return Messages.CONTENT_ITEM_TYPE;
            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }
    }

    /** {@inheritDoc} */
    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs,
                        String sortOrder) {
        LOGV(TAG, "query(uri=" + uri + ", proj=" + Arrays.toString(projection) + ")");
        final SQLiteDatabase db = mOpenHelper.getReadableDatabase();

        final int match = sUriMatcher.match(uri);
        switch (match) {
            default: {
                // Most cases are handled with simple SelectionBuilder
                final SelectionBuilder builder = buildSimpleSelection(uri);
                return builder.where(selection, selectionArgs).query(db, projection, sortOrder);
            }
        }
    }

    /** {@inheritDoc} */
    @Override
    public Uri insert(Uri uri, ContentValues values) {
        LOGV(TAG, "insert(uri=" + uri + ", values=" + values.toString() + ")");
        final SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        final int match = sUriMatcher.match(uri);
        boolean syncToNetwork = !QodemeContract.hasCallerIsSyncAdapterParameter(uri);
        switch (match) {

            case CONTACTS: {
                db.insertOrThrow(Tables.CONTACTS, null, values);
                notifyChange(uri, syncToNetwork);
                return Contacts.buildContactUri(Contacts.CONTACT_ID);
            }
            case CHATS: {
                db.insertOrThrow(Tables.CHATS, null, values);
                notifyChange(uri, syncToNetwork);
                return Chats.buildChatUri(values.getAsString(Chats.CHAT_ID));
            }

            case MESSAGES: {
                long id = db.insertOrThrow(Tables.MESSAGES, null, values);
                notifyChange(uri, syncToNetwork);
                return Messages.buildMessageUri(""+id);//values.getAsString(Messages.MESSAGE_ID));
            }

            default: {
                throw new UnsupportedOperationException("Unknown uri: " + uri);
            }
        }
    }

    /** {@inheritDoc} */
    @Override
    public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        LOGV(TAG, "update(uri=" + uri + ", values=" + values.toString() + ")");
        final SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        final SelectionBuilder builder = buildSimpleSelection(uri);
        int retVal = builder.where(selection, selectionArgs).update(db, values);
        boolean syncToNetwork = !QodemeContract.hasCallerIsSyncAdapterParameter(uri);
        notifyChange(uri, syncToNetwork);
        return retVal;
    }

    /** {@inheritDoc} */
    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        LOGV(TAG, "delete(uri=" + uri + ")");
        if (uri == QodemeContract.BASE_CONTENT_URI) {
            // Handle whole database deletes (e.g. when signing out)
            clearAllTables();
            notifyChange(uri, false);
            return 1;
        }
        final SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        final SelectionBuilder builder = buildSimpleSelection(uri);
        int retVal = builder.where(selection, selectionArgs).delete(db);
        notifyChange(uri, !QodemeContract.hasCallerIsSyncAdapterParameter(uri));
        return retVal;
    }

    private void clearAllTables() {
        mOpenHelper.clearAllTables();
    }

    private void notifyChange(Uri uri, boolean syncToNetwork) {
        Context context = getContext();
        context.getContentResolver().notifyChange(uri, null, syncToNetwork);
    }

    /**
     * Apply the given set of {@link android.content.ContentProviderOperation}, executing inside
     * a {@link android.database.sqlite.SQLiteDatabase} transaction. All changes will be rolled back if
     * any single one fails.
     */
    @Override
    public ContentProviderResult[] applyBatch(ArrayList<ContentProviderOperation> operations)
            throws OperationApplicationException {
        final SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        db.beginTransaction();
        try {
            final int numOperations = operations.size();
            final ContentProviderResult[] results = new ContentProviderResult[numOperations];
            for (int i = 0; i < numOperations; i++) {
                results[i] = operations.get(i).apply(this, results, i);
            }
            db.setTransactionSuccessful();
            return results;
        } finally {
            db.endTransaction();
        }
    }

    /**
     * Build a simple {@link com.blulabellabs.code.utils.SelectionBuilder} to match the requested
     * {@link android.net.Uri}. This is usually enough to support {@link #insert},
     * {@link #update}, and {@link #delete} operations.
     */
    private SelectionBuilder buildSimpleSelection(Uri uri) {
        final SelectionBuilder builder = new SelectionBuilder();
        final int match = sUriMatcher.match(uri);
        switch (match) {
            case CONTACTS: {
                return builder.table(Tables.CONTACTS);
            }
            case CONTACTS_ID: {
                final String contactId = Contacts.getContactId(uri);
                return builder.table(Tables.CONTACTS)
                        .where(Contacts.CONTACT_ID + "=?", contactId);
            }
            case CHATS: {
                return builder.table(Tables.CHATS);
            }
            case CHATS_ID: {
                final String chatId = Chats.getChatId(uri);
                return builder.table(Tables.CHATS)
                        .where(Chats.CHAT_ID + "=?", chatId);
            }
            case MESSAGES: {
                return builder.table(Tables.MESSAGES);
            }
            case MESSAGE_ID: {
                final String messageId = Messages.getMessageId(uri);
                return builder.table(Tables.MESSAGES)
                        .where(Messages.MESSAGE_ID + "=?", messageId);
            }
            default: {
                throw new UnsupportedOperationException("Unknown uri for " + match + ": " + uri);
            }
        }
    }

}
