/*
 * Copyright (C) 2017 Hazuki
 * fixed by Cynthia Project
 * Copyright (C) 2010 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *	  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package jp.hazuki.yuzubrowser.utils.view.pie;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Canvas;
import android.graphics.ColorFilter;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Point;
import android.graphics.PointF;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffColorFilter;
import android.graphics.RectF;
import android.support.v4.content.res.ResourcesCompat;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.SoundEffectConstants;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import java.util.ArrayList;
import java.util.List;

import jp.hazuki.yuzubrowser.R;

public class PieMenu extends FrameLayout {

    private static final int MAX_LEVELS = 5;

    public interface PieController {
        /**
         * called before menu opens to customize menu
         * returns if pie state has been changed
         */
        boolean onOpen();
    }

    /**
     * A view like object that lives off of the pie menu
     */
    public interface PieView {

        interface OnLayoutListener {
            void onLayout(int ax, int ay, boolean left);
        }

        void setLayoutListener(OnLayoutListener l);

        void layout(int anchorX, int anchorY, boolean onleft, float angle);

        void draw(Canvas c);

        boolean onTouchEvent(MotionEvent evt);

    }

    private Point mCenter;
    private int mRadius;
    private int mRadiusInc;
    private int mSlop;
    private int mTouchOffset;

    private int mPosition;
    private boolean mOpen;
    private PieController mController;

    private List<PieItem> mItems;
    private int mLevels;
    private int[] mCounts;
    private PieView mPieView = null;

    private Paint mNormalPaint;
    private Paint mSelectedPaint;

    // touch handling
    PieItem mCurrentItem;

    /**
     * @param context
     * @param attrs
     * @param defStyle
     */
    public PieMenu(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init(context);
    }

    /**
     * @param context
     * @param attrs
     */
    public PieMenu(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    /**
     * @param context
     */
    public PieMenu(Context context) {
        super(context);
        init(context);
    }

    private void init(Context ctx) {
        mItems = new ArrayList<>();
        mLevels = 0;
        mCounts = new int[MAX_LEVELS];
        Resources res = ctx.getResources();
        mRadius = (int) res.getDimension(R.dimen.qc_radius_start);
        mRadiusInc = (int) res.getDimension(R.dimen.qc_radius_increment);
        mSlop = (int) res.getDimension(R.dimen.qc_slop);
        mTouchOffset = (int) res.getDimension(R.dimen.qc_touch_offset);
        mOpen = false;
        setWillNotDraw(false);
        setDrawingCacheEnabled(false);
        mCenter = new Point(0, 0);
        mNormalPaint = new Paint();
        mNormalPaint.setColor(ResourcesCompat.getColor(res, R.color.qc_normal, ctx.getTheme()));
        mNormalPaint.setAntiAlias(true);
        mSelectedPaint = new Paint();
        mSelectedPaint.setColor(ResourcesCompat.getColor(res, R.color.qc_selected, ctx.getTheme()));
        mSelectedPaint.setAntiAlias(true);
    }

    public void setController(PieController ctl) {
        mController = ctl;
    }

    public void addItem(PieItem item) {
        // add the item to the pie itself
        mItems.add(item);
        int l = item.getLevel();
        mLevels = Math.max(mLevels, l);
        mCounts[l]++;
    }

    public void removeItem(PieItem item) {
        mItems.remove(item);
    }

    public void clearItems() {
        mItems.clear();
    }

    private boolean onTheLeft() {
        return mCenter.x < mSlop;
    }

    /**
     * guaranteed has center set
     *
     * @param show
     */
    private void show(boolean show) {
        mOpen = show;
        if (mOpen) {
            if (mController != null) {
                mController.onOpen();
            }
            layoutPie();
        }
        if (!show) {
            mCurrentItem = null;
            mPieView = null;
        }
        invalidate();
    }

    private void setCenter(int x, int y) {
        if (x < mSlop) {

            mCenter.x = 0;
        } else {
            mCenter.x = getWidth();
        }
        mCenter.y = y;
    }

    private void layoutPie() {
        float emptyangle = (float) Math.PI / 16;
        int rgap = 2;
        int inner = mRadius + rgap;
        int outer = mRadius + mRadiusInc - rgap;
        int gap = 1;
        for (int i = 0; i < mLevels; i++) {
            int level = i + 1;
            float sweep = (float) (Math.PI - 2 * emptyangle) / mCounts[level];
            float angle = emptyangle + sweep / 2;
            for (PieItem item : mItems) {
                if (item.getLevel() == level) {
                    View view = item.getView();
                    int w = view.getLayoutParams().width;
                    int h = view.getLayoutParams().height;
                    int r = inner + (outer - inner) * 55 / 100;
                    int x = (int) (r * Math.sin(angle));
                    int y = mCenter.y - (int) (r * Math.cos(angle)) - h / 2;
                    if (onTheLeft()) {
                        x = mCenter.x + x - w / 2;
                    } else {
                        x = mCenter.x - x - w / 2;
                    }
                    view.layout(x, y, x + w, y + h);
                    float itemstart = angle - sweep / 2;
                    Path slice = makeSlice(getDegrees(itemstart) - gap,
                            getDegrees(itemstart + sweep) + gap,
                            outer, inner, mCenter);
                    item.setGeometry(itemstart, sweep, inner, outer, slice);
                    angle += sweep;
                }
            }
            inner += mRadiusInc;
            outer += mRadiusInc;
        }
    }


    /**
     * converts a
     *
     * @param angle from 0..PI to Android degrees (clockwise starting at 3
     *              o'clock)
     * @return skia angle
     */
    private float getDegrees(double angle) {
        return (float) (270 - 180 * angle / Math.PI);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        if (mOpen) {
            int state;
            for (PieItem item : mItems) {
                Paint p = item.isSelected() ? mSelectedPaint : mNormalPaint;
                state = canvas.save();
                if (onTheLeft()) {
                    canvas.scale(-1, 1);
                }
                drawPath(canvas, item.getPath(), p);
                canvas.restoreToCount(state);
                drawItem(canvas, item);
            }
            if (mPieView != null) {
                mPieView.draw(canvas);
            }
        }
    }

    private void drawItem(Canvas canvas, PieItem item) {
        // draw the item view
        View view = item.getView();
        int state = canvas.save();
        canvas.translate(view.getX(), view.getY());
        view.draw(canvas);
        canvas.restoreToCount(state);
    }

    private void drawPath(Canvas canvas, Path path, Paint paint) {
        canvas.drawPath(path, paint);
    }

    private Path makeSlice(float start, float end, int outer, int inner, Point center) {
        RectF bb =
                new RectF(center.x - outer, center.y - outer, center.x + outer,
                        center.y + outer);
        RectF bbi =
                new RectF(center.x - inner, center.y - inner, center.x + inner,
                        center.y + inner);
        Path path = new Path();
        path.arcTo(bb, start, end - start, true);
        path.arcTo(bbi, end, start - end);
        path.close();
        return path;
    }

    // touch handling for pie

    @Override
    public boolean onTouchEvent(MotionEvent evt) {
        float x = evt.getX();
        float y = evt.getY();
        int action = evt.getActionMasked();
        if (MotionEvent.ACTION_DOWN == action) {
            if ((mPosition != 1 && x > getWidth() - mSlop) || (mPosition != 2 && x < mSlop)) {
                setCenter((int) x, (int) y);
                show(true);
                return true;
            }
        } else if (MotionEvent.ACTION_UP == action) {
            if (mOpen) {
                boolean handled = false;
                if (mPieView != null) {
                    handled = mPieView.onTouchEvent(evt);
                }
                PieItem item = mCurrentItem;
                deselect();
                show(false);
                if (!handled && (item != null)) {
                    item.getView().performClick();
                }
                return true;
            }
        } else if (MotionEvent.ACTION_CANCEL == action) {
            if (mOpen) {
                show(false);
            }
            deselect();
            return false;
        } else if (MotionEvent.ACTION_MOVE == action) {
            boolean handled = false;
            PointF polar = getPolar(x, y);
            int maxr = mRadius + mLevels * mRadiusInc + 50;
            if (mPieView != null) {
                handled = mPieView.onTouchEvent(evt);
            }
            if (handled) {
                invalidate();
                return false;
            }
            if (polar.y > maxr) {
                deselect();
                show(false);
                evt.setAction(MotionEvent.ACTION_DOWN);
                if (getParent() != null) {
                    ((ViewGroup) getParent()).dispatchTouchEvent(evt);
                }
                return false;
            }
            PieItem item = findItem(polar);
            if (mCurrentItem != item) {
                onEnter(item);
                if ((item != null) && item.isPieView()) {
                    int cx = item.getView().getLeft() + (onTheLeft()
                            ? item.getView().getWidth() : 0);
                    int cy = item.getView().getTop();
                    mPieView = item.getPieView();
                    layoutPieView(mPieView, cx, cy,
                            (item.getStartAngle() + item.getSweep()) / 2);
                }
                invalidate();
            }
        }
        // always re-dispatch event
        return false;
    }

    private void layoutPieView(PieView pv, int x, int y, float angle) {
        pv.layout(x, y, onTheLeft(), angle);
    }

    /**
     * enter a slice for a view
     * updates model only
     *
     * @param item
     */
    private void onEnter(PieItem item) {
        // deselect
        if (mCurrentItem != null) {
            mCurrentItem.setSelected(false);
        }
        if (item != null) {
            // clear up stack
            playSoundEffect(SoundEffectConstants.CLICK);
            item.setSelected(true);
            mPieView = null;
        }
        mCurrentItem = item;
    }

    private void deselect() {
        if (mCurrentItem != null) {
            mCurrentItem.setSelected(false);
        }
        mCurrentItem = null;
        mPieView = null;
    }

    private PointF getPolar(float x, float y) {
        PointF res = new PointF();
        // get angle and radius from x/y
        res.x = (float) Math.PI / 2;
        x = mCenter.x - x;
        if (mCenter.x < mSlop) {
            x = -x;
        }
        y = mCenter.y - y;
        res.y = (float) Math.sqrt(x * x + y * y);
        if (y > 0) {
            res.x = (float) Math.asin(x / res.y);
        } else if (y < 0) {
            res.x = (float) (Math.PI - Math.asin(x / res.y));
        }
        return res;
    }

    /**
     * @param polar x: angle, y: dist
     * @return the item at angle/dist or null
     */
    private PieItem findItem(PointF polar) {
        // find the matching item:
        for (PieItem item : mItems) {
            if ((item.getInnerRadius() - mTouchOffset < polar.y)
                    && (item.getOuterRadius() - mTouchOffset > polar.y)
                    && (item.getStartAngle() < polar.x)
                    && (item.getStartAngle() + item.getSweep() > polar.x)) {
                return item;
            }
        }
        return null;
    }

    public void notifyChangeState() {
        for (PieItem item : mItems) {
            item.notifyChangeState();
        }
    }

    public void setRadiusStart(int radius) {
        this.mRadius = radius;
    }

    public void setRadiusIncrement(int radiusInc) {
        this.mRadiusInc = radiusInc;
    }

    public void setSlop(int slop) {
        this.mSlop = slop;
    }

    public void setPosition(int position) {
        this.mPosition = position;
    }

    public void setTouchOffset(int touchOffset) {
        this.mTouchOffset = touchOffset;
    }

    public void setNormalColor(int color) {
        mNormalPaint.setColor(color);
    }

    public void setSelectedColor(int color) {
        mSelectedPaint.setColor(color);
    }

    public void setColorFilterToItems(int color) {
        ColorFilter cf = (color != 0) ? new PorterDuffColorFilter(color, PorterDuff.Mode.MULTIPLY) : null;
        for (PieItem item : mItems) {
            item.getView().setColorFilter(cf);
        }
    }
}
