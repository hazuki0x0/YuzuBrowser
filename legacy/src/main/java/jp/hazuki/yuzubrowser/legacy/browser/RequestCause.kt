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

package jp.hazuki.yuzubrowser.legacy.browser

import androidx.annotation.IntDef

@IntDef(BrowserController.REQUEST_WEB_UPLOAD,
        BrowserController.REQUEST_SEARCHBOX,
        BrowserController.REQUEST_BOOKMARK,
        BrowserController.REQUEST_HISTORY,
        BrowserController.REQUEST_SETTING,
        BrowserController.REQUEST_USERAGENT,
        BrowserController.REQUEST_DEFAULT_USERAGENT,
        BrowserController.REQUEST_USERJS_SETTING,
        BrowserController.REQUEST_WEB_ENCODE_SETTING,
        BrowserController.REQUEST_SHARE_IMAGE,
        BrowserController.REQUEST_ACTION_LIST)
@Retention(AnnotationRetention.SOURCE)
annotation class RequestCause