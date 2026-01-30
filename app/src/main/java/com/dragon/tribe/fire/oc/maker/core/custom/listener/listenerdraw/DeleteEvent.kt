package com.dragon.tribe.fire.oc.maker.core.custom.listener.listenerdraw

import android.view.MotionEvent
import com.dragon.tribe.fire.oc.maker.core.custom.listener.listenerdraw.DrawEvent
import com.dragon.tribe.fire.oc.maker.core.custom.DrawView

class DeleteEvent : DrawEvent {
    override fun onActionDown(tattooView: DrawView?, event: MotionEvent?) {}
    override fun onActionMove(tattooView: DrawView?, event: MotionEvent?) {}
    override fun onActionUp(tattooView: DrawView?, event: MotionEvent?) {
        if (!tattooView!!.isLocking()) {
            tattooView.removeDrawCurrent()
        }
    }
}
