package jp.hazuki.yuzubrowser.bookmark;

import android.text.TextUtils;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;

import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import jp.hazuki.yuzubrowser.bookmark.util.BookmarkIdGenerator;
import jp.hazuki.yuzubrowser.tab.MainTabData;

public class BookmarkFolder extends BookmarkItem implements Serializable {
    protected static final String COLUMN_NAME_LIST = "2";
    public static final int BOOKMARK_ITEM_ID = 1;

    public BookmarkFolder parent;
    public final ArrayList<BookmarkItem> list;

    public BookmarkFolder(String title, BookmarkFolder parent, long id) {
        super(title, id);
        this.list = new ArrayList<>();
        this.parent = parent;
    }

    public BookmarkFolder(List<MainTabData> list, long id) {
        super(null, id);
        this.list = new ArrayList<>(list.size());
        for (MainTabData tab : list)
            if (!TextUtils.isEmpty(tab.mUrl))
                this.list.add(new BookmarkSite((tab.mTitle != null) ? tab.mTitle : tab.mUrl, tab.mUrl, BookmarkIdGenerator.getNewId()));
        this.parent = null;
    }

    public void add(BookmarkItem item) {
        list.add(item);
    }

    public void addAll(Collection<BookmarkItem> addlist) {
        list.addAll(addlist);
    }

    public void clear() {
        list.clear();
    }

    @Override
    protected int getType() {
        return BOOKMARK_ITEM_ID;
    }

    @Override
    protected boolean writeMain(JsonGenerator generator) throws IOException {
        generator.writeArrayFieldStart(COLUMN_NAME_LIST);
        for (BookmarkItem item : list) {
            if (!item.write(generator)) return false;
        }
        generator.writeEndArray();
        return true;
    }

    @Override
    protected boolean readMain(JsonParser parser) throws IOException {
        if (!COLUMN_NAME_LIST.equals(parser.getCurrentName())) return false;

        if (parser.nextToken() != JsonToken.START_ARRAY) return false;
        while (parser.nextToken() != JsonToken.END_ARRAY) {
            BookmarkItem item = read(parser, this);
            if (item == null) return false;
            list.add(item);
        }
        return true;
    }

    public final boolean writeForRoot(JsonGenerator generator) throws IOException {
        generator.writeStartArray();
        for (BookmarkItem item : list) {
            if (!item.write(generator)) return false;
        }
        generator.writeEndArray();
        return true;
    }

    public final boolean readForRoot(JsonParser parser) throws IOException {
        if (parser.nextToken() != JsonToken.START_ARRAY) return false;
        while (parser.nextToken() != JsonToken.END_ARRAY) {
            BookmarkItem item = read(parser, this);
            if (item == null) return false;
            list.add(item);
        }
        return true;
    }
}
