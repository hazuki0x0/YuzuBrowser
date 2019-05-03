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

package jp.hazuki.yuzubrowser.bookmark.util;

public final class BookmarkIdGenerator {
    private static BookmarkIdGenerator generator;

    private long lastId = getNow();

    private synchronized long createId() {
        while (true) {
            long newId = getNow();

            if (newId / 1000 == lastId / 1000) {
                if (lastId % 1000 < 999) {
                    return ++lastId;
                }

                try {
                    Thread.sleep(1);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                continue;
            } else if (newId < lastId) {
                try {
                    Thread.sleep(100);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                continue;
            }

            return lastId = newId;
        }
    }

    private static long getNow() {
        long time = System.currentTimeMillis() / 1000;
        return time * 1000;
    }

    public static long getNewId() {
        if (generator == null) {
            generator = new BookmarkIdGenerator();
        }
        return generator.createId();
    }
}
