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

package jp.hazuki.yuzubrowser.adblock;

import android.content.Context;
import android.net.Uri;
import android.webkit.WebResourceResponse;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;

import jp.hazuki.yuzubrowser.utils.ErrorReport;
import jp.hazuki.yuzubrowser.utils.IOUtils;
import jp.hazuki.yuzubrowser.utils.fastmatch.FastMatcher;
import jp.hazuki.yuzubrowser.utils.fastmatch.FastMatcherList;

public class AdBlockController {
    private static final EmptyInputStream DUMMY_TEXT = new EmptyInputStream();
    private byte[] DUMMY_IMAGE;

    private AdBlockManager manager;
    private FastMatcherList blackList;
    private FastMatcherList whiteList;
    private FastMatcherList whitePageList;

    public AdBlockController(Context context) {
        manager = new AdBlockManager(context);

        try {
            DUMMY_IMAGE = IOUtils.readByte(context.getResources().getAssets().open("blank.png"));
        } catch (IOException e) {
            ErrorReport.printAndWriteLog(e);
        }

        new Thread(new Runnable() {
            @Override
            public void run() {
                blackList = manager.getFastMatcherCachedList(AdBlockManager.BLACK_TABLE_NAME);
                whiteList = manager.getFastMatcherCachedList(AdBlockManager.WHITE_TABLE_NAME);
                whitePageList = manager.getFastMatcherCachedList(AdBlockManager.WHITE_PAGE_TABLE_NAME);
            }
        }).run();
    }

    public boolean isBlock(Uri pageUri, Uri uri) {
        if (whitePageList == null || whiteList == null || blackList == null) return false;
        if (pageUri != null) {
            for (FastMatcher matcher : whitePageList.getMatcherList())
                if (matcher.match(pageUri))
                    return false;
        }


        for (FastMatcher matcher : whiteList.getMatcherList())
            if (matcher.match(uri))
                return false;

        for (FastMatcher matcher : blackList.getMatcherList())
            if (matcher.match(uri))
                return true;

        return false;
    }

    public void onResume() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                manager.updateOrder(AdBlockManager.BLACK_TABLE_NAME, blackList);
                manager.updateOrder(AdBlockManager.WHITE_TABLE_NAME, whiteList);
                manager.updateOrder(AdBlockManager.WHITE_PAGE_TABLE_NAME, whitePageList);
            }
        }).run();
    }

    public WebResourceResponse createDummy(Uri uri) {
        String last = uri.getLastPathSegment();
        if (last != null && (last.endsWith(".js") || last.endsWith(".css"))) {
            return new WebResourceResponse("text/html", "UTF-8", DUMMY_TEXT);
        } else {
            return new WebResourceResponse("image/png", null, new ByteArrayInputStream(DUMMY_IMAGE));
        }
    }

    private static final class EmptyInputStream extends InputStream {
        @Override
        public int read() throws IOException {
            return -1;
        }
    }
}
