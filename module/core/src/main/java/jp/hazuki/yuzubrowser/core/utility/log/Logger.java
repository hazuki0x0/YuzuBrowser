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

package jp.hazuki.yuzubrowser.core.utility.log;

import android.util.Log;

import java.net.URISyntaxException;

public class Logger {
    public static boolean isDebug = true;

    public static void d(String tag, Object... msg) {
        if (isDebug) {
            StringBuilder builder = new StringBuilder();
            for (Object m : msg) {
                if (m != null)
                    builder.append(m.toString());
            }

            Log.d(tag, builder.toString());
        }
    }

    public static void d(String tag, String msg) {
        if (isDebug) {
            Log.d(tag, msg);
        }
    }

    public static void d(String tag, Object msg) {
        if (isDebug) {
            Log.d(tag, msg.toString());
        }
    }

    public static void i(String tag, String msg) {
        if (isDebug) {
            Log.d(tag, msg);
        }
    }

    public static void w(String tag, String msg) {
        if (isDebug) {
            Log.w(tag, msg);
        }
    }

    public static void w(String tag, Object... msg) {
        if (isDebug) {
            StringBuilder builder = new StringBuilder();
            for (Object m : msg)
                if (m != null)
                    builder.append(m.toString());
            Log.w(tag, builder.toString());
        }
    }

    public static void w(String tag, Exception e, Object... msg) {
        if (isDebug) {
            StringBuilder builder = new StringBuilder();
            for (Object m : msg)
                if (m != null)
                    builder.append(m.toString());
            Log.w(tag, builder.toString(), e);
        }
    }

    public static void e(String tag, String msg) {
        if (isDebug) {
            Log.e(tag, msg);
        }
    }

    public static void e(String tag, String s, URISyntaxException e) {
        if (isDebug) {
            Log.d(tag, s, e);
        }
    }
}
