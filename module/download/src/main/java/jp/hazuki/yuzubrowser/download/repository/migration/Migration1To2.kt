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

package jp.hazuki.yuzubrowser.download.repository.migration

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

internal class Migration1To2 : Migration(1, 2) {
    override fun migrate(database: SupportSQLiteDatabase) {
        database.apply {
            execSQL(CREATE_TABLE)

            execSQL("""
                insert into $NEW_TABLE (`id`,`url`,`mimeType`,`root`,`name`,`size`,`resumable`,`startTime`,`state`)
                select `_id`,`url`,`mimeType`,`root`,`name`,`size`,`resumable`,`start_time`,`state` from $TABLE
            """.trimIndent())

            execSQL("drop table $TABLE")
            execSQL("alter table $NEW_TABLE rename to $TABLE")
        }
    }

    companion object {

        private const val TABLE = "downloads"

        private const val NEW_TABLE = "new_downloads"

        private const val CREATE_TABLE = "CREATE TABLE IF NOT EXISTS `new_downloads` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `url` TEXT NOT NULL, `mimeType` TEXT NOT NULL, `root` TEXT NOT NULL, `name` TEXT NOT NULL, `size` INTEGER NOT NULL, `resumable` INTEGER NOT NULL, `startTime` INTEGER NOT NULL, `state` INTEGER NOT NULL)"

        private const val INSERT = "INSERT OR ABORT INTO `downloads` (`id`,`url`,`mimeType`,`root`,`name`,`size`,`resumable`,`startTime`,`state`) VALUES (nullif(?, 0),?,?,?,?,?,?,?,?)"
    }
}
