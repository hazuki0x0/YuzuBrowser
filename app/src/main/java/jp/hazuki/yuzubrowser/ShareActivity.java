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

package jp.hazuki.yuzubrowser;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;

import jp.hazuki.yuzubrowser.settings.data.AppData;
import jp.hazuki.yuzubrowser.utils.WebUtils;

public class ShareActivity extends AppCompatActivity {
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (getIntent() != null && Intent.ACTION_SEND.equals(getIntent().getAction())) {
            String query = getIntent().getStringExtra(Intent.EXTRA_TEXT);
            if (!TextUtils.isEmpty(query)) {
                if (WebUtils.isUrl(query)) {
                    startBrowser(query);
                } else {
                    String text = WebUtils.extractionUrl(query);
                    if (query.equals(text)) {
                        text = WebUtils.makeUrlFromQuery(query, AppData.search_url.get(), "%s");
                    }
                    startBrowser(text);
                }
            }
        }

        finish();
    }

    private void startBrowser(String url) {
        Intent send = new Intent(this, BrowserActivity.class);
        send.setAction(Intent.ACTION_VIEW);
        send.setData(Uri.parse(url));
        startActivity(send);
    }
}
