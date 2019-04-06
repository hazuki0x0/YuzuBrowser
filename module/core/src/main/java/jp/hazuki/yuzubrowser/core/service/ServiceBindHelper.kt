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

package jp.hazuki.yuzubrowser.core.service

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.IBinder

class ServiceBindHelper<T>(private val context: Context, private val connectionHelper: ServiceConnectionHelper<T>) {
    private var isBound = false
    private val connection: ServiceConnection
    var binder: T? = null
        private set

    init {
        connection = object : ServiceConnection {
            override fun onServiceConnected(name: ComponentName, service: IBinder) {
                binder = connectionHelper.onBind(service)
            }

            override fun onServiceDisconnected(name: ComponentName) {
                binder = null
            }
        }
    }

    fun bindService(intent: Intent) {
        if (!isBound) {
            context.bindService(intent, connection, Context.BIND_AUTO_CREATE)
            isBound = true
        }
    }

    fun unbindService() {
        if (isBound) {
            connectionHelper.onUnbind(binder)
            context.unbindService(connection)
            isBound = false
            binder = null
        }
    }
}
