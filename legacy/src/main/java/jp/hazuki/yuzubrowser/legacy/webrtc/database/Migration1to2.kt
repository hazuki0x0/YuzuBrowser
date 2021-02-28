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

package jp.hazuki.yuzubrowser.legacy.webrtc.database

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

internal class Migration1to2 : Migration(1, 2) {
    override fun migrate(database: SupportSQLiteDatabase) {
        database.apply {
            execSQL(CREATE_TABLE)

            execSQL("""
                insert or ignore into $NEW_TABLE (`host`,`camera`,`microphone`,`midi`,`mediaId`)
                select `host`, `camera`, `mic`, `midi`, `media_id` from $TABLE
            """.trimIndent())

            execSQL("drop table $TABLE")
            execSQL("alter table $NEW_TABLE rename to $TABLE")
        }
    }

    companion object {
        private const val TABLE = "permissions"

        private const val NEW_TABLE = "new_permissions"

        private const val CREATE_TABLE = "CREATE TABLE IF NOT EXISTS `new_permissions` (`host` TEXT NOT NULL, `camera` INTEGER NOT NULL, `microphone` INTEGER NOT NULL, `midi` INTEGER NOT NULL, `mediaId` INTEGER NOT NULL, PRIMARY KEY(`host`))"
    }
}
