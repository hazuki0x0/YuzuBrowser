package jp.hazuki.yuzubrowser.toolbar.sub;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageButton;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;

import jp.hazuki.yuzubrowser.R;
import jp.hazuki.yuzubrowser.toolbar.SubToolbar;
import jp.hazuki.yuzubrowser.webkit.CustomWebView;

public class WebViewPageFastScroller extends SubToolbar {
    private final ImageButton buttonUp;
    private final ImageButton buttonDown;
    private OnEndListener mOnEndListener;
    private CustomWebView mWeb;
    private SeekBar seekBar;

    public WebViewPageFastScroller(Context context) {
        super(context);

        View view = LayoutInflater.from(context).inflate(R.layout.page_fast_scroll, this);
        buttonUp = view.findViewById(R.id.buttonUp);
        buttonDown = view.findViewById(R.id.buttonDown);
        ImageButton buttonEnd = view.findViewById(R.id.buttonEnd);
        seekBar = view.findViewById(R.id.seekBar);

        buttonEnd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                close();
            }
        });
    }

    public void show(final CustomWebView web) {
        mWeb = web;

        web.setMyOnScrollChangedListener(new CustomWebView.OnScrollChangedListener() {
            @Override
            public void onScrollChanged(int l, int t, int oldl, int oldt) {
                seekBar.setMax(web.computeVerticalScrollRangeMethod() - web.computeVerticalScrollExtentMethod());
                seekBar.setProgress(web.computeVerticalScrollOffsetMethod());
            }
        });
        buttonUp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                web.pageUp(false);
            }
        });
        buttonUp.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                web.pageUp(true);
                return true;
            }
        });
        buttonDown.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                web.pageDown(false);
            }
        });
        buttonDown.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                web.pageDown(true);
                return true;
            }
        });
        seekBar.setMax(web.computeVerticalScrollRangeMethod() - web.computeVerticalScrollExtentMethod());
        seekBar.setProgress(web.computeVerticalScrollOffsetMethod());
        seekBar.setOnSeekBarChangeListener(new OnSeekBarChangeListener() {
            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                web.scrollTo(web.computeHorizontalScrollOffsetMethod(), seekBar.getProgress());
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                seekBar.setMax(web.computeVerticalScrollRangeMethod() - web.computeVerticalScrollExtentMethod());
            }

            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (fromUser) {
                    web.scrollTo(web.computeHorizontalScrollOffsetMethod(), progress);
                }
            }
        });
    }

    public void close() {
        if (mWeb != null) {
            mWeb.setMyOnScrollChangedListener(null);
            mWeb = null;
        }
        if (mOnEndListener != null)
            mOnEndListener.onEnd();
    }

    public interface OnEndListener {
        boolean onEnd();
    }

    public void setOnEndListener(OnEndListener l) {
        mOnEndListener = l;
    }
}
