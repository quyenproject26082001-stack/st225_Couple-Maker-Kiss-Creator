package com.temp.ui.intro

import android.content.Context
import com.temp.core.base.BaseAdapter
import com.temp.core.extensions.loadImage
import com.temp.core.extensions.select
import com.temp.core.extensions.strings
import com.temp.data.model.IntroModel
import com.temp.databinding.ItemIntroBinding

class IntroAdapter(val context: Context) : BaseAdapter<IntroModel, ItemIntroBinding>(
    ItemIntroBinding::inflate
) {
    override fun onBind(binding: ItemIntroBinding, item: IntroModel, position: Int) {
        binding.apply {
            loadImage(root, item.image, imvImage, false)
            tvContent.text = context.strings(item.content)
            tvContent.select()
        }
    }
}