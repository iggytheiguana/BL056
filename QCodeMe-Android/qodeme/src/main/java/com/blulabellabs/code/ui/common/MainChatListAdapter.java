package com.blulabellabs.code.ui.common;

import android.database.DataSetObserver;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;

import com.blulabellabs.code.ui.one2one.ChatListFragment;
import com.blulabellabs.code.ui.one2one.ChatListGroupFragment;
import com.blulabellabs.code.ui.one2one.ChatListGroupPublicFragment;

import java.util.ArrayList;
import java.util.List;

public class MainChatListAdapter extends FragmentStatePagerAdapter {
    List<Fragment> items = new ArrayList<Fragment>();

    public MainChatListAdapter(FragmentManager fm) {
        super(fm);
        items.add(ChatListGroupFragment.newInstance());

        Fragment f2 = null;
        if (fm != null && fm.getFragments() != null) {
            for (Fragment f : fm.getFragments()) {
                if (f instanceof ChatListGroupPublicFragment) {
                    f2 = f;
                    break;
                }
            }
        }
        if (f2 != null) {
            items.add(f2);
        } else {
            items.add(ChatListGroupPublicFragment.newInstance());
        }

        Fragment f3 = null;
        if (fm != null && fm.getFragments() != null) {
            for (Fragment f : fm.getFragments()) {
                if (f instanceof ChatListFragment) {
                    f3 = f;
                    break;
                }
            }
        }
        if (f3 != null) {
            items.add(f3);
        } else {
            items.add(ChatListFragment.newInstance());
        }
    }

    @Override
    public int getCount() {
        return items.size();
    }

    @Override
    public Fragment getItem(int position) {
        return items.get(position);
    }

    @Override
    public void unregisterDataSetObserver(DataSetObserver observer) {
        if (observer != null) {
            super.unregisterDataSetObserver(observer);
        }
    }
}
