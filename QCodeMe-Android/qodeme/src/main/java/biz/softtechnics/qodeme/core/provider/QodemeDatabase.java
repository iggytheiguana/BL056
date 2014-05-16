package biz.softtechnics.qodeme.core.provider;

import android.accounts.Account;
import android.content.ContentResolver;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.provider.BaseColumns;

import biz.softtechnics.qodeme.core.accounts.GenericAccountService;
import biz.softtechnics.qodeme.core.data.preference.QodemePreferences;
import biz.softtechnics.qodeme.core.sync.SyncHelper;

import static biz.softtechnics.qodeme.core.provider.QodemeContract.CONTENT_AUTHORITY;
import static biz.softtechnics.qodeme.core.provider.QodemeContract.ChatSettingColumns;
import static biz.softtechnics.qodeme.core.provider.QodemeContract.Chats;
import static biz.softtechnics.qodeme.core.provider.QodemeContract.Contacts;
import static biz.softtechnics.qodeme.core.provider.QodemeContract.Messages;
import static biz.softtechnics.qodeme.core.provider.QodemeContract.SyncColumns;
import static biz.softtechnics.qodeme.utils.LogUtils.LOGD;
import static biz.softtechnics.qodeme.utils.LogUtils.LOGI;
import static biz.softtechnics.qodeme.utils.LogUtils.makeLogTag;

/**
 * Created by Alex on 11/26/13.
 */
public class QodemeDatabase extends SQLiteOpenHelper {

    private static final String TAG = makeLogTag(QodemeDatabase.class);



    static abstract interface Tables {
        public static final String CONTACTS = "contacts";
        public static final String MESSAGES = "messages";
        public static final String CHATS = "chats";
        public static final String CHAT_SETTINGS = "chat_settings";
        public static final String GLOBAL_SETTINGS = "global_settings";

    }

    private static final String DATABASE_NAME = QodemePreferences.getInstance().getDbName();
    private static final int VER_0_4 = 104;
    private static final int VER_0_5 = 105;
    private static final int VER_0_7 = 107;
    private static final int DATABASE_VERSION = VER_0_7;
    private final Context mContext;

    public QodemeDatabase(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
        this.mContext = context;
    }

    public void onCreate(SQLiteDatabase db) {

        db.execSQL("CREATE TABLE " + Tables.CONTACTS + " ("
                + BaseColumns._ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                + SyncColumns.UPDATED + " INTEGER NOT NULL,"
                + Contacts.CONTACT_ID + " INTEGER,"
                + Contacts.CONTACT_QRCODE + " TEXT,"
                + Contacts.CONTACT_TITLE + " TEXT NOT NULL,"
                + Contacts.CONTACT_COLOR + " INTEGER NOT NULL,"
                + Contacts.CONTACT_STATE + " INTEGER NOT NULL,"
                + Contacts.CONTACT_PUBLIC_NAME + " TEXT,"
                + Contacts.CONTACT_MESSAGE + " TEXT,"
                + Contacts.CONTACT_LOCATION + " TEXT,"
                + Contacts.CONTACT_DATETIME + " TEXT,"
                + Contacts.CONTACT_CHAT_ID + " INTEGER)");

        db.execSQL("CREATE TABLE " + Tables.CHATS + " ("
                + BaseColumns._ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                + SyncColumns.UPDATED + " INTEGER NOT NULL,"
                + Chats.CHAT_ID + " INTEGER,"
                + Chats.CHAT_QRCODE + " TEXT,"
                + Chats.CHAT_TITLE + " TEXT,"
                + Chats.CHAT_TAGS + " TEXT,"
                + Chats.CHAT_COLOR + " INTEGER NOT NULL DEFAULT 0,"
                + Chats.CHAT_TYPE + " INTEGER NOT NULL," 
                + Chats.CHAT_LATITUDE + " TEXT,"
                + Chats.CHAT_LONGITUDE + " TEXT,"
                + Chats.CHAT_NUMBER_OF_MEMBER + " INTEGER NOT NULL DEFAULT 0,"
                + Chats.CHAT_DESCRIPTION + " TEXT,"
                + Chats.CHAT_IS_LOCKED + " INTEGER NOT NULL DEFAULT 0,"
                + Chats.CHAT_NUMBER_OF_FLAGGED + " INTEGER NOT NULL DEFAULT 0,"
                + Chats.CHAT_STATUS + " TEXT)");

        db.execSQL("CREATE TABLE " + Tables.MESSAGES + " ("
                + BaseColumns._ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                + SyncColumns.UPDATED + " INTEGER NOT NULL,"
                + Messages.MESSAGE_ID + " INTEGER,"
                + Messages.MESSAGE_CHAT_ID + " INTEGER,"
                + Messages.MESSAGE_QRCODE + " TEXT,"
                + Messages.MESSAGE_TEXT + " TEXT,"
                + Messages.MESSAGE_CREATED + " TEXT,"
                + Messages.MESSAGE_STATE + " INTEGER NOT NULL DEFAULT 0," 
                + Messages.MESSAGE_PHOTO_URL + " TEXT,"
                + Messages.MESSAGE_HASH_PHOTO + " INTEGER,"
                + Messages.MESSAGE_REPLY_TO_ID + " INTEGER,"
                + Messages.MESSAGE_LATITUDE + " TEXT,"
                + Messages.MESSAGE_LONGITUDE + " TEXT,"
                + Messages.MESSAGE_SENDERNAME + " TEXT,"
                + Messages.MESSAGE_PHOTO_URL_LOCAL + " TEXT )");

        db.execSQL("CREATE TABLE " + Tables.CHAT_SETTINGS + " ("
                + BaseColumns._ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                + SyncColumns.UPDATED + " INTEGER NOT NULL,"
                + ChatSettingColumns.CHAT_ID + " INTEGER NOT NULL,"
                + ChatSettingColumns.CHAT_HEIGHT + " TEXT,"
                + "UNIQUE (" + ChatSettingColumns.CHAT_ID + ") ON CONFLICT REPLACE)");
    }

    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        LOGD(TAG, "onUpgrade() from " + oldVersion + " to " + newVersion);

        Account account = GenericAccountService.GetAccount();
        LOGI(TAG, "Cancelling any pending syncs for for account");
        ContentResolver.cancelSync(account, CONTENT_AUTHORITY);

        db.execSQL("DROP TABLE IF EXISTS " + Tables.CONTACTS);
        db.execSQL("DROP TABLE IF EXISTS " + Tables.MESSAGES);
        db.execSQL("DROP TABLE IF EXISTS " + Tables.CHATS);
        db.execSQL("DROP TABLE IF EXISTS " + Tables.CHAT_SETTINGS);
        db.execSQL("DROP TABLE IF EXISTS " + Tables.GLOBAL_SETTINGS);


        onCreate(db);

        LOGI(TAG, "DB upgrade complete. Requesting resync.");
        SyncHelper.requestManualSync();
    }

    public void clearAllTables() {
        final SQLiteDatabase db = getWritableDatabase();
        db.beginTransaction();
        try {
            db.execSQL("DELETE from " + Tables.CONTACTS);
            db.execSQL("DELETE from " + Tables.MESSAGES);
            db.execSQL("DELETE from " + Tables.CHATS);
            db.execSQL("DELETE from " + Tables.CHAT_SETTINGS);
            //db.execSQL("DELETE from " + Tables.GLOBAL_SETTINGS);
            db.setTransactionSuccessful();
            return;
        } finally {
            db.endTransaction();
        }
    }

    public static void deleteDatabase(Context context) {
        context.deleteDatabase(DATABASE_NAME);
    }


}