package biz.softtechnics.qodeme.ui.common;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Paint.Cap;
import android.graphics.Paint.Join;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.view.View;
import android.widget.TextView;
import biz.softtechnics.qodeme.R;

public class CustomDotView extends TextView {

	private Paint mPaintLine;
	private Paint mPaintCircle;
	private boolean isReply;
	private boolean isVerticalLine = true;
	private boolean isSecondVerticalLine1 = false;
	private boolean isSecondVerticalLine2 = false;
	private boolean isCircle = true;
	private int dotColor;
	Bitmap circle = null;
	int textSize = 16;
	int textPixel = 16;
	int padding = 5;
	int paddingDp = 5;
	float lineWidth = 1;

	public CustomDotView(Context context) {
		this(context, null);
	}

	public CustomDotView(Context context, AttributeSet attrs) {
		this(context, attrs, 0);
	}

	@SuppressLint("Recycle")
	public CustomDotView(Context context, AttributeSet attrs, int defStyleAttr) {
		super(context, attrs, defStyleAttr);

		if (attrs != null) {
			// get the number of columns in portrait and landscape
			TypedArray typedArray = context.obtainStyledAttributes(attrs,
					R.styleable.CustomDotView, defStyleAttr, 0);

			textSize = typedArray.getDimensionPixelSize(R.styleable.CustomDotView_text_size, 16);
			isVerticalLine = typedArray.getBoolean(R.styleable.CustomDotView_vertical_line, true);
			paddingDp = typedArray.getDimensionPixelOffset(R.styleable.CustomDotView_padding, 5);

		}
		init();
	}

	public float convertDpToPixel(float dp, Context context) {
		Resources resources = context.getResources();
		DisplayMetrics metrics = resources.getDisplayMetrics();
		float px = dp * (metrics.densityDpi / 160f);
		return px;
	}

	private void init() {

		padding = (int) convertDpToPixel(paddingDp, this.getContext());

		mPaintLine = new Paint();
		mPaintLine.setFlags(Paint.ANTI_ALIAS_FLAG);
		mPaintLine.setStrokeCap(Cap.ROUND);
		mPaintLine.setStrokeJoin(Join.ROUND);
		mPaintLine.setAntiAlias(true);
		mPaintLine.setColor(getResources().getColor(R.color.line_color));

		mPaintCircle = new Paint();
		mPaintCircle.setAntiAlias(true);
		mPaintCircle.setFlags(Paint.ANTI_ALIAS_FLAG);
		mPaintCircle.setStyle(Paint.Style.FILL);
		mPaintCircle.setColor(getResources().getColor(R.color.text_message_text));
		lineWidth = convertDpToPixel(1, getContext());

		mPaintLine.setStrokeWidth(lineWidth);
		// mPaintCircle.setColor(Color.RED);
		// Resources res = getResources();
		// final Drawable drawable = res.getDrawable(R.drawable.dot);
		// // drawable.setColorFilter(Color.YELLOW, Mode.SRC_ATOP);
		// circle = drawableToBitmap(drawable);

	}

	public static Bitmap drawableToBitmap(Drawable drawable) {
		if (drawable instanceof BitmapDrawable) {
			return ((BitmapDrawable) drawable).getBitmap();
		}

		Bitmap bitmap = Bitmap.createBitmap(drawable.getIntrinsicWidth(),
				drawable.getIntrinsicHeight(), Config.ARGB_8888);
		Canvas canvas = new Canvas(bitmap);
		drawable.setBounds(0, 0, canvas.getWidth(), canvas.getHeight());
		drawable.draw(canvas);

		return bitmap;
	}

	@Override
	protected void onDraw(Canvas canvas) {
		// if (isVerticalLine)
		// canvas.drawLine((textSize / 2)+2, 0, (textSize / 2)+2, getHeight(),
		// mPaintLine);

		if (isReply()) {
			if ((!isSecondVerticalLine1 && isSecondVerticalLine2)
					|| (!isSecondVerticalLine1 && !isSecondVerticalLine2))
				canvas.drawLine((textSize / 2) + 2, padding + 2 + (textSize / 2), getWidth() - 15,
						padding + 2 + (textSize / 2), mPaintLine);
			if (isSecondVerticalLine1)
				canvas.drawLine(getWidth() - 15, 0, getWidth() - 15, padding + 2 + (textSize / 2),
						mPaintLine);
			if (isSecondVerticalLine2)
				canvas.drawLine(getWidth() - 15, padding + 2 + (textSize / 2), getWidth() - 15,
						getHeight(), mPaintLine);
			if (isCircle)
				canvas.drawCircle(getWidth() - 15, padding + 2 + (textSize / 2), (textSize / 2) - 2,
						mPaintCircle);// RADIUS = 7
			// canvas.drawLine(10, padding+2+(textSize/2), getWidth() - 15,
			// padding+2+(textSize/2), mPaintLine);

			// canvas.drawBitmap(circle, 0,0,new Paint());
		} else {
			// int textHeight = (int) mPaintCircle.measureText("T");
			if (isCircle)
				canvas.drawCircle((textSize / 2) + 2, padding + 2 + (textSize / 2), (textSize / 2) - 2,
						mPaintCircle);
			// canvas.drawBitmap(circle, 0,0,new Paint());
		}

	}

	public void setDotColor(int dotColor) {
		this.dotColor = dotColor;
		mPaintCircle.setColor(dotColor);
	}

	public int getDotColor() {
		return dotColor;
	}

	public void setReply(boolean isReply) {
		this.isReply = isReply;
	}

	public boolean isReply() {
		return isReply;
	}

	public void setSecondVerticalLine(boolean isSecondVerticalLine) {
		this.isSecondVerticalLine1 = isSecondVerticalLine;
	}

	public boolean isSecondVerticalLine() {
		return isSecondVerticalLine1;
	}

	public void setSecondVerticalLine2(boolean isSecondVerticalLine2) {
		this.isSecondVerticalLine2 = isSecondVerticalLine2;
	}

	public boolean isSecondVerticalLine2() {
		return isSecondVerticalLine2;
	}

	public void setCircle(boolean isCircle) {
		this.isCircle = isCircle;
	}

	public boolean isCircle() {
		return isCircle;
	}

}
