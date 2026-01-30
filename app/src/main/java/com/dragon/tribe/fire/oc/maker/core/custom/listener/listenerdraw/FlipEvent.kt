package com.dragon.tribe.fire.oc.maker.core.custom.listener.listenerdraw

import android.view.MotionEvent
import com.dragon.tribe.fire.oc.maker.core.custom.DrawKey
import com.dragon.tribe.fire.oc.maker.core.custom.DrawView


class FlipEvent : DrawEvent {
    override fun onActionDown(tattooView: DrawView?, event: MotionEvent?) {}
    override fun onActionMove(tattooView: DrawView?, event: MotionEvent?) {}
    override fun onActionUp(tattooView: DrawView?, event: MotionEvent?) {
        if (tattooView != null && tattooView.getStickerCount() > 0) tattooView.flipCurrentDraw(
            DrawKey.FLIP_HORIZONTALLY)
    }
}