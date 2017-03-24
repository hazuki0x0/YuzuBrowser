package jp.hazuki.yuzubrowser.bookmark;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;

import java.io.IOException;
import java.io.Serializable;

import jp.hazuki.yuzubrowser.bookmark.util.BookmarkIdGenerator;

public abstract class BookmarkItem implements Serializable {
    protected static final String COLUMN_NAME_TYPE = "0";
    protected static final String COLUMN_NAME_TITLE = "1";
    protected static final String COLUMN_NAME_ID = "3";

    public String title;
    private long id;

    public BookmarkItem(String title, long id) {
        this.title = title;
        this.id = id;
    }

    protected final boolean write(JsonGenerator generator) throws IOException {
        boolean ret;
        generator.writeStartObject();
        generator.writeNumberField(COLUMN_NAME_TYPE, getType());
        generator.writeStringField(COLUMN_NAME_TITLE, title);
        generator.writeNumberField(COLUMN_NAME_ID, id);
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
        parser.nextToken();
        long itemId;
        if (COLUMN_NAME_ID.equals(parser.getCurrentName()) && parser.nextToken() == JsonToken.VALUE_NUMBER_INT) {
            itemId = parser.getLongValue();
            parser.nextToken();
        } else {
            itemId = BookmarkIdGenerator.getNewId();
        }

        BookmarkItem item;
        switch (id) {
            case BookmarkFolder.BOOKMARK_ITEM_ID:
                item = new BookmarkFolder(title, parent, itemId);
                break;
            case BookmarkSite.BOOKMARK_ITEM_ID:
                item = new BookmarkSite(title, itemId);
                break;
            default:
                return null;
        }
        item.readMain(parser);
        parser.skipChildren();

        if (parser.nextToken() != JsonToken.END_OBJECT) return null;
        return item;
    }

    public long getId() {
        return id;
    }

    protected abstract int getType();

    protected abstract boolean writeMain(JsonGenerator generator) throws IOException;

    protected abstract boolean readMain(JsonParser parser) throws IOException;
}
