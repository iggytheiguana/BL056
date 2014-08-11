package com.blulabellabs.code.ui;

import android.accounts.Account;
import android.app.Activity;
import android.app.ActivityOptions;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.ContentProviderOperation;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SyncStatusObserver;
import android.content.res.Configuration;
import android.database.ContentObserver;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Point;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.provider.MediaStore;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.support.v4.widget.DrawerLayout;
import android.support.v4.widget.DrawerLayout.DrawerListener;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.text.Html;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.VolleyError;
import com.aphidmobile.flip.FlipViewController;
import com.blulabellabs.code.Application;
import com.blulabellabs.code.ApplicationConstants;
import com.blulabellabs.code.R;
import com.blulabellabs.code.core.accounts.GenericAccountService;
import com.blulabellabs.code.core.data.IntentKey;
import com.blulabellabs.code.core.data.entities.ChatType;
import com.blulabellabs.code.core.data.entities.LookupChatEntity;
import com.blulabellabs.code.core.data.preference.QodemePreferences;
import com.blulabellabs.code.core.io.RestAsyncHelper;
import com.blulabellabs.code.core.io.model.ChatLoad;
import com.blulabellabs.code.core.io.model.Contact;
import com.blulabellabs.code.core.io.model.Message;
import com.blulabellabs.code.core.io.model.ModelHelper;
import com.blulabellabs.code.core.io.responses.BaseResponse;
import com.blulabellabs.code.core.io.responses.ChatAddMemberResponse;
import com.blulabellabs.code.core.io.responses.ChatCreateResponse;
import com.blulabellabs.code.core.io.responses.LookupResponse;
import com.blulabellabs.code.core.io.responses.VoidResponse;
import com.blulabellabs.code.core.io.utils.RestError;
import com.blulabellabs.code.core.io.utils.RestErrorType;
import com.blulabellabs.code.core.io.utils.RestListener;
import com.blulabellabs.code.core.provider.QodemeContract;
import com.blulabellabs.code.core.provider.QodemeContract.Contacts.Sync;
import com.blulabellabs.code.core.provider.QodemeContract.SyncColumns;
import com.blulabellabs.code.core.sync.SyncHelper;
import com.blulabellabs.code.images.utils.ImageCache;
import com.blulabellabs.code.images.utils.ImageFetcher;
import com.blulabellabs.code.images.utils.Utils;
import com.blulabellabs.code.ui.common.FullChatListBaseAdapter;
import com.blulabellabs.code.ui.common.MainChatListAdapter;
import com.blulabellabs.code.ui.common.MenuListAdapter;
import com.blulabellabs.code.ui.contacts.ContactDetailsActivity;
import com.blulabellabs.code.ui.contacts.ContactListItem;
import com.blulabellabs.code.ui.contacts.ContactListItemEntity;
import com.blulabellabs.code.ui.contacts.ContactListItemInvited;
import com.blulabellabs.code.ui.one2one.ChatGroupPhotosFragment;
import com.blulabellabs.code.ui.one2one.ChatGroupProfileFragment;
import com.blulabellabs.code.ui.one2one.ChatInsideFragment;
import com.blulabellabs.code.ui.one2one.ChatInsideGroupFragment;
import com.blulabellabs.code.ui.one2one.ChatListFragment;
import com.blulabellabs.code.ui.one2one.ChatListGroupPublicFragment;
import com.blulabellabs.code.ui.one2one.ChatPhotosFragment;
import com.blulabellabs.code.ui.one2one.ChatProfileFragment;
import com.blulabellabs.code.ui.preferences.SettingsActivity;
import com.blulabellabs.code.ui.qr.QrCodeCaptureActivity;
import com.blulabellabs.code.ui.qr.QrCodeShowActivity;
import com.blulabellabs.code.utils.AnalyticsHelper;
import com.blulabellabs.code.utils.Converter;
import com.blulabellabs.code.utils.DbUtils;
import com.blulabellabs.code.utils.Helper;
import com.blulabellabs.code.utils.LatLonCity;
import com.blulabellabs.code.utils.MyLocation;
import com.blulabellabs.code.utils.MyLocation.LocationResult;
import com.blulabellabs.code.utils.NullHelper;
import com.blulabellabs.code.utils.QrUtils;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesClient;
import com.google.android.gms.location.LocationClient;
import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.primitives.Longs;

import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class MainActivity extends ActionBarActivity implements ChatListFragment.One2OneChatListFragmentCallback, ChatInsideFragment.One2OneChatInsideFragmentCallback,
        GooglePlayServicesClient.ConnectionCallbacks, GooglePlayServicesClient.OnConnectionFailedListener {
    public static final String DELETE_BRODCAST_ACTION = "delete_broadcast_action";
    public static final String CHAT_ADDED_BRODCAST_ACTION = "chat_added_broadcast_action";
    private static final String TAG = "ImageGridFragment";
    //    private static final String CHAT_LIST_FRAGMENT = "chat_list_fragment";
    private static final String IMAGE_CACHE_DIR = "thumbs";
    private static final String JPEG_FILE_PREFIX = "IMG_";
    private static final String JPEG_FILE_SUFFIX = ".jpg";

    private static final int REQUEST_ACTIVITY_SCAN_QR_CODE = 2;
    private static final int REQUEST_ACTIVITY_CONTACT_DETAILS = 3;
    private static final int REQUEST_ACTIVITY_PHOTO_GALLERY = 4;
    private static final int REQUEST_ACTIVITY_CAMERA = 5;
    private static final int REQUEST_ACTIVITY_MORE = 6;
    private static final int REQUEST_ACTIVITY_SHOW_QR_CODE = 7;
    public static final int REQUEST_ACTIVITY_SCAN_QR_CODE_2 = 8;
    private static final int DEFAULT_HEIGHT_DP = 200;

    private int mDefaultHeightPx;
    private boolean mActive;
    private String mSearchText;
    private String mCurrentPhotoPath;
    private long currentChatId = -1;
    private int chatType = 0;
    private int fullChatIndex = 0;
    private boolean isAddMemberOnExistingChat = false;
    private MenuListAdapter<ContactListItemEntity> mContactListAdapter;
    private boolean mContactInfoUpdated;
    private List<Contact> mContacts;
    private List<Contact> mApprovedContacts;
    private List<Contact> mBlockContacts;
    private List<Contact> mBlockedContacts;
    private List<ChatLoad> mChatListSearchPublic = Lists.newArrayList();
    private List<ChatLoad> mChatListSearchPrivate = Lists.newArrayList();
    private List<Contact> mChatListSearchOneToOne = Lists.newArrayList();
    private List<Contact> selectedContact = Lists.newArrayList();
    public List<ChatLoad> mChatList;
    public List<Long> refressedChatId = Lists.newArrayList();
    private Map<Long, Integer> mChatNewMessagesMap;
    private Map<Long, List<Message>> mChatMessagesMap = Maps.newHashMap();
    private Map<Long, Integer> mChatHeightMap;
    public Map<Integer, ChatLoad> newChatCreated = new HashMap<Integer, ChatLoad>();
    public Map<Long, Integer> messageColorMap = new HashMap<Long, Integer>();

    private ContactListLoader mContactListLoader;
    private MessageListLoader mMessageListLoader;
    private ChatLoadListLoader mChatLoadListLoader;

    private LocationClient mLocationClient;
    private boolean isAddContact = false;
    private boolean isPublicSearch = false;
    private boolean isOneToOneSearch = false;
    private String publicSearchString = "";
    private String oneToOneSearchString = "";
    private Location currentLocation;
    public static boolean isKeyboardVisible = false;
    public static boolean isKeyboardHide = false;
//    private int mShortAnimationDuration;
    private ImageButton mImgCamera, mImgBtnOneToOne, mImgBtnPublic;
    private ChatListFragment one2OneChatListFragment;
    //    private ChatListGroupFragment privateChatListFragment;
    public ChatListGroupPublicFragment publicChatListFragment;
    Button buttonshare, buttonMore, buttonInvite;

    private ProgressDialog progressDialog;

    private DrawerLayout mDrawerLayout;
    private LinearLayout drawerPanel;
    private ListView mContactListView;
    public ActionBar mActionBar;
    private MainChatListAdapter adapter;
    private ViewPager mViewPagerMain;
    private FullChatListBaseAdapter mChatFlipperAdapter;
    private FlipViewController mChatFlipper;
    private Object mSyncObserverHandle;
    private ImageFetcher mImageFetcher;

    private FrameLayout contentFrame;
    private FrameLayout expandedChatFrame;

    private AlbumStorageDirFactory mAlbumStorageDirFactory = null;
    private MyLocation myLocation;

    private long chatFromNotification = -1;
//    float startScaleFinal;
    Rect startBounds = new Rect();

    public interface LoadMoreChatListener {
        public void onSearchResult(int count, int responseCode);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        AnalyticsHelper.onCreateActivity(this);
        setContentView(R.layout.activity_main);
        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        mViewPagerMain = (ViewPager) findViewById(R.id.pager_main);
        expandedChatFrame = (FrameLayout) findViewById(R.id.expanded_chatView);
        contentFrame = (FrameLayout) findViewById(R.id.content_frame);
        initImageFetcher();
        initActionBar();
        initContactsList();
        ((Application) getApplication()).setMainActivity(this);
        initChatHeight();
        initChatListFragment();
        if (savedInstanceState != null) {
            // Then the application is being reloaded
            mSearchText = savedInstanceState.getString("searchText");
        }

        mLocationClient = new LocationClient(this, this, this);
        initKeyboardListener();
        initFullChatLayout();

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.FROYO) {
            mAlbumStorageDirFactory = new FroyoAlbumDirFactory();
        } else {
            mAlbumStorageDirFactory = new BaseAlbumDirFactory();
        }
        myLocation = new MyLocation();
        myLocation.getLocation(this, locationResult);

        Bundle bundle = getIntent().getExtras();
        if (bundle != null) {
            chatFromNotification = bundle.getLong("chat_id");
        }
        mViewPagerMain.setCurrentItem(1);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK)
            switch (requestCode) {
                case REQUEST_ACTIVITY_SCAN_QR_CODE: {
                    final String qrCode = data.getStringExtra(IntentKey.QR_CODE);
                    final String title = data.getStringExtra(IntentKey.CONTACT_NAME);
                    final int typeChat = data.getIntExtra("Type", 0);
                    final long chatId = data.getLongExtra(IntentKey.CHAT_ID, -1);
                    if (QodemePreferences.getInstance().getQrcode().equals(qrCode)) {
                        showMessage("You can't add own QR code!");
                        return;
                    }

                    int type = data.getIntExtra(IntentKey.CHAT_TYPE, -1);
                    if ((type & QrCodeCaptureActivity.QODEME_CONTACT) == QrCodeCaptureActivity.QODEME_CONTACT
                            && !TextUtils.isEmpty(qrCode)) {
                        if (typeChat == 0) {
                            Cursor c = getContentResolver().query(QodemeContract.Contacts.CONTENT_URI,
                                    QodemeContract.Contacts.ContactQuery.PROJECTION,
                                    QodemeContract.Contacts.CONTACT_QRCODE + " = '" + qrCode + "'",
                                    null, null);
                            if (!c.moveToFirst()) {
                                getContentResolver().insert(QodemeContract.Contacts.CONTENT_URI,
                                        QodemeContract.Contacts.addNewContactValues(qrCode, title));
                                SyncHelper.requestManualSync();
                            } else {
                                if (c.moveToFirst()) {
                                    int state = c
                                            .getInt(QodemeContract.Contacts.ContactQuery.CONTACT_STATE);
                                    long id = c.getLong(QodemeContract.Contacts.ContactQuery._ID);
                                    if (state != QodemeContract.Contacts.State.APPRUVED) {
                                        ContentValues contentValues = new ContentValues();
                                        contentValues.put(SyncColumns.UPDATED, Sync.NEW | Sync.UPDATED);
                                        getContentResolver().update(
                                                QodemeContract.Contacts.CONTENT_URI, contentValues,
                                                DbUtils.getWhereClauseForId(),
                                                DbUtils.getWhereArgsForId(id));
                                        SyncHelper.requestManualSync();
                                    } else
                                        showMessage("It's already your contact!");
                                } else
                                    showMessage("It's already your contact!");
                            }
                        } else if (typeChat == 2) {
                            if (chatId != -1) {
                                RestAsyncHelper.getInstance().chatAddMember(chatId,
                                        QodemePreferences.getInstance().getQrcode(),
                                        new RestListener<ChatAddMemberResponse>() {

                                            @Override
                                            public void onResponse(ChatAddMemberResponse response) {
                                                Log.d("Chat add in public ", "Chat add mem "
                                                        + response.getChat().getId());
                                            }

                                            @Override
                                            public void onServiceError(RestError error) {
                                                Log.d("Error", "Chat add member");
                                            }
                                        }
                                );
                            }
                        }
                    }
                    mViewPagerMain.setCurrentItem(1);
                    break;
                }
                case REQUEST_ACTIVITY_CONTACT_DETAILS:
                    long id = data.getLongExtra(QodemeContract.Contacts._ID, -1);
                    int type = data.getIntExtra("color_type", 0);
                    int color = data.getIntExtra(QodemeContract.Contacts.CONTACT_COLOR, 0);
                    if (type == 0) {
                        int updated = data.getIntExtra(QodemeContract.Contacts.UPDATED,
                                QodemeContract.Contacts.Sync.UPDATED);
                        getContentResolver().update(QodemeContract.Contacts.CONTENT_URI,
                                QodemeContract.Contacts.updateContactInfoValues(null, color, updated),
                                DbUtils.getWhereClauseForId(), DbUtils.getWhereArgsForId(id));
                        SyncHelper.requestManualSync();
                    } else if (type == 2) {

                        int updated = data.getIntExtra(QodemeContract.Contacts.UPDATED,
                                QodemeContract.Contacts.Sync.UPDATED);
                        getContentResolver().update(QodemeContract.Contacts.CONTENT_URI,
                                QodemeContract.Contacts.updateContact(updated),
                                DbUtils.getWhereClauseForId(), DbUtils.getWhereArgsForId(id));

                        getContentResolver().update(
                                QodemeContract.Chats.CONTENT_URI,
                                QodemeContract.Chats.updateChatInfoValues("", color, "", 0, "", "",
                                        updated, 1),
                                // updateContactInfoValues(null, color, updated),
                                QodemeContract.Chats.CHAT_ID + " = " + currentChatId, null
                        );

                        SyncHelper.requestManualSync();
                    } else {
                        int updated = QodemeContract.Contacts.Sync.DONE;// data.getIntExtra(QodemeContract.Contacts.UPDATED,QodemeContract.Contacts.Sync.DONE);
                        getContentResolver().update(
                                QodemeContract.Chats.CONTENT_URI,
                                QodemeContract.Chats.updateChatInfoValues("", color, "", 0, "", "",
                                        updated, 1),
                                DbUtils.getWhereClauseForId(), DbUtils.getWhereArgsForId(id)
                        );

                        ChatLoad chatLoad = getChatLoad(currentChatId);
                        if (chatLoad != null)
                            setChatInfo(currentChatId, null, color, chatLoad.tag, chatLoad.description,
                                    chatLoad.chat_status, chatLoad.is_locked, chatLoad.title,
                                    chatLoad.latitude, chatLoad.longitude);
                    }
                    mChatFlipper.setSelection(1);
                    break;
                case REQUEST_ACTIVITY_PHOTO_GALLERY:
                    if (!(data == null)) {
                        System.gc();
                        Uri mImageCaptureUri = data.getData();
                        File file = new File(getPath(mImageCaptureUri));
                        try {
                            System.out.println(file.exists());
                            try {

                                ChatInsideFragment chatInsideFragment = null;
                                ChatInsideGroupFragment chatInsideGroupFragment = null;

                                if (!getActionBar().isShowing()) {
                                    if (mChatFlipperAdapter != null) {
                                        if (chatType == 0)
                                            chatInsideFragment = (ChatInsideFragment) mChatFlipperAdapter
                                                    .getItem(0);
                                        else
                                            chatInsideGroupFragment = (ChatInsideGroupFragment) mChatFlipperAdapter
                                                    .getItem(0);
                                    }
                                    if (chatInsideFragment != null)
                                        chatInsideFragment.sendImageMessage(file.getAbsolutePath());
                                    else if (chatInsideGroupFragment != null) {
                                        chatInsideGroupFragment
                                                .sendImageMessage(file.getAbsolutePath());
                                    }
                                } else {
                                    if (currentChatId != -1)
                                        sendMessage(currentChatId, "", "", 1, -1, 0, 0, "",
                                                file.getAbsolutePath());
                                }

                            } catch (OutOfMemoryError e) {
                                e.printStackTrace();
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                    break;

                case REQUEST_ACTIVITY_CAMERA:
                    File file = new File(mCurrentPhotoPath);
                    ChatInsideFragment chatInsideFragment = null;
                    ChatInsideGroupFragment chatInsideGroupFragment = null;

                    if (!getActionBar().isShowing()) {
                        if (mChatFlipperAdapter != null) {
                            if (chatType == 0)
                                chatInsideFragment = (ChatInsideFragment) mChatFlipperAdapter.getItem(0);
                            else
                                chatInsideGroupFragment = (ChatInsideGroupFragment) mChatFlipperAdapter
                                        .getItem(0);
                        }
                        if (chatInsideFragment != null)
                            chatInsideFragment.sendImageMessage(file.getAbsolutePath());
                        else if (chatInsideGroupFragment != null) {
                            chatInsideGroupFragment.sendImageMessage(file.getAbsolutePath());
                        }
                    } else {
                        if (currentChatId != -1)
                            sendMessage(currentChatId, "", "", 1, -1, 0, 0, "", file.getAbsolutePath());
                    }
                    break;
                case REQUEST_ACTIVITY_MORE:
                    int typeData = data.getIntExtra("type", 0);
                    if (typeData == 1) {
                        RestAsyncHelper.getInstance().registerToken("", new RestListener() {

                            @Override
                            public void onResponse(BaseResponse response) {
                                RestAsyncHelper.getInstance().accountLogout(new RestListener() {
                                    @Override
                                    public void onResponse(BaseResponse response) {
                                        logoutHandler();
                                    }

                                    @Override
                                    public void onServiceError(RestError error) {
                                        showMessage(RestErrorType.getMessage(getContext(),
                                                error.getErrorType())
                                                + error.getServerMsg());
                                    }

                                    @Override
                                    public void onNetworkError(VolleyError error) {
                                        super.onNetworkError(error);
                                        showMessage(error.getMessage());
                                    }
                                });
                            }

                            @Override
                            public void onServiceError(RestError error) {
                                showMessage(RestErrorType.getMessage(getContext(), error.getErrorType())
                                        + error.getServerMsg());
                            }

                            @Override
                            public void onNetworkError(VolleyError error) {
                                super.onNetworkError(error);
                                showMessage("No internet connection!");
                            }

                            private void logoutHandler() {
                                clearActivityCache();
                                QodemePreferences.getInstance().setLogged(false);
                                QodemePreferences.getInstance().setGcmTokenSycnWithRest(false);
                                startActivity(new Intent(getApplicationContext(), LoginActivity.class));
                                finish();

                            }
                        });
                    } else if (typeData == 2) {
                        Intent i = new Intent(getContext(), QrCodeCaptureActivity.class);
                        i.putExtra(IntentKey.CHAT_TYPE, QrCodeCaptureActivity.QODEME_CONTACT);
                        startActivityForResult(i, REQUEST_ACTIVITY_SCAN_QR_CODE);
                    } else if (typeData == 3) {
                        Intent i = new Intent(this, QrCodeShowActivity.class);
                        i.putExtra(IntentKey.QR_CODE, QodemePreferences.getInstance().getQrcode());
                        startActivityForResult(i, REQUEST_ACTIVITY_SHOW_QR_CODE);
                    }
                    break;
                case REQUEST_ACTIVITY_SHOW_QR_CODE:
                    int typeData1 = data.getIntExtra("type", 0);
                    if (typeData1 == 2) {
                        Intent i = new Intent(getContext(), QrCodeCaptureActivity.class);
                        i.putExtra(IntentKey.CHAT_TYPE, QrCodeCaptureActivity.QODEME_CONTACT);
                        startActivityForResult(i, REQUEST_ACTIVITY_SCAN_QR_CODE);
                    }
                    break;
            }
        else {
            setCurrentChatId(-1);
            if (requestCode == REQUEST_ACTIVITY_SCAN_QR_CODE) {
                mViewPagerMain.setCurrentItem(1);
            }
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        mLocationClient.connect();
        AnalyticsHelper.onStartActivity(this);
    }

    @Override
    protected void onStop() {
        // Disconnecting the client invalidates it.
        mLocationClient.disconnect();
        super.onStop();
        AnalyticsHelper.onStopActivity(this);
    }

    @Override
    public void onConnected(Bundle bundle) {
        // mCurrentLocation = mLocationClient.getLastLocation();
        Location loc = mLocationClient.getLastLocation();
        if (loc != null)
            getMyLocation(loc);
        else
            myLocation.getLocation(this, locationResult);
    }

    @Override
    public void onDisconnected() {
    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        Log.d("MENU", "Cliced MenuItem is " + item.getTitle());
        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                return true;
            case R.id.action_settings: {
                Intent i = new Intent(getContext(), SettingsActivity.class);
                startActivity(i);
                return true;
            }
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        Log.d("config", "call");
        try {
            if (chatType != 0) {
                ChatInsideGroupFragment chatInsideGroupFragment = (ChatInsideGroupFragment) mChatFlipperAdapter.getItem(0);
                chatInsideGroupFragment.onConfigurationChanged(newConfig);
            }

            mChatFlipperAdapter.notifyDataSetChanged();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void onDestroy() {
        MainActivity.isKeyboardVisible = false;
        mImageFetcher.closeCache();
        QodemePreferences.getInstance().setNewPublicGroupChatId(-1l);
        QodemePreferences.getInstance().setNewPrivateGroupChatId(-1l);
        try {
            super.onDestroy();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onBackPressed() {
        MainActivity.isKeyboardVisible = false;
        if (expandedChatFrame.getVisibility()==View.VISIBLE) {
            getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN);
            getActionBar().show();
            fullChatIndex = 0;
            zoomOut(expandedChatFrame);
            if (mChatFlipperAdapter != null) {
                Long chatId = null;
                if (mChatFlipperAdapter.getItem(0) instanceof ChatInsideFragment) {
                    ChatInsideFragment chatInsideFragment = (ChatInsideFragment) mChatFlipperAdapter
                            .getItem(0);
                    chatId = chatInsideFragment.getChatId();
                } else if (mChatFlipperAdapter.getItem(0) instanceof ChatInsideGroupFragment) {
                    ChatInsideGroupFragment chatInsideGroupFragment = (ChatInsideGroupFragment) mChatFlipperAdapter
                            .getItem(0);
                    chatId = chatInsideGroupFragment.getChatId();
                }
                if (chatId != null) {
                    QodemePreferences.getInstance().set("" + chatId, null);
                    List<Message> messages = getChatMessages(chatId);
                    if (messages != null)
                        messageRead(chatId);
                }
            }
            return;
        }
        super.onBackPressed();
    }

    private void zoomOut(final View expandedImageView) {
//        AnimatorSet set = new AnimatorSet();
//        set.play(
//                ObjectAnimator.ofFloat(expandedImageView, View.X,
//                        (startBounds.right - startBounds.left) / 2)
//        )
//                .with(ObjectAnimator.ofFloat(expandedImageView, View.Y, startBounds.top + 50))
//                .with(ObjectAnimator.ofFloat(expandedImageView, View.SCALE_X, startScaleFinal))
//                .with(ObjectAnimator.ofFloat(expandedImageView, View.SCALE_Y, startScaleFinal));
//        set.setDuration(mShortAnimationDuration);
//        set.setInterpolator(new DecelerateInterpolator());
//        set.addListener(new AnimatorListenerAdapter() {
//            @Override
//            public void onAnimationEnd(Animator animation) {
//                expandedImageView.setVisibility(View.GONE);
//            }
//
//            @Override
//            public void onAnimationCancel(Animator animation) {
                expandedImageView.setVisibility(View.GONE);
//            }
//        });
//        set.start();
    }

    @Override
    public List<Contact> getContactList() {
        List<Contact> contacts = Lists.newArrayList();
        if (mApprovedContacts != null)
            contacts.addAll(mApprovedContacts);
        if (mBlockedContacts != null)
            contacts.addAll(mBlockedContacts);
        return contacts;
    }

    @Override
    public List<ChatLoad> getChatList(int chatType) {
        List<ChatLoad> tempList = Lists.newArrayList();
        if (mChatList != null) {
            for (ChatLoad chatLoad : mChatList) {
                // Log.d("Chattype", "" + chatLoad.type);
                if (chatLoad.type == chatType) {
                    tempList.add(chatLoad);
                }
            }
        }

        if (chatType == 2 && mChatListSearchPublic != null) {
            if (getPublicSearchString().trim().length() > 0) {
                List<ChatLoad> temp = Lists.newArrayList();
                List<ChatLoad> removeFromSearch = Lists.newArrayList();
                for (ChatLoad chatLoad : tempList) {
                    if ((chatLoad.title != null && chatLoad.title.contains(getPublicSearchString()))
                            || (chatLoad.tag != null && chatLoad.tag
                            .contains(getPublicSearchString()))) {
                        temp.add(chatLoad);
                    }
                    for (ChatLoad c : mChatListSearchPublic) {
                        if (c.chatId == chatLoad.chatId)
                            removeFromSearch.add(c);

                    }
                }
                mChatListSearchPublic.removeAll(removeFromSearch);
                temp.addAll(mChatListSearchPublic);
                return temp;
            } else {
                List<ChatLoad> removeFromSearch = Lists.newArrayList();
                for (ChatLoad chatLoad : tempList) {

                    for (ChatLoad c : mChatListSearchPublic) {
                        if (c.chatId == chatLoad.chatId)
                            removeFromSearch.add(c);
                    }
                }

                mChatListSearchPublic.removeAll(removeFromSearch);
            }

            tempList.addAll(mChatListSearchPublic);
        }
        return tempList;
    }

    @Override
    public List<Message> getChatMessages(long chatId) {
        List<Message> messages = mChatMessagesMap.get(chatId);
        if (messages != null) {
            List<Message> temp = Lists.newArrayList();
            for (Message msg : messages) {
                for (Contact contact : mBlockContacts) {
                    if (msg.qrcode.trim().equals(contact.qrCode.trim()))
                        temp.add(msg);
                }
            }
            messages.removeAll(temp);
        }
        return messages;
    }

    @Override
    public void sendMessage(final long chatId, String message, String photoUrl, int hashPhoto,
                            long replyTo_Id, double latitude, double longitude, String senderName, String localUrl) {

        ChatLoad chatLoad = null;
        int is_search = 1;

        try {
            if (mChatList != null) {
                for (ChatLoad chat : mChatList)
                    if (chat.chatId == chatId)
                        chatLoad = chat;
                is_search = 0;
            }
            if (chatLoad == null) {
                if (mChatListSearchPublic != null) {
                    for (ChatLoad chat : mChatListSearchPublic)
                        if (chat.chatId == chatId)
                            // return chatLoad;
                            chatLoad = chat;
                    chatLoad.is_favorite = 1;
                }
            } else {
                if (chatLoad.type == 2) {
                    int num_of_favorite = chatLoad.number_of_likes;
                    int is_favorite = 1;
                    if (chatLoad.is_favorite == 1) {
                    } else {
                        is_favorite = 1;
                        if (num_of_favorite <= 0) {
                            num_of_favorite = 1;
                        } else
                            num_of_favorite++;

                        getContentResolver().update(
                                QodemeContract.Chats.CONTENT_URI,
                                QodemeContract.Chats.updateFavorite(is_favorite,
                                        num_of_favorite),
                                QodemeContract.Chats.CHAT_ID + " = " + chatId, null
                        );
                    }
                }
            }
            getContentResolver().insert(
                    QodemeContract.Messages.CONTENT_URI,
                    QodemeContract.Messages.addNewMessageValues(chatId, message, photoUrl,
                            hashPhoto, replyTo_Id, latitude, longitude, senderName, localUrl,
                            is_search)
            );

            // For starting the background sync process
            SyncHelper.requestManualSync();

            if (is_search == 1) {
                new Handler().postDelayed(new Runnable() {

                    @Override
                    public void run() {
                        RestAsyncHelper.getInstance().chatAddMember(chatId,
                                QodemePreferences.getInstance().getQrcode(),
                                new RestListener<ChatAddMemberResponse>() {

                                    @Override
                                    public void onResponse(ChatAddMemberResponse response) {
                                        Log.d("Chat add in public ", "Chat add mem "
                                                + response.getChat().getId());
                                    }

                                    @Override
                                    public void onServiceError(RestError error) {
                                        Log.d("Error", "Chat add member");
                                    }
                                }
                        );
                    }
                }, 1000);

            }
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    @Override
    public int getHeight(long chatId) {
        if (chatId == -1) {
            if (chatType == 1) {
                Integer height = Converter.dipToPx(getApplicationContext(), 150);
                return height;
            } else {
                Integer height = Converter.dipToPx(getApplicationContext(), 120);
                return height;
            }
        }

        mChatList.size();

        Integer height = mChatHeightMap.get(chatId);

        List<Message> l = mChatMessagesMap.get(chatId);
        if (l!=null && height ==null ) {
            height = 120+30*l.size();
        }
        return height != null ? height : mDefaultHeightPx;
    }

    @Override
    public void setChatHeight(long chatId, int height) {
        mChatHeightMap.put(chatId, height);
    }

    @Override
    public void showChat(final Contact c, final boolean firstUpdate, final View view) {
        mContactInfoUpdated = false;
        isKeyboardHide = true;

        Helper.hideKeyboard(MainActivity.this);
        new Handler().post(new Runnable() {

            @Override
            public void run() {
                showOne2OneChatFragment(c, view, true);
            }
        });
    }

    @Override
    public void showChat(final ChatLoad c, final boolean firstUpdate, final View view) {
        mContactInfoUpdated = false;
        isKeyboardHide = true;
        Helper.hideKeyboard(MainActivity.this);
        new Handler().post(new Runnable() {

            @Override
            public void run() {
                showOne2OneChatFragment(c, view, true);
            }
        });
    }

    public int getNewMessagesCount(long chatId) {
        return mChatNewMessagesMap != null ? NullHelper.notNull(mChatNewMessagesMap.get(chatId), 0)
                : 0;
    }

    @Override
    public void messageRead(final long chatId) {
        if (getNewMessagesCount(chatId) > 0) {

            new AsyncTask<String, String, String>() {

                @Override
                protected String doInBackground(String... params) {
                    List<Message> messages = mChatMessagesMap.get(chatId);
                    if (messages != null && !messages.isEmpty()) {
                        ArrayList<ContentProviderOperation> batch = Lists.newArrayList();
                        List<Long> idList = Lists.newArrayList();
                        for (Message m : messages) {
                            if (m.state == QodemeContract.Messages.State.NOT_READ) {
                                idList.add(m._id);
                                m.state = QodemeContract.Messages.State.READ_LOCAL;
                            }
                        }
                        long[] ids = Longs.toArray(idList);
                        if (ids.length > 0) {
                            ContentProviderOperation.Builder builder = ContentProviderOperation
                                    .newUpdate(QodemeContract.Messages.CONTENT_URI);
                            builder.withValue(QodemeContract.SyncColumns.UPDATED,
                                    QodemeContract.Sync.UPDATED);
                            builder.withValue(QodemeContract.Messages.MESSAGE_STATE,
                                    QodemeContract.Messages.State.READ_LOCAL);
                            builder.withSelection(DbUtils.getWhereClauseForIds(ids),
                                    DbUtils.getWhereArgsForIds(ids));
                            batch.add(builder.build());
                            QodemeContract.applyBatch(getContext(), batch);
                            SyncHelper.requestManualSync();
                        }
                    }
                    return null;
                }

            }.execute("");

        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        mImageFetcher.setExitTasksEarly(false);
        mActive = true;
        mSyncStatusObserver.onStatusChanged(0);
        // Watch for sync state changes
        final int mask = ContentResolver.SYNC_OBSERVER_TYPE_PENDING
                | ContentResolver.SYNC_OBSERVER_TYPE_ACTIVE;
        mSyncObserverHandle = ContentResolver.addStatusChangeListener(mask, mSyncStatusObserver);
        getSupportLoaderManager().restartLoader(0, null, mContactListLoader);
        getSupportLoaderManager().restartLoader(1, null, mMessageListLoader);
        getSupportLoaderManager().restartLoader(2, null, mChatLoadListLoader);
        getContentResolver().registerContentObserver(QodemeContract.Contacts.CONTENT_URI, true,
                mContactListObserver);
        getContentResolver().registerContentObserver(QodemeContract.Messages.CONTENT_URI, true,
                mMessageListObjerver);
        getContentResolver().registerContentObserver(QodemeContract.Chats.CONTENT_URI, true,
                mChatListObserver);
        ((Application)getApplication()).start();

        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(DELETE_BRODCAST_ACTION);
        intentFilter.addAction(CHAT_ADDED_BRODCAST_ACTION);

        registerReceiver(broadcastReceiverForDeleteChat, intentFilter);
    }

    @Override
    protected void onPause() {
        super.onPause();
        mImageFetcher.setExitTasksEarly(true);
        mImageFetcher.flushCache();
        mActive = false;
        if (mSyncObserverHandle != null) {
            ContentResolver.removeStatusChangeListener(mSyncObserverHandle);
//            mSyncObserverHandle = null;
        }
        getContentResolver().unregisterContentObserver(mContactListObserver);
        getContentResolver().unregisterContentObserver(mMessageListObjerver);
        Helper.hideKeyboard(this);
        ((Application)getApplication()).stop();
        unregisterReceiver(broadcastReceiverForDeleteChat);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putString("searchText", mSearchText);
    }

    private void initContactsList() {
        mContactListView = (ListView) findViewById(R.id.existing_contacts);
        drawerPanel = (LinearLayout) findViewById(R.id.more_item_header);

        ((TextView) findViewById(R.id.contacts)).setTypeface(Application.typefaceRegular);


        mContactListView.setOnItemClickListener(new ListView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView parent, View view, int position, long id) {
                try {
                    final Contact ce;
                    if (view instanceof ContactListItem) {
                        ce = ((ContactListItem) view).getContact();
                    } else {
                        ce = ((ContactListItemInvited) view).getContact();
                    }
                    if (ce.state == QodemeContract.Contacts.State.APPRUVED && ce.isArchive == 1) {
                        getContentResolver().update(QodemeContract.Contacts.CONTENT_URI,
                                QodemeContract.Contacts.isArchiveValues(0),
                                DbUtils.getWhereClauseForId(), DbUtils.getWhereArgsForId(ce._id));
                        mDrawerLayout.closeDrawer(drawerPanel);
                        mViewPagerMain.setCurrentItem(2);
                        Helper.hideKeyboard(MainActivity.this);
                        showOne2OneChatFragment(ce, mChatFlipper, false);
                    } else if (ce.state == QodemeContract.Contacts.State.APPRUVED) {
                        mDrawerLayout.closeDrawer(drawerPanel);
                        mViewPagerMain.setCurrentItem(2);
                        Helper.hideKeyboard(MainActivity.this);
                        showOne2OneChatFragment(ce, mChatFlipper, false);
                    } else if (ce.state == QodemeContract.Contacts.State.INVITATION_SENT) {
                        Intent i = new Intent(getContext(), ContactDetailsActivity.class);
                        i.putExtra(QodemeContract.Contacts._ID, ce._id);
                        i.putExtra(QodemeContract.Contacts.CONTACT_TITLE, ce.title);
                        i.putExtra(QodemeContract.Contacts.CONTACT_COLOR, ce.color);
                        i.putExtra(QodemeContract.Contacts.UPDATED, ce.updated);
                        startActivityForResult(i, REQUEST_ACTIVITY_CONTACT_DETAILS);
                        mDrawerLayout.closeDrawer(drawerPanel);
                    } else if (ce.state == QodemeContract.Contacts.State.BLOCKED_BY) {
                        final CharSequence[] items = {"Unblock"};

                        AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                        builder.setTitle(R.string.app_name);
                        builder.setItems(items, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int item) {
                                switch (item) {
                                    case 0: { // Unblock
                                        long id = ce._id;
                                        int updated = ce.updated;
                                        getContentResolver().update(
                                                QodemeContract.Contacts.CONTENT_URI,
                                                QodemeContract.Contacts.acceptContactValues(updated),
                                                DbUtils.getWhereClauseForId(),
                                                DbUtils.getWhereArgsForId(id));
                                        SyncHelper.requestManualSync();

                                        break;
                                    }
                                }
                            }
                        });
                        AlertDialog alert = builder.create();
                        alert.show();
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
        buttonMore = (Button) findViewById(R.id.btn_more);
        buttonMore.setTypeface(Application.typefaceRegular);
        buttonMore.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, MoreOptionActivity.class);
                startActivityForResult(intent, REQUEST_ACTIVITY_MORE);
                mDrawerLayout.closeDrawers();
            }
        });
        buttonInvite = (Button) findViewById(R.id.btn_invite);
        buttonInvite.setTypeface(Application.typefaceRegular);
        buttonInvite.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                mDrawerLayout.closeDrawers();
                Intent i = new Intent(MainActivity.this, QrCodeShowActivity.class);
                i.putExtra(IntentKey.QR_CODE, QodemePreferences.getInstance().getQrcode());
                startActivityForResult(i, REQUEST_ACTIVITY_SHOW_QR_CODE);
            }
        });
        buttonshare = (Button) findViewById(R.id.btn_more_share);
        buttonshare.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mDrawerLayout.closeDrawers();
                email();
            }
        });
        mContactListView.setLongClickable(true);
        List<ContactListItemEntity> listForAdapter = Lists.newArrayList();
        mContactListAdapter = new MenuListAdapter<ContactListItemEntity>(this,
                R.layout.contact_list_item, listForAdapter) {

            @Override
            public View getView(int position, View convertView, ViewGroup parent) {

                ContactListItemEntity e = getItem(position);

                if (e.isHeader()) {
                    if (convertView == null || (convertView instanceof ContactListItem)
                            || (convertView instanceof ContactListItemInvited)) {
                        convertView = layoutInflater.inflate(R.layout.contact_list_item_header,
                                null);
                    }

                    TextView tvHeader = ((TextView) convertView.findViewById(R.id.header));
                    Button addbtn = (Button) convertView.findViewById(R.id.btn_add);
                    addbtn.setVisibility(View.GONE);
                    Button btnMore = (Button) convertView.findViewById(R.id.btn_more);
                    btnMore.setVisibility(View.GONE);

                    switch (e.getState()) {
                        case QodemeContract.Contacts.State.INVITED:
                            tvHeader.setText("Invitation");
                            break;
                        case QodemeContract.Contacts.State.INVITATION_SENT:
                            tvHeader.setText("Invited");
                            break;
                        case QodemeContract.Contacts.State.BLOCKED:
                            tvHeader.setVisibility(View.GONE);
                            break;
                        case QodemeContract.Contacts.State.APPRUVED:
                            btnMore.setVisibility(View.GONE);
                            btnMore.setOnClickListener(new View.OnClickListener() {

                                @Override
                                public void onClick(View v) {
                                    Intent intent = new Intent(MainActivity.this,
                                            MoreOptionActivity.class);
                                    startActivityForResult(intent, REQUEST_ACTIVITY_MORE);
                                    mDrawerLayout.closeDrawers();
                                }
                            });
                            tvHeader.setText("Contacts");
                            if (isAddContact) {

                                addbtn.setVisibility(View.VISIBLE);
                                addbtn.setOnClickListener(new View.OnClickListener() {

                                    @Override
                                    public void onClick(View v) {
                                        List<Contact> selectedContact = Lists.newArrayList();
                                        for (int i = 0; i < getCount(); i++) {
                                            ContactListItemEntity entity = getItem(i);
                                            if (entity.isChecked()) {
                                                selectedContact.add(entity.getContact());
                                                entity.setChecked(false);
                                            }
                                        }
                                        if (selectedContact.size() > 0) {
                                            if (isAddMemberOnExistingChat) {
                                                addMemberInChat(selectedContact);
                                            } else {
                                                MainActivity.this.selectedContact = selectedContact;
                                                ChatLoad chatLoad = new ChatLoad();
                                                chatLoad.type = chatType;
                                                chatLoad.chatId = -1;
                                                chatLoad.isCreated = false;
                                                newChatCreated.put(chatType, chatLoad);
                                                refreshOne2OneList();
                                            }
                                        }
                                        mDrawerLayout.closeDrawers();
                                    }
                                });
                                addbtn.setVisibility(View.VISIBLE);
                            }
                            break;
                        case QodemeContract.Contacts.State.BLOCKED_BY:
                            tvHeader.setText("Blocked");
                            break;

                    }
                } else {
                    if (e.getState() == QodemeContract.Contacts.State.INVITED) {
                        ContactListItemInvited view;
                        if (convertView == null || !(convertView instanceof ContactListItemInvited)) {
                            view = (ContactListItemInvited) layoutInflater.inflate(
                                    R.layout.contact_list_item_invited, null);
                        } else {
                            view = (ContactListItemInvited) convertView;
                        }
                        view.fill(e);
                        return view;

                    } else {
                        ContactListItem view;
                        if (convertView == null || !(convertView instanceof ContactListItem)) {
                            view = (ContactListItem) layoutInflater.inflate(layoutResId, null);
                        } else {
                            view = (ContactListItem) convertView;
                        }
                        view.setAddContactToChat(isAddContact);
                        view.fill(e);
                        return view;
                    }
                }
                return convertView;
            }
        };
        mContactListView.setAdapter(mContactListAdapter);
        mContactListLoader = new ContactListLoader();
        mMessageListLoader = new MessageListLoader();
        mChatLoadListLoader = new ChatLoadListLoader();

        getSupportLoaderManager().initLoader(0, null, mContactListLoader);
        getSupportLoaderManager().initLoader(1, null, mMessageListLoader);
        getSupportLoaderManager().initLoader(2, null, mChatLoadListLoader);
    }

    public void createChat(final String title, final int chatType) {

        ChatType mChatType = null;
        if (chatType == 1)
            mChatType = ChatType.PRIVATE_GROUP;
        else if (chatType == 2)
            mChatType = ChatType.PUBLIC_GROUP;
        if (mChatType == null) {
            mChatType = ChatType.PRIVATE_GROUP;
        }

        Location location = mLocationClient.getLastLocation();
        double latitude = 0;
        double longitude = 0;
        if (location != null) {
            latitude = location.getLatitude();
            longitude = location.getLongitude();
        } else if (getCurrentLocation() != null) {
            latitude = getCurrentLocation().getLatitude();
            longitude = getCurrentLocation().getLongitude();
        }
        final double lat = latitude;
        final double lng = longitude;
        RestAsyncHelper.getInstance().chatCreate(mChatType, title, "", 0, "", 0, "", latitude,
                longitude, new RestListener<ChatCreateResponse>() {

                    @Override
                    public void onResponse(ChatCreateResponse response) {
                        Log.d("Chat create", "Chat Created " + response.getChat().getId());

                        if (chatType == 2) {
                            QodemePreferences.getInstance().setNewPublicGroupChatId(
                                    response.getChat().getId());
                            Integer height = Converter.dipToPx(getApplicationContext(), 120);
                            setChatHeight(response.getChat().getId(), height);
                        }
                        if (chatType == 1) {
                            QodemePreferences.getInstance().setNewPrivateGroupChatId(
                                    response.getChat().getId());
                            Integer height = Converter.dipToPx(getApplicationContext(), 150);
                            setChatHeight(response.getChat().getId(), height);
                        }
                        newChatCreated.remove(chatType);
                        getContentResolver().insert(
                                QodemeContract.Chats.CONTENT_URI,
                                QodemeContract.Chats.addNewChatValues(response.getChat().getId(),
                                        response.getChat().getType(), response.getChat()
                                                .getQrcode(), QodemePreferences.getInstance()
                                                .getQrcode(), lat, lng, title
                                )
                        );

                        if (chatType == 1) {
                            for (Contact contact : selectedContact) {
                                final long chatid = response.getChat().getId();
                                final String qr = contact.qrCode;
                                Cursor cursor = getContentResolver().query(
                                        QodemeContract.Chats.CONTENT_URI,
                                        QodemeContract.Chats.ChatQuery.PROJECTION,
                                        QodemeContract.Chats.CHAT_ID + "=" + chatid, null, null);
                                String memberString = "";
                                int numberOfMember = 1;
                                if (cursor != null && cursor.moveToFirst()) {
                                    memberString = cursor
                                            .getString(QodemeContract.Chats.ChatQuery.CHAT_MEMBER_QRCODES);
                                    numberOfMember = cursor
                                            .getInt(QodemeContract.Chats.ChatQuery.CHAT_NUMBER_OF_MEMBER);
                                }
                                if (memberString == null || memberString.equals(""))
                                    memberString = qr;
                                else {
                                    memberString += "," + qr;
                                }
                                if (numberOfMember == 0) {
                                    numberOfMember = 2;
                                } else {
                                    numberOfMember++;
                                }
                                ContentValues contentValues = new ContentValues();
                                contentValues.put(QodemeContract.Chats.CHAT_MEMBER_QRCODES,
                                        memberString);
                                contentValues.put(QodemeContract.Chats.CHAT_NUMBER_OF_MEMBER,
                                        numberOfMember);
                                getContentResolver().update(QodemeContract.Chats.CONTENT_URI,
                                        contentValues, QodemeContract.Chats.CHAT_ID + "=" + chatid,  null);
                                RestAsyncHelper.getInstance().chatAddMember(
                                        response.getChat().getId(), contact.qrCode,
                                        new RestListener<ChatAddMemberResponse>() {

                                            @Override
                                            public void onResponse(ChatAddMemberResponse response) {
                                                Log.d("Chat add ", "Chat add mem "
                                                        + response.getChat().getId());
                                            }

                                            @Override
                                            public void onServiceError(RestError error) {
                                                Log.d("Error", "Chat add member");
                                            }
                                        }
                                );
                            }
                            selectedContact.clear();
                        }
                    }

                    @Override
                    public void onServiceError(RestError error) {
                        Log.d("Error", "Chat not Created");
                    }
                }
        );
    }

    public void deleteChat(ChatLoad mChatLoad) {
        if (mChatLoad==null) {
            mChatLoad=newChatCreated.get(2);
        }
        publicChatListFragment.mListAdapter.remove(mChatLoad);
        newChatCreated.remove(2);
        publicChatListFragment.mListAdapter.notifyDataSetChanged();
        Helper.hideKeyboard(this);
    }

    private void initImageFetcher() {
        final DisplayMetrics displayMetrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        final int height = displayMetrics.heightPixels;
        final int width = displayMetrics.widthPixels;
        final int longest = (height > width ? height : width) / 2;
        ImageCache.ImageCacheParams cacheParams = new ImageCache.ImageCacheParams(getActivity(),
                IMAGE_CACHE_DIR);
        cacheParams.setMemCacheSizePercent(0.25f); // Set memory cache to 25% of
        mImageFetcher = new ImageFetcher(this, longest);// mImageThumbSize
        mImageFetcher.addImageCache(getSupportFragmentManager(), cacheParams);
    }

    private void initFullChatLayout() {
        //mShortAnimationDuration = getResources().getInteger(android.R.integer.config_shortAnimTime);

        mChatFlipper = (FlipViewController) findViewById(R.id.pager);
        mChatFlipper.setOnViewFlipListener(new FlipViewController.ViewFlipListener() {
            @Override
            public void onViewFlipped(View view, int position) {
                fullChatIndex = position;
                if (fullChatIndex == 0)
                    getWindow().setSoftInputMode(
                            WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);
                else
                    getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN);
                Helper.hideKeyboard(MainActivity.this);
            }
        });
    }

    private void initKeyboardListener() {
        mDrawerLayout.setDrawerListener(new DrawerListener() {

            @Override
            public void onDrawerStateChanged(int arg0) {

            }

            @Override
            public void onDrawerSlide(View arg0, float arg1) {

            }

            @Override
            public void onDrawerOpened(View arg0) {

            }

            @Override
            public void onDrawerClosed(View arg0) {
                buttonshare.setVisibility(View.GONE);
                buttonMore.setVisibility(View.VISIBLE);
                buttonInvite.setVisibility(View.VISIBLE);
                refreshContactList();
                isAddContact = false;
                isAddMemberOnExistingChat = false;
                mContactListAdapter.notifyDataSetChanged();
            }
        });
    }

//    private void initializeSearchView() {
//        List<Contact> contacts = getContactList();
//        String[] items = new String[contacts.size()];
//        for (int i = 0; i < contacts.size(); i++) {
//            Contact contact = contacts.get(i);
//            items[i] = contact.title;
//        }
//
//        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this,
//                android.R.layout.simple_dropdown_item_1line, items);
//
////        final SearchView.SearchAutoComplete autoCompleteTextView = (SearchView.SearchAutoComplete) mSearchView
////                .findViewById(R.id.search_src_text);
////        autoCompleteTextView.setAdapter(adapter);
////        autoCompleteTextView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
////            @Override
////            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
////                String item = (String) parent.getItemAtPosition(position);
////                autoCompleteTextView.setText(item);
////                autoCompleteTextView.setSelection(item.length());
////
////                openChat(item);
////            }
////        });
//    }

//    private void openChat(String name) {
//        ChatListFragment one2OneChatListFragment = (ChatListFragment) getSupportFragmentManager()
//                .findFragmentByTag(CHAT_LIST_FRAGMENT);
//        if (one2OneChatListFragment != null) {
//            one2OneChatListFragment.openChat(name);
//        }
//    }

    private Handler mHandler = new Handler() {// handler for commiting fragment after data is loaded

        @Override
        public void handleMessage(android.os.Message msg) {
            if (msg.what == 2) { // refresh chat inside
                refreshOne2OneInside();
                ChatLoad chatload = null;
                if (chatFromNotification != -1) {
                    for (ChatLoad c : mChatList) {
                        if (c.chatId == chatFromNotification) {
                            chatload = c;
                            break;
                        }
                    }
                }
                if (chatload != null) {
                    Helper.hideKeyboard(MainActivity.this);
                    showOne2OneChatFragment(chatload, mChatFlipper, false);
                    mViewPagerMain.setCurrentItem(chatload.type);
                    chatFromNotification = -1;
                }
            }
        }
    };

    private void initChatHeight() {
        mDefaultHeightPx = Converter.dipToPx(getApplicationContext(), DEFAULT_HEIGHT_DP);
        mChatHeightMap = Maps.newHashMap();// chatHeightDataSource.getChatHeightMap();
    }

    private void initChatListFragment() {
        adapter = new MainChatListAdapter(getSupportFragmentManager());
        mViewPagerMain.setAdapter(adapter);
        mViewPagerMain.setOnPageChangeListener(new OnPageChangeListener() {

            @Override
            public void onPageSelected(int arg0) {
                if (arg0 == 1) {
                    chatType = 2;
                }
                if (arg0 == 2) {
                    chatType = 0;
                }
                if (arg0 != 2) {
                    messageColorMap.clear();
                }
                switch (arg0) {
                    case 0:
                        mImgCamera.setImageResource(R.drawable.camera_shutter);
                        mImgBtnOneToOne.setImageResource(R.drawable.ic_one_to_one);
                        mImgBtnPublic.setImageResource(R.drawable.ic_public_group);
                        mImgCamera.callOnClick();
                        break;
                    case 1:
                        mImgCamera.setImageResource(R.drawable.camera_shutter_gray);
                        mImgBtnPublic.setImageResource(R.drawable.ic_public_group_h);
                        mImgBtnOneToOne.setImageResource(R.drawable.ic_one_to_one);
                        break;
                    case 2:
                        mImgCamera.setImageResource(R.drawable.camera_shutter_gray);
                        mImgBtnPublic.setImageResource(R.drawable.ic_public_group);
                        mImgBtnOneToOne.setImageResource(R.drawable.ic_one_to_one_h);
                        break;
                    default:
                        break;
                }
            }

            @Override
            public void onPageScrolled(int arg0, float arg1, int arg2) {
            }

            @Override
            public void onPageScrollStateChanged(int arg0) {
            }
        });
    }

    public void showOne2OneChatFragment(Contact c, View v, boolean isAnim) {
        getActionBar().hide();
        currentChatId = c.chatId;
        final FrameLayout expandedImageView = (FrameLayout) findViewById(R.id.expanded_chatView);
        if (fullChatIndex == 0)
            getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);
        if (isAnim) {
//            zoomImageFromThumb(v, null, c);
//        }
//        else {
            expandedImageView.setVisibility(View.VISIBLE);
            if (mChatFlipperAdapter ==null) {
                mChatFlipperAdapter = new FullChatListBaseAdapter(getSupportFragmentManager(), c);
            } else {
                mChatFlipperAdapter.setContact(c);
            }
            mChatFlipper.setAdapter(mChatFlipperAdapter);
            mChatFlipper.setSelection(fullChatIndex);
        }
    }

    public void showOne2OneChatFragment(ChatLoad c, View view, boolean isAnim) {
        try {
            getActionBar().hide();
            currentChatId = c.chatId;
            final FrameLayout expandedImageView = (FrameLayout) findViewById(R.id.expanded_chatView);
            getWindow().setSoftInputMode(fullChatIndex == 0 ? WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE : WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN);
            if (isAnim) {
//                zoomImageFromThumb(view, c, null);
//            }
//            else {
                expandedImageView.setVisibility(View.VISIBLE);
                if (mChatFlipperAdapter == null) {
                    mChatFlipperAdapter = new FullChatListBaseAdapter(getSupportFragmentManager(), c);
                } else {
                    mChatFlipperAdapter.setChatLoad(c);
                }
                mChatFlipper.setAdapter(mChatFlipperAdapter);
                mChatFlipper.setSelection(fullChatIndex);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void zoomImageFromThumb(final View v, final ChatLoad c, final Contact cc) {
        startBounds = new Rect();
        final Rect finalBounds = new Rect();
        final Point globalOffset = new Point();
        v.getGlobalVisibleRect(startBounds);
        contentFrame.getGlobalVisibleRect(finalBounds, globalOffset);
        startBounds.offset(-globalOffset.x, -globalOffset.y);
        finalBounds.offset(-globalOffset.x, -globalOffset.y);
        final ImageView chatZoomImage = (ImageView) findViewById(R.id.chatZoomImage);
        RelativeLayout.LayoutParams ll = (RelativeLayout.LayoutParams) chatZoomImage.getLayoutParams();
        ll.setMargins(0,startBounds.top,0,0);
        ll.height = startBounds.bottom-startBounds.top;
        chatZoomImage.setLayoutParams(ll);
        chatZoomImage.setImageBitmap(getBitmapFromView(v));
        Animation animZoomIn = AnimationUtils.loadAnimation(getApplicationContext(),
                R.anim.zoom_in);
        v.startAnimation(animZoomIn);
        animZoomIn.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
            }

            @Override
            public void onAnimationEnd(Animation animation) {
                if (c != null) {
                    mChatFlipperAdapter = new FullChatListBaseAdapter(getSupportFragmentManager(), c);
                } else if (cc != null) {
                    mChatFlipperAdapter = new FullChatListBaseAdapter(getSupportFragmentManager(), cc);
                }
                mChatFlipper.setAdapter(mChatFlipperAdapter);
                mChatFlipper.setSelection(fullChatIndex);
                expandedChatFrame.setVisibility(View.VISIBLE);
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });
    }
    public static Bitmap getBitmapFromView(View view) {
        Bitmap returnedBitmap = Bitmap.createBitmap(view.getWidth(), view.getHeight(),Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(returnedBitmap);
        Drawable bgDrawable =view.getBackground();
        if (bgDrawable!=null)
            bgDrawable.draw(canvas);
        else
            canvas.drawColor(Color.WHITE);
        view.draw(canvas);
        return returnedBitmap;
    }

    private void initActionBar() {
        mActionBar = getSupportActionBar();
        mActionBar.setIcon(R.drawable.ic_action_camera);
        mActionBar.setDisplayShowTitleEnabled(false);
        mActionBar.setDisplayShowHomeEnabled(false);
        mActionBar.setDisplayShowCustomEnabled(true);
        final RelativeLayout customActionView = (RelativeLayout) getLayoutInflater().inflate(
                R.layout.action_home, null);
        mActionBar.setCustomView(customActionView);
        customActionView.findViewById(R.id.drawer).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mDrawerLayout.isDrawerOpen(drawerPanel)) {
                    mDrawerLayout.closeDrawer(drawerPanel);
                } else {
                    mDrawerLayout.openDrawer(drawerPanel);
                }
            }
        });
        mImgCamera = (ImageButton) customActionView.findViewById(R.id.home);
        mImgCamera.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(getContext(), QrCodeCaptureActivity.class);
                i.putExtra(IntentKey.CHAT_TYPE, QrCodeCaptureActivity.QODEME_CONTACT);
                startActivityForResult(i, REQUEST_ACTIVITY_SCAN_QR_CODE);
            }
        });
        customActionView.findViewById(R.id.home).setOnLongClickListener(
                new View.OnLongClickListener() {
                    @Override
                    public boolean onLongClick(View v) {
                        Intent i = new Intent(getContext(), QrCodeShowActivity.class);
                        i.putExtra(IntentKey.QR_CODE, QodemePreferences.getInstance().getQrcode());
                        startActivity(i);
                        return true;
                    }
                }
        );

        mImgBtnOneToOne = (ImageButton) customActionView.findViewById(R.id.imgBtn_one2one);

        mImgBtnOneToOne.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                if (chatType == 0)
                    return;
                chatType = 0;
                mImgCamera.setImageResource(R.drawable.camera_shutter_gray);
                mImgBtnOneToOne.setImageResource(R.drawable.ic_one_to_one_h);
                mImgBtnPublic.setBackgroundResource(R.drawable.ic_public_group);
                mViewPagerMain.setCurrentItem(2);
            }
        });

        mImgBtnPublic = (ImageButton) customActionView.findViewById(R.id.imgBtn_public);
        mImgBtnPublic.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                if (chatType == 2)
                    return;
                chatType = 2;
                mImgCamera.setImageResource(R.drawable.camera_shutter_gray);
                mImgBtnOneToOne.setImageResource(R.drawable.ic_one_to_one);
                mImgBtnPublic.setBackgroundResource(R.drawable.ic_public_group_h);
                mViewPagerMain.setCurrentItem(1);
            }
        });
        Button buttonAdd = (Button) customActionView.findViewById(R.id.imgBtn_add);
        buttonAdd.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                if (chatType == 2) {
                    ChatLoad chatLoad = new ChatLoad();
                    chatLoad.chatId = -1;
                    chatLoad.type = chatType;
                    chatLoad.isCreated = false;
                    newChatCreated.put(chatType, chatLoad);
                    refreshOne2OneList();
                } else {
                    isAddContact = chatType != 0 ? true : false;
                    refreshContactList();
                    mContactListAdapter.notifyDataSetChanged();
                    mDrawerLayout.openDrawer(drawerPanel);
                }
                try {
                    publicChatListFragment = (ChatListGroupPublicFragment) adapter.getItem(1);
                    one2OneChatListFragment = (ChatListFragment) adapter.getItem(2);
//                    privateChatListFragment = (ChatListGroupFragment) adapter.getItem(1);

                    if (chatType == 0) {
                        one2OneChatListFragment.setLocationFilter(false);
                        one2OneChatListFragment.setFavoriteFilter(false);
//                    } else if (chatType == 1) {
//                        privateSearchString = "";
//                        privateChatListFragment.setLocationFilter(false);
//                        privateChatListFragment.setFavoriteFilter(false);
                    } else if (chatType == 2) {
                        publicSearchString = "";
                        publicChatListFragment.setLocationFilter(false);
                        publicChatListFragment.setFavoriteFilter(false);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
    }

    public void takePhotoFromGallery() {
        Intent intent = new Intent(Intent.ACTION_PICK,
                android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(intent, REQUEST_ACTIVITY_PHOTO_GALLERY);
    }

    private void dispatchTakePictureIntent(int actionCode) {

        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);

        switch (actionCode) {
            case REQUEST_ACTIVITY_CAMERA:
                File f = null;

                try {
                    f = setUpPhotoFile();
                    mCurrentPhotoPath = f.getAbsolutePath();
                    takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(f));
                } catch (IOException e) {
                    e.printStackTrace();
                    f = null;
                    mCurrentPhotoPath = null;
                }
                break;

            default:
                break;
        } // switch

        startActivityForResult(takePictureIntent, actionCode);
    }

    private File createImageFile() throws IOException {
        // Create an image file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = JPEG_FILE_PREFIX + timeStamp + "_";
        File albumF = getAlbumDir();
        File imageF = File.createTempFile(imageFileName, JPEG_FILE_SUFFIX, albumF);
        return imageF;
    }

    private File setUpPhotoFile() throws IOException {

        File f = createImageFile();
        mCurrentPhotoPath = f.getAbsolutePath();

        return f;
    }

    private File getAlbumDir() {
        File storageDir = null;

        if (Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState())) {

            storageDir = mAlbumStorageDirFactory.getAlbumStorageDir(getString(R.string.album_name));

            if (storageDir != null) {
                if (!storageDir.mkdirs()) {
                    if (!storageDir.exists()) {
                        Log.d("CameraSample", "failed to create directory");
                        return null;
                    }
                }
            }

        } else {
            Log.v(getString(R.string.app_name), "External storage is not mounted READ/WRITE.");
        }

        return storageDir;
    }

    public void takePhoto() {
        CharSequence charSequence[] = {"Camera", "Gallery"};
        new AlertDialog.Builder(this).setTitle("Select")
                .setItems(charSequence, new DialogInterface.OnClickListener() {

                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if (which == 0) {
                            // takePhotoFromCamera();
                            dispatchTakePictureIntent(REQUEST_ACTIVITY_CAMERA);

                        } else {
                            takePhotoFromGallery();
                        }
                        dialog.dismiss();
                    }
                }).create().show();
    }

    private String getPath(Uri uri) {
        System.gc();
        String[] proj = {MediaStore.Images.Media.DATA};
        CursorLoader loader = new CursorLoader(this, uri, proj, null, null, null);
        Cursor cursor = loader.loadInBackground();
        int column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
        cursor.moveToFirst();
        return cursor.getString(column_index);
    }

    private void getMyLocation(Location loc) {
        if (loc != null) {
            final LatLonCity latLonCity = new LatLonCity();
            latLonCity.setLat((int) (loc.getLatitude() * 1E6));
            latLonCity.setLon((int) (loc.getLongitude() * 1E6));
            latLonCity.setLatitude(loc.getLatitude() + "");
            latLonCity.setLongitude(loc.getLongitude() + "");
            new AsyncTask<LatLonCity, Void, String>() {

                @Override
                protected String doInBackground(LatLonCity... params) {
                    try {
                        List<Address> addresses = new Geocoder(getContext(), Locale.ENGLISH)
                                .getFromLocation(latLonCity.getLat() / 1E6,
                                        latLonCity.getLon() / 1E6, 1);
                        if (!addresses.isEmpty()) {
                            return addresses.get(0).getLocality() + ", "
                                    + addresses.get(0).getCountryName();
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }

                    return null;
                }

                @Override
                protected void onPostExecute(String s) {
                    super.onPostExecute(s);
                    if (s != null) {
                        latLonCity.setCity(s);
                        QodemePreferences.getInstance().setLastLocation(latLonCity);
                    }
                }
            }.execute(latLonCity);
        }
    }

    private class ContactListLoader implements LoaderManager.LoaderCallbacks<Cursor> {

        @Override
        public Loader<Cursor> onCreateLoader(int i, Bundle bundle) {
            return new CursorLoader(getActivity(), QodemeContract.Contacts.CONTENT_URI,
                    QodemeContract.Contacts.ContactQuery.PROJECTION, String.format(
                    "%s IN (%d, %d, %d, %d, %d)", QodemeContract.Contacts.CONTACT_STATE,
                    QodemeContract.Contacts.State.APPRUVED,
                    QodemeContract.Contacts.State.INVITATION_SENT,
                    QodemeContract.Contacts.State.INVITED,
                    QodemeContract.Contacts.State.BLOCKED_BY,
                    QodemeContract.Contacts.State.BLOCKED), null,
                    QodemeContract.Contacts.CONTACT_LIST_SORT
            );
        }

        @Override
        public void onLoadFinished(Loader<Cursor> cursorLoader, Cursor cursor) {
            mContacts = ModelHelper.getContactList(cursor);
            refreshContactList();
        }

        @Override
        public void onLoaderReset(Loader<Cursor> cursorLoader) {

        }

    }

    private class ChatLoadListLoader implements LoaderManager.LoaderCallbacks<Cursor> {

        @Override
        public Loader<Cursor> onCreateLoader(int i, Bundle bundle) {
            return new CursorLoader(getActivity(), QodemeContract.Chats.CONTENT_URI,
                    QodemeContract.Chats.ChatQuery.PROJECTION, null, null,
                    QodemeContract.Chats.DEFAULT_SORT);
        }

        @Override
        public void onLoadFinished(Loader<Cursor> cursorLoader, Cursor cursor) {
            mChatList = ModelHelper.getChatList(cursor);
            refreshOne2OneList();
            mHandler.sendEmptyMessage(2);
        }

        @Override
        public void onLoaderReset(Loader<Cursor> cursorLoader) {
        }

    }

    private class MessageListLoader implements LoaderManager.LoaderCallbacks<Cursor> {

        @Override
        public Loader<Cursor> onCreateLoader(int i, Bundle bundle) {
            return new CursorLoader(getActivity(), QodemeContract.Messages.CONTENT_URI,
                    QodemeContract.Messages.Query.PROJECTION,
                    QodemeContract.Messages.MESSAGE_HAS_DELETED + " != 1", null,
                    QodemeContract.Messages.DEFAULT_SORT);
        }

        @Override
        public void onLoadFinished(Loader<Cursor> cursorLoader, Cursor cursor) {
            ModelHelper.MessageStructure ms = ModelHelper.getChatMessagesMap(cursor);
            mChatMessagesMap = ms.getMessageMap();
            mChatNewMessagesMap = ms.getNewMassageMap();
            refreshOne2OneList();
            mHandler.sendEmptyMessage(2);
        }

        @Override
        public void onLoaderReset(Loader<Cursor> cursorLoader) {
        }

    }

    private void refreshContactList() {
        List<Contact> filtredContacts = null;
        filtredContacts = Lists.newArrayList(mContacts);

        Collections.sort(filtredContacts, new Comparator<Contact>() {
            @Override
            public int compare(Contact lhs, Contact rhs) {
                if (lhs.state != rhs.state) {
                    return Integer.valueOf(getStateWeight(lhs.state)).compareTo(
                            getStateWeight(rhs.state));
                }
                return 0;
            }

            int getStateWeight(int state) {
                switch (state) {
                    case QodemeContract.Contacts.State.INVITED:
                        return 0;
                    case QodemeContract.Contacts.State.INVITATION_SENT:
                        return 1;
                    case QodemeContract.Contacts.State.APPRUVED:
                        return 2;
                    case QodemeContract.Contacts.State.BLOCKED:
                        return 3;
                    case QodemeContract.Contacts.State.BLOCKED_BY:
                        return 4;
                }
                return -1;
            }
        });

        // Add contact list headers
        List<ContactListItemEntity> contactListItems = Lists.newArrayList();
        if (!filtredContacts.isEmpty()) {

            contactListItems
                    .add(new ContactListItemEntity(true, filtredContacts.get(0).state, null));
        }
        for (Contact c : filtredContacts) {
            if (contactListItems.get(contactListItems.size() - 1).getState() != c.state) {
                if (c.state != QodemeContract.Contacts.State.BLOCKED)
                    contactListItems.add(new ContactListItemEntity(true, c.state, null));
            }
            contactListItems.add(new ContactListItemEntity(false, c.state, c));
        }

        // Refresh list of contacts
        for (ContactListItemEntity c : contactListItems) {
            c.setChecked(false);
        }
        mContactListAdapter.clear();
        mContactListAdapter.addAll(contactListItems);

        // Create list of approved contacts
        mApprovedContacts = Lists.newArrayList(Iterables.filter(filtredContacts,
                new Predicate<Contact>() {
                    @Override
                    public boolean apply(Contact contact) {
                        return contact.state == QodemeContract.Contacts.State.APPRUVED;
                    }
                }
        ));

        mBlockContacts = Lists.newArrayList(Iterables.filter(filtredContacts,
                new Predicate<Contact>() {
                    @Override
                    public boolean apply(Contact contact) {
                        return contact.state == QodemeContract.Contacts.State.BLOCKED_BY;
                    }
                }
        ));
        mBlockedContacts = Lists.newArrayList(Iterables.filter(filtredContacts,
                new Predicate<Contact>() {
                    @Override
                    public boolean apply(Contact contact) {
                        return contact.state == QodemeContract.Contacts.State.BLOCKED;
                    }
                }
        ));
        mContactInfoUpdated = true;
        refreshOne2OneList();

        Contact contact = null;
        if (chatFromNotification != -1) {
            for (Contact c : mApprovedContacts) {
                if (c.chatId == chatFromNotification) {
                    contact = c;
                    break;
                }
            }

        }
        if (contact == null)
            mHandler.sendEmptyMessage(2);
        else {
            Helper.hideKeyboard(MainActivity.this);
            showOne2OneChatFragment(contact, mChatFlipper, false);
            chatFromNotification = -1;
        }

    }

    private final ContentObserver mContactListObserver = new ContentObserver(new Handler()) {
        @Override
        public void onChange(boolean selfChange) {
            getSupportLoaderManager().restartLoader(1, null, mContactListLoader);
            getSupportLoaderManager().restartLoader(2, null, mMessageListLoader);
        }
    };

    private final ContentObserver mMessageListObjerver = new ContentObserver(new Handler()) {
        @Override
        public void onChange(boolean selfChange) {
            getSupportLoaderManager().restartLoader(2, null, mMessageListLoader);
        }
    };

    private final ContentObserver mChatListObserver = new ContentObserver(new Handler()) {
        @Override
        public void onChange(boolean selfChange) {
            getSupportLoaderManager().restartLoader(3, null, mChatLoadListLoader);
        }
    };

    private void refreshOne2OneList() {
        publicChatListFragment = (ChatListGroupPublicFragment) adapter.getItem(1);
        one2OneChatListFragment = (ChatListFragment) adapter.getItem(2);

        one2OneChatListFragment.updateUi();
        publicChatListFragment.updateUi();

        adapter.notifyDataSetChanged();
    }

    private void refreshOne2OneInside() {
        try {
            if (chatType == 0) {
                ChatInsideFragment one2OneChatInsideFragment = null;
                ChatProfileFragment chatProfileFragment = null;
                ChatPhotosFragment chatPhotosFragment = null;
                if (mChatFlipperAdapter != null && !getActionBar().isShowing()) {
                    one2OneChatInsideFragment = (ChatInsideFragment) mChatFlipperAdapter.getItem(0);
                    chatProfileFragment = (ChatProfileFragment) mChatFlipperAdapter.getItem(1);
                    chatPhotosFragment = (ChatPhotosFragment) mChatFlipperAdapter.getItem(2);
                }
                if (chatProfileFragment != null)
                    chatProfileFragment.setData();
                if (chatPhotosFragment != null)
                    chatPhotosFragment.updateUi();
                if (one2OneChatInsideFragment != null) {
                    one2OneChatInsideFragment.updateUi();
                    if (mContactInfoUpdated) {
                        long chatId = one2OneChatInsideFragment.getChatId();
                        Contact c = findContactEntityByChatId(chatId);
                        mContactInfoUpdated = false;
                        showOne2OneChatFragment(c, mChatFlipper, false);
                    }
                    one2OneChatInsideFragment.updateUi();
                    chatProfileFragment.setData();
                    chatPhotosFragment.updateUi();
                    mChatFlipper.setSelection(fullChatIndex);

                }
            } else {
                ChatInsideGroupFragment groupChatInsideFragment = null;
                ChatGroupProfileFragment chatGroupProfileFragment = null;
                ChatGroupPhotosFragment chatGroupPhotosFragment = null;

                if (mChatFlipperAdapter != null && !getActionBar().isShowing()) {
                    groupChatInsideFragment = (ChatInsideGroupFragment) mChatFlipperAdapter.getItem(0);
                    chatGroupProfileFragment = (ChatGroupProfileFragment) mChatFlipperAdapter.getItem(1);
                    chatGroupPhotosFragment = (ChatGroupPhotosFragment) mChatFlipperAdapter.getItem(2);
                }
                if (groupChatInsideFragment != null) {

                    try {
                        long chatId = groupChatInsideFragment.getChatId();
                        ChatLoad chatLoad1 = getChatLoad(chatId);
                        showOne2OneChatFragment(chatLoad1, mChatFlipper, false);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }

                    if (chatGroupProfileFragment != null) {
                        List<ChatLoad> chatLoads = getChatList(chatType);
                        for (ChatLoad chatLoad : chatLoads) {
                            if (chatLoad.chatId == chatGroupProfileFragment.getChatload().chatId) {
                                groupChatInsideFragment.setChatLoad(chatLoad);
                                chatGroupProfileFragment.setChatload(chatLoad);
                                chatGroupProfileFragment.setChatload(chatLoad);
                                if (chatGroupPhotosFragment != null) {
                                    chatGroupPhotosFragment.setChatLoad(chatLoad);
                                    chatGroupPhotosFragment.updateUi();
                                }
                                break;
                            }
                        }
                        chatGroupProfileFragment.setData();
                    }
                    groupChatInsideFragment.updateUi();
                    if (chatGroupPhotosFragment != null) {
                        chatGroupPhotosFragment.updateUi();
                    }
                    mChatFlipper.setSelection(fullChatIndex);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private Contact findContactEntityByChatId(long chatId) {
        for (Contact c : mContacts) {
            if (chatId == c.chatId) {
                return c;
            }
        }
        return null;
    }

    private void showMessage(String message) {
        try {
            new AlertDialog.Builder(getContext()).setTitle("Attention").setMessage(message)
                    .setCancelable(true)
                    .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                        }
                    }).create().show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private SyncStatusObserver mSyncStatusObserver = new SyncStatusObserver() {
        /** Callback invoked with the sync adapter status changes. */
        @Override
        public void onStatusChanged(int which) {
            runOnUiThread(new Runnable() {
                /**
                 * The SyncAdapter runs on a background thread. To update the
                 * UI, onStatusChanged() runs on the UI thread.
                 */
                @Override
                public void run() {
                    Account account = GenericAccountService.GetAccount();
                    if (account == null) {
                        return;
                    }
                }
            });
        }
    };

    private void clearActivityCache() {
        mContacts = Lists.newArrayList();
        mApprovedContacts = Lists.newArrayList();
    }

    public void setCurrentChatId(long currentChatId) {
        this.currentChatId = currentChatId;
    }

    public void callColorPicker(Contact ce, int type) {
        Intent i = new Intent(getContext(), ContactDetailsActivity.class);
        i.putExtra(QodemeContract.Contacts._ID, ce._id);
        i.putExtra(QodemeContract.Contacts.CONTACT_TITLE, ce.title);
        i.putExtra(QodemeContract.Contacts.CONTACT_COLOR, ce.color);
        i.putExtra(QodemeContract.Contacts.UPDATED, ce.updated);
        i.putExtra("color_type", type);
        startActivityForResult(i, REQUEST_ACTIVITY_CONTACT_DETAILS);
    }

    public void callColorPicker(ChatLoad ce, int type) {
        Intent i = new Intent(getContext(), ContactDetailsActivity.class);
        i.putExtra(QodemeContract.Contacts._ID, ce._id);
        i.putExtra(QodemeContract.Contacts.CONTACT_TITLE, ce.title);
        i.putExtra(QodemeContract.Contacts.CONTACT_COLOR, ce.color);
        i.putExtra(QodemeContract.Contacts.UPDATED, ce.updated);
        i.putExtra("color_type", type);
        startActivityForResult(i, REQUEST_ACTIVITY_CONTACT_DETAILS);
    }

    @Override
    public Contact getContact(String qrString) {
        Contact contact = null;
        if (qrString != null) {
            List<Contact> contacts = Lists.newArrayList();
            if (mApprovedContacts != null)
                contacts.addAll(mApprovedContacts);
            if (mBlockedContacts != null)
                contacts.addAll(mBlockedContacts);
            if (contacts != null)
                for (Contact c : contacts) {
                    if (c.qrCode.equals(qrString)) {
                        contact = c;
                        break;
                    }
                }
            if (contact == null && mBlockContacts != null)
                for (Contact c : mBlockContacts) {
                    if (c.qrCode.equals(qrString)) {
                        contact = c;
                        break;
                    }
                }
        }
        return contact;
    }

    public void addMemberInExistingChat() {
        isAddMemberOnExistingChat = true;
        if (chatType != 0) {
            isAddContact = true;
        } else {
            isAddContact = false;
        }
        buttonshare.setVisibility(View.VISIBLE);
        buttonMore.setVisibility(View.GONE);
        buttonInvite.setVisibility(View.GONE);
        refreshContactList();
        mContactListAdapter.notifyDataSetChanged();
        mDrawerLayout.openDrawer(drawerPanel);
    }

    private void addMemberInChat(final List<Contact> contactsList) {
        isAddMemberOnExistingChat = false;
        Log.d("contact add in public", contactsList.get(0).title + "");
        for (Contact contact : contactsList) {
            final String qr = contact.qrCode;
            Cursor cursor = getContentResolver().query(QodemeContract.Chats.CONTENT_URI,
                    QodemeContract.Chats.ChatQuery.PROJECTION,
                    QodemeContract.Chats.CHAT_ID + "=" + currentChatId, null, null);
            String memberString = "";
            int numberOfMember = 1;
            if (cursor != null && cursor.moveToFirst()) {
                memberString = cursor.getString(QodemeContract.Chats.ChatQuery.CHAT_MEMBER_QRCODES);
                numberOfMember = cursor
                        .getInt(QodemeContract.Chats.ChatQuery.CHAT_NUMBER_OF_MEMBER);
            }
            if (memberString == null || memberString.equals(""))
                memberString = qr;
            else {
                memberString += "," + qr;
            }
            if (numberOfMember == 0) {
                numberOfMember = 2;
            } else {
                numberOfMember++;
            }
            ContentValues contentValues = new ContentValues();
            contentValues.put(QodemeContract.Chats.CHAT_MEMBER_QRCODES, memberString);
            contentValues.put(QodemeContract.Chats.CHAT_NUMBER_OF_MEMBER, numberOfMember);
            getContentResolver().update(QodemeContract.Chats.CONTENT_URI, contentValues,
                    QodemeContract.Chats.CHAT_ID + "=" + currentChatId, null);
            RestAsyncHelper.getInstance().chatAddMember(currentChatId, contact.qrCode,
                    new RestListener<ChatAddMemberResponse>() {

                        @Override
                        public void onResponse(ChatAddMemberResponse response) {
                            Log.d("Chat add in public ", "Chat add mem "
                                    + response.getChat().getId());
                        }

                        @Override
                        public void onServiceError(RestError error) {
                            Log.d("Error", "Chat add member");
                        }
                    }
            );
        }
    }

    public void setChatInfo(long chatId, String title, Integer color, String tag, String desc,
                            String status, Integer isLocked, String chat_title, String latitude, String longitude) {
        RestAsyncHelper.getInstance().chatSetInfo(chatId, title, color, tag, desc, isLocked,
                status, chat_title, latitude, longitude, new RestListener<VoidResponse>() {

                    @Override
                    public void onResponse(VoidResponse response) {
                        Toast.makeText(getActivity(), "Profile updated", Toast.LENGTH_LONG).show();
                    }

                    @Override
                    public void onServiceError(RestError error) {
                        Log.d("Error", error.getMessage() + "");
                        Toast.makeText(getActivity(), "Connection error", Toast.LENGTH_LONG).show();
                    }
                }
        );
    }

    @Override
    public ImageFetcher getImageFetcher() {
        return mImageFetcher;
    }

    @Override
    public int getChatType(long chatId) {
        int chatType = 0;
        if (mChatList != null) {
            for (ChatLoad chatLoad : mChatList) {
                if (chatLoad.chatId == chatId) {
                    chatType = chatLoad.type;
                }
            }
        }
        return chatType;
    }

    @Override
    public ChatLoad getChatLoad(long chatId) {
        List<ChatLoad> chatLoads = getChatList(chatType);

        if (chatLoads != null) {
            for (ChatLoad chatLoad : chatLoads)
                if (chatLoad.chatId == chatId)
                    return chatLoad;
        }
        return null;

    }

    public void fullImageView(View v, Message msg) {
        final Intent i = new Intent(getActivity(), ImageDetailActivity.class);
        i.putExtra(ImageDetailActivity.EXTRA_IMAGE, msg.photoUrl);
        i.putExtra("flag", msg.is_flagged);
        i.putExtra("message_id", msg.messageId);
        if (Utils.hasJellyBean()) {
            ActivityOptions options = ActivityOptions.makeScaleUpAnimation(v, 0, 0, v.getWidth(),
                    v.getHeight());
            getActivity().startActivity(i, options.toBundle());
        } else {
            startActivity(i);
        }
    }

    public void searchChats(String searchString, final int type, final int pageNo,
                            final LoadMoreChatListener chatListener) {
        if (type == 2) {
            setPublicSearch(true);
        } else if (type == 1) {
//            setPrivateSearch(true);
            if (mChatListSearchPrivate == null || pageNo == 1)
                mChatListSearchPrivate = Lists.newArrayList();
            if (mChatList != null) {
                for (ChatLoad chatLoad : mChatList) {
                    if (chatLoad.title.contains(searchString)) {
                        mChatListSearchPrivate.add(chatLoad);
                    }
                }
                refreshOne2OneList();
            }
            return;
        } else if (type == 0) {
            setOneToOneSearch(true);
            if (mChatListSearchOneToOne == null || pageNo == 1)
                mChatListSearchOneToOne = Lists.newArrayList();

            if (mApprovedContacts != null) {
                for (Contact c : mApprovedContacts) {
                    if (c.title.contains(searchString)) {
                        mChatListSearchOneToOne.add(c);
                    }
                }
                refreshOne2OneList();
            }
            return;
        }
        RestAsyncHelper.getInstance().lookup(searchString, type, pageNo,
                new RestListener<LookupResponse>() {

                    @Override
                    public void onResponse(LookupResponse response) {
                        if (response.getChatList() != null) {
                            if (type == 2) {
                                if (mChatListSearchPublic == null || pageNo == 1)
                                    mChatListSearchPublic = Lists.newArrayList();
                                for (LookupChatEntity entity : response.getChatList()) {
                                    ChatLoad chatLoad = new ChatLoad();
                                    chatLoad.title = entity.getTitle();
                                    chatLoad.chatId = entity.getId();
                                    chatLoad.qrcode = entity.getQrcode();
                                    chatLoad.tag = entity.getTags();
                                    chatLoad.status = entity.getStatus();
                                    chatLoad.description = entity.getDescription();
                                    chatLoad.number_of_likes = entity.getNumber_of_likes();
                                    chatLoad.number_of_members = entity.getNumber_of_member();
                                    chatLoad.number_of_members = entity.getNumber_of_member();
                                    chatLoad.latitude = entity.getLatitude();
                                    chatLoad.longitude = entity.getLongitude();
                                    chatLoad.is_favorite = entity.getIs_favorite();
                                    chatLoad.type = 2;
                                    chatLoad.created = entity.getCreated();
                                    chatLoad.messages = entity.getMessages();
                                    chatLoad.isSearchResult = true;

                                    ChatLoad chatLoad2 = null;
                                    if (mChatList != null) {
                                        for (ChatLoad c : mChatList) {
                                            if (c.chatId == entity.getId()) {
                                                chatLoad.isSearchResult = false;
                                                chatLoad2 = c;
                                                break;
                                            }
                                        }
                                    }
                                    if (chatLoad2 == null)
                                        mChatListSearchPublic.add(chatLoad);
                                }
                                chatListener.onSearchResult(response.getChatList().size(), 1);
                                refreshOne2OneList();
                            }
                        } else {
                            chatListener.onSearchResult(0, 2);
                            Log.d("lookup", "null response");
                        }
                    }

                    @Override
                    public void onServiceError(RestError error) {
                        chatListener.onSearchResult(0, 2);
                        Toast.makeText(getActivity(), error.getServerMsg(), Toast.LENGTH_SHORT)
                                .show();
                    }
                }
        );
    }

    public void setPublicSearch(boolean isPublicSearch) {
        this.isPublicSearch = isPublicSearch;
    }

    public boolean isPublicSearch() {
        return isPublicSearch;
    }

    public boolean isActive() {
        return mActive;
    }


    public void setOneToOneSearch(boolean isOneToOneSearch) {
        this.isOneToOneSearch = isOneToOneSearch;
    }

    public boolean isOneToOneSearch() {
        return isOneToOneSearch;
    }

    public void setPublicSearchString(String publicSearchString) {
        this.publicSearchString = publicSearchString;
    }

    public void clearPublicSearch() {
        this.mChatListSearchPublic.clear();
    }

    public String getPublicSearchString() {
        return publicSearchString;
    }

    public void setOneToOneSearchString(String oneToOneSearchString) {
        this.oneToOneSearchString = oneToOneSearchString;
    }

    public String getOneToOneSearchString() {
        return oneToOneSearchString;
    }

    public void setCurrentLocation(Location currentLocation) {
        this.currentLocation = currentLocation;
    }

    public Location getCurrentLocation() {
        return currentLocation;
    }

    private int GetEventForChatEvents() {
        return 0;
    }

    private int GetEventForUserStartedTypingMessage() {
        return 1;
    }

    private int GetEventForUserStoppedTypingMessage() {
        return 2;
    }

    public void receiveOtherUserStoppedTypingEvent(long chatId) {
        ChatLoad chatLoad = getChatLoad(chatId);
        if (chatLoad != null) {
            chatLoad.isTyping = false;
//            privateChatListFragment = (ChatListGroupFragment) adapter.getItem(1);
            publicChatListFragment = (ChatListGroupPublicFragment) adapter.getItem(1);
            one2OneChatListFragment = (ChatListFragment) adapter.getItem(2);

            if (chatLoad.type == 0)
                one2OneChatListFragment.notifyUi(chatId, chatLoad);
//            else if (chatLoad.type == 1)
//                privateChatListFragment.notifyUi(chatId, chatLoad);
        }
    }

    public void receiveOtherUserStartedTypingEvent(long chatId) {
        ChatLoad chatLoad = getChatLoad(chatId);
        if (chatLoad != null) {
            if (!chatLoad.isTyping)
                chatLoad.isTyping = true;
//            privateChatListFragment = (ChatListGroupFragment) adapter.getItem(1);
            publicChatListFragment = (ChatListGroupPublicFragment) adapter.getItem(1);
            one2OneChatListFragment = (ChatListFragment) adapter.getItem(2);

            if (chatLoad.type == 0)
                one2OneChatListFragment.notifyUi(chatId, chatLoad);
//            else if (chatLoad.type == 1)
//                privateChatListFragment.notifyUi(chatId, chatLoad);
        }
    }



    BroadcastReceiver broadcastReceiverForDeleteChat = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {

            if (intent.getAction().equals(DELETE_BRODCAST_ACTION)) {
                if (!mActionBar.isShowing()) {
                    try {
                        long chat_id = intent.getLongExtra("chat_id", -1);
                        if (chat_id != -1 && chat_id == currentChatId)
                            onBackPressed();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        }
    };

    @Override
    protected void onNewIntent(Intent intent) {
        Bundle bundle = intent.getExtras();
        if (bundle != null) {
            chatFromNotification = bundle.getLong("chat_id");

            if (mChatList != null)
                mHandler.sendEmptyMessage(2);
        }
    }

    private void email() {

        ChatLoad chatLoad = getChatLoad(currentChatId);
        if (chatLoad != null) {
            progressDialog = ProgressDialog.show(this, "", "Sharing...");
            ShareAsyncTask asyncTask = new ShareAsyncTask(chatLoad);
            asyncTask.execute("");
        }
    }

    class ShareAsyncTask extends AsyncTask<String, String, String> {

        ChatLoad chatLoad = null;
        String data = "";
        String path;

        public ShareAsyncTask(ChatLoad chatLoad) {
            this.chatLoad = chatLoad;
        }

        @Override
        protected String doInBackground(String... params) {
            JSONObject jsonObject = new JSONObject();
            try {
                jsonObject.put(IntentKey.QR_CODE, chatLoad.qrcode);
                jsonObject.put(IntentKey.CHAT_TYPE, 2);
                jsonObject.put(IntentKey.CONTACT_NAME, chatLoad.title);
                jsonObject.put(IntentKey.CHAT_ID, chatLoad.chatId);
            } catch (Exception e) {
                e.printStackTrace();
            }
            Bitmap mBitmap = QrUtils.encodeQrCode(
                    (ApplicationConstants.QR_CODE_CONTACT_PREFIX + jsonObject), 500, 500,
                    Color.BLACK, Color.WHITE);
            path = MediaStore.Images.Media
                    .insertImage(getContentResolver(), mBitmap, "title", null);

            List<Message> messages = getChatMessages(chatLoad.chatId);
            int total = 0;
            int photo = 0;
            if (messages != null) {
                total = messages.size();
                for (Message me : messages)
                    if (me.hasPhoto == 1)
                        photo++;
            } else {
                if (chatLoad.isSearchResult) {
                    Message[] messages2 = chatLoad.messages;
                    if (messages2 != null) {
                        total = messages2.length;
                        for (Message me : messages2)
                            if (me.hasPhoto == 1)
                                photo++;
                    }
                }
            }
            int member = chatLoad.number_of_members == 0 ? 1 : chatLoad.number_of_members;

            data = "<html><body><h1>Join the Conversation</h1><hr><br><p>The conversation "
                    + chatLoad.title
                    + " has been shared with you. Scan the attached code to join the conversation.</p><br><br><h2>"
                    + chatLoad.title
                    + "</h2><br><p>"
                    + member
                    + " members, "
                    + total
                    + " messages, "
                    + photo
                    + " photos</p>"
                    + "<a href=\"code:other/parameter\"> View Conversation </a> <br><hr><h2>What is Code!?</h2><br><p>Lorem ipsum dolor sit amet, sldfha consectetur adipisicing elit, sed do eiusmod tempor incididunt ut lab et dolore magna eliqua.</p><br><h2>Available On</h2><br><a href=\"http://play.google.com/store/apps/details?id=com.blulabellabs.code\"> Google Play </a></body></html>";
            return null;
        }

        @Override
        protected void onPostExecute(String result) {
            if (progressDialog != null && progressDialog.isShowing())
                progressDialog.dismiss();
            if (path == null) {
                showMessage(getString(R.string.alert_no_access_to_external_storage));
                return;
            }
            Uri screenshotUri = Uri.parse(path);
            final Intent emailIntent = new Intent(Intent.ACTION_SEND);
            emailIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            emailIntent.putExtra(Intent.EXTRA_STREAM, screenshotUri);
            emailIntent.setType("image/png");
            emailIntent.putExtra(Intent.EXTRA_TEXT, Html.fromHtml(data));
            emailIntent.putExtra(Intent.EXTRA_SUBJECT, "Code! contact");
            startActivity(Intent.createChooser(emailIntent, "Send email using"));
        }

    }

    private Activity getActivity() {
        return this;
    }

    public Context getContext() {
        return this;
    }

    public Application getMyApplication() {
        return (Application) getApplication();
    }


    LocationResult locationResult = new LocationResult() {

        @Override
        public void gotLocation(Location location) {
            if (location != null) {
                getMyLocation(location);
                setCurrentLocation(location);
            }
        }
    };

    @Override
    public void onDetachedFromWindow() {
        try {
            super.onDetachedFromWindow();
        } catch(IllegalArgumentException iae) {
            iae.printStackTrace();
        }
    }

}
