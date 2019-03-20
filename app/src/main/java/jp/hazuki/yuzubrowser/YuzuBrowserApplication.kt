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

package jp.hazuki.yuzubrowser

import android.content.Context
import android.webkit.WebView
import androidx.appcompat.app.AppCompatDelegate
import androidx.fragment.app.Fragment
import com.crashlytics.android.Crashlytics
import com.crashlytics.android.answers.Answers
import com.crashlytics.android.core.CrashlyticsCore
import com.squareup.moshi.Moshi
import dagger.android.AndroidInjector
import dagger.android.DaggerApplication
import dagger.android.DispatchingAndroidInjector
import dagger.android.support.HasSupportFragmentInjector
import io.fabric.sdk.android.Fabric
import jp.hazuki.yuzubrowser.core.utility.log.Logger
import jp.hazuki.yuzubrowser.di.DaggerAppComponent
import jp.hazuki.yuzubrowser.download.registerDownloadNotification
import jp.hazuki.yuzubrowser.legacy.settings.data.AppData
import jp.hazuki.yuzubrowser.legacy.utils.CrashlyticsUtils
import jp.hazuki.yuzubrowser.provider.ProviderManager
import jp.hazuki.yuzubrowser.ui.BrowserApplication
import javax.inject.Inject


class YuzuBrowserApplication : DaggerApplication(), BrowserApplication, HasSupportFragmentInjector {

    override val applicationId = BuildConfig.APPLICATION_ID
    override val permissionAppSignature = PERMISSION_MYAPP_SIGNATURE
    override val browserState = BrowserStateImpl()
    override val providerManager = ProviderManager()
    override val context: Context
        get() = this
    @Inject
    lateinit var dispatchingAndroidFragmentInjector: DispatchingAndroidInjector<Fragment>
    @Inject
    override lateinit var moshi: Moshi

    override fun onCreate() {
        super.onCreate()
        val crashlytics = Crashlytics.Builder()
                .core(CrashlyticsCore.Builder().disabled(BuildConfig.DEBUG).build())
                .build()
        Fabric.with(this, crashlytics, Answers())
        CrashlyticsUtils.setChromeVersion(this)
        CrashlyticsUtils.setWebViewMode()
        registerDownloadNotification()

        Logger.d(TAG, "onCreate()")
        browserState.isNeedLoad = false
        ErrorReportServer.initialize(this)
        AppData.load(this, moshi)
        ErrorReportServer.setDetailedLog(AppData.detailed_log.get())
        if (AppData.slow_rendering.get()) {
            WebView.enableSlowWholeDocumentDraw()
        }
        Logger.isDebug = BuildConfig.DEBUG
    }

    override fun supportFragmentInjector(): AndroidInjector<Fragment> {
        return dispatchingAndroidFragmentInjector
    }

    override fun applicationInjector(): AndroidInjector<out DaggerApplication> {
        val appComponent = DaggerAppComponent.builder().create(this)
        appComponent.inject(this)
        return appComponent
    }

    companion object {
        private const val TAG = "YuzuBrowserApplication"
        const val PERMISSION_MYAPP_SIGNATURE = BuildConfig.APPLICATION_ID + ".permission.myapp.signature"
        lateinit var instance: YuzuBrowserApplication

        init {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
        }
    }
}
