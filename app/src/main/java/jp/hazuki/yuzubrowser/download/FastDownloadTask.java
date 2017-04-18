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

package jp.hazuki.yuzubrowser.download;

import android.content.Context;
import android.support.v4.content.AsyncTaskLoader;
import android.text.TextUtils;
import android.webkit.CookieManager;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import jp.hazuki.yuzubrowser.utils.HttpUtils;
import jp.hazuki.yuzubrowser.utils.net.HttpClientBuilder;
import jp.hazuki.yuzubrowser.utils.net.HttpResponseData;

public class FastDownloadTask extends AsyncTaskLoader<File> {
    private static final int DOWNLOAD_BUFFER_SIZE = 1024 * 10;

    private boolean isLoading;
    private String mUrl;
    private String mReferer;
    private String mDefaultExt;

    public FastDownloadTask(Context context, String url, String referer, String defaultExt) {
        super(context);
        mUrl = url;
        mReferer = referer;
        mDefaultExt = defaultExt;
    }

    @Override
    public File loadInBackground() {
        isLoading = true;
        if (mUrl.startsWith("data:")) {
            return DownloadUtils.saveBase64Image(mUrl);
        } else {
            return normalDownload();
        }
    }

    private File normalDownload() {
        HttpClientBuilder httpClient = HttpClientBuilder.createInstance(mUrl);
        if (httpClient == null)
            return null;

        String cookie = CookieManager.getInstance().getCookie(mUrl);
        if (!TextUtils.isEmpty(cookie)) {
            httpClient.setHeader("Cookie", cookie);
        }

        if (!TextUtils.isEmpty(mReferer)) {
            httpClient.setHeader("Referer", mReferer);

        }

        HttpResponseData response = httpClient.connect();

        File file = HttpUtils.getFileName(mUrl, mDefaultExt, response.getHeaderFields());
        if (file.getParentFile() != null) {
            file.getParentFile().mkdirs();
        }

        try (OutputStream outputStream = new FileOutputStream(file);
             InputStream inputStream = response.getInputStream()) {

            int n;
            byte[] buffer = new byte[DOWNLOAD_BUFFER_SIZE];

            while ((n = inputStream.read(buffer)) >= 0) {
                outputStream.write(buffer, 0, n);
            }

            outputStream.flush();

            return file;
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            httpClient.destroy();
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
