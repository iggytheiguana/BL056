package biz.softtechnics.qodeme.ui.common;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.Button;
import biz.softtechnics.qodeme.Application;


public class ButtonRegular extends Button {
	Context mContext;

	public ButtonRegular(Context context) {
		super(context);
		this.mContext = context;
		init();
	}

	public ButtonRegular(Context context, AttributeSet attrs) {
		super(context, attrs);
		this.mContext = context;
		init();
	}

	public ButtonRegular(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		this.mContext = context;
		init();
	}

	public void init() {
		if (!isInEditMode()) {
			setTypeface(Application.typefaceRegular);
		} else {
		}
	}
}
