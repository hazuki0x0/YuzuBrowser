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

import java.util.List;

import jp.hazuki.yuzubrowser.R;
import jp.hazuki.yuzubrowser.utils.HtmlUtils;
import jp.hazuki.yuzubrowser.utils.IOUtils;

public class SpeedDialHtml {

    private String html;

    public SpeedDialHtml(Context context, List<SpeedDial> speedDialList) {
        StringBuilder builder = new StringBuilder(8000);
        String start = getResourceString(context, R.raw.speeddial_start);
        builder.append(start);
        for (SpeedDial speedDial : speedDialList) {
            builder.append("<div class=\"box\"><a href=\"")
                    .append(speedDial.getUrl())
                    .append("\"><img src=\"")
                    .append(speedDial.getIcon().getIconBase64())
                    .append("\" /><div class=\"name\">")
                    .append(HtmlUtils.sanitize(speedDial.getTitle()))
                    .append("</div></a></div>");
        }
        builder.append(getResourceString(context, R.raw.speeddial_end));
        html = builder.toString();
    }

    public String getSpeedDialHtml() {
        return html;
    }

    private String getResourceString(Context context, int id) {
        return IOUtils.readString(context.getResources().openRawResource(id));
    }

}
