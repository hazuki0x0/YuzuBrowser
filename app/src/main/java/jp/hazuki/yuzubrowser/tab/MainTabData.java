package jp.hazuki.yuzubrowser.tab;

import android.content.res.Resources;
import android.graphics.Bitmap;
import android.support.v4.content.res.ResourcesCompat;
import android.view.View;
import android.widget.TextView;

import jp.hazuki.yuzubrowser.R;
import jp.hazuki.yuzubrowser.settings.data.ThemeData;
import jp.hazuki.yuzubrowser.webkit.CustomWebView;

public class MainTabData extends TabData {
    public MainTabData(CustomWebView web, View view) {
        super(web);
        mTabView = view;
    }

    @Override
    public void onPageStarted(String url, Bitmap favicon) {
        super.onPageStarted(url, favicon);
        setText(url);
    }

    @Override
    public void onPageFinished(CustomWebView web, String url) {
        super.onPageFinished(web, url);
        setText((mTitle != null) ? mTitle : url);
    }

    @Override
    public void onReceivedTitle(String title) {
        super.onReceivedTitle(title);
        setText(mTitle);
    }

    @Override
    public void onStateChanged(TabData tabdata) {
        super.onStateChanged(tabdata);
        if (mTitle != null)
            setText(mTitle);
        else
            setText(mUrl);
    }

    public void onMoveTabToBackground(Resources res, Resources.Theme theme) {
        ThemeData themedata = ThemeData.getInstance();
        if (themedata != null && themedata.tabBackgroundNormal != null)
            mTabView.setBackground(themedata.tabBackgroundNormal);
        else
            mTabView.setBackgroundResource(R.drawable.tab_background_normal);

        TextView textView = (TextView) mTabView.findViewById(R.id.textView);
        if (isNavLock())
            if (themedata != null && themedata.tabTextColorLock != 0)
                textView.setTextColor(themedata.tabTextColorLock);
            else
                textView.setTextColor(ResourcesCompat.getColor(res, R.color.tab_text_color_locked, theme));
        else if (themedata != null && themedata.tabTextColorNormal != 0)
            textView.setTextColor(themedata.tabTextColorNormal);
        else
            textView.setTextColor(ResourcesCompat.getColor(res, R.color.tab_text_color_normal, theme));
    }

    public void onMoveTabToForeground(Resources res, Resources.Theme theme) {
        ThemeData themedata = ThemeData.getInstance();
        if (themedata != null && themedata.tabBackgroundSelect != null)
            mTabView.setBackground(themedata.tabBackgroundSelect);
        else
            mTabView.setBackgroundResource(R.drawable.tab_background_selected);

        TextView textView = (TextView) mTabView.findViewById(R.id.textView);
        if (isNavLock())
            if (themedata != null && themedata.tabTextColorLock != 0)
                textView.setTextColor(themedata.tabTextColorLock);
            else
                textView.setTextColor(ResourcesCompat.getColor(res, R.color.tab_text_color_locked, theme));
        else if (themedata != null && themedata.tabTextColorSelect != 0)
            textView.setTextColor(themedata.tabTextColorSelect);
        else
            textView.setTextColor(ResourcesCompat.getColor(res, R.color.tab_text_color_selected, theme));
    }

    public void invalidateView(boolean isCurrent, Resources res, Resources.Theme theme) {
        if (isCurrent)
            onMoveTabToForeground(res, theme);
        else
            onMoveTabToBackground(res, theme);
    }

    private void setText(String text) {
        ((TextView) mTabView.findViewById(R.id.textView)).setText(text);
    }

    private final View mTabView;
}
