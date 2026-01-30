package com.dragon.tribe.fire.oc.maker.ui.background1

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide

import com.dragon.tribe.fire.oc.maker.core.extensions.gone
import com.dragon.tribe.fire.oc.maker.core.extensions.onSingleClick
import com.dragon.tribe.fire.oc.maker.core.extensions.show
import com.dragon.tribe.fire.oc.maker.core.utils.SystemUtils.shimmerDrawable
import com.dragon.tribe.fire.oc.maker.data.model.BackGroundModel
import com.dragon.tribe.fire.oc.maker.databinding.ItemBackgorundBinding

class BackgroundAdapter(private val context: Context) : RecyclerView.Adapter<BackgroundAdapter.BackgroundViewHolder>() {
    private val itemList: ArrayList<BackGroundModel> = arrayListOf()
    var onItemClick: ((BackGroundModel, Int) -> Unit)? = null
    var onNoneClick: ((Int) -> Unit)? = null

    inner class BackgroundViewHolder(private val binding: ItemBackgorundBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(item: BackGroundModel, position: Int) {
            // Hiển thị ảnh hoặc "None"
            if (item.path == null) {
                binding.btnNone.show()
                binding.imvImage.gone()
            } else {
                binding.btnNone.gone()
                binding.imvImage.show()
                Glide.with(binding.root)
                    .load(item.path)
                    .placeholder(shimmerDrawable)
                    .error(shimmerDrawable)
                    .into(binding.imvImage)
            }

            binding.layoutFocus1.isVisible = item.isSelected

            binding.root.onSingleClick { onItemClick?.invoke(item, position) }
            binding.btnNone.onSingleClick { onItemClick?.invoke(item, position) }
            binding.imvImage.onSingleClick { onItemClick?.invoke(item, position) }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BackgroundViewHolder {
        return BackgroundViewHolder(ItemBackgorundBinding.inflate(LayoutInflater.from(parent.context), parent, false))
    }

    override fun onBindViewHolder(holder: BackgroundViewHolder, position: Int) {
        holder.bind(itemList[position], position)
    }

    override fun getItemCount(): Int = itemList.size

    @SuppressLint("NotifyDataSetChanged")
    fun submitList(list: ArrayList<BackGroundModel>) {
        itemList.clear()
        itemList.addAll(list)
        notifyDataSetChanged()
    }

    val currentList: ArrayList<BackGroundModel>
        get() = itemList
    fun getCurrentList(): List<BackGroundModel> = itemList

}
