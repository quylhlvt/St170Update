package com.ocmaker.fullbody.creator.custom.listener.listenerdraw

import android.view.MotionEvent
import com.ocmaker.fullbody.creator.custom.DrawView


interface DrawEvent {
    fun onActionDown(tattooView: DrawView?, event: MotionEvent?)
    fun onActionMove(tattooView: DrawView?, event: MotionEvent?)
    fun onActionUp(tattooView: DrawView?, event: MotionEvent?)
}