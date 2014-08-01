package com.blulabellabs.code.ui.one2one;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Typeface;
import android.location.Location;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.AbsListView;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.TextView.OnEditorActionListener;

import com.blulabellabs.code.R;
import com.blulabellabs.code.core.io.model.ChatLoad;
import com.blulabellabs.code.core.io.model.Contact;
import com.blulabellabs.code.core.io.model.Message;
import com.blulabellabs.code.images.utils.ImageFetcher;
import com.blulabellabs.code.ui.MainActivity;
import com.blulabellabs.code.ui.common.ExListAdapter;
import com.blulabellabs.code.ui.common.ScrollDisabledListView;
import com.blulabellabs.code.utils.ChatFocusSaver;
import com.blulabellabs.code.utils.Fonts;
import com.blulabellabs.code.utils.Helper;
import com.google.common.collect.Lists;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class ChatListFragment extends Fragment {

    private One2OneChatListFragmentCallback callback;
    private boolean isViewCreated = false;

    private ScrollDisabledListView mListView;
    private ExListAdapter<ChatListItem, Contact, ChatListAdapterCallback> mListAdapter;
    private ImageButton mImgBtnClear, mImgBtnLocationFilter, mImgBtnSearch, mImgBtnFavoriteFilter;
    private ImageView mImgViewSearchHint;
    private EditText mEditTextSearch;
    private LinearLayout mLinearLayoutSearch;
    protected boolean isFavoriteFilter = false;
    protected boolean isLocationFilter = false;
    List<Contact> contacts = Lists.newArrayList();

    int lastFirstVisibleItem = 0;
    int pageNo = 1;
    private String searchString = "";


    public interface One2OneChatListFragmentCallback {
        List<Contact> getContactList();

        List<ChatLoad> getChatList(int type);

        List<Message> getChatMessages(long chatId);

        void sendMessage(long chatId, String message, String photoUrl, int hashPhoto,
                         long replyTo_Id, double latitude, double longitude, String senderName,
                         String localUrl);

        int getHeight(long chatId);

        void setChatHeight(long chatId, int height);

        void showChat(Contact c, boolean firstUpdate, View view);

        void showChat(ChatLoad c, boolean firstUpdate, View view);

        int getNewMessagesCount(long chatId);

        void messageRead(long chatId);

        Contact getContact(String qrString);

        ImageFetcher getImageFetcher();

        int getChatType(long chatId);

        ChatLoad getChatLoad(long chatId);
    }

    public static ChatListFragment newInstance() {
        ChatListFragment chatListFragment = new ChatListFragment();
        Bundle args = new Bundle();
        args.putInt("index", 1);
        chatListFragment.setArguments(args);
        return chatListFragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        return inflater.inflate(R.layout.fragment_one2one_chat_list, null);
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        callback = (One2OneChatListFragmentCallback) activity;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        mImgBtnSearch = (ImageButton) getView().findViewById(R.id.imgBtn_search);
        mImgBtnClear = (ImageButton) getView().findViewById(R.id.imgBtn_clear);
        mImgBtnLocationFilter = (ImageButton) getView().findViewById(R.id.imgBtn_locationFilter);

        mEditTextSearch = (EditText) getView().findViewById(R.id.editText_Search);
        mImgBtnFavoriteFilter = (ImageButton) getView().findViewById(R.id.imgBtn_favoriteFilter);
        mImgViewSearchHint = (ImageView) getView().findViewById(R.id.searchHintIcon);
        mLinearLayoutSearch  = (LinearLayout) getView().findViewById(R.id.linearLayout_search);

        initListView();
        isViewCreated = true;

        mImgBtnClear.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                MainActivity activity = (MainActivity) callback;
                activity.setOneToOneSearch(false);
                activity.setOneToOneSearchString("");
                mEditTextSearch.setText("");
                mImgBtnClear.setVisibility(View.GONE);
                mEditTextSearch.setEnabled(true);
                mEditTextSearch.setText("");
                pageNo = 1;
                searchString = "";
                Bitmap bm = BitmapFactory.decodeResource(getResources(),
                        R.drawable.ic_location_gray);
                mImgBtnLocationFilter.setImageBitmap(bm);
                isLocationFilter = false;
                isFavoriteFilter = false;
                mImgBtnFavoriteFilter.setImageResource(R.drawable.ic_chat_favorite_h);
                updateUi();
            }
        });
        MainActivity activity = (MainActivity) callback;
        if (activity.isOneToOneSearch()) {
            searchString = activity.getOneToOneSearchString();
            mEditTextSearch.setText(searchString);
            mImgBtnClear.setVisibility(View.VISIBLE);
            mImgBtnSearch.setVisibility(View.GONE);
        } else {
            mImgBtnClear.setVisibility(View.GONE);
        }

        mImgBtnLocationFilter.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                if (isFavoriteFilter) {
                    isFavoriteFilter = false;
                    mImgBtnFavoriteFilter.setImageResource(R.drawable.ic_chat_favorite_h);
                }

                if (isLocationFilter) {
                    isLocationFilter = false;
                    Bitmap bm = BitmapFactory.decodeResource(getResources(),
                            R.drawable.ic_location_gray);
                    mImgBtnLocationFilter.setImageBitmap(bm);
                    updateUi();
                } else {
                    mImgBtnLocationFilter.setImageResource(R.drawable.ic_location_blue_big);
                    isLocationFilter = true;
                    List<Contact> temp = Lists.newArrayList();
                    List<Contact> searchList = searchContact(searchString);
                    for (Contact c : searchList) {
                        ChatLoad chatLoad = callback.getChatLoad(c.chatId);
                        if (chatLoad != null && chatLoad.latitude != null
                                && chatLoad.longitude != null && !chatLoad.latitude.equals("")
                                && !chatLoad.longitude.equals("") && !chatLoad.latitude.equals("0")
                                && !chatLoad.latitude.equals("0.0")
                                && !chatLoad.latitude.equals("-1")
                                && !chatLoad.longitude.equals("0")
                                && !chatLoad.longitude.equals("0.0")
                                && !chatLoad.longitude.equals("-1")) {
                            Log.d("latLong", chatLoad.latitude + " " + chatLoad.longitude);
                            Log.d("latLong", c.latitude + " " + c.longitude);
                            temp.add(c);
                        }
                    }
                    Collections.sort(temp, new CustomComparator());
                    mListAdapter.clear();
                    mListAdapter.addAll(temp);
                }
            }
        });

        mImgBtnFavoriteFilter.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                if (isLocationFilter) {
                    isLocationFilter = false;
                    Bitmap bm = BitmapFactory.decodeResource(getResources(),
                            R.drawable.ic_location_gray);
                    mImgBtnLocationFilter.setImageBitmap(bm);
                }
                if (isFavoriteFilter) {
                    isFavoriteFilter = false;
                    mImgBtnFavoriteFilter.setImageResource(R.drawable.ic_chat_favorite_h);
                    updateUi();
                } else {
                    mImgBtnFavoriteFilter.setImageResource(R.drawable.ic_star_blue);
                    isFavoriteFilter = true;
                    List<Contact> temp = Lists.newArrayList();
                    List<Contact> searchList = searchContact(searchString);
                    for (Contact c : searchList) {
                        ChatLoad chatLoad = callback.getChatLoad(c.chatId);
                        if (chatLoad != null && chatLoad.is_favorite == 1) {
                            temp.add(c);
                        }
                    }
                    mListAdapter.clear();
                    mListAdapter.addAll(temp);
                }
            }
        });
        updateUi();

        mImgBtnSearch.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                String data = mEditTextSearch.getText().toString();
                searchString = data;
                MainActivity activity = (MainActivity) callback;
                activity.setOneToOneSearch(true);
                activity.setOneToOneSearchString(data);

                List<Contact> searchList = searchContact(searchString);

                List<Contact> temp = Lists.newArrayList();
                if (isFavoriteFilter) {
                    for (Contact c : searchList) {
                        ChatLoad chatLoad = callback.getChatLoad(c.chatId);

                        if (chatLoad != null && chatLoad.is_favorite == 1) {
                            temp.add(c);
                        }
                    }
                } else if (isLocationFilter) {
                    for (Contact c : searchList) {
                        ChatLoad chatLoad = callback.getChatLoad(c.chatId);

                        if (chatLoad != null && chatLoad.latitude != null
                                && chatLoad.longitude != null && !chatLoad.latitude.equals("")
                                && !chatLoad.longitude.equals("") && !chatLoad.latitude.equals("0")
                                && !chatLoad.latitude.equals("0.0")
                                && !chatLoad.latitude.equals("-1")
                                && !chatLoad.longitude.equals("0")
                                && !chatLoad.longitude.equals("0.0")
                                && !chatLoad.longitude.equals("-1")) {
                            Log.d("latLong", chatLoad.latitude + " " + chatLoad.longitude);
                            Log.d("latLong", c.latitude + " " + c.longitude);
                            temp.add(c);
                        }
                    }

                    Collections.sort(temp, new CustomComparator());
                } else {
                    temp.addAll(searchList);
                }

                mListAdapter.clear();
                mListAdapter.addAll(temp);

                mImgBtnClear.setVisibility(View.VISIBLE);
                mImgBtnSearch.setVisibility(View.GONE);
            }
        });
        mEditTextSearch.setOnEditorActionListener(new OnEditorActionListener() {

            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_SEARCH
                        || actionId == EditorInfo.IME_ACTION_GO
                        || actionId == EditorInfo.IME_ACTION_DONE
                        || actionId == EditorInfo.IME_ACTION_UNSPECIFIED
                        || event.getAction() == KeyEvent.ACTION_DOWN
                        && event.getKeyCode() == KeyEvent.KEYCODE_ENTER) {

                    String data = v.getText().toString();
                    searchString = data;
                    MainActivity activity = (MainActivity) callback;
                    activity.setOneToOneSearch(true);
                    activity.setOneToOneSearchString(data);
                    List<Contact> searchList = searchContact(searchString);

                    List<Contact> temp = Lists.newArrayList();
                    if (isFavoriteFilter) {
                        for (Contact c : searchList) {
                            ChatLoad chatLoad = callback.getChatLoad(c.chatId);

                            if (chatLoad != null && chatLoad.is_favorite == 1) {
                                temp.add(c);
                            }
                        }
                    } else if (isLocationFilter) {
                        for (Contact c : searchList) {
                            ChatLoad chatLoad = callback.getChatLoad(c.chatId);

                            if (chatLoad != null && chatLoad.latitude != null
                                    && chatLoad.longitude != null && !chatLoad.latitude.equals("")
                                    && !chatLoad.longitude.equals("")
                                    && !chatLoad.latitude.equals("0")
                                    && !chatLoad.latitude.equals("0.0")
                                    && !chatLoad.latitude.equals("-1")
                                    && !chatLoad.longitude.equals("0")
                                    && !chatLoad.longitude.equals("0.0")
                                    && !chatLoad.longitude.equals("-1")) {
                                Log.d("latLong", chatLoad.latitude + " " + chatLoad.longitude);
                                Log.d("latLong", c.latitude + " " + c.longitude);
                                temp.add(c);
                            }
                        }

                        Collections.sort(temp, new CustomComparator());
                    } else {
                        temp.addAll(searchList);
                    }

                    mListAdapter.clear();
                    mListAdapter.addAll(temp);

                    mImgBtnClear.setVisibility(View.VISIBLE);
                    mImgBtnSearch.setVisibility(View.GONE);
                    return true;
                }
                return false;
            }
        });

        mEditTextSearch.addTextChangedListener(new TextWatcher() {

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

                if (mEditTextSearch.getText().toString().trim().length() > 0) {
                    mImgBtnClear.setVisibility(View.VISIBLE);
                    mImgViewSearchHint.setVisibility(View.INVISIBLE);
                } else {
                    mImgViewSearchHint.setVisibility(View.VISIBLE);
                    mImgBtnClear.setVisibility(View.GONE);
                }
            }

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
    }
//
//    public void openChat(String name) {
//        for (int i = 0; i < callback.getContactList().size(); i++) {
//            Contact contact = callback.getContactList().get(i);
//            if (contact.title.equalsIgnoreCase(name)) {
//                mListView.setSelection(i);
//
//                final int position = i;
//                mListView.post(new Runnable() {
//                    public void run() {
//
//                        int firstPosition = mListView.getFirstVisiblePosition()
//                                - mListView.getHeaderViewsCount(); // This is
//                        int wantedChild = position - firstPosition;
//                        if (wantedChild < 0 || wantedChild >= mListView.getChildCount()) {
//                            return;
//                        }
//                        ChatListItem chatListItem1 = (ChatListItem) mListView
//                                .getChildAt(wantedChild);
//                        if (chatListItem1 != null) {
//                            chatListItem1.showMessage();
//                        }
//                    }
//                });
//                break;
//            }
//        }
//    }
//
    public class CustomComparator implements Comparator<Contact> {

        @Override
        public int compare(Contact lhs, Contact rhs) {
            MainActivity activity = (MainActivity) callback;
            Location location = activity.getCurrentLocation();
            Integer leftDistance = 0;
            Integer rightDistance = 0;
            ChatLoad lhsChat = callback.getChatLoad(lhs.chatId);
            ChatLoad rhsChat = callback.getChatLoad(rhs.chatId);
            if (location != null && lhsChat != null && rhsChat != null) {
                try {
                    double lat = Double.parseDouble(lhsChat.latitude);
                    double lng = Double.parseDouble(lhsChat.longitude);
                    leftDistance = (int) distance(lat, lng, location.getLatitude(),
                            location.getLongitude(), 'M');

                } catch (Exception e) {
                    e.printStackTrace();
                }
                try {
                    double lat = Double.parseDouble(rhsChat.latitude);
                    double lng = Double.parseDouble(rhsChat.longitude);
                    rightDistance = (int) distance(lat, lng, location.getLatitude(),
                            location.getLongitude(), 'M');
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            return leftDistance.compareTo(rightDistance);
        }
    }

    private void initListView() {

        View headerSearchView = getLayoutInflater(getArguments()).inflate(
                R.layout.linear_search_header, null);

        mListView = (ScrollDisabledListView) getView().findViewById(R.id.listview);
        mListView.addHeaderView(headerSearchView);
        List<Contact> listForAdapter = Lists.newArrayList();

        mListAdapter = new ExListAdapter<ChatListItem, Contact, ChatListAdapterCallback>(
                getActivity(), R.layout.one2one_chat_list_item, listForAdapter, chatListCallback);
        mListView.setAdapter(mListAdapter);

        mListView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                return false;
            }
        });
        mListView.setOnScrollListener(new AbsListView.OnScrollListener() {

            @Override
            public void onScrollStateChanged(AbsListView view, int scrollState) {
                mListAdapter.isScroll = scrollState == SCROLL_STATE_FLING || scrollState == SCROLL_STATE_TOUCH_SCROLL;
            }

            @Override
            public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount,
                                 int totalItemCount) {
                if (lastFirstVisibleItem != firstVisibleItem) {
                    if (lastFirstVisibleItem > firstVisibleItem) {
                        if (mLinearLayoutSearch.getVisibility() != View.VISIBLE) {
                            mLinearLayoutSearch.setAlpha(0f);
                            mLinearLayoutSearch.setVisibility(View.VISIBLE);
                            mLinearLayoutSearch.animate().alpha(1f).setDuration(200)
                                    .setListener(null);
                        }
                    } else {
                        if (mLinearLayoutSearch.getVisibility() == View.VISIBLE) {
                            mLinearLayoutSearch.animate().alpha(0f).setDuration(500)
                                    .setListener(new AnimatorListenerAdapter() {
                                        @Override
                                        public void onAnimationEnd(Animator animation) {
                                            mLinearLayoutSearch.setVisibility(View.INVISIBLE);
                                        }
                                    });
                        }
                    }

                }
                if (firstVisibleItem == 0) {
                    mLinearLayoutSearch.setAlpha(1f);
                    mLinearLayoutSearch.setVisibility(View.VISIBLE);
                }
                lastFirstVisibleItem = firstVisibleItem;
            }
        });
    }

    public void updateUi() {
        if (isViewCreated && callback.getContactList() != null) {
            contacts.clear();
            contacts = Lists.newArrayList();
            for (Contact c : callback.getContactList()) {
                if (c.isArchive != 1)
                    contacts.add(c);
            }

            if (!isLocationFilter && !isFavoriteFilter) {
                mListAdapter.clear();
                mListAdapter.addAll(searchContact(searchString));
            } else {
                if (isLocationFilter) {
                    List<Contact> temp = Lists.newArrayList();
                    List<Contact> searchList = searchContact(searchString);
                    for (Contact c : searchList) {
                        ChatLoad chatLoad = callback.getChatLoad(c.chatId);

                        if (chatLoad != null && chatLoad.latitude != null
                                && chatLoad.longitude != null && !chatLoad.latitude.equals("")
                                && !chatLoad.longitude.equals("") && !chatLoad.latitude.equals("0")
                                && !chatLoad.latitude.equals("0.0")
                                && !chatLoad.latitude.equals("-1")
                                && !chatLoad.longitude.equals("0")
                                && !chatLoad.longitude.equals("0.0")
                                && !chatLoad.longitude.equals("-1")) {
                            Log.d("latLong", chatLoad.latitude + " " + chatLoad.longitude);
                            Log.d("latLong", c.latitude + " " + c.longitude);
                            temp.add(c);
                        }
                    }

                    Collections.sort(temp, new CustomComparator());
                    mListAdapter.clear();
                    mListAdapter.addAll(temp);
                } else {
                    List<Contact> temp = Lists.newArrayList();
                    List<Contact> searchList = searchContact(searchString);
                    for (Contact c : searchList) {
                        ChatLoad chatLoad = callback.getChatLoad(c.chatId);

                        if (chatLoad != null && chatLoad.is_favorite == 1) {
                            temp.add(c);
                        }
                    }

                    mListAdapter.clear();
                    mListAdapter.addAll(temp);
                }
            }
            long focusedChat = ChatFocusSaver.getFocusedChatId();
            selectChat(focusedChat);

        }
    }

    public void notifyUi(long chatId, ChatLoad chatLoad) {
        ChatListItem chatListGroupItem = null;
        for (int i = mListView.getFirstVisiblePosition(); i < mListView.getLastVisiblePosition(); i++) {
            Contact contact = mListAdapter.getItem(i);
            if (contact.chatId == chatId) {
                chatListGroupItem = (ChatListItem) mListView.getChildAt(i
                        - mListView.getFirstVisiblePosition() + 1);
                break;
            }
        }
        if (chatListGroupItem != null) {
            if (chatLoad.isTyping)
                chatListGroupItem.getUserTyping()
                        .setBackgroundResource(R.drawable.bg_user_typing_h);
            else
                chatListGroupItem.getUserTyping().setBackgroundResource(R.drawable.bg_user_typing);
        }

    }

    private List<Contact> searchContact(String searchString) {
        if (searchString.trim().equals("")) {
            return contacts;
        }
        List<Contact> temp = Lists.newArrayList();

        for (Contact c : contacts) {
            if (c.title != null && c.title.toLowerCase().contains(searchString.toLowerCase())) {
                temp.add(c);
            }
        }
        return temp;
    }

    private void selectChat(long chatId) {
        for (int i = 0; i < callback.getContactList().size(); i++) {
            Contact contact = callback.getContactList().get(i);
            if (contact.chatId == chatId) {

                final int position = i;
                mListView.post(new Runnable() {
                    public void run() {
                        showItemInListView(position);
                    }
                });
                return;
            }
        }
    }

    private void showItemInListView(int position) {
        if (mListView.getFirstVisiblePosition() > position
                || mListView.getLastVisiblePosition() < position) {
            mListView.setSelection(position);
        }
    }

    ChatListAdapterCallback chatListCallback = new ChatListAdapterCallback() {

        public void onSingleTap(View view, int position, Contact ce) {
        }

        public void onDoubleTap(View view, int position, Contact c) {
            Helper.hideKeyboard(view.getContext(), mEditTextSearch);
            callback.showChat(c, true, view);
        }

        public int getChatHeight(long chatId) {
            return callback.getHeight(chatId);
        }

        public void setChatHeight(long chatId, int height) {
            callback.setChatHeight(chatId, height);
        }

        public void setDragModeEnabled(boolean value) {
            mListView.setDragMode(value);
        }

        public List<Message> getMessages(Contact c) {
            return callback.getChatMessages(c.chatId);
        }

        @Override
        public int getNewMessagesCount(long chatId) {
            return callback.getNewMessagesCount(chatId);
        }

        @Override
        public void messageRead(long chatId) {
            callback.messageRead(chatId);
        }

        @Override
        public void sendMessage(Contact c, String message, String photoUrl,
                                int hashPhoto, long replyTo_Id, double latitude, double longitude,
                                String senderName, String localUrl) {
            callback.sendMessage(c.chatId, message, photoUrl, hashPhoto, replyTo_Id,
                    latitude, longitude, senderName, localUrl);

        }

        @Override
        public List<Message> getMessages(long chatId) {
            return null;
        }

        @Override
        public void sendMessage(long c, String message, String photoUrl, int hashPhoto,
                                long replyTo_Id, double latitude, double longitude, String senderName,
                                String localUrl) {
        }

        @Override
        public void onDoubleTap(View view, int position, ChatLoad c) {
        }

        @Override
        public void onSingleTap(View view, int position, ChatLoad c) {
        }

        @Override
        public Contact getContact(String qrCode) {
            return callback.getContact(qrCode);
        }

        @Override
        public ImageFetcher getImageFetcher() {
            return callback.getImageFetcher();
        }

        @Override
        public int getChatType(long chatId) {
            return callback.getChatType(chatId);
        }

        @Override
        public ChatLoad getChatLoad(long chatId) {
            return callback.getChatLoad(chatId);
        }
    };

    private double distance(double lat1, double lon1, double lat2, double lon2, char unit) {
        double theta = lon1 - lon2;
        double dist = Math.sin(deg2rad(lat1)) * Math.sin(deg2rad(lat2)) + Math.cos(deg2rad(lat1))
                * Math.cos(deg2rad(lat2)) * Math.cos(deg2rad(theta));
        dist = Math.acos(dist);
        dist = rad2deg(dist);
        dist = dist * 60 * 1.1515;
        if (unit == 'K') {
            dist = dist * 1.609344;
        } else if (unit == 'N') {
            dist = dist * 0.8684;
        }
        return (dist);
    }

    /* ::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::: */
    /* :: This function converts decimal degrees to radians : */
    /* ::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::: */
    private double deg2rad(double deg) {
        return (deg * Math.PI / 180.0);
    }

    /* ::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::: */
    /* :: This function converts radians to decimal degrees : */
    /* ::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::: */
    private double rad2deg(double rad) {
        return (rad * 180.0 / Math.PI);
    }

    public void setLocationFilter(boolean isLocationFilter) {
        this.isLocationFilter = isLocationFilter;
    }

    public void setFavoriteFilter(boolean isFavoriteFilter) {
        this.isFavoriteFilter = isFavoriteFilter;
    }


}
