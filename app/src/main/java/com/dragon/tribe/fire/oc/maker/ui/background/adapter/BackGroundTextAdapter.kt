package com.dragon.tribe.fire.oc.maker.ui.background.adapter

import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.dragon.tribe.fire.oc.maker.base.AbsBaseAdapter
import com.dragon.tribe.fire.oc.maker.base.AbsBaseDiffCallBack
import com.dragon.tribe.fire.oc.maker.data.model.SelectedModel
import com.dragon.tribe.fire.oc.maker.utils.onSingleClick
import com.dragon.tribe.fire.oc.maker.R
import com.dragon.tribe.fire.oc.maker.databinding.ItemTextBgBinding

class BackGroundTextAdapter :
    AbsBaseAdapter<SelectedModel, ItemTextBgBinding>(R.layout.item_text_bg, DiffCallBack()) {
    var onClick: ((Int) -> Unit)? = null
    var posSelect1: Int = -1

    override fun bind(
        binding: ItemTextBgBinding,
        position: Int,
        data: SelectedModel,
        holder: RecyclerView.ViewHolder
    ) {
        binding.imv.onSingleClick {
            if (posSelect1 >= 0 && posSelect1 < currentList.size) {
                currentList[posSelect1].isSelected = false
            }

            data.isSelected = true
            posSelect1 = position
            notifyDataSetChanged()
            onClick?.invoke(position)
        }
        binding.apply {
            material.strokeColor= if (data.isSelected) ContextCompat.getColor(binding.root.context,R.color.app_color)else ContextCompat.getColor(binding.root.context,R.color.white)
        }
        Glide.with(binding.root).load(data.path)
            .override(512, 512)
            .encodeQuality(80)
            .diskCacheStrategy(DiskCacheStrategy.RESOURCE).into(binding.imv)
    }

    class DiffCallBack : AbsBaseDiffCallBack<SelectedModel>() {
        override fun itemsTheSame(
            oldItem: SelectedModel,
            newItem: SelectedModel
        ): Boolean {
            return oldItem.path == newItem.path
        }

        override fun contentsTheSame(
            oldItem: SelectedModel,
            newItem: SelectedModel
        ): Boolean {
            return oldItem.path != newItem.path || oldItem.isSelected != newItem.isSelected
        }

    }
}