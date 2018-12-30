package jp.hazuki.utility.utils;

public interface Transformer<S, T> {
    S transform(T from);
}