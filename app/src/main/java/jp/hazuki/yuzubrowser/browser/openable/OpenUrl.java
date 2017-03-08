package jp.hazuki.yuzubrowser.browser.openable;

import android.os.Parcel;
import android.os.Parcelable;

import jp.hazuki.yuzubrowser.webkit.WebBrowser;

public class OpenUrl implements BrowserOpenable {
    private final String url;
    private final int target;

    public OpenUrl(String url, int target) {
        this.url = url;
        this.target = target;
    }

    public OpenUrl(Parcel source) {
        this.url = source.readString();
        this.target = source.readInt();
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(url);
        dest.writeInt(target);
    }

    public String getUrl() {
        return url;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Parcelable.Creator<OpenUrl> CREATOR = new Parcelable.Creator<OpenUrl>() {
        @Override
        public OpenUrl createFromParcel(Parcel source) {
            return new OpenUrl(source);
        }

        @Override
        public OpenUrl[] newArray(int size) {
            return new OpenUrl[size];
        }
    };

    @Override
    public void open(WebBrowser browser) {
        browser.loadUrl(url, target);
    }
}
