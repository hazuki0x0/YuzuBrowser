package jp.hazuki.yuzubrowser.utils.util;

public interface Transformer<S, T> {
    S transform(T from);
}