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

package jp.hazuki.yuzubrowser.legacy.theme

import android.content.Context
import android.net.Uri
import jp.hazuki.yuzubrowser.core.utility.extensions.forEach
import jp.hazuki.yuzubrowser.core.utility.log.ErrorReport
import jp.hazuki.yuzubrowser.core.utility.utils.FileUtils
import jp.hazuki.yuzubrowser.core.utility.utils.externalUserDirectory
import jp.hazuki.yuzubrowser.legacy.R
import jp.hazuki.yuzubrowser.ui.theme.ThemeManifest
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.util.zip.ZipInputStream

internal fun importTheme(context: Context, uri: Uri):Result {
    val root = File(externalUserDirectory, "theme")
    val tmpFolder = File(root, System.currentTimeMillis().toString())
    val tmpFolderPath = tmpFolder.canonicalPath

    try {
        ZipInputStream(context.contentResolver.openInputStream(uri)).use { zis ->
            val buffer = ByteArray(8192)

            zis.forEach { entry ->
                val file = File(tmpFolder, entry.name)
                if (!file.canonicalPath.startsWith(tmpFolderPath)) {
                    throw IOException("This file is not put in tmp folder. to:${file.canonicalPath}")
                }
                if (entry.isDirectory) {
                    if (file.exists()) {
                        FileUtils.deleteFile(file)
                    }
                    if (!file.mkdirs()) {
                        FileUtils.deleteFile(tmpFolder)
                        return Result(false, context.getString(R.string.cant_create_folder))
                    }
                } else {
                    if (file.exists()) {
                        FileUtils.deleteFile(file)
                    } else if (!file.parentFile.exists()) {
                        if (!file.parentFile.mkdirs()) {
                            FileUtils.deleteFile(tmpFolder)
                            return Result(false, context.getString(R.string.cant_create_folder))
                        }
                    }
                    FileOutputStream(file).use { os ->
                        var len: Int
                        while (zis.read(buffer).also { len = it } > 0) {
                            os.write(buffer, 0, len)
                        }
                    }
                }
            }
        }
    } catch (e: IOException) {
        e.printStackTrace()
        FileUtils.deleteFile(tmpFolder)
        return Result(false, context.getString(R.string.theme_unknown_error))
    }

    val manifestFile = File(tmpFolder, ThemeManifest.MANIFEST)
    val manifest: ThemeManifest?

    try {
        manifest = ThemeManifest.decodeManifest(manifestFile)
        if (manifest == null) {
            FileUtils.deleteFile(tmpFolder)
            return Result(false, context.getString(R.string.theme_manifest_not_found))
        }
    } catch (e: ThemeManifest.IllegalManifestException) {
        FileUtils.deleteFile(tmpFolder)
        val text = when (e.errorType) {
            0 -> R.string.theme_unknown_error
            1 -> R.string.theme_broken_manifest
            2 -> R.string.theme_unknown_version
            else -> R.string.theme_unknown_error
        }
        return Result(false, context.getString(text))
    }


    val name = FileUtils.replaceProhibitionWord(manifest.name)
    if (name.isEmpty()) {
        FileUtils.deleteFile(tmpFolder)
        return Result(false, context.getString(R.string.theme_broken_manifest))
    }

    val theme = File(root, name)

    if (theme.exists()) {
        if (theme.isDirectory) {
            val destManifest = File(theme, ThemeManifest.MANIFEST)
            if (destManifest.exists()) {
                try {
                    val dest = ThemeManifest.decodeManifest(destManifest)
                    if (dest != null) {
                        if (dest.id == manifest.id) {
                            if (dest.version == manifest.version) {
                                FileUtils.deleteFile(tmpFolder)
                                return Result(false, context.getString(R.string.theme_installed_version))
                            }
                        } else {
                            FileUtils.deleteFile(tmpFolder)
                            return Result(false, context.getString(R.string.theme_same_name, manifest.name))
                        }
                    }
                } catch (e: ThemeManifest.IllegalManifestException) {
                    ErrorReport.printAndWriteLog(e)
                }
            }
        }
        FileUtils.deleteFile(theme)
    }

    if (tmpFolder.renameTo(theme)) {
        return Result(true, manifest.name)
    }

    FileUtils.deleteFile(tmpFolder)
    return Result(false, context.getString(R.string.theme_unknown_error))
}

class Result constructor(val isSuccess: Boolean, val message: String)
