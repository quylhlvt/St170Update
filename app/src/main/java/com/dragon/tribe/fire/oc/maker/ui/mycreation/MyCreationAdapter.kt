package com.dragon.tribe.fire.oc.maker.ui.mycreation

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide

import com.dragon.tribe.fire.oc.maker.core.extensions.gone
import com.dragon.tribe.fire.oc.maker.core.extensions.onSingleClick
import com.dragon.tribe.fire.oc.maker.core.extensions.show
import com.dragon.tribe.fire.oc.maker.data.model.MyCreationModel
import com.dragon.tribe.fire.oc.maker.core.utils.SystemUtils.shimmerDrawable
import com.dragon.tribe.fire.oc.maker.R
import com.dragon.tribe.fire.oc.maker.databinding.ItemMyCreationBinding

class MyCreationAdapter(val context: Context) : RecyclerView.Adapter<MyCreationAdapter.MyLibraryViewHolder>() {
     var listMyLibrary: ArrayList<MyCreationModel> = arrayListOf()
    var onItemClick: ((String) -> Unit)? = null
    var onMoreClick: ((String, Int, View) -> Unit)? = null
    var onLongClick: ((Int) -> Unit)? = null
    var onItemTick: ((Int) -> Unit)? = null
    var selectionMode = false

    inner class MyLibraryViewHolder(val binding: ItemMyCreationBinding) : RecyclerView.ViewHolder(binding.root) {
        @SuppressLint("ClickableViewAccessibility")
        fun bind(item: MyCreationModel, position: Int) {
            Glide.with(binding.root)
                .load(item.path)
                .placeholder(shimmerDrawable)
                .error(shimmerDrawable)
                .into(binding.imvImage)

            // Hiển thị tick hoặc more
            if (item.isShowSelection) {
                binding.btnSelect.show()
                binding.btnMore.gone()
            } else {
                binding.btnSelect.gone()
                binding.btnMore.show()
            }

            // Cập nhật icon tick
            binding.btnSelect.setImageResource(
                if (item.isSelected) R.drawable.ic_tick_item else R.drawable.ic_not_tick_item
            )

            // ⚙️ NGĂN “nháy” khi giữ — flag này chặn click sau long click
            var isLongPressed = false

            binding.root.setOnTouchListener { _, event ->
                if (event.action == android.view.MotionEvent.ACTION_DOWN) {
                    isLongPressed = false
                }
                false
            }

            binding.root.setOnLongClickListener {
                isLongPressed = true
                onLongClick?.invoke(position)
                true
            }

            binding.root.onSingleClick {
                if (!isLongPressed) {
                    onItemClick?.invoke(item.path)
                }
            }

            binding.btnSelect.onSingleClick {
                onItemTick?.invoke(position)
            }
        }

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyLibraryViewHolder {
        return MyLibraryViewHolder(ItemMyCreationBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        )
    }

    override fun getItemCount(): Int {
        return listMyLibrary.size
    }

    override fun onBindViewHolder(holder: MyLibraryViewHolder, position: Int) {
        val item = listMyLibrary[position]
        holder.bind(item, position)
    }

    @SuppressLint("NotifyDataSetChanged")
    fun submitList(list: ArrayList<MyCreationModel>) {
        listMyLibrary.clear()
        listMyLibrary.addAll(list)
        notifyDataSetChanged()
    }

    fun submitItem(position: Int, isSelect: Boolean) {
        listMyLibrary[position].isSelected = isSelect
        notifyItemChanged(position)
    }
    fun isSelectionMode(): Boolean = selectionMode

    @SuppressLint("NotifyDataSetChanged")
    fun enableSelectionMode(enable: Boolean) {
        selectionMode = enable
        listMyLibrary.forEach { it.isShowSelection = enable }
        notifyDataSetChanged()
    }

    fun toggleSelect(position: Int) {
        listMyLibrary[position].isSelected = !listMyLibrary[position].isSelected
        notifyItemChanged(position)
    }

    fun getSelectedCount(): Int {
        return listMyLibrary.count { it.isSelected }
    }
    fun clearAllSelections() {
        listMyLibrary.forEach { it.isSelected = false
            it.isShowSelection = false
        }
        notifyDataSetChanged()
    }

}