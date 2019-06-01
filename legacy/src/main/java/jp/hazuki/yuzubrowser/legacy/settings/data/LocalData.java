/*
 * Copyright (C) 2017-2019 Hazuki
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

package jp.hazuki.yuzubrowser.legacy.settings.data;

import android.content.Context;
import android.content.SharedPreferences;

import java.lang.reflect.Field;
import java.util.ArrayList;

import jp.hazuki.yuzubrowser.core.utility.log.ErrorReport;
import jp.hazuki.yuzubrowser.ui.settings.container.Containable;

public class LocalData {
    private final String mPreferenceName;

    protected LocalData(String preferenceName) {
        mPreferenceName = preferenceName;
    }

    public boolean load(Context context) {
        SharedPreferences shared_preference = context.getSharedPreferences(mPreferenceName, Context.MODE_PRIVATE);
        for (Containable pref : getPreferenceList()) {
            pref.read(shared_preference);
        }
        return true;
    }

    public boolean commit(Context context) {
        SharedPreferences.Editor editor = context.getSharedPreferences(mPreferenceName, Context.MODE_PRIVATE).edit();
        for (Containable pref : getPreferenceList()) {
            pref.write(editor);
        }
        return editor.commit();
    }

    protected ArrayList<Containable> getPreferenceList() {
        ArrayList<Containable> list;

        list = new ArrayList<>();
        try {
            Field[] fields = getClass().getDeclaredFields();
            for (Field field : fields) {
                Object obj = field.get(this);
                if (obj instanceof Containable) {// null returns false
                    list.add((Containable) obj);
                }
            }
        } catch (IllegalArgumentException | IllegalAccessException e) {
            ErrorReport.printAndWriteLog(e);
        }

        return list;
    }
}
