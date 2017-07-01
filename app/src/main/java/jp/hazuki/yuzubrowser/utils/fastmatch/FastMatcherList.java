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

import android.net.Uri;

import java.util.ArrayList;
import java.util.Collections;

public class FastMatcherList {
    private long dbTime = -1;
    private ArrayList<FastMatcher> matcherList = new ArrayList<>();

    public long getDbTime() {
        return dbTime;
    }

    public void setDbTime(long dbTime) {
        this.dbTime = dbTime;
    }

    public ArrayList<FastMatcher> getMatcherList() {
        return matcherList;
    }

    public void setMatcherList(ArrayList<FastMatcher> matcherList) {
        this.matcherList = matcherList;
    }

    public void add(FastMatcher matcher) {
        matcherList.add(matcher);
    }

    public boolean match(Uri uri) {
        for (FastMatcher matcher : matcherList)
            if (matcher.match(uri)) return true;
        return false;
    }

    public void sort() {
        Collections.sort(matcherList, new FastMatcherSorter());
    }
}
