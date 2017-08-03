/*
 * Copyright (C) 2017 Hazuki
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

package jp.hazuki.yuzubrowser.utils.view.recycler;

import android.content.Context;
import android.view.MenuItem;
import android.view.View;
import android.widget.PopupMenu;

import jp.hazuki.yuzubrowser.R;

public class RecyclerMenu {

    private final PopupMenu popupMenu;

    public RecyclerMenu(Context context, View anchor, final int position, final OnRecyclerMenuListener menuListener, final OnRecyclerMoveListener moveListener) {
        popupMenu = new PopupMenu(context, anchor);

        popupMenu.getMenuInflater().inflate(R.menu.recycler_menu, popupMenu.getMenu());

        popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                switch (item.getItemId()) {
                    case R.id.delete:
                        menuListener.onDelete(position);
                        return true;
                    case R.id.moveUp:
                        moveListener.onMoveUp(position);
                        return true;
                    case R.id.moveDown:
                        moveListener.onMoveDown(position);
                        return true;
                    default:
                        return false;
                }
            }
        });
    }


    public void show() {
        popupMenu.show();
    }

    public interface OnRecyclerMenuListener {
        void onDelete(int position);
    }

    public interface OnRecyclerMoveListener {
        void onMoveUp(int position);

        void onMoveDown(int position);
    }
}
