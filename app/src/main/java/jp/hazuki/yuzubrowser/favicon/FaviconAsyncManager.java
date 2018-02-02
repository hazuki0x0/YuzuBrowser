/*
 * Copyright (C) 2017-2018 Hazuki
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package jp.hazuki.yuzubrowser.favicon;

import android.content.Context;
import android.graphics.Bitmap;

import java.util.concurrent.LinkedBlockingQueue;

import jp.hazuki.yuzubrowser.utils.Logger;

public class FaviconAsyncManager {

    private final FaviconManager manager;
    private FaviconThread faviconThread;

    public FaviconAsyncManager(Context context) {
        manager = FaviconManager.getInstance(context);
        faviconThread = new FaviconThread();
        faviconThread.start();
    }

    public void destroy() {
        faviconThread.interrupt();
    }

    public void updateAsync(String url, Bitmap icon) {
        if (url != null && icon != null)
            faviconThread.addQueue(new FaviconItem(url, icon));
    }

    public Bitmap get(String url) {
        return manager.get(url);
    }

    private static final class FaviconItem {
        private final String url;
        private final Bitmap icon;

        FaviconItem(String url, Bitmap icon) {
            this.url = url;
            this.icon = icon;
        }
    }

    private class FaviconThread extends Thread {

        private final LinkedBlockingQueue<FaviconItem> queue = new LinkedBlockingQueue<>();

        @SuppressWarnings("InfiniteLoopStatement")
        @Override
        public void run() {
            setPriority(MIN_PRIORITY);
            try {
                while (true) {
                    update(queue.take());
                }
            } catch (InterruptedException e) {
                Logger.i("Speed dial", "thread stop");
            }
        }

        private void update(FaviconItem item) {
            manager.update(item.url, item.icon);
        }

        void addQueue(FaviconItem item) {
            queue.add(item);
        }

    }
}
