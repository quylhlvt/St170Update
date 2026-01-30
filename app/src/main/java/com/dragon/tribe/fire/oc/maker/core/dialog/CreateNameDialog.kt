package com.dragon.tribe.fire.oc.maker.core.dialog

import android.app.Activity
import android.widget.Toast
import com.dragon.tribe.fire.oc.maker.R
import com.dragon.tribe.fire.oc.maker.core.base.BaseDialog1
import com.dragon.tribe.fire.oc.maker.core.extensions.onSingleClick
import com.dragon.tribe.fire.oc.maker.databinding.DialogCreateNameBinding


class CreateNameDialog(val context: Activity) :
    BaseDialog1<DialogCreateNameBinding>(context, maxWidth = true, maxHeight = true) {
    override val layoutId: Int = R.layout.dialog_create_name
    override val isCancelOnTouchOutside: Boolean = false
    override val isCancelableByBack: Boolean = false

    var onNoClick: (() -> Unit) = {}
    var onYesClick: ((String) -> Unit) = {}
    var onDismissClick: (() -> Unit) = {}

    override fun initView() {
    }

    override fun initAction() {
        binding.apply {
            tvNo.onSingleClick {
                onNoClick.invoke()
            }
            tvYes.onSingleClick {
                val input = edtName.text.toString().trim()

                when {
                    input == "" -> {
                        Toast.makeText(context, context.getString(R.string.please_enter_your_package_name), Toast.LENGTH_SHORT).show()
                    }


                    else -> {
                        onYesClick.invoke(input)
                    }
                }
            }
            flOutSide.onSingleClick {
                onDismissClick.invoke()
            }
        }
    }

    override fun onDismissListener() {

    }
}