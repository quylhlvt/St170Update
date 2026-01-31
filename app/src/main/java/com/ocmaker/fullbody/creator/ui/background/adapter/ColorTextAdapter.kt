package com.ocmaker.fullbody.creator.ui.background.adapter

import androidx.recyclerview.widget.RecyclerView
import com.ocmaker.fullbody.creator.R
import com.ocmaker.fullbody.creator.databinding.ItemColorEdtBinding
import com.ocmaker.fullbody.creator.utils.hide
import com.ocmaker.fullbody.creator.utils.onSingleClick
import com.ocmaker.fullbody.creator.utils.show
import com.ocmaker.fullbody.creator.base.AbsBaseAdapter
import com.ocmaker.fullbody.creator.base.AbsBaseDiffCallBack
import com.ocmaker.fullbody.creator.data.model.SelectedModel

class ColorTextAdapter :
    AbsBaseAdapter<SelectedModel, ItemColorEdtBinding>(R.layout.item_color_edt, DiffCallBack()) {
    var onClick: ((Int) -> Unit)? = null
    var posSelect = 1
    override fun bind(
        binding: ItemColorEdtBinding,
        position: Int,
        data: SelectedModel,
        holder: RecyclerView.ViewHolder
    ) {
        binding.bg.onSingleClick {

            onClick?.invoke(position)
        }
        binding.bg.setBackgroundColor(data.color)
        if(position == 0){
            binding.imvPlus.show()
        }else{
            binding.imvPlus.hide()
        }
        if(data.isSelected){
            binding.imv.show()
        }else{
            binding.imv.hide()
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