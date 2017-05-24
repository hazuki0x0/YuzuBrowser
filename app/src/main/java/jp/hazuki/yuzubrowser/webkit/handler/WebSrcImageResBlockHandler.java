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

package jp.hazuki.yuzubrowser.webkit.handler;

import android.app.Activity;
import android.content.Intent;

import java.lang.ref.WeakReference;

import jp.hazuki.yuzubrowser.resblock.ResourceBlockListActivity;

public class WebSrcImageResBlockHandler extends WebSrcImageHandler {
    private WeakReference<Activity> reference;

    public WebSrcImageResBlockHandler(Activity activity) {
        reference = new WeakReference<>(activity);
    }

    @Override
    public void handleUrl(String url) {
        Activity activity = reference.get();
        if (activity != null) {
            Intent intent = new Intent(activity, ResourceBlockListActivity.class);
            intent.setAction(ResourceBlockListActivity.ACTION_BLOCK_IMAGE);
            intent.putExtra(Intent.EXTRA_TEXT, url);
            activity.startActivity(intent);
        }
    }
}
