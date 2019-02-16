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

package jp.hazuki.yuzubrowser.legacy.action.manager

import android.view.View
import android.webkit.WebView
import jp.hazuki.yuzubrowser.legacy.R
import jp.hazuki.yuzubrowser.legacy.action.Action
import jp.hazuki.yuzubrowser.legacy.action.ActionNameArray
import jp.hazuki.yuzubrowser.legacy.action.SingleAction
import jp.hazuki.yuzubrowser.webview.CustomWebView

interface ActionController {
    fun checkAndRun(action: Action, target: TargetInfo?): Boolean {
        return if (target is HitTestResultTargetInfo) {
            run(action, target)
        } else {
            run(action, target)
        }
    }

    fun run(action: SingleAction, target: TargetInfo? = null, button: View? = null): Boolean

    fun run(list: Action, target: TargetInfo? = null, view: View? = null): Boolean {
        if (list.isEmpty()) return false
        for (action in list) {
            run(action, target, view)
        }
        return true
    }

    fun run(action: SingleAction, target: HitTestResultTargetInfo): Boolean

    fun run(list: Action, target: HitTestResultTargetInfo): Boolean {
        if (list.isEmpty()) return false
        for (action in list) {
            run(action, target)
        }
        return true
    }

    open class TargetInfo(var target: Int = -1)

    class HitTestResultTargetInfo(val webView: CustomWebView, val result: WebView.HitTestResult) : TargetInfo() {
        private var mActionNameArray: ActionNameArray? = null

        val actionNameArray: ActionNameArray
            get() {
                val context = webView.view.context

                if (mActionNameArray == null) {
                    when (result.type) {
                        WebView.HitTestResult.SRC_ANCHOR_TYPE -> mActionNameArray = ActionNameArray(context, R.array.pref_lpress_link_list, R.array.pref_lpress_link_values)
                        WebView.HitTestResult.IMAGE_TYPE -> mActionNameArray = ActionNameArray(context, R.array.pref_lpress_image_list, R.array.pref_lpress_image_values)
                        WebView.HitTestResult.SRC_IMAGE_ANCHOR_TYPE -> mActionNameArray = ActionNameArray(context, R.array.pref_lpress_linkimage_list, R.array.pref_lpress_linkimage_values)
                    }
                }
                return mActionNameArray!!
            }
    }
}