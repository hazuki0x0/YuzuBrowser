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

package jp.hazuki.yuzubrowser.adblock.ui.abp

import android.app.AlertDialog
import android.app.Dialog
import android.os.Bundle
import androidx.fragment.app.DialogFragment
import dagger.android.support.DaggerDialogFragment
import jp.hazuki.yuzubrowser.adblock.R
import jp.hazuki.yuzubrowser.adblock.repository.abp.AbpDatabase
import jp.hazuki.yuzubrowser.adblock.repository.abp.AbpEntity
import jp.hazuki.yuzubrowser.adblock.service.AbpUpdateService
import jp.hazuki.yuzubrowser.core.utility.utils.ui
import javax.inject.Inject

class AbpFilterSubscribeDialog : DaggerDialogFragment() {

    @Inject
    internal lateinit var abpDatabase: AbpDatabase

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val activity = activity ?: throw IllegalStateException()
        val arguments = arguments ?: throw IllegalArgumentException()
        val url = arguments.getString(ARG_URL) ?: throw IllegalArgumentException()

        return AlertDialog.Builder(activity)
            .setTitle(R.string.add_filter)
            .setMessage("${getString(R.string.title)}:\"${arguments.getString(ARG_TITLE)}\"\n" +
                "${getString(R.string.pattern_edittext_hint)}:$url")
            .setPositiveButton(android.R.string.ok) { _, _ ->
                ui {
                    val entity = AbpEntity(url = url)
                    entity.entityId = abpDatabase.abpDao().inset(entity).toInt()
                    AbpUpdateService.update(activity, entity)
                }
            }
            .setNegativeButton(android.R.string.cancel, null)
            .create()
    }

    companion object {
        private const val ARG_TITLE = "title"
        private const val ARG_URL = "url"

        fun create(title: String, url: String): DialogFragment {
            return AbpFilterSubscribeDialog().apply {
                arguments = Bundle().apply {
                    putString(ARG_TITLE, title)
                    putString(ARG_URL, url)
                }
            }
        }
    }
}
