/*
 * Copyright (C) 2017 Hazuki
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

package jp.hazuki.yuzubrowser.speeddial;

import android.content.Context;
import android.graphics.Bitmap;

import java.util.ArrayList;
import java.util.concurrent.LinkedBlockingQueue;

import jp.hazuki.yuzubrowser.utils.Logger;

public class SpeedDialAsyncManager {

    private SpeedDialManager manager;
    private SDThread sdThread;

    public SpeedDialAsyncManager(Context context) {
        manager = SpeedDialManager.getInstance(context);
        sdThread = new SDThread(manager);
        sdThread.start();
    }

    public void destroy() {
        sdThread.interrupt();
    }

    public void updateAsync(String url, Bitmap icon) {
        if (url != null && icon != null)
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

    public ArrayList<SpeedDial> getAll() {
        return manager.getAll();
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
                Logger.i("Speed dial", "thread stop");
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
