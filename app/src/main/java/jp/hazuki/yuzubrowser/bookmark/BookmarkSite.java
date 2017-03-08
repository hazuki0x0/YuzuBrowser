package jp.hazuki.yuzubrowser.bookmark;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;

import java.io.IOException;
import java.io.Serializable;

public class BookmarkSite extends BookmarkItem implements Serializable {
    protected static final String COLUMN_NAME_URL = "2";
    public static final int BOOKMARK_ITEM_ID = 2;

    public String url;

    public BookmarkSite(String title) {
        super(title);
    }

    public BookmarkSite(String title, String url) {
        super(title);
        this.url = url;
    }

    @Override
    protected int getId() {
        return BOOKMARK_ITEM_ID;
    }

    @Override
    protected boolean writeMain(JsonGenerator generator) throws IOException {
        generator.writeStringField(COLUMN_NAME_URL, url);
        return true;
    }

    @Override
    protected boolean readMain(JsonParser parser) throws IOException {
        parser.nextToken();
        if (!COLUMN_NAME_URL.equals(parser.getCurrentName())) return false;
        if (parser.nextToken() != JsonToken.VALUE_STRING) return false;
        url = parser.getText();
        return true;
    }

}
