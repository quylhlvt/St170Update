package com.dragon.tribe.fire.oc.maker.core.dialog

import android.app.Activity
import android.graphics.Color
import com.dragon.tribe.fire.oc.maker.R
import com.dragon.tribe.fire.oc.maker.core.base.BaseDialog
import com.dragon.tribe.fire.oc.maker.core.base.BaseDialog2
import com.dragon.tribe.fire.oc.maker.core.extensions.onSingleClick
import com.dragon.tribe.fire.oc.maker.databinding.DialogColorPickerBinding


class ChooseColorDialog(context: Activity) : BaseDialog2<DialogColorPickerBinding>(context, false) {
    var onDoneEvent: ((Int) -> Unit) = {}
    private var color = Color.WHITE
    override fun getContentView(): Int = R.layout.dialog_color_picker

    override fun initView() {
        binding.apply {
            colorPickerView.apply {
                hueSliderView = hueSlider
            }
            txtColor.post { txtColor.text = String.format("#%06X", 0xFFFFFF and color) }
        }
    }

    override fun bindView() {
        binding.apply {
            colorPickerView.setOnColorChangedListener { newColor -> color = newColor
                txtColor.post { txtColor.text = String.format("#%06X", 0xFFFFFF and color) } }
            btnClose.onSingleClick { dismiss() }
            btnDone.onSingleClick {
                dismiss()
                onDoneEvent.invoke(color)
            }
        }
    }


}