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

package jp.hazuki.yuzubrowser

import android.app.Application
import android.os.Environment
import android.webkit.WebView
import com.crashlytics.android.Crashlytics
import com.crashlytics.android.answers.Answers
import com.crashlytics.android.core.CrashlyticsCore
import io.fabric.sdk.android.Fabric
import jp.hazuki.yuzubrowser.settings.data.AppData
import jp.hazuki.yuzubrowser.utils.AppUtils
import jp.hazuki.yuzubrowser.utils.CrashlyticsUtils
import jp.hazuki.yuzubrowser.utils.ErrorReport
import jp.hazuki.yuzubrowser.utils.Logger
import java.io.File

class BrowserApplication : Application() {
    init {
        instance = this
    }

    override fun onCreate() {
        super.onCreate()
        val crashlytics = Crashlytics.Builder()
                .core(CrashlyticsCore.Builder().disabled(BuildConfig.DEBUG).build())
                .build()
        Fabric.with(this, crashlytics, Answers())
        CrashlyticsUtils.setChromeVersion(this)
        AppUtils.registNotification(this)

        Logger.d(TAG, "onCreate()")
        isNeedLoad = false
        ErrorReport.initialize(this)
        AppData.load(this)
        ErrorReport.setDetailedLog(AppData.detailed_log.get())
        if (AppData.slow_rendering.get()) {
            WebView.enableSlowWholeDocumentDraw()
        }
    }

    companion object {
        private const val TAG = "BrowserApplication"
        const val PERMISSION_MYAPP_SIGNATURE = BuildConfig.APPLICATION_ID + ".permission.myapp.signature"
        lateinit var instance: BrowserApplication
        var isNeedLoad: Boolean = false

        val externalUserDirectory: File
            get() = File(Environment.getExternalStorageDirectory().toString() + File.separator + "YuzuBrowser" + File.separator)
    }
}
