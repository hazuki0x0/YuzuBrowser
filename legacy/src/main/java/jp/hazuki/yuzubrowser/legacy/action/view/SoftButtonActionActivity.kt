/*
 * Copyright (C) 2017-2021 Hazuki
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

package jp.hazuki.yuzubrowser.legacy.action.view

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.commit
import jp.hazuki.yuzubrowser.legacy.R
import jp.hazuki.yuzubrowser.legacy.action.ActionManager
import jp.hazuki.yuzubrowser.ui.app.ThemeActivity

class SoftButtonActionActivity : ThemeActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.fragment_base)

        intent?.let {
            val title = it.getStringExtra(Intent.EXTRA_TITLE)
            setTitle(title)
        }

        if (savedInstanceState == null) {
            intent?.let {
                val isDetail = it.getBooleanExtra(MODE_DETAIL, false)
                val actionType = it.getIntExtra(ActionManager.INTENT_EXTRA_ACTION_TYPE, 0)
                val actionId = it.getIntExtra(ActionManager.INTENT_EXTRA_ACTION_ID, 0)
                val fragment = if (isDetail) {
                    val position = it.getIntExtra(EXTRA_ACTION_POSITION, 0)
                    SoftButtonActionDetailFragment(actionType, actionId, position)
                } else {
                    SoftButtonActionArrayFragment(actionType, actionId)
                }

                supportFragmentManager.commit {
                    replace(R.id.container, fragment)
                }
            }
        }
    }


    companion object {
        const val MODE_DETAIL = "detail"
        const val EXTRA_ACTION_POSITION = "pos"

        fun createIntent(context: Context, title: String, actionType: Int, actionId: Int, position: Int): Intent {
            return Intent(context, SoftButtonActionActivity::class.java).apply {
                putExtra(MODE_DETAIL, true)
                putExtra(Intent.EXTRA_TITLE, title)
                putExtra(ActionManager.INTENT_EXTRA_ACTION_TYPE, actionType)
                putExtra(ActionManager.INTENT_EXTRA_ACTION_ID, actionId)
                putExtra(EXTRA_ACTION_POSITION, position)
            }
        }
    }
}
