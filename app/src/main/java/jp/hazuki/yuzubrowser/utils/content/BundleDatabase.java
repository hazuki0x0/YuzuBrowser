package jp.hazuki.yuzubrowser.utils.content;

import android.os.Bundle;
import android.os.Parcel;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import jp.hazuki.yuzubrowser.utils.ErrorReport;
import jp.hazuki.yuzubrowser.utils.IOUtils;
import jp.hazuki.yuzubrowser.webkit.WebBrowser;

public class BundleDatabase {
    private final File mFile;

    public BundleDatabase(File file) {
        mFile = file;
    }

    public void clear() {
        mFile.delete();
    }

    public boolean readList(WebBrowser browser) {
        if (!mFile.exists() || mFile.isDirectory())
            return true;

        try (InputStream is = new BufferedInputStream(new FileInputStream(mFile))) {
            Parcel parcel = Parcel.obtain();
            byte b[] = IOUtils.readByte(is);
            parcel.unmarshall(b, 0, b.length);
            parcel.setDataPosition(0);
            Bundle bundle = parcel.readBundle(Bundle.class.getClassLoader());
            if (bundle == null)
                throw new NullPointerException();
            browser.restoreWebState(bundle);
            parcel.recycle();

            return true;
        } catch (IOException e) {
            ErrorReport.printAndWriteLog(e);
        }
        return false;
    }

    public boolean writeList(WebBrowser browser) {
        Bundle bundle = new Bundle();
        if (!browser.saveWebState(bundle))
            return false;

        try (OutputStream os = new BufferedOutputStream(new FileOutputStream(mFile))) {
            Parcel parcel = Parcel.obtain();
            parcel.writeBundle(bundle);

            os.write(parcel.marshall());
            parcel.recycle();
            return true;
        } catch (IOException e) {
            ErrorReport.printAndWriteLog(e);
        }
        return false;
    }
}
