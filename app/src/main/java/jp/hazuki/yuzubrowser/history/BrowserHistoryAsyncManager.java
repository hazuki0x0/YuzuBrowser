package jp.hazuki.yuzubrowser.history;

import android.content.Context;

import java.util.concurrent.LinkedBlockingQueue;

import jp.hazuki.yuzubrowser.utils.Logger;

public class BrowserHistoryAsyncManager {
    private static final int ADD_URL = 1;
    private static final int UPDATE_TITLE = 2;
    private final MyThread mThread;
    private final BrowserHistoryManager mHistoryManager;

    public BrowserHistoryAsyncManager(Context context) {
        mHistoryManager = BrowserHistoryManager.getInstance(context);
        mThread = new MyThread(mHistoryManager);
        mThread.start();
    }

    public void destroy() {
        mThread.interrupt();
    }

    public void add(String url) {
        mThread.sendMessage(new MyMessage(ADD_URL, url));
    }

    public void update(String url, String title) {
        mThread.sendMessage(new MyMessage(UPDATE_TITLE, url, title));
    }

    public String[] getHistoryArray(int limit) {
        return mHistoryManager.getHistoryArray(limit);
    }

    private static final class MyMessage {
        public final int what;
        public final String url;
        public final Object obj;

        public MyMessage(int what, String url, Object obj) {
            this.what = what;
            this.url = url;
            this.obj = obj;
        }

        public MyMessage(int what, String url) {
            this.what = what;
            this.url = url;
            this.obj = null;
        }
    }

    private static class MyThread extends Thread {
        private final BrowserHistoryManager mHistoryManager;
        private final LinkedBlockingQueue<MyMessage> mMessageQueue = new LinkedBlockingQueue<>();

        MyThread(BrowserHistoryManager manager) {
            mHistoryManager = manager;
        }

        @SuppressWarnings("InfiniteLoopStatement")
        @Override
        public void run() {
            setPriority(MIN_PRIORITY);
            try {
                while (true) {
                    handleMessage(mMessageQueue.take());
                }
            } catch (InterruptedException e) {
                Logger.i("history", "thread stop");
            }
        }

        private void handleMessage(MyMessage msg) {
            switch (msg.what) {
                case ADD_URL:
                    mHistoryManager.add(msg.url);
                    break;
                case UPDATE_TITLE:
                    mHistoryManager.update(msg.url, (String) msg.obj);
                    break;
            }
        }

        public void sendMessage(MyMessage msg) {
            mMessageQueue.add(msg);
        }


    }
}
