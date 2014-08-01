package com.blulabellabs.code.ui.one2one;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.blulabellabs.code.R;


public class ChatListGroupFragment extends Fragment {

    public static ChatListGroupFragment newInstance() {
        return new ChatListGroupFragment();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        return inflater.inflate(R.layout.stub_fragment, null);
    }
}
