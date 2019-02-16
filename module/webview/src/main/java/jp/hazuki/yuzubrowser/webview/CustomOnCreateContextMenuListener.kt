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

package jp.hazuki.yuzubrowser.webview

import android.view.ContextMenu
import android.view.ContextMenu.ContextMenuInfo
import android.view.View
import android.view.View.OnCreateContextMenuListener

abstract class CustomOnCreateContextMenuListener : OnCreateContextMenuListener {
    abstract fun onCreateContextMenu(menu: ContextMenu, webView: CustomWebView, menuInfo: ContextMenuInfo?)

    override fun onCreateContextMenu(menu: ContextMenu, v: View, menuInfo: ContextMenuInfo?) {
        if (v is CustomWebView)
            onCreateContextMenu(menu, v as CustomWebView, menuInfo)
        else
            throw IllegalArgumentException()
    }
}
