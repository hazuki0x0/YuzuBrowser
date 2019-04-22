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

package jp.hazuki.yuzubrowser.adblock.core

import jp.hazuki.yuzubrowser.adblock.filter.Filter
import jp.hazuki.yuzubrowser.adblock.filter.abp.ABP_PREFIX_ELEMENT
import jp.hazuki.yuzubrowser.adblock.filter.unified.element.ElementFilter
import jp.hazuki.yuzubrowser.adblock.filter.unified.io.ElementReader
import jp.hazuki.yuzubrowser.adblock.filter.unified.io.FilterReader
import jp.hazuki.yuzubrowser.adblock.repository.abp.AbpEntity
import jp.hazuki.yuzubrowser.core.utility.log.ErrorReport
import java.io.File
import java.io.IOException

class AbpLoader(private val abpDir: File, private val entityList: List<AbpEntity>) {

    fun loadAll(prefix: String, filterMatcher: FilterMatcher) {
        entityList.forEach {
            if (it.enabled) {
                try {
                    val file = File(abpDir, prefix + it.entityId)
                    if (!file.exists()) return@forEach
                    val reader = FilterReader(file.inputStream().buffered())
                    if (reader.checkHeader()) {
                        filterMatcher.addAll(reader.readAll())
                    }
                } catch (e: IOException) {
                    ErrorReport.printAndWriteLog(e)
                }
            }

        }
    }

    fun loadAllList(prefix: String): List<Filter> {
        val list = mutableListOf<Filter>()
        entityList.forEach {
            if (it.enabled) {
                try {
                    val file = File(abpDir, prefix + it.entityId)
                    if (!file.exists()) return@forEach
                    val reader = FilterReader(file.inputStream().buffered())
                    if (reader.checkHeader()) {
                        list.addAll(reader.readAll())
                    }
                } catch (e: IOException) {
                    ErrorReport.printAndWriteLog(e)
                }
            }
        }
        return list
    }

    fun loadAllElementFilter(): List<ElementFilter> {
        val list = mutableListOf<ElementFilter>()
        entityList.forEach {
            if (it.enabled) {
                try {
                    val file = File(abpDir, ABP_PREFIX_ELEMENT + it.entityId)
                    if (!file.exists()) return@forEach
                    val reader = ElementReader(file.inputStream().buffered())
                    if (reader.checkHeader()) {
                        list.addAll(reader.readAll())
                    }
                } catch (e: IOException) {
                    ErrorReport.printAndWriteLog(e)
                }
            }
        }
        return list
    }
}
