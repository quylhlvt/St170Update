package com.dragon.tribe.fire.oc.maker.ui.background.adapter

import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.dragon.tribe.fire.oc.maker.base.AbsBaseAdapter
import com.dragon.tribe.fire.oc.maker.base.AbsBaseDiffCallBack
import com.dragon.tribe.fire.oc.maker.data.model.SelectedModel
import com.dragon.tribe.fire.oc.maker.utils.hide
import com.dragon.tribe.fire.oc.maker.utils.onSingleClick
import com.dragon.tribe.fire.oc.maker.utils.show
import com.dragon.tribe.fire.oc.maker.R
import com.dragon.tribe.fire.oc.maker.databinding.ItemImageBinding

class ImageAdapter :
    AbsBaseAdapter<SelectedModel, ItemImageBinding>(R.layout.item_image, DiffCallBack()) {
    var onClick: ((Int) -> Unit)? = null
    var posSelect = -1
    override fun bind(
        binding: ItemImageBinding,
        position: Int,
        data: SelectedModel,
        holder: RecyclerView.ViewHolder
    ) {
        binding.imvImage.onSingleClick {
            onClick?.invoke(position)
        }
        Glide.with(binding.root).load(data.path).into(binding.imvImage)
        if (position == 0) {
            binding.lnlAddItem.show()
        } else {
            binding.lnlAddItem.hide()
        }
        if (data.isSelected) {
            binding.vFocus.show()

        } else {
            binding.vFocus.hide()

        }
    }

    class DiffCallBack : AbsBaseDiffCallBack<SelectedModel>() {
        override fun itemsTheSame(
            oldItem: SelectedModel,
            newItem: SelectedModel
        ): Boolean {
            return oldItem == newItem
        }

        override fun contentsTheSame(
            oldItem: SelectedModel,
            newItem: SelectedModel
        ): Boolean {
            return oldItem.path != newItem.path || oldItem.isSelected != newItem.isSelected
        }

    }
}