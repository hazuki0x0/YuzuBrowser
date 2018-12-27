package jp.hazuki.yuzubrowser.legacy.utils.matcher;

import android.content.Context;

import com.fasterxml.jackson.core.JsonGenerator;

import java.io.IOException;
import java.io.Serializable;

public abstract class AbstractPatternAction implements Serializable {
    public abstract String getTitle(Context context);

    public abstract boolean write(JsonGenerator generator) throws IOException;
}
