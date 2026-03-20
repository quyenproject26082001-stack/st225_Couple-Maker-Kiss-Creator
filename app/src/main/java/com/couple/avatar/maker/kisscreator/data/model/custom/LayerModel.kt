package com.couple.avatar.maker.kisscreator.data.model.custom

import com.couple.avatar.maker.kisscreator.data.model.custom.ColorModel

data class LayerModel(
    val image: String,
    val isMoreColors: Boolean = false,
    var listColor: ArrayList<ColorModel> = arrayListOf()
)