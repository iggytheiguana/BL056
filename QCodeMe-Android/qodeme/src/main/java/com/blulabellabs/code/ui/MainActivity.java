package com.blulabellabs.code.ui;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
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
import java.util.regex.Pattern;

import org.json.JSONException;
import org.json.JSONObject;

import android.accounts.Account;
import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.annotation.SuppressLint;
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
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Point;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.provider.MediaStore;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.support.v4.widget.DrawerLayout;
import android.support.v4.widget.DrawerLayout.DrawerListener;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.SearchView;
import android.text.Html;
import android.text.TextUtils;
import android.util.Base64;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.WindowManager;
import android.view.animation.DecelerateInterpolator;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.VolleyError;
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
import com.blulabellabs.code.core.io.responses.ChatLoadResponse;
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
import com.blulabellabs.code.ui.common.FullChatListAdapter;
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
import com.blulabellabs.code.ui.one2one.ChatListGroupFragment;
import com.blulabellabs.code.ui.one2one.ChatListGroupPublicFragment;
import com.blulabellabs.code.ui.one2one.ChatPhotosFragment;
import com.blulabellabs.code.ui.one2one.ChatProfileFragment;
import com.blulabellabs.code.ui.preferences.SettingsActivity;
import com.blulabellabs.code.ui.qr.QrCodeCaptureActivity;
import com.blulabellabs.code.ui.qr.QrCodeShowActivity;
import com.blulabellabs.code.utils.AnalyticsHelper;
import com.blulabellabs.code.utils.Converter;
import com.blulabellabs.code.utils.DbUtils;
import com.blulabellabs.code.utils.Fonts;
import com.blulabellabs.code.utils.Helper;
import com.blulabellabs.code.utils.LatLonCity;
import com.blulabellabs.code.utils.MyLocation;
import com.blulabellabs.code.utils.QrUtils;
import com.blulabellabs.code.utils.MyLocation.LocationResult;
import com.blulabellabs.code.utils.NullHelper;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesClient;
import com.google.android.gms.location.LocationClient;
import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.primitives.Longs;

import de.tavendo.autobahn.WebSocketConnection;
import de.tavendo.autobahn.WebSocketException;
import de.tavendo.autobahn.WebSocketHandler;

@SuppressLint({ "HandlerLeak", "SimpleDateFormat" })
@SuppressWarnings({ "unused", "unchecked", "rawtypes" })
public class MainActivity extends BaseActivity implements
		ChatListFragment.One2OneChatListFragmentCallback,
		ChatInsideFragment.One2OneChatInsideFragmentCallback,
		GooglePlayServicesClient.ConnectionCallbacks,
		GooglePlayServicesClient.OnConnectionFailedListener {

	private static final int REQUEST_ACTIVITY_SCAN_QR_CODE = 2;
	private static final int REQUEST_ACTIVITY_CONTACT_DETAILS = 3;
	private static final int REQUEST_ACTIVITY_PHOTO_GALLERY = 4;
	private static final int REQUEST_ACTIVITY_CAMERA = 5;
	private static final int REQUEST_ACTIVITY_MORE = 6;
	private static final int REQUEST_ACTIVITY_SHOW_QR_CODE = 7;
	public static final int REQUEST_ACTIVITY_SCAN_QR_CODE_2 = 8;

	private static final String CHAT_LIST_FRAGMENT = "chat_list_fragment";
	private static final String CHAT_LIST_PRIVATE_FRAGMENT = "chat_list_private_fragment";
	private static final String CHAT_LIST_PUBLIC_FRAGMENT = "chat_list_public_fragment";

	private static final String CHAT_INSIDE_FRAGMENT = "chat_inside_fragment";
	private static final int DEFAULT_HEIGHT_DP = 200;
	public static final String DELETE_BRODCAST_ACTION = "delete_broadcast_action";
	public static final String CHAT_ADDED_BRODCAST_ACTION = "chat_added_broadcast_action";

	private int mDefaultHeightPx;
	private DrawerLayout mDrawerLayout;
	private ListView mContactListView;
	public ActionBar mActionBar;
	private boolean mActive; // Activity active
	private SearchView mSearchView;
	private MenuItem mSearchMenuItem;
	private boolean mIsSearchActive;
	private String mSearchText;
	private long currentChatId = -1;
	private int chatType = 0;
	private int fullChatIndex = 0;
	private boolean isAddMemberOnExistingChat = false;

	// Fonts cache
	private Map<Fonts, Typeface> fontMap = new HashMap();

	// Memory cache
	private MenuListAdapter<ContactListItemEntity> mContactListAdapter;
	// private MenuListAddToChatAdapter<ContactListItemEntity>
	// mContactListAddChatAdapter;
	private Map<Long, List<Message>> mChatMessagesMap = Maps.newHashMap();
	private Map<Long, Long> mLastMessageInChatMap;
	private Map<Long, Integer> mChatHeightMap;
	private boolean mContactInfoUpdated;
	private List<Contact> mContacts;
	private List<Contact> mApprovedContacts;
	private List<Contact> mBlockContacts;
	private List<Contact> mBlockedContacts;
	private List<ChatLoad> mChatList;
	private List<ChatLoad> mChatListSearchPublic = Lists.newArrayList();
	private List<ChatLoad> mChatListSearchPrivate = Lists.newArrayList();
	private List<Contact> mChatListSearchOneToOne = Lists.newArrayList();
	private Map<Long, Integer> mChatNewMessagesMap;

	private ContactListLoader mContactListLoader;
	private MessageListLoader mMessageListLoader;
	private ChatLoadListLoader mChatLoadListLoader;
	private boolean mIsFirstResume;
	private LocationClient mLocationClient;
	// private Location mCurrentLocation;
	private String mSearchFilter;
	private boolean mKeyboardActive;
	private boolean isAddContact = false;
	private boolean isPublicSearch = false;
	private boolean isPrivateSearch = false;
	private boolean isOneToOneSearch = false;
	private String publicSearchString = "";
	private String privateSearchString = "";
	private String oneToOneSearchString = "";
	private Location currentLocation;
	public static boolean isKeyboardVisible = false;

	private ImageButton mImgBtnOneToOne, mImgBtnPrivate, mImgBtnPublic;

	private ChatListFragment one2OneChatListFragment;
	private ChatListGroupFragment privateChatListFragment;
	public ChatListGroupPublicFragment publicChatListFragment;
	public HashMap<Long, Integer> messageColorMap = new HashMap<Long, Integer>();
	Button buttonshare, buttonMore, buttonInvite;

	/*
	 * Animator for Zoom chat view
	 */
	private Animator mCurrentAnimator;
	/**
	 * The system "short" animation time duration, in milliseconds. This
	 * duration is ideal for subtle animations or animations that occur very
	 * frequently.
	 */
	private int mShortAnimationDuration;
	private FullChatListAdapter mPagerAdapter;

	private ViewPager mViewPager, mViewPagerMain;

	/**
	 * Handle to a SyncObserver. The ProgressBar element is visible until the
	 * SyncObserver reports that the sync is complete.
	 * <p/>
	 * <p>
	 * This allows us to delete our SyncObserver once the application is no
	 * longer in the foreground.
	 */
	private Object mSyncObserverHandle;

	/**
	 * Load Images by url parameter for cache
	 */
	private static final String TAG = "ImageGridFragment";
	private static final String IMAGE_CACHE_DIR = "thumbs";

	private int mImageThumbSize;
	private int mImageThumbSpacing;
	private ImageFetcher mImageFetcher;

	private static final String JPEG_FILE_PREFIX = "IMG_";
	private static final String JPEG_FILE_SUFFIX = ".jpg";
	private AlbumStorageDirFactory mAlbumStorageDirFactory = null;
	private MyLocation myLocation;
	private final WebSocketConnection mConnection = new WebSocketConnection();

	private long chatFromNotification = -1;
	public ArrayList<Long> refressedChatId = Lists.newArrayList();
	public static boolean isKeyboardHide = false;
	private ProgressDialog progressDialog;
	@SuppressLint("UseSparseArrays")
	public HashMap<Integer, ChatLoad> newChatCreated = new HashMap<Integer, ChatLoad>();
	private List<Contact> selectedContact = Lists.newArrayList();

	/**
	 * Called when the activity is first created.
	 */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		AnalyticsHelper.onCreateActivity(this);
		setContentView(R.layout.activity_main);
		mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
		// loadData();

		mViewPagerMain = (ViewPager) findViewById(R.id.pager_main);

		initImageFetcher();
		initActionBar();
		initContactsList();
		// TODO remove temporary solution
		((Application) getApplication()).setMainActivity(this);
		initChatHeight();
		initChatListFragment();
		// randomColorGenerator = new RandomColorGenerator();
		if (savedInstanceState != null) {
			// Then the application is being reloaded
			mSearchText = savedInstanceState.getString("searchText");
		}
		mIsFirstResume = true;
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
	}

	private void initImageFetcher() {
		final DisplayMetrics displayMetrics = new DisplayMetrics();
		getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
		final int height = displayMetrics.heightPixels;
		final int width = displayMetrics.widthPixels;

		final int longest = (height > width ? height : width) / 2;

		mImageThumbSize = getResources().getDimensionPixelSize(R.dimen.image_thumbnail_size);
		mImageThumbSpacing = getResources().getDimensionPixelSize(R.dimen.image_thumbnail_spacing);

		ImageCache.ImageCacheParams cacheParams = new ImageCache.ImageCacheParams(getActivity(),
				IMAGE_CACHE_DIR);

		cacheParams.setMemCacheSizePercent(0.25f); // Set memory cache to 25% of
													// app memory

		// The ImageFetcher takes care of loading images into our ImageView
		// children asynchronously
		mImageFetcher = new ImageFetcher(this, longest);// mImageThumbSize
		// mImageFetcher.setLoadingImage(R.drawable.empty_photo);
		mImageFetcher.addImageCache(getSupportFragmentManager(), cacheParams);
	}

	private void initFullChatLayout() {
		mShortAnimationDuration = getResources().getInteger(android.R.integer.config_longAnimTime);

		mViewPager = (ViewPager) findViewById(R.id.pager);
		mViewPager.setOnPageChangeListener(new OnPageChangeListener() {

			@Override
			public void onPageSelected(int arg0) {
				fullChatIndex = arg0;
				if (fullChatIndex == 0)
					getWindow().setSoftInputMode(
							WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);
				else
					getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN);
				Helper.hideKeyboard(MainActivity.this);
			}

			@Override
			public void onPageScrolled(int arg0, float arg1, int arg2) {
			}

			@Override
			public void onPageScrollStateChanged(int arg0) {
			}
		});
	}

	public class ChatListAdapter extends FragmentPagerAdapter {
		// private ArrayList<Fragment> mFragmentsList = new
		// ArrayList<Fragment>();

		public ChatListAdapter(FragmentManager fm) {
			super(fm);

			// for (int i = 0; i < 3; i++)
			// getmFragmentsList().add(ChatListFragment.getInstance());
			// getmFragmentsList().add(ChatListGroupFragment.getInstance());
			// getmFragmentsList().add(ChatListGroupPublicFragment.getInstance());

		}

		@Override
		public int getCount() {
			return 3;
		}

		@Override
		public Fragment getItem(int position) {
			switch (position) {
			case 0:
				return ChatListFragment.getInstance();
			case 1:
				return ChatListGroupFragment.getInstance();
			case 2:
				return ChatListGroupPublicFragment.getInstance();
			default:
				return ChatListGroupPublicFragment.getInstance();
			}
		}

	}

	private void initKeyboardListener() {
		mDrawerLayout.getViewTreeObserver().addOnGlobalLayoutListener(
				new ViewTreeObserver.OnGlobalLayoutListener() {
					@Override
					public void onGlobalLayout() {
						int heightDiff = mDrawerLayout.getRootView().getHeight()
								- mDrawerLayout.getHeight();
						mKeyboardActive = (heightDiff > 100);
					}
				});
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
				// mContactListView.setAdapter(mContactListAdapter);
			}
		});
	}

	private Handler mHandler = new Handler() // handler for commiting fragment
												// after data is loaded
	{
		@Override
		public void handleMessage(android.os.Message msg) {
			if (msg.what == 2) // refresh chat inside
			{
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
					showOne2OneChatFragment(chatload, true, mViewPager, false);

					mViewPagerMain.setCurrentItem(chatload.type);
					chatFromNotification = -1;
				}
			}
		}
	};
	private String mCurrentPhotoPath;

	private void initChatHeight() {
		mDefaultHeightPx = Converter.dipToPx(getApplicationContext(), DEFAULT_HEIGHT_DP);
		// chatHeightDataSource = new ChatHeightDataSource(this);
		// chatHeightDataSource.open();
		mChatHeightMap = Maps.newHashMap();// chatHeightDataSource.getChatHeightMap();
		// chatHeightDataSource.close();
	}

	MainChatListAdapter adapter;

	private void initChatListFragment() {
		// ChatListFragment one2OneChatListFragment = new ChatListFragment();
		// FragmentTransaction transaction =
		// getSupportFragmentManager().beginTransaction();
		// transaction.add(R.id.content_frame, one2OneChatListFragment,
		// CHAT_LIST_FRAGMENT);
		// transaction.commit();
		// one2OneChatListFragment = ChatListFragment.getInstance();
		// privateChatListFragment = ChatListGroupFragment.getInstance();
		// publicChatListFragment = ChatListGroupPublicFragment.getInstance();

		adapter = new MainChatListAdapter(getSupportFragmentManager());
		// ArrayList<Fragment> arrayList = new ArrayList<Fragment>();
		// arrayList.add(one2OneChatListFragment);
		// arrayList.add(privateChatListFragment);
		// arrayList.add(publicChatListFragment);
		// adapter.setmFragmentsList(arrayList);
		// one2OneChatListFragment = (ChatListFragment) adapter.getItem(0);
		// privateChatListFragment = (ChatListGroupFragment) adapter.getItem(1);
		// publicChatListFragment = (ChatListGroupPublicFragment)
		// adapter.getItem(2);
		mViewPagerMain.setAdapter(adapter);
		mViewPagerMain.setOnPageChangeListener(new OnPageChangeListener() {

			@Override
			public void onPageSelected(int arg0) {
				// Toast.makeText(getActivity(), "" + arg0,
				// Toast.LENGTH_SHORT).show();
				chatType = arg0;
				if (arg0 != 2) {
					messageColorMap.clear();
				}
				Bitmap oneToone;
				Bitmap priateGroup;
				Bitmap publicGroup;

				switch (arg0) {
				case 0:
					oneToone = BitmapFactory.decodeResource(getResources(),
							R.drawable.ic_one_to_one_h);
					priateGroup = BitmapFactory.decodeResource(getResources(),
							R.drawable.ic_private_group);
					publicGroup = BitmapFactory.decodeResource(getResources(),
							R.drawable.ic_public_group);
					mImgBtnOneToOne.setImageBitmap(oneToone);
					mImgBtnPrivate.setImageBitmap(priateGroup);
					mImgBtnPublic.setImageBitmap(publicGroup);
					break;
				case 1:
					oneToone = BitmapFactory.decodeResource(getResources(),
							R.drawable.ic_one_to_one);
					priateGroup = BitmapFactory.decodeResource(getResources(),
							R.drawable.ic_private_group_h);
					publicGroup = BitmapFactory.decodeResource(getResources(),
							R.drawable.ic_public_group);
					mImgBtnOneToOne.setImageBitmap(oneToone);
					mImgBtnPrivate.setImageBitmap(priateGroup);
					mImgBtnPublic.setImageBitmap(publicGroup);
					break;
				case 2:
					oneToone = BitmapFactory.decodeResource(getResources(),
							R.drawable.ic_one_to_one);
					priateGroup = BitmapFactory.decodeResource(getResources(),
							R.drawable.ic_private_group);
					publicGroup = BitmapFactory.decodeResource(getResources(),
							R.drawable.ic_public_group_h);
					mImgBtnOneToOne.setImageBitmap(oneToone);
					mImgBtnPrivate.setImageBitmap(priateGroup);
					mImgBtnPublic.setImageBitmap(publicGroup);
					break;

				default:
					break;
				}

			}

			@Override
			public void onPageScrolled(int arg0, float arg1, int arg2) {
				// TODO Auto-generated method stub

			}

			@Override
			public void onPageScrollStateChanged(int arg0) {
				// TODO Auto-generated method stub

			}
		});
	}

	@SuppressLint("NewApi")
	public void showOne2OneChatFragment(Contact c, boolean firstUpdate, View v, boolean isAnim) {
		// // ChatInsideFragment chatInsideFragment = (ChatInsideFragment)
		// // getSupportFragmentManager()
		// // .findFragmentByTag(CHAT_INSIDE_FRAGMENT);
		// FullViewChatFragment chatInsideFragment = (FullViewChatFragment)
		// getSupportFragmentManager()
		// .findFragmentByTag(CHAT_INSIDE_FRAGMENT);
		// FragmentTransaction transaction =
		// getSupportFragmentManager().beginTransaction();
		// if (chatInsideFragment != null) {
		//
		// getSupportFragmentManager().popBackStack();
		// transaction.remove(chatInsideFragment);
		// }
		//
		// // chatInsideFragment = ChatInsideFragment.newInstance(c,
		// firstUpdate);
		// chatInsideFragment = FullViewChatFragment.newInstance(c,
		// firstUpdate);
		// transaction.setCustomAnimations(R.anim.zoom_in, R.anim.zoom_out);
		// transaction.replace(R.id.content_frame, chatInsideFragment,
		// CHAT_INSIDE_FRAGMENT);
		// transaction.addToBackStack(null);
		// transaction.commit();
		// mActionBar.setDisplayShowHomeEnabled(true);
		// mActionBar.setDisplayHomeAsUpEnabled(true);
		// mActionBar.setDisplayShowCustomEnabled(false);
		// mSearchMenuItem.setVisible(false);
		// mIsSearchActive = mSearchView.isShown();
		// mSearchText = mSearchView.getQuery().toString();
		// if (mIsSearchActive) {
		// MenuItemCompat.collapseActionView(mSearchMenuItem);
		// }
		getActionBar().hide();
		// if (mPagerAdapter == null) {
		// mPagerAdapter = null;
		mPagerAdapter = new FullChatListAdapter(getSupportFragmentManager(), c, firstUpdate);
		mViewPager.setAdapter(mPagerAdapter);
		// }
		final FrameLayout expandedImageView = (FrameLayout) findViewById(R.id.expanded_chatView);
		// expandedImageView.setVisibility(View.VISIBLE);
		if (fullChatIndex == 0)
			getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);
		mViewPager.setCurrentItem(fullChatIndex);
		currentChatId = c.chatId;

		if (isAnim)
			zoomImageFromThumb(v, 0);
		else
			expandedImageView.setVisibility(View.VISIBLE);

		// zoomImageFromThumb(v, 0);
		// Animation animator = AnimationUtils.loadAnimation(this,
		// R.anim.zoom_in);
		// expandedImageView.startAnimation(animator);
		// ScaleAnimation animation = new ScaleAnimation(0f, 1f, 0f, 1f,
		// Animation.RELATIVE_TO_SELF,
		// (float) 0.5, Animation.RELATIVE_TO_SELF, (float) 0.5);
		// animation.setDuration(500);
		// expandedImageView.setAnimation(animation);
		// animation.start();
		// Helper.hideKeyboard(this);
	}

	@SuppressLint("NewApi")
	public void showOne2OneChatFragment(ChatLoad c, boolean firstUpdate, View view, boolean isAnim) {
		// // ChatInsideFragment chatInsideFragment = (ChatInsideFragment)
		// // getSupportFragmentManager()
		// // .findFragmentByTag(CHAT_INSIDE_FRAGMENT);
		// FullViewChatFragment chatInsideFragment = (FullViewChatFragment)
		// getSupportFragmentManager()
		// .findFragmentByTag(CHAT_INSIDE_FRAGMENT);
		// FragmentTransaction transaction =
		// getSupportFragmentManager().beginTransaction();
		// if (chatInsideFragment != null) {
		//
		// getSupportFragmentManager().popBackStack();
		// transaction.remove(chatInsideFragment);
		// }
		//
		// // chatInsideFragment = ChatInsideFragment.newInstance(c,
		// firstUpdate);
		// chatInsideFragment = FullViewChatFragment.newInstance(c,
		// firstUpdate);
		// transaction.setCustomAnimations(R.anim.zoom_in, R.anim.zoom_out);
		// transaction.replace(R.id.content_frame, chatInsideFragment,
		// CHAT_INSIDE_FRAGMENT);
		// transaction.addToBackStack(null);
		// transaction.commit();
		// mActionBar.setDisplayShowHomeEnabled(true);
		// mActionBar.setDisplayHomeAsUpEnabled(true);
		// mActionBar.setDisplayShowCustomEnabled(false);
		// mSearchMenuItem.setVisible(false);
		// mIsSearchActive = mSearchView.isShown();
		// mSearchText = mSearchView.getQuery().toString();
		// if (mIsSearchActive) {
		// MenuItemCompat.collapseActionView(mSearchMenuItem);
		// }
		try {
			getActionBar().hide();
			currentChatId = c.chatId;
			if (mPagerAdapter != null)
				mPagerAdapter = null;
			mPagerAdapter = new FullChatListAdapter(getSupportFragmentManager(), c, firstUpdate);
			mViewPager.setAdapter(mPagerAdapter);
			final FrameLayout expandedImageView = (FrameLayout) findViewById(R.id.expanded_chatView);
			// expandedImageView.setVisibility(View.VISIBLE);
			// ExpandAnimation animation = new
			// ExpandAnimation(expandedImageView,
			// 200);
			// expandedImageView.startAnimation(animation);

			if (fullChatIndex == 0)
				getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);
			else
				getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN);
			mViewPager.setCurrentItem(fullChatIndex);

			if (isAnim)
				zoomImageFromThumb(view, 0);
			else
				expandedImageView.setVisibility(View.VISIBLE);
			// mViewPager.setVisibility(View.GONE);
			// zoomImageFromThumb(view, 0);
			// Helper.hideKeyboard(MainActivity.this);
		} catch (Exception e) {
		}
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

				if (mDrawerLayout.isDrawerOpen(mContactListView)) {
					mDrawerLayout.closeDrawer(mContactListView);
				} else {
					mDrawerLayout.openDrawer(mContactListView);
				}
			}
		});
		customActionView.findViewById(R.id.home).setOnClickListener(new View.OnClickListener() {
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
				});

		mImgBtnOneToOne = (ImageButton) customActionView.findViewById(R.id.imgBtn_one2one);

		mImgBtnOneToOne.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				if (chatType == 0)
					return;
				chatType = 0;
				Bitmap oneToone = BitmapFactory.decodeResource(getResources(),
						R.drawable.ic_one_to_one_h);
				Bitmap priateGroup = BitmapFactory.decodeResource(getResources(),
						R.drawable.ic_private_group);
				Bitmap publicGroup = BitmapFactory.decodeResource(getResources(),
						R.drawable.ic_public_group);
				((ImageButton) customActionView.findViewById(R.id.imgBtn_private))
						.setImageBitmap(priateGroup);
				((ImageButton) customActionView.findViewById(R.id.imgBtn_public))
						.setImageBitmap(publicGroup);
				((ImageButton) v).setImageBitmap(oneToone);

				mViewPagerMain.setCurrentItem(0);
				// ChatListFragment one2OneChatListFragment =
				// (ChatListFragment) getSupportFragmentManager()
				// .findFragmentByTag(CHAT_LIST_FRAGMENT);
				// if (one2OneChatListFragment != null) {
				//
				// } else {
				// one2OneChatListFragment = new ChatListFragment();
				// }
				// FragmentTransaction transaction =
				// getSupportFragmentManager()
				// .beginTransaction();
				// transaction.replace(R.id.content_frame,
				// one2OneChatListFragment,
				// CHAT_LIST_FRAGMENT);
				// transaction.commit();
			}
		});

		mImgBtnPrivate = (ImageButton) customActionView.findViewById(R.id.imgBtn_private);
		mImgBtnPrivate.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				if (chatType == 1)
					return;
				chatType = 1;
				// customActionView.findViewById(R.id.imgBtn_one2one).setBackgroundResource(0);
				// customActionView.findViewById(R.id.imgBtn_public).setBackgroundResource(0);
				// v.setBackgroundResource(R.drawable.bg_tab_h);

				Bitmap oneToone = BitmapFactory.decodeResource(getResources(),
						R.drawable.ic_one_to_one);
				Bitmap priateGroup = BitmapFactory.decodeResource(getResources(),
						R.drawable.ic_private_group_h);
				Bitmap publicGroup = BitmapFactory.decodeResource(getResources(),
						R.drawable.ic_public_group);
				((ImageButton) customActionView.findViewById(R.id.imgBtn_one2one))
						.setImageBitmap(oneToone);
				((ImageButton) customActionView.findViewById(R.id.imgBtn_public))
						.setImageBitmap(publicGroup);
				((ImageButton) v).setImageBitmap(priateGroup);

				mViewPagerMain.setCurrentItem(1);
				// ChatListGroupFragment privateChatListFragment =
				// (ChatListGroupFragment) getSupportFragmentManager()
				// .findFragmentByTag(CHAT_LIST_PRIVATE_FRAGMENT);
				// if (privateChatListFragment == null) {
				// privateChatListFragment = new
				// ChatListGroupFragment(1);
				// }
				// FragmentTransaction transaction =
				// getSupportFragmentManager()
				// .beginTransaction();
				// transaction.replace(R.id.content_frame,
				// privateChatListFragment,
				// CHAT_LIST_PRIVATE_FRAGMENT);
				// transaction.commit();
			}
		});
		mImgBtnPublic = (ImageButton) customActionView.findViewById(R.id.imgBtn_public);
		mImgBtnPublic.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				if (chatType == 2)
					return;
				chatType = 2;
				// customActionView.findViewById(R.id.imgBtn_one2one).setBackgroundResource(0);
				// customActionView.findViewById(R.id.imgBtn_private).setBackgroundResource(0);
				// v.setBackgroundResource(R.drawable.bg_tab_h);
				Bitmap oneToone = BitmapFactory.decodeResource(getResources(),
						R.drawable.ic_one_to_one);
				Bitmap priateGroup = BitmapFactory.decodeResource(getResources(),
						R.drawable.ic_private_group);
				Bitmap publicGroup = BitmapFactory.decodeResource(getResources(),
						R.drawable.ic_public_group_h);
				((ImageButton) customActionView.findViewById(R.id.imgBtn_one2one))
						.setImageBitmap(oneToone);
				((ImageButton) customActionView.findViewById(R.id.imgBtn_private))
						.setImageBitmap(priateGroup);
				((ImageButton) v).setImageBitmap(publicGroup);

				mViewPagerMain.setCurrentItem(2);
				// ChatListGroupPublicFragment publicChatListFragment =
				// (ChatListGroupPublicFragment)
				// getSupportFragmentManager()
				// .findFragmentByTag(CHAT_LIST_PUBLIC_FRAGMENT);
				// if (publicChatListFragment == null) {
				// publicChatListFragment = new
				// ChatListGroupPublicFragment(2);
				// }
				// // ChatListGroupFragment privateChatListFragment =
				// new
				// // ChatListGroupFragment(2);
				// FragmentTransaction transaction =
				// getSupportFragmentManager()
				// .beginTransaction();
				// transaction.replace(R.id.content_frame,
				// publicChatListFragment,
				// CHAT_LIST_PUBLIC_FRAGMENT);
				// transaction.commit();

			}
		});
		Button buttonAdd = (Button) customActionView.findViewById(R.id.imgBtn_add);
		buttonAdd.setTypeface(Application.typefaceThin);
		buttonAdd.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				// mContactListView.setAdapter(mContactListAddChatAdapter);
				if (chatType == 2) {
					List<Contact> contacts = Lists.newArrayList();
					// createChat(contacts);
					ChatLoad chatLoad = new ChatLoad();
					chatLoad.chatId = -1;
					chatLoad.type = chatType;
					chatLoad.isCreated = false;
					newChatCreated.put(chatType, chatLoad);
					refreshOne2OneList();
				} else {
					if (chatType != 0) {
						isAddContact = true;
					} else {
						isAddContact = false;
					}
					refreshContactList();
					mContactListAdapter.notifyDataSetChanged();
					mDrawerLayout.openDrawer(mContactListView);
				}

				try {
					one2OneChatListFragment = (ChatListFragment) adapter.getItem(0);
					privateChatListFragment = (ChatListGroupFragment) adapter.getItem(1);
					publicChatListFragment = (ChatListGroupPublicFragment) adapter.getItem(2);

					if (chatType == 0) {
						one2OneChatListFragment.setLocationFilter(false);
						one2OneChatListFragment.setFavoriteFilter(false);
					} else if (chatType == 1) {
						privateSearchString = "";
						privateChatListFragment.setLocationFilter(false);
						privateChatListFragment.setFavoriteFilter(false);
					} else if (chatType == 2) {
						publicSearchString = "";
						publicChatListFragment.setLocationFilter(false);
						publicChatListFragment.setFavoriteFilter(false);
					}

				} catch (Exception e) {
				}

				// Intent i = new Intent(getContext(),
				// QrCodeShowActivity.class);
				// i.putExtra(IntentKey.QR_CODE,
				// QodemePreferences.getInstance().getQrcode());
				// startActivity(i);
			}
		});
	}

	@SuppressLint("NewApi")
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
									// contentValues.put(QodemeContract.Contacts.CONTACT_STATE,
									// value)
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
									});
						}
					}
				}
				break;
			}
			case REQUEST_ACTIVITY_CONTACT_DETAILS:
				long id = data.getLongExtra(QodemeContract.Contacts._ID, -1);
				// String title =
				// data.getStringExtra(QodemeContract.Contacts.CONTACT_TITLE);
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
							QodemeContract.Chats.CHAT_ID + " = " + currentChatId, null);

					SyncHelper.requestManualSync();
				} else {
					int updated = QodemeContract.Contacts.Sync.DONE;// data.getIntExtra(QodemeContract.Contacts.UPDATED,QodemeContract.Contacts.Sync.DONE);
					getContentResolver().update(
							QodemeContract.Chats.CONTENT_URI,
							QodemeContract.Chats.updateChatInfoValues("", color, "", 0, "", "",
									updated, 1),
							// updateContactInfoValues(null, color, updated),
							DbUtils.getWhereClauseForId(), DbUtils.getWhereArgsForId(id));

					ChatLoad chatLoad = getChatLoad(currentChatId);
					if (chatLoad != null)
						setChatInfo(currentChatId, null, color, chatLoad.tag, chatLoad.description,
								chatLoad.chat_status, chatLoad.is_locked, chatLoad.title,
								chatLoad.latitude, chatLoad.longitude);
					// else
					// setChatInfo(currentChatId, null, color, null, null, null,
					// null, null, "0",
					// "0");
				}
				mViewPager.setCurrentItem(1);
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
								if (mPagerAdapter != null) {
									if (chatType == 0)
										chatInsideFragment = (ChatInsideFragment) mPagerAdapter
												.getItem(0);
									else
										chatInsideGroupFragment = (ChatInsideGroupFragment) mPagerAdapter
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

						// is.close();
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
				break;

			case REQUEST_ACTIVITY_CAMERA:
				// handleBigCameraPhoto();
				File file = new File(mCurrentPhotoPath);
				ChatInsideFragment chatInsideFragment = null;
				ChatInsideGroupFragment chatInsideGroupFragment = null;

				if (!getActionBar().isShowing()) {
					if (mPagerAdapter != null) {
						if (chatType == 0)
							chatInsideFragment = (ChatInsideFragment) mPagerAdapter.getItem(0);
						else
							chatInsideGroupFragment = (ChatInsideGroupFragment) mPagerAdapter
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
		}
	}

	private void handleBigCameraPhoto() {

		if (mCurrentPhotoPath != null) {
			// setPic();
			galleryAddPic();
			mCurrentPhotoPath = null;
		}

	}

	private void galleryAddPic() {
		Intent mediaScanIntent = new Intent("android.intent.action.MEDIA_SCANNER_SCAN_FILE");
		File f = new File(mCurrentPhotoPath);
		Uri contentUri = Uri.fromFile(f);
		mediaScanIntent.setData(contentUri);
		this.sendBroadcast(mediaScanIntent);
	}

	private void uploadImage(String imageLocal) {
		String mProfileImageBase64 = null;
		try {
			File file = new File(imageLocal);
			Bitmap resizedBitmap = decodeFile(file);

			Matrix matrix = new Matrix();
			matrix.postRotate(getImageOrientation(file.getAbsolutePath().toString().trim()));
			Bitmap rotatedBitmap = Bitmap.createBitmap(resizedBitmap, 0, 0,
					resizedBitmap.getWidth(), resizedBitmap.getHeight(), matrix, true);

			ByteArrayOutputStream stream = new ByteArrayOutputStream();
			rotatedBitmap.compress(Bitmap.CompressFormat.PNG, 100, stream);
			byte[] byteArray = stream.toByteArray();

			mProfileImageBase64 = Base64.encodeToString(byteArray, Base64.NO_WRAP);

		} catch (OutOfMemoryError e) {
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}
		if (mProfileImageBase64 != null) {
			// UploadImageResponse imageResponse = rest.chatImage(id,
			// mProfileImageBase64);
			// new ChatImageUploadHandler(context, id).parse(imageResponse);
		}

	}

	public void takePhotoFromGallery() {
		Intent intent = new Intent(Intent.ACTION_PICK,
				android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
		startActivityForResult(intent, REQUEST_ACTIVITY_PHOTO_GALLERY);
	}

	// public void takePhotoFromCamera() {
	// Intent intent = new Intent(Intent.ACTION_PICK,
	// android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
	// startActivityForResult(intent, REQUEST_ACTIVITY_PHOTO_GALLERY);
	// }

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

	/* Photo album for this application */
	private String getAlbumName() {
		return getString(R.string.album_name);
	}

	private File getAlbumDir() {
		File storageDir = null;

		if (Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState())) {

			storageDir = mAlbumStorageDirFactory.getAlbumStorageDir(getAlbumName());

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
		CharSequence charSequence[] = { "Camera", "Gallery" };
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

	public int getImageOrientation(String imagePath) {
		int rotate = 0;
		try {

			File imageFile = new File(imagePath);
			ExifInterface exif = new ExifInterface(imageFile.getAbsolutePath());
			int orientation = exif.getAttributeInt(ExifInterface.TAG_ORIENTATION,
					ExifInterface.ORIENTATION_NORMAL);
			// Log.d(TAG,"orientation : "+orientation);
			switch (orientation) {
			case ExifInterface.ORIENTATION_ROTATE_270:
				rotate = 270;
				break;
			case ExifInterface.ORIENTATION_ROTATE_180:
				rotate = 180;
				break;
			case ExifInterface.ORIENTATION_ROTATE_90:
				rotate = 90;
				break;
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		return rotate;
	}

	private String getPath(Uri uri) {
		System.gc();
		String[] proj = { MediaStore.Images.Media.DATA };
		CursorLoader loader = new CursorLoader(this, uri, proj, null, null, null);
		Cursor cursor = loader.loadInBackground();
		int column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
		cursor.moveToFirst();
		return cursor.getString(column_index);
	}

	private Bitmap decodeFile(File f) {
		try {
			// Decode image size
			BitmapFactory.Options o = new BitmapFactory.Options();
			o.inJustDecodeBounds = true;
			BitmapFactory.decodeStream(new FileInputStream(f), null, o);

			// The new size we want to scale to
			final int REQUIRED_SIZE = 70;

			// Find the correct scale value. It should be the power of 2.
			int scale = 1;
			while (o.outWidth / scale / 2 >= REQUIRED_SIZE
					&& o.outHeight / scale / 2 >= REQUIRED_SIZE)
				scale *= 2;

			// Decode with inSampleSize
			BitmapFactory.Options o2 = new BitmapFactory.Options();
			o2.inSampleSize = scale;
			return BitmapFactory.decodeStream(new FileInputStream(f), null, o2);
		} catch (FileNotFoundException e) {
		}
		return null;
	}

	@Override
	protected void onStart() {
		super.onStart();
		// Connect the client.
		mLocationClient.connect();
		AnalyticsHelper.onStartActivity(this);
		/*
		 * Location l = null; l.getLongitude();
		 */
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

	LocationResult locationResult = new LocationResult() {

		@Override
		public void gotLocation(Location location) {
			if (location != null) {
				getMyLocation(location);
				setCurrentLocation(location);
			}
		}
	};

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

	public Location getLastLocation() {
		Location loc = mLocationClient.getLastLocation();
		return loc;
	}

	@Override
	public void onDisconnected() {
	}

	@Override
	public void onConnectionFailed(ConnectionResult connectionResult) {
	}

	private class DrawerItemClickListener implements ListView.OnItemClickListener {
		@Override
		public void onItemClick(AdapterView parent, View view, int position, long id) {
			selectItem(position);
		}
	}

	private void selectItem(int position) {
		// To change body of created methods use File | Settings | File
		// Templates.
	}

	// @Override
	// public boolean onCreateOptionsMenu(Menu menu) {
	// getMenuInflater().inflate(R.menu.main, menu);
	// mSearchMenuItem = menu.findItem(R.id.action_search);
	// mSearchView = (SearchView)
	// MenuItemCompat.getActionView(mSearchMenuItem);
	// if (mSearchText != null) {
	// mSearchView.setQuery(mSearchText, false);
	// }
	// if (getContactList() != null) {
	// initializeSearchView();
	// }

	// return true;
	// }

	private void initializeSearchView() {
		List<Contact> contacts = getContactList();
		String[] items = new String[contacts.size()];
		for (int i = 0; i < contacts.size(); i++) {
			Contact contact = contacts.get(i);
			items[i] = contact.title;
		}

		ArrayAdapter<String> adapter = new ArrayAdapter<String>(this,
				android.R.layout.simple_dropdown_item_1line, items);

		final SearchView.SearchAutoComplete autoCompleteTextView = (SearchView.SearchAutoComplete) mSearchView
				.findViewById(R.id.search_src_text);
		autoCompleteTextView.setAdapter(adapter);
		autoCompleteTextView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				String item = (String) parent.getItemAtPosition(position);
				autoCompleteTextView.setText(item);
				autoCompleteTextView.setSelection(item.length());

				openChat(item);
			}
		});
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		Log.d("MENU", "Cliced MenuItem is " + item.getTitle());
		switch (item.getItemId()) {
		case android.R.id.home:
			onBackPressed();
			return true;
			// case R.id.action_logout:
			// RestAsyncHelper.getInstance().registerToken("", new
			// RestListener() {
			//
			// @Override
			// public void onResponse(BaseResponse response) {
			// RestAsyncHelper.getInstance().accountLogout(new RestListener() {
			// @Override
			// public void onResponse(BaseResponse response) {
			// logoutHandler();
			// }
			//
			// @Override
			// public void onServiceError(RestError error) {
			// showMessage(RestErrorType.getMessage(getContext(),
			// error.getErrorType())
			// + error.getServerMsg());
			// }
			//
			// @Override
			// public void onNetworkError(VolleyError error) {
			// super.onNetworkError(error);
			// showMessage(error.getMessage());
			// }
			// });
			// }
			//
			// @Override
			// public void onServiceError(RestError error) {
			// showMessage(RestErrorType.getMessage(getContext(),
			// error.getErrorType())
			// + error.getServerMsg());
			// }
			//
			// @Override
			// public void onNetworkError(VolleyError error) {
			// super.onNetworkError(error);
			// showMessage("No internet connection!");
			// }
			//
			// private void logoutHandler() {
			// clearActivityCache();
			// QodemePreferences.getInstance().setLogged(false);
			// QodemePreferences.getInstance().setGcmTokenSycnWithRest(false);
			// startActivity(new Intent(getApplicationContext(),
			// LoginActivity.class));
			// finish();
			//
			// }
			// });
			//
			// return true;

			// case R.id.action_share: {
			// Intent i = new Intent(getContext(), QrCodeShowActivity.class);
			// i.putExtra(IntentKey.QR_CODE,
			// QodemePreferences.getInstance().getQrcode());
			// startActivity(i);
			// return true;
			// }
			//
			// case R.id.action_help: {
			// Intent i = new Intent(getContext(), TutorialActivity.class);
			// startActivity(i);
			// return true;
			// }

		case R.id.action_settings: {
			Intent i = new Intent(getContext(), SettingsActivity.class);
			startActivity(i);
			return true;
		}
		}

		return super.onOptionsItemSelected(item);
	}

	@Override
	protected void onPostCreate(Bundle savedInstanceState) {
		super.onPostCreate(savedInstanceState);
		// Sync the toggle state after onRestoreInstanceState has occurred.
		// mDrawerToggle.syncState();
	}

	@Override
	public void onConfigurationChanged(Configuration newConfig) {
		super.onConfigurationChanged(newConfig);
		Log.d("config", "call");
		// Pass any configuration change to the drawer toggls
		// mDrawerToggle.onConfigurationChanged(newConfig);
		try {
			if (chatType == 0) {
				// one2OneChatListFragment = (ChatListFragment)
				// mPagerAdapter.getItem(0);
				// privateChatListFragment = (ChatListGroupFragment)
				// mPagerAdapter.getItem(1);
				// publicChatListFragment = (ChatListGroupPublicFragment)
				// mPagerAdapter.getItem(2);
			} else {
				ChatInsideGroupFragment chatInsideGroupFragment = (ChatInsideGroupFragment) mPagerAdapter
						.getItem(0);
				chatInsideGroupFragment.onConfigurationChanged(newConfig);

			}

			mPagerAdapter.notifyDataSetChanged();
		} catch (Exception e) {
		}
	}

	private void initContactsList() {
		mContactListView = (ListView) findViewById(R.id.left_drawer);

		mContactListView.setOnItemClickListener(new DrawerItemClickListener() {
			@Override
			public void onItemClick(AdapterView parent, View view, int position, long id) {
				super.onItemClick(parent, view, position, id);
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
						mDrawerLayout.closeDrawer(mContactListView);
						mViewPagerMain.setCurrentItem(0);
						Helper.hideKeyboard(MainActivity.this);
						showOne2OneChatFragment(ce, true, mViewPager, false);
					} else if (ce.state == QodemeContract.Contacts.State.APPRUVED) {
						mDrawerLayout.closeDrawer(mContactListView);
						mViewPagerMain.setCurrentItem(0);
						Helper.hideKeyboard(MainActivity.this);
						showOne2OneChatFragment(ce, true, mViewPager, false);
					} else if (ce.state == QodemeContract.Contacts.State.INVITATION_SENT) {
						Intent i = new Intent(getContext(), ContactDetailsActivity.class);
						i.putExtra(QodemeContract.Contacts._ID, ce._id);
						i.putExtra(QodemeContract.Contacts.CONTACT_TITLE, ce.title);
						i.putExtra(QodemeContract.Contacts.CONTACT_COLOR, ce.color);
						i.putExtra(QodemeContract.Contacts.UPDATED, ce.updated);
						startActivityForResult(i, REQUEST_ACTIVITY_CONTACT_DETAILS);
						mDrawerLayout.closeDrawer(mContactListView);
					} else if (ce.state == QodemeContract.Contacts.State.BLOCKED_BY) {
						final CharSequence[] items = { "Unblock" };

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

		View moreBtnView = getLayoutInflater().inflate(R.layout.more_item_header, null);
		buttonMore = (Button) moreBtnView.findViewById(R.id.btn_more);
		buttonMore.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				Intent intent = new Intent(MainActivity.this, MoreOptionActivity.class);
				startActivityForResult(intent, REQUEST_ACTIVITY_MORE);
				mDrawerLayout.closeDrawers();
			}
		});
		buttonInvite = (Button) moreBtnView.findViewById(R.id.btn_invite);
		buttonInvite.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				mDrawerLayout.closeDrawers();
				Intent i = new Intent(MainActivity.this, QrCodeShowActivity.class);
				i.putExtra(IntentKey.QR_CODE, QodemePreferences.getInstance().getQrcode());
				startActivityForResult(i, REQUEST_ACTIVITY_SHOW_QR_CODE);
			}
		});

		buttonshare = (Button) moreBtnView.findViewById(R.id.btn_more_share);
		buttonshare.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				mDrawerLayout.closeDrawers();
				email();
			}
		});

		mContactListView.addHeaderView(moreBtnView);
		mContactListView.setLongClickable(true);

		List<ContactListItemEntity> listForAdapter = Lists.newArrayList();

		// listForAdapter.addAll(mContactList); // it's important for safety of
		// the orders list in the model.
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
											// createChat(selectedContact);
											MainActivity.this.selectedContact = selectedContact;
											ChatLoad chatLoad = new ChatLoad();
											chatLoad.type = chatType;
											chatLoad.chatId = -1;
											chatLoad.isCreated = false;
											newChatCreated.put(chatType, chatLoad);
											refreshOne2OneList();
										}
									}
									mDrawerLayout.closeDrawer(mContactListView);
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
		// mContactListAddChatAdapter = new
		// MenuListAddToChatAdapter<ContactListItemEntity>(this,
		// R.layout.contact_list_item_add, listForAdapter) {
		//
		// @Override
		// public View getView(int position, View convertView, ViewGroup parent)
		// {
		//
		// ContactListItemEntity e = getItem(position);
		//
		// if (e.isHeader()) {
		// if (convertView == null || (convertView instanceof ContactListItem)
		// || (convertView instanceof ContactListItemInvited)) {
		// convertView =
		// layoutInflater.inflate(R.layout.contact_list_item_header,
		// null);
		// }
		//
		// TextView tvHeader = ((TextView)
		// convertView.findViewById(R.id.header));
		// Button addbtn = (Button) convertView.findViewById(R.id.btn_add);
		// addbtn.setVisibility(View.GONE);
		// switch (e.getState()) {
		// case QodemeContract.Contacts.State.INVITED:
		// tvHeader.setText("Invitation");
		// break;
		// case QodemeContract.Contacts.State.INVITATION_SENT:
		// tvHeader.setText("Invited");
		// break;
		// case QodemeContract.Contacts.State.APPRUVED:
		//
		// addbtn.setOnClickListener(new View.OnClickListener() {
		//
		// @Override
		// public void onClick(View v) {
		// List<Contact> selectedContact = Lists.newArrayList();
		// for (int i = 0; i < getCount(); i++) {
		// ContactListItemEntity entity = getItem(i);
		// if (entity.isChecked()) {
		// selectedContact.add(entity.getContact());
		// entity.setChecked(false);
		// }
		// }
		// if (selectedContact.size() > 0) {
		// createChat(selectedContact);
		// }
		// mDrawerLayout.closeDrawer(mContactListView);
		// }
		// });
		// addbtn.setVisibility(View.VISIBLE);
		// tvHeader.setText("Contacts");
		// break;
		// case QodemeContract.Contacts.State.BLOCKED_BY:
		// tvHeader.setText("Blocked");
		// break;
		//
		// }
		// } else {
		// if (e.getState() == QodemeContract.Contacts.State.INVITED) {
		// ContactListItemInvited view;
		// if (convertView == null || !(convertView instanceof
		// ContactListItemInvited)) {
		// view = (ContactListItemInvited) layoutInflater.inflate(
		// R.layout.contact_list_item_invited, null);
		// } else {
		//
		// view = (ContactListItemInvited) convertView;
		// }
		// view.fill(e);
		// return view;
		//
		// } else {
		// ContactListItem view;
		// if (convertView == null || !(convertView instanceof ContactListItem))
		// {
		// view = (ContactListItem) layoutInflater.inflate(layoutResId, null);
		// } else {
		// view = (ContactListItem) convertView;
		// }
		// view.fill(e);
		// return view;
		//
		// }
		//
		// }
		//
		// return convertView;
		// }
		// };

		mContactListView.setAdapter(mContactListAdapter);

		mContactListLoader = new ContactListLoader();
		mMessageListLoader = new MessageListLoader();
		mChatLoadListLoader = new ChatLoadListLoader();

		getSupportLoaderManager().initLoader(0, null, mContactListLoader);
		getSupportLoaderManager().initLoader(1, null, mMessageListLoader);
		getSupportLoaderManager().initLoader(2, null, mChatLoadListLoader);

	}

	// private void createChat(final List<Contact> contactsList) {
	// // Log.d("contact add", contactsList.get(0).title + "");
	// ChatType mChatType = null;
	// if (chatType == 1)
	// mChatType = ChatType.PRIVATE_GROUP;
	// else if (chatType == 2)
	// mChatType = ChatType.PUBLIC_GROUP;
	//
	// if (mChatType == null) {
	// mChatType = ChatType.PRIVATE_GROUP;
	// }
	// Location location = mLocationClient.getLastLocation();
	//
	// double latitude = 0;
	// double longitude = 0;
	// if (location != null) {
	// latitude = location.getLatitude();
	// longitude = location.getLongitude();
	// } else if (getCurrentLocation() != null) {
	// latitude = getCurrentLocation().getLatitude();
	// longitude = getCurrentLocation().getLongitude();
	// }
	// final double lat = latitude;
	// final double lng = longitude;
	// RestAsyncHelper.getInstance().chatCreate(mChatType, "", "", 0, "", 0, "",
	// latitude,
	// longitude, new RestListener<ChatCreateResponse>() {
	//
	// @Override
	// public void onResponse(ChatCreateResponse response) {
	// // TODO Auto-generated method stub
	// Log.d("Chat create", "Chat Created " + response.getChat().getId());
	//
	// if (chatType == 2)
	// QodemePreferences.getInstance().setNewPublicGroupChatId(
	// response.getChat().getId());
	// if (chatType == 1)
	// QodemePreferences.getInstance().setNewPrivateGroupChatId(
	// response.getChat().getId());
	// getContentResolver().insert(
	// QodemeContract.Chats.CONTENT_URI,
	// QodemeContract.Chats.addNewChatValues(response.getChat().getId(),
	// response.getChat().getType(), response.getChat()
	// .getQrcode(), QodemePreferences.getInstance()
	// .getQrcode(), lat, lng,""));
	//
	// for (Contact contact : contactsList) {
	// final long chatid = response.getChat().getId();
	// final String qr = contact.qrCode;
	// Cursor cursor = getContentResolver().query(
	// QodemeContract.Chats.CONTENT_URI,
	// QodemeContract.Chats.ChatQuery.PROJECTION,
	// QodemeContract.Chats.CHAT_ID + "=" + chatid, null, null);
	// String memberString = "";
	// int numberOfMember = 1;
	// if (cursor != null && cursor.moveToFirst()) {
	// memberString = cursor
	// .getString(QodemeContract.Chats.ChatQuery.CHAT_MEMBER_QRCODES);
	// numberOfMember = cursor
	// .getInt(QodemeContract.Chats.ChatQuery.CHAT_NUMBER_OF_MEMBER);
	// }
	// if (memberString == null || memberString.equals(""))
	// memberString = qr;
	// else {
	// memberString += "," + qr;
	// }
	// if (numberOfMember == 0) {
	// numberOfMember = 2;
	// } else {
	// numberOfMember++;
	// }
	// ContentValues contentValues = new ContentValues();
	// contentValues.put(QodemeContract.Chats.CHAT_MEMBER_QRCODES,
	// memberString);
	// contentValues.put(QodemeContract.Chats.CHAT_NUMBER_OF_MEMBER,
	// numberOfMember);
	// getContentResolver().update(QodemeContract.Chats.CONTENT_URI,
	// contentValues, QodemeContract.Chats.CHAT_ID + "=" + chatid,
	// null);
	//
	// RestAsyncHelper.getInstance().chatAddMember(response.getChat().getId(),
	// contact.qrCode, new RestListener<ChatAddMemberResponse>() {
	//
	// @Override
	// public void onResponse(ChatAddMemberResponse response) {
	// Log.d("Chat add ", "Chat add mem "
	// + response.getChat().getId());
	//
	// }
	//
	// @Override
	// public void onServiceError(RestError error) {
	// Log.d("Error", "Chat add member");
	// }
	// });
	// }
	// }
	//
	// @Override
	// public void onServiceError(RestError error) {
	// Log.d("Error", "Chat not Created");
	// }
	// });
	// }

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
						// TODO Auto-generated method stub
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
												.getQrcode(), lat, lng, title));

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
										contentValues, QodemeContract.Chats.CHAT_ID + "=" + chatid,
										null);

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
										});
							}
							selectedContact.clear();
						}
					}

					@Override
					public void onServiceError(RestError error) {
						Log.d("Error", "Chat not Created");
					}
				});
	}

	@Override
	protected void onDestroy() {
		MainActivity.isKeyboardVisible = false;
		mImageFetcher.closeCache();
		QodemePreferences.getInstance().setNewPublicGroupChatId(-1l);
		QodemePreferences.getInstance().setNewPrivateGroupChatId(-1l);

		// unregisterReceiver(broadcastReceiverForDeleteChat);
		super.onDestroy();
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
					QodemeContract.Contacts.CONTACT_LIST_SORT);
		}

		@Override
		public void onLoadFinished(Loader<Cursor> cursorLoader, Cursor cursor) {
			mContacts = ModelHelper.getContactList(cursor);
			refreshContactList();

			if (mSearchView != null) {
				initializeSearchView();
			}
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
			// QodemeContract.Messages.UPDATED + " ASC ");

		}

		@Override
		public void onLoadFinished(Loader<Cursor> cursorLoader, Cursor cursor) {
			ModelHelper.MessageStructure ms = ModelHelper.getChatMessagesMap(cursor);
			mChatMessagesMap = ms.getMessageMap();
			mChatNewMessagesMap = ms.getNewMassageMap();
			mLastMessageInChatMap = ms.getLastMessageInChatMap();

			refreshOne2OneList();
			mHandler.sendEmptyMessage(2);
		}

		@Override
		public void onLoaderReset(Loader<Cursor> cursorLoader) {
		}

	}

	private void refreshContactList() {
		// Search filter
		List<Contact> filtredContacts = null;
		if (TextUtils.isEmpty(mSearchFilter))
			filtredContacts = Lists.newArrayList(mContacts);
		else
			filtredContacts = Lists.newArrayList(Iterables.filter(mContacts,
					new Predicate<Contact>() {
						@Override
						public boolean apply(Contact contact) {
							return Pattern
									.compile(Pattern.quote(mSearchFilter), Pattern.CASE_INSENSITIVE)
									.matcher(contact.title).find();
						}
					}));

		// Sort contact list by state
		Collections.sort(filtredContacts, new Comparator<Contact>() {
			@Override
			public int compare(Contact lhs, Contact rhs) {
				if (lhs.state == rhs.state) {
					// TODO sort abc
				} else {
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
				}));

		mBlockContacts = Lists.newArrayList(Iterables.filter(filtredContacts,
				new Predicate<Contact>() {
					@Override
					public boolean apply(Contact contact) {
						return contact.state == QodemeContract.Contacts.State.BLOCKED_BY;
					}
				}));
		mBlockedContacts = Lists.newArrayList(Iterables.filter(filtredContacts,
				new Predicate<Contact>() {
					@Override
					public boolean apply(Contact contact) {
						return contact.state == QodemeContract.Contacts.State.BLOCKED;
					}
				}));
		// if (mBlockedContacts != null)
		// mApprovedContacts.addAll(mBlockedContacts);
		// mChatList
		mContactInfoUpdated = true;

		// List<ContactListItemEntity> contactListItemsApp =
		// Lists.newArrayList();
		// if (!mApprovedContacts.isEmpty()) {
		//
		// contactListItemsApp.add(new ContactListItemEntity(true,
		// mApprovedContacts.get(0).state,
		// null));
		// }
		// for (Contact c : mApprovedContacts) {
		// if (contactListItemsApp.get(mApprovedContacts.size() - 1).getState()
		// != c.state) {
		// contactListItemsApp.add(new ContactListItemEntity(true, c.state,
		// null));
		// }
		// contactListItemsApp.add(new ContactListItemEntity(false, c.state,
		// c));
		// }
		// mContactListAddChatAdapter.clear();
		// mContactListAddChatAdapter.addAll(contactListItemsApp);

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
			showOne2OneChatFragment(contact, true, mViewPager, false);
			chatFromNotification = -1;
		}

	}

	private final ContentObserver mContactListObserver = new ContentObserver(new Handler()) {
		@Override
		public void onChange(boolean selfChange) {
			// TODO check, do we need this line
			/*
			 * if (getActivity() == null) { return; }
			 */

			getSupportLoaderManager().restartLoader(1, null, mContactListLoader);
			getSupportLoaderManager().restartLoader(2, null, mMessageListLoader);
		}
	};

	private final ContentObserver mMessageListObjerver = new ContentObserver(new Handler()) {
		@Override
		public void onChange(boolean selfChange) {
			// TODO check, do we need this line
			/*
			 * if (getActivity() == null) { return; }
			 */

			getSupportLoaderManager().restartLoader(2, null, mMessageListLoader);
		}
	};
	private final ContentObserver mChatListObserver = new ContentObserver(new Handler()) {
		@Override
		public void onChange(boolean selfChange) {
			// TODO check, do we need this line
			/*
			 * if (getActivity() == null) { return; }
			 */

			getSupportLoaderManager().restartLoader(3, null, mChatLoadListLoader);
		}
	};

	private class ApprovedContactsComparator implements Comparator<Contact> {

		private Map<Long, Long> lastMessaheTimeMap;

		ApprovedContactsComparator(Map<Long, Long> lastMessaheTimeMap) {
			this.lastMessaheTimeMap = lastMessaheTimeMap;
		}

		private long getLastTimeMarker(Long chatId) {
			return lastMessaheTimeMap != null ? NullHelper.notNull(lastMessaheTimeMap.get(chatId),
					0L) : 0L;
		}

		@Override
		public int compare(Contact lhs, Contact rhs) {
			return Longs.compare(getLastTimeMarker(rhs.chatId), getLastTimeMarker(lhs.chatId));
		}
	}

	private class ApprovedChatComparator implements Comparator<ChatLoad> {

		private Map<Long, Long> lastMessaheTimeMap;

		ApprovedChatComparator(Map<Long, Long> lastMessaheTimeMap) {
			this.lastMessaheTimeMap = lastMessaheTimeMap;
		}

		private long getLastTimeMarker(Long chatId) {
			return lastMessaheTimeMap != null ? NullHelper.notNull(lastMessaheTimeMap.get(chatId),
					0L) : 0L;
		}

		@Override
		public int compare(ChatLoad lhs, ChatLoad rhs) {
			return Longs.compare(getLastTimeMarker(rhs.chatId), getLastTimeMarker(lhs.chatId));
		}
	}

	private void refreshOne2OneList() {
		// Sort by new messages
		// if (mApprovedContacts != null)
		// Collections.sort(mApprovedContacts, new ApprovedContactsComparator(
		// mLastMessageInChatMap));
		//
		// if (mChatList != null) {
		// Collections.sort(mChatList, new
		// ApprovedChatComparator(mLastMessageInChatMap));
		// }
		one2OneChatListFragment = (ChatListFragment) adapter.getItem(0);
		privateChatListFragment = (ChatListGroupFragment) adapter.getItem(1);
		publicChatListFragment = (ChatListGroupPublicFragment) adapter.getItem(2);

		one2OneChatListFragment.updateUi();
		privateChatListFragment.updateUi();
		publicChatListFragment.updateUi();

		adapter.notifyDataSetChanged();

		// ChatListFragment one2OneChatListFragment = (ChatListFragment)
		// getSupportFragmentManager()
		// .findFragmentByTag(CHAT_LIST_FRAGMENT);
		//
		// ChatListGroupFragment privateChatListFragment =
		// (ChatListGroupFragment) getSupportFragmentManager()
		// .findFragmentByTag(CHAT_LIST_PRIVATE_FRAGMENT);
		// ChatListGroupPublicFragment publicChatListFragment =
		// (ChatListGroupPublicFragment) getSupportFragmentManager()
		// .findFragmentByTag(CHAT_LIST_PUBLIC_FRAGMENT);
		// if (one2OneChatListFragment != null)
		// one2OneChatListFragment.updateUi();
		// if (privateChatListFragment != null)
		// privateChatListFragment.updateUi();
		// if (publicChatListFragment != null)
		// publicChatListFragment.updateUi();
	}

	private void openChat(String name) {
		ChatListFragment one2OneChatListFragment = (ChatListFragment) getSupportFragmentManager()
				.findFragmentByTag(CHAT_LIST_FRAGMENT);
		if (one2OneChatListFragment != null) {
			one2OneChatListFragment.openChat(name);
		}
	}

	@SuppressLint("NewApi")
	private void refreshOne2OneInside() {
		// final ChatInsideFragment one2OneChatInsideFragment =
		// (ChatInsideFragment) getSupportFragmentManager()
		// .findFragmentByTag(CHAT_INSIDE_FRAGMENT);

		// final FullViewChatFragment one2OneChatInsideFragment =
		// (FullViewChatFragment) getSupportFragmentManager()
		// .findFragmentByTag(CHAT_INSIDE_FRAGMENT);
		try {
			if (chatType == 0) {
				ChatInsideFragment one2OneChatInsideFragment = null;
				ChatProfileFragment chatProfileFragment = null;
				ChatPhotosFragment chatPhotosFragment = null;
				if (mPagerAdapter != null && !getActionBar().isShowing()) {
					one2OneChatInsideFragment = (ChatInsideFragment) mPagerAdapter.getItem(0);
					chatProfileFragment = (ChatProfileFragment) mPagerAdapter.getItem(1);
					chatPhotosFragment = (ChatPhotosFragment) mPagerAdapter.getItem(2);
				}
				if (chatProfileFragment != null)
					chatProfileFragment.setData();
				if (chatPhotosFragment != null)
					chatPhotosFragment.updateUi();
				if (one2OneChatInsideFragment != null) {
					one2OneChatInsideFragment.updateUi();
					if (mContactInfoUpdated) {
						long chatId = one2OneChatInsideFragment.getChatId();
						int chatColor = 0;
						String chatName = "";
						Contact c = findContactEntityByChatId(chatId);
						if (c != null) {
							chatColor = c.color;
							chatName = c.title;
						}
						// final int fColor = chatColor;
						// final String fName = chatName;

						mContactInfoUpdated = false;
						showOne2OneChatFragment(c, false, mViewPager, false);

					}
					one2OneChatInsideFragment.updateUi();
					// Contact c = findContactEntityByChatId(currentChatId);
					// if (c != null)
					// chatProfileFragment.setContact(c);
					chatProfileFragment.setData();
					chatPhotosFragment.updateUi();
					mViewPager.setCurrentItem(fullChatIndex);

				}
			} else {
				ChatInsideGroupFragment groupChatInsideFragment = null;
				ChatGroupProfileFragment chatGroupProfileFragment = null;
				ChatGroupPhotosFragment chatGroupPhotosFragment = null;

				if (mPagerAdapter != null && !getActionBar().isShowing()) {
					groupChatInsideFragment = (ChatInsideGroupFragment) mPagerAdapter.getItem(0);
					chatGroupProfileFragment = (ChatGroupProfileFragment) mPagerAdapter.getItem(1);
					chatGroupPhotosFragment = (ChatGroupPhotosFragment) mPagerAdapter.getItem(2);
				}
				if (groupChatInsideFragment != null) {

					try {
						long chatId = groupChatInsideFragment.getChatId();
						ChatLoad chatLoad1 = getChatLoad(chatId);

						showOne2OneChatFragment(chatLoad1, false, mViewPager, false);
					} catch (Exception e) {
						// TODO: handle exception
					}
					// if (mContactInfoUpdated) {
					// long chatId = one2OneChatInsideFragment.getChatId();
					// int chatColor = 0;
					// String chatName = "";
					// Contact c = findContactEntityByChatId(chatId);
					// if (c != null) {
					// chatColor = c.color;
					// chatName = c.title;
					// }
					// // final int fColor = chatColor;
					// // final String fName = chatName;
					//
					// mContactInfoUpdated = false;
					// showOne2OneChatFragment(c, false);
					//
					// }

					if (chatGroupProfileFragment != null) {

						List<ChatLoad> chatLoads = getChatList(chatType);

						for (ChatLoad chatLoad : chatLoads) {
							if (chatLoad.chatId == chatGroupProfileFragment.getChatload().chatId) {
								groupChatInsideFragment.setChatLoad(chatLoad);
								chatGroupProfileFragment.setChatload(chatLoad);
								// groupChatInsideFragment.setArgument(chatLoad);
								chatGroupProfileFragment.setChatload(chatLoad);
								if (chatGroupPhotosFragment != null) {
									chatGroupPhotosFragment.setChatLoad(chatLoad);
									chatGroupPhotosFragment.updateUi();
								}
								// chatGroupPhotosFragment.setArgument(chatLoad);
								break;
							}
						}
						chatGroupProfileFragment.setData();
					}

					groupChatInsideFragment.updateUi();
					if (chatGroupPhotosFragment != null) {
						chatGroupPhotosFragment.updateUi();
					}
					mViewPager.setCurrentItem(fullChatIndex);

				}

			}
		} catch (Exception e) {
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
			// TODO: handle exception
		}
	}

	@SuppressLint("NewApi")
	@Override
	public void onBackPressed() {
		MainActivity.isKeyboardVisible = false;
		// ChatInsideFragment oneToOneChatFragment = (ChatInsideFragment)
		// getSupportFragmentManager()
		// .findFragmentByTag(CHAT_INSIDE_FRAGMENT);
		// FullViewChatFragment oneToOneChatFragment = (FullViewChatFragment)
		// getSupportFragmentManager()
		// .findFragmentByTag(CHAT_INSIDE_FRAGMENT);
		//
		// if (oneToOneChatFragment != null) {
		// mActionBar.setDisplayShowHomeEnabled(false);
		// mActionBar.setDisplayHomeAsUpEnabled(false);
		// mActionBar.setDisplayShowCustomEnabled(true);
		// mSearchMenuItem.setVisible(true);
		// if (mIsSearchActive)
		// MenuItemCompat.expandActionView(mSearchMenuItem);
		// mSearchView.setQuery(mSearchText, false);
		// getActionBar().show(); // show the action bar when back from full
		// // chat view
		// }
		if (!getActionBar().isShowing()) {
			// messageColorMap.clear();
			getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN);
			getActionBar().show();
			fullChatIndex = 0;
			// mViewPager.setVisibility(View.INVISIBLE);
			final FrameLayout expandedImageView = (FrameLayout) findViewById(R.id.expanded_chatView);
			// expandedImageView.setVisibility(View.INVISIBLE);
			zoomOut(expandedImageView);
			if (mPagerAdapter != null) {
				Long chatId = null;
				if (mPagerAdapter.getItem(0) instanceof ChatInsideFragment) {
					ChatInsideFragment chatInsideFragment = (ChatInsideFragment) mPagerAdapter
							.getItem(0);
					chatId = chatInsideFragment.getChatId();
				} else if (mPagerAdapter.getItem(0) instanceof ChatInsideGroupFragment) {
					ChatInsideGroupFragment chatInsideGroupFragment = (ChatInsideGroupFragment) mPagerAdapter
							.getItem(0);
					chatId = chatInsideGroupFragment.getChatId();
				}
				if (chatId != null) {
					QodemePreferences.getInstance().set("" + chatId, null);
					List<Message> messages = getChatMessages(chatId);
					if (messages != null)
						messageRead(chatId);
					// for (Message message : messages) {
					// if (message.state ==
					// QodemeContract.Messages.State.NOT_READ) {
					// getContentResolver().update(
					// QodemeContract.Messages.CONTENT_URI,
					// QodemeContract.Messages.msssageReadLocalValues(),
					// QodemeContract.Messages.MESSAGE_ID + "="
					// + message.messageId, null);
					// }
					// }

				}
			}
			return;
		}

		super.onBackPressed();
	}

	@SuppressLint("NewApi")
	private void zoomOut(final View expandedImageView) {
		AnimatorSet set = new AnimatorSet();
		set.play(
				ObjectAnimator.ofFloat(expandedImageView, View.X,
						(startBounds.right - startBounds.left) / 2))
				.with(ObjectAnimator.ofFloat(expandedImageView, View.Y, startBounds.top + 50))
				.with(ObjectAnimator.ofFloat(expandedImageView, View.SCALE_X, startScaleFinal))
				.with(ObjectAnimator.ofFloat(expandedImageView, View.SCALE_Y, startScaleFinal));
		set.setDuration(mShortAnimationDuration);
		set.setInterpolator(new DecelerateInterpolator());
		set.addListener(new AnimatorListenerAdapter() {
			@Override
			public void onAnimationEnd(Animator animation) {
				// thumbView.setAlpha(1f);
				expandedImageView.setVisibility(View.GONE);
				mCurrentAnimator = null;
			}

			@Override
			public void onAnimationCancel(Animator animation) {
				// thumbView.setAlpha(1f);
				expandedImageView.setVisibility(View.GONE);
				mCurrentAnimator = null;
			}
		});
		set.start();
		mCurrentAnimator = set;
	}

	@Override
	public List<Contact> getContactList() {
		// if (chatType == 0 && isOneToOneSearch())
		// return mChatListSearchOneToOne;
		List<Contact> contacts = Lists.newArrayList();
		if (mApprovedContacts != null)
			contacts.addAll(mApprovedContacts);
		if (mBlockedContacts != null)
			contacts.addAll(mBlockedContacts);
		return contacts;
		// return mApprovedContacts;
	}

	@Override
	public List<ChatLoad> getChatList(int chatType) {
		// if (chatType == 2) {
		// if (isPublicSearch()) {
		// return mChatListSearchPublic;
		// }
		// }
		// else if (chatType == 1) {
		// if (isPrivateSearch()) {
		// return mChatListSearchPrivate;
		// }
		// }
		// else if(chatType == 0){
		// if(isOneToOneSearch()){
		// return mChatListSearchOneToOne;
		// }
		// }
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
			// if (isPublicSearch()) {
			// return mChatListSearchPublic;
			// }
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
		// List<Message> temp = Lists.newArrayList();
		// if (messages != null) {
		// if (mBlockContacts != null && mBlockContacts.size() > 0) {
		// //
		// for (Message msg : messages) {
		// boolean isBlock = false;
		// for (Contact contact : mBlockContacts) {
		// if (msg.qrcode.trim().equals(contact.qrCode.trim())) {
		// isBlock = true;
		// break;
		// }
		// }
		// if (!isBlock)
		// temp.add(msg);
		// }
		// } else {
		// temp.addAll(messages);
		// return temp;
		// }
		// // // if (temp.size() > 0)
		// // // messages.removeAll(temp);
		// }
		// return temp;
		return messages;
		// return sortMessages(mChatMessagesMap.get(chatId));
	}

	/**
	 * sort by timestamp
	 * 
	 * @param messages
	 * @return
	 */
	private List<Message> sortMessages(List<Message> messages) {

		// List<User> users = userDao.loadUsersWithIds(userIds);
		// Ordering<Message> orderById =
		// Ordering.explicit(userIds).onResultOf(UserFunctions.getId());
		// return orderById.immutableSortedCopy(users);
		if (messages != null) {
			Collections.sort(messages, new Comparator<Message>() {
				@Override
				public int compare(Message u1, Message u2) {
					return u1.timeStamp.compareTo(u2.timeStamp);
				}
			});
		}
		return messages;
	}

	@Override
	public void sendMessage(final long chatId, String message, String photoUrl, int hashPhoto,
			long replyTo_Id, double latitude, double longitude, String senderName, String localUrl) {

		ChatLoad chatLoad = null;// getChatLoad(chatId);
		try {
			// String public_name =
			// QodemePreferences.getInstance().getPublicName();
			try {
				if (mChatList != null) {
					for (ChatLoad chat : mChatList)
						if (chat.chatId == chatId)
							// return chatLoad;
							chatLoad = chat;
				}

			} catch (Exception e) {
			}

			int is_search = 0;
			if (chatLoad == null) {
				is_search = 1;
				try {
					if (mChatListSearchPublic != null) {
						for (ChatLoad chat : mChatListSearchPublic)
							if (chat.chatId == chatId)
								// return chatLoad;
								chatLoad = chat;
					}
					if (chatLoad != null)
						chatLoad.is_favorite = 1;
				} catch (Exception e) {
				}
				// String date = Converter.getCurrentGtmTimestampString();
				// RestAsyncHelper.getInstance().setFavorite(date, 1, chatId,
				// new RestListener<SetFavoriteResponse>() {
				//
				// @Override
				// public void onResponse(SetFavoriteResponse response) {
				//
				// }
				//
				// @Override
				// public void onServiceError(RestError error) {
				//
				// }
				// });

				// RestAsyncHelper.getInstance().chatAddMember(chatId,
				// QodemePreferences.getInstance().getQrcode(),
				// new RestListener<ChatAddMemberResponse>() {
				//
				// @Override
				// public void onResponse(ChatAddMemberResponse response) {
				// Log.d("Chat add in public ", "Chat add mem "
				// + response.getChat().getId());
				// }
				//
				// @Override
				// public void onServiceError(RestError error) {
				// Log.d("Error", "Chat add member");
				// }
				// });
			} else {
				is_search = 0;

				try {
					if (chatLoad.type == 2) {

						int num_of_favorite = chatLoad.number_of_likes;
						int is_favorite = 1;
						if (chatLoad.is_favorite == 1) {
							// is_favorite = 2;
							// num_of_favorite--;
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
									QodemeContract.Chats.CHAT_ID + " = " + chatId, null);
							String date = Converter.getCurrentGtmTimestampString();
						}
					}
				} catch (Exception e) {
				}
			}
			getContentResolver().insert(
					QodemeContract.Messages.CONTENT_URI,
					QodemeContract.Messages.addNewMessageValues(chatId, message, photoUrl,
							hashPhoto, replyTo_Id, latitude, longitude, senderName, localUrl,
							is_search));

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
								});
					}
				}, 1000);

			}
		} catch (Exception e) {
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
		Integer height = mChatHeightMap.get(chatId);
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
				showOne2OneChatFragment(c, firstUpdate, view, true);
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
				showOne2OneChatFragment(c, firstUpdate, view, true);
			}
		});
	}

	@Override
	public Typeface getFont(Fonts font) {
		Typeface result = fontMap.get(font);
		if (result == null) {
			result = Typeface.createFromAsset(getAssets(), font.toString());
			fontMap.put(font, result);
		}
		return result;
	}

	@Override
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

		/*
		 * if (mIsFirstResume){ mIsFirstResume = false; } else {
		 */
		getSupportLoaderManager().restartLoader(1, null, mContactListLoader);
		getSupportLoaderManager().restartLoader(2, null, mMessageListLoader);
		getSupportLoaderManager().restartLoader(3, null, mChatLoadListLoader);
		// }
		getContentResolver().registerContentObserver(QodemeContract.Contacts.CONTENT_URI, true,
				mContactListObserver);
		getContentResolver().registerContentObserver(QodemeContract.Messages.CONTENT_URI, true,
				mMessageListObjerver);
		getContentResolver().registerContentObserver(QodemeContract.Chats.CONTENT_URI, true,
				mChatListObserver);
		start();

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
			mSyncObserverHandle = null;
		}
		getContentResolver().unregisterContentObserver(mContactListObserver);
		getContentResolver().unregisterContentObserver(mMessageListObjerver);
		Helper.hideKeyboard(this);
		stop();
		unregisterReceiver(broadcastReceiverForDeleteChat);
	}

	public boolean isActive() {
		return mActive;
	}

	@Override
	protected void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		outState.putString("searchText", mSearchText);
	}

	/**
	 * Crfate a new anonymous SyncStatusObserver. It's attached to the app's
	 * ContentResolver in onResume(), and removed in onPause(). If status
	 * changes, it sets the state of the Refresh button. If a sync is active or
	 * pending, the Refresh button is replaced by an indeterminate ProgressBar;
	 * otherwise, the button itself is displayed.
	 */
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
					// Create a handle to the account that was created by
					// SyncService.CreateSyncAccount(). This will be used to
					// query the system to
					// see how the sync status has changed.
					Account account = GenericAccountService.GetAccount();
					if (account == null) {
						setRefreshActionButtonState(false);
						return;
					}
				}
			});
		}
	};

	/**
	 * Set the state of the Refresh button. If a sync is active, turn on the
	 * ProgressBar widget. Otherwise, turn it off.
	 * 
	 * @param refreshing
	 *            True if an active sync is occuring, false otherwise
	 */
	public void setRefreshActionButtonState(boolean refreshing) {
		// TODO refresh view
		/*
		 * if (mOptionsMenu == null) { return; }
		 * 
		 * final MenuItem refreshItem =
		 * mOptionsMenu.findItem(R.id.menu_refresh); if (refreshItem != null) {
		 * if (refreshing) {
		 * refreshItem.setActionView(R.layout.actionbar_indeterminate_progress);
		 * } else { refreshItem.setActionView(null); } }
		 */
	}

	private Activity getActivity() {
		return this;
	}

	private void clearActivityCache() {
		mContacts = Lists.newArrayList();
		mApprovedContacts = Lists.newArrayList();
	}

	Rect startBounds = new Rect();
	float startScaleFinal;

	@SuppressLint("NewApi")
	private void zoomImageFromThumb(final View thumbView, int imageResId) {

		// If there's an animation in progress, cancel it immediately and
		// proceed with this one.
		if (mCurrentAnimator != null) {
			mCurrentAnimator.cancel();
		}

		// Load the high-resolution "zoomed-in" image.
		final FrameLayout expandedImageView = (FrameLayout) findViewById(R.id.expanded_chatView);
		// expandedImageView.setImageResource(imageResId);

		// Calculate the starting and ending bounds for the zoomed-in image.
		// This step
		// involves lots of math. Yay, math.
		startBounds = new Rect();
		final Rect finalBounds = new Rect();
		final Point globalOffset = new Point();

		// The start bounds are the global visible rectangle of the thumbnail,
		// and the
		// final bounds are the global visible rectangle of the container view.
		// Also
		// set the container view's offset as the origin for the bounds, since
		// that's
		// the origin for the positioning animation properties (X, Y).
		thumbView.getGlobalVisibleRect(startBounds);
		findViewById(R.id.content_frame).getGlobalVisibleRect(finalBounds, globalOffset);
		startBounds.offset(-globalOffset.x, -globalOffset.y);
		finalBounds.offset(-globalOffset.x, -globalOffset.y);

		// Adjust the start bounds to be the same aspect ratio as the final
		// bounds using the
		// "center crop" technique. This prevents undesirable stretching during
		// the animation.
		// Also calculate the start scaling factor (the end scaling factor is
		// always 1.0).
		float startScale;
		if ((float) finalBounds.width() / finalBounds.height() > (float) startBounds.width()
				/ startBounds.height()) {
			// Extend start bounds horizontally
			startScale = (float) 50 / finalBounds.height();
			float startWidth = startScale * finalBounds.width();
			float deltaWidth = (startWidth - 50) / 2;
			startBounds.left -= deltaWidth;
			startBounds.right += deltaWidth;
		} else {
			// Extend start bounds vertically
			startScale = (float) 50 / finalBounds.width();
			float startHeight = startScale * finalBounds.height();
			float deltaHeight = (startHeight - 50) / 2;
			startBounds.top -= deltaHeight;
			startBounds.bottom += deltaHeight;
		}

		// Hide the thumbnail and show the zoomed-in view. When the animation
		// begins,
		// it will position the zoomed-in view in the place of the thumbnail.
		// thumbView.setAlpha(0f);
		expandedImageView.setVisibility(View.VISIBLE);

		// Set the pivot point for SCALE_X and SCALE_Y transformations to the
		// top-left corner of
		// the zoomed-in view (the default is the center of the view).
		expandedImageView.setPivotX(0f);
		expandedImageView.setPivotY(0f);

		// Construct and run the parallel animation of the four translation and
		// scale properties
		// (X, Y, SCALE_X, and SCALE_Y).
		AnimatorSet set = new AnimatorSet();
		set.play(
				ObjectAnimator.ofFloat(expandedImageView, View.X,
						(startBounds.right - startBounds.left) / 2, finalBounds.left))
				.with(ObjectAnimator.ofFloat(expandedImageView, View.Y, startBounds.top,
						finalBounds.top))
				.with(ObjectAnimator.ofFloat(expandedImageView, View.SCALE_X, startScale, 1f))
				.with(ObjectAnimator.ofFloat(expandedImageView, View.SCALE_Y, startScale, 1f));
		set.setDuration(mShortAnimationDuration);
		set.setInterpolator(new DecelerateInterpolator());
		set.addListener(new AnimatorListenerAdapter() {
			@Override
			public void onAnimationEnd(Animator animation) {
				mCurrentAnimator = null;
			}

			@Override
			public void onAnimationCancel(Animator animation) {
				mCurrentAnimator = null;
			}
		});
		set.start();
		mCurrentAnimator = set;

		// Upon clicking the zoomed-in image, it should zoom back down to the
		// original bounds
		// and show the thumbnail instead of the expanded image.
		startScaleFinal = startScale;
		expandedImageView.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				if (mCurrentAnimator != null) {
					mCurrentAnimator.cancel();
				}

				// Animate the four positioning/sizing properties in parallel,
				// back to their
				// original values.
				AnimatorSet set = new AnimatorSet();
				set.play(ObjectAnimator.ofFloat(expandedImageView, View.X, startBounds.left))
						.with(ObjectAnimator.ofFloat(expandedImageView, View.Y, startBounds.top))
						.with(ObjectAnimator.ofFloat(expandedImageView, View.SCALE_X,
								startScaleFinal))
						.with(ObjectAnimator.ofFloat(expandedImageView, View.SCALE_Y,
								startScaleFinal));
				set.setDuration(mShortAnimationDuration);
				set.setInterpolator(new DecelerateInterpolator());
				set.addListener(new AnimatorListenerAdapter() {
					@Override
					public void onAnimationEnd(Animator animation) {
						thumbView.setAlpha(1f);
						expandedImageView.setVisibility(View.GONE);
						mCurrentAnimator = null;
					}

					@Override
					public void onAnimationCancel(Animator animation) {
						thumbView.setAlpha(1f);
						expandedImageView.setVisibility(View.GONE);
						mCurrentAnimator = null;
					}
				});
				set.start();
				mCurrentAnimator = set;
			}
		});

	}

	public void setCurrentChatId(long currentChatId) {
		this.currentChatId = currentChatId;
	}

	public long getCurrentChatId() {
		return currentChatId;
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
		// int color = getResources().getColor(R.color.text_message_not_read);
		Contact contact = null;
		if (qrString != null) {
			//
			List<Contact> contacts = Lists.newArrayList();
			if (mApprovedContacts != null)
				contacts.addAll(mApprovedContacts);
			if (mBlockedContacts != null)
				contacts.addAll(mBlockedContacts);
			//
			if (contacts != null)// if (getContactList() != null)
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
		mDrawerLayout.openDrawer(mContactListView);
	}

	private void addMemberInChat(final List<Contact> contactsList) {
		isAddMemberOnExistingChat = false;
		Log.d("contact add in public", contactsList.get(0).title + "");

		// final ChatLoad chatload = getChatLoad(currentChatId);
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
					});
		}
		// setChatInfo(currentChatId, null, chatload.color, chatload.tag,
		// chatload.description,
		// chatload.status, chatload.is_locked, chatload.title);

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
				});
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

	@SuppressLint("NewApi")
	public void fullImageView(View v, Message msg) {
		final Intent i = new Intent(getActivity(), ImageDetailActivity.class);
		i.putExtra(ImageDetailActivity.EXTRA_IMAGE, msg.photoUrl);
		i.putExtra("flag", msg.is_flagged);
		i.putExtra("message_id", msg.messageId);
		if (Utils.hasJellyBean()) {
			// makeThumbnailScaleUpAnimation() looks kind of ugly here as the
			// loading spinner may
			// show plus the thumbnail image in GridView is cropped. so using
			// makeScaleUpAnimation() instead.
			ActivityOptions options = ActivityOptions.makeScaleUpAnimation(v, 0, 0, v.getWidth(),
					v.getHeight());
			getActivity().startActivity(i, options.toBundle());
		} else {
			// android.support.v4.app.ActivityOptionsCompat
			// activityOptionsCompat =
			// android.support.v4.app.ActivityOptionsCompat.makeScaleUpAnimation(v,
			// 0, 0, v.getWidth(),
			// v.getHeight());
			// getActivity().startActivity(i, activityOptionsCompat.toBundle());
			startActivity(i);
		}
	}

	public void searchChats(String searchString, final int type, final int pageNo,
			final LoadMoreChatListener chatListener) {
		if (type == 2) {
			setPublicSearch(true);
		} else if (type == 1) {
			setPrivateSearch(true);
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
									// Log.d("lookup", entity.getTitle() + " " +
									// entity.getTags()
									// + " " + entity.getId());
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
									for (ChatLoad c : mChatList) {
										if (c.chatId == entity.getId()) {
											chatLoad.isSearchResult = false;
											chatLoad2 = c;
											break;
										}
									}

									if (chatLoad2 == null)
										mChatListSearchPublic.add(chatLoad);
								}
								chatListener.onSearchResult(response.getChatList().size(), 1);
								refreshOne2OneList();
							}
							// else if (type == 1) {
							// if (mChatListSearchPrivate == null || pageNo ==
							// 1)
							// mChatListSearchPrivate = Lists.newArrayList();
							// for (LookupChatEntity entity :
							// response.getChatList()) {
							// Log.d("lookup", entity.getTitle() + " " +
							// entity.getTags()
							// + " " + entity.getId());
							// ChatLoad chatLoad = new ChatLoad();
							// chatLoad.title = entity.getTitle();
							// chatLoad.chatId = entity.getId();
							// chatLoad.qrcode = entity.getQrcode();
							// chatLoad.tag = entity.getTags();
							// chatLoad.status = entity.getStatus();
							// chatLoad.description = entity.getDescription();
							// chatLoad.number_of_likes =
							// entity.getNumber_of_likes();
							// chatLoad.number_of_members =
							// entity.getNumber_of_member();
							// chatLoad.latitude = entity.getLatitude();
							// chatLoad.longitude = entity.getLongitude();
							// chatLoad.type = 1;
							// mChatListSearchPrivate.add(chatLoad);
							//
							// }
							// chatListener.onSearchResult(response.getChatList().size(),
							// 1);
							// refreshOne2OneList();
							// } else if (type == 0) {
							// if (mChatListSearchOneToOne == null || pageNo ==
							// 1)
							// mChatListSearchOneToOne = Lists.newArrayList();
							// for (LookupChatEntity entity :
							// response.getChatList()) {
							// Log.d("lookup", entity.getTitle() + " " +
							// entity.getTags()
							// + " " + entity.getId());
							// Contact chatLoad = new Contact();
							// chatLoad.title = entity.getTitle();
							// chatLoad.chatId = entity.getId();
							// // chatLoad.qrcode = entity.getQrcode();
							// // chatLoad.tag = entity.getTags();
							// mChatListSearchOneToOne.add(chatLoad);
							//
							// }
							// // chatListener.onSearchResult("", 1);
							// chatListener.onSearchResult(response.getChatList().size(),
							// 1);
							// refreshOne2OneList();
							// }
						} else {
							chatListener.onSearchResult(0, 2);
							Log.d("lookup", "null response");
							// Toast.makeText(MainActivity.this,
							// getString(R.string.no_network_connection_toast),
							// Toast.LENGTH_SHORT).show();
						}
					}

					@Override
					public void onServiceError(RestError error) {
						chatListener.onSearchResult(0, 2);
						Toast.makeText(getActivity(), error.getServerMsg(), Toast.LENGTH_SHORT)
								.show();
					}
				});
	}

	public void getMessageFromSearch() {
		List<ChatLoad> chatLoads = getChatList(2);
		if (chatLoads != null) {
			for (final ChatLoad chatLoad : chatLoads) {
				RestAsyncHelper.getInstance().chatLoad(chatLoad.chatId, 1, 100,
						new RestListener<ChatLoadResponse>() {

							@Override
							public void onResponse(ChatLoadResponse response) {
								if (response != null) {
									ChatLoad chat = response.getChatLoad();
									if (chat != null) {
										chatLoad.messages = chat.messages;
									}
								}
							}

							@Override
							public void onServiceError(RestError error) {

							}
						});
			}
			refreshOne2OneList();
		}
	}

	public void setPublicSearch(boolean isPublicSearch) {
		this.isPublicSearch = isPublicSearch;
	}

	public boolean isPublicSearch() {
		return isPublicSearch;
	}

	public void setPrivateSearch(boolean isPrivateSearch) {
		this.isPrivateSearch = isPrivateSearch;
	}

	public boolean isPrivateSearch() {
		return isPrivateSearch;
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

	public void setPrivateSearchString(String privateSearchString) {
		this.privateSearchString = privateSearchString;
	}

	public String getPrivateSearchString() {
		return privateSearchString;
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

	public interface LoadMoreChatListener {
		public void onSearchResult(int count, int responseCode);
	}

	public void addPrivateAdapter() {

	}

	private void start() {
		final String wsuri = "ws://54.204.45.228/python";

		try {
			if (!mConnection.isConnected()) {
				mConnection.connect(wsuri, new WebSocketHandler() {

					@Override
					public void onOpen() {
						// Log.d(TAG, "Status: Connected to " + wsuri);
						// mConnection.sendTextMessage("Hello, world!");
						// sendRegisterForChatEvents();
						if (mChatList != null)
							for (ChatLoad chatLoad : mChatList) {
								if (chatLoad.type != 2)
									sendRegisterForChatEvents(chatLoad.chatId);
							}
					}

					@Override
					public void onTextMessage(String payload) {
						// Log.d(TAG, "Got echo: " + payload);
						receiveWebSocketMessageWith(payload);
					}

					@Override
					public void onClose(int code, String reason) {
						// Log.d(TAG, "Connection lost.");
					}
				});
			}
		} catch (WebSocketException e) {

			Log.d(TAG, e.toString());
		}
	}

	private void stop() {
		try {
			if (mConnection.isConnected()) {
				// let's disconnect the socket
				mConnection.disconnect();
				Log.d(TAG, "Disconnected web socket");
			}

		} catch (Exception e) {
			Log.d(TAG, e.toString());

		}
	}

	public void sendUserStoppedTypingMessage(long chatId) {
		String activityName = "sendUserStoppedTypingMessage:";
		if (mConnection.isConnected()) {
			// we have a open web socket connection
			// long chatId = getChatId();
			String restToken = QodemePreferences.getInstance().getRestToken();
			int event = GetEventForUserStoppedTypingMessage();
			Log.d(TAG, activityName + "Sending user stopped typing message...");
			sendWebSocketMessageWith(chatId, restToken, event);
		}
	}

	public void sendUserTypingMessage(long chatId) {
		String activityName = "sendUserTypingMessage:";
		// this will send over the web socket a message that the user has begun
		// typing
		if (mConnection.isConnected()) {
			sendRegisterForChatEvents(chatId);
			// we need the chat id
			// long chatId = getChatId();
			// the auth token
			String restToken = QodemePreferences.getInstance().getRestToken();

			int event = GetEventForUserStartedTypingMessage();
			Log.d(TAG, activityName + "Sending user typing message...");
			sendWebSocketMessageWith(chatId, restToken, event);

		}
	}

	private void sendWebSocketMessageWith(long chatId, String authToken, int event) {
		String activityName = "sendWebSocketMessageWith:";

		if (mConnection.isConnected()) {

			try {
				JSONObject json = new JSONObject();
				json.put("chatId", chatId);
				json.put("authToken", authToken);
				json.put("event", event);
				// json.put("event1", authToken);

				// now we send the message
				mConnection.sendTextMessage(json.toString());

				Log.d(TAG, activityName + "Successfully sent payload " + json.toString());
			} catch (JSONException e) {
				Log.e(TAG, activityName + "Received JSONException: " + e.toString());
			} catch (Exception e) {
				Log.e(TAG, activityName + "Received Exception: " + e.toString());

			}
		}
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

	private void sendRegisterForChatEvents(long chatId) {
		// this method sends a message over the websocket registering for
		// notifications that
		// are related to the current chat id
		String activityName = "sendRegisterForChatEvents:";

		if (mConnection.isConnected()) {
			// we need the chat id
			// long chatId = getChatId();
			// the auth token
			String restToken = QodemePreferences.getInstance().getRestToken();

			int event = GetEventForChatEvents();
			Log.d(TAG, activityName + "Sending register for chat event message...");
			sendWebSocketMessageWith(chatId, restToken, event);

		}
	}

	// This method is called to handle the data received as part of a web socket
	// message
	private void receiveWebSocketMessageWith(String message) {
		String activityName = "receiveWebSocketMessageWith:";

		try {
			JSONObject messageJson = new JSONObject(message);
			long chatId = messageJson.getLong("chatId");
			int event = messageJson.getInt("event");
			// String token = messageJson.getString("event1");

			Log.d(TAG, activityName + "Received event: " + event + " in chat: " + chatId);
			if (event == GetEventForUserStartedTypingMessage()) {
				// if
				// (QodemePreferences.getInstance().getRestToken().equals(token))
				receiveOtherUserStartedTypingEvent(chatId);
			} else if (event == GetEventForUserStoppedTypingMessage()) {
				receiveOtherUserStoppedTypingEvent(chatId);
			}

		} catch (JSONException je) {
			Log.e(TAG, activityName + je.toString());
		}
	}

	private void receiveOtherUserStoppedTypingEvent(long chatId) {
		// if (chatId == getChatId()) {
		// // this.imgUserTyping.setVisibility(View.INVISIBLE);
		// customDotViewUserTyping.setVisibility(View.INVISIBLE);
		// footerView.setVisibility(View.GONE);
		// isUsertyping = false;
		// }
		ChatLoad chatLoad = getChatLoad(chatId);
		if (chatLoad != null) {
			chatLoad.isTyping = false;
			// refreshOne2OneList();
			one2OneChatListFragment = (ChatListFragment) adapter.getItem(0);
			privateChatListFragment = (ChatListGroupFragment) adapter.getItem(1);
			publicChatListFragment = (ChatListGroupPublicFragment) adapter.getItem(2);

			if (chatLoad.type == 0)
				one2OneChatListFragment.notifyUi(chatId, chatLoad);
			else if (chatLoad.type == 1)
				privateChatListFragment.notifyUi(chatId, chatLoad);
			else
				publicChatListFragment.notifyUi(chatId, chatLoad);

		}
	}

	private void receiveOtherUserStartedTypingEvent(long chatId) {
		ChatLoad chatLoad = getChatLoad(chatId);
		if (chatLoad != null) {
			if (!chatLoad.isTyping)
				chatLoad.isTyping = true;

			// refreshOne2OneList();
			one2OneChatListFragment = (ChatListFragment) adapter.getItem(0);
			privateChatListFragment = (ChatListGroupFragment) adapter.getItem(1);
			publicChatListFragment = (ChatListGroupPublicFragment) adapter.getItem(2);

			if (chatLoad.type == 0)
				one2OneChatListFragment.notifyUi(chatId, chatLoad);
			else if (chatLoad.type == 1)
				privateChatListFragment.notifyUi(chatId, chatLoad);
			else
				publicChatListFragment.notifyUi(chatId, chatLoad);
		}
		// if (chatId == getChatId()) {
		// // we make visible the image view to show the other user is typing
		// // this.imgUserTyping.setVisibility(View.VISIBLE);
		// customDotViewUserTyping.setVisibility(View.VISIBLE);
		// footerView.setVisibility(View.VISIBLE);
		// if (!isUsertyping) {
		// handlerForUserTyping.sendEmptyMessageDelayed(0, 500);
		// isUsertyping = true;
		// }
		// }
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
					}
				}
			} else if (intent.getAction().equals(CHAT_ADDED_BRODCAST_ACTION)) {
				// try {
				// long chat_id = intent.getLongExtra("chat_id", -1);
				// if (chat_id != -1) {
				// String date = Converter.getCurrentGtmTimestampString();
				// RestAsyncHelper.getInstance().setFavorite(date, 1, chat_id,
				// new RestListener<SetFavoriteResponse>() {
				//
				// @Override
				// public void onResponse(SetFavoriteResponse response) {
				//
				// }
				//
				// @Override
				// public void onServiceError(RestError error) {
				//
				// }
				// });
				// }
				// } catch (Exception e) {
				// }
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
	};

	private void email() {

		ChatLoad chatLoad = getChatLoad(currentChatId);
		if (chatLoad != null) {

			// Bitmap mBitmap =
			// QrUtils.encodeQrCode((TextUtils.isEmpty(chatLoad.qrcode) ?
			// "Qr Code"
			// : ApplicationConstants.QR_CODE_CONTACT_PREFIX + chatLoad.qrcode),
			// 500, 500,
			// Color.BLACK, Color.WHITE);
			// // Bitmap mBitmap = BitmapFactory.decodeResource(getResources(),
			// // R.drawable.bg_qr_temp);
			// String path =
			// MediaStore.Images.Media.insertImage(getContentResolver(),
			// mBitmap,
			// "title", null);
			// if (path == null) {
			// showMessage(getString(R.string.alert_no_access_to_external_storage));
			// return;
			// }
			//
			// List<Message> messages = getChatMessages(chatLoad.chatId);
			// int total = 0;
			// int photo = 0;
			// if (messages != null) {
			// total = messages.size();
			// for (Message me : messages)
			// if (me.hasPhoto == 1)
			// photo++;
			// } else {
			// if (chatLoad.isSearchResult) {
			// Message[] messages2 = chatLoad.messages;
			// if (messages2 != null) {
			// total = messages2.length;
			// for (Message me : messages2)
			// if (me.hasPhoto == 1)
			// photo++;
			// }
			// }
			// }
			// int member = chatLoad.number_of_members == 0 ? 1 :
			// chatLoad.number_of_members;
			//
			// String data =
			// "<html><body><h1>Join the Conversation</h1><hr><br><p>The conversation "
			// + chatLoad.title
			// +
			// " has been shared with you. Scan the attached code to join the conversation.</p><br><br><h2>"
			// + chatLoad.title
			// + "</h2><br><p>"
			// + member
			// + " members, "
			// + total
			// + " messages, "
			// + photo
			// + " photos</p>"
			// +
			// "<a href=\"code:other/parameter\"> View Conversation </a> <br><hr><h2>What is Code Me?</h2><br><p>Lorem ipsum dolor sit amet, sldfha consectetur adipisicing elit, sed do eiusmod tempor incididunt ut lab et dolore magna eliqua.</p><br><h2>Available On</h2><br><a href=\"http://play.google.com/store/apps/details?id=com.blulabellabs.code\"> Google Play </a></body></html>";
			//
			// Uri screenshotUri = Uri.parse(path);
			// final Intent emailIntent = new Intent(Intent.ACTION_SEND);
			// emailIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
			// emailIntent.putExtra(Intent.EXTRA_STREAM, screenshotUri);
			// emailIntent.setType("image/png");
			// emailIntent.putExtra(Intent.EXTRA_TEXT, Html.fromHtml(data));
			// emailIntent.putExtra(Intent.EXTRA_SUBJECT, "QODEME contact");
			// startActivity(Intent.createChooser(emailIntent,
			// "Send email using"));

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
			}
			Bitmap mBitmap = QrUtils.encodeQrCode(
					(ApplicationConstants.QR_CODE_CONTACT_PREFIX + jsonObject), 500, 500,
					Color.BLACK, Color.WHITE);
			// Bitmap mBitmap =
			// QrUtils.encodeQrCode((TextUtils.isEmpty(chatLoad.qrcode) ?
			// "Qr Code"
			// : ApplicationConstants.QR_CODE_CONTACT_PREFIX + chatLoad.qrcode),
			// 500, 500,
			// Color.BLACK, Color.WHITE);
			// Bitmap mBitmap = BitmapFactory.decodeResource(getResources(),
			// R.drawable.bg_qr_temp);
			path = MediaStore.Images.Media
					.insertImage(getContentResolver(), mBitmap, "title", null);
			// if (path == null) {
			// showMessage(getString(R.string.alert_no_access_to_external_storage));
			// return;
			// }

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
}
