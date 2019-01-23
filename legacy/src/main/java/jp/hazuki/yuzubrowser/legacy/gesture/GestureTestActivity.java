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

package jp.hazuki.yuzubrowser.legacy.gesture;

import android.content.Intent;
import android.gesture.Gesture;
import android.gesture.GestureOverlayView;
import android.os.Bundle;
import android.widget.Toast;

import jp.hazuki.yuzubrowser.legacy.R;
import jp.hazuki.yuzubrowser.legacy.action.ActionNameArray;
import jp.hazuki.yuzubrowser.ui.app.ThemeActivity;

public class GestureTestActivity extends ThemeActivity {
    private ActionNameArray mActionNameArray;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.gesture_activity);

        Intent intent = getIntent();
        if (intent == null)
            throw new IllegalStateException("intent is null");

        String title = intent.getStringExtra(Intent.EXTRA_TITLE);
        if (title != null)
            setTitle(title);

        int gesture_id = intent.getIntExtra(GestureManager.INTENT_EXTRA_GESTURE_ID, -1);
        if (gesture_id < 0)
            throw new IllegalStateException("Unknown intent id");

        final GestureManager manager = GestureManager.getInstance(getApplicationContext(), gesture_id);

        GestureOverlayView gestureOverlayView = findViewById(R.id.gestureOverlayView);
        gestureOverlayView.setGestureStrokeType(manager.getGestureStrokeType());

        mActionNameArray = intent.getParcelableExtra(ActionNameArray.INTENT_EXTRA);
        if (mActionNameArray == null)
            mActionNameArray = new ActionNameArray(getApplicationContext());

        gestureOverlayView.addOnGesturePerformedListener(new GestureOverlayView.OnGesturePerformedListener() {
            @Override
            public void onGesturePerformed(GestureOverlayView overlay, final Gesture gesture) {
                overlay.clear(false);
                if (gesture.getStrokesCount() == 0)
                    return;
                GestureScore score = manager.getScore(gesture);
                if (score == null || score.action == null)
                    return;
                Toast.makeText(getApplicationContext(), score.score + " : " + score.action.toString(mActionNameArray), Toast.LENGTH_SHORT).show();
            }
        });
    }
}
