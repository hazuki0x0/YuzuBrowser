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

import android.app.AlertDialog
import android.app.Dialog
import android.os.Bundle
import android.support.v4.app.DialogFragment
import jp.hazuki.yuzubrowser.R
import jp.hazuki.yuzubrowser.utils.PermissionUtils

class PermissionDialog : DialogFragment() {
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val builder = AlertDialog.Builder(activity)
        builder.setTitle(R.string.permission_probrem)
                .setMessage(R.string.confirm_permission_storage_app)
                .setPositiveButton(android.R.string.ok) { _, _ -> PermissionUtils.openRequestPermissionSettings(activity, getString(R.string.request_permission_storage_setting)) }
                .setNegativeButton(android.R.string.no) { _, _ -> activity.finish() }
        isCancelable = false
        return builder.create()
    }
}