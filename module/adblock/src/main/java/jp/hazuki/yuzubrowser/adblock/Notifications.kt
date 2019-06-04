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

package jp.hazuki.yuzubrowser.adblock

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build

const val NOTIFICATION_CHANNEL_ADBLOCK_FILTER_UPDATE = "jp.hazuki.yuzubrowser.channel.abp.update"

fun Context.registerAdBlockNotification() {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
        val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        val service = NotificationChannel(
            NOTIFICATION_CHANNEL_ADBLOCK_FILTER_UPDATE,
            getString(R.string.updating_ad_filters),
            NotificationManager.IMPORTANCE_MIN)

        service.lockscreenVisibility = Notification.VISIBILITY_PRIVATE


        manager.createNotificationChannels(listOf(service))
    }
}
