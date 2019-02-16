/*
 * Copyright (C) 2017-2018 Hazuki
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

package jp.hazuki.yuzubrowser.legacy.utils;

import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;

public class WebDownloadUtils {
    private WebDownloadUtils() {
        throw new UnsupportedOperationException();
    }

    public static boolean shouldOpen(String contentDisposition) {
        return (contentDisposition == null || !contentDisposition.regionMatches(true, 0, "attachment", 0, 10));
    }

    public static boolean openFile(Context context, String url, String mimetype) {
        if (url.startsWith("data:")) return false;

        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setDataAndType(Uri.parse(url), mimetype);

        if (context.getPackageManager().queryIntentActivities(intent, 0).isEmpty())
            return false;

        try {
            context.startActivity(intent);
            return true;
        } catch (ActivityNotFoundException | SecurityException e) {
            return false;
        }
    }
}
