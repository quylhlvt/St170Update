package com.dragon.tribe.fire.oc.maker.ui.customize

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.dragon.tribe.fire.oc.maker.core.extensions.onSingleClick
import com.dragon.tribe.fire.oc.maker.core.helper.UnitHelper
import com.dragon.tribe.fire.oc.maker.core.utils.SystemUtils.shimmerDrawable
import com.dragon.tribe.fire.oc.maker.data.custom.NavigationModel
import com.dragon.tribe.fire.oc.maker.databinding.ItemNaviBinding

class BottomNavigationAdapter(private val context: Context) :
    ListAdapter<NavigationModel, BottomNavigationAdapter.BottomNavViewHolder>(DiffCallback) {

    var onItemClick: (Int) -> Unit = {}

    inner class BottomNavViewHolder(private val binding: ItemNaviBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(item: NavigationModel, position: Int) = with(binding) {
            // Bo viền và highlight nếu đang được chọn
            val margin = if (item.isSelected) UnitHelper.pxToDpInt(context, 2) else 0
            val params = cvContent.layoutParams as ViewGroup.MarginLayoutParams
            params.setMargins(margin, margin, margin, margin)
            cvContent.layoutParams = params

            val cvRadius = if (item.isSelected)
                UnitHelper.pxToDpInt(context, 6)
            else
                UnitHelper.pxToDpInt(context, 8)
            cvContent.radius = cvRadius.toFloat()

            vFocus.isVisible = item.isSelected

            Glide.with(root)
                .load(item.imageNavigation)
                .placeholder(shimmerDrawable)
                .into(imvImage)

            root.onSingleClick {
                onItemClick.invoke(position)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BottomNavViewHolder {
        val binding = ItemNaviBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return BottomNavViewHolder(binding)
    }

    override fun onBindViewHolder(holder: BottomNavViewHolder, position: Int) {
        holder.bind(getItem(position), position)
    }

    /**
     * Gọi hàm này để highlight item được chọn
     * mà không cần submit lại toàn bộ list.
     */
    fun select(position: Int) {
        val updatedList = currentList.mapIndexed { index, item ->
            item.copy(isSelected = index == position)
        }
        submitList(updatedList)
    }

    companion object {
        val DiffCallback = object : DiffUtil.ItemCallback<NavigationModel>() {
            override fun areItemsTheSame(oldItem: NavigationModel, newItem: NavigationModel): Boolean {
                // So sánh theo ID hoặc image path thay vì reference
                return oldItem.imageNavigation == newItem.imageNavigation
            }

            override fun areContentsTheSame(oldItem: NavigationModel, newItem: NavigationModel): Boolean {
                // So sánh toàn bộ nội dung, kể cả isSelected
                return oldItem == newItem
            }
        }
    }
}
