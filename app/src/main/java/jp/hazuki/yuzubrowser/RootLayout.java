package jp.hazuki.yuzubrowser;

import android.app.Activity;
import android.content.Context;
import android.support.annotation.AttrRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.CoordinatorLayout;
import android.util.AttributeSet;

import jp.hazuki.yuzubrowser.utils.ImeUtils;

public class RootLayout extends CoordinatorLayout {
    private OnImeShownListener mOnImeShownListener;

    public RootLayout(Context context) {
        super(context);
    }

    public RootLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public RootLayout(@NonNull Context context, @Nullable AttributeSet attrs, @AttrRes int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        if (mOnImeShownListener != null) {
            mOnImeShownListener.onImeVisibilityChanged(ImeUtils.isImeShown((Activity) getContext()));
        }
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    }

    public void setOnImeShownListener(OnImeShownListener l) {
        mOnImeShownListener = l;
    }

    public interface OnImeShownListener {
        void onImeVisibilityChanged(boolean visible);
    }
}
