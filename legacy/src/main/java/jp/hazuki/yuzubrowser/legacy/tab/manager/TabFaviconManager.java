/*
 * Copyright (C) 2017-2019 Hazuki
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

package jp.hazuki.yuzubrowser.legacy.tab.manager;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.view.View;
import android.widget.TextView;

import java.util.List;

import jp.hazuki.yuzubrowser.favicon.FaviconManager;
import jp.hazuki.yuzubrowser.legacy.R;
import jp.hazuki.yuzubrowser.ui.settings.AppPrefs;

public class TabFaviconManager {

    private FaviconManager manager;
    private Context context;
    private boolean iconMode;

    public TabFaviconManager(Context context, FaviconManager faviconManager) {
        this.context = context;
        manager = faviconManager;
    }

    public void onPreferenceReset(List<View> tabViews, List<TabIndexData> indexDataList) {
        if (iconMode != AppPrefs.toolbar_show_favicon.get()) {
            iconMode = AppPrefs.toolbar_show_favicon.get();

            int length = Math.min(tabViews.size(), indexDataList.size());

            if (length == 0 || tabViews.get(0).getHeight() == 0) {
                iconMode = !iconMode;
                return;
            }

            for (int i = 0; i < length; i++) {
                setFavicon(tabViews.get(i), indexDataList.get(i), iconMode);
            }
        }
    }

    public void setFavicon(View view, TabIndexData indexData) {
        setFavicon(view, indexData, iconMode);
    }

    private void setFavicon(View view, TabIndexData indexData, boolean show) {
        TextView titleTextView = view.findViewById(R.id.textView);
        if (show && indexData.getOriginalUrl() != null && !indexData.getOriginalUrl().startsWith("yuzu:")) {
            Bitmap bitmap = manager.get(indexData.getOriginalUrl());
            Drawable icon;
            if (bitmap != null) {
                icon = new BitmapDrawable(context.getResources(), bitmap);
            } else {
                icon = context.getDrawable(R.drawable.ic_page_white_24px);
            }
            int size = titleTextView.getHeight() - titleTextView.getPaddingTop() - titleTextView.getPaddingBottom();
            assert icon != null;
            icon.setBounds(0, 0, size, size);
            titleTextView.setCompoundDrawables(icon, null, null, null);
        } else {
            titleTextView.setCompoundDrawables(null, null, null, null);
        }
    }
}
