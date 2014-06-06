package com.blulabellabs.code.ui;

import com.blulabellabs.code.R;
import com.blulabellabs.code.ui.one2one.GroupMemberFragment;

import android.os.Bundle;
import android.support.v4.app.FragmentActivity;

public class GroupMemberListActivity extends FragmentActivity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_memberlist);

		getSupportFragmentManager().beginTransaction()
				.replace(R.id.container, GroupMemberFragment.newInstance(null)).commit();

	}
}
