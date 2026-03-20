package com.couple.avatar.maker.kisscreator.core.custom.layout

import android.widget.ImageView
import com.couple.avatar.maker.kisscreator.core.custom.imageview.StrokeImageView

interface EventRatioFrame {
    fun onImageClick(image: StrokeImageView, btnEdit: ImageView)
}