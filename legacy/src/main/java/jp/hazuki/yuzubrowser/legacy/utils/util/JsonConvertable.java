package jp.hazuki.yuzubrowser.legacy.utils.util;

public interface JsonConvertable {
    String toJsonString();

    boolean fromJsonString(String str);
}
