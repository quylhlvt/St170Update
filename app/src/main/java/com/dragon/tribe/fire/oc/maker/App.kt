package com.dragon.tribe.fire.oc.maker

import com.dragon.tribe.fire.oc.maker.ui.splash.SplashActivity
import com.lvt.ads.util.AdsApplication
import com.lvt.ads.util.AppOpenManager
import com.dragon.tribe.fire.oc.maker.R
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.android.HiltAndroidApp
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
@HiltAndroidApp
class App :  AdsApplication()   {
    override fun onCreate() {
        super.onCreate()
        AppOpenManager.getInstance().disableAppResumeWithActivity(SplashActivity::class.java)

    }
    override fun enableAdsResume(): Boolean {
        return true
    }

    override fun getListTestDeviceId(): MutableList<String>? {
        return null
    }

    override fun getResumeAdId(): String {
        return getString(R.string.open_resume)
    }

    override fun buildDebug(): Boolean {
        return true
    }
}