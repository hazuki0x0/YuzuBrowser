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

package jp.hazuki.yuzubrowser.legacy.webkit.handler

import androidx.appcompat.app.AppCompatActivity
import jp.hazuki.yuzubrowser.adblock.ui.original.AddAdBlockDialog
import java.lang.ref.WeakReference

class WebSrcImageWhiteListHandler(activity: AppCompatActivity) : WebSrcImageHandler() {
    private val mReference: WeakReference<AppCompatActivity> = WeakReference(activity)

    override fun handleUrl(url: String) {
        mReference.get()?.run {
            AddAdBlockDialog.addWhiteListInstance(url)
                    .show(supportFragmentManager, "add white")
        }
    }
}