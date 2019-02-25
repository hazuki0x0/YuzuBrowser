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

package jp.hazuki.yuzubrowser.legacy

class Constants {
    object activity {
        const val MAIN_BROWSER = "jp.hazuki.yuzubrowser.browser.BrowserActivity"
    }

    object intent {
        private const val PREFIX_ACTION = "jp.hazuki.yuzubrowser.action"
        private const val PREFIX_EXTRA = "jp.hazuki.yuzubrowser.extra"

        const val ACTION_OPEN_DEFAULT = "$PREFIX_ACTION.default"
        const val EXTRA_MODE_FULLSCREEN = "$PREFIX_EXTRA.fullscreen"
        const val EXTRA_MODE_ORIENTATION = "$PREFIX_EXTRA.orientation"
        const val EXTRA_URL = "$PREFIX_EXTRA.url"
        const val EXTRA_USER_AGENT = "$PREFIX_EXTRA.user_agent"
        const val EXTRA_OPEN_FROM_YUZU = "$PREFIX_EXTRA.open.from.yuzu"

        /** download */
        const val ACTION_START_DOWNLOAD     = "$PREFIX_ACTION.download.start"
        const val ACTION_RESTART_DOWNLOAD   = "$PREFIX_ACTION.download.restart"
        const val ACTION_CANCEL_DOWNLOAD    = "$PREFIX_ACTION.cancel.download"
        const val ACTION_PAUSE_DOWNLOAD     = "$PREFIX_ACTION.pause.download"
        const val ACTION_FINISH = "$PREFIX_ACTION.finish"
        const val ACTION_NEW_TAB = "$PREFIX_ACTION.newTab"
        const val EXTRA_ROOT_URI            = "$PREFIX_EXTRA.root"
        const val EXTRA_REQUEST_DOWNLOAD    = "$PREFIX_EXTRA.request.download"
        const val EXTRA_METADATA            = "$PREFIX_EXTRA.metadata"
        const val EXTRA_ID                  = "$PREFIX_EXTRA.id"
        const val EXTRA_FORCE_DESTROY = "$PREFIX_EXTRA.force_destroy"

        /** res block */
        const val ACTION_BLOCK_IMAGE        = "$PREFIX_ACTION.action_block_image"
    }

    object notification {
        const val CHANNEL_DOWNLOAD_SERVICE = "jp.hazuki.yuzubrowser.channel.dl.service"
        const val CHANNEL_DOWNLOAD_NOTIFY = "jp.hazuki.yuzubrowser.channel.dl.notify2"
    }

    object download {
        const val TMP_FILE_SUFFIX = ".yuzudownload"
    }

    object mimeType {
        const val UNKNOWN = "application/octet-stream"
        const val MHTML = "multipart/related"
        const val HTML = "text/html"
    }
}
