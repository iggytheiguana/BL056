package com.blulabellabs.code.ui.one2one;

import com.blulabellabs.code.R;
import com.blulabellabs.code.core.io.model.ChatLoad;
import com.google.android.gms.internal.ad;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;

public class GroupMemberFragment extends Fragment {
	private static final String CHAT_ID = "chat_id";

	ListView mListView;

	public static GroupMemberFragment newInstance(ChatLoad c) {
		GroupMemberFragment fragment = new GroupMemberFragment();
		Bundle args = new Bundle();
		//args.putLong(CHAT_ID, c.chatId);
		fragment.setArguments(args);

		return fragment;
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		return inflater.inflate(R.layout.fragment_group_members, null);
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		mListView = (ListView) getView().findViewById(R.id.listview_groupMember);
		
		MemberListAdapter adapter = new MemberListAdapter(getActivity());
		adapter.add("");
		adapter.add("");
		adapter.add("");
		mListView.setAdapter(adapter);
		
	}

	class MemberListAdapter extends ArrayAdapter<String> {

		public MemberListAdapter(Context context) {
			super(context, 0);
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			return getLayoutInflater(getArguments()).inflate(R.layout.list_item_group_member, null);
			
		}

	}
}
