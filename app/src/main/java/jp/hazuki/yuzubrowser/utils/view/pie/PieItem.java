/*
 * fixed by Cynthia Project
 * Copyright (C) 2011 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */

package jp.hazuki.yuzubrowser.utils.view.pie;

import android.content.Context;
import android.graphics.Path;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ImageView.ScaleType;

import jp.hazuki.yuzubrowser.action.Action;
import jp.hazuki.yuzubrowser.action.ActionCallback;
import jp.hazuki.yuzubrowser.utils.view.pie.PieMenu.PieView;

/**
 * Pie menu item
 */
public class PieItem implements View.OnClickListener {
    private Action action;
    private ActionCallback callback;
    private ImageView mView;
    private PieView mPieView;
    private int level;
    private float start;
    private float sweep;
    private int inner;
    private int outer;
    private boolean mSelected;
    private Path mPath;

    public PieItem(Context context, int itemsize, Action action, ActionCallback callback, int level) {
        this(context, itemsize, action, callback, level, null);
    }

    public PieItem(Context context, int itemsize, Action action, ActionCallback callback, int level, PieView sym) {
        this.action = action;
        this.callback = callback;
        this.level = level;
        mPieView = sym;

        mView = new ImageView(context);
        mView.setMinimumWidth(itemsize);
        mView.setMinimumHeight(itemsize);
        mView.setScaleType(ScaleType.CENTER);
        ViewGroup.LayoutParams lp = new ViewGroup.LayoutParams(itemsize, itemsize);
        mView.setLayoutParams(lp);
        mView.setOnClickListener(this);
    }

    public void notifyChangeState() {
        mView.setImageDrawable(callback.getIcon(action));
    }

    public void setSelected(boolean s) {
        mSelected = s;
        if (mView != null) {
            mView.setSelected(s);
        }
    }

    public boolean isSelected() {
        return mSelected;
    }

    public int getLevel() {
        return level;
    }

    public void setGeometry(float st, float sw, int inside, int outside, Path p) {
        start = st;
        sweep = sw;
        inner = inside;
        outer = outside;
        mPath = p;
    }

    public float getStartAngle() {
        return start;
    }

    public float getSweep() {
        return sweep;
    }

    public int getInnerRadius() {
        return inner;
    }

    public int getOuterRadius() {
        return outer;
    }

    public boolean isPieView() {
        return (mPieView != null);
    }

    public ImageView getView() {
        return mView;
    }

    public void setPieView(PieView sym) {
        mPieView = sym;
    }

    public PieView getPieView() {
        return mPieView;
    }

    public Path getPath() {
        return mPath;
    }

    @Override
    public void onClick(View v) {
        callback.run(action);
    }
}
