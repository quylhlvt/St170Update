package com.dragon.tribe.fire.oc.maker.ui.my_creation

import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.dragon.tribe.fire.oc.maker.base.AbsBaseAdapter
import com.dragon.tribe.fire.oc.maker.base.AbsBaseDiffCallBack
import com.dragon.tribe.fire.oc.maker.utils.hide
import com.dragon.tribe.fire.oc.maker.utils.onSingleClick
import com.dragon.tribe.fire.oc.maker.utils.shimmer
import com.dragon.tribe.fire.oc.maker.utils.show
import com.facebook.shimmer.ShimmerDrawable
import com.dragon.tribe.fire.oc.maker.R
import com.dragon.tribe.fire.oc.maker.databinding.ItemMyAvatarBinding

class AvatarAdapter :
    AbsBaseAdapter<String, ItemMyAvatarBinding>(R.layout.item_my_avatar, DiffCallBack()) {
    var onClick: ((Int, String) -> Unit)? = null
    var arrCheckTick = arrayListOf<Int>()
    var checkLongClick = false
    override fun bind(
        binding: ItemMyAvatarBinding,
        position: Int,
        data: String,
        holder: RecyclerView.ViewHolder
    ) {
        val shimmerDrawable = ShimmerDrawable().apply {
            setShimmer(shimmer)
        }
        Glide.with(binding.root).load(data).placeholder(shimmerDrawable)
           .into(binding.imvImage)
        binding.imvImage.onSingleClick {
            onClick?.invoke(position, "item")
        }
        binding.btnDelete.onSingleClick {
            onClick?.invoke(position, "delete")
        }
        binding.btnEdit.onSingleClick {
            onClick?.invoke(position, "edit")
        }
        binding.imvImage.setOnLongClickListener {
            onClick?.invoke(position, "longclick")
            true
        }
        binding.btnSelect.onSingleClick {
            onClick?.invoke(position, "tick")
        }
        if (checkLongClick) {
            binding.btnSelect.show()
            if (position in arrCheckTick) {
                binding.btnSelect.setImageResource(R.drawable.imv_check_true)
            } else {
                binding.btnSelect.setImageResource(R.drawable.imv_check_false)
            }
            binding.btnEdit.hide()
            binding.btnDelete.hide()
        } else {
            binding.btnSelect.hide()
            binding.btnEdit.show()
            binding.btnDelete.show()
        }

    }

    class DiffCallBack : AbsBaseDiffCallBack<String>() {
        override fun itemsTheSame(oldItem: String, newItem: String): Boolean {
            return oldItem == newItem
        }
        override fun contentsTheSame(
            oldItem: String,
            newItem: String
        ): Boolean {
            return oldItem != newItem
        }
    }
}