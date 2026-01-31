package com.dragon.tribe.fire.oc.maker.custom.listener.listenerdraw

import android.view.MotionEvent
import com.dragon.tribe.fire.oc.maker.custom.DrawView


interface DrawEvent {
    fun onActionDown(tattooView: DrawView?, event: MotionEvent?)
    fun onActionMove(tattooView: DrawView?, event: MotionEvent?)
    fun onActionUp(tattooView: DrawView?, event: MotionEvent?)
}