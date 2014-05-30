package biz.softtechnics.qodeme.ui;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
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

import android.accounts.Account;
import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Fragment;
import android.content.ContentProviderOperation;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SyncStatusObserver;
import android.content.res.Configuration;
import android.database.ContentObserver;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
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
import android.support.v4.app.FragmentTransaction;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.view.MenuItemCompat;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.support.v4.widget.DrawerLayout;
import android.support.v4.widget.DrawerLayout.DrawerListener;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.SearchView;
import android.text.TextUtils;
import android.util.Base64;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.Animation;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.LinearInterpolator;
import android.view.animation.ScaleAnimation;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;
import biz.softtechnics.qodeme.Application;
import biz.softtechnics.qodeme.R;
import biz.softtechnics.qodeme.core.accounts.GenericAccountService;
import biz.softtechnics.qodeme.core.data.IntentKey;
import biz.softtechnics.qodeme.core.data.entities.ChatType;
import biz.softtechnics.qodeme.core.data.preference.QodemePreferences;
import biz.softtechnics.qodeme.core.io.RestAsyncHelper;
import biz.softtechnics.qodeme.core.io.model.ChatLoad;
import biz.softtechnics.qodeme.core.io.model.Contact;
import biz.softtechnics.qodeme.core.io.model.Message;
import biz.softtechnics.qodeme.core.io.model.ModelHelper;
import biz.softtechnics.qodeme.core.io.responses.BaseResponse;
import biz.softtechnics.qodeme.core.io.responses.ChatAddMemberResponse;
import biz.softtechnics.qodeme.core.io.responses.ChatCreateResponse;
import biz.softtechnics.qodeme.core.io.responses.VoidResponse;
import biz.softtechnics.qodeme.core.io.utils.RestError;
import biz.softtechnics.qodeme.core.io.utils.RestErrorType;
import biz.softtechnics.qodeme.core.io.utils.RestListener;
import biz.softtechnics.qodeme.core.provider.QodemeContract;
import biz.softtechnics.qodeme.core.sync.SyncHelper;
import biz.softtechnics.qodeme.images.utils.ImageCache;
import biz.softtechnics.qodeme.images.utils.ImageFetcher;
import biz.softtechnics.qodeme.ui.common.FullChatListAdapter;
import biz.softtechnics.qodeme.ui.common.MenuListAdapter;
import biz.softtechnics.qodeme.ui.common.MenuListAddToChatAdapter;
import biz.softtechnics.qodeme.ui.contacts.ContactDetailsActivity;
import biz.softtechnics.qodeme.ui.contacts.ContactListItem;
import biz.softtechnics.qodeme.ui.contacts.ContactListItemEntity;
import biz.softtechnics.qodeme.ui.contacts.ContactListItemInvited;
import biz.softtechnics.qodeme.ui.one2one.ChatGroupPhotosFragment;
import biz.softtechnics.qodeme.ui.one2one.ChatGroupProfileFragment;
import biz.softtechnics.qodeme.ui.one2one.ChatInsideFragment;
import biz.softtechnics.qodeme.ui.one2one.ChatInsideGroupFragment;
import biz.softtechnics.qodeme.ui.one2one.ChatListFragment;
import biz.softtechnics.qodeme.ui.one2one.ChatListGroupFragment;
import biz.softtechnics.qodeme.ui.one2one.ChatListGroupPublicFragment;
import biz.softtechnics.qodeme.ui.one2one.ChatProfileFragment;
import biz.softtechnics.qodeme.ui.preferences.SettingsActivity;
import biz.softtechnics.qodeme.ui.qr.QrCodeCaptureActivity;
import biz.softtechnics.qodeme.ui.qr.QrCodeShowActivity;
import biz.softtechnics.qodeme.ui.tutorial.TutorialActivity;
import biz.softtechnics.qodeme.utils.AnalyticsHelper;
import biz.softtechnics.qodeme.utils.Converter;
import biz.softtechnics.qodeme.utils.DbUtils;
import biz.softtechnics.qodeme.utils.Fonts;
import biz.softtechnics.qodeme.utils.Helper;
import biz.softtechnics.qodeme.utils.LatLonCity;
import biz.softtechnics.qodeme.utils.LocationUtils;
import biz.softtechnics.qodeme.utils.MyLocation;
import biz.softtechnics.qodeme.utils.MyLocation.LocationResult;
import biz.softtechnics.qodeme.utils.NullHelper;

import com.android.volley.VolleyError;
import com.flurry.sdk.ch;
import com.flurry.sdk.en;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesClient;
import com.google.android.gms.internal.cn;
import com.google.android.gms.location.LocationClient;
import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.primitives.Longs;

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

	private static final String CHAT_LIST_FRAGMENT = "chat_list_fragment";
	private static final String CHAT_LIST_PRIVATE_FRAGMENT = "chat_list_private_fragment";
	private static final String CHAT_LIST_PUBLIC_FRAGMENT = "chat_list_public_fragment";
	private static final String CHAT_INSIDE_FRAGMENT = "chat_inside_fragment";
	private static final int DEFAULT_HEIGHT_DP = 200;

	private int mDefaultHeightPx;
	private DrawerLayout mDrawerLayout;
	private ListView mContactListView;
	private ActionBar mActionBar;
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
	private List<ChatLoad> mChatList;
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
	private ViewPager mViewPager;

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

	private void initChatListFragment() {
		ChatListFragment one2OneChatListFragment = new ChatListFragment();
		FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
		transaction.add(R.id.content_frame, one2OneChatListFragment, CHAT_LIST_FRAGMENT);
		transaction.commit();
	}

	@SuppressLint("NewApi")
	public void showOne2OneChatFragment(Contact c, boolean firstUpdate, View v) {
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
		// final FrameLayout expandedImageView = (FrameLayout)
		// findViewById(R.id.expanded_chatView);
		// expandedImageView.setVisibility(View.VISIBLE);
		zoomImageFromThumb(v, 0);
		// ScaleAnimation animation = new ScaleAnimation(0f, 1f, 0f, 1f,
		// Animation.RELATIVE_TO_SELF,
		// (float) 0.5, Animation.RELATIVE_TO_SELF, (float) 0.5);
		// animation.setDuration(500);
		// expandedImageView.setAnimation(animation);
		// animation.start();
		Helper.hideKeyboard(this);
	}

	@SuppressLint("NewApi")
	public void showOne2OneChatFragment(ChatLoad c, boolean firstUpdate, View view) {
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
		if (mPagerAdapter != null)
			mPagerAdapter = null;
		mPagerAdapter = new FullChatListAdapter(getSupportFragmentManager(), c, firstUpdate);
		mViewPager.setAdapter(mPagerAdapter);
		// final FrameLayout expandedImageView = (FrameLayout)
		// findViewById(R.id.expanded_chatView);
		// expandedImageView.setVisibility(View.VISIBLE);
		// mViewPager.setVisibility(View.GONE);
		zoomImageFromThumb(view, 0);
		Helper.hideKeyboard(MainActivity.this);
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
		customActionView.findViewById(R.id.imgBtn_one2one).setOnClickListener(
				new View.OnClickListener() {

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

						ChatListFragment one2OneChatListFragment = (ChatListFragment) getSupportFragmentManager()
								.findFragmentByTag(CHAT_LIST_FRAGMENT);
						if (one2OneChatListFragment != null) {

						} else {
							one2OneChatListFragment = new ChatListFragment();
						}
						FragmentTransaction transaction = getSupportFragmentManager()
								.beginTransaction();
						transaction.replace(R.id.content_frame, one2OneChatListFragment,
								CHAT_LIST_FRAGMENT);
						transaction.commit();
					}
				});
		customActionView.findViewById(R.id.imgBtn_private).setOnClickListener(
				new View.OnClickListener() {

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

						ChatListGroupFragment privateChatListFragment = (ChatListGroupFragment) getSupportFragmentManager()
								.findFragmentByTag(CHAT_LIST_PRIVATE_FRAGMENT);
						if (privateChatListFragment == null) {
							privateChatListFragment = new ChatListGroupFragment(1);
						}
						FragmentTransaction transaction = getSupportFragmentManager()
								.beginTransaction();
						transaction.replace(R.id.content_frame, privateChatListFragment,
								CHAT_LIST_PRIVATE_FRAGMENT);
						transaction.commit();
					}
				});
		customActionView.findViewById(R.id.imgBtn_public).setOnClickListener(
				new View.OnClickListener() {

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

						ChatListGroupPublicFragment publicChatListFragment = (ChatListGroupPublicFragment) getSupportFragmentManager()
								.findFragmentByTag(CHAT_LIST_PUBLIC_FRAGMENT);
						if (publicChatListFragment == null) {
							publicChatListFragment = new ChatListGroupPublicFragment(2);
						}
						// ChatListGroupFragment privateChatListFragment = new
						// ChatListGroupFragment(2);
						FragmentTransaction transaction = getSupportFragmentManager()
								.beginTransaction();
						transaction.replace(R.id.content_frame, publicChatListFragment,
								CHAT_LIST_PUBLIC_FRAGMENT);
						transaction.commit();

					}
				});
		customActionView.findViewById(R.id.imgBtn_add).setOnClickListener(
				new View.OnClickListener() {

					@Override
					public void onClick(View v) {
						// mContactListView.setAdapter(mContactListAddChatAdapter);
						if (chatType == 2) {
							List<Contact> contacts = Lists.newArrayList();
							createChat(contacts);
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
				if (QodemePreferences.getInstance().getQrcode().equals(qrCode)) {
					showMessage("You can't add own QR code!");
					return;
				}

				int type = data.getIntExtra(IntentKey.CHAT_TYPE, -1);
				if ((type & QrCodeCaptureActivity.QODEME_CONTACT) == QrCodeCaptureActivity.QODEME_CONTACT
						&& !TextUtils.isEmpty(qrCode)) {
					Cursor c = getContentResolver().query(QodemeContract.Contacts.CONTENT_URI,
							new String[] { QodemeContract.Contacts._ID },
							QodemeContract.Contacts.CONTACT_QRCODE + " = '" + qrCode + "'", null,
							null);
					if (!c.moveToFirst()) {
						getContentResolver().insert(QodemeContract.Contacts.CONTENT_URI,
								QodemeContract.Contacts.addNewContactValues(qrCode));
						SyncHelper.requestManualSync();
					} else {
						showMessage("It's already your contact!");
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
								chatLoad.chat_status, chatLoad.is_locked, chatLoad.title);
					else
						setChatInfo(currentChatId, null, color, null, null, null, null, null);
				}
				mViewPager.setCurrentItem(1);
				break;
			case REQUEST_ACTIVITY_PHOTO_GALLERY:
				if (!(data == null)) {
					System.gc();
					Uri mImageCaptureUri = data.getData();
					File file = new File(getPath(mImageCaptureUri));
					// selectedImagePath = getPath(targetUri);
					// File newFile = new File(selectedImagePath);
					try {
						System.out.println(file.exists());
						// InputStream is = new FileInputStream(file);

						// byte[] data1 = new byte[is.available()];
						// is.read(data1);
						/*
						 * mProfileImageBase64 = Base64.encodeToString(data1,
						 * 0);
						 */
						// Log.i("s---", "" + photoData);
						try {
							// Bitmap resizedBitmap = decodeFile(file);
							// Bitmap resizedBitmap = Media.getBitmap(
							// getContentResolver(), targetUri);
							// resizedBitmap = Bitmap.createScaledBitmap(
							// resizedBitmap,
							// mImgProfile[selectedImagePosition].getWidth(),
							// mImgProfile[selectedImagePosition].getWidth(),
							// true);

							// Matrix matrix = new Matrix();
							// matrix.postRotate(getImageOrientation(file.getAbsolutePath().toString()
							// .trim()));
							// Bitmap rotatedBitmap =
							// Bitmap.createBitmap(resizedBitmap, 0, 0,
							// resizedBitmap.getWidth(),
							// resizedBitmap.getHeight(), matrix,
							// true);
							//
							// ByteArrayOutputStream stream = new
							// ByteArrayOutputStream();
							// rotatedBitmap.compress(Bitmap.CompressFormat.PNG,
							// 100, stream);
							// byte[] byteArray = stream.toByteArray();
							//
							// String mProfileImageBase64 =
							// Base64.encodeToString(byteArray,
							// Base64.NO_WRAP);

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
							// mImgProfile[selectedImagePosition]
							// .setImageBitmap(rotatedBitmap);
							// //
							// arrayListImageUrl.add(selectedImagePosition,file.getAbsolutePath());
							// arrayImageUrl[selectedImagePosition] = file
							// .getAbsolutePath().toString().trim();

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
			getMyLocation(location);
		}
	};

	private void getMyLocation(Location loc) {
		if (loc != null) {
			final LatLonCity latLonCity = new LatLonCity();
			latLonCity.setLat((int) (loc.getLatitude() * 1E6));
			latLonCity.setLon((int) (loc.getLongitude() * 1E6));
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
		// Pass any configuration change to the drawer toggls
		// mDrawerToggle.onConfigurationChanged(newConfig);
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

					if (ce.state == QodemeContract.Contacts.State.APPRUVED
							|| ce.state == QodemeContract.Contacts.State.INVITATION_SENT) {
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
		Button button = (Button) moreBtnView.findViewById(R.id.btn_more);
		button.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				Intent intent = new Intent(MainActivity.this, MoreOptionActivity.class);
				startActivityForResult(intent, REQUEST_ACTIVITY_MORE);
				mDrawerLayout.closeDrawers();
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
										} else
											createChat(selectedContact);
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

	private void createChat(final List<Contact> contactsList) {
		// Log.d("contact add", contactsList.get(0).title + "");
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
		}
		final double lat = latitude;
		final double lng = longitude;
		RestAsyncHelper.getInstance().chatCreate(mChatType, "", "", 0, "", 0, "", latitude,
				longitude, new RestListener<ChatCreateResponse>() {

					@Override
					public void onResponse(ChatCreateResponse response) {
						// TODO Auto-generated method stub
						Log.d("Chat create", "Chat Created " + response.getChat().getId());

						if (chatType == 2)
							QodemePreferences.getInstance().setNewPublicGroupChatId(
									response.getChat().getId());
						getContentResolver().insert(
								QodemeContract.Chats.CONTENT_URI,
								QodemeContract.Chats.addNewChatValues(response.getChat().getId(),
										response.getChat().getType(), response.getChat()
												.getQrcode(), QodemePreferences.getInstance()
												.getQrcode(), lat, lng));

						for (Contact contact : contactsList) {
							RestAsyncHelper.getInstance().chatAddMember(response.getChat().getId(),
									contact.qrCode, new RestListener<ChatAddMemberResponse>() {

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
					}

					@Override
					public void onServiceError(RestError error) {
						Log.d("Error", "Chat not Created");
					}
				});
	}

	@Override
	protected void onDestroy() {
		QodemePreferences.getInstance().setNewPublicGroupChatId(-1l);
		super.onDestroy();
	}

	private class ContactListLoader implements LoaderManager.LoaderCallbacks<Cursor> {

		@Override
		public Loader<Cursor> onCreateLoader(int i, Bundle bundle) {
			return new CursorLoader(getActivity(), QodemeContract.Contacts.CONTENT_URI,
					QodemeContract.Contacts.ContactQuery.PROJECTION, String.format(
							"%s IN (%d, %d, %d, %d)", QodemeContract.Contacts.CONTACT_STATE,
							QodemeContract.Contacts.State.APPRUVED,
							QodemeContract.Contacts.State.INVITATION_SENT,
							QodemeContract.Contacts.State.INVITED,
							QodemeContract.Contacts.State.BLOCKED_BY), null,
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
			// Log.d("Chatlist", "size " + mChatList.size() + " ");
			// refreshContactList();

			// if (mSearchView != null) {
			// initializeSearchView();
			// }
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
					QodemeContract.Messages.Query.PROJECTION, null, null,
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
				case QodemeContract.Contacts.State.BLOCKED_BY:
					return 3;
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
		mHandler.sendEmptyMessage(2);
	}

	private final ContentObserver mContactListObserver = new ContentObserver(new Handler()) {
		@Override
		public void onChange(boolean selfChange) {
			// TODO check, do we need this line
			/*
			 * if (getActivity() == null) { return; }
			 */

			getSupportLoaderManager().restartLoader(1, null, mContactListLoader);
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

		ChatListFragment one2OneChatListFragment = (ChatListFragment) getSupportFragmentManager()
				.findFragmentByTag(CHAT_LIST_FRAGMENT);

		ChatListGroupFragment privateChatListFragment = (ChatListGroupFragment) getSupportFragmentManager()
				.findFragmentByTag(CHAT_LIST_PRIVATE_FRAGMENT);
		ChatListGroupPublicFragment publicChatListFragment = (ChatListGroupPublicFragment) getSupportFragmentManager()
				.findFragmentByTag(CHAT_LIST_PUBLIC_FRAGMENT);
		if (one2OneChatListFragment != null)
			one2OneChatListFragment.updateUi();
		if (privateChatListFragment != null)
			privateChatListFragment.updateUi();
		if (publicChatListFragment != null)
			publicChatListFragment.updateUi();
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

		if (chatType == 0) {
			ChatInsideFragment one2OneChatInsideFragment = null;
			if (mPagerAdapter != null && !getActionBar().isShowing()) {
				one2OneChatInsideFragment = (ChatInsideFragment) mPagerAdapter.getItem(0);
			}
			if (one2OneChatInsideFragment != null) {
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
					showOne2OneChatFragment(c, false, mViewPager);

				}
				one2OneChatInsideFragment.updateUi();
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
							groupChatInsideFragment.setArgument(chatLoad);
							chatGroupProfileFragment.setChatload(chatLoad);
							if (chatGroupPhotosFragment != null)
								chatGroupPhotosFragment.setArgument(chatLoad);
							break;
						}
					}
					chatGroupProfileFragment.setData();
				}

				groupChatInsideFragment.updateUi();
				if (chatGroupPhotosFragment != null)
					chatGroupPhotosFragment.updateUi();
				mViewPager.setCurrentItem(fullChatIndex);

			}

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
			getActionBar().show();
			fullChatIndex = 0;
			// mViewPager.setVisibility(View.INVISIBLE);
			final FrameLayout expandedImageView = (FrameLayout) findViewById(R.id.expanded_chatView);
			expandedImageView.setVisibility(View.INVISIBLE);
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
					List<Message> messages = getChatMessages(chatId);
					if (messages != null)
						for (Message message : messages) {
							if (message.state == QodemeContract.Messages.State.NOT_READ) {
								getContentResolver().update(
										QodemeContract.Messages.CONTENT_URI,
										QodemeContract.Messages.msssageReadLocalValues(),
										QodemeContract.Messages.MESSAGE_ID + "="
												+ message.messageId, null);
							}
						}

				}
			}
			return;
		}
		// else {
		// new AlertDialog.Builder(this).setTitle(R.string.app_name)
		// .setMessage("Are you want to logout?")
		// .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
		//
		// @Override
		// public void onClick(DialogInterface dialog, int which) {
		// // TODO Auto-generated method stub
		// RestAsyncHelper.getInstance().registerToken("", new RestListener() {
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
		// error.getErrorType()) + error.getServerMsg());
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
		// }
		// }).setNegativeButton("No", new DialogInterface.OnClickListener() {
		//
		// @Override
		// public void onClick(DialogInterface dialog, int which) {
		// // TODO Auto-generated method stub
		// finish();
		// }
		// }).create().show();
		// }

		super.onBackPressed();
	}

	@Override
	public List<Contact> getContactList() {
		return mApprovedContacts;
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
		return tempList;
	}

	@Override
	public List<Message> getChatMessages(long chatId) {
		List<Message> messages = mChatMessagesMap.get(chatId);
		// List<Message> temp = Lists.newArrayList();
		// if (messages != null) {
		// if (mBlockContacts != null && mBlockContacts.size()>0) {
		//
		// for (Contact contact : mBlockContacts) {
		// for (Message msg : messages) {
		// if (!msg.qrcode.trim().equals(contact.qrCode.trim())) {
		// temp.add(msg);
		// }
		// }
		// }
		// }else{
		// //temp.addAll(messages);
		// return messages;
		// }
		// // if (temp.size() > 0)
		// // messages.removeAll(temp);
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
		// String public_name = QodemePreferences.getInstance().getPublicName();
		getContentResolver().insert(
				QodemeContract.Messages.CONTENT_URI,
				QodemeContract.Messages.addNewMessageValues(chatId, message, photoUrl, hashPhoto,
						replyTo_Id, latitude, longitude, senderName, localUrl));
		SyncHelper.requestManualSync();
		// Long rawContactId = ContentUris.parseId(uri);
		// String carId = uri.getPathSegments().get(1);

		// Log.d("insert", "Content inserted at: " + uri);
		// Log.d("insert", "Car_id: " + carId);
		//
		// Log.d("insert", uri + "");
		/*
		 * RestAsyncHelper.getInstance().chatMessage(chatId, message,
		 * unixTimeStamp, new RestListener() {
		 * 
		 * @Override public void onResponse(BaseResponse response) {
		 * refreshContactList(); }
		 * 
		 * @Override public void onServiceError(RestError error) {
		 * showMessage(error.getServerMsg()); }
		 * 
		 * @Override public void onNetworkError(VolleyError error) {
		 * showMessage(error.getMessage()); } });
		 */

	}

	@Override
	public int getHeight(long chatId) {
		Integer height = mChatHeightMap.get(chatId);
		return height != null ? height : mDefaultHeightPx;
	}

	@Override
	public void setChatHeight(long chatId, int height) {
		mChatHeightMap.put(chatId, height);
	}

	@Override
	public void showChat(Contact c, boolean firstUpdate, View view) {
		mContactInfoUpdated = false;
		showOne2OneChatFragment(c, firstUpdate, view);
	}

	@Override
	public void showChat(ChatLoad c, boolean firstUpdate, View view) {
		mContactInfoUpdated = false;
		showOne2OneChatFragment(c, firstUpdate, view);
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
	public void messageRead(long chatId) {
		if (getNewMessagesCount(chatId) > 0) {
			List<Message> messages = mChatMessagesMap.get(chatId);
			if (messages != null && !messages.isEmpty()) {
				ArrayList<ContentProviderOperation> batch = Lists.newArrayList();
				List<Long> idList = Lists.newArrayList();
				for (Message m : messages) {
					if (m.state == QodemeContract.Messages.State.NOT_READ) {
						idList.add(m._id);
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
		}
	}

	@Override
	protected void onResume() {
		super.onResume();
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

	}

	@Override
	protected void onPause() {
		super.onPause();
		mActive = false;
		if (mSyncObserverHandle != null) {
			ContentResolver.removeStatusChangeListener(mSyncObserverHandle);
			mSyncObserverHandle = null;
		}
		getContentResolver().unregisterContentObserver(mContactListObserver);
		getContentResolver().unregisterContentObserver(mMessageListObjerver);
		Helper.hideKeyboard(this);
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

	@SuppressLint("NewApi")
	private void zoomImageFromThumb(final View thumbView, int imageResId) {
		// // If there's an animation in progress, cancel it immediately and
		// // proceed with this one.
		// if (mCurrentAnimator != null) {
		// mCurrentAnimator.cancel();
		// }
		//
		// // Load the high-resolution "zoomed-in" image.
		// final FrameLayout expandedImageView = (FrameLayout)
		// findViewById(R.id.expanded_chatView);
		// // expandedImageView.setImageResource(imageResId);
		//
		// // Calculate the starting and ending bounds for the zoomed-in image.
		// // This step
		// // involves lots of math. Yay, math.
		// final Rect startBounds = new Rect();
		// final Rect finalBounds = new Rect();
		// final Point globalOffset = new Point();
		//
		// // The start bounds are the global visible rectangle of the
		// thumbnail,
		// // and the
		// // final bounds are the global visible rectangle of the container
		// view.
		// // Also
		// // set the container view's offset as the origin for the bounds,
		// since
		// // that's
		// // the origin for the positioning animation properties (X, Y).
		// thumbView.getGlobalVisibleRect(startBounds);
		// findViewById(R.id.content_frame).getGlobalVisibleRect(finalBounds,
		// globalOffset);
		// startBounds.offset(-globalOffset.x, -globalOffset.y);
		// finalBounds.offset(-globalOffset.x, -globalOffset.y);
		//
		// // Adjust the start bounds to be the same aspect ratio as the final
		// // bounds using the
		// // "center crop" technique. This prevents undesirable stretching
		// during
		// // the animation.
		// // Also calculate the start scaling factor (the end scaling factor is
		// // always 1.0).
		// float startScale;
		// if ((float) finalBounds.width() / finalBounds.height() > (float)
		// startBounds.width()
		// / startBounds.height()) {
		// // Extend start bounds horizontally
		// startScale = (float) startBounds.height() / finalBounds.height();
		// float startWidth = startScale * finalBounds.width();
		// float deltaWidth = (startWidth - startBounds.width()) / 2;
		// startBounds.left -= deltaWidth;
		// startBounds.right += deltaWidth;
		// } else {
		// // Extend start bounds vertically
		// startScale = (float) startBounds.width() / finalBounds.width();
		// float startHeight = startScale * finalBounds.height();
		// float deltaHeight = (startHeight - startBounds.height()) / 2;
		// startBounds.top -= deltaHeight;
		// startBounds.bottom += deltaHeight;
		// }
		//
		// // Hide the thumbnail and show the zoomed-in view. When the animation
		// // begins,
		// // it will position the zoomed-in view in the place of the thumbnail.
		// // thumbView.setAlpha(0f);
		// expandedImageView.setVisibility(View.VISIBLE);
		//
		// // Set the pivot point for SCALE_X and SCALE_Y transformations to the
		// // top-left corner of
		// // the zoomed-in view (the default is the center of the view).
		// expandedImageView.setPivotX(0f);
		// expandedImageView.setPivotY(0f);
		//
		// // Construct and run the parallel animation of the four translation
		// and
		// // scale properties
		// // (X, Y, SCALE_X, and SCALE_Y).
		// AnimatorSet set = new AnimatorSet();
		// set.play(
		// ObjectAnimator.ofFloat(expandedImageView, View.X, startBounds.left,
		// finalBounds.left))
		// .with(ObjectAnimator.ofFloat(expandedImageView, View.Y,
		// startBounds.top,
		// finalBounds.top))
		// .with(ObjectAnimator.ofFloat(expandedImageView, View.SCALE_X,
		// startScale, 1f))
		// .with(ObjectAnimator.ofFloat(expandedImageView, View.SCALE_Y,
		// startScale, 1f));
		// set.setDuration(mShortAnimationDuration);
		// set.setInterpolator(new DecelerateInterpolator());
		// set.addListener(new AnimatorListenerAdapter() {
		// @Override
		// public void onAnimationEnd(Animator animation) {
		// mCurrentAnimator = null;
		// }
		//
		// @Override
		// public void onAnimationCancel(Animator animation) {
		// mCurrentAnimator = null;
		// }
		// });
		// set.start();
		// mCurrentAnimator = set;
		//
		// // Upon clicking the zoomed-in image, it should zoom back down to the
		// // original bounds
		// // and show the thumbnail instead of the expanded image.
		// final float startScaleFinal = startScale;
		// expandedImageView.setOnClickListener(new View.OnClickListener() {
		// @Override
		// public void onClick(View view) {
		// if (mCurrentAnimator != null) {
		// mCurrentAnimator.cancel();
		// }
		//
		// // Animate the four positioning/sizing properties in parallel,
		// // back to their
		// // original values.
		// AnimatorSet set = new AnimatorSet();
		// set.play(ObjectAnimator.ofFloat(expandedImageView, View.X,
		// startBounds.left))
		// .with(ObjectAnimator.ofFloat(expandedImageView, View.Y,
		// startBounds.top))
		// .with(ObjectAnimator.ofFloat(expandedImageView, View.SCALE_X,
		// startScaleFinal))
		// .with(ObjectAnimator.ofFloat(expandedImageView, View.SCALE_Y,
		// startScaleFinal));
		// set.setDuration(mShortAnimationDuration);
		// set.setInterpolator(new DecelerateInterpolator());
		// set.addListener(new AnimatorListenerAdapter() {
		// @Override
		// public void onAnimationEnd(Animator animation) {
		// // thumbView.setAlpha(1f);
		// expandedImageView.setVisibility(View.GONE);
		// mCurrentAnimator = null;
		// }
		//
		// @Override
		// public void onAnimationCancel(Animator animation) {
		// // thumbView.setAlpha(1f);
		// expandedImageView.setVisibility(View.GONE);
		// mCurrentAnimator = null;
		// }
		// });
		// set.start();
		// mCurrentAnimator = set;
		// }
		// });

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
		final Rect startBounds = new Rect();
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
		set.setInterpolator(new LinearInterpolator());
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
		final float startScaleFinal = startScale;
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
		for (Contact c : getContactList()) {
			if (c.qrCode.equals(qrString)) {
				contact = c;
				break;
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
		refreshContactList();
		mContactListAdapter.notifyDataSetChanged();
		mDrawerLayout.openDrawer(mContactListView);
	}

	private void addMemberInChat(final List<Contact> contactsList) {
		isAddMemberOnExistingChat = false;
		Log.d("contact add in public", contactsList.get(0).title + "");

		// final ChatLoad chatload = getChatLoad(currentChatId);
		for (Contact contact : contactsList) {
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
			String status, Integer isLocked, String chat_title) {
		RestAsyncHelper.getInstance().chatSetInfo(chatId, title, color, tag, desc, isLocked,
				status, chat_title, new RestListener<VoidResponse>() {

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
}
