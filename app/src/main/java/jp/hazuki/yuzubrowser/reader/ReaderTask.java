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
import android.graphics.Canvas;
import android.graphics.ColorFilter;
import android.graphics.PixelFormat;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.content.AsyncTaskLoader;
import android.text.Html;
import android.text.Spanned;
import android.text.TextUtils;
import android.webkit.WebSettings;

import java.util.Locale;

import jp.hazuki.yuzubrowser.reader.snacktory.HtmlFetcher;
import jp.hazuki.yuzubrowser.reader.snacktory.JResult;
import jp.hazuki.yuzubrowser.utils.HttpUtils;
import jp.hazuki.yuzubrowser.utils.ImageUtils;
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
                Spanned html;
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                    html = Html.fromHtml(result.getText(), Html.FROM_HTML_MODE_LEGACY, this::getImage, null);
                } else {
                    html = Html.fromHtml(result.getText(), this::getImage, null);
                }
                return new ReaderData(result.getTitle(), html);
            }
        } catch (Exception e) {
            e.printStackTrace();
        } catch (OutOfMemoryError e) {
            System.gc();
            Logger.w("reader", e, "Out of memory");
        }
        return null;
    }

    private Drawable getImage(String imageUrl) {
        Drawable drawable = ImageUtils.getDrawable(getContext(), HttpUtils.getImage(imageUrl, userAgent, url));
        if (drawable != null) {
            return drawable;
        } else {
            return new ColorDrawable(0);
        }
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

    private static class DummyDrawable extends Drawable {

        @Override
        public void draw(@NonNull Canvas canvas) {

        }

        @Override
        public void setAlpha(int i) {

        }

        @Override
        public void setColorFilter(@Nullable ColorFilter colorFilter) {

        }

        @Override
        public int getOpacity() {
            return PixelFormat.UNKNOWN;
        }

        @Override
        public int getIntrinsicWidth() {
            return 0;
        }

        @Override
        public int getIntrinsicHeight() {
            return 0;
        }
    }
}
