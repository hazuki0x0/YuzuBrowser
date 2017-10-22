package jp.hazuki.yuzubrowser.browser.openable

import android.os.Parcel
import android.os.Parcelable

import jp.hazuki.yuzubrowser.browser.BrowserController

class OpenUrl(val url: String, private val target: Int) : BrowserOpenable {

    constructor(source: Parcel) : this(source.readString(), source.readInt())

    override fun writeToParcel(dest: Parcel, flags: Int) {
        dest.writeString(url)
        dest.writeInt(target)
    }

    override fun describeContents(): Int {
        return 0
    }

    override fun open(controller: BrowserController) {
        controller.loadUrl(url, target)
    }

    companion object {
        @JvmField
        val CREATOR: Parcelable.Creator<OpenUrl> = object : Parcelable.Creator<OpenUrl> {
            override fun createFromParcel(source: Parcel): OpenUrl {
                return OpenUrl(source)
            }

            override fun newArray(size: Int): Array<OpenUrl?> {
                return arrayOfNulls(size)
            }
        }
    }
}
