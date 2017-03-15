package jp.hazuki.yuzubrowser.utils;

import android.os.Handler;
import android.support.annotation.NonNull;
import android.view.ViewConfiguration;

public class Api24LongPressFix {
    private final int longPressTimeout = ViewConfiguration.getLongPressTimeout();
    private final Handler handler = new Handler();
    private final OnBackLongClickListener longClickListener;

    private long time;

    public Api24LongPressFix(@NonNull OnBackLongClickListener listener) {
        longClickListener = listener;
    }

    public void onBackKeyDown() {
        time = System.currentTimeMillis();
        handler.postDelayed(longPress, longPressTimeout);
    }

    public boolean onBackKeyUp() {
        handler.removeCallbacks(longPress);
        return System.currentTimeMillis() - time > longPressTimeout;
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
