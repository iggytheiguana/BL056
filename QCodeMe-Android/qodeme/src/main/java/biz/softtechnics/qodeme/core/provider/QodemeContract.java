package biz.softtechnics.qodeme.core.provider;

import android.content.ContentProviderOperation;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.OperationApplicationException;
import android.net.Uri;
import android.os.RemoteException;
import android.provider.BaseColumns;
import android.provider.ContactsContract;
import android.text.TextUtils;

import java.util.ArrayList;

import biz.softtechnics.qodeme.core.data.preference.QodemePreferences;
import biz.softtechnics.qodeme.core.io.model.Contact;
import biz.softtechnics.qodeme.core.io.model.Message;
import biz.softtechnics.qodeme.utils.Converter;
import biz.softtechnics.qodeme.utils.LatLonCity;
import biz.softtechnics.qodeme.utils.RandomColorGenerator;

import static biz.softtechnics.qodeme.utils.LogUtils.LOGE;
import static biz.softtechnics.qodeme.utils.LogUtils.makeLogTag;

/**
 * Created by Alex on 10/12/13.
 */
public class QodemeContract {

    private static final String TAG = makeLogTag(QodemeContract.class);

    private QodemeContract(){}

    public interface Sync{
        /** Don't update it */
        int NEVER = -2;
        /** Uncnown update */
        int UNKNOWN = -1;
        /** Updated */
        int DONE = 0;
        /** NEW */
        int NEW = 1;
        /** NEW */
        int UPDATED = 2;
    }

    public interface SyncColumns {
        /** Last time this entry was updated or synchronized. */
        String UPDATED = "updated";
    }

    interface ContactsColumns {
        /** Unique contact ID */
        String CONTACT_ID = "contact_id";
        /** QR code of the contact */
        String CONTACT_QRCODE = "contact_qrcode";
        /** Name of the contact */
        String CONTACT_TITLE = "contact_title";
        /** Color of the contact */
        String CONTACT_COLOR = "contact_color";
        /** Reference to chat */
        String CONTACT_CHAT_ID = "contact_chat_id";
        /** State of the contact
         * Approved(0)/Invited(1)/Inviting(2)/Blocked(3)*/
        String CONTACT_STATE = "contact_state";
        /** Date time */
        String CONTACT_DATETIME = "datetime";
        /** Public name */
        String CONTACT_PUBLIC_NAME = "public_name";
        /** Location */
        String CONTACT_LOCATION = "location";
        /** Message */
        String CONTACT_MESSAGE = "message";
    }

    interface ChatColumns {
        /** Unique chat ID */
        String CHAT_ID = "chat_id";
        /** QR code of the chat */
        String CHAT_QRCODE = "chat_qrcode";
        /** Name of chat */
        String CHAT_TITLE = "chat_title";
        /** Color of chat */
        String CHAT_TAGS = "chat_tags";
        /** Color of chat */
        String CHAT_COLOR = "chat_color";
        /** State of chat
         * type=1 (private_group) или 2 (public_group)*/
        String CHAT_TYPE = "chat_type";
    }

    interface MessagesColumns {
        /** Unique message ID */
        String MESSAGE_ID = "message_id";
        /** Chat id of message */
        String MESSAGE_CHAT_ID = "message_chat_id";
        /** Message text */
        String MESSAGE_TEXT = "message_text";
        /** Message created */
        String MESSAGE_CREATED = "message_created";
        /** Message qr code */
        String MESSAGE_QRCODE = "message_qrcode";
        /** Message state */
        String MESSAGE_STATE = "message_state";
    }

    interface ChatSettingColumns {
        /** Unique chat ID */
        String CHAT_ID = "chat_id";
        /** Height of chat */
        String CHAT_HEIGHT = "chat_qrcode";
    }

    interface GlobalSettingColumns {

    }

    public static final String CONTENT_AUTHORITY = "biz.softtechnics.qodeme";

    public static final Uri BASE_CONTENT_URI = Uri.parse("content://" + CONTENT_AUTHORITY);

    private static final String PATH_CONTACTS = "contacts";
    private static final String PATH_CHATS = "chats";
    private static final String PATH_SETTING = "setting";
    private static final String PATH_HEIGHT = "height";
    private static final String PATH_QRCODE = "qrcode";
    private static final String PATH_SEARCH = "search";
    private static final String PATH_MESSAGES = "messages";

    /**
     * Contact entities
     */
    public static class Contacts implements ContactsColumns, BaseColumns, SyncColumns {

        public static final Uri CONTENT_URI =
                BASE_CONTENT_URI.buildUpon().appendPath(PATH_CONTACTS).build();

        /**
         * MIME type for lists of contacts.
         */
        public static final String CONTENT_TYPE =
                ContentResolver.CURSOR_DIR_BASE_TYPE + "/vnd.qodeme.contacts";
        /**
         * MIME type for individual contact.
         */
        public static final String CONTENT_ITEM_TYPE =
                ContentResolver.CURSOR_ITEM_BASE_TYPE + "/vnd.qodeme.contact";


        public interface State{
            int APPRUVED = 0;
            int INVITATION_SENT = 1;
            int INVITED = 2;
            int BLOCKED = 3;
            int REJECTED = 4;
            int BLOCKED_BY = 5;
        }

        public interface Sync extends QodemeContract.Sync {
            /** Status changed */
            int STATE_UPDATED = 4;
        }

        public static String DEFAULT_NAME = "User";

        /** Default "ORDER BY" clause. */
        public static final String DEFAULT_SORT = BaseColumns._ID + " ASC ";
        public static final String CONTACT_LIST_SORT = CONTACT_STATE + ", " + BaseColumns._ID + " ASC ";

        /** Build {@link android.net.Uri} to contact for given id. */
        public static Uri buildContactUri(String contactId) {
            return CONTENT_URI.buildUpon().appendPath(contactId).build();
        }

        /** Build {@link android.net.Uri} to contact for given Qr Code. */
        public static Uri buildContactQrCodeUri(String qrCode) {
            return CONTENT_URI.buildUpon().appendPath(PATH_QRCODE).appendPath(qrCode).build();
        }

        /** Build {@link android.net.Uri} to contacts for given search query. */
        public static Uri buildContactSearchUri(String query) {
            return CONTENT_URI.buildUpon().appendPath(PATH_SEARCH).appendPath(query).build();
        }

        /** Read {@link #CONTACT_ID} from {@link Contacts} {@link android.net.Uri}. */
        public static String getContactId(Uri uri) {
            return uri.getPathSegments().get(1);
        }

        public static ContentValues addNewContactValues(String qrCode) {
            ContentValues contentValues = new ContentValues();
            contentValues.put(SyncColumns.UPDATED, Sync.NEW | Sync.UPDATED);
            contentValues.put(CONTACT_STATE, State.INVITATION_SENT);
            contentValues.put(CONTACT_QRCODE, qrCode);
            contentValues.put(CONTACT_TITLE, DEFAULT_NAME);
            contentValues.put(CONTACT_COLOR, RandomColorGenerator.getInstance().nextColor());

            String publicName = "";
            if (QodemePreferences.getInstance().isPublicNameChecked())
                publicName = QodemePreferences.getInstance().getPublicName();
            contentValues.put(CONTACT_PUBLIC_NAME, publicName);

            String location = "";
            //if (QodemePreferences.getInstance().isSaveLocationDateChecked()){
            LatLonCity latLonCity = QodemePreferences.getInstance().getLastLocation();
            if (latLonCity != null && latLonCity.getCity() != null)
                location = latLonCity.getCity();
            //}
            contentValues.put(CONTACT_LOCATION, location);

            String message = "";
            if (QodemePreferences.getInstance().isMessageChecked())
                message = QodemePreferences.getInstance().getMessage();
            contentValues.put(CONTACT_MESSAGE, message);
            contentValues.put(CONTACT_DATETIME, Converter.getCurrentGtmTimestampString());
            return contentValues;
        }

        public static ContentValues addNewContactPushValues(Contact contact) {
            ContentValues contentValues = new ContentValues();
            contentValues.put(SyncColumns.UPDATED, Sync.UPDATED);
            contentValues.put(CONTACT_STATE, contact.state);
            contentValues.put(CONTACT_QRCODE, contact.qrCode);
            contentValues.put(CONTACT_TITLE, TextUtils.isEmpty(contact.publicName) ? "User" : contact.publicName);
            contentValues.put(CONTACT_COLOR, RandomColorGenerator.getInstance().nextColor());
            contentValues.put(CONTACT_CHAT_ID, contact.chatId);
            contentValues.put(CONTACT_DATETIME, contact.date);
            contentValues.put(CONTACT_ID, contact.contactId);
            contentValues.put(CONTACT_PUBLIC_NAME, contact.publicName);
            String location = "";
            LatLonCity latLonCity = QodemePreferences.getInstance().getLastLocation();
            if (latLonCity != null && latLonCity.getCity() != null)
                location = latLonCity.getCity();
            contentValues.put(CONTACT_LOCATION, location);
            return contentValues;
        }

        public static ContentValues updateContactInfoValues(String title, int color, int updated) {
            ContentValues contentValues = new ContentValues();
            contentValues.put(SyncColumns.UPDATED, updated | Sync.UPDATED);
            contentValues.put(CONTACT_TITLE, title);
            contentValues.put(CONTACT_COLOR, color);
            return contentValues;
        }

        public static ContentValues acceptContactValues(int updated) {
            ContentValues contentValues = new ContentValues();
            contentValues.put(CONTACT_STATE, State.APPRUVED);
            contentValues.put(SyncColumns.UPDATED, updated | Sync.STATE_UPDATED);
            return contentValues;
        }

        public static ContentValues acceptContactPushValues() {
            ContentValues contentValues = new ContentValues();
            contentValues.put(CONTACT_STATE, State.APPRUVED);
            return contentValues;
        }

        public static ContentValues rejectContactValues(int updated) {
            ContentValues contentValues = new ContentValues();
            contentValues.put(CONTACT_STATE, State.REJECTED);
            contentValues.put(SyncColumns.UPDATED, updated | Sync.STATE_UPDATED);
            return contentValues;
        }

        public static ContentValues blockContactValues(int updated) {
            ContentValues contentValues = new ContentValues();
            contentValues.put(CONTACT_STATE, State.BLOCKED_BY);
            contentValues.put(SyncColumns.UPDATED, updated | Sync.STATE_UPDATED);
            return contentValues;
        }

        public static ContentValues blockContactPushValues() {
            ContentValues contentValues = new ContentValues();
            contentValues.put(CONTACT_STATE, State.BLOCKED);
            return contentValues;
        }

        public interface ContactQuery {
            String[] PROJECTION = {
                    Contacts._ID,
                    Contacts.UPDATED,
                    Contacts.CONTACT_ID,
                    Contacts.CONTACT_TITLE,
                    Contacts.CONTACT_QRCODE,
                    Contacts.CONTACT_COLOR,
                    Contacts.CONTACT_CHAT_ID,
                    Contacts.CONTACT_STATE,
                    Contacts.CONTACT_PUBLIC_NAME,
                    Contacts.CONTACT_MESSAGE,
                    Contacts.CONTACT_LOCATION,
                    Contacts.CONTACT_DATETIME
            };

            int _ID = 0;
            int UPDATED = 1;
            int CONTACT_ID = 2;
            int CONTACT_TITLE = 3;
            int CONTACT_QRCODE = 4;
            int CONTACT_COLOR = 5;
            int CONTACT_CHAT_ID = 6;
            int CONTACT_STATE = 7;
            int CONTACT_PUBLIC_NAME = 8;
            int CONTACT_MESSAGE = 9;
            int CONTACT_LOCATION = 10;
            int CONTACT_DATETIME = 11;

        }
    }

    /**
     * Chat entities
     */
    public static class Chats implements ChatColumns, BaseColumns, SyncColumns {

        public static final Uri CONTENT_URI =
                BASE_CONTENT_URI.buildUpon().appendPath(PATH_CHATS).build();

        /**
         * MIME type for lists of chats.
         */
        public static final String CONTENT_TYPE =
                ContentResolver.CURSOR_DIR_BASE_TYPE + "/vnd.qodeme.chats";
        /**
         * MIME type for individual chat.
         */
        public static final String CONTENT_ITEM_TYPE =
                ContentResolver.CURSOR_ITEM_BASE_TYPE + "/vnd.qodeme.chat";

        /**
         * MIME type for individual chat settings.
         */
        public static final String CONTENT_ITEM_SETTINGS_TYPE =
                ContentResolver.CURSOR_ITEM_BASE_TYPE + "/vnd.qodeme.chat.settings";

        /** Default "ORDER BY" clause. */
        public static final String DEFAULT_SORT = BaseColumns._ID + " ASC ";

        /** Build {@link android.net.Uri} to chat for given id. */
        public static Uri buildChatUri(String chatId) {
            return CONTENT_URI.buildUpon().appendPath(chatId).build();
        }

        /** Build {@link android.net.Uri} to chat for given Qr Code. */
        public static Uri buildChatQrCodeUri(String qrCode) {
            return CONTENT_URI.buildUpon().appendPath(PATH_QRCODE).appendPath(qrCode).build();
        }

        /** Build {@link android.net.Uri} to chats for given search query. */
        public static Uri buildChatSearchUri(String query) {
            return CONTENT_URI.buildUpon().appendPath(PATH_SEARCH).appendPath(query).build();
        }

        /** Read {@link #CHAT_ID} from {@link Chats} {@link android.net.Uri}. */
        public static String getChatId(Uri uri) {
            return uri.getPathSegments().get(1);
        }
    }

    public static Uri addCallerIsSyncAdapterParameter(Uri uri) {
        return uri.buildUpon().appendQueryParameter(
                ContactsContract.CALLER_IS_SYNCADAPTER, "true").build();
    }

    public static boolean hasCallerIsSyncAdapterParameter(Uri uri) {
        return TextUtils.equals("true",
                uri.getQueryParameter(ContactsContract.CALLER_IS_SYNCADAPTER));
    }

    public static void applyBatch(Context context, ArrayList<ContentProviderOperation> batch) {
        if (batch.isEmpty())
            return;
        final ContentResolver resolver = context.getContentResolver();
        try {
            resolver.applyBatch(QodemeContract.CONTENT_AUTHORITY, batch);
        } catch (RemoteException e) {
            LOGE(TAG, "", e);
        } catch (OperationApplicationException e) {
            LOGE(TAG, "", e);
        }
    }


    /**
     * Contact entities
     */
    public static class Messages implements MessagesColumns, BaseColumns, SyncColumns {

        public static final Uri CONTENT_URI =
                BASE_CONTENT_URI.buildUpon().appendPath(PATH_MESSAGES).build();

        /**
         * MIME type for lists of contacts.
         */
        public static final String CONTENT_TYPE =
                ContentResolver.CURSOR_DIR_BASE_TYPE + "/vnd.qodeme.messages";
        /**
         * MIME type for individual contact.
         */
        public static final String CONTENT_ITEM_TYPE =
                ContentResolver.CURSOR_ITEM_BASE_TYPE + "/vnd.qodeme.message";


        public interface State{
            int LOCAL = 0;
            int SENT = 1;
            int READ = 2;
            int NOT_READ = 3; // After push: New message
            int READ_LOCAL = 4;
            int WAS_READ = 5;
        }

        public interface Sync extends QodemeContract.Sync {
            /** Status changed */
            int STATE_UPDATED = 4;
        }

        /** Default "ORDER BY" clause. */
        public static final String DEFAULT_SORT = BaseColumns._ID + " ASC ";

        /** Build {@link android.net.Uri} to contact for given id. */
        public static Uri buildMessageUri(String messageId) {
            return CONTENT_URI.buildUpon().appendPath(messageId).build();
        }

       /* *//** Build {@link android.net.Uri} to contact for given Qr Code. *//*
        public static Uri buildContactQrCodeUri(String qrCode) {
            return CONTENT_URI.buildUpon().appendPath(PATH_QRCODE).appendPath(qrCode).build();
        }

        *//** Build {@link android.net.Uri} to contacts for given search query. *//*
        public static Uri buildContactSearchUri(String query) {
            return CONTENT_URI.buildUpon().appendPath(PATH_SEARCH).appendPath(query).build();
        }*/

        /** Read {@link #MESSAGE_ID} from {@link Contacts} {@link android.net.Uri}. */
        public static String getMessageId(Uri uri) {
            return uri.getPathSegments().get(1);
        }

        public static ContentValues addNewMessageValues(long chatId, String message) {
            ContentValues c = new ContentValues();
            c.put(SyncColumns.UPDATED, Sync.NEW);
            c.put(MESSAGE_STATE, State.LOCAL);
            c.put(MESSAGE_CREATED, Converter.getCurrentGtmTimestampString());
            c.put(MESSAGE_CHAT_ID, String.valueOf(chatId));
            c.put(MESSAGE_TEXT, message);
            c.put(MESSAGE_QRCODE, QodemePreferences.getInstance().getQrcode());
            return c;
        }

        public static ContentValues addNewMessagePushValues(Message mMessage) {
            ContentValues c = new ContentValues();
            c.put(SyncColumns.UPDATED, QodemeContract.Sync.NEW);
            c.put(MESSAGE_STATE, State.NOT_READ);
            c.put(MESSAGE_CREATED, mMessage.created);
            c.put(MESSAGE_CHAT_ID, String.valueOf(mMessage.chatId));
            c.put(MESSAGE_ID, String.valueOf(mMessage.messageId));
            c.put(MESSAGE_TEXT, mMessage.message);
            c.put(MESSAGE_QRCODE, mMessage.qrcode);
            return c;
        }

        public static ContentValues addNewMessageWasReadValues() {
            ContentValues c = new ContentValues();
            c.put(MESSAGE_STATE, State.READ);
            return c;
        }


        public static ContentValues msssageReadLocalValues() {
            ContentValues c = new ContentValues();
            c.put(MESSAGE_STATE, State.READ_LOCAL);
            return c;
        }

        public interface Query {
            String[] PROJECTION = {
                    Messages._ID,
                    Messages.UPDATED,
                    Messages.MESSAGE_ID,
                    Messages.MESSAGE_CHAT_ID,
                    Messages.MESSAGE_QRCODE,
                    Messages.MESSAGE_TEXT,
                    Messages.MESSAGE_CREATED,
                    Messages.MESSAGE_STATE
            };

            int _ID = 0;
            int UPDATED = 1;
            int MESSAGE_ID = 2;
            int MESSAGE_CHAT_ID = 3;
            int MESSAGE_QRCODE = 4;
            int MESSAGE_TEXT = 5;
            int MESSAGE_CREATED = 6;
            int MESSAGE_STATE = 7;
        }
    }

}
