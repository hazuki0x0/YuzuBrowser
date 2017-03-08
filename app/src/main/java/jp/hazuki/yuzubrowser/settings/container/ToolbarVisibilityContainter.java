package jp.hazuki.yuzubrowser.settings.container;

public class ToolbarVisibilityContainter extends IntContainer {
    public ToolbarVisibilityContainter(String name, Integer def_value) {
        super(name, def_value);
    }

    public void setVisible(boolean visible) {
        if (visible)
            mValue |= 0x01;
        else
            mValue &= ~0x01;
    }

    public void setAlwaysVisible(boolean visible) {
        if (visible)
            mValue = 0x01;
        else
            mValue = 0;
    }

    public boolean isVisible() {
        return (mValue & 0x01) != 0;
    }

    public boolean isHideWhenFullscreen() {
        return (mValue & 0x02) != 0;
    }

    public boolean isHideWhenEndLoading() {
        return (mValue & 0x04) != 0;
    }

    public boolean isHideWhenPortrait() {
        return (mValue & 0x08) != 0;
    }

    public boolean isHideWhenLandscape() {
        return (mValue & 0x10) != 0;
    }

    public boolean isHideWhenLayoutShrink() {
        return (mValue & 0x20) != 0;
    }

    //if add, please also add MultiListIntPreference max
}