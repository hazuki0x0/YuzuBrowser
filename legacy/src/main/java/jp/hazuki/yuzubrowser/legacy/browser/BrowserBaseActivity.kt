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

package jp.hazuki.yuzubrowser.legacy.browser

import android.annotation.SuppressLint
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.FragmentManager
import jp.hazuki.yuzubrowser.ui.app.LongPressFixActivity
import java.util.*

@SuppressLint("Registered")
open class BrowserBaseActivity : LongPressFixActivity() {

    private val dialogQueue = ArrayDeque<DialogNode>(4)
    private var isResumed = false

    override fun onResume() {
        super.onResume()
        isResumed = true

        while (dialogQueue.size > 0 && isResumed) {
            dialogQueue.poll()!!.show(supportFragmentManager)
        }
    }

    override fun onPause() {
        super.onPause()
        isResumed = false
    }

    fun showDialog(dialog: DialogFragment, tag: String) {
        if (isResumed) {
            dialog.show(supportFragmentManager, tag)
        } else {
            dialogQueue.addLast(DialogNode(dialog, tag))
        }
    }

    private class DialogNode(val dialog: DialogFragment, val tag: String) {
        fun show(fragmentManager: FragmentManager) = dialog.show(fragmentManager, tag)
    }
}
