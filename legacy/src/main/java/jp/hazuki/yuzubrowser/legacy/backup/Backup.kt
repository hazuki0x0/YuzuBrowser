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

package jp.hazuki.yuzubrowser.legacy.backup

import android.content.Context
import android.net.Uri
import jp.hazuki.yuzubrowser.core.utility.extensions.forEach
import org.xmlpull.v1.XmlPullParserException
import java.io.File
import java.io.IOException
import java.util.zip.ZipEntry
import java.util.zip.ZipInputStream
import java.util.zip.ZipOutputStream

fun BackupManager.backup(context: Context, uri: Uri): Boolean {
    try {
        val os = context.contentResolver.openOutputStream(uri) ?: return false
        ZipOutputStream(os).use { zip ->
            val rootUri = root.toURI()

            backUpFiles.forEach { file ->
                val name = rootUri.relativize(file.toURI()).toString()
                zip.putNextEntry(ZipEntry(name))
                file.inputStream().use {
                    it.copyTo(zip)
                }
            }
        }
        return true
    } catch (e: IOException) {
        return false
    }
}

fun BackupManager.restore(context: Context, uri: Uri): Boolean {
    try {
        val rootPath = root.canonicalPath
        val ins = context.contentResolver.openInputStream(uri) ?: return false
        ZipInputStream(ins).use { zip ->
            cleanFiles()
            zip.forEach {
                val file = File(rootPath, it.name)
                if (!file.canonicalPath.startsWith(rootPath)) {
                    throw IOException("This file is not put in tmp folder. to:" + file.absolutePath)
                }
                if ("main_preference.xml".equals(file.name, ignoreCase = true)) {
                    val parser = PrefXmlParser(context, "main_preference")
                    try {
                        parser.load(zip)
                    } catch (e: XmlPullParserException) {
                        e.printStackTrace()
                    }
                } else {
                    if (it.isDirectory) {
                        if (file.exists()) {
                            file.deleteRecursively()
                        }
                        file.mkdir()
                    } else {
                        if (file.exists()) {
                            file.deleteRecursively()
                        } else if (!file.parentFile!!.exists()) {
                            file.parentFile!!.mkdirs()
                        }
                        file.outputStream().use { os ->
                            zip.copyTo(os)
                        }
                    }
                }
            }
        }
        return true
    } catch (e: IOException) {
        return false
    }
}
