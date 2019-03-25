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

package jp.hazuki.yuzubrowser.legacy.action.view

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.Parcelable
import jp.hazuki.yuzubrowser.legacy.R
import jp.hazuki.yuzubrowser.legacy.action.Action
import jp.hazuki.yuzubrowser.ui.app.OnActivityResultListener
import jp.hazuki.yuzubrowser.ui.app.StartActivityInfo
import jp.hazuki.yuzubrowser.ui.app.ThemeActivity

class CloseAutoSelectActivity : ThemeActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.fragment_base)

        supportFragmentManager.beginTransaction()
                .replace(R.id.container, CloseAutoSelectFragment(
                        intent?.getParcelableExtra(DEFAULT),
                        intent?.getParcelableExtra(INTENT),
                        intent?.getParcelableExtra(WINDOW)))
                .commit()
    }

    class Builder(private val con: Context) {
        private var listener: OnActivityResultListener? = null

        fun setListener(callback: (defaultAction: Action, intentAction: Action, windowAction: Action) -> Unit): Builder {
            listener = { _, resultCode, intent ->
                if (resultCode == RESULT_OK && intent != null) {
                    val defaultAction = intent.getParcelableExtra<Action>(DEFAULT)
                    val intentAction = intent.getParcelableExtra<Action>(INTENT)
                    val windowAction = intent.getParcelableExtra<Action>(WINDOW)
                    callback(defaultAction, intentAction, windowAction)
                }
            }
            return this
        }

        fun getActivityInfo(defaultAction: Action, intentAction: Action, windowAction: Action): StartActivityInfo {
            val intent = Intent(con, CloseAutoSelectActivity::class.java).apply {
                putExtra(DEFAULT, defaultAction as Parcelable)
                putExtra(INTENT, intentAction as Parcelable)
                putExtra(WINDOW, windowAction as Parcelable)
            }

            return StartActivityInfo(intent, listener)
        }
    }

    companion object {
        const val DEFAULT = "0"
        const val INTENT = "1"
        const val WINDOW = "2"
    }
}
