package jp.hazuki.yuzubrowser.utils.view;

import android.content.Context;
import android.support.v7.widget.AppCompatTextView;
import android.util.AttributeSet;
import android.view.View;
import android.widget.TextView;

import jp.hazuki.yuzubrowser.utils.ClipboardUtils;

public class CopyableTextView extends AppCompatTextView implements TextView.OnLongClickListener {
    public CopyableTextView(Context context) {
        this(context, null);
    }

    public CopyableTextView(Context context, AttributeSet attrs) {
        super(context, attrs);
        setOnLongClickListener(this);
    }

    @Override
    public boolean onLongClick(View v) {
        ClipboardUtils.setClipboardText(getContext().getApplicationContext(), getText().toString());
        return true;
    }
}
