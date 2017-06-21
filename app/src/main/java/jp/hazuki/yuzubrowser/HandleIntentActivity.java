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

package jp.hazuki.yuzubrowser;

import android.app.SearchManager;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Browser;
import android.speech.RecognizerResultsIntent;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.widget.Toast;

import java.util.ArrayList;

import jp.hazuki.yuzubrowser.settings.data.AppData;
import jp.hazuki.yuzubrowser.utils.WebUtils;

public class HandleIntentActivity extends AppCompatActivity {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (getIntent() != null && getIntent().getAction() != null) {
            handleIntent(getIntent());
        }

        finish();
    }

    private void handleIntent(@NonNull Intent intent) {
        String action = intent.getAction();

        if (Intent.ACTION_VIEW.equals(action)) {
            String url = intent.getDataString();
            if (TextUtils.isEmpty(url))
                url = intent.getStringExtra(Intent.EXTRA_TEXT);
            if (!TextUtils.isEmpty(url)) {
                startBrowser(url, false);
                return;
            }
        } else if (Intent.ACTION_WEB_SEARCH.equals(action)) {
            String url = WebUtils.makeSearchUrlFromQuery(intent.getStringExtra(SearchManager.QUERY), AppData.search_url.get(), "%s");
            if (!TextUtils.isEmpty(url)) {
                startBrowser(url, getPackageName().equals(intent.getStringExtra(Browser.EXTRA_APPLICATION_ID)));
                return;
            }
        } else if (RecognizerResultsIntent.ACTION_VOICE_SEARCH_RESULTS.equals(action)) {
            ArrayList<String> urls = intent.getStringArrayListExtra(RecognizerResultsIntent.EXTRA_VOICE_SEARCH_RESULT_URLS);
            if (urls != null && !urls.isEmpty()) {
                startBrowser(urls.get(0), false);
                return;
            }
        }

        Toast.makeText(getApplicationContext(), R.string.page_not_found, Toast.LENGTH_SHORT).show();
    }

    private void startBrowser(String url, boolean window) {
        Intent send = new Intent(this, BrowserActivity.class);
        send.setAction(Intent.ACTION_VIEW);
        send.setData(Uri.parse(url));
        send.putExtra(BrowserActivity.EXTRA_WINDOW_MODE, window);
        startActivity(send);
    }
}
