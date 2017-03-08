package jp.hazuki.yuzubrowser.browser.openable;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.ArrayList;
import java.util.List;

import jp.hazuki.yuzubrowser.webkit.WebBrowser;

public class OpenUrlList implements BrowserOpenable {
    private final List<String> urllist;
    private final int target;

    public OpenUrlList(List<String> url, int target) {
        this.urllist = url;
        this.target = target;
    }

    public OpenUrlList(Parcel source) {
        source.readStringList(this.urllist = new ArrayList<>());
        this.target = source.readInt();
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeStringList(urllist);
        dest.writeInt(target);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Parcelable.Creator<OpenUrlList> CREATOR = new Parcelable.Creator<OpenUrlList>() {
        @Override
        public OpenUrlList createFromParcel(Parcel source) {
            return new OpenUrlList(source);
        }

        @Override
        public OpenUrlList[] newArray(int size) {
            return new OpenUrlList[size];
        }
    };

    @Override
    public void open(WebBrowser browser) {
        for (String url : urllist)
            browser.loadUrl(url, target);
    }
}
