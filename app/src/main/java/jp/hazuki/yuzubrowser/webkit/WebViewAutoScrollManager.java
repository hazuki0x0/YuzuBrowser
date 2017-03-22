/*
 * Copyright (c) 2017 Hazuki
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package jp.hazuki.yuzubrowser.webkit;

import android.os.Handler;

public class WebViewAutoScrollManager {

    private boolean isRunning = false;
    private boolean init = false;
    private double scrollSpeed;
    private double scrollY;
    private int scrollMax;
    private Handler handler;
    private OnStop mOnStopListener;

    public WebViewAutoScrollManager() {
        handler = new Handler();
    }

    public void start(final CustomWebView webView, int speed) {
        scrollSpeed = speed * 0.01;
        isRunning = true;
        init = true;
        scrollY = webView.computeVerticalScrollOffsetMethod();
        scrollMax = webView.computeVerticalScrollRangeMethod() - webView.computeVerticalScrollExtentMethod();

        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                init = false;
            }
        }, 200);

        final Runnable runScroll = new Runnable() {
            @Override
            public void run() {
                scrollY += scrollSpeed;
                if (scrollY > scrollMax) {
                    scrollY = scrollMax;
                    stop();
                }
                webView.scrollTo(webView.getScrollX(), (int) scrollY);
            }
        };

        new Thread(new Runnable() {
            @Override
            public void run() {
                while (isRunning) {
                    try {
                        Thread.sleep(10);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    handler.post(runScroll);
                }
            }
        }).start();
    }

    public void stop() {
        if (init) return;
        isRunning = false;
        if (mOnStopListener != null)
            mOnStopListener.onStop();
    }

    public interface OnStop {
        void onStop();
    }

    public void setOnStopListener(OnStop listener) {
        mOnStopListener = listener;
    }
}
