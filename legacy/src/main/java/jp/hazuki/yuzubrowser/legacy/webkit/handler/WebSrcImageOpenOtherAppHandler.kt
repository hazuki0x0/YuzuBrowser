package jp.hazuki.yuzubrowser.legacy.webkit.handler

import android.content.Context
import jp.hazuki.yuzubrowser.legacy.R
import jp.hazuki.yuzubrowser.legacy.utils.PackageUtils
import java.lang.ref.WeakReference

class WebSrcImageOpenOtherAppHandler(activity: Context) : WebSrcImageHandler() {
    private val mReference: WeakReference<Context> = WeakReference(activity)

    override fun handleUrl(url: String) {
        mReference.get()?.run {
            startActivity(PackageUtils.createChooser(this, url, getText(R.string.open_other_app)))
        }
    }
}
