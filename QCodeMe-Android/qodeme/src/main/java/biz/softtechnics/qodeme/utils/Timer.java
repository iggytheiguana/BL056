package biz.softtechnics.qodeme.utils;

import android.os.Handler;
import android.os.Looper;

public abstract class Timer implements Runnable {
	private Handler handler;
	private boolean scheduled;

	public Timer() {
		this.handler = new Handler(Looper.getMainLooper());
	}
	
	public Timer( Handler handler ) {
		this.handler = handler;
	}
	
	public void schedule(long delay) {
		scheduled = true;
		handler.postDelayed(this, delay);
	}
	
	public static void cancel( Timer timer ) {
		if( timer != null ) {
			timer.cancel();
		}
	}

	public void cancel() {
		if (scheduled) {
			handler.removeCallbacks(this);
			scheduled = false;
		}
	}
}