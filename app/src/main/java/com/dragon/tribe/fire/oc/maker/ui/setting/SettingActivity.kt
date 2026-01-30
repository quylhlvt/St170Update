package com.dragon.tribe.fire.oc.maker.ui.setting

import android.view.LayoutInflater
import android.view.View
import com.dragon.tribe.fire.oc.maker.R
import com.dragon.tribe.fire.oc.maker.core.base.BaseActivity
import com.dragon.tribe.fire.oc.maker.core.dialog.RateDialog
import com.dragon.tribe.fire.oc.maker.core.extensions.handleBack
import com.dragon.tribe.fire.oc.maker.core.extensions.onSingleClick
import com.dragon.tribe.fire.oc.maker.core.extensions.openPlayStoreForReview
import com.dragon.tribe.fire.oc.maker.core.extensions.showToast
import com.dragon.tribe.fire.oc.maker.core.extensions.startIntentAnim
import com.dragon.tribe.fire.oc.maker.core.utils.KeyApp.FROM_SETTINGS
import com.dragon.tribe.fire.oc.maker.core.utils.SystemUtils
import com.dragon.tribe.fire.oc.maker.core.utils.SystemUtils.policy
import com.dragon.tribe.fire.oc.maker.core.utils.SystemUtils.reviewApp
import com.dragon.tribe.fire.oc.maker.core.utils.SystemUtils.shareApp
import com.dragon.tribe.fire.oc.maker.databinding.ActivitySettingBinding
import com.dragon.tribe.fire.oc.maker.ui.language.LanguageActivity

class SettingActivity : BaseActivity<ActivitySettingBinding>() {
    override fun setViewBinding(): ActivitySettingBinding {
        return ActivitySettingBinding.inflate(LayoutInflater.from(this))
    }

    override fun initView() {
        if (sharePreference.getIsRate(this@SettingActivity)) binding.btnRate.visibility = View.GONE
    }

    override fun viewListener() {
        binding.apply {
            btnBack.onSingleClick {
                handleBack()
            }
            btnLang.onSingleClick {
                startIntentAnim(LanguageActivity::class.java, FROM_SETTINGS)
            }
            btnRate.onSingleClick {
                val rateDialog = RateDialog(this@SettingActivity)
                rateDialog.init(object : RateDialog.OnPress {
                    override fun send(rate: Float) {
                        binding.btnRate.visibility = View.GONE
                        if (rate > 3L) reviewApp(this@SettingActivity, false)
                         showToast(R.string.have_rated)
                        sharePreference.setIsRate( true)
                        rateDialog.dismiss()

                    }

                    override fun rating() {
                    }

                    override fun cancel() {
                    }

                    override fun later() {
                    }

                })
                rateDialog.show()
            }

            btnShare.onSingleClick {
                shareApp()
            }
            btnPolicy.onSingleClick {
                policy()
            }
        }

    }

    override fun initText() {

    }

}