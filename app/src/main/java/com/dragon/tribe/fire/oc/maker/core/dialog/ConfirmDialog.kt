package com.dragon.tribe.fire.oc.maker.core.dialog

import android.app.Activity
import android.graphics.Color
import android.graphics.LinearGradient
import android.graphics.Shader
import android.os.Build
import android.view.View
import android.view.WindowInsets
import android.view.WindowInsetsController
import android.view.WindowManager
import android.widget.TextView
import androidx.core.content.ContextCompat
import com.dragon.tribe.fire.oc.maker.R
import com.dragon.tribe.fire.oc.maker.core.base.BaseDialog
import com.dragon.tribe.fire.oc.maker.core.extensions.hideNavigation

import com.dragon.tribe.fire.oc.maker.core.extensions.onSingleClick
import com.dragon.tribe.fire.oc.maker.core.extensions.select
import com.dragon.tribe.fire.oc.maker.databinding.DialogConfirmBinding
import com.lvt.ads.util.Admob

import kotlin.apply

class ConfirmDialog(val context: Activity, val title: Int, val description: Int,var checkExit: Boolean = false) :
    BaseDialog<DialogConfirmBinding>(context, maxWidth = true, maxHeight = true) {
    override val layoutId: Int = R.layout.dialog_confirm
    override val isCancel: Boolean = false
    override val isBack: Boolean = false

    var onNoClick: (() -> Unit)? = null
    var onYesClick: (() -> Unit)? = null
    var onDismissClick: (() -> Unit)? = null
    override fun initView() {
        makeDialogFullscreen()
        initText()
        if(checkExit){
            binding.nativeAds.visibility = View.VISIBLE
            Admob.getInstance().loadNativeAd(
                context,
                context.getString(R.string.native_exit),
                binding.nativeAds,
                R.layout.ads_native_avg2_white
            )
        }
        context.hideNavigation()
    }

    override fun initAction() {
        binding.apply {
            txtTitle.isSelected=true
            btnNo.onSingleClick {
                onNoClick?.invoke()
            }
            btnYes.onSingleClick {
                onYesClick?.invoke()
            }
            main.onSingleClick {
                onDismissClick?.invoke()
            }
        }
    }

    override fun onDismissListener() {

    }

    private fun initText() {
        binding.apply {
            txtTitle.text = ContextCompat.getString(context, title)
            txtDescription.text = ContextCompat.getString(context, description)
            txtDescription.select()
            txtYes.select()
        }
    }
    private fun setGradientHeightTextColor(textView: TextView) {
        val paint = textView.paint
        val height = textView.textSize
        val textShader = LinearGradient(
            0f, 0f, 0f, height, intArrayOf(Color.parseColor("#0D8AFC"), Color.parseColor("#33F0B0")), null, Shader.TileMode.CLAMP
        )
        textView.paint.shader = textShader
    }
    private fun makeDialogFullscreen() {
        window?.apply {
            // Làm nền dialog trong suốt để chỉ hiển thị layout #80545454
            setBackgroundDrawableResource(android.R.color.transparent)

            // Cho phép layout phủ cả vùng status + navigation
            setFlags(
                WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN or
                        WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
                WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN or
                        WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS
            )

            // Ẩn thanh hệ thống mà không đổi màu nền
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                setDecorFitsSystemWindows(false)
                decorView.windowInsetsController?.apply {
                    hide(WindowInsets.Type.statusBars() or WindowInsets.Type.navigationBars())
                    systemBarsBehavior =
                        WindowInsetsController.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
                }
            } else {
                @Suppress("DEPRECATION")
                decorView.systemUiVisibility = (
                        View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                                or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                                or View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                                or View.SYSTEM_UI_FLAG_FULLSCREEN
                                or View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                                or View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                        )
            }
        }
    }

}