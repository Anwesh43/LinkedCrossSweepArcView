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
