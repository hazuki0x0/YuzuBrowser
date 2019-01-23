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

package jp.hazuki.yuzubrowser.legacy.toolbar.sub

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.webkit.GeolocationPermissions
import android.widget.CheckBox
import android.widget.TextView
import jp.hazuki.yuzubrowser.legacy.R
import jp.hazuki.yuzubrowser.legacy.toolbar.SubToolbar
import jp.hazuki.yuzubrowser.ui.theme.ThemeData

open class GeolocationPermissionToolbar(context: Context) : SubToolbar(context), View.OnClickListener {
    private var mOrigin: String? = null
    private var mCallback: GeolocationPermissions.Callback? = null

    init {
        LayoutInflater.from(context).inflate(R.layout.geolocation_alert, this)
    }

    fun onGeolocationPermissionsShowPrompt(origin: String, callback: GeolocationPermissions.Callback) {
        mOrigin = origin
        mCallback = callback
        findViewById<TextView>(R.id.urlTextView).text = origin
        findViewById<View>(R.id.okButton).setOnClickListener(this)
        findViewById<View>(R.id.cancelButton).setOnClickListener(this)
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        mOrigin = null
        mCallback = null
    }

    override fun onClick(v: View) {
        mCallback?.invoke(mOrigin, v.id == R.id.okButton, findViewById<CheckBox>(R.id.rememberCheckBox).isChecked)
        onHideToolbar()
    }

    open fun onHideToolbar() {}

    override fun applyThemeAutomatically(themeData: ThemeData?) {
        /* no theme */
    }
}
