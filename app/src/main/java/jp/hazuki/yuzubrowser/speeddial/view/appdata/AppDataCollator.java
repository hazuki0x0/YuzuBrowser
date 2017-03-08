package jp.hazuki.yuzubrowser.speeddial.view.appdata;

import java.text.Collator;
import java.util.Comparator;

/**
 * Created by hazuki on 16/10/21.
 */

public class AppDataCollator implements Comparator<AppData> {

    private static final Collator collator = Collator.getInstance();

    @Override
    public int compare(AppData a1, AppData a2) {

        return collator.compare(a1.getAppName(), a2.getAppName());
    }
}
