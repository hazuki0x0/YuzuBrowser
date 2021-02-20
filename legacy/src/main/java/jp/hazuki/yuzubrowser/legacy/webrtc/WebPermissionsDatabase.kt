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

package jp.hazuki.yuzubrowser.legacy.webrtc

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import jp.hazuki.yuzubrowser.legacy.webrtc.core.WebPermissions
import jp.hazuki.yuzubrowser.legacy.webrtc.database.Migration1to2
import jp.hazuki.yuzubrowser.legacy.webrtc.database.PermissionStateConverter

@Database(entities = [WebPermissions::class], version = 2, exportSchema = false)
@TypeConverters(PermissionStateConverter::class)
abstract class WebPermissionsDatabase : RoomDatabase() {
    abstract fun webPermissionsDao(): WebPermissionsDao

    companion object {
        fun newInstance(context: Context): WebPermissionsDatabase {
            return Room.databaseBuilder(context, WebPermissionsDatabase::class.java, FILENAME)
                .addMigrations(Migration1to2())
                .build()
        }

        private const val FILENAME = "WebPermissions.db"
    }
}
