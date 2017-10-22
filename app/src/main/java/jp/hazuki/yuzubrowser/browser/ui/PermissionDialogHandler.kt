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

package jp.hazuki.yuzubrowser.browser.ui

import android.os.Message
import android.support.v7.app.AppCompatActivity
import jp.hazuki.yuzubrowser.utils.PermissionUtils
import jp.hazuki.yuzubrowser.utils.handler.PauseHandler
import java.lang.ref.WeakReference

class PermissionDialogHandler(activity: AppCompatActivity) : PauseHandler() {
    private val activityRef = WeakReference<AppCompatActivity>(activity)

    companion object {
        const val SHOW_DIALOG = 1
    }

    override fun storeMessage(message: Message): Boolean {
        return true
    }

    override fun processMessage(message: Message) {
        when (message.what) {
            SHOW_DIALOG -> {
                val activity = activityRef.get()
                if (activity != null && !PermissionUtils.checkWriteStorage(activity)) {
                    PermissionDialog().show(activity.supportFragmentManager, "permission")
                }
            }
        }
    }
}