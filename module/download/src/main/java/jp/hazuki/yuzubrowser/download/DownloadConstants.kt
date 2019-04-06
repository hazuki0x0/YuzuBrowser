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

package jp.hazuki.yuzubrowser.download

import jp.hazuki.yuzubrowser.ui.INTENT_ACTION_PREFIX
import jp.hazuki.yuzubrowser.ui.INTENT_EXTRA_PREFIX

const val TMP_FILE_SUFFIX = ".yuzudownload"

const val NOTIFICATION_CHANNEL_DOWNLOAD_SERVICE = "jp.hazuki.yuzubrowser.channel.dl.service"
const val NOTIFICATION_CHANNEL_DOWNLOAD_NOTIFY = "jp.hazuki.yuzubrowser.channel.dl.notify2"

const val INTENT_ACTION_START_DOWNLOAD = "$INTENT_ACTION_PREFIX.download.start"
const val INTENT_ACTION_RESTART_DOWNLOAD = "$INTENT_ACTION_PREFIX.download.restart"
const val INTENT_ACTION_CANCEL_DOWNLOAD = "$INTENT_ACTION_PREFIX.cancel.download"
const val INTENT_ACTION_PAUSE_DOWNLOAD = "$INTENT_ACTION_PREFIX.pause.download"

const val INTENT_EXTRA_DOWNLOAD_ID = "$INTENT_EXTRA_PREFIX.download.id"
const val INTENT_EXTRA_DOWNLOAD_REQUEST = "$INTENT_EXTRA_PREFIX.download.request"
const val INTENT_EXTRA_DOWNLOAD_METADATA = "$INTENT_EXTRA_PREFIX.download.metadata"
const val INTENT_EXTRA_DOWNLOAD_ROOT_URI = "$INTENT_EXTRA_PREFIX.download.root"