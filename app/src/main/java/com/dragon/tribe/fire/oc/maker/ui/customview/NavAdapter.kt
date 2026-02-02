package com.dragon.tribe.fire.oc.maker.ui.customview

import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.dragon.tribe.fire.oc.maker.base.AbsBaseAdapter
import com.dragon.tribe.fire.oc.maker.base.AbsBaseDiffCallBack
import com.dragon.tribe.fire.oc.maker.data.model.BodyPartModel
import com.dragon.tribe.fire.oc.maker.utils.onClickCustom
import com.dragon.tribe.fire.oc.maker.R
import com.dragon.tribe.fire.oc.maker.databinding.ItemNavigationBinding

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

        binding.apply {
            bg.strokeColor= if (posNav == position) ContextCompat.getColor(binding.root.context,R.color.app_color)else ContextCompat.getColor(binding.root.context,R.color.white)
        }
        binding.root.onClickCustom {
            onClick?.invoke(position)
        }
    }

}