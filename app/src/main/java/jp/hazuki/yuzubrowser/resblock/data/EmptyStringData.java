package jp.hazuki.yuzubrowser.resblock.data;

import android.content.Context;
import android.webkit.WebResourceResponse;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;

import java.io.IOException;
import java.io.InputStream;

import jp.hazuki.yuzubrowser.resblock.ResourceData;

public class EmptyStringData extends ResourceData {
    private static final EmptyInputStream sInputStream = new EmptyInputStream();

    public EmptyStringData() {
    }

    public EmptyStringData(JsonParser parser) throws IOException {
        //if(parser.nextToken() == JsonToken.VALUE_NULL) return;
    }

    public int getTypeId() {
        return EMPTY_STRING_DATA;
    }

    @Override
    public String getTitle(Context context) {
        return null;
    }

    @Override
    public WebResourceResponse getResource(Context context) {
        return new WebResourceResponse("text/html", "UTF-8", sInputStream);
    }

    @Override
    public boolean write(JsonGenerator generator) throws IOException {
        generator.writeNumber(EMPTY_STRING_DATA);
        return true;
    }

    private static final class EmptyInputStream extends InputStream {
        @Override
        public int read() throws IOException {
            return -1;
        }
    }
}
