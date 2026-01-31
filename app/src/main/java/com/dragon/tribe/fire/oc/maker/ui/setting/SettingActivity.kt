package com.dragon.tribe.fire.oc.maker.ui.setting

import android.view.View
import com.dragon.tribe.fire.oc.maker.base.AbsBaseActivity
import com.dragon.tribe.fire.oc.maker.ui.language.LanguageActivity
import com.dragon.tribe.fire.oc.maker.utils.RATE
import com.dragon.tribe.fire.oc.maker.utils.SharedPreferenceUtils
import com.dragon.tribe.fire.oc.maker.utils.newIntent
import com.dragon.tribe.fire.oc.maker.utils.onSingleClick
import com.dragon.tribe.fire.oc.maker.utils.policy
import com.dragon.tribe.fire.oc.maker.utils.rateUs
import com.dragon.tribe.fire.oc.maker.utils.shareApp
import com.dragon.tribe.fire.oc.maker.utils.unItem
import com.dragon.tribe.fire.oc.maker.R
import com.dragon.tribe.fire.oc.maker.databinding.ActivitySettingBinding
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class SettingActivity : AbsBaseActivity<ActivitySettingBinding>() {
    @Inject
    lateinit var sharedPreferences: SharedPreferenceUtils
    override fun getLayoutId(): Int = R.layout.activity_setting

    override fun initView() {
        binding.titleSetting.isSelected = true
        if (sharedPreferences.getBooleanValue(RATE)) {
            binding.llRateUs.visibility = View.GONE
        }
        unItem = {
            binding.llRateUs.visibility = View.GONE
        }
    }

    override fun onStop() {
        super.onStop()
    }
    override fun initAction() {
        binding.apply {
            llLanguage.onSingleClick {
                startActivity(
                    newIntent(
                        this@SettingActivity,
                        LanguageActivity::class.java
                    )
                )
            }
            llRateUs.onSingleClick {
                rateUs(0)
            }
            llShareApp.onSingleClick {
                shareApp()
            }
            llPrivacy.onSingleClick {
                policy()
            }
            imvBack.onSingleClick {
                finish()
            }
        }
    }
}