package com.blulabellabs.code.ui;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

import android.content.Context;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

import com.blulabellabs.code.R;
import com.blulabellabs.code.core.io.model.Contact;
import com.blulabellabs.code.core.provider.QodemeContract;
import com.blulabellabs.code.core.provider.QodemeContract.Contacts.Sync;
import com.blulabellabs.code.core.sync.SyncHelper;
import com.blulabellabs.code.utils.Converter;
import com.blulabellabs.code.utils.DbUtils;

public class GroupMemberListActivity extends ActionBarActivity {
	private static final String MEMBER_LIST = "MemberList";
	private static final String CHAT_ID = "chat_id";
	ListView mListView;

	private long chat_id;
	private ArrayList<Contact> arrayList;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		getSupportActionBar().hide();

		setContentView(R.layout.fragment_group_members);

		mListView = (ListView) findViewById(R.id.listview_groupMember);

		Bundle bundle = getIntent().getExtras();
		if (bundle != null) {
			chat_id = bundle.getLong(CHAT_ID);
			arrayList = bundle.getParcelableArrayList(MEMBER_LIST);
		}

		MemberListAdapter adapter = new MemberListAdapter(this);
		if (arrayList != null) {
			for (Contact c : arrayList)
				adapter.add(c);
		}
		mListView.setAdapter(adapter);
		// getSupportFragmentManager().beginTransaction()
		// .replace(R.id.container,
		// GroupMemberFragment.newInstance(null)).commit();

	}

	class MemberListAdapter extends ArrayAdapter<Contact> {

		public MemberListAdapter(Context context) {
			super(context, 0);
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			ViewHolder holder;
			if (convertView == null) {
				holder = new ViewHolder();
				convertView = getLayoutInflater().inflate(R.layout.list_item_group_member, null);

				holder.textViewName = (TextView) convertView.findViewById(R.id.textView_name);
				holder.textViewLocation = (TextView) convertView
						.findViewById(R.id.textView_location);
				holder.mBtnBlock = (Button) convertView.findViewById(R.id.btnBlock);
				holder.mBtnUnBlock = (Button) convertView.findViewById(R.id.btnUnBlock);

				holder.mBtnBlock.setTag(position);
				holder.mBtnUnBlock.setTag(position);

				convertView.setTag(holder);
			} else
				holder = (ViewHolder) convertView.getTag();

			Contact c = getItem(position);
			holder.textViewName.setText(c.title);
			holder.textViewName.setTextColor(c.color);
			// holder.textViewLocation.setText(c.location);
			SimpleDateFormat fmtOut = new SimpleDateFormat("MM/dd/yy h:mm a", Locale.US);
			String dateStr = fmtOut.format(new Date(Converter.getCrurentTimeFromTimestamp(c.date)));
			holder.textViewLocation.setText(dateStr + ", " + c.location);
			if (c.state == QodemeContract.Contacts.State.BLOCKED_BY) {
				holder.mBtnBlock.setBackgroundResource(R.drawable.bg_block_h);
				holder.mBtnUnBlock.setBackgroundResource(R.drawable.bg_block);
			} else {
				holder.mBtnBlock.setBackgroundResource(R.drawable.bg_block);
				holder.mBtnUnBlock.setBackgroundResource(R.drawable.bg_block_h);
			}

			holder.mBtnBlock.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View v) {
					int tag = (Integer) v.getTag();
					if (getItem(tag).state != QodemeContract.Contacts.State.BLOCKED_BY) {
						getItem(tag).state = QodemeContract.Contacts.State.BLOCKED_BY;
						notifyDataSetChanged();
						getContentResolver().update(
								QodemeContract.Contacts.CONTENT_URI,
								QodemeContract.Contacts.blockContactValues(Sync.STATE_UPDATED),
								QodemeContract.Contacts.CONTACT_QRCODE + "= '"
										+ getItem(tag).qrCode + "'", null);
						SyncHelper.requestManualSync();
					}
				}
			});
			holder.mBtnUnBlock.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View v) {
					int tag = (Integer) v.getTag();

					if (getItem(tag).state == QodemeContract.Contacts.State.BLOCKED_BY) {
						getItem(tag).state = QodemeContract.Contacts.State.APPRUVED;
						notifyDataSetChanged();

						// int updated = ce.updated;
						getContentResolver().update(QodemeContract.Contacts.CONTENT_URI,
								QodemeContract.Contacts.acceptContactValues(Sync.STATE_UPDATED),
								DbUtils.getWhereClauseForId(),
								DbUtils.getWhereArgsForId(getItem(tag)._id));
						SyncHelper.requestManualSync();
					}
				}
			});

			return convertView;

		}

		class ViewHolder {
			TextView textViewName;
			TextView textViewLocation;
			Button mBtnBlock;
			Button mBtnUnBlock;
		}
	}

}
