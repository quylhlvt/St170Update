package com.ocmaker.fullbody.creator.ui.customview

import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.ocmaker.fullbody.creator.R
import com.ocmaker.fullbody.creator.base.AbsBaseAdapter
import com.ocmaker.fullbody.creator.data.model.BodyPartModel
import com.ocmaker.fullbody.creator.databinding.ItemNavigationBinding
import com.ocmaker.fullbody.creator.utils.onClickCustom
import com.ocmaker.fullbody.creator.base.AbsBaseDiffCallBack

class NavAdapter : AbsBaseAdapter<BodyPartModel, ItemNavigationBinding>(R.layout.item_navigation, DiffNav()) {
    var posNav = 0
    var onClick: ((Int) -> Unit)? = null

    class DiffNav : AbsBaseDiffCallBack<BodyPartModel>() {
        override fun itemsTheSame(oldItem: BodyPartModel, newItem: BodyPartModel): Boolean {
            return oldItem.icon == newItem.icon
        }

        override fun contentsTheSame(oldItem: BodyPartModel, newItem: BodyPartModel): Boolean {
            return oldItem.icon != newItem.icon
        }

    }

    fun setPos(pos: Int) {
        posNav = pos
    }

    override fun bind(
        binding: ItemNavigationBinding,
        position: Int,
        data: BodyPartModel,
        holder: RecyclerView.ViewHolder
    ) {
        Glide.with(binding.root).load(data.icon).encodeQuality(90).override(256).diskCacheStrategy(DiskCacheStrategy.RESOURCE).into(binding.imv)
        if (posNav == position) {
            binding.bg.setCardBackgroundColor(ContextCompat.getColor(binding.root.context,R.color.FFCC00))
        } else {
            binding.bg.setCardBackgroundColor(ContextCompat.getColor(binding.root.context,R.color.white))
        }
        binding.root.onClickCustom {
            onClick?.invoke(position)
        }
    }

}