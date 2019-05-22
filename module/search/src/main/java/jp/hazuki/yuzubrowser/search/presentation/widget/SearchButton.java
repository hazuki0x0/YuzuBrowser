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

package jp.hazuki.yuzubrowser.search.presentation.widget;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;

import androidx.appcompat.widget.AppCompatImageButton;
import jp.hazuki.yuzubrowser.search.R;
import jp.hazuki.yuzubrowser.ui.widget.swipebutton.SwipeController;

public class SearchButton extends AppCompatImageButton implements SwipeController.OnChangeListener {
    private final SwipeController mSwipeController;
    private Callback mCallback;

    public SearchButton(Context context) {
        this(context, null);
    }

    public SearchButton(Context context, AttributeSet attrs) {
        super(context, attrs);

        mSwipeController = new SwipeController(context);
        mSwipeController.setOnChangeListener(this);
    }

    public interface Callback {
        void forceOpenUrl();

        void forceSearchWord();

        void autoSearch();

        void recognizeSpeech();
    }

    public void setActionCallback(Callback callback) {
        mCallback = callback;
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        mSwipeController.onTouchEvent(event);
        return super.onTouchEvent(event);
    }

    @Override
    public boolean onEventOutSide() {
        setImageResource(R.drawable.ic_search_white_24dp);
        return false;
    }

    @Override
    public boolean onEventCancel() {
        setImageResource(R.drawable.ic_search_white_24dp);
        return false;
    }

    @Override
    public boolean onEventActionUp(int whatNo) {
        setImageResource(R.drawable.ic_search_white_24dp);

        switch (whatNo) {
            case SwipeController.SWIPE_LEFT:
                mCallback.forceOpenUrl();
                break;
            case SwipeController.SWIPE_DOWN:
                mCallback.forceSearchWord();
                break;
            case SwipeController.SWIPE_PRESS:
                mCallback.autoSearch();
                break;
        }
        return false;
    }

    @Override
    public boolean onEventActionDown() {
        return false;
    }

    @Override
    public void onChangeState(int whatNo) {
        switch (whatNo) {
            case SwipeController.SWIPE_LEFT:
                setImageResource(R.drawable.ic_public_white_24dp);
                break;
            case SwipeController.SWIPE_DOWN:
                setImageResource(R.drawable.ic_mode_edit_white_24dp);
                break;
            default:
                setImageResource(R.drawable.ic_search_white_24dp);
                break;
        }
    }

    @Override
    public void onLongPress() {
        mCallback.recognizeSpeech();
    }

    public void setSense(int sense) {
        mSwipeController.setSense(sense);
    }
}
