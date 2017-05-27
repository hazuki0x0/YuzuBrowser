package jp.hazuki.yuzubrowser.toolbar;

import android.content.Context;
import android.graphics.ColorFilter;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffColorFilter;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.StateListDrawable;
import android.support.v4.content.res.ResourcesCompat;
import android.util.StateSet;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import jp.hazuki.yuzubrowser.R;
import jp.hazuki.yuzubrowser.theme.ThemeData;

public class AbstractToolbar extends LinearLayout {
    private static ColorFilter THEME_IMAGE_COLOR_FILTER;
    private static Drawable THEME_BUTTON_BG;

    public AbstractToolbar(Context context) {
        super(context);
    }

    //themedata maybe null
    public void onThemeChanged(ThemeData themedata) {
        if (themedata != null && themedata.toolbarImageColor != 0)
            THEME_IMAGE_COLOR_FILTER = new PorterDuffColorFilter(themedata.toolbarImageColor, PorterDuff.Mode.SRC_ATOP);
        else
            THEME_IMAGE_COLOR_FILTER = null;

        if (themedata != null && themedata.toolbarButtonBackgroundPress != null) {
            Drawable press;
            press = themedata.toolbarButtonBackgroundPress;
            StateListDrawable drawable = new StateListDrawable();
            drawable.addState(new int[]{android.R.attr.state_pressed}, press);
            drawable.addState(StateSet.WILD_CARD, getResources().getDrawable(R.drawable.swipebtn_image_background_normal, getContext().getTheme()));
            THEME_BUTTON_BG = drawable;
        } else {
            THEME_BUTTON_BG = getResources().getDrawable(R.drawable.swipebtn_image_background, getContext().getTheme());
        }
        applyTheme(themedata);
    }

    public void applyTheme(ThemeData themedata) {
    }

    protected void applyThemeAutomatically(ThemeData themedata) {
        applyTheme(themedata, this);
    }

    protected void applyTheme(ThemeData themedata, ViewGroup viewgroup) {
        final int count = viewgroup.getChildCount();
        for (int i = 0; i < count; ++i) {
            View view = viewgroup.getChildAt(i);
            if (view instanceof ViewGroup)
                applyTheme(themedata, (ViewGroup) view);
            else if (view instanceof TextView)
                applyTheme(themedata, (TextView) view);
            else if (view instanceof ImageView)
                applyTheme(themedata, (ImageView) view);
        }
    }

    protected void applyTheme(ThemeData themedata, TextView textview) {
        if (themedata != null && themedata.toolbarTextColor != 0)
            textview.setTextColor(themedata.toolbarTextColor);
        else {
            textview.setTextColor(ResourcesCompat.getColor(getResources(), R.color.toolbar_text_color, getContext().getTheme()));
        }

    }

    protected static void applyTheme(ThemeData themedata, ImageView imageview) {
        imageview.setColorFilter(THEME_IMAGE_COLOR_FILTER);
    }

    protected void applyTheme(ThemeData themedata, Button button) {
        applyTheme(themedata, (TextView) button);
        button.setBackground(THEME_BUTTON_BG);
    }

    protected static void applyTheme(ThemeData themedata, ImageButton button) {
        applyTheme(themedata, (ImageView) button);
        button.setBackground(THEME_BUTTON_BG);
    }

    protected static void applyTheme(ThemeData themedata, ButtonToolbarController controller) {
        controller.setColorFilter(THEME_IMAGE_COLOR_FILTER);
        controller.setBackgroundDrawable(THEME_BUTTON_BG);
    }
}
