package com.blulabellabs.code.ui.one2one;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.location.Location;
import android.os.Bundle;
import android.support.v4.app.ListFragment;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.AbsListView;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.TextView.OnEditorActionListener;

import com.blulabellabs.code.R;
import com.blulabellabs.code.core.data.preference.QodemePreferences;
import com.blulabellabs.code.core.io.RestAsyncHelper;
import com.blulabellabs.code.core.io.model.ChatLoad;
import com.blulabellabs.code.core.io.model.Contact;
import com.blulabellabs.code.core.io.model.Message;
import com.blulabellabs.code.core.io.responses.ClearSearchResponse;
import com.blulabellabs.code.core.io.utils.RestError;
import com.blulabellabs.code.core.io.utils.RestListener;
import com.blulabellabs.code.images.utils.ImageFetcher;
import com.blulabellabs.code.ui.MainActivity;
import com.blulabellabs.code.ui.MainActivity.LoadMoreChatListener;
import com.blulabellabs.code.ui.common.ExGroupListAdapter;
import com.blulabellabs.code.ui.one2one.ChatListFragment.One2OneChatListFragmentCallback;
import com.blulabellabs.code.utils.Helper;
import com.google.common.collect.Lists;
import com.handmark.pulltorefresh.library.PullToRefreshBase;
import com.handmark.pulltorefresh.library.PullToRefreshListView;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class ChatListGroupPublicFragment extends ListFragment {

    private static final int CHAT_TYPE = 2;
    private One2OneChatListFragmentCallback callback;
    public ExGroupListAdapter mListAdapter;
    private ImageButton mImgBtnClear;
    private ImageButton mImgBtnLocationFilter;
    private ImageButton mImgBtnFavoriteFilter;
    private EditText mEditTextSearch;
    private ImageView mImgViewSearchHint;
    private LinearLayout mLinearLayoutSearch;
    private LinearLayout mFooterLayout;
    private PullToRefreshListView mListView;
    PullToRefreshBase<?> mRefreshedView;


    private boolean isThreadRunning;
    private String searchString = "";
    private boolean isLocationFilter = false;
    private boolean isFavoriteFilter = false;
    private boolean isViewCreated = false;
    List<ChatLoad> chatLoads = Lists.newArrayList();
    ChatLoad chatLoad;
    boolean isMoreData = true;
    int pageNo = 1;

    public ChatListGroupPublicFragment() {
        super();
    }

    public static ChatListGroupPublicFragment newInstance() {
        ChatListGroupPublicFragment chatListGroupPublicFragment = new ChatListGroupPublicFragment();
        Bundle args = new Bundle();
        args.putInt("index", 1);
        chatListGroupPublicFragment.setArguments(args);
        return chatListGroupPublicFragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        return inflater.inflate(R.layout.fragment_one2one_chat_list_public, null);
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        callback = (One2OneChatListFragmentCallback) activity;
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        ImageButton mImgBtnSearch = (ImageButton) getView().findViewById(R.id.imgBtn_search);
        mImgBtnClear = (ImageButton) getView().findViewById(R.id.imgBtn_clear);
        mImgBtnLocationFilter = (ImageButton) getView().findViewById(R.id.imgBtn_locationFilter);
        mImgBtnFavoriteFilter = (ImageButton) getView().findViewById(R.id.imgBtn_favoriteFilter);

        mEditTextSearch = (EditText) getView().findViewById(R.id.editText_Search);
        mImgViewSearchHint = (ImageView) getView().findViewById(R.id.searchHintIcon);

        initListView();

        isViewCreated = true;
        updateUi();
        mImgBtnClear.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                MainActivity activity = (MainActivity) callback;
                activity.setPublicSearch(false);
                activity.setPublicSearchString("");
                activity.clearPublicSearch();
                mImgBtnClear.setVisibility(View.GONE);
                mEditTextSearch.getText().clear();
                searchString = "";
                pageNo = 1;
                isMoreData = true;
                Bitmap bm = BitmapFactory.decodeResource(getResources(),
                        R.drawable.ic_location_gray);
                mImgBtnLocationFilter.setImageBitmap(bm);
                isLocationFilter = false;

                isFavoriteFilter = false;
                mImgBtnFavoriteFilter.setImageResource(R.drawable.ic_chat_favorite_h);
                updateUi();

                RestAsyncHelper.getInstance().clearSearchChats(2,
                        new RestListener<ClearSearchResponse>() {

                            @Override
                            public void onResponse(ClearSearchResponse response) {
                                Log.d("clearSearch", "Ok");
                            }

                            @Override
                            public void onServiceError(RestError error) {
                                Log.d("clearSearch", "Error " + error.getMessage());
                            }

                        }
                );
            }
        });

        mImgBtnFavoriteFilter.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                if (isFavoriteFilter) {
                    isFavoriteFilter = false;
                    updateUi();
                    mImgBtnFavoriteFilter.setImageResource(R.drawable.ic_chat_favorite_h);
                } else {
                    mImgBtnFavoriteFilter.setImageResource(R.drawable.ic_star_blue);
                    isFavoriteFilter = true;
                    if (chatLoads != null) {
                        mListAdapter.clearViews();
                        mListAdapter.addAll(filterMessages());
                    }
                }
            }
        });

        mImgBtnLocationFilter.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                if (isLocationFilter) {
                    isLocationFilter = false;
                    updateUi();
                    mImgBtnLocationFilter.setImageResource(R.drawable.ic_location_gray);
                } else {
                    mImgBtnLocationFilter.setImageResource(R.drawable.ic_location_blue_big);
                    isLocationFilter = true;
                    if (chatLoads != null) {
                        mListAdapter.clearViews();
                        mListAdapter.addAll(filterMessages());
                    }
                }
            }
        });

        MainActivity activity = (MainActivity) callback;
        if (activity.isPublicSearch()) {
            searchString = activity.getPublicSearchString();
            mEditTextSearch.setText(searchString);
            if (activity.getPublicSearchString().trim().length() > 0) {
                mImgBtnClear.setVisibility(View.VISIBLE);
            }
        } else {
            mImgBtnClear.setVisibility(View.GONE);
        }
        mImgBtnSearch.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                String data = mEditTextSearch.getText().toString();
                searchString = data;
                MainActivity activity = (MainActivity) callback;
                activity.setPublicSearch(true);
                activity.setPublicSearchString(data);
                pageNo = 1;
                activity.clearPublicSearch();
                activity.searchChats(data, 2, pageNo, chatListener);
                isMoreData = true;
                isThreadRunning = true;
                mImgBtnClear.setVisibility(View.VISIBLE);
            }
        });

//        mEditTextSearch.setInputType(InputType.TYPE_TEXT_FLAG_NO_SUGGESTIONS);
        mEditTextSearch.setImeOptions(EditorInfo.IME_ACTION_SEARCH);
        mEditTextSearch.setOnEditorActionListener(new OnEditorActionListener() {

            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_SEARCH
                        || actionId == EditorInfo.IME_ACTION_GO
                        || actionId == EditorInfo.IME_ACTION_DONE
                        || actionId == EditorInfo.IME_ACTION_UNSPECIFIED) {

                    String data = v.getText().toString();
                    searchString = data;
                    MainActivity activity = (MainActivity) callback;
                    activity.setPublicSearch(true);
                    activity.setPublicSearchString(data);
                    pageNo = 1;
                    activity.clearPublicSearch();
                    activity.searchChats(data, 2, pageNo, chatListener);
                    isMoreData = true;
                    isThreadRunning = true;
                    mImgBtnClear.setVisibility(View.VISIBLE);
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

    private List<ChatLoad> filterMessages() {
        List<ChatLoad> temp = Lists.newArrayList();
        List<ChatLoad> temp1 = Lists.newArrayList();
        if (isFavoriteFilter) {
            for (ChatLoad c : chatLoads) {
                try {
                    if (c.is_favorite == 1) {
                        temp.add(c);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        } else {
            temp.addAll(chatLoads);
        }

        if (isLocationFilter) {
            for (ChatLoad c : temp) {
                try {
                    if (c.latitude != null && c.longitude != null && !c.latitude.equals("")
                            && !c.longitude.equals("") && !c.latitude.equals("0")
                            && !c.latitude.equals("0.0") && !c.latitude.equals("-1")
                            && !c.longitude.equals("0") && !c.longitude.equals("0.0")
                            && !c.longitude.equals("-1")) {
                        temp1.add(c);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            Collections.sort(temp1, new CustomComparator());
        } else {
            temp1.addAll(temp);
        }
        return temp1;
    }

    public class CustomComparator implements Comparator<ChatLoad> {

        @Override
        public int compare(ChatLoad lhs, ChatLoad rhs) {
            MainActivity activity = (MainActivity) callback;
            Location location = activity.getCurrentLocation();
            Integer leftDistance = 0;
            Integer rightDistance = 0;
            if (location != null) {
                try {
                    double lat = Double.parseDouble(lhs.latitude);
                    double lng = Double.parseDouble(lhs.longitude);
                    leftDistance = (int) distance(lat, lng, location.getLatitude(),
                            location.getLongitude(), 'M');

                } catch (Exception e) {
                    e.printStackTrace();
                }
                try {
                    double lat = Double.parseDouble(rhs.latitude);
                    double lng = Double.parseDouble(rhs.longitude);
                    rightDistance = (int) distance(lat, lng, location.getLatitude(),
                            location.getLongitude(), 'M');
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            return leftDistance.compareTo(rightDistance);
        }

    }

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

    private double deg2rad(double deg) {
        return (deg * Math.PI / 180.0);
    }

    private double rad2deg(double rad) {
        return (rad * 180.0 / Math.PI);
    }

    private void initListView() {
        List<ChatLoad> listForAdapter = Lists.newArrayList();
        mFooterLayout = (LinearLayout) getView().findViewById(R.id.list_footer);
        mListView = (PullToRefreshListView) getView().findViewById(R.id.listview);
        mListAdapter = new ExGroupListAdapter(getActivity(), R.layout.group_public_chat_list_item, listForAdapter, chatListCallback);
        mLinearLayoutSearch = (LinearLayout) getView().findViewById(R.id.linearLayout_search);
        mListView.mLinearSearchLayout = mLinearLayoutSearch;

        mListView.setAdapter(mListAdapter);
        if (!isThreadRunning && isMoreData) {
            mFooterLayout.setVisibility(View.VISIBLE);
            isThreadRunning = true;
            MainActivity activity = (MainActivity) callback;
            activity.setPublicSearch(true);
            activity.searchChats(searchString, 2, pageNo, chatListener);
        }

        mListView.setOnRefreshListener(new PullToRefreshBase.OnRefreshListener<ListView>() {

            @Override
            public void onRefresh(PullToRefreshBase<ListView> refreshView) {
                mRefreshedView = refreshView;
                String data = mEditTextSearch.getText().toString();

                searchString = data;
                MainActivity activity = (MainActivity) callback;
                activity.setPublicSearch(true);
                activity.setPublicSearchString(data);
                pageNo = 1;
                activity.clearPublicSearch();
                activity.searchChats(data, 2, pageNo, chatListener);
                mEditTextSearch.setEnabled(false);
                isMoreData = true;

                isThreadRunning = true;
                if (mEditTextSearch.getText().toString().trim().length() > 0) {
                    mImgBtnClear.setVisibility(View.VISIBLE);
                } else {
                    mImgBtnClear.setVisibility(View.GONE);
                }
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
                int lastInScreen = firstVisibleItem + visibleItemCount;
                if ((lastInScreen == totalItemCount)) {
                    if (!isThreadRunning && isMoreData) {
                        mFooterLayout.setVisibility(View.VISIBLE);
                        isThreadRunning = true;
                        MainActivity activity = (MainActivity) callback;
                        activity.setPublicSearch(true);
                        activity.searchChats(searchString, 2, pageNo, chatListener);
                    }
                }
            }
        });
    }

    private void sortByMember() {
        Collections.sort(chatLoads, new Comparator<ChatLoad>() {

            @Override
            public int compare(ChatLoad lhs, ChatLoad rhs) {
                Integer left = 0;
                Integer right = 0;
                if (lhs.members != null)
                    left = lhs.members.length;
                if (rhs.members != null)
                    right = rhs.members.length;
                return right.compareTo(left);
            }
        });
    }

    public void updateUi() {
        try {
            if (isViewCreated && callback != null && callback.getChatList(CHAT_TYPE) != null) {
                mImgBtnFavoriteFilter.setImageResource(!isFavoriteFilter ? R.drawable.ic_chat_favorite_h : R.drawable.ic_star_blue);
                mImgBtnLocationFilter.setImageResource(isLocationFilter ? R.drawable.ic_location_blue_big : R.drawable.ic_location_gray);
                chatLoads = callback.getChatList(CHAT_TYPE);
                if (chatLoads != null && QodemePreferences.getInstance().getNewPublicGroupChatId() != -1) {
                    try {
                        sortByMember();
                        ChatLoad newChatLoad = null;
                        for (ChatLoad c : chatLoads) {
                            if (QodemePreferences.getInstance().getNewPublicGroupChatId() == c.chatId) {
                                newChatLoad = c;
                                break;
                            }
                        }
                        chatLoad = newChatLoad;
                        if (newChatLoad != null) {
                            chatLoads.remove(newChatLoad);
                            chatLoads.add(0, newChatLoad);
                            mListAdapter.notifyDataSetChanged();
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }

                    mListAdapter.clearViews();
                    mListAdapter.addAll(!isLocationFilter && !isFavoriteFilter ? chatLoads : filterMessages());
                }
                MainActivity activity = (MainActivity) callback;
                ChatLoad chatLoad = activity.newChatCreated.get(2);

                mListAdapter.clearViews();
                if (!isLocationFilter && !isFavoriteFilter) {
                    sortByMember();
                    if (chatLoad != null) {
                        chatLoads.add(0, chatLoad);
                    }
                    mListAdapter.addAll(chatLoads);
                } else {
                    if (chatLoad != null) {
                        chatLoads.add(0, chatLoad);
                    }
                    mListAdapter.addAll(filterMessages());
                }

                for (int i = 0; i < mListAdapter.getCount(); i++) {
                    mListAdapter.getView(i, null, null);
                }
                mListAdapter.notifyDataSetChanged();
            }
        } catch (Exception e) {
            e.printStackTrace();
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

//        @Override
//        public int getNewMessagesCount(long chatId) {
//            return callback.getNewMessagesCount(chatId);
//        }

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
            return callback.getChatMessages(chatId);
        }

        @Override
        public void sendMessage(long c, String message, String photoUrl, int hashPhoto,
                                long replyTo_Id, double latitude, double longitude, String senderName,
                                String localUrl) {
            callback.sendMessage(c, message, photoUrl, hashPhoto, replyTo_Id, latitude,
                    longitude, senderName, localUrl);
        }

        @Override
        public void onDoubleTap(View view, int position, ChatLoad c) {
            Helper.hideKeyboard(view.getContext(), mEditTextSearch);
            callback.showChat(c, true, view);
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

    public void setLocationFilter(boolean isLocationFilter) {
        this.isLocationFilter = isLocationFilter;
    }

    public void setFavoriteFilter(boolean isFavoriteFilter) {
        this.isFavoriteFilter = isFavoriteFilter;
    }

    public LoadMoreChatListener chatListener = new LoadMoreChatListener() {

        @Override
        public void onSearchResult(int count, int responseCode) {
            if (mRefreshedView != null)
                mRefreshedView.onRefreshComplete();
            mFooterLayout.setVisibility(View.GONE);
            isThreadRunning = false;
            if (count > 0) {
                pageNo++;
            } else
                isMoreData = false;
        }
    };
}
