package com.dragon.tribe.fire.oc.maker.ui.intro

import android.annotation.SuppressLint
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import androidx.viewpager2.widget.ViewPager2
import com.dragon.tribe.fire.oc.maker.R
import com.dragon.tribe.fire.oc.maker.core.base.BaseActivity
import com.dragon.tribe.fire.oc.maker.core.extensions.onSingleClick
import com.dragon.tribe.fire.oc.maker.core.extensions.startIntentAnim
import com.dragon.tribe.fire.oc.maker.core.utils.DataLocal
import com.dragon.tribe.fire.oc.maker.core.utils.SystemUtils
import com.dragon.tribe.fire.oc.maker.core.utils.key.IntentKey.FROM_INTRO
import com.dragon.tribe.fire.oc.maker.databinding.ActivityIntroBinding
import com.dragon.tribe.fire.oc.maker.ui.home.HomeActivity
import com.dragon.tribe.fire.oc.maker.ui.permission.PermissionActivity
import com.lvt.ads.util.Admob
import kotlin.jvm.java
import kotlin.system.exitProcess
import kotlin.text.compareTo

class IntroActivity : BaseActivity<ActivityIntroBinding>() {
    private val adapter = IntroAdapter(this, DataLocal.itemIntroList)
    override fun setViewBinding(): ActivityIntroBinding {
        return ActivityIntroBinding.inflate(LayoutInflater.from(this))
    }

    override fun initView() {
        initVpg()
    }

    override fun viewListener() {
        binding.txtNext.onSingleClick {
            handleNext()
        }
        binding.vpgTutorial.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                super.onPageSelected(position)
                binding.apply{
                    if (position == 1) {

                       nativeAds.visibility = View.GONE

                } else {
                    nativeAds.visibility = View.VISIBLE
                }}
            }
        })
    }

    override fun initText() {

    }
    private fun initVpg() {
        binding.apply {
            vpgTutorial.adapter = adapter
            dotsIndicator.setViewPager2(binding.vpgTutorial)

        }
    }
    private fun handleNext(){
        binding.apply { val nextItem = binding.vpgTutorial.currentItem + 1
            if (nextItem < DataLocal.itemIntroList.size) {
                vpgTutorial.setCurrentItem(nextItem, true)
            } else {
                    if ( sharePreference.getIsFirstPermission()) {
                        startIntentAnim(PermissionActivity::class.java)
                        finishAffinity()
                    } else {
                        startIntentAnim(HomeActivity::class.java,FROM_INTRO)
                        finishAffinity()
                    }
                }
            }

    }
    override fun initAds() {
        Admob.getInstance().loadNativeAd(this,getString(R.string.native_intro),binding.nativeAds, R.layout.ads_native_avg2)
    }
    @SuppressLint("MissingSuperCall")
    override fun onBackPressed() {
        exitProcess(0)
    }

}