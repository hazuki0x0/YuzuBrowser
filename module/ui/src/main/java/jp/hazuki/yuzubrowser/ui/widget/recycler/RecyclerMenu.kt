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

package jp.hazuki.yuzubrowser.ui.widget.recycler

import android.content.Context
import android.view.View
import android.widget.PopupMenu
import jp.hazuki.yuzubrowser.ui.R

class RecyclerMenu(context: Context, anchor: View, position: Int, menuListener: OnRecyclerMenuListener, moveListener: OnRecyclerMoveListener) {

    private val popupMenu: PopupMenu = PopupMenu(context, anchor)

    init {
        popupMenu.menuInflater.inflate(R.menu.recycler_menu, popupMenu.menu)

        popupMenu.setOnMenuItemClickListener { item ->
            when (item.itemId) {
                R.id.delete -> {
                    menuListener.onDeleteClicked(position)
                    true
                }
                R.id.moveUp -> {
                    moveListener.onMoveUp(position)
                    true
                }
                R.id.moveDown -> {
                    moveListener.onMoveDown(position)
                    true
                }
                else -> false
            }
        }
    }


    fun show() {
        popupMenu.show()
    }

    interface OnRecyclerMenuListener {
        fun onDeleteClicked(position: Int)
    }

    interface OnRecyclerMoveListener {
        fun onMoveUp(position: Int)

        fun onMoveDown(position: Int)
    }
}
