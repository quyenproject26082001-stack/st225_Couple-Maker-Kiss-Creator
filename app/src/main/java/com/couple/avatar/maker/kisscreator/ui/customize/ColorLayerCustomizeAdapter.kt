package com.couple.avatar.maker.kisscreator.ui.customize

import android.content.Context
import androidx.core.graphics.toColorInt
import androidx.core.view.isVisible
import androidx.recyclerview.widget.RecyclerView
import com.couple.avatar.maker.kisscreator.core.base.BaseAdapter
import com.couple.avatar.maker.kisscreator.core.extensions.tap
import com.couple.avatar.maker.kisscreator.data.model.custom.ItemColorModel
import com.couple.avatar.maker.kisscreator.databinding.ItemColorBinding

class ColorLayerCustomizeAdapter(val context: Context) :
    BaseAdapter<ItemColorModel, ItemColorBinding>(ItemColorBinding::inflate) {
    var onItemClick: ((Int) -> Unit) = {}
    override fun onBind(binding: ItemColorBinding, item: ItemColorModel, position: Int) {
        binding.apply {
            imvImage.setBackgroundColor(item.color.toColorInt())
            imvFocus.isVisible = item.isSelected
            root.tap {
                val rv = root.parent as? RecyclerView ?: return@tap
                val currentPosition = rv.getChildAdapterPosition(root)
                if (currentPosition != RecyclerView.NO_POSITION) {
                    onItemClick.invoke(currentPosition)
                }
            }
        }
    }
}