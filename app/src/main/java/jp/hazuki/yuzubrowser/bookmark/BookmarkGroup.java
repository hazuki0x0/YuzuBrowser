package jp.hazuki.yuzubrowser.bookmark;

import android.text.TextUtils;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;

public class BookmarkGroup extends BookmarkItem {
    protected static final String COLUMN_NAME_LIST = "2";
    public static final int BOOKMARK_ITEM_ID = 3;

    public final ArrayList<String> list;

    public BookmarkGroup(String title) {
        super(title);
        this.list = new ArrayList<>();
    }

    public BookmarkGroup(String title, Collection<String> list) {
        super(title);
        this.list = new ArrayList<>(list.size());
        addAll(list);
    }

    public BookmarkGroup(String title, String[] list) {
        super(title);
        this.list = new ArrayList<>(list.length);
        addAll(list);
    }

    public void addAll(Collection<String> addlist) {
        for (String url : addlist) {
            url = url.trim();
            if (!TextUtils.isEmpty(url))
                list.add(url);
        }
    }

    public void addAll(String[] addlist) {
        for (String url : addlist) {
            url = url.trim();
            if (!TextUtils.isEmpty(url))
                list.add(url);
        }
    }

    @Override
    protected int getId() {
        return BOOKMARK_ITEM_ID;
    }

    @Override
    protected boolean writeMain(JsonGenerator generator) throws IOException {
        generator.writeArrayFieldStart(COLUMN_NAME_LIST);
        for (String item : list) {
            generator.writeString(item);
        }
        generator.writeEndArray();
        return true;
    }

    @Override
    protected boolean readMain(JsonParser parser) throws IOException {
        parser.nextToken();
        if (!COLUMN_NAME_LIST.equals(parser.getCurrentName())) return false;

        if (parser.nextToken() != JsonToken.START_ARRAY) return false;
        while (parser.nextToken() != JsonToken.END_ARRAY) {
            if (parser.getCurrentToken() != JsonToken.VALUE_STRING) return false;
            list.add(parser.getText());
        }
        return true;
    }
}
