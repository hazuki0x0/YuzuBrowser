package jp.hazuki.yuzubrowser.gesture;

import android.app.Activity;
import android.content.Intent;
import android.gesture.Gesture;
import android.gesture.GestureOverlayView;
import android.os.Bundle;
import android.widget.Toast;

import jp.hazuki.yuzubrowser.R;
import jp.hazuki.yuzubrowser.action.ActionNameArray;

public class GestureTestActivity extends Activity {
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

        GestureOverlayView gestureOverlayView = (GestureOverlayView) findViewById(R.id.gestureOverlayView);
        gestureOverlayView.setGestureStrokeType(manager.getGestureStrokeType());

        mActionNameArray = intent.getParcelableExtra(ActionNameArray.INTENT_EXTRA);
        if (mActionNameArray == null)
            mActionNameArray = new ActionNameArray(getApplicationContext());

        gestureOverlayView.addOnGesturePerformedListener(new GestureOverlayView.OnGesturePerformedListener() {
            @Override
            public void onGesturePerformed(GestureOverlayView overlay, final Gesture gesture) {
                overlay.clear(false);
                GestureScore score = manager.getScore(gesture);
                if (score == null)
                    return;
                Toast.makeText(getApplicationContext(), score.score + " : " + score.action.toString(mActionNameArray), Toast.LENGTH_SHORT).show();
            }
        });
    }
}
