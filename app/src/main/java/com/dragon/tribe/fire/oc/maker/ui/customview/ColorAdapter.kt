package com.dragon.tribe.fire.oc.maker.ui.customview

import android.view.View
import androidx.core.graphics.toColorInt
import androidx.recyclerview.widget.RecyclerView
import com.dragon.tribe.fire.oc.maker.base.AbsBaseAdapter
import com.dragon.tribe.fire.oc.maker.base.AbsBaseDiffCallBack
import com.dragon.tribe.fire.oc.maker.data.model.ColorModel
import com.dragon.tribe.fire.oc.maker.utils.onClickCustom
import com.dragon.tribe.fire.oc.maker.R
import com.dragon.tribe.fire.oc.maker.databinding.ItemColorBinding

class ColorAdapter : AbsBaseAdapter<ColorModel, ItemColorBinding>(R.layout.item_color, DiffColor()) {
    var onClick: ((Int) -> Unit)? = null
    var posColor = 0
    fun setPos(pos: Int) {
        posColor = pos
    }

    class DiffColor : AbsBaseDiffCallBack<ColorModel>() {
        override fun itemsTheSame(oldItem: ColorModel, newItem: ColorModel): Boolean {
            return oldItem.color == newItem.color
        }

        override fun contentsTheSame(oldItem: ColorModel, newItem: ColorModel): Boolean {
            return oldItem.color != newItem.color
        }

    }

    override fun bind(
        binding: ItemColorBinding,
        position: Int,
        data: ColorModel,
        holder: RecyclerView.ViewHolder
    ) {
//        if(position == arr.size-1){
//            setLayoutParam(binding.ctl,0f,0f,0f,0f)
//        }else{
//            setLayoutParam(binding.ctl,0f, dpToPx(100f,binding.root.context),0f,0f)
//        }
        if (posColor == position) {
            binding.imv.visibility = View.VISIBLE
        } else {
            binding.imv.visibility = View.GONE
        }
        binding.bg.setColorFilter("#${data.color}".toColorInt())
        binding.bg.onClickCustom {
            onClick?.invoke(position)
        }
    }
}