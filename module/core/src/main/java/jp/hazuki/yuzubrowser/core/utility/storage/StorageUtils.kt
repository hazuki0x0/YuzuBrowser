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

package jp.hazuki.yuzubrowser.core.utility.storage

import android.content.Context
import android.os.Build
import android.os.Environment
import android.os.storage.StorageManager
import android.os.storage.StorageVolume
import androidx.annotation.RequiresApi
import java.io.File
import java.lang.reflect.InvocationTargetException


fun Context.getStorageList(): List<StorageInfo> {
    return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
        getStorageListApi24()
    } else {
        getStorageListApi21()
    }
}

private fun Context.getStorageListApi21(): List<StorageInfo> {
    val manager = getSystemService(Context.STORAGE_SERVICE) as StorageManager
    val storageList = mutableListOf<StorageInfo>()
    try {
        val getVolumeList = manager.javaClass.getDeclaredMethod("getVolumeList")
        @Suppress("UNCHECKED_CAST")
        val volumeList = getVolumeList.invoke(manager) as Array<Any>
        for (volume in volumeList) {
            val getPath = volume.javaClass.getDeclaredMethod("getPath")
            val getUuid = volume.javaClass.getDeclaredMethod("getUuid")
            val isPrimary = volume.javaClass.getDeclaredMethod("isPrimary")
            val isRemovable = volume.javaClass.getDeclaredMethod("isRemovable")
            val path = getPath.invoke(volume) as String
            val primary = isPrimary.invoke(volume) as Boolean
            val uuid = if (primary) "primary" else getUuid.invoke(volume) as? String ?: ""
            val removable = isRemovable.invoke(volume) as Boolean
            storageList.add(StorageInfo(primary, removable, uuid, path))
        }
    } catch (e: ClassCastException) {
        e.printStackTrace()
    } catch (e: NoSuchMethodException) {
        e.printStackTrace()
    } catch (e: InvocationTargetException) {
        e.printStackTrace()
    } catch (e: IllegalAccessException) {
        e.printStackTrace()
    }
    return storageList
}

@RequiresApi(Build.VERSION_CODES.N)
private fun Context.getStorageListApi24(): List<StorageInfo> {
    val storages = getStorages()
    val storageList = mutableListOf<StorageInfo>()
    storages.forEach {
        val path = it.resolvePath()
        if (path != null) {
            val isPrimary = it.isPrimary
            val uuid = if (isPrimary) "primary" else it.uuid ?: ""
            storageList.add(StorageInfo(isPrimary, it.isRemovable, uuid, path))
        }
    }
    return storageList
}

@RequiresApi(Build.VERSION_CODES.N)
fun Context.getStorages(): List<StorageVolume> {
    val manager = getSystemService(Context.STORAGE_SERVICE) as StorageManager
    return manager.storageVolumes
}

@RequiresApi(Build.VERSION_CODES.N)
fun StorageVolume.resolvePath(): String? {
    if (isPrimary) {
        return Environment.getExternalStorageDirectory().absolutePath
    }

    try {
        val getPath = javaClass.getDeclaredMethod("getPath")
        return getPath.invoke(this) as String
    } catch (e: ClassCastException) {
        e.printStackTrace()
    } catch (e: NoSuchMethodException) {
        e.printStackTrace()
    } catch (e: InvocationTargetException) {
        e.printStackTrace()
    } catch (e: IllegalAccessException) {
        e.printStackTrace()
    }

    if (File("/storage/$uuid").exists()) {
        return "/storage/$uuid"
    }
    if (File("/mnt/media_rw/$uuid").exists()) {
        return "/mnt/media_rw/$uuid"
    }

    return null
}