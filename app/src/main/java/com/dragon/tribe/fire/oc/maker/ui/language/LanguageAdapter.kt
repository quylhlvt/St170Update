package com.dragon.tribe.fire.oc.maker.ui.language

import android.annotation.SuppressLint
import android.content.Context
import com.dragon.tribe.fire.oc.maker.R
import com.dragon.tribe.fire.oc.maker.core.base.BaseAdapter
import com.dragon.tribe.fire.oc.maker.core.extensions.loadImageGlide
import com.dragon.tribe.fire.oc.maker.core.extensions.onSingleClick
import com.dragon.tribe.fire.oc.maker.data.model.LanguageModel
import com.dragon.tribe.fire.oc.maker.databinding.ItemLanguageBinding

class LanguageAdapter(val context: Context) : BaseAdapter<LanguageModel, ItemLanguageBinding>(
    ItemLanguageBinding::inflate
) {
    var onItemClick: ((String) -> Unit) = {}
    override fun onBind(
        binding: ItemLanguageBinding, item: LanguageModel, position: Int
    ) {
        binding.apply {
            loadImageGlide(root, item.flag, imvFlag, false)
            txtLang.text = item.name

            if (item.activate) {
                loadImageGlide(root, R.drawable.ic_tick_lang, rdbLang, false)
            } else {
                loadImageGlide(root, R.drawable.ic_not_tick_lang, rdbLang, false)
            }

            root.onSingleClick {
                onItemClick.invoke(item.code)
            }
        }
    }

    @SuppressLint("NotifyDataSetChanged")
    fun submitItem(position: Int) {
        items.forEach { it.activate = false }
        items[position].activate = true
        notifyDataSetChanged()
    }
}