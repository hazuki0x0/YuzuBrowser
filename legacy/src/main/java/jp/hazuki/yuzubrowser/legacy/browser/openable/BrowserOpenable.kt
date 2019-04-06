package jp.hazuki.yuzubrowser.legacy.browser.openable

import android.os.Parcelable

import jp.hazuki.yuzubrowser.legacy.browser.BrowserController

interface BrowserOpenable : Parcelable {
    fun open(controller: BrowserController)
}
