package com.example.crosssweeparcview

import android.view.View
import android.view.MotionEvent
import android.app.Activity
import android.content.Context
import android.graphics.Paint
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.RectF

val colors : Array<Int> = arrayOf(
    "#F44336",
    "#009688",
    "#FF9800",
    "#FF5722",
    "#3F51B5"
).map {
    Color.parseColor(it)
}.toTypedArray()
val parts : Int = 4
val scGap : Float = 0.02f / parts
val strokeFactor : Float = 90f
val sizeFactor : Float = 4.9f
val deg : Float = 90f
val delay : Long = 20
val backColor : Int = Color.parseColor("#BDBDBD")

fun Int.inverse() : Float = 1f / this
fun Float.maxScale(i : Int, n : Int) : Float = Math.max(0f, this - i * n.inverse())
fun Float.divideScale(i : Int, n : Int) : Float = Math.min(n.inverse(), maxScale(i, n)) * n
fun Float.sinify() : Float = Math.sin(this * Math.PI).toFloat()

fun Canvas.drawCrossSweepArc(scale : Float, w : Float, h : Float, paint : Paint) {
    val w : Float = width.toFloat()
    val h : Float = height.toFloat()
    val size : Float = Math.min(w, h) / sizeFactor
    val sf : Float = scale.sinify()
    save()
    translate(w / 2, h / 2)
    rotate(deg * sf.divideScale(2, parts))
    for (j in 0..1) {
        val lSize : Float = size * sf.divideScale(0, parts)
        save()
        rotate(90f * j)
        drawLine(0f, -lSize, 0f, lSize, paint)
        restore()
        save()
        drawArc(
            RectF(-size, -size, size, size),
            180f * j - deg,
            deg * sf.divideScale(1, parts),
            true,
            paint
        )
        restore()
    }
    restore()
}

fun Canvas.drawCSANode(i : Int, scale : Float, paint : Paint) {
    val w : Float = width.toFloat()
    val h : Float = height.toFloat()
    paint.color = colors[i]
    paint.strokeCap = Paint.Cap.ROUND
    paint.strokeWidth = Math.min(w, h) / strokeFactor
    drawCrossSweepArc(scale, w, h, paint)
}

class CrossSweepArcView(ctx : Context) : View(ctx) {

    private val renderer : Renderer = Renderer(this)

    override fun onDraw(canvas : Canvas) {
        renderer.render(canvas)
    }

    override fun onTouchEvent(event : MotionEvent) : Boolean {
        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                renderer.handleTap()
            }
        }
        return true
    }

    data class State(var scale : Float = 0f, var dir : Float = 0f, var prevScale : Float = 0f) {

        fun update(cb : (Float) -> Unit) {
            scale += scGap * dir
            if (Math.abs(scale - prevScale) > 1f) {
                scale = prevScale + dir
                dir = 0f
                prevScale = scale
                cb(prevScale)
            }
        }

        fun startUpdating(cb : () -> Unit) {
            if (dir == 0f) {
                dir = 1f - 2 * prevScale
                cb()
            }
        }
    }

    data class Animator(var view : View, var animated : Boolean = false) {

        fun animate(cb : () -> Unit) {
            if (animated) {
                cb()
                try {
                    Thread.sleep(delay)
                    view.invalidate()
                } catch(ex : Exception) {

                }
            }
        }

        fun start() {
            if (!animated) {
                animated = true
                view.postInvalidate()
            }
        }

        fun stop() {
            if (animated) {
                animated = false
            }
        }
    }

    data class CSANode(var i : Int, val state : State = State()) {

        private var next : CSANode? = null
        private var prev : CSANode? = null

        init {
            addNeighbor()
        }

        fun addNeighbor() {
            if (i < colors.size- 1) {
                next = CSANode(i + 1)
                next?.prev = this
            }
        }

        fun draw(canvas : Canvas, paint : Paint) {
            canvas.drawCSANode(i, state.scale, paint)
        }

        fun update(cb : (Float) -> Unit) {
            state.update(cb)
        }

        fun startUpdating(cb : () -> Unit) {
            state.startUpdating(cb)
        }

        fun getNext(dir : Int, cb : () -> Unit) : CSANode {
            var curr : CSANode? = prev
            if (dir == 1) {
                curr = next
            }
            if (curr != null) {
                return curr
            }
            cb()
            return this
        }
    }

    data class CrossSweepArc(var i : Int) {

        private var curr : CSANode = CSANode(0)
        private var dir : Int = 1

        fun draw(canvas : Canvas, paint : Paint) {
            curr.draw(canvas, paint)
        }

        fun update(cb : (Float) -> Unit) {
            curr.update {
                curr = curr.getNext(dir) {
                    dir *= -1
                }
                cb(it)
            }
        }

        fun startUpdating(cb : () -> Unit) {
            curr.startUpdating(cb)
        }
    }

    data class Renderer(var view : CrossSweepArcView) {

        private val csa : CrossSweepArc = CrossSweepArc(0)
        private val animator : Animator = Animator(view)
        private val paint : Paint = Paint(Paint.ANTI_ALIAS_FLAG)

        fun render(canvas : Canvas) {
            canvas.drawColor(backColor)
            csa.draw(canvas, paint)
            animator.animate {
                csa.update {
                    animator.stop()
                }
            }
        }

        fun handleTap() {
            csa.startUpdating {
                animator.start()
            }
        }
    }

    companion object {

        fun create(activity : Activity) : CrossSweepArcView {
            val view : CrossSweepArcView = CrossSweepArcView(activity)
            activity.setContentView(view)
            return view
        }
    }
}