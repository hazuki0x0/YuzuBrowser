/*
 * Copyright (c) 2017 Hazuki
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package jp.hazuki.yuzubrowser.webkit;

import android.support.annotation.NonNull;

/**
 * Created by hazuki on 17/03/09.
 */

public class ScrollController {
    private final OnScrollEnable mScrollEnableListener;
    private int mScrollY;

    public ScrollController(@NonNull OnScrollEnable listener) {
        mScrollEnableListener = listener;
    }

    public void onMove(int scrollY) {
        mScrollEnableListener.onScrollEnable(mScrollY != 0);
        if (scrollY > 0) {
            mScrollY = scrollY;
        }
    }

    public void onPageChange() {
        mScrollY = 0;
    }

    public interface OnScrollEnable {
        void onScrollEnable(boolean enable);
    }
}
