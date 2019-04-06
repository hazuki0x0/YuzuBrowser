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

package jp.hazuki.yuzubrowser.legacy.gesture.multiFinger

import android.os.Bundle
import jp.hazuki.yuzubrowser.legacy.R
import jp.hazuki.yuzubrowser.legacy.gesture.multiFinger.data.MultiFingerGestureItem
import jp.hazuki.yuzubrowser.ui.app.ThemeActivity

class MultiFingerSettingsActivity : ThemeActivity(), MfsFragment.OnMfsFragmentListener, MfsListFragment.OnMfsListListener, MfsEditFragment.OnMfsEditFragmentListener {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.fragment_base)

        if (savedInstanceState == null) {
            supportFragmentManager.beginTransaction()
                    .replace(R.id.container, MfsFragment())
                    .commit()
        }
    }

    override fun onGoToList() {
        supportFragmentManager.beginTransaction()
                .addToBackStack("")
                .replace(R.id.container, MfsListFragment(), "list")
                .commit()
    }

    override fun goEdit(index: Int, item: MultiFingerGestureItem) {
        supportFragmentManager.beginTransaction()
                .addToBackStack("")
                .replace(R.id.container, MfsEditFragment.newInstance(index, item))
                .commit()
    }

    override fun onEdited(index: Int, item: MultiFingerGestureItem) {
        val fragment = supportFragmentManager.findFragmentByTag("list")
        (fragment as? MfsListFragment)?.onEdited(index, item)
    }
}
