package com.blulabellabs.code.ui.common;

import java.util.ArrayList;

import com.blulabellabs.code.core.io.model.ChatLoad;
import com.blulabellabs.code.core.io.model.Contact;
import com.blulabellabs.code.ui.one2one.ChatGroupPhotosFragment;
import com.blulabellabs.code.ui.one2one.ChatGroupProfileFragment;
import com.blulabellabs.code.ui.one2one.ChatInsideFragment;
import com.blulabellabs.code.ui.one2one.ChatInsideGroupFragment;
import com.blulabellabs.code.ui.one2one.ChatListFragment;
import com.blulabellabs.code.ui.one2one.ChatListGroupFragment;
import com.blulabellabs.code.ui.one2one.ChatListGroupPublicFragment;
import com.blulabellabs.code.ui.one2one.ChatPhotosFragment;
import com.blulabellabs.code.ui.one2one.ChatProfileFragment;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;

public class MainChatListAdapter extends FragmentStatePagerAdapter {
	private ArrayList<Fragment> mFragmentsList = new ArrayList<Fragment>();

	public MainChatListAdapter(FragmentManager fm) {
		super(fm);

		// for (int i = 0; i < 3; i++)
		getFragmentsList().add(ChatListFragment.getInstance());
		getFragmentsList().add(ChatListGroupFragment.getInstance());
		getFragmentsList().add(ChatListGroupPublicFragment.getInstance());

	}

	public MainChatListAdapter(FragmentManager fm, ChatLoad c, boolean firstUpdate) {
		super(fm);
	}

	@Override
	public int getCount() {
		return getFragmentsList().size();
	}

	@Override
	public Fragment getItem(int position) {
		return getFragmentsList().get(position);
	}

	public void setFragmentsList(ArrayList<Fragment> mFragmentsList) {
		this.mFragmentsList = mFragmentsList;
	}

	public ArrayList<Fragment> getFragmentsList() {
		return mFragmentsList;
	}

}
