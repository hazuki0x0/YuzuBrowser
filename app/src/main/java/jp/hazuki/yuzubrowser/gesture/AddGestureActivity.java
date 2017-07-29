package jp.hazuki.yuzubrowser.gesture;

import android.content.Intent;
import android.gesture.Gesture;
import android.gesture.GestureOverlayView;
import android.os.Bundle;

import jp.hazuki.yuzubrowser.R;
import jp.hazuki.yuzubrowser.action.Action;
import jp.hazuki.yuzubrowser.action.ActionNameArray;
import jp.hazuki.yuzubrowser.action.view.ActionActivity;
import jp.hazuki.yuzubrowser.utils.app.ThemeActivity;

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

        GestureOverlayView gestureOverlayView = (GestureOverlayView) findViewById(R.id.gestureOverlayView);
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
