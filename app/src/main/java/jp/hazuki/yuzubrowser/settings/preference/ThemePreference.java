package jp.hazuki.yuzubrowser.settings.preference;

import android.content.Context;
import android.preference.ListPreference;
import android.util.AttributeSet;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import jp.hazuki.yuzubrowser.BrowserApplication;
import jp.hazuki.yuzubrowser.R;

/**
 * Created by hazuki on 17/01/26.
 */

public class ThemePreference extends ListPreference {
    public ThemePreference(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    private void init() {
        File dir = new File(BrowserApplication.getExternalUserDirectory(), "theme");

        if (!dir.isDirectory()) {
            dir.delete();
        }

        if (!dir.exists()) {
            dir.mkdirs();
        }

        File noMedia = new File(dir, ".nomedia");

        if (!noMedia.exists()) {
            try {
                noMedia.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        File[] themes = dir.listFiles();

        List<String> themeList = new ArrayList<>();
        List<String> valueList = new ArrayList<>();

        //Add default
        themeList.add(getContext().getString(R.string.default_text));
        valueList.add("");

        for (File theme : themes) {
            if (theme.isDirectory()) {
                themeList.add(theme.getName());
                valueList.add(theme.getName());
            }
        }

        setEntries(themeList.toArray(new String[themeList.size()]));
        setEntryValues(valueList.toArray(new String[valueList.size()]));
    }
}
