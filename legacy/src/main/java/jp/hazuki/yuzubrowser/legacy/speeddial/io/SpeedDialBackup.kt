/*
 * Copyright (C) 2017-2021 Hazuki
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

package jp.hazuki.yuzubrowser.legacy.speeddial.io

import android.content.Context
import android.net.Uri
import jp.hazuki.yuzubrowser.core.utility.extensions.forEach
import jp.hazuki.yuzubrowser.legacy.speeddial.SpeedDialHtml
import jp.hazuki.yuzubrowser.legacy.speeddial.SpeedDialManager
import java.io.IOException
import java.util.zip.ZipEntry
import java.util.zip.ZipInputStream
import java.util.zip.ZipOutputStream

fun Context.backupSpeedDial(uri: Uri): Boolean {
    val db = getDatabasePath(SpeedDialManager.DB_NAME)

    try {
        val out = contentResolver.openOutputStream(uri) ?: return false
        ZipOutputStream(out).use { zip ->
            db.inputStream().use { ins ->
                zip.putNextEntry(ZipEntry(SpeedDialManager.DB_NAME))
                ins.copyTo(zip)
                return true
            }
        }
    } catch (e: IOException) {
        e.printStackTrace()
    }
    return false
}

fun Context.restoreSpeedDial(uri: Uri): Boolean {
    val db = getDatabasePath(SpeedDialManager.DB_NAME)

    try {
        val out = contentResolver.openInputStream(uri) ?: return false
        ZipInputStream(out).use { zip ->
            db.outputStream().use { os ->
                zip.forEach {
                    if (SpeedDialManager.DB_NAME == it.name) {
                        zip.copyTo(os)
                    }
                }
                SpeedDialHtml.clearCache(this)
                SpeedDialManager.closeAll()
                return true
            }
        }
    } catch (e: IOException) {
        e.printStackTrace()
    }
    return false
}
