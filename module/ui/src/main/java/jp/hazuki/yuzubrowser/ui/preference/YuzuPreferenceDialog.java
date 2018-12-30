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

package jp.hazuki.yuzubrowser.ui.preference;

import android.os.Bundle;
import android.support.v7.preference.Preference;
import android.support.v7.preference.PreferenceDialogFragmentCompat;

public abstract class YuzuPreferenceDialog extends PreferenceDialogFragmentCompat {

    protected static <T extends YuzuPreferenceDialog> T newInstance(T dialog, Preference preference) {
        Bundle bundle = new Bundle();
        bundle.putString(ARG_KEY, preference.getKey());
        dialog.setArguments(bundle);
        return dialog;
    }

    @SuppressWarnings("unchecked")
    public <T extends Preference> T getParentPreference() {
        return (T) getPreference();
    }
}
