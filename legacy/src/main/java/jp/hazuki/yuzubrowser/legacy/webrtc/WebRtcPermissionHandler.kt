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

package jp.hazuki.yuzubrowser.legacy.webrtc

import android.Manifest
import android.content.Context
import jp.hazuki.asyncpermissions.AsyncPermissions
import jp.hazuki.asyncpermissions.PermissionResult
import jp.hazuki.yuzubrowser.legacy.R
import jp.hazuki.yuzubrowser.legacy.browser.openRequestPermissionSettings

class WebRtcPermissionHandler(private val context: Context, private val asyncPermissions: AsyncPermissions) {

    suspend fun requestPermissions(permissions: List<String>): Boolean {
        return when (permissions.size) {
            0 -> true
            else -> {
                asyncPermissions.request(*permissions.toTypedArray()).let {
                    handlePermissionResult(it)
                }
            }
        }
    }

    private suspend fun handlePermissionResult(result: PermissionResult): Boolean {
        return when (result) {
            is PermissionResult.Granted -> true
            is PermissionResult.Denied -> false
            is PermissionResult.ShouldShowRationale -> {
                result.proceed().let {
                    handlePermissionResult(it)
                }
            }
            is PermissionResult.NeverAskAgain -> {
                context.openRequestPermissionSettings(context.getRequestRtcToastMessage(result.permissions))
                false
            }
        }
    }

    private fun Context.getRequestRtcToastMessage(resources: List<String>): String {
        val builder = StringBuilder()
        var next = false
        resources.forEach {
            if (next) builder.append(',')
            when (it) {
                Manifest.permission.RECORD_AUDIO -> builder.append(getString(R.string.mic))
                Manifest.permission.CAMERA -> builder.append(getString(R.string.camera))
            }
            next = true
        }
        return getString(R.string.permission_request_toast, builder.toString())
    }
}