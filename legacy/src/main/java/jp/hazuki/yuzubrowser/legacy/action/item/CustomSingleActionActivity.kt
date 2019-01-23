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

package jp.hazuki.yuzubrowser.legacy.action.item

import android.os.Bundle
import jp.hazuki.yuzubrowser.legacy.R
import jp.hazuki.yuzubrowser.legacy.action.Action
import jp.hazuki.yuzubrowser.legacy.action.ActionNameArray
import jp.hazuki.yuzubrowser.ui.app.ThemeActivity

class CustomSingleActionActivity : ThemeActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.fragment_base)
        setTitle(R.string.action_custom_setting)

        if (savedInstanceState == null) {
            val intent = intent
            val action = intent.getParcelableExtra<Action>(EXTRA_ACTION)
            val name = intent.getStringExtra(EXTRA_NAME)
            val actionNameArray = intent.getParcelableExtra<ActionNameArray>(ActionNameArray.INTENT_EXTRA)
            supportFragmentManager.beginTransaction()
                    .replace(R.id.container, CustomSingleActionFragment.newInstance(action, name, actionNameArray))
                    .commit()
        }
    }

    companion object {

        const val EXTRA_ACTION = "CustomSingleActionActivity.extra.action"
        const val EXTRA_NAME = "CustomSingleActionActivity.extra.name"
        const val EXTRA_ICON = "CustomSingleActionActivity.extra.icon"
        const val EXTRA_ICON_MODE = "CustomSingleActionActivity.extra.icon.mode"
    }

}
