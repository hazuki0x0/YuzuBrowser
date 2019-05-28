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

package jp.hazuki.yuzubrowser.ui


const val ACTIVITY_MAIN_BROWSER = "jp.hazuki.yuzubrowser.browser.BrowserActivity"

const val INTENT_ACTION_PREFIX = "jp.hazuki.yuzubrowser.action"

const val INTENT_EXTRA_PREFIX = "jp.hazuki.yuzubrowser.extra"
const val INTENT_EXTRA_MODE_FULLSCREEN = "$INTENT_EXTRA_PREFIX.fullscreen"
const val INTENT_EXTRA_MODE_ORIENTATION = "$INTENT_EXTRA_PREFIX.orientation"

const val BROADCAST_ACTION_NOTIFY_CHANGE_WEB_STATE = "jp.hazuki.yuzubrowser.adblock.broadcast.update.browser.webState"

const val PREFERENCE_FILE_NAME = "main_preference"

const val INTENT_EXTRA_OPENABLE = "jp.hazuki.yuzubrowser.BrowserActivity.extra.EXTRA_OPENABLE"
const val INTENT_EXTRA_LOAD_URL_TAB = "jp.hazuki.yuzubrowser.BrowserActivity.extra.EXTRA_LOAD_URL_TAB"
const val INTENT_EXTRA_RESTART = "jp.hazuki.yuzubrowser.BrowserActivity.extra.restart"

const val BROWSER_LOAD_URL_TAB_CURRENT = 0
const val BROWSER_LOAD_URL_TAB_NEW = 1
const val BROWSER_LOAD_URL_TAB_BG = 2
const val BROWSER_LOAD_URL_TAB_NEW_RIGHT = 3
const val BROWSER_LOAD_URL_TAB_BG_RIGHT = 4
const val BROWSER_LOAD_URL_TAB_CURRENT_FORCE = 5
