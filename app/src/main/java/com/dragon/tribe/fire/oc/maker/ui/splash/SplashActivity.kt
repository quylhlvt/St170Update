package com.dragon.tribe.fire.oc.maker.ui.splash

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import com.dragon.tribe.fire.oc.maker.core.base.BaseActivity
import com.dragon.tribe.fire.oc.maker.core.utils.SystemUtils
import com.dragon.tribe.fire.oc.maker.ui.home.DataViewModel
import com.dragon.tribe.fire.oc.maker.R
import com.dragon.tribe.fire.oc.maker.core.extensions.initNetworkMonitor
import com.dragon.tribe.fire.oc.maker.core.utils.SystemUtils.shareApp
import com.dragon.tribe.fire.oc.maker.databinding.ActivitySplashBinding
import com.dragon.tribe.fire.oc.maker.ui.intro.IntroActivity
import com.dragon.tribe.fire.oc.maker.ui.language.LanguageActivity
import com.lvt.ads.callback.InterCallback
import com.lvt.ads.util.Admob
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.getValue
import kotlin.jvm.java

class SplashActivity : BaseActivity<ActivitySplashBinding>() {
    var interCallBack: InterCallback? = null
    private var nextIntent: Intent?=null
    private var check = false
    private val viewModel: DataViewModel by viewModels()
    override fun setViewBinding(): ActivitySplashBinding {
        return ActivitySplashBinding.inflate(LayoutInflater.from(this))
    }

    override fun initView() {

        if (!isTaskRoot && intent.hasCategory(Intent.CATEGORY_LAUNCHER) && intent.action != null && intent.action.equals(
                Intent.ACTION_MAIN
            )
        ) {
            finish(); return;
        }

        nextIntent= if (sharePreference.getIsFirstLang())
          Intent(this, LanguageActivity::class.java)else Intent(this, IntroActivity::class.java)
        // chi de test 3s con day len chplay la 30s
        Admob.getInstance().setTimeLimitShowAds(30000)
        interCallBack = object : InterCallback() {
            override fun onNextAction() {
                super.onNextAction()
                startActivity(nextIntent)
                finishAffinity()
            }
        }
        initNetworkMonitor()
        viewModel.ensureData(this)

    }

    override fun dataObservable() {
        lifecycleScope.launch {
            viewModel.allData.collect { data ->
                if (data.isNotEmpty()){
                    moveNextScreen()
                }
            }
        }
    }
    private fun moveNextScreen() {
        Admob.getInstance().loadSplashInterAds(
            this,
            getString(R.string.inter_splash),
            30000,
            2000,
            interCallBack
        )
    }
    override fun viewListener() {

    }

    override fun initText() {

    }

    override fun onBackPressed() {
        if (check) {
            super.onBackPressed()
        } else {
            check = false
        }
    }
    override fun onResume() {
        super.onResume()
        Admob.getInstance().onCheckShowSplashWhenFail(this, interCallBack, 1000)
    }
}