package com.blulabellabs.code.ui.tutorial;

import com.blulabellabs.code.R;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;



public class TutorialFragmentAdapter extends FragmentPagerAdapter {
	
	private static final int COUNT = 7;

	public TutorialFragmentAdapter(FragmentManager fm) {
		super(fm);
	}

	@Override
	public Fragment getItem(int position) {
		return TutorialFragment.newInstance(getLayoutId(position));
	}
	
	@Override
	public int getCount() {
		return COUNT;
	}

	private static int getLayoutId(int position) {
		switch (position) {
		case 0: return R.layout.fragment_tutorial_1;
		case 1: return R.layout.fragment_tutorial_2;
		case 2: return R.layout.fragment_tutorial_3;
		case 3: return R.layout.fragment_tutorial_4;
		case 4: return R.layout.fragment_tutorial_5;
		case 5: return R.layout.fragment_tutorial_6;
		case 6: return R.layout.fragment_tutorial_7;
		default: return 0;
		}
	}

}
