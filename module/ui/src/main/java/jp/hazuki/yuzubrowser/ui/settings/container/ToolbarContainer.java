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

package jp.hazuki.yuzubrowser.ui.settings.container;

import android.content.SharedPreferences;

import java.lang.reflect.Field;
import java.util.ArrayList;

import jp.hazuki.yuzubrowser.core.utility.log.ErrorReport;

import static jp.hazuki.yuzubrowser.ui.settings.PreferenceConstants.TOOLBAR_LOCATION_TOP;
import static jp.hazuki.yuzubrowser.ui.settings.PreferenceConstants.TOOLBAR_LOCATION_UNDEFINED;

public class ToolbarContainer implements Containable {
    public final ArrayList<Containable> mPreferenceList = new ArrayList<>();
    public final IntContainer location;
    public final IntContainer location_priority;
    public final IntContainer location_landscape;
    public final IntContainer location_landscape_priority;
    public final IntContainer size;
    public final jp.hazuki.yuzubrowser.ui.settings.container.ToolbarVisibilityContainer visibility;

    public ToolbarContainer(String id, int loc) {
        location = new IntContainer("toolbar_location_" + id, TOOLBAR_LOCATION_TOP);
        location_priority = new IntContainer("toolbar_location_priority_" + id, loc);
        location_landscape = new IntContainer("toolbar_location_l_" + id, TOOLBAR_LOCATION_UNDEFINED);
        location_landscape_priority = new IntContainer("toolbar_location_l_priority_" + id, -1);
        size = new IntContainer("toolbar_size_" + id, 48);
        visibility = new ToolbarVisibilityContainer("toolbar_visibility_" + id, 1);

        checkPreferenceList();
    }

    @Override
    public void read(SharedPreferences shared_preference) {
        for (Containable pref : mPreferenceList) {
            pref.read(shared_preference);
        }
    }

    @Override
    public void write(SharedPreferences.Editor editor) {
        for (Containable pref : mPreferenceList) {
            pref.write(editor);
        }
    }

    private void checkPreferenceList() {
        //if (!mPreferenceList.isEmpty())
        //	return;
        try {
            Field[] fields = ToolbarContainer.class.getDeclaredFields();
            for (Field field : fields) {
                Object obj = field.get(this);
                if (obj instanceof Containable) {
                    mPreferenceList.add((Containable) obj);
                }
            }
        } catch (IllegalArgumentException | IllegalAccessException e) {
            ErrorReport.printAndWriteLog(e);
        }
    }
}
