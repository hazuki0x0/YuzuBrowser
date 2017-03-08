package jp.hazuki.yuzubrowser.speeddial;

import android.content.Context;

import java.util.List;

import jp.hazuki.yuzubrowser.R;
import jp.hazuki.yuzubrowser.utils.IOUtils;

/**
 * Created by hazuki on 17/02/19.
 */

public class SpeedDialHtml {

    private String html;

    public SpeedDialHtml(Context context, List<SpeedDial> speedDialList) {
        StringBuilder builder = new StringBuilder(8000);
        builder.append(getResourceString(context, R.raw.speeddial_start));
        for (SpeedDial speedDial : speedDialList) {
            builder.append("<div class=\"box\"><a href=\"")
                    .append(speedDial.getUrl())
                    .append("\"><div><img src=\"")
                    .append(speedDial.getIcon().getIconBase64())
                    .append("\" /><div class=\"name\">")
                    .append(speedDial.getTitle())
                    .append("</div></div></a></div>");
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
