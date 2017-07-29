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

package jp.hazuki.yuzubrowser.utils.app;

import android.os.Handler;
import android.view.KeyEvent;
import android.view.ViewConfiguration;

public class LongPressFixActivity extends ThemeActivity {
    private final int longPressTimeout = ViewConfiguration.getLongPressTimeout();
    private final Handler handler = new Handler();

    private long time;

    @Override
    public boolean dispatchKeyEvent(KeyEvent event) {
        if (event.getKeyCode() == KeyEvent.KEYCODE_BACK) {
            switch (event.getAction()) {
                case KeyEvent.ACTION_DOWN:
                    time = System.currentTimeMillis();
                    handler.postDelayed(longPress, longPressTimeout);
                    break;
                case KeyEvent.ACTION_UP:
                    handler.removeCallbacks(longPress);
                    if (System.currentTimeMillis() - time > longPressTimeout)
                        return true;
                    break;
            }
        }
        return super.dispatchKeyEvent(event);
    }

    public final void onDestroyActionMode() {
        handler.removeCallbacks(longPress);
    }

    private Runnable longPress = new Runnable() {
        @Override
        public void run() {
            onBackKeyLongPressed();
        }
    };

    public void onBackKeyLongPressed() {

    }
}
