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

import android.annotation.SuppressLint
import android.content.Context
import android.os.Parcelable
import android.webkit.CookieManager
import androidx.documentfile.provider.DocumentFile
import jp.hazuki.yuzubrowser.core.MIME_TYPE_UNKNOWN
import jp.hazuki.yuzubrowser.download.core.utils.*
import kotlinx.android.parcel.Parcelize
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.IOException
import java.util.concurrent.TimeUnit

@SuppressLint("ParcelCreator")
@Parcelize
class MetaData constructor(val name: String, val mineType: String, val size: Long, val resumable: Boolean) : Parcelable {

    companion object {

        operator fun invoke(context: Context, okHttpClient: OkHttpClient, root: DocumentFile, url: String, request: DownloadRequest, resolvedName: String? = null): MetaData {
            if (url.startsWith("http")) {
                try {
                    val httpRequest = Request.Builder()
                            .url(url)
                            .head()
                            .setCookie(CookieManager.getInstance().getCookie(url))
                            .setReferrer(request.referrer)
                            .setUserAgent(context, request.userAgent)
                            .build()

                    val client = okHttpClient.newBuilder()
                            .connectTimeout(1, TimeUnit.SECONDS)
                            .build()

                    val newCall = client.newCall(httpRequest)
                    val response = newCall.execute()

                    var mimeType = response.mimeType
                    if (mimeType.isEmpty()) mimeType = MIME_TYPE_UNKNOWN
                    val name = resolvedName
                            ?: response.getFileName(root, url, mimeType, request.defaultExt)
                    return MetaData(name, mimeType, response.contentLength, response.isResumable)
                } catch (e: IOException) {
                    // Connection error
                }
            }
            return MetaData(resolvedName
                    ?: guessDownloadFileName(root, url, null, null, request.defaultExt), "application/octet-stream", -1, false)
        }

        operator fun invoke(info: DownloadFileInfo): MetaData {
            return MetaData(info.name, info.mimeType, info.size, info.resumable)
        }
    }
}