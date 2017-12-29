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

package jp.hazuki.yuzubrowser.utils

import android.content.Context
import android.os.Environment


// https://stackoverflow.com/a/40205116
fun Context.getExternalStorageDirectories(): Array<String> {

    val results = ArrayList<String>()

    //Method
    val externalDirs = getExternalFilesDirs(null)

    for (file in externalDirs) {
        val path = file.path.split("/Android")[0]

        if (Environment.isExternalStorageRemovable(file)) {
            results.add(path)
        }
    }

    if (results.isEmpty()) { //Method 2 for all versions
        // better variation of: http://stackoverflow.com/a/40123073/5002496
        var output = ""
        try {
            val process = ProcessBuilder().command("mount | grep /dev/block/vold")
                    .redirectErrorStream(true).start()
            process.waitFor()
            process.inputStream.use { stream ->
                val buffer = ByteArray(1024)
                val builder = StringBuilder()
                while (stream.read(buffer) != -1) {
                    builder.append(buffer)
                }
                output = builder.toString()
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }

        if (!output.trim { it <= ' ' }.isEmpty()) {
            val devicePoints = output.split("\n".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
            devicePoints.mapTo(results) { it.split(" ".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()[2] }
        }
    }

    //Below few lines is to remove paths which may not be external memory card, like OTG (feel free to comment them out)
//    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
//        for (i in results.lastIndex downTo 0) {
//            if (!results[i].toLowerCase().matches(".*[0-9a-f]{4}[-][0-9a-f]{4}".toRegex())) {
//                Logger.d("storage", results[i] + " might not be ext SD card")
//                results.removeAt(i)
//            }
//        }
//    } else {
//        for (i in results.lastIndex downTo 0) {
//            if (!results[i].toLowerCase().contains("ext") && !results[i].toLowerCase().contains("sdcard")) {
//                Logger.d("storage", results[i] + " might not be ext SD card")
//                results.removeAt(i)
//            }
//        }
//    }

    return results.toTypedArray()
}