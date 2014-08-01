package com.blulabellabs.code.ui.common;

import android.content.Context;
import android.database.DataSetObserver;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.LinearLayout;

import com.blulabellabs.code.R;
import com.blulabellabs.code.core.io.model.ChatLoad;
import com.blulabellabs.code.core.io.model.Contact;
import com.blulabellabs.code.ui.one2one.ChatGroupPhotosFragment;
import com.blulabellabs.code.ui.one2one.ChatGroupProfileFragment;
import com.blulabellabs.code.ui.one2one.ChatInsideFragment;
import com.blulabellabs.code.ui.one2one.ChatInsideGroupFragment;
import com.blulabellabs.code.ui.one2one.ChatPhotosFragment;
import com.blulabellabs.code.ui.one2one.ChatProfileFragment;

import java.util.ArrayList;
import java.util.List;

public class FullChatListBaseAdapter extends BaseAdapter {
    private final FragmentManager fm;
    List items = new ArrayList<Fragment>();

    public FullChatListBaseAdapter(FragmentManager supportFragmentManager, Contact c) {
        fm = supportFragmentManager;
        setContact(c);
    }

    public void setContact(Contact c) {
        clearPreviousFragments();
        items.add(ChatInsideFragment.newInstance(c));
        items.add(ChatProfileFragment.newInstance(c));
        items.add(ChatPhotosFragment.newInstance(c));

        FragmentTransaction fragmentTransaction = fm.beginTransaction();
        fragmentTransaction.add(R.id.pager_container2, (Fragment) items.get(0)).show((Fragment) items.get(0)).commit();
        fragmentTransaction = fm.beginTransaction();
        fragmentTransaction.add(R.id.pager_container2, (Fragment) items.get(1)).show((Fragment) items.get(1)).commit();
        fragmentTransaction = fm.beginTransaction();
        fragmentTransaction.add(R.id.pager_container2, (Fragment) items.get(2)).show((Fragment) items.get(2)).commit();
        fm.executePendingTransactions();
    }

    public FullChatListBaseAdapter(FragmentManager supportFragmentManager, ChatLoad c) {
        fm = supportFragmentManager;
        setChatLoad(c);
    }

    public void setChatLoad(ChatLoad c) {
        clearPreviousFragments();
        items.add(ChatInsideGroupFragment.newInstance(c));
        items.add(ChatGroupProfileFragment.newInstance(c));
        items.add(ChatGroupPhotosFragment.newInstance(c));

        FragmentTransaction fragmentTransaction = fm.beginTransaction();
        fragmentTransaction.add(R.id.pager_container, (Fragment) items.get(0)).show((Fragment) items.get(0)).commit();
        fragmentTransaction = fm.beginTransaction();
        fragmentTransaction.add(R.id.pager_container, (Fragment) items.get(1)).show((Fragment) items.get(1)).commit();
        fragmentTransaction = fm.beginTransaction();
        fragmentTransaction.add(R.id.pager_container, (Fragment) items.get(2)).show((Fragment) items.get(2)).commit();
        fm.executePendingTransactions();
    }


    public void clearPreviousFragments() {
        FragmentTransaction fragmentTransaction;
        for (Object item : items) {
            fragmentTransaction = fm.beginTransaction();
            fragmentTransaction.remove((Fragment) item).commit();
        }
        fm.executePendingTransactions();
        items.clear();
    }

    @Override
    public int getCount() {
        return 3;
    }

    @Override
    public Fragment getItem(int position) {
        return (Fragment) items.get(position);
    }

    @Override
    public long getItemId(int i) {
        return i;
    }

    @Override
    public View getView(int i, View view, ViewGroup viewGroup) {
        return ((android.support.v4.app.Fragment) items.get(i)).getView();
    }

    @Override
    public void unregisterDataSetObserver(DataSetObserver observer) {
        if (observer != null) {
            super.unregisterDataSetObserver(observer);
        }
    }
}
