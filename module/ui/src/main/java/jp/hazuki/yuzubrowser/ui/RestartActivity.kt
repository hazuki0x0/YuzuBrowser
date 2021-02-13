/*
 * Copyright (C) 2017-2021 Hazuki
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

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.Process
import androidx.appcompat.app.AppCompatActivity

class RestartActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val pid = intent.getIntExtra(EXTRA_PID, -1)
        Process.killProcess(pid)

        val context = applicationContext
        context.startActivity(context.createHomeIntent())
        finish()

        Process.killProcess(Process.myPid())
    }

    private fun Context.createHomeIntent(): Intent {
        return packageManager.getLaunchIntentForPackage(packageName)!!.apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
    }

    companion object {
        private const val EXTRA_PID = "pid"

        fun createIntent(context: Context) = Intent(context, RestartActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
            putExtra(EXTRA_PID, Process.myPid())
        }
    }
}
