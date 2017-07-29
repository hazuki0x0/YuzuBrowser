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

package jp.hazuki.yuzubrowser.gesture.multiFinger;

import android.app.Fragment;
import android.os.Bundle;
import android.support.annotation.Nullable;

import jp.hazuki.yuzubrowser.R;
import jp.hazuki.yuzubrowser.gesture.multiFinger.data.MultiFingerGestureItem;
import jp.hazuki.yuzubrowser.utils.app.ThemeActivity;

public class MultiFingerSettingsActivity extends ThemeActivity implements MfsFragment.OnMfsFragmentListener, MfsListFragment.OnMfsListListener, MfsEditFragment.OnMfsEditFragmentListener {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.fragment_base);

        if (savedInstanceState == null) {
            getFragmentManager().beginTransaction()
                    .replace(R.id.container, new MfsFragment())
                    .commit();
        }
    }

    @Override
    public void onGoToList() {
        getFragmentManager().beginTransaction()
                .addToBackStack("")
                .replace(R.id.container, new MfsListFragment(), "list")
                .commit();
    }

    @Override
    public void goEdit(int index, MultiFingerGestureItem item) {
        getFragmentManager().beginTransaction()
                .addToBackStack("")
                .replace(R.id.container, MfsEditFragment.newInstance(index, item))
                .commit();
    }

    @Override
    public void onEdited(int index, MultiFingerGestureItem item) {
        Fragment fragment = getFragmentManager().findFragmentByTag("list");
        if (fragment instanceof MfsListFragment) {
            ((MfsListFragment) fragment).onEdited(index, item);
        }
    }
}
