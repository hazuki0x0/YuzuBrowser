package jp.hazuki.yuzubrowser.bookmark;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;

import java.io.IOException;
import java.io.Serializable;

public abstract class BookmarkItem implements Serializable {
    protected static final String COLUMN_NAME_TYPE = "0";
    protected static final String COLUMN_NAME_TITLE = "1";

    public String title;

    public BookmarkItem(String title) {
        this.title = title;
    }

    protected final boolean write(JsonGenerator generator) throws IOException {
        boolean ret;
        generator.writeStartObject();
        generator.writeNumberField(COLUMN_NAME_TYPE, getId());
        generator.writeStringField(COLUMN_NAME_TITLE, title);
        ret = writeMain(generator);
        generator.writeEndObject();
        return ret;
    }

    protected final BookmarkItem read(JsonParser parser, BookmarkFolder parent) throws IOException {
        if (parser.getCurrentToken() != JsonToken.START_OBJECT) return null;

        parser.nextToken();
        if (!COLUMN_NAME_TYPE.equals(parser.getCurrentName())) return null;
        if (parser.nextToken() != JsonToken.VALUE_NUMBER_INT) return null;
        int id = parser.getIntValue();

        parser.nextToken();
        if (!COLUMN_NAME_TITLE.equals(parser.getCurrentName())) return null;
        if (parser.nextToken() != JsonToken.VALUE_STRING) return null;
        String title = parser.getText();

        BookmarkItem item;
        switch (id) {
            case BookmarkFolder.BOOKMARK_ITEM_ID:
                item = new BookmarkFolder(title, parent);
                break;
            case BookmarkSite.BOOKMARK_ITEM_ID:
                item = new BookmarkSite(title);
                break;
            default:
                return null;
        }
        item.readMain(parser);
        parser.skipChildren();

        if (parser.nextToken() != JsonToken.END_OBJECT) return null;
        return item;
    }

    protected abstract int getId();

    protected abstract boolean writeMain(JsonGenerator generator) throws IOException;

    protected abstract boolean readMain(JsonParser parser) throws IOException;
}
