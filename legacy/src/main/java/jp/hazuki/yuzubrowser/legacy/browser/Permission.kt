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

package jp.hazuki.yuzubrowser.legacy.browser

import android.Manifest
import android.app.Activity
import android.app.AlertDialog
import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import androidx.appcompat.app.AppCompatActivity
import jp.hazuki.asyncpermissions.AsyncPermissions
import jp.hazuki.asyncpermissions.PermissionResult
import jp.hazuki.yuzubrowser.legacy.R
import jp.hazuki.yuzubrowser.ui.settings.AppPrefs
import org.jetbrains.anko.longToast
import java.lang.ref.SoftReference

fun Activity.checkBrowserPermission(): Boolean {
    if (checkNeed()) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            return false
        }
    } else {
        if (!checkStoragePermission()) {
            return false
        }
    }
    return true
}

suspend fun AppCompatActivity.requestBrowserPermissions(asyncPermissions: AsyncPermissions) {
    val requests = if (checkNeed()) {
        setNoNeed(true)
        arrayOf(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION,
                Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE)
    } else {
        if (supportFragmentManager.findFragmentByTag("permission") != null) return
        arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE)
    }

    handleResult(asyncPermissions, asyncPermissions.request(*requests), true, true)
}

fun Activity.checkStoragePermission(): Boolean {
    return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
        checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED
    } else {
        true
    }
}

suspend fun AppCompatActivity.requestStoragePermission(asyncPermissions: AsyncPermissions) {
    asyncPermissions.request(Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE).let {
        handleResult(asyncPermissions, it, true)
    }
}

fun Activity.checkLocationPermission(): Boolean {
    return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
        checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
    } else {
        true
    }
}

suspend fun AppCompatActivity.requestLocationPermission(asyncPermissions: AsyncPermissions) {
    asyncPermissions.request(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION).let {
        handleResult(asyncPermissions, it, location = true)
    }
}

fun Context.openRequestPermissionSettings(text: CharSequence) {
    startActivity(Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
            .apply { data = Uri.fromParts("package", packageName, null) })
    longToast(text)
}

private suspend fun AppCompatActivity.handleResult(asyncPermissions: AsyncPermissions, permissionResult: PermissionResult, storage: Boolean = false, location: Boolean = false) {
    when (permissionResult) {
        is PermissionResult.Granted -> Unit
        is PermissionResult.Denied -> {
            if (location && !checkLocationPermission() && AppPrefs.web_geolocation.get()) {
                AppPrefs.web_geolocation.set(false)
                AppPrefs.commit(this, AppPrefs.web_geolocation)
            }
            if (storage && !checkStoragePermission()) {
                requestStoragePermission(asyncPermissions)
            }
        }
        is PermissionResult.ShouldShowRationale -> permissionResult.proceed().let { handleResult(asyncPermissions, it, true) }
        is PermissionResult.NeverAskAgain -> {
            if (storage && !checkStoragePermission() && supportFragmentManager.findFragmentByTag("permission") == null) {
                PermissionDialog().show(supportFragmentManager, "permission")
            }
        }
    }
}

class PermissionDialog : androidx.fragment.app.DialogFragment() {
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val activity = activity ?: throw IllegalStateException()

        val builder = AlertDialog.Builder(activity)
        builder.setTitle(R.string.permission_probrem)
                .setMessage(R.string.confirm_permission_storage_app)
                .setPositiveButton(android.R.string.ok) { _, _ -> activity.openRequestPermissionSettings(getString(R.string.request_permission_storage_setting)) }
                .setNegativeButton(android.R.string.no) { _, _ -> activity.finish() }
        isCancelable = false
        return builder.create()
    }
}

private const val NO_NEED = "no_need"

private fun Context.setNoNeed(noNeed: Boolean) {
    if (checkNeed() == noNeed)
        pref.edit().putBoolean(NO_NEED, noNeed).apply()
}

private fun Context.checkNeed() = !pref.getBoolean(NO_NEED, false)

private const val PREF = "permission"
private var preferences: SoftReference<SharedPreferences>? = null

private val Context.pref: SharedPreferences
    get() {
        var cache = preferences?.get()
        if (cache != null) return cache

        cache = getSharedPreferences(PREF, Context.MODE_PRIVATE)
        preferences = SoftReference(cache)
        return cache
    }
