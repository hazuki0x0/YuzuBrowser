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

package jp.hazuki.yuzubrowser.reader;

import android.content.Context;
import android.support.v4.content.AsyncTaskLoader;
import android.text.TextUtils;
import android.webkit.WebSettings;

import java.util.Locale;

import jp.hazuki.yuzubrowser.reader.snacktory.HtmlFetcher;
import jp.hazuki.yuzubrowser.reader.snacktory.JResult;
import jp.hazuki.yuzubrowser.utils.Logger;

public class ReaderTask extends AsyncTaskLoader<ReaderData> {

    private boolean isLoading;
    private final String url;
    private final String userAgent;

    public ReaderTask(Context context, String url, String userAgent) {
        super(context);
        this.url = url;
        this.userAgent = userAgent;
    }

    @Override
    public ReaderData loadInBackground() {
        isLoading = true;
        HtmlFetcher fetcher = new HtmlFetcher();
        if (TextUtils.isEmpty(userAgent)) {
            fetcher.setUserAgent(WebSettings.getDefaultUserAgent(getContext()));
        } else {
            fetcher.setUserAgent(userAgent);
        }
        fetcher.setReferrer(url);
        Locale locale = Locale.getDefault();
        String language = locale.getLanguage() + "-" + locale.getCountry();
        if (language.length() >= 5) {
            fetcher.setLanguage(language);
        }

        try {
            JResult result = fetcher.fetchAndExtract(url, 2500, true);
            if (!TextUtils.isEmpty(result.getText())) {
                return new ReaderData(result.getTitle(), result.getText());
            }
        } catch (Exception e) {
            e.printStackTrace();
        } catch (OutOfMemoryError e) {
            System.gc();
            Logger.w("reader", e, "Out of memory");
        }
        return null;
    }

    @Override
    protected void onStartLoading() {
        if (!isLoading)
            forceLoad();
    }

    @Override
    protected void onStopLoading() {
        cancelLoad();
    }
}
