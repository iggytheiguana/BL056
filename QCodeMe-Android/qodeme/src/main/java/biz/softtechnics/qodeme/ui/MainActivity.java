package biz.softtechnics.qodeme.ui;

import android.accounts.Account;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.ContentProviderOperation;
import android.content.ContentResolver;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SyncStatusObserver;
import android.content.res.Configuration;
import android.database.ContentObserver;
import android.database.Cursor;
import android.graphics.Typeface;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.view.MenuItemCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.SearchView;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.android.volley.VolleyError;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesClient;
import com.google.android.gms.location.LocationClient;
import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.primitives.Longs;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.regex.Pattern;

import biz.softtechnics.qodeme.Application;
import biz.softtechnics.qodeme.R;
import biz.softtechnics.qodeme.core.accounts.GenericAccountService;
import biz.softtechnics.qodeme.core.data.IntentKey;
import biz.softtechnics.qodeme.core.data.preference.QodemePreferences;
import biz.softtechnics.qodeme.core.io.RestAsyncHelper;
import biz.softtechnics.qodeme.core.io.model.Contact;
import biz.softtechnics.qodeme.core.io.model.Message;
import biz.softtechnics.qodeme.core.io.model.ModelHelper;
import biz.softtechnics.qodeme.core.io.responses.BaseResponse;
import biz.softtechnics.qodeme.core.io.utils.RestError;
import biz.softtechnics.qodeme.core.io.utils.RestErrorType;
import biz.softtechnics.qodeme.core.io.utils.RestListener;
import biz.softtechnics.qodeme.core.provider.QodemeContract;
import biz.softtechnics.qodeme.core.sync.SyncHelper;
import biz.softtechnics.qodeme.ui.common.MenuListAdapter;
import biz.softtechnics.qodeme.ui.contacts.ContactDetailsActivity;
import biz.softtechnics.qodeme.ui.contacts.ContactListItem;
import biz.softtechnics.qodeme.ui.contacts.ContactListItemEntity;
import biz.softtechnics.qodeme.ui.contacts.ContactListItemInvited;
import biz.softtechnics.qodeme.ui.one2one.ChatInsideFragment;
import biz.softtechnics.qodeme.ui.one2one.ChatListFragment;
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
import biz.softtechnics.qodeme.utils.NullHelper;

public class MainActivity extends BaseActivity implements
        ChatListFragment.One2OneChatListFragmentCallback,
        ChatInsideFragment.One2OneChatInsideFragmentCallback,
        GooglePlayServicesClient.ConnectionCallbacks,
        GooglePlayServicesClient.OnConnectionFailedListener {

    private static final int REQUEST_ACTIVITY_SCAN_QR_CODE = 2;
    private static final int REQUEST_ACTIVITY_CONTACT_DETAILS = 3;
    private static final String CHAT_LIST_FRAGMENT = "chat_list_fragment";
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

    // Fonts cache
    private Map<Fonts, Typeface> fontMap = new HashMap();

    // Memory cache
    private MenuListAdapter<ContactListItemEntity> mContactListAdapter;
    private Map<Long, List<Message>> mChatMessagesMap = Maps.newHashMap();
    private Map<Long,Long> mLastMessageInChatMap;
    private Map<Long, Integer> mChatHeightMap;
    private boolean mContactInfoUpdated;
    private List<Contact> mContacts;
    private List<Contact> mApprovedContacts;
    private Map<Long, Integer> mChatNewMessagesMap;

    private ContactListLoader mContactListLoader;
    private MessageListLoader mMessageListLoader;
    private boolean mIsFirstResume;
    private LocationClient mLocationClient;
    //private Location mCurrentLocation;
    private String mSearchFilter;
    private boolean mKeyboardActive;


    /**
     * Handle to a SyncObserver. The ProgressBar element is visible until the SyncObserver reports
     * that the sync is complete.
     * <p/>
     * <p>This allows us to delete our SyncObserver once the application is no longer in the
     * foreground.
     */
    private Object mSyncObserverHandle;



    /**
     * Called when the activity is first created.
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        AnalyticsHelper.onCreateActivity(this);
        setContentView(R.layout.activity_main);
        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        //loadData();
        initActionBar();
        initContactsList();
        //TODO remove temporary solution
        ((Application) getApplication()).setMainActivity(this);
        initChatHeight();
        initChatListFragment();
        //randomColorGenerator = new RandomColorGenerator();
        if (savedInstanceState != null) {
            //Then the application is being reloaded
            mSearchText = savedInstanceState.getString("searchText");
        }
        mIsFirstResume = true;
        mLocationClient = new LocationClient(this, this, this);
        initKeyboardListener();
    }

    private void initKeyboardListener() {
        mDrawerLayout.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                int heightDiff = mDrawerLayout.getRootView().getHeight() - mDrawerLayout.getHeight();
                mKeyboardActive = (heightDiff > 100);
            }
        });
    }

    private Handler mHandler = new Handler()  // handler for commiting fragment after data is loaded
    {
        @Override
        public void handleMessage(android.os.Message msg) {
            if (msg.what == 2) // refresh chat inside
            {
                refreshOne2OneInside();
            }
        }
    };

    private void initChatHeight() {
        mDefaultHeightPx = Converter.dipToPx(getApplicationContext(), DEFAULT_HEIGHT_DP);
        //chatHeightDataSource = new ChatHeightDataSource(this);
        //chatHeightDataSource.open();
        mChatHeightMap = Maps.newHashMap();//chatHeightDataSource.getChatHeightMap();
        //chatHeightDataSource.close();
    }


    private void initChatListFragment() {
        ChatListFragment one2OneChatListFragment = new ChatListFragment();
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.add(R.id.content_frame, one2OneChatListFragment, CHAT_LIST_FRAGMENT);
        transaction.commit();
    }

    public void showOne2OneChatFragment(Contact c, boolean firstUpdate) {
        ChatInsideFragment chatInsideFragment = (ChatInsideFragment) getSupportFragmentManager().findFragmentByTag(CHAT_INSIDE_FRAGMENT);
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        if (chatInsideFragment != null) {

            getSupportFragmentManager().popBackStack();
            transaction.remove(chatInsideFragment);
        }
        chatInsideFragment = ChatInsideFragment.newInstance(c, firstUpdate);
        transaction.replace(R.id.content_frame, chatInsideFragment, CHAT_INSIDE_FRAGMENT);
        transaction.addToBackStack(null);
        transaction.commit();
        mActionBar.setDisplayShowHomeEnabled(true);
        mActionBar.setDisplayHomeAsUpEnabled(true);
        mActionBar.setDisplayShowCustomEnabled(false);
        mSearchMenuItem.setVisible(false);
        mIsSearchActive = mSearchView.isShown();
        mSearchText = mSearchView.getQuery().toString();
        if (mIsSearchActive) {
            MenuItemCompat.collapseActionView(mSearchMenuItem);
        }

    }

    private void initActionBar() {
        mActionBar = getSupportActionBar();
        mActionBar.setIcon(R.drawable.ic_action_camera);
        mActionBar.setDisplayShowTitleEnabled(false);
        mActionBar.setDisplayShowHomeEnabled(false);
        mActionBar.setDisplayShowCustomEnabled(true);
        View customActionView = getLayoutInflater().inflate(R.layout.action_home, null);
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
        customActionView.findViewById(R.id.home).setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                Intent i = new Intent(getContext(), QrCodeShowActivity.class);
                i.putExtra(IntentKey.QR_CODE, QodemePreferences.getInstance().getQrcode());
                startActivity(i);
                return true;
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK)
            switch (requestCode) {
                case REQUEST_ACTIVITY_SCAN_QR_CODE: {
                    final String qrCode = data.getStringExtra(IntentKey.QR_CODE);
                    if (QodemePreferences.getInstance().getQrcode().equals(qrCode)){
                        showMessage("You can't add own QR code!");
                        return;
                    }

                    int type = data.getIntExtra(IntentKey.CHAT_TYPE, -1);
                    if ((type & QrCodeCaptureActivity.QODEME_CONTACT) == QrCodeCaptureActivity.QODEME_CONTACT
                            && !TextUtils.isEmpty(qrCode)) {
                        Cursor c = getContentResolver().query(QodemeContract.Contacts.CONTENT_URI, new String[]{QodemeContract.Contacts._ID}, QodemeContract.Contacts.CONTACT_QRCODE + " = '" + qrCode + "'", null, null);
                        if (!c.moveToFirst()) {
                            getContentResolver().insert(
                                    QodemeContract.Contacts.CONTENT_URI,
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
                    String title = data.getStringExtra(QodemeContract.Contacts.CONTACT_TITLE);
                    int color = data.getIntExtra(QodemeContract.Contacts.CONTACT_COLOR, 0);
                    int updated = data.getIntExtra(QodemeContract.Contacts.UPDATED, QodemeContract.Contacts.Sync.UPDATED);
                    getContentResolver().update(
                            QodemeContract.Contacts.CONTENT_URI,
                            QodemeContract.Contacts.updateContactInfoValues(title, color, updated), DbUtils.getWhereClauseForId(), DbUtils.getWhereArgsForId(id));
                    SyncHelper.requestManualSync();
                    break;
            }
    }

    @Override
    protected void onStart() {
        super.onStart();
        // Connect the client.
        mLocationClient.connect();
        AnalyticsHelper.onStartActivity(this);
        /*Location l = null;
        l.getLongitude();*/
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
        //mCurrentLocation = mLocationClient.getLastLocation();
        Location loc = mLocationClient.getLastLocation();
        if (loc != null) {
            final LatLonCity latLonCity = new LatLonCity();
            latLonCity.setLat((int) (loc.getLatitude() * 1E6));
            latLonCity.setLon((int) (loc.getLongitude() * 1E6));
            new AsyncTask<LatLonCity, Void, String>() {

                @Override
                protected String doInBackground(LatLonCity... params) {
                    try {
                        List<Address> addresses = new Geocoder(getContext(), Locale.ENGLISH).getFromLocation(latLonCity.getLat() / 1E6, latLonCity.getLon() / 1E6, 1);
                        if (!addresses.isEmpty()) {
                            return addresses.get(0).getLocality();
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
        //To change body of created methods use File | Settings | File Templates.
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);
        mSearchMenuItem = menu.findItem(R.id.action_search);
        mSearchView = (SearchView) MenuItemCompat.getActionView(mSearchMenuItem);
        if (mSearchText != null) {
            mSearchView.setQuery(mSearchText, false);
        }
        if (getContactList() != null) {
            initializeSearchView();
        }

        return true;
    }

    private void initializeSearchView() {
        List<Contact> contacts = getContactList();
        String[] items = new String[contacts.size()];
        for (int i = 0; i < contacts.size(); i++) {
            Contact contact = contacts.get(i);
            items[i] = contact.title;
        }

        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this,
                android.R.layout.simple_dropdown_item_1line, items);

        final SearchView.SearchAutoComplete autoCompleteTextView = (SearchView.SearchAutoComplete) mSearchView.findViewById(R.id.search_src_text);
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
            case R.id.action_logout:
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
                                showMessage(RestErrorType.getMessage(getContext(), error.getErrorType()) + error.getServerMsg());
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
                        showMessage(RestErrorType.getMessage(getContext(), error.getErrorType()) + error.getServerMsg());
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

                return true;

            case R.id.action_share: {
                Intent i = new Intent(getContext(), QrCodeShowActivity.class);
                i.putExtra(IntentKey.QR_CODE, QodemePreferences.getInstance().getQrcode());
                startActivity(i);
                return true;
            }

            case R.id.action_help: {
                Intent i = new Intent(getContext(), TutorialActivity.class);
                startActivity(i);
                return true;
            }

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
        //mDrawerToggle.syncState();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        // Pass any configuration change to the drawer toggls
        //mDrawerToggle.onConfigurationChanged(newConfig);
    }

    private void initContactsList() {
        mContactListView = (ListView) findViewById(R.id.left_drawer);

        mContactListView.setOnItemClickListener(new DrawerItemClickListener() {
            @Override
            public void onItemClick(AdapterView parent, View view, int position, long id) {
                super.onItemClick(parent, view, position, id);
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
                    final CharSequence[] items = {"Unblock"};

                    AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                    builder.setTitle("Pick a color");
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

            }
        });

        mContactListView.setLongClickable(true);

        List<ContactListItemEntity> listForAdapter = Lists.newArrayList();

//        listForAdapter.addAll(mContactList); // it's important for safety of the orders list in the model.
        mContactListAdapter = new MenuListAdapter<ContactListItemEntity>(this, R.layout.contact_list_item, listForAdapter) {

            @Override
            public View getView(int position, View convertView, ViewGroup parent) {

                ContactListItemEntity e = getItem(position);

                if (e.isHeader()) {
                    if (convertView == null || (convertView instanceof ContactListItem) || (convertView instanceof ContactListItemInvited)) {
                        convertView = layoutInflater.inflate(R.layout.contact_list_item_header, null);
                    }

                    TextView tvHeader = ((TextView) convertView.findViewById(R.id.header));
                    switch (e.getState()) {
                        case QodemeContract.Contacts.State.INVITED:
                            tvHeader.setText("Invitation");
                            break;
                        case QodemeContract.Contacts.State.INVITATION_SENT:
                            tvHeader.setText("Invited");
                            break;
                        case QodemeContract.Contacts.State.APPRUVED:
                            tvHeader.setText("Contacts");
                            break;
                        case QodemeContract.Contacts.State.BLOCKED_BY:
                            tvHeader.setText("Blocked");
                            break;

                    }
                } else {
                    if (e.getState() == QodemeContract.Contacts.State.INVITED) {
                        ContactListItemInvited view;
                        if (convertView == null || !(convertView instanceof ContactListItemInvited)) {
                            view = (ContactListItemInvited) layoutInflater.inflate(R.layout.contact_list_item_invited, null);
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

        getSupportLoaderManager().initLoader(0, null, mContactListLoader);
        getSupportLoaderManager().initLoader(1, null, mMessageListLoader);

    }

    private class ContactListLoader implements LoaderManager.LoaderCallbacks<Cursor> {

        @Override
        public Loader<Cursor> onCreateLoader(int i, Bundle bundle) {
            return new CursorLoader(getActivity(), QodemeContract.Contacts.CONTENT_URI,
                    QodemeContract.Contacts.ContactQuery.PROJECTION,
                    String.format("%s IN (%d, %d, %d, %d)",
                            QodemeContract.Contacts.CONTACT_STATE,
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

    private class MessageListLoader implements LoaderManager.LoaderCallbacks<Cursor> {

        @Override
        public Loader<Cursor> onCreateLoader(int i, Bundle bundle) {
            return new CursorLoader(getActivity(), QodemeContract.Messages.CONTENT_URI,
                    QodemeContract.Messages.Query.PROJECTION,
                    null, null,
                    QodemeContract.Messages.DEFAULT_SORT);
        }

        @Override
        public void onLoadFinished(Loader<Cursor> cursorLoader, Cursor cursor) {
            ModelHelper.MessageStructure ms = ModelHelper.getChatMessagesMap(cursor);
            mChatMessagesMap =  ms.getMessageMap();
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
            filtredContacts = Lists.newArrayList(Iterables.filter(mContacts, new Predicate<Contact>() {
                @Override
                public boolean apply(Contact contact) {
                    return Pattern.compile(Pattern.quote(mSearchFilter), Pattern.CASE_INSENSITIVE).matcher(contact.title).find();
                }
            }));


        // Sort contact list by state
        Collections.sort(filtredContacts, new Comparator<Contact>() {
            @Override
            public int compare(Contact lhs, Contact rhs) {
                if (lhs.state == rhs.state) {
                    // TODO sort abc
                } else {
                    return Integer.valueOf(getStateWeight(lhs.state)).compareTo(getStateWeight(rhs.state));
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

            contactListItems.add(new ContactListItemEntity(true, filtredContacts.get(0).state, null));
        }
        for (Contact c : filtredContacts) {
            if (contactListItems.get(contactListItems.size() - 1).getState() != c.state) {
                contactListItems.add(new ContactListItemEntity(true, c.state, null));
            }
            contactListItems.add(new ContactListItemEntity(false, c.state, c));
        }

        // Refresh list of contacts
        mContactListAdapter.clear();
        mContactListAdapter.addAll(contactListItems);

        // Create list of approved contacts
        mApprovedContacts = Lists.newArrayList(Iterables.filter(filtredContacts, new Predicate<Contact>() {
            @Override
            public boolean apply(Contact contact) {
                return contact.state == QodemeContract.Contacts.State.APPRUVED;
            }
        }));

        mContactInfoUpdated = true;

        refreshOne2OneList();
        mHandler.sendEmptyMessage(2);
    }


    private final ContentObserver mContactListObserver = new ContentObserver(new Handler()) {
        @Override
        public void onChange(boolean selfChange) {
            // TODO check, do we need this line
            /*if (getActivity() == null) {
                return;
            }*/

            getSupportLoaderManager().restartLoader(1, null, mContactListLoader);
        }
    };

    private final ContentObserver mMessageListObjerver = new ContentObserver(new Handler()) {
        @Override
        public void onChange(boolean selfChange) {
            // TODO check, do we need this line
            /*if (getActivity() == null) {
                return;
            }*/

            getSupportLoaderManager().restartLoader(2, null, mMessageListLoader);
        }
    };

    private class ApprovedContactsComparator implements Comparator<Contact> {

        private Map<Long, Long> lastMessaheTimeMap;

        ApprovedContactsComparator(Map<Long, Long> lastMessaheTimeMap){
            this.lastMessaheTimeMap = lastMessaheTimeMap;
        }

        private long getLastTimeMarker(Long chatId){
            return lastMessaheTimeMap != null ? NullHelper.notNull(lastMessaheTimeMap.get(chatId), 0L) : 0L;
        }

        @Override
        public int compare(Contact lhs, Contact rhs) {
            return Longs.compare(getLastTimeMarker(rhs.chatId), getLastTimeMarker(lhs.chatId));
        }
    }

    private void refreshOne2OneList() {
        // Sort by new messages
        if (mApprovedContacts != null)
            Collections.sort(mApprovedContacts, new ApprovedContactsComparator(mLastMessageInChatMap));

        ChatListFragment one2OneChatListFragment = (ChatListFragment) getSupportFragmentManager().findFragmentByTag(CHAT_LIST_FRAGMENT);
        if (one2OneChatListFragment != null)
            one2OneChatListFragment.updateUi();
    }

    private void openChat(String name) {
        ChatListFragment one2OneChatListFragment = (ChatListFragment) getSupportFragmentManager().findFragmentByTag(CHAT_LIST_FRAGMENT);
        if (one2OneChatListFragment != null) {
            one2OneChatListFragment.openChat(name);
        }
    }

    private void refreshOne2OneInside() {
        final ChatInsideFragment one2OneChatInsideFragment = (ChatInsideFragment) getSupportFragmentManager().findFragmentByTag(CHAT_INSIDE_FRAGMENT);
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
                //final int fColor = chatColor;
                //final String fName = chatName;

                mContactInfoUpdated = false;
                showOne2OneChatFragment(c, false);

            }
            one2OneChatInsideFragment.updateUi();
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
        new AlertDialog.Builder(getContext()).setTitle("Attention")
                .setMessage(message)
                .setCancelable(true)
                .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                })
                .create().show();
    }

    @Override
    public void onBackPressed() {
        ChatInsideFragment oneToOneChatFragment = (ChatInsideFragment) getSupportFragmentManager().findFragmentByTag(CHAT_INSIDE_FRAGMENT);
        if (oneToOneChatFragment != null) {
            mActionBar.setDisplayShowHomeEnabled(false);
            mActionBar.setDisplayHomeAsUpEnabled(false);
            mActionBar.setDisplayShowCustomEnabled(true);
            mSearchMenuItem.setVisible(true);
            if (mIsSearchActive)
                MenuItemCompat.expandActionView(mSearchMenuItem);
            mSearchView.setQuery(mSearchText, false);
        }
        super.onBackPressed();
    }

    @Override
    public List<Contact> getContactList() {
        return mApprovedContacts;
    }

    @Override
    public List<Message> getChatMessages(long chatId) {
        return mChatMessagesMap.get(chatId);
    }

    @Override
    public void sendMessage(final long chatId, String message) {
        getContentResolver().insert(
                QodemeContract.Messages.CONTENT_URI,
                QodemeContract.Messages.addNewMessageValues(chatId, message));
        SyncHelper.requestManualSync();
        /*RestAsyncHelper.getInstance().chatMessage(chatId, message, unixTimeStamp, new RestListener() {
            @Override
            public void onResponse(BaseResponse response) {
                refreshContactList();
            }

            @Override
            public void onServiceError(RestError error) {
                showMessage(error.getServerMsg());
            }

            @Override
            public void onNetworkError(VolleyError error) {
                showMessage(error.getMessage());
            }
        });*/

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
    public void showChat(Contact c, boolean firstUpdate) {
        mContactInfoUpdated = false;
        showOne2OneChatFragment(c, firstUpdate);
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
        return mChatNewMessagesMap != null ? NullHelper.notNull(mChatNewMessagesMap.get(chatId), 0) : 0;
    }

    @Override
    public void messageRead(long chatId) {
        if (getNewMessagesCount(chatId) > 0) {
            List<Message> messages = mChatMessagesMap.get(chatId);
            if (messages != null && !messages.isEmpty()) {
                ArrayList<ContentProviderOperation> batch = Lists.newArrayList();
                List<Long> idList = Lists.newArrayList();
                for (Message m : messages){
                    if (m.state == QodemeContract.Messages.State.NOT_READ) {
                        idList.add(m._id);
                    }
                }
                long[] ids = Longs.toArray(idList);
                ContentProviderOperation.Builder builder = ContentProviderOperation
                        .newUpdate(QodemeContract.Messages.CONTENT_URI);
                builder.withValue(QodemeContract.SyncColumns.UPDATED, QodemeContract.Sync.UPDATED);
                builder.withValue(QodemeContract.Messages.MESSAGE_STATE, QodemeContract.Messages.State.READ_LOCAL);
                builder.withSelection(DbUtils.getWhereClauseForIds(ids), DbUtils.getWhereArgsForIds(ids));
                batch.add(builder.build());
                QodemeContract.applyBatch(getContext(), batch);
                SyncHelper.requestManualSync();
            }
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        mActive = true;
        mSyncStatusObserver.onStatusChanged(0);
        // Watch for sync state changes
        final int mask = ContentResolver.SYNC_OBSERVER_TYPE_PENDING |
                ContentResolver.SYNC_OBSERVER_TYPE_ACTIVE;
        mSyncObserverHandle = ContentResolver.addStatusChangeListener(mask, mSyncStatusObserver);

        /*if (mIsFirstResume){
            mIsFirstResume = false;
        } else {*/
        getSupportLoaderManager().restartLoader(1, null, mContactListLoader);
        getSupportLoaderManager().restartLoader(2, null, mMessageListLoader);
        //}
        getContentResolver().registerContentObserver(QodemeContract.Contacts.CONTENT_URI, true, mContactListObserver);
        getContentResolver().registerContentObserver(QodemeContract.Messages.CONTENT_URI, true, mMessageListObjerver);
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
     * Crfate a new anonymous SyncStatusObserver. It's attached to the app's ContentResolver in
     * onResume(), and removed in onPause(). If status changes, it sets the state of the Refresh
     * button. If a sync is active or pending, the Refresh button is replaced by an indeterminate
     * ProgressBar; otherwise, the button itself is displayed.
     */
    private SyncStatusObserver mSyncStatusObserver = new SyncStatusObserver() {
        /** Callback invoked with the sync adapter status changes. */
        @Override
        public void onStatusChanged(int which) {
            runOnUiThread(new Runnable() {
                /**
                 * The SyncAdapter runs on a background thread. To update the UI, onStatusChanged()
                 * runs on the UI thread.
                 */
                @Override
                public void run() {
                    // Create a handle to the account that was created by
                    // SyncService.CreateSyncAccount(). This will be used to query the system to
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
     * Set the state of the Refresh button. If a sync is active, turn on the ProgressBar widget.
     * Otherwise, turn it off.
     *
     * @param refreshing True if an active sync is occuring, false otherwise
     */
    public void setRefreshActionButtonState(boolean refreshing) {
        // TODO refresh view
        /*if (mOptionsMenu == null) {
            return;
        }

        final MenuItem refreshItem = mOptionsMenu.findItem(R.id.menu_refresh);
        if (refreshItem != null) {
            if (refreshing) {
                refreshItem.setActionView(R.layout.actionbar_indeterminate_progress);
            } else {
                refreshItem.setActionView(null);
            }
        }*/
    }

    private Activity getActivity() {
        return this;
    }

    private void clearActivityCache() {
        mContacts = Lists.newArrayList();
        mApprovedContacts = Lists.newArrayList();
    }

}