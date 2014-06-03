package com.blulabellabs.code.ui.tutorial;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

public class TutorialFragment extends Fragment {

	private static final String KEY = "tutorial.layout.id";
	
	static TutorialFragment newInstance(int position) {
		TutorialFragment tf = new TutorialFragment();
		Bundle b = new Bundle();
		b.putInt(KEY, position);
		tf.setArguments(b);
		return tf;
    }
	
	public int getShownLayoutId() {
		return getArguments().getInt(KEY, 0);
	}
	
	@Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		if (container == null) {
			return null;
		}
		return inflater.inflate(getShownLayoutId(), container, false);
    }

}
