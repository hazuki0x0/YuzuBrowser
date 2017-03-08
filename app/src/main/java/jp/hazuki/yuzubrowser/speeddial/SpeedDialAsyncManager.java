package jp.hazuki.yuzubrowser.speeddial;

import android.content.Context;
import android.graphics.Bitmap;

import java.util.concurrent.LinkedBlockingQueue;

import jp.hazuki.yuzubrowser.utils.Logger;

/**
 * Created by hazuki on 17/02/20.
 */

public class SpeedDialAsyncManager extends SpeedDialManager {

    private SDThread sdThread;

    public SpeedDialAsyncManager(Context context) {
        super(context);
        sdThread = new SDThread(this);
        sdThread.start();
    }

    public void destroy() {
        sdThread.interrupt();
    }

    @Override
    public void update(final String url, final Bitmap icon) {
        sdThread.addQueue(new SDItem(url, icon));
    }

    private static final class SDItem {
        private final String url;
        private final Bitmap icon;

        SDItem(String url, Bitmap icon) {
            this.url = url;
            this.icon = icon;
        }
    }

    private static class SDThread extends Thread {
        private final SpeedDialManager manager;
        private final LinkedBlockingQueue<SDItem> queue = new LinkedBlockingQueue<>();

        SDThread(SpeedDialManager speedDialManager) {
            manager = speedDialManager;
        }

        @SuppressWarnings("InfiniteLoopStatement")
        @Override
        public void run() {
            setPriority(MIN_PRIORITY);
            try {
                while (true) {
                    update(queue.take());
                }
            } catch (InterruptedException e) {
                Logger.d("Speed dial", "thread stop");
            }
        }

        private void update(SDItem item) {
            manager.update(item.url, item.icon);
        }

        void addQueue(SDItem item) {
            queue.add(item);
        }

    }
}
