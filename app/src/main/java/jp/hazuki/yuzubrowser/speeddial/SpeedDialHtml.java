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

package jp.hazuki.yuzubrowser.speeddial;

import android.content.Context;
import android.webkit.WebResourceResponse;

import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import jp.hazuki.yuzubrowser.R;
import jp.hazuki.yuzubrowser.settings.data.AppData;
import jp.hazuki.yuzubrowser.utils.HtmlUtils;
import jp.hazuki.yuzubrowser.utils.IOUtils;

public class SpeedDialHtml {

    public static WebResourceResponse createResponse(Context context, List<SpeedDial> speedDialList) {
        StringBuilder builder = new StringBuilder(8000);
        String start = getResourceString(context, R.raw.speeddial_start);
        builder.append(start);
        for (SpeedDial speedDial : speedDialList) {
            builder.append("<div class=\"box\"><a href=\"")
                    .append(speedDial.getUrl())
                    .append("\"><img src=\"data:image/png;base64,")
                    .append(speedDial.getIcon().getIconBase64())
                    .append("\" /><div class=\"name\">")
                    .append(HtmlUtils.sanitize(speedDial.getTitle()))
                    .append("</div></a></div>");
        }
        builder.append(getResourceString(context, R.raw.speeddial_end));
        return getNoCacheResponse("text/html", builder);
    }

    public static WebResourceResponse getBaseCss(Context context) {
        return new WebResourceResponse("text/css", "UTF-8", context.getResources().openRawResource(R.raw.speeddial_css));
    }

    public static WebResourceResponse getCustomCss() {
        StringBuilder builder = new StringBuilder(400);

        if (!AppData.speeddial_show_header.get())
            builder.append(".browserName{display:none}");

        if (!AppData.speeddial_show_search.get())
            builder.append("#searchBox{display:none}");

        if (!AppData.speeddial_show_icon.get())
            builder.append(".box img{display:none;}");

        int v_column = AppData.speeddial_column.get();
        int l_column = AppData.speeddial_column_landscape.get();

        // portrait
        builder.append("@media screen and (orientation:portrait){.linkBox{max-width:")
                .append(AppData.speeddial_column_width.get() * v_column).append("px}");

        // = (100 - marginSize * (column + 1) * 2 + 2 * marginSize) / column
        builder.append(".box{width:").append((100f - 2f * v_column) / v_column).append("%}");

        builder.append(".box:nth-child(n+").append(v_column + 1).append("){order:1}}");

        // landscape
        builder.append("@media screen and (orientation:landscape){.linkBox{max-width:")
                .append(AppData.speeddial_column_width.get() * l_column).append("px}");

        // = (100 - marginSize * (column + 1) * 2 + 2 * marginSize) / column
        builder.append(".box{width:").append((100f - 2f * l_column) / l_column).append("%}");

        builder.append(".box:nth-child(n+").append(l_column + 1).append("){order:1}}");

        return getNoCacheResponse("text/css", builder);
    }

    private static WebResourceResponse getNoCacheResponse(String mimeType, CharSequence sequence) {
        WebResourceResponse response = new WebResourceResponse(mimeType, "UTF-8",
                new ByteArrayInputStream(sequence.toString().getBytes(StandardCharsets.UTF_8)));
        Map<String, String> headers = new HashMap<>();
        headers.put("Cache-Control", "no-cache");
        response.setResponseHeaders(headers);
        return response;
    }

    private static String getResourceString(Context context, int id) {
        return IOUtils.readString(context.getResources().openRawResource(id));
    }
}
