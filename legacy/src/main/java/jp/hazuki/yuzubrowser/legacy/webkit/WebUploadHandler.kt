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

package jp.hazuki.yuzubrowser.legacy.webkit

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.webkit.ValueCallback
import android.webkit.WebChromeClient
import jp.hazuki.yuzubrowser.core.utility.utils.getMimeType

class WebUploadHandler {
    private var uploadMsg: ValueCallback<Array<Uri>?>? = null

    fun onActivityResult(resultCode: Int, intent: Intent?) {
        uploadMsg?.run {
            onReceiveValue(parseChooserIntent(resultCode, intent))
            uploadMsg = null
        }
    }

    fun destroy() {
        uploadMsg?.run {
            onReceiveValue(null)
            uploadMsg = null
        }
    }


    fun onShowFileChooser(uploadMsg: ValueCallback<Array<Uri>?>, params: WebChromeClient.FileChooserParams): Intent {
        this.uploadMsg = uploadMsg
        return params.createChooserIntent()
    }

    private fun WebChromeClient.FileChooserParams.createChooserIntent(): Intent {
        var acceptTypes = acceptTypes.toList()
        val multiple = mode == WebChromeClient.FileChooserParams.MODE_OPEN_MULTIPLE

        if (acceptTypes.isNotEmpty()) {
            acceptTypes = acceptTypes.filter { it.isNotEmpty() }

            // Convert extension to MimeType
            if (acceptTypes.any { !it.contains('/') }) {
                acceptTypes = acceptTypes.map { if (it.contains('/')) it else getMimeType(it) }
            }
        }

        return Intent(Intent.ACTION_GET_CONTENT).apply {
            addCategory(Intent.CATEGORY_OPENABLE)
            if (multiple) {
                putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true)
            }
            if (acceptTypes.isNotEmpty()) {
                putExtra(Intent.EXTRA_MIME_TYPES, acceptTypes.toTypedArray())
            }
            type = "*/*"
        }
    }

    private fun parseChooserIntent(resultCode: Int, intent: Intent?): Array<Uri>? {
        if (resultCode != Activity.RESULT_OK || intent == null) return null

        val clip = intent.clipData
        if (clip != null) {
            val uris = mutableListOf<Uri>()
            for (i in 0 until clip.itemCount) {
                uris.add(clip.getItemAt(i).uri)
            }
            return uris.toTypedArray()
        }
        val data = intent.data
        if (data != null) {
            return arrayOf(data)
        }
        return null
    }
}
