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

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ItemDecoder {

    private static final Pattern IP_ADDRESS = Pattern.compile("^\\d+\\.\\d+\\.\\d+\\.\\d+$");
    private static final Pattern HOST = Pattern.compile("^https?://([0-9a-z.\\-]+)/?$");

    private FastMatcherFactory factory;

    public ItemDecoder() {
        factory = new FastMatcherFactory();
    }

    public FastMatcher singleDecode(String line, int id, int count, long time) {
        SimpleCountMatcher matcher = singleDecode(line);
        if (matcher != null) {
            matcher.setId(id);
            matcher.setCount(count);
            matcher.setTime(time);
        }
        return matcher;
    }

    public SimpleCountMatcher singleDecode(String line) {
        if (line.length() > 2) {
            if (line.charAt(0) == '[' && line.charAt(line.length() - 1) == ']') {
                return new RegexUrl(line.substring(1, line.length() - 1));
            }
            int space = line.indexOf(' ');
            if (space > 0) {
                String ip = line.substring(0, space);
                if ((IP_ADDRESS.matcher(ip).matches() && line.length() > space + 1)
                        || ip.equals("h") || ip.equals("host")) {
                    return factory.compileHost(line.substring(space + 1));
                } else if (ip.equals("c")) {
                    return new ContainsHost(line.substring(space + 1));
                }
            }
            Matcher matcher = HOST.matcher(line);
            if (matcher.matches()) {
                String host = matcher.group();
                return factory.compileHost(host);
            }

            return factory.compileUrl(line);
        }
        return null;
    }

    public void release() {
        factory.release();
    }
}
