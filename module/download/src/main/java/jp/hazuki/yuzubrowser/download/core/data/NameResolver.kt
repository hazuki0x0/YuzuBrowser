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

package jp.hazuki.yuzubrowser.download.core.data

import android.os.Parcelable
import androidx.documentfile.provider.DocumentFile
import jp.hazuki.yuzubrowser.download.core.utils.guessDownloadFileName
import kotlinx.android.parcel.Parcelize

@Parcelize
internal class NameResolver(
    private val url: String,
    private val contentDisposition: String?,
    val mimeType: String?,
    val contentLength: Long
) : Parcelable {

    fun resolveName(downloadDir: DocumentFile): String {
        return guessDownloadFileName(downloadDir, url, contentDisposition, mimeType, null)
    }
}
