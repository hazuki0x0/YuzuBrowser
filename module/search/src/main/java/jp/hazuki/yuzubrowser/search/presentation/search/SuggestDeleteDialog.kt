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

package jp.hazuki.yuzubrowser.search.presentation.search

import android.app.AlertDialog
import android.app.Dialog
import android.content.Context
import android.os.Bundle
import androidx.fragment.app.DialogFragment
import jp.hazuki.yuzubrowser.search.R

class SuggestDeleteDialog : DialogFragment() {

    private var deleteQuery: OnDeleteQuery? = null

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        return AlertDialog.Builder(activity)
            .setTitle(R.string.delete_history)
            .setMessage(R.string.confirm_delete_history)
            .setPositiveButton(android.R.string.ok) { _, _ ->
                deleteQuery?.onDelete(arguments!!.getString(ARG_QUERY)!!)
            }
            .setNegativeButton(android.R.string.cancel, null)
            .create()
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        deleteQuery = activity as OnDeleteQuery
    }

    override fun onDetach() {
        super.onDetach()
        deleteQuery = null
    }

    internal interface OnDeleteQuery {
        fun onDelete(query: String)
    }

    companion object {
        private const val ARG_QUERY = "query"

        fun newInstance(query: String): SuggestDeleteDialog {
            val dialog = SuggestDeleteDialog()
            val bundle = Bundle()
            bundle.putString(ARG_QUERY, query)
            dialog.arguments = bundle
            return dialog
        }
    }
}
