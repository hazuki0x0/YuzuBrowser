package jp.hazuki.yuzubrowser.utils;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import jp.hazuki.yuzubrowser.utils.util.Predicate;
import jp.hazuki.yuzubrowser.utils.util.Transformer;

public class ArrayUtils {
    private ArrayUtils() {
        throw new UnsupportedOperationException();
    }

    public static long[] toArray(Collection<Long> collection) {
        Object[] boxedArray = collection.toArray();
        int size = boxedArray.length;
        long[] array = new long[size];
        for (int i = 0; i < size; i++) {
            array[i] = (Long) boxedArray[i];
        }
        return array;
    }

    public interface StringConverter<T> {
        String toString(T object);
    }

    public static <T> String[] toStringArray(Collection<T> collection, StringConverter<T> converter) {
        String strs[] = new String[collection.size()];
        int i = 0;
        for (T object : collection) {
            strs[i] = converter.toString(object);
            ++i;
        }
        return strs;
    }

    public static <T> int findIndexOfValue(T value, T[] intarray) {
        if (value == null)
            return -1;
        for (int i = 0; i < intarray.length; ++i) {
            if (value.equals(intarray[i])) return i;
        }
        return -1;
    }

    public static int findIndexOfValue(int value, int[] intarray) {
        for (int i = 0; i < intarray.length; ++i) {
            if (value == intarray[i]) return i;
        }
        return -1;
    }

    public static boolean[] getBits(int num, int max) {
        boolean bits[] = new boolean[max];

        for (int i = 0; i < max; ++i)
            bits[i] = (num & (1 << i)) != 0;

        return bits;
    }

    public static int getBitsInt(boolean[] array) {
        if (array == null)
            return 0;

        int num = 0;

        for (int i = 0; i < array.length; ++i)
            if (array[i])
                num |= 1 << i;

        return num;
    }

    public static <T> void move(List<T> list, int positionFrom, int positionTo) {
        T item = list.remove(positionFrom);
        list.add(positionTo, item);
        //Collections.swap(mCurrentFolder.list, positionFrom, positionTo);
    }

    public static <T> String join(Collection<T> list, String c) {
        if (list == null || list.isEmpty())
            return "";

        StringBuilder builder = new StringBuilder();
        for (T obj : list)
            builder.append(obj.toString()).append(c);

        return builder.substring(0, builder.length() - 1);
    }

    @SuppressWarnings("unchecked")
    public static <T> T[] copyIf(T[] array, Predicate<T> predicate) {
        ArrayList<T> list = new ArrayList<>();
        for (T value : array)
            if (predicate.evaluate(value))
                list.add(value);
        return list.toArray((T[]) Array.newInstance(array.getClass().getComponentType(), 0));
    }

    public static <S, T> Collection<S> transform(Collection<T> from, Transformer<S, T> trans) {
        ArrayList<S> list = new ArrayList<>(from.size());
        for (T item : from)
            list.add(trans.transform(item));
        return list;
    }
}