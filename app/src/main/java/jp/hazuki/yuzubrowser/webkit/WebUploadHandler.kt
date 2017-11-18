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

package jp.hazuki.yuzubrowser.webkit

import android.content.Intent
import android.net.Uri
import android.webkit.ValueCallback
import android.webkit.WebChromeClient

class WebUploadHandler {
    private var uploadMsg: ValueCallback<Array<Uri>>? = null

    fun onActivityResult(resultCode: Int, intent: Intent?) {
        uploadMsg?.run {
            onReceiveValue(WebChromeClient.FileChooserParams.parseResult(resultCode, intent))
            uploadMsg = null
        }
    }

    fun destroy() {
        uploadMsg?.run {
            onReceiveValue(null)
            uploadMsg = null
        }
    }


    fun onShowFileChooser(uploadMsg: ValueCallback<Array<Uri>>, params: WebChromeClient.FileChooserParams): Intent {
        this.uploadMsg = uploadMsg
        return params.createIntent()
    }
}
