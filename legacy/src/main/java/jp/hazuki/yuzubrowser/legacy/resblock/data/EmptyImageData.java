package jp.hazuki.yuzubrowser.legacy.resblock.data;

import android.content.Context;
import android.webkit.WebResourceResponse;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;

import java.io.ByteArrayInputStream;
import java.io.IOException;

import jp.hazuki.yuzubrowser.legacy.resblock.ResourceData;
import jp.hazuki.yuzubrowser.legacy.utils.ErrorReport;
import jp.hazuki.yuzubrowser.legacy.utils.IOUtils;

public class EmptyImageData extends ResourceData {
    private static byte[] sData;

    public EmptyImageData() {
    }

    public EmptyImageData(JsonParser parser) throws IOException {
        //if(parser.nextToken() == JsonToken.VALUE_NULL) return;
    }

    public int getTypeId() {
        return EMPTY_IMAGE_DATA;
    }

    @Override
    public String getTitle(Context context) {
        return null;
    }

    @Override
    public WebResourceResponse getResource(Context context) {
        if (sData == null)
            try {
                sData = IOUtils.readByte(context.getResources().getAssets().open("blank.png"));
            } catch (IOException e) {
                ErrorReport.printAndWriteLog(e);
            }
        return new WebResourceResponse("image/png", null, new ByteArrayInputStream(sData));
    }

    @Override
    public boolean write(JsonGenerator generator) throws IOException {
        generator.writeNumber(EMPTY_IMAGE_DATA);
        return true;
    }
}
