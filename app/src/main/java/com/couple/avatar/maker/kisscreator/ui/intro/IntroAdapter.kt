package com.couple.avatar.maker.kisscreator.ui.intro

import android.content.Context
import com.couple.avatar.maker.kisscreator.core.base.BaseAdapter
import com.couple.avatar.maker.kisscreator.core.extensions.loadImage
import com.couple.avatar.maker.kisscreator.core.extensions.select
import com.couple.avatar.maker.kisscreator.core.extensions.strings
import com.couple.avatar.maker.kisscreator.data.model.IntroModel
import com.couple.avatar.maker.kisscreator.databinding.ItemIntroBinding

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