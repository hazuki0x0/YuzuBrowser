/*
 * Copyright (C) 2017 Hazuki
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

package jp.hazuki.yuzubrowser.utils.fastmatch;

import android.content.Context;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Scanner;

public class FastMatcherCache {
    private static final String FOLDER = "fastMatcher";
    private static final String CACHE_HEADER = "Y.FM.CACHE.V1";
    private static final String ASCII_RS = "\u001e";

    public static void save(Context context, String fileName, FastMatcherList matcherList) {
        File file = new File(context.getCacheDir(), FOLDER + "/" + fileName);
        if (!file.getParentFile().exists()) {
            file.getParentFile().mkdirs();
        }

        try (FileOutputStream fos = new FileOutputStream(file);
             PrintWriter pw = new PrintWriter(fos)) {

            pw.println(CACHE_HEADER);
            pw.println(matcherList.getDbTime());

            for (FastMatcher matcher : matcherList.getMatcherList())
                pw.println(
                        matcher.getType() + ASCII_RS +
                                matcher.getId() + ASCII_RS +
                                matcher.getFrequency() + ASCII_RS +
                                matcher.getTime() + ASCII_RS +
                                matcher.getPattern());

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static long getLastTime(Context context, String fileName) {
        File file = new File(context.getCacheDir(), FOLDER + "/" + fileName);
        if (!file.exists()) return -1;

        try (Scanner scanner = new Scanner(file)) {
            if (scanner.hasNext() && CACHE_HEADER.equals(scanner.nextLine())) {
                if (scanner.hasNext())
                    return Long.parseLong(scanner.nextLine());
            }
        } catch (NumberFormatException | FileNotFoundException e) {
            e.printStackTrace();
        }

        return -1;
    }

    public static FastMatcherList getMatcher(Context context, String fileName) {
        File file = new File(context.getCacheDir(), FOLDER + "/" + fileName);
        FastMatcherList matcherList = new FastMatcherList();
        if (!file.exists()) return matcherList;

        try (Scanner scanner = new Scanner(file)) {
            if (scanner.hasNext() && CACHE_HEADER.equals(scanner.nextLine())) {
                if (scanner.hasNext())
                    matcherList.setDbTime(Long.parseLong(scanner.nextLine()));
                else
                    return matcherList;

                while (scanner.hasNext()) {
                    String[] items = scanner.nextLine().split(ASCII_RS);
                    SimpleCountMatcher matcher;
                    switch (items[0]) {
                        case "1":
                            matcher = new SimpleHost(items[4]);
                            break;
                        case "2":
                            matcher = new SimpleUrl(items[4]);
                            break;
                        case "3":
                            matcher = new RegexHost(items[4]);
                            break;
                        case "4":
                            matcher = new RegexUrl(items[4]);
                            break;
                        case "5":
                            matcher = new ContainsHost(items[4]);
                            break;
                        default:
                            continue;
                    }
                    matcher.setId(Integer.parseInt(items[1]));
                    matcher.setCount(Integer.parseInt(items[2]));
                    matcher.setTime(Long.parseLong(items[3]));
                    matcherList.add(matcher);
                }
            }
        } catch (NumberFormatException | FileNotFoundException e) {
            e.printStackTrace();
        }

        return matcherList;
    }
}
