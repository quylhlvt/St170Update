package com.dragon.tribe.fire.oc.maker.ui.customize

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions
import com.dragon.tribe.fire.oc.maker.R
import com.dragon.tribe.fire.oc.maker.core.base.BaseAdapter
import com.dragon.tribe.fire.oc.maker.core.extensions.gone
import com.dragon.tribe.fire.oc.maker.core.extensions.onSingleClick
import com.dragon.tribe.fire.oc.maker.core.extensions.visible
import com.dragon.tribe.fire.oc.maker.core.utils.DataLocal
import com.dragon.tribe.fire.oc.maker.core.utils.key.AssetsKey
import com.dragon.tribe.fire.oc.maker.data.custom.ItemNavCustomModel
import com.dragon.tribe.fire.oc.maker.databinding.ItemCusBinding
import com.facebook.shimmer.ShimmerDrawable

class CustomizeLayerAdapter(val context: Context) : ListAdapter<ItemNavCustomModel, CustomizeLayerAdapter.CustomizeViewHolder>(DiffCallback) {

    var onItemClick: ((ItemNavCustomModel, Int) -> Unit) = { _, _ -> }
    var onNoneClick: ((Int) -> Unit) = {}
    var onRandomClick: (() -> Unit) = {}

    inner class CustomizeViewHolder(val binding: ItemCusBinding) : RecyclerView.ViewHolder(binding.root){
        fun bind(item: ItemNavCustomModel, position: Int) {
            binding.apply {
                val shimmerDrawable = ShimmerDrawable().apply {
                    setShimmer(DataLocal.shimmer)
                }

                vFocus.isVisible = item.isSelected
                Glide.with(root).load(item.path).placeholder(shimmerDrawable).into(imvImage)

                when (item.path) {
                    AssetsKey.NONE_LAYER -> {
                        btnNone.visible()
                        btnRandom.gone()
                        imvImage.gone()
                    }
                    AssetsKey.RANDOM_LAYER -> {
                        btnNone.gone()
                        btnRandom.visible()
                        imvImage.gone()
                    }
                    else -> {
                        btnNone.gone()
                        imvImage.visible()
                        btnRandom.gone()
                        Glide.with(root).load(item.path).placeholder(shimmerDrawable).into(imvImage)
                    }
                }

                binding.imvImage.onSingleClick(100) { onItemClick.invoke(item, position) }

                binding.btnRandom.onSingleClick { onRandomClick.invoke() }

                binding.btnNone.onSingleClick { onNoneClick.invoke(position) }
            }

        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CustomizeViewHolder {
        return CustomizeViewHolder(ItemCusBinding.inflate(LayoutInflater.from(parent.context), parent, false))
    }

    override fun onBindViewHolder(holder: CustomizeViewHolder, position: Int) {
        holder.bind(getItem(position), position)
    }
    companion object {
        val DiffCallback = object : DiffUtil.ItemCallback<ItemNavCustomModel>(){
            override fun areItemsTheSame(oldItem: ItemNavCustomModel, newItem: ItemNavCustomModel): Boolean {
                return oldItem.path == newItem.path
            }

            override fun areContentsTheSame(oldItem: ItemNavCustomModel, newItem: ItemNavCustomModel): Boolean {
                return oldItem == newItem
            }
        }
    }
}
