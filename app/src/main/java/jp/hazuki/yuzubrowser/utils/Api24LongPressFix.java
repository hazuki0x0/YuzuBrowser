package jp.hazuki.yuzubrowser.utils;

import android.os.Handler;
import android.support.annotation.NonNull;
import android.view.ViewConfiguration;

public class Api24LongPressFix {
    private long time;
    private final OnBackLongClickListener longClickListener;
    private Handler handler = new Handler();

    public Api24LongPressFix(@NonNull OnBackLongClickListener listener) {
        longClickListener = listener;
    }

    public void onBackKeyDown() {
        time = System.currentTimeMillis();
        handler.postDelayed(longPress, ViewConfiguration.getLongPressTimeout());
    }

    public boolean onBackKeyUp() {
        handler.removeCallbacks(longPress);
        return System.currentTimeMillis() - time > ViewConfiguration.getLongPressTimeout();
    }

    private Runnable longPress = new Runnable() {
        @Override
        public void run() {
            longClickListener.onBackLongClick();
        }
    };

    public interface OnBackLongClickListener {
        void onBackLongClick();
    }
}
