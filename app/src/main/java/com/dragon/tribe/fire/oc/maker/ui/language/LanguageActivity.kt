package com.dragon.tribe.fire.oc.maker.ui.language

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.widget.Toast
import androidx.activity.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.charactor.avatar.maker.pfp.ui.language.LanguageViewModel
import com.dragon.tribe.fire.oc.maker.core.base.BaseActivity
import com.dragon.tribe.fire.oc.maker.core.extensions.handleBack
import com.dragon.tribe.fire.oc.maker.core.extensions.hide
import com.dragon.tribe.fire.oc.maker.core.extensions.onSingleClick
import com.dragon.tribe.fire.oc.maker.core.extensions.select
import com.dragon.tribe.fire.oc.maker.core.extensions.show
import com.dragon.tribe.fire.oc.maker.core.extensions.startIntentAnim
import com.dragon.tribe.fire.oc.maker.core.utils.DataLocal.getLanguageList
import com.dragon.tribe.fire.oc.maker.core.utils.KeyApp.INTENT_KEY
import com.dragon.tribe.fire.oc.maker.core.utils.SystemUtils
import com.dragon.tribe.fire.oc.maker.core.utils.SystemUtils.setFirstLang
import com.dragon.tribe.fire.oc.maker.core.utils.SystemUtils.setPreLanguage
import com.dragon.tribe.fire.oc.maker.R
import com.dragon.tribe.fire.oc.maker.core.extensions.showToast
import com.dragon.tribe.fire.oc.maker.core.extensions.startIntentReverse
import com.dragon.tribe.fire.oc.maker.core.extensions.startIntentRightToLeft
import com.dragon.tribe.fire.oc.maker.core.extensions.visible
import com.dragon.tribe.fire.oc.maker.core.utils.SystemUtils.setLocale
import com.dragon.tribe.fire.oc.maker.core.utils.key.IntentKey
import com.dragon.tribe.fire.oc.maker.databinding.ActivityLanguageBinding
import com.dragon.tribe.fire.oc.maker.ui.intro.IntroActivity
import com.dragon.tribe.fire.oc.maker.data.model.LanguageModel
import com.dragon.tribe.fire.oc.maker.ui.home.HomeActivity
import com.lvt.ads.util.Admob
import kotlinx.coroutines.launch
import kotlin.collections.forEach
import kotlin.getValue
import kotlin.system.exitProcess
class LanguageActivity  : BaseActivity<ActivityLanguageBinding>() {
    private val viewModel: LanguageViewModel by viewModels()

    private val languageAdapter by lazy { LanguageAdapter(this) }

    override fun setViewBinding(): ActivityLanguageBinding {
        return ActivityLanguageBinding.inflate(LayoutInflater.from(this))
    }

    override fun initView() {
        initRcv()
        val intentValue = intent.getStringExtra(IntentKey.INTENT_KEY)
        val currentLang = sharePreference.getPreLanguage()
        viewModel.setFirstLanguage(intentValue == null)
        viewModel.loadLanguages(currentLang)
    }

    override fun dataObservable() {
        lifecycleScope.launch {
            viewModel.isFirstLanguage.collect { isFirst ->
                if (isFirst) {
                    binding.btnDone.show()
                    binding.btnBack.hide()
                    binding.btnChangSetting.hide()
                } else {
                    binding.btnBack.show()
                    binding.btnChangSetting.show()
                    binding.btnDone.hide()
                }
            }
        }

        lifecycleScope.launch {
            viewModel.languageList.collect { list ->
                languageAdapter.submitList(list)
            }
        }
        lifecycleScope.launch {
            viewModel.codeLang.collect { code ->
                if (code.isNotEmpty()) {
                    binding.btnDone.visible()
                }
            }
        }
    }

    override fun viewListener() {
        binding.apply {
            btnBack.onSingleClick {
                if (!viewModel.isFirstLanguage.value) {
                    handleBack()
                } else {
                    exitProcess(0)
                }
            }

            btnDone.onSingleClick {
                handleDone()
            }

        }

        handleRcv()
    }

    override fun initText() {
        binding.txtLanguageCenter.select()
    }



    private fun initRcv() {
        binding.rcv.apply {
            layoutManager = LinearLayoutManager(this@LanguageActivity)
            adapter = languageAdapter
            itemAnimator = null
        }
    }

    private fun handleRcv() {
        binding.apply {
            languageAdapter.onItemClick = { code ->
                viewModel.selectLanguage(code)
            }
        }
    }

    private fun handleDone() {
        val code = viewModel.codeLang.value
        if (code.isEmpty()) {
            showToast(R.string.not_select_lang)
            return
        }
        sharePreference.setPreLanguage(code)
        SystemUtils.changeLang(code, this)

        if (viewModel.isFirstLanguage.value) {
            sharePreference.setIsFirstLang(false)
            startIntentAnim(IntroActivity::class.java)
            finishAffinity()
        } else {
            startIntentReverse()        }
    }

    @SuppressLint("MissingSuperCall", "GestureBackNavigation")
    override fun onBackPressed() {
        if (!viewModel.isFirstLanguage.value) {
            handleBack()
        } else {
            exitProcess(0)
        }
    }
    override fun initAds() {
        Admob.getInstance().loadNativeAd(this,getString(R.string.native_language),binding.nativeAds,R.layout.ads_native_big)
    }

}