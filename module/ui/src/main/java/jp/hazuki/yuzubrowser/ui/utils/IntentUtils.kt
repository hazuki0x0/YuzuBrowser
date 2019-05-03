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

package jp.hazuki.yuzubrowser.ui.utils

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import jp.hazuki.yuzubrowser.ui.R


fun createShareWebIntent(url: String, title: String?): Intent {
    val intent = Intent(Intent.ACTION_SEND)
    intent.type = "text/plain"
    intent.putExtra(Intent.EXTRA_TEXT, url)
    if (title != null)
        intent.putExtra(Intent.EXTRA_SUBJECT, title)
    return intent
}

fun shareWeb(context: Context, url: String?, title: String?) {
    if (url == null) return

    val intent = createShareWebIntent(url, title)
    try {
        context.startActivity(Intent.createChooser(intent, context.getText(R.string.share)))
    } catch (e: ActivityNotFoundException) {
        e.printStackTrace()
    }

}
