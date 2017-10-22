package jp.hazuki.yuzubrowser.browser.openable

import android.os.Parcelable

import jp.hazuki.yuzubrowser.browser.BrowserController

interface BrowserOpenable : Parcelable {
    fun open(controller: BrowserController)
}
