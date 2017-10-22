package jp.hazuki.yuzubrowser.browser.openable

import android.os.Parcel
import android.os.Parcelable
import jp.hazuki.yuzubrowser.browser.BrowserController
import java.util.*

class OpenUrlList : BrowserOpenable {
    private val urlList: List<String>
    private val target: Int

    constructor(url: List<String>, target: Int) {
        this.urlList = url
        this.target = target
    }

    constructor(source: Parcel) {
        urlList = ArrayList()
        source.readStringList(urlList)
        this.target = source.readInt()
    }

    override fun writeToParcel(dest: Parcel, flags: Int) {
        dest.writeStringList(urlList)
        dest.writeInt(target)
    }

    override fun describeContents(): Int {
        return 0
    }

    override fun open(controller: BrowserController) {
        for (url in urlList)
            controller.loadUrl(url, target)
    }

    companion object {

        @JvmField
        val CREATOR: Parcelable.Creator<OpenUrlList> = object : Parcelable.Creator<OpenUrlList> {
            override fun createFromParcel(source: Parcel): OpenUrlList {
                return OpenUrlList(source)
            }

            override fun newArray(size: Int): Array<OpenUrlList?> {
                return arrayOfNulls(size)
            }
        }
    }
}
