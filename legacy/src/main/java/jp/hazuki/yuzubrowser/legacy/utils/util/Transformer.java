package jp.hazuki.yuzubrowser.legacy.utils.util;

public interface Transformer<S, T> {
    S transform(T from);
}