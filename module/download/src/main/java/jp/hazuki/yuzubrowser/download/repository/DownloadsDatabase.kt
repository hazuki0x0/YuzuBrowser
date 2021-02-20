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

package jp.hazuki.yuzubrowser.download.repository

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import jp.hazuki.yuzubrowser.download.core.data.DownloadFileInfo
import jp.hazuki.yuzubrowser.download.core.utils.UriConverter
import jp.hazuki.yuzubrowser.download.repository.migration.Migration1To2

@Database(entities = [DownloadFileInfo::class], version = 2, exportSchema = false)
@TypeConverters(UriConverter::class)
abstract class DownloadsDatabase : RoomDatabase() {
    abstract fun downloadsDao(): DownloadsDao

    companion object {
        fun createInstance(context: Context): DownloadsDatabase {
            return Room.databaseBuilder(context, DownloadsDatabase::class.java, FILENAME)
                .addMigrations(Migration1To2())
                .build()
        }

        private const val FILENAME = "download.db"
    }
}
