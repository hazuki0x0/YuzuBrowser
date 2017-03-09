package jp.hazuki.yuzubrowser.speeddial.view.appinfo;

import java.text.Collator;
import java.util.Comparator;

/**
 * Created by hazuki on 16/10/21.
 */

public class AppInfoCollator implements Comparator<AppInfo> {

    private static final Collator collator = Collator.getInstance();

    @Override
    public int compare(AppInfo a1, AppInfo a2) {

        return collator.compare(a1.getAppName(), a2.getAppName());
    }
}
