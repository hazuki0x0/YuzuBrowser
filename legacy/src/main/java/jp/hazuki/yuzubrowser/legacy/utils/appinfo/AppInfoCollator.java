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

package jp.hazuki.yuzubrowser.legacy.utils.appinfo;

import java.text.Collator;
import java.util.Comparator;

public class AppInfoCollator implements Comparator<AppInfo> {

    private static final Collator collator = Collator.getInstance();

    @Override
    public int compare(AppInfo a1, AppInfo a2) {

        return collator.compare(a1.getAppName(), a2.getAppName());
    }
}
