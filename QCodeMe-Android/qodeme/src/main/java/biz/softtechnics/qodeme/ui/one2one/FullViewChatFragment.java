package biz.softtechnics.qodeme.ui.one2one;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import biz.softtechnics.qodeme.R;
import biz.softtechnics.qodeme.core.io.model.Contact;
import biz.softtechnics.qodeme.ui.common.FullChatListAdapter;

public class FullViewChatFragment extends Fragment {
	private static final String CHAT_ID = "chat_id";
	private static final String CHAT_COLOR = "chat_color";
	private static final String CHAT_NAME = "chat_name";
	private static final String QRCODE = "contact_qr";
	private static final String LOCATION = "location";
	private static final String DATE = "date";
	private static final String FIRST_UPDATE = "first_update";

	private FullChatListAdapter mPagerAdapter;
	private ViewPager mViewPager;

	private Contact mContact;
	private boolean firstUpdate;

	public static FullViewChatFragment newInstance(Contact c, boolean firstUpdate) {
		FullViewChatFragment f = new FullViewChatFragment();
		Bundle args = new Bundle();
		args.putLong(CHAT_ID, c.chatId);
		args.putInt(CHAT_COLOR, c.color);
		args.putString(CHAT_NAME, c.title);
		args.putString(QRCODE, c.qrCode);
		args.putString(LOCATION, c.location);
		args.putString(DATE, c.date);
		args.putBoolean(FIRST_UPDATE, firstUpdate);
		f.setArguments(args);
		f.mContact = c;
		f.firstUpdate = firstUpdate;
		// f.mPagerAdapter = new
		// FullChatListAdapter(f.getChildFragmentManager(), c, firstUpdate);
		return f;
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View view =  inflater.inflate(R.layout.fragment_full_chatview, null);
		mViewPager = (ViewPager) view.findViewById(R.id.pager);
		// if (mPagerAdapter != null)
		// mPagerAdapter = null;
		mPagerAdapter = new FullChatListAdapter(getChildFragmentManager(), mContact, firstUpdate);
		mViewPager.setAdapter(mPagerAdapter);
		return view;
	}

	public long getChatId() {
		return getArguments().getLong(CHAT_ID, 0L);
	}

	public void updateUi() {
		if (mPagerAdapter != null) {
			ChatInsideFragment chatInsideFragment = (ChatInsideFragment) mPagerAdapter.getItem(0);
			chatInsideFragment.updateUi();
		}
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);

//		mViewPager = (ViewPager) getView().findViewById(R.id.pager);
//		// if (mPagerAdapter != null)
//		// mPagerAdapter = null;
//		mPagerAdapter = new FullChatListAdapter(getChildFragmentManager(), mContact, firstUpdate);
//		mViewPager.setAdapter(mPagerAdapter);
	}

}
