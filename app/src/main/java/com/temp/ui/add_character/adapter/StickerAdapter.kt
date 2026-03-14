package com.temp.ui.add_character.adapter

import com.temp.core.base.BaseAdapter
import com.temp.core.extensions.loadImage
import com.temp.core.extensions.loadImageSticker
import com.temp.core.extensions.tap
import com.temp.data.model.SelectedModel
import com.temp.databinding.ItemStickerBinding

class StickerAdapter : BaseAdapter<SelectedModel, ItemStickerBinding>(ItemStickerBinding::inflate) {
    var onItemClick : ((String) -> Unit) = {}
    override fun onBind(binding: ItemStickerBinding, item: SelectedModel, position: Int) {
        binding.apply {
            loadImageSticker(root, item.path, imvSticker)
            root.tap { onItemClick.invoke(item.path) }
        }
    }
}