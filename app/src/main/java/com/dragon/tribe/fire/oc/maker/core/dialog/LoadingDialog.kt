package com.dragon.tribe.fire.oc.maker.core.dialog

import android.app.Activity
import com.dragon.tribe.fire.oc.maker.R
import com.dragon.tribe.fire.oc.maker.core.base.BaseDialog
import com.dragon.tribe.fire.oc.maker.databinding.DialogLoadingBinding


class LoadingDialog(val context: Activity) : BaseDialog<DialogLoadingBinding>(context, maxWidth = true, maxHeight = true) {
    override val layoutId: Int = R.layout.dialog_loading
    override val isCancel: Boolean = false
    override val isBack: Boolean = false
    override fun initView() {
//        binding.txtDescription.select()
        binding.apply {
            txtDescription.isSelected =true
            txtDescription2.isSelected=true
        }
    }

    override fun initAction() {

    }

    override fun onDismissListener() {

    }
}