package com.blulabellabs.code.ui.one2one;

import android.app.Activity;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;

import com.blulabellabs.code.R;
import com.blulabellabs.code.core.data.preference.QodemePreferences;
import com.blulabellabs.code.core.io.RestAsyncHelper;
import com.blulabellabs.code.core.io.model.ChatLoad;
import com.blulabellabs.code.core.io.model.Message;
import com.blulabellabs.code.core.io.responses.ChatAddMemberResponse;
import com.blulabellabs.code.core.io.responses.SetFavoriteResponse;
import com.blulabellabs.code.core.io.utils.RestError;
import com.blulabellabs.code.core.io.utils.RestListener;
import com.blulabellabs.code.core.provider.QodemeContract;
import com.blulabellabs.code.core.sync.SyncHelper;
import com.blulabellabs.code.images.utils.ImageCache;
import com.blulabellabs.code.images.utils.ImageFetcher;
import com.blulabellabs.code.ui.MainActivity;
import com.blulabellabs.code.ui.common.GridAdapter;
import com.blulabellabs.code.ui.common.SeparatedListAdapter;
import com.blulabellabs.code.ui.one2one.ChatInsideFragment.One2OneChatInsideFragmentCallback;
import com.blulabellabs.code.utils.Converter;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class ChatGroupPhotosFragment extends Fragment {
    private static final String CHAT_ID = "chat_id";
    private static final String CHAT_COLOR = "chat_color";
    private static final String CHAT_NAME = "chat_name";
    private static final String QRCODE = "contact_qr";
    private static final String CHAT_STATUS = "chat_status";
    private static final String LOCATION = "location";
    private static final String DATE = "date";
    private One2OneChatInsideFragmentCallback callback;
    private boolean isViewCreated;
    SeparatedListAdapter mListAdapter;
    ListView mListViewPhotos;
    private ChatLoad chatLoad;
    private ImageButton mBtnImageSend, mImgFavorite;
    private TextView mName, mStatus, mTextViewNumFavorite;

    private static final SimpleDateFormat SIMPLE_DATE_FORMAT_MAIN = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS", Locale.US);
    private static final SimpleDateFormat SIMPLE_DATE_FORMAT_HEADER = new SimpleDateFormat("MMM dd yyyy", Locale.US);
    private static final String TAG = "ImageGridFragment";
    private static final String IMAGE_CACHE_DIR = "thumbs";
    private ImageFetcher mImageFetcher;

    public static ChatGroupPhotosFragment newInstance(ChatLoad c) {
        ChatGroupPhotosFragment f = new ChatGroupPhotosFragment();
        Bundle args = new Bundle();
        args.putLong(CHAT_ID, c.chatId);
        args.putInt(CHAT_COLOR, c.color);
        args.putString(CHAT_NAME, c.title);
        args.putString(QRCODE, c.qrcode);
        args.putString(CHAT_STATUS, c.status);
        f.setChatLoad(c);
        f.setArguments(args);
        return f;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        final DisplayMetrics displayMetrics = new DisplayMetrics();
        getActivity().getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        final int height = displayMetrics.heightPixels;
        final int width = displayMetrics.widthPixels;
        final int longest = (height > width ? height : width) / 2;
        ImageCache.ImageCacheParams cacheParams = new ImageCache.ImageCacheParams(getActivity(),
                IMAGE_CACHE_DIR);
        cacheParams.setMemCacheSizePercent(0.25f);
        mImageFetcher = new ImageFetcher(getActivity(), longest);
        mImageFetcher.setLoadingImage(R.drawable.empty_photo);
        mImageFetcher.addImageCache(getActivity().getSupportFragmentManager(), cacheParams);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_chat_photos, null);
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        callback = (One2OneChatInsideFragmentCallback) activity;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        mName = (TextView) getView().findViewById(R.id.name);
        mStatus = (TextView) getView().findViewById(R.id.textView_status);
        mBtnImageSend = (ImageButton) getView().findViewById(R.id.btn_camera);
        mBtnImageSend.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                MainActivity activity = (MainActivity) getActivity();
                activity.setCurrentChatId(getArguments().getLong(CHAT_ID));
                activity.takePhoto();
            }
        });
        mListViewPhotos = (ListView) getView().findViewById(R.id.listview);
        mImgFavorite = (ImageButton) getView().findViewById(R.id.btnFavorite);
        mImgFavorite.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                int is_favorite = 1;
                MainActivity activity = (MainActivity) getActivity();
                ChatLoad chatLoad = activity.getChatLoad(getArguments().getLong(CHAT_ID));
                if (chatLoad != null) {
                    int num_of_favorite = chatLoad.number_of_likes;
                    if (chatLoad.is_favorite == 1) {
                        is_favorite = 2;
                        num_of_favorite--;
                    } else {
                        is_favorite = 1;
                        if (num_of_favorite <= 0) {
                            num_of_favorite = 1;
                        } else
                            num_of_favorite++;
                    }
                    if (chatLoad.isSearchResult) {
                        chatLoad.number_of_likes = num_of_favorite;
                        chatLoad.is_favorite = is_favorite;
                        mImgFavorite.setImageResource(is_favorite == 1 ? R.drawable.ic_chat_favorite : R.drawable.ic_chat_favorite_h);
                        String date = Converter.getCurrentGtmTimestampString();
                        RestAsyncHelper.getInstance().setFavorite(date, is_favorite,
                                chatLoad.chatId, new RestListener<SetFavoriteResponse>() {
                                    @Override
                                    public void onResponse(SetFavoriteResponse response) {
                                    }

                                    @Override
                                    public void onServiceError(RestError error) {
                                    }
                                }
                        );
                        RestAsyncHelper.getInstance().chatAddMember(chatLoad.chatId,
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
                    } else {
                        getActivity().getContentResolver().update(QodemeContract.Chats.CONTENT_URI,
                                QodemeContract.Chats.updateFavorite(is_favorite, num_of_favorite),
                                QodemeContract.Chats.CHAT_ID + " = " + chatLoad.chatId, null);
                        SyncHelper.requestManualSync();
                    }
                }
            }
        });
        mTextViewNumFavorite = (TextView) getView().findViewById(R.id.textView_totalFavorite);
        isViewCreated = true;
        updateUi();
    }

    public long getChatId() {
        return getArguments().getLong(CHAT_ID, 0L);
    }

    class PhotoMessage {
        String date;
        List<Message> arrayList = new ArrayList<Message>();
    }

    public void updateUi() {
        if (isViewCreated && callback != null) {
            mImgFavorite.setImageResource(getChatLoad().is_favorite == 1 ? R.drawable.ic_chat_favorite : R.drawable.ic_chat_favorite_h);
            if (getChatLoad().type == 2) {
                mTextViewNumFavorite.setText(getChatLoad().number_of_likes + "");
                mTextViewNumFavorite.setVisibility(View.VISIBLE);
            }
            if (getChatLoad() != null && getChatLoad().is_locked == 1 && !QodemePreferences.getInstance().getQrcode().equals(getChatLoad().user_qrcode)) {
                mBtnImageSend.setVisibility(View.GONE);
            } else {
                mBtnImageSend.setVisibility(View.VISIBLE);
            }
            mListAdapter = new SeparatedListAdapter((MainActivity) callback);
            List<Message> messages = callback.getChatMessages(getChatId());

            mName.setText(getChatLoad() != null ? getChatLoad().title : getArguments().getString(
                    CHAT_NAME));
            mStatus.setText(getChatLoad() != null ? getChatLoad().status : getArguments()
                    .getString(CHAT_STATUS));

            if (messages != null) {
                Message previousMessage = null;
                List<Message> temp = new ArrayList<Message>();
                for (Message me : messages) {
                    if (me.hasPhoto == 1) {
                        temp.add(me);
                    }
                }
                Log.d("photo", temp.size() + "");
                List<PhotoMessage> arrayListMessage = new ArrayList<ChatGroupPhotosFragment.PhotoMessage>();
                for (Message me : temp) {
                    if (previousMessage != null) {
                        try {
                            Calendar currentDate = Calendar.getInstance();
                            String date = me.created;
                            if (me.created != null && !(me.created.contains(".")))
                                date += ".000";
                            currentDate.setTime(SIMPLE_DATE_FORMAT_MAIN.parse(date));
                            Calendar previousDate = Calendar.getInstance();
                            String preDate = previousMessage.created;
                            if (previousMessage.created != null
                                    && (!previousMessage.created.contains(".")))
                                preDate = preDate + ".000";
                            previousDate.setTime(SIMPLE_DATE_FORMAT_MAIN.parse(preDate));
                            if (currentDate.get(Calendar.DATE) != previousDate.get(Calendar.DATE)) {
                                Date dateTemp = new Date(Converter.getCrurentTimeFromTimestamp(date));
                                PhotoMessage photoMessage = new PhotoMessage();
                                photoMessage.date = SIMPLE_DATE_FORMAT_HEADER.format(dateTemp);
                                photoMessage.arrayList.add(me);
                                arrayListMessage.add(photoMessage);
                            } else {
                                if (arrayListMessage.size() > 0) {
                                    PhotoMessage message = arrayListMessage.get(arrayListMessage
                                            .size() - 1);
                                    message.arrayList.add(me);
                                }
                            }
                        } catch (ParseException e) {
                            e.printStackTrace();
                        }
                    } else {
                        PhotoMessage photoMessage = new PhotoMessage();
                        String date = me.created;
                        if (me.created != null && !(me.created.contains(".")))
                            date += ".000";
                        Date dateTemp = new Date(Converter.getCrurentTimeFromTimestamp(date));
                        photoMessage.date = SIMPLE_DATE_FORMAT_HEADER.format(dateTemp);
                        photoMessage.arrayList.add(me);
                        arrayListMessage.add(photoMessage);
                    }
                    previousMessage = me;
                }

                Log.d("photo", arrayListMessage.size() + "");
                for (PhotoMessage photoMessage : arrayListMessage) {
                    ArrayList<HashMap<String, Message>> mMapList = new ArrayList<HashMap<String, Message>>();
                    int count = photoMessage.arrayList.size() % 2 == 0 ? photoMessage.arrayList.size() / 2 : (photoMessage.arrayList.size() / 2) + 1;
                    Log.d("photo size", photoMessage.arrayList.size() + "");
                    int j = 0;
                    for (int i = 0; i < count; i++) {
                        Map<String, Message> mHashMapLeft = new HashMap<String, Message>();
                        mHashMapLeft.put("0", photoMessage.arrayList.get(j));
                        try {
                            mHashMapLeft.put("1", photoMessage.arrayList.get(j + 1));
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                        mMapList.add((HashMap<String, Message>) mHashMapLeft);
                        j = j + 2;
                    }
                    mListAdapter.addSection(photoMessage.date, new GridAdapter(getActivity(),
                            mMapList, mImageFetcher));
                }

            }
            mListViewPhotos.setAdapter(mListAdapter);
        }
        if (getChatLoad().is_deleted == 1) {
            mBtnImageSend.setVisibility(View.INVISIBLE);
            mImgFavorite.setClickable(false);

        }
    }

    public void setChatLoad(ChatLoad chatLoad) {
        this.chatLoad = chatLoad;
    }

    public ChatLoad getChatLoad() {
        return chatLoad;
    }
}
