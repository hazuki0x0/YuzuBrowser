/*
 * Copyright (C) 2017-2019 Hazuki
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package jp.hazuki.yuzubrowser.legacy.utils.view.pie

import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.SoundEffectConstants
import android.view.ViewGroup
import android.widget.FrameLayout
import jp.hazuki.yuzubrowser.core.utility.extensions.dimension
import jp.hazuki.yuzubrowser.core.utility.extensions.getResColor
import jp.hazuki.yuzubrowser.legacy.R
import java.util.*

class PieMenu @JvmOverloads constructor(
        context: Context,
        attrs: AttributeSet? = null,
        defStyleAttr: Int = 0
) : FrameLayout(context, attrs, defStyleAttr) {

    private val center = Point(0, 0)
    private var radius: Int = 0
    private var radiusInc: Int = 0
    private var slop: Int = 0
    private var touchOffset: Int = 0

    private var position: Int = 0
    var isOpen: Boolean = false
        private set
    private var controller: PieController? = null

    private val items = ArrayList<PieItem>()
    private var levels: Int = 0
    private val counts = IntArray(MAX_LEVELS)
    private var pieView: PieView? = null

    private val normalPaint = Paint()
    private val selectedPaint = Paint()

    // touch handling
    internal var currentItem: PieItem? = null

    interface PieController {
        /**
         * called before menu opens to customize menu
         * returns if pie state has been changed
         */
        fun onOpen(): Boolean
    }

    /**
     * A view like object that lives off of the pie menu
     */
    interface PieView {

        interface OnLayoutListener {
            fun onLayout(ax: Int, ay: Int, left: Boolean)
        }

        fun setLayoutListener(l: OnLayoutListener)

        fun layout(anchorX: Int, anchorY: Int, onleft: Boolean, angle: Float)

        fun draw(c: Canvas)

        fun onTouchEvent(evt: MotionEvent): Boolean

    }

    init {
        radius = context.dimension(R.dimen.qc_radius_start)
        radiusInc = context.dimension(R.dimen.qc_radius_increment)
        slop = context.dimension(R.dimen.qc_slop)
        touchOffset = context.dimension(R.dimen.qc_touch_offset)
        setWillNotDraw(false)
        isDrawingCacheEnabled = false
        normalPaint.color = context.getResColor(R.color.qc_normal)
        normalPaint.isAntiAlias = true
        selectedPaint.color = context.getResColor(R.color.qc_selected)
        selectedPaint.isAntiAlias = true
    }

    fun setController(ctl: PieController) {
        controller = ctl
    }

    fun addItem(item: PieItem) {
        // add the item to the pie itself
        items.add(item)
        val l = item.level
        levels = Math.max(levels, l)
        counts[l]++
    }

    fun removeItem(item: PieItem) {
        items.remove(item)
        counts[item.level]--
    }

    fun clearItems() {
        items.clear()
        levels = 0
        for (i in 0 until MAX_LEVELS) {
            counts[i] = 0
        }
    }

    private fun onTheLeft(): Boolean {
        return center.x < slop
    }

    /**
     * guaranteed has center set
     *
     * @param show
     */
    private fun show(show: Boolean) {
        isOpen = show
        if (isOpen) {
            if (controller != null) {
                controller!!.onOpen()
            }
            layoutPie()
        }
        if (!show) {
            currentItem = null
            pieView = null
        }
        invalidate()
    }

    private fun setCenter(x: Int, y: Int) {
        if (x < slop) {

            center.x = 0
        } else {
            center.x = width
        }
        center.y = y
    }

    private fun layoutPie() {
        val emptyangle = Math.PI.toFloat() / 16
        val rgap = 2
        var inner = radius + rgap
        var outer = radius + radiusInc - rgap
        val gap = 1
        for (i in 0 until levels) {
            val level = i + 1
            val sweep = (Math.PI - 2 * emptyangle).toFloat() / counts[level]
            var angle = emptyangle + sweep / 2
            for (item in items) {
                if (item.level == level) {
                    val view = item.view
                    val w = view.layoutParams.width
                    val h = view.layoutParams.height
                    val r = inner + (outer - inner) * 55 / 100
                    var x = (r * Math.sin(angle.toDouble())).toInt()
                    val y = center.y - (r * Math.cos(angle.toDouble())).toInt() - h / 2
                    x = if (onTheLeft()) {
                        center.x + x - w / 2
                    } else {
                        center.x - x - w / 2
                    }
                    view.layout(x, y, x + w, y + h)
                    val itemstart = angle - sweep / 2
                    val slice = makeSlice(getDegrees(itemstart.toDouble()) - gap,
                            getDegrees((itemstart + sweep).toDouble()) + gap,
                            outer, inner, center)
                    item.setGeometry(itemstart, sweep, inner, outer, slice)
                    angle += sweep
                }
            }
            inner += radiusInc
            outer += radiusInc
        }
    }


    /**
     * converts a
     *
     * @param angle from 0..PI to Android degrees (clockwise starting at 3
     * o'clock)
     * @return skia angle
     */
    private fun getDegrees(angle: Double): Float {
        return (270 - 180 * angle / Math.PI).toFloat()
    }

    override fun onDraw(canvas: Canvas) {
        if (isOpen) {
            var state: Int
            for (item in items) {
                val p = if (item.isSelected) selectedPaint else normalPaint
                state = canvas.save()
                if (onTheLeft()) {
                    canvas.scale(-1f, 1f)
                }
                drawPath(canvas, item.path, p)
                canvas.restoreToCount(state)
                drawItem(canvas, item)
            }
            if (pieView != null) {
                pieView!!.draw(canvas)
            }
        }
    }

    private fun drawItem(canvas: Canvas, item: PieItem) {
        // draw the item view
        val view = item.view
        val state = canvas.save()
        canvas.translate(view.x, view.y)
        view.draw(canvas)
        canvas.restoreToCount(state)
    }

    private fun drawPath(canvas: Canvas, path: Path?, paint: Paint?) {
        canvas.drawPath(path!!, paint!!)
    }

    private fun makeSlice(start: Float, end: Float, outer: Int, inner: Int, center: Point?): Path {
        val bb = RectF((center!!.x - outer).toFloat(), (center.y - outer).toFloat(), (center.x + outer).toFloat(),
                (center.y + outer).toFloat())
        val bbi = RectF((center.x - inner).toFloat(), (center.y - inner).toFloat(), (center.x + inner).toFloat(),
                (center.y + inner).toFloat())
        val path = Path()
        path.arcTo(bb, start, end - start, true)
        path.arcTo(bbi, end, start - end)
        path.close()
        return path
    }

    // touch handling for pie

    override fun onTouchEvent(evt: MotionEvent): Boolean {
        val x = evt.x
        val y = evt.y
        val action = evt.actionMasked
        if (MotionEvent.ACTION_DOWN == action) {
            if (position != 1 && x > width - slop || position != 2 && x < slop) {
                setCenter(x.toInt(), y.toInt())
                show(true)
                return true
            }
        } else if (MotionEvent.ACTION_UP == action) {
            if (isOpen) {
                val handled = pieView?.onTouchEvent(evt) ?: false
                val item = currentItem
                deselect()
                show(false)
                if (!handled && item != null) {
                    item.view.performClick()
                }
                return true
            }
        } else if (MotionEvent.ACTION_CANCEL == action) {
            if (isOpen) {
                show(false)
            }
            deselect()
            return false
        } else if (MotionEvent.ACTION_MOVE == action) {
            val handled = pieView?.onTouchEvent(evt) ?: false
            val polar = getPolar(x, y)
            val maxr = radius + levels * radiusInc + 50
            if (handled) {
                invalidate()
                return false
            }
            if (polar.y > maxr) {
                deselect()
                show(false)
                evt.action = MotionEvent.ACTION_DOWN
                if (parent != null) {
                    (parent as ViewGroup).dispatchTouchEvent(evt)
                }
                return false
            }
            val item = findItem(polar)
            if (currentItem != item) {
                onEnter(item)
                if (item != null && item.isPieView) {
                    val cx = item.view.left + if (onTheLeft())
                        item.view.width
                    else
                        0
                    val cy = item.view.top
                    pieView = item.pieView
                    layoutPieView(pieView!!, cx, cy,
                            (item.startAngle + item.sweep) / 2)
                }
                invalidate()
            }
        }
        // always re-dispatch event
        return false
    }

    private fun layoutPieView(pv: PieView, x: Int, y: Int, angle: Float) {
        pv.layout(x, y, onTheLeft(), angle)
    }

    /**
     * enter a slice for a view
     * updates model only
     *
     * @param item
     */
    private fun onEnter(item: PieItem?) {
        // deselect
        currentItem?.isSelected = false
        if (item != null) {
            // clear up stack
            playSoundEffect(SoundEffectConstants.CLICK)
            item.isSelected = true
            pieView = null
        }
        currentItem = item
    }

    private fun deselect() {
        currentItem?.isSelected = false
        currentItem = null
        pieView = null
    }

    private fun getPolar(tx: Float, ty: Float): PointF {
        var x = tx
        var y = ty
        val res = PointF()
        // get angle and radius from x/y
        res.x = Math.PI.toFloat() / 2
        x = center.x - x
        if (center.x < slop) {
            x = -x
        }
        y = center.y - y
        res.y = Math.sqrt((x * x + y * y).toDouble()).toFloat()
        if (y > 0) {
            res.x = Math.asin((x / res.y).toDouble()).toFloat()
        } else if (y < 0) {
            res.x = (Math.PI - Math.asin((x / res.y).toDouble())).toFloat()
        }
        return res
    }

    /**
     * @param polar x: angle, y: dist
     * @return the item at angle/dist or null
     */
    private fun findItem(polar: PointF): PieItem? {
        // find the matching item:
        return items.firstOrNull {
            it.innerRadius - touchOffset < polar.y
                    && it.outerRadius - touchOffset > polar.y
                    && it.startAngle < polar.x
                    && it.startAngle + it.sweep > polar.x
        }
    }

    fun notifyChangeState() {
        for (item in items) {
            item.notifyChangeState()
        }
    }

    fun setRadiusStart(radius: Int) {
        this.radius = radius
    }

    fun setRadiusIncrement(radiusInc: Int) {
        this.radiusInc = radiusInc
    }

    fun setSlop(slop: Int) {
        this.slop = slop
    }

    fun setPosition(position: Int) {
        this.position = position
    }

    fun setTouchOffset(touchOffset: Int) {
        this.touchOffset = touchOffset
    }

    fun setNormalColor(color: Int) {
        normalPaint.color = color
    }

    fun setSelectedColor(color: Int) {
        selectedPaint.color = color
    }

    fun setColorFilterToItems(color: Int) {
        val cf = if (color != 0) PorterDuffColorFilter(color, PorterDuff.Mode.MULTIPLY) else null
        for (item in items) {
            item.view.colorFilter = cf
        }
    }

    companion object {

        private const val MAX_LEVELS = 5
    }
}
