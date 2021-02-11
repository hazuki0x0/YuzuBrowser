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

package jp.hazuki.yuzubrowser.core.utility.utils

import android.content.ContentResolver
import android.content.ContentValues
import android.graphics.Bitmap
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import java.io.File
import java.io.IOException

private const val DIRECTORY_NAME = "YuzuBrowser"

fun ContentResolver.savePictureAsPng(fileName: String, bitmap: Bitmap): Boolean {
    return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
        savePictureAsPngQ(fileName, bitmap)
    } else {
        savePictureAsPngClassic(fileName, bitmap)
    }
}

private fun ContentResolver.savePictureAsPngQ(fileName: String, bitmap: Bitmap): Boolean {
    val values = createContentValues(fileName).apply {
        put(MediaStore.Images.Media.IS_PENDING, 1)
        put(MediaStore.Images.Media.RELATIVE_PATH, "${Environment.DIRECTORY_PICTURES}/$DIRECTORY_NAME")
    }

    val uri = insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values) ?: return false

    return try {
        openOutputStream(uri).use {
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, it)
        }

        values.apply {
            clear()
            put(MediaStore.Images.Media.IS_PENDING, 0)
        }
        update(uri, values, null, null)
        true
    } catch (e: IOException) {
        false
    }
}

@Suppress("DEPRECATION")
private fun ContentResolver.savePictureAsPngClassic(fileName: String, bitmap: Bitmap): Boolean {
    val picturesDirectory = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES)
    val directory = File(picturesDirectory, DIRECTORY_NAME)

    if (!directory.exists()) directory.mkdirs()

    val file = File(directory, fileName)

    return try {
        file.outputStream().use {
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, it)
        }

        val values = createContentValues(fileName).apply {
            put(MediaStore.Images.Media.DATA, file.absolutePath)
        }

        insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values) != null
    } catch (e: IOException) {
        false
    }

}

private fun createContentValues(fileName: String) = ContentValues().apply {
    put(MediaStore.Images.Media.DISPLAY_NAME, fileName)
    put(MediaStore.Images.Media.MIME_TYPE, "image/png")
}
