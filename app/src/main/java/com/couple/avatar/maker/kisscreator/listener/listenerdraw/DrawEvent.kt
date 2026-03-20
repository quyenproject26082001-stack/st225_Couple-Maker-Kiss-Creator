package com.couple.avatar.maker.kisscreator.listener.listenerdraw

import android.view.MotionEvent
import com.couple.avatar.maker.kisscreator.core.custom.drawview.DrawView


interface DrawEvent {
    fun onActionDown(tattooView: DrawView?, event: MotionEvent?)
    fun onActionMove(tattooView: DrawView?, event: MotionEvent?)
    fun onActionUp(tattooView: DrawView?, event: MotionEvent?)
}