package biz.softtechnics.qodeme.ui.common;

import biz.softtechnics.qodeme.Application;
import android.content.Context;
import android.util.AttributeSet;
import android.widget.TextView;


public class TextViewRegular extends TextView {
	Context mContext;

	public TextViewRegular(Context context) {
		super(context);
		this.mContext = context;
		init();
	}

	public TextViewRegular(Context context, AttributeSet attrs) {
		super(context, attrs);
		this.mContext = context;
		init();
	}

	public TextViewRegular(Context context, AttributeSet attrs, int defStyle) {
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
