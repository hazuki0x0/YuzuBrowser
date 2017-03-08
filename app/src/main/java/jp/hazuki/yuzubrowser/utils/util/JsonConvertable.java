package jp.hazuki.yuzubrowser.utils.util;

public interface JsonConvertable {
    String toJsonString();

    boolean fromJsonString(String str);
}
