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

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import jp.hazuki.yuzubrowser.core.utility.log.ErrorReport;
import jp.hazuki.yuzubrowser.core.utility.log.Logger;

public class ReflectionUtils {
    private ReflectionUtils() {
        throw new UnsupportedOperationException();
    }

    private static final String TAG = "ReflectionUtils";

    public static Method getMethod(Class<?> cls, String name, Class<?>... args) {
        try {
            return cls.getDeclaredMethod(name, args);
        } catch (NoSuchMethodException e) {
            ErrorReport.printAndWriteLog(e);
        }
        Logger.w(TAG, "getMethod: error");
        return null;
    }

    public static Method getMethod(String clsname, String name, Class<?>... args) {
        try {
            return Class.forName(clsname).getDeclaredMethod(name, args);
        } catch (NoSuchMethodException | ClassNotFoundException e) {
            ErrorReport.printAndWriteLog(e);
        }
        Logger.w(TAG, "getMethod: error");
        return null;
    }

    public static Object invokeMethod(Method method, Object receiver, Object... args) {
        if (method == null) {
            Logger.w(TAG, "invokeMethod: method is null");
            return null;
        }
        try {
            return method.invoke(receiver, args);
        } catch (IllegalArgumentException | InvocationTargetException | IllegalAccessException e) {
            ErrorReport.printAndWriteLog(e);
        }
        Logger.w(TAG, "invokeMethod: error");
        return null;
    }

    public static Class<?> getClass(String name) {
        try {
            return Class.forName(name);
        } catch (ClassNotFoundException e) {
            ErrorReport.printAndWriteLog(e);
        }
        Logger.w(TAG, "getClass: error");
        return null;
    }

    public static Object getMemberObject(Object obj, String name) {
        if (obj == null) {
            Logger.w(TAG, "getMemberObject: obj is null");
            return null;
        }
        try {
            Field field = obj.getClass().getDeclaredField(name);
            field.setAccessible(true);
            return field.get(obj);
        } catch (NoSuchFieldException | IllegalAccessException | IllegalArgumentException e) {
            ErrorReport.printAndWriteLog(e);
        }
        Logger.w(TAG, "getMemberObject: error");
        return null;
    }

    public static boolean setMemberObject(Object obj, String name, Object value) {
        if (obj == null) {
            Logger.w(TAG, "getMemberObject: obj is null");
            return false;
        }
        try {
            Field field = obj.getClass().getDeclaredField(name);
            field.setAccessible(true);
            field.set(obj, value);
            return true;
        } catch (NoSuchFieldException | IllegalAccessException | IllegalArgumentException e) {
            ErrorReport.printAndWriteLog(e);
        }
        Logger.w(TAG, "setMemberObject: error");
        return false;
    }

    public static Constructor<?> getConstructor(String clsname, Class<?>... parameterTypes) {
        try {
            return Class.forName(clsname).getConstructor(parameterTypes);
        } catch (IllegalArgumentException | ClassNotFoundException | NoSuchMethodException e) {
            ErrorReport.printAndWriteLog(e);
        }
        Logger.w(TAG, "getConstructor: error");
        return null;
    }

    public static Object newInstance(Constructor<?> constructor, Object... args) {
        if (constructor == null) {
            Logger.w(TAG, "newInstance: constructor is null");
            return null;
        }
        try {
            return constructor.newInstance(args);
        } catch (IllegalArgumentException | InvocationTargetException | IllegalAccessException | InstantiationException e) {
            ErrorReport.printAndWriteLog(e);
        }
        Logger.w(TAG, "newInstance: error");
        return null;
    }

    public static Object newInstance(String clsname) {
        try {
            return Class.forName(clsname).getConstructor().newInstance();
        } catch (IllegalArgumentException | ClassNotFoundException | NoSuchMethodException | InvocationTargetException | IllegalAccessException | InstantiationException e) {
            ErrorReport.printAndWriteLog(e);
        }
        Logger.w(TAG, "newInstance: error");
        return null;
    }
}
