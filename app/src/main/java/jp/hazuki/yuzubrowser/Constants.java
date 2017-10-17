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

package jp.hazuki.yuzubrowser;

public final class Constants {
    public static final class intent {
        public static final String ACTION_OPEN_DEFAULT = "jp.hazuki.yuzubrowser.action.default";
        public static final String EXTRA_MODE_FULLSCREEN = "jp.hazuki.yuzubrowser.extra.fullscreen";
        public static final String EXTRA_MODE_ORIENTATION = "jp.hazuki.yuzubrowser.extra.orientation";
        public static final String EXTRA_URL = "jp.hazuki.yuzubrowser.extra.url";
        public static final String EXTRA_USER_AGENT = "jp.hazuki.yuzubrowser.extra.user_agent";
    }

    public static final class notification {
        public static final String CHANNEL_DOWNLOAD_SERVICE = "jp.hazuki.yuzubrowser.channel.dl.service";
        public static final String CHANNEL_DOWNLOAD_NOTIFY = "jp.hazuki.yuzubrowser.channel.dl.notify2";
    }
}
