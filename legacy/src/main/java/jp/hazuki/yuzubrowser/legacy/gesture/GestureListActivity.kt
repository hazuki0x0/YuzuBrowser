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

package jp.hazuki.yuzubrowser.legacy.gesture

import android.content.Intent
import android.os.Bundle
import android.view.Menu
import jp.hazuki.yuzubrowser.legacy.R
import jp.hazuki.yuzubrowser.legacy.action.ActionNameArray
import jp.hazuki.yuzubrowser.ui.app.ThemeActivity

class GestureListActivity : ThemeActivity() {

    private var mGestureId: Int = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.fragment_base)

        val intent = intent ?: throw IllegalStateException("intent is null")

        val title = intent.getStringExtra(Intent.EXTRA_TITLE)

        if (title != null)
            setTitle(title)

        val actionNameArray = intent.getParcelableExtra(ActionNameArray.INTENT_EXTRA) ?: ActionNameArray(applicationContext)

        mGestureId = intent.getIntExtra(GestureManager.INTENT_EXTRA_GESTURE_ID, -1)
        if (mGestureId < 0)
            throw IllegalStateException("Unknown intent id:" + mGestureId)

        if (savedInstanceState == null) {
            supportFragmentManager.beginTransaction()
                    .replace(R.id.container, GestureListFragment(mGestureId, actionNameArray))
                    .commit()
        }
    }


    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menu.add(R.string.gesture_test).setOnMenuItemClickListener {
            val intent = Intent(applicationContext, GestureTestActivity::class.java)
            intent.putExtra(GestureManager.INTENT_EXTRA_GESTURE_ID, mGestureId)
            intent.putExtra(Intent.EXTRA_TITLE, title)
            startActivity(intent)
            false
        }
        return super.onCreateOptionsMenu(menu)
    }
}
