package com.dragon.tribe.fire.oc.maker.core.dialog

import android.app.Activity
import com.dragon.tribe.fire.oc.maker.R
import com.dragon.tribe.fire.oc.maker.core.base.BaseDialog

import com.dragon.tribe.fire.oc.maker.core.extensions.onSingleClick
import com.dragon.tribe.fire.oc.maker.databinding.DialogNoInternetBinding
import kotlin.apply

class NoInternetDialog (val context: Activity) : BaseDialog<DialogNoInternetBinding>(context, maxWidth = true, maxHeight = true) {
    override val layoutId: Int = R.layout.dialog_no_internet
    override val isCancel: Boolean = false
    override val isBack: Boolean = false
    var onOkClick: (() -> Unit)? = null
    var onDismissClick: (() -> Unit)? = null

    override fun initView() {
        binding.apply {
//            txtCheck.select()
            txtDescription.isSelected=true
            txtTitle.isSelected=true
        }
    }

    override fun initAction() {
        binding.btnOk.onSingleClick {
            onOkClick?.invoke()
        }
        binding.main.onSingleClick {
            onDismissClick?.invoke()
        }
    }

    override fun onDismissListener() {

    }
}