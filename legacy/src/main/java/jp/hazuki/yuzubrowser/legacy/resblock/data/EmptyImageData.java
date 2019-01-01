/*
 * Copyright (C) 2017-2019 Hazuki
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package jp.hazuki.yuzubrowser.legacy.resblock.data;

import android.content.Context;
import android.webkit.WebResourceResponse;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;

import java.io.ByteArrayInputStream;
import java.io.IOException;

import jp.hazuki.yuzubrowser.core.utility.log.ErrorReport;
import jp.hazuki.yuzubrowser.legacy.resblock.ResourceData;
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
