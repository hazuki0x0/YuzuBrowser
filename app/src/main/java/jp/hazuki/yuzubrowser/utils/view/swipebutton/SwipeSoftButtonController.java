package jp.hazuki.yuzubrowser.utils.view.swipebutton;

import android.content.Context;
import android.graphics.drawable.Drawable;

import jp.hazuki.yuzubrowser.action.ActionCallback;
import jp.hazuki.yuzubrowser.action.manager.SoftButtonActionFile;

public class SwipeSoftButtonController extends SwipeController {
    private ActionCallback mCallback = null;
    private SoftButtonActionFile mActionList = null;
    //private Drawable mBackgroundDrawable;

    public SwipeSoftButtonController(Context context) {
        super(context);
    }

    public void setActionData(SoftButtonActionFile actionlist, ActionCallback callback) {
        mActionList = actionlist;
        mCallback = callback;
    }

    public Drawable getIcon() {
        return getIcon(getCurrentWhatNo());
    }

    public Drawable getIcon(int whatNo) {
        if (mCallback == null) return null;
        switch (whatNo) {
            case SWIPE_CANCEL:
            case SWIPE_PRESS:
                return mCallback.getIcon(mActionList.press);
            case SWIPE_LPRESS:
                return mCallback.getIcon(mActionList.lpress);
            case SWIPE_UP:
                return mCallback.getIcon(mActionList.up);
            case SWIPE_DOWN:
                return mCallback.getIcon(mActionList.down);
            case SWIPE_LEFT:
                return mCallback.getIcon(mActionList.left);
            case SWIPE_RIGHT:
                return mCallback.getIcon(mActionList.right);
        }
        return null;
    }

    public Drawable getDefaultIcon() {
        if (mCallback == null) return null;
        return mCallback.getIcon(mActionList.press);
    }

    @Override
    protected void onEventActionUp(int whatNo) {
        //mBackgroundDrawable.setState(STATE_NOTHING);
        if (mActionList != null && mCallback != null) {
            switch (whatNo) {
                case SWIPE_PRESS:
                    mCallback.run(mActionList.press);
                    break;
                case SWIPE_LPRESS:
                    //mCallback.run(mActionList.lpress);
                    break;
                case SWIPE_UP:
                    mCallback.run(mActionList.up);
                    break;
                case SWIPE_DOWN:
                    mCallback.run(mActionList.down);
                    break;
                case SWIPE_LEFT:
                    mCallback.run(mActionList.left);
                    break;
                case SWIPE_RIGHT:
                    mCallback.run(mActionList.right);
                    break;
            }
        }
    }

    protected void onEventActionDown() {
        //mBackgroundDrawable.setState(STATE_PRESSED);
    }

    protected void onEventCancel() {
        //mBackgroundDrawable.setState(STATE_NOTHING);
    }

    protected void onEventOutSide() {
        //mBackgroundDrawable.setState(STATE_NOTHING);
    }

    @Override
    protected void onEventLongPress() {
        if (mActionList != null && mCallback != null) {
            mCallback.run(mActionList.lpress);
        }
    }

    public boolean shouldShow() {
        return !mActionList.press.isEmpty();
    }

    /*public void setBackgroundDrawable(Drawable background) {
        mBackgroundDrawable = background;
	}
	
	private static final int[] STATE_NOTHING = new int[] {0};
	private static final int[] STATE_PRESSED = new int[] {android.R.attr.state_pressed};
	
	public void getBackgroundDrawable(Drawable background) {
		mBackgroundDrawable = background;
		mBackgroundDrawable.setState(STATE_NOTHING);
	}
	
	public Drawable getCurrentBackground() {
		return mBackgroundDrawable.getCurrent();
	}*/
}
