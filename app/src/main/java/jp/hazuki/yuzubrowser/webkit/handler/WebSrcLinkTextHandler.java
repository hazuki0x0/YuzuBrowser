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

package jp.hazuki.yuzubrowser.webkit.handler;

import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;

public class WebSrcLinkTextHandler extends Handler {

    private OnGetTextListener mListener;

    public WebSrcLinkTextHandler(OnGetTextListener listener) {
        mListener = listener;
    }

    @Override
    public void handleMessage(Message msg) {
        String text = msg.getData().getString("title");
        if (!TextUtils.isEmpty(text)) {
            mListener.onGetText(text);
        }
    }

    public interface OnGetTextListener {
        void onGetText(String text);
    }
}
