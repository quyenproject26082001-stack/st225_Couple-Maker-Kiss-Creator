package com.temp.ui.choose_character

import com.temp.core.base.BaseAdapter
import com.temp.core.extensions.gone
import com.temp.core.extensions.loadImage
import com.temp.core.extensions.tap
import com.temp.data.model.custom.CustomizeModel
import com.temp.databinding.ItemChooseAvatarBinding

class ChooseCharacterAdapter : BaseAdapter<CustomizeModel, ItemChooseAvatarBinding>(ItemChooseAvatarBinding::inflate) {
    var onItemClick: ((position: Int) -> Unit) = {}
    override fun onBind(binding: ItemChooseAvatarBinding, item: CustomizeModel, position: Int) {
        binding.apply {
            loadImage(item.avatar, imvImage, onDismissLoading = {
                sflShimmer.stopShimmer()
                sflShimmer.gone()
            })
            root.tap { onItemClick.invoke(position) }
        }
    }
}