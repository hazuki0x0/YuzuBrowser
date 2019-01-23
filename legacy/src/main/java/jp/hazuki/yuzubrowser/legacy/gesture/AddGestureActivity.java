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

import jp.hazuki.yuzubrowser.legacy.R;
import jp.hazuki.yuzubrowser.legacy.action.Action;
import jp.hazuki.yuzubrowser.legacy.action.ActionNameArray;
import jp.hazuki.yuzubrowser.legacy.action.view.ActionActivity;
import jp.hazuki.yuzubrowser.ui.app.ThemeActivity;

public class AddGestureActivity extends ThemeActivity {
    private static final int RESULT_REQUEST_ADD = 0;
    private static final String ITEM_GESTURE = "item";
    private ActionNameArray mActionNameArray;
    private GestureManager manager;

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

        mActionNameArray = intent.getParcelableExtra(ActionNameArray.INTENT_EXTRA);

        int gesture_id = intent.getIntExtra(GestureManager.INTENT_EXTRA_GESTURE_ID, -1);
        if (gesture_id < 0)
            throw new IllegalStateException("Unknown intent id");

        manager = GestureManager.getInstance(getApplicationContext(), gesture_id);

        GestureOverlayView gestureOverlayView = findViewById(R.id.gestureOverlayView);
        gestureOverlayView.setGestureStrokeType(manager.getGestureStrokeType());

        gestureOverlayView.addOnGesturePerformedListener(new GestureOverlayView.OnGesturePerformedListener() {
            @Override
            public void onGesturePerformed(GestureOverlayView overlay, final Gesture gesture) {
                overlay.clear(false);
                Bundle bundle = new Bundle();
                bundle.putParcelable(ITEM_GESTURE, gesture);

                Intent start = new ActionActivity.Builder(AddGestureActivity.this)
                        .setTitle(R.string.action_settings)
                        .setActionNameArray(mActionNameArray)
                        .setReturnData(bundle)
                        .create();

                startActivityForResult(start, RESULT_REQUEST_ADD);
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case RESULT_REQUEST_ADD:
                if (resultCode == RESULT_OK && data != null) {
                    Bundle bundle = ActionActivity.getReturnData(data);
                    Gesture gesture = bundle.getParcelable(ITEM_GESTURE);
                    Action action = ActionActivity.getActionFromIntent(data);
                    manager.add(gesture, action);
                    setResult(RESULT_OK);
                    finish();
                }
                break;
        }
    }
}
