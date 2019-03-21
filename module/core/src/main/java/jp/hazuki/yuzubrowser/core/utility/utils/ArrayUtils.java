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

package jp.hazuki.yuzubrowser.core.utility.utils;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class ArrayUtils {
    private ArrayUtils() {
        throw new UnsupportedOperationException();
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

    public static String[] copyOf(String[] original, int newLength) {
        final String[] result = new String[newLength];
        System.arraycopy(original, 0, result, 0, newLength);
        return result;
    }

    public static Object[] copyOf(Object[] original, int newLength) {
        final Object[] result = new Object[newLength];
        System.arraycopy(original, 0, result, 0, newLength);
        return result;
    }
}