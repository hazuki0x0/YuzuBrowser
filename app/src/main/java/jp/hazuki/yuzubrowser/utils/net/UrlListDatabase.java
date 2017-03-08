package jp.hazuki.yuzubrowser.utils.net;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

import jp.hazuki.yuzubrowser.tab.MainTabData;
import jp.hazuki.yuzubrowser.tab.TabList;
import jp.hazuki.yuzubrowser.utils.ErrorReport;
import jp.hazuki.yuzubrowser.webkit.TabType;
import jp.hazuki.yuzubrowser.webkit.WebBrowser;

public class UrlListDatabase {
    private final File mFile;

    public UrlListDatabase(File file) {
        mFile = file;
    }

    public void clear() {
        mFile.delete();
    }

    public boolean readList(WebBrowser browser) {
        UrlListData list = getList();
        if (list == null)
            return false;

        for (String url : list.list) {
            browser.openInBackground(url, TabType.DEFAULT);
        }

        int currentNo = list.current;
        int tabCount = browser.getTabCount();
        if (currentNo >= 0 && currentNo < tabCount)
            browser.setCurrentTab(currentNo);
        else if (tabCount != 0)
            browser.setCurrentTab(0);

        return true;
    }

    public static final class UrlListData {
        public int current;
        public List<String> list = new ArrayList<>();
    }

    public UrlListData getList() {
        if (!mFile.exists() || mFile.isDirectory())
            return null;
        try (InputStream is = new BufferedInputStream(new FileInputStream(mFile))) {
            JsonFactory factory = new JsonFactory();
            JsonParser parser = factory.createParser(is);

            if (parser.nextToken() != JsonToken.START_ARRAY) return null;
            if (parser.nextToken() != JsonToken.VALUE_NUMBER_INT) return null;
            UrlListData urlListData = new UrlListData();
            urlListData.current = parser.getIntValue();
            while (parser.nextToken() != JsonToken.END_ARRAY) {
                if (parser.getCurrentToken() != JsonToken.VALUE_STRING) return null;
                urlListData.list.add(parser.getText());
            }

            parser.close();
            return urlListData;
        } catch (IOException e) {
            ErrorReport.printAndWriteLog(e);
        }
        return null;
    }

    public boolean writeList(TabList list) {
        if (list == null || list.isEmpty()) {
            mFile.delete();
            return true;
        }

        try (OutputStream os = new BufferedOutputStream(new FileOutputStream(mFile))) {

            JsonFactory factory = new JsonFactory();
            JsonGenerator generator = factory.createGenerator(os);

            generator.writeStartArray();
            generator.writeNumber(list.getCurrentTabNo());
            for (MainTabData tab : list) {
                generator.writeString(tab.mUrl);
            }
            generator.writeEndArray();

            generator.close();
            return true;
        } catch (IOException e) {
            ErrorReport.printAndWriteLog(e);
        }
        return false;
    }
}
