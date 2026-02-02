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
import com.dragon.tribe.fire.oc.maker.databinding.ItemStikerBgBinding

class StikerAdapter :
    AbsBaseAdapter<SelectedModel, ItemStikerBgBinding>(
        R.layout.item_stiker_bg,
        DiffCallBack()
    ) {
    var onClick: ((String) -> Unit)? = null
    var posSelect: Int = -1
    override fun bind(
        binding: ItemStikerBgBinding,
        position: Int,
        data: SelectedModel,
        holder: RecyclerView.ViewHolder
    ) {
        binding.imv.onSingleClick {
            if (posSelect >= 0 && posSelect < currentList.size) {
                currentList[posSelect].isSelected = false
            }

            data.isSelected = true
            posSelect = position
            notifyDataSetChanged()
            onClick?.invoke(data.path)
        }
        binding.apply {
            material.strokeColor= if (data.isSelected) ContextCompat.getColor(binding.root.context,R.color.app_color)else ContextCompat.getColor(binding.root.context,R.color.white)
        }
        Glide.with(binding.root).load(data.path)
            .override(512, 512)
            .encodeQuality(50)
            .diskCacheStrategy(DiskCacheStrategy.RESOURCE).into(binding.imv)
    }

    class DiffCallBack :
        AbsBaseDiffCallBack<SelectedModel>() {
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