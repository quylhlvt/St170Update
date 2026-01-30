package com.dragon.tribe.fire.oc.maker.ui.permission

import android.content.pm.PackageManager
import android.os.Build
import android.view.LayoutInflater
import android.view.View
import androidx.activity.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.dragon.tribe.fire.oc.maker.R
import com.dragon.tribe.fire.oc.maker.core.base.BaseActivity
import com.dragon.tribe.fire.oc.maker.core.extensions.checkPermissions
import com.dragon.tribe.fire.oc.maker.core.extensions.goToSettings
import com.dragon.tribe.fire.oc.maker.core.extensions.gone
import com.dragon.tribe.fire.oc.maker.core.extensions.onSingleClick
import com.dragon.tribe.fire.oc.maker.core.extensions.requestPermission
import com.dragon.tribe.fire.oc.maker.core.extensions.select
import com.dragon.tribe.fire.oc.maker.core.extensions.showInterAll
import com.dragon.tribe.fire.oc.maker.core.extensions.showToast
import com.dragon.tribe.fire.oc.maker.core.extensions.startIntentAnim
import com.dragon.tribe.fire.oc.maker.core.extensions.visible
import com.dragon.tribe.fire.oc.maker.core.utils.key.IntentKey.FROM_INTRO
import com.dragon.tribe.fire.oc.maker.core.utils.key.RequestKey
import com.dragon.tribe.fire.oc.maker.databinding.ActivityPermissionBinding
import com.dragon.tribe.fire.oc.maker.ui.home.HomeActivity
import com.google.android.gms.ads.interstitial.InterstitialAd
import com.google.android.gms.ads.nativead.NativeAd
import com.google.android.gms.ads.nativead.NativeAdView
import com.lvt.ads.callback.InterCallback
import com.lvt.ads.callback.NativeCallback
import com.lvt.ads.util.Admob
import kotlinx.coroutines.launch

class PermissionActivity : BaseActivity<ActivityPermissionBinding>() {
    private var inter: InterstitialAd? = null


    private val viewModel: PermissionViewModel by viewModels()

    override fun setViewBinding() = ActivityPermissionBinding.inflate(LayoutInflater.from(this))

    override fun initView() {
        sharePreference.setIsFirstPermission(false)
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) {
            binding.btnStorage.visible()
            binding.btnNotification.gone()
        } else {
            binding.btnNotification.visible()
            binding.btnStorage.gone()
        }
    }

    override fun initText() {
        binding.txtTitle1.select()
        binding.apply {
            val allowText = getString(R.string.allow)
            val appName = getString(R.string.app_name)
            val toAccess = getString(R.string.to_access)

            txtPer.text = when {
                Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU -> {
                    "$allowText $appName $toAccess (Android 13+)"
                }

                else -> {
                    "$allowText $appName $toAccess"
                }
            }
        }
    }

    override fun viewListener() {
        binding.swPermission.onSingleClick { handlePermissionRequest(isStorage = true) }
        binding.swNotification.onSingleClick { handlePermissionRequest(isStorage = false) }
        Admob.getInstance().loadInterAds(
            this@PermissionActivity, getString(R.string.inter_per), object : InterCallback() {
                override fun onAdLoadSuccess(interstitialAd: InterstitialAd?) {
                    super.onAdLoadSuccess(interstitialAd)
                    inter = interstitialAd
                }
            })

        binding.txtContinue.onSingleClick(1500) { handleContinue() }
    }

    override fun dataObservable() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.storageGranted.collect { granted ->
                    updatePermissionUI(granted, true)
                }
            }
        }

        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.notificationGranted.collect { granted ->
                    updatePermissionUI(granted, false)
                }
            }
        }
    }

    private fun handlePermissionRequest(isStorage: Boolean) {
        val perms =
            if (isStorage) viewModel.getStoragePermissions() else viewModel.getNotificationPermissions()
        if (checkPermissions(perms)) {
            showToast(if (isStorage) R.string.granted_storage else R.string.granted_notification)
        } else if (viewModel.needGoToSettings(sharePreference, isStorage)) {
            goToSettings()
        } else {
            val requestCode =
                if (isStorage) RequestKey.STORAGE_PERMISSION_CODE else RequestKey.NOTIFICATION_PERMISSION_CODE
            requestPermission(perms, requestCode)
        }
    }

    private fun updatePermissionUI(granted: Boolean, isStorage: Boolean) {
        val imageView = if (isStorage) binding.swPermission else binding.swNotification
        imageView.setImageResource(if (granted) R.drawable.switch_on_permission else R.drawable.switch_off_permission)
    }

    override fun onRequestPermissionsResult(
        requestCode: Int, permissions: Array<String>, grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        val granted =
            grantResults.isNotEmpty() && grantResults.all { it == PackageManager.PERMISSION_GRANTED }
        when (requestCode) {
            RequestKey.STORAGE_PERMISSION_CODE -> viewModel.updateStorageGranted(
                sharePreference, granted
            )

            RequestKey.NOTIFICATION_PERMISSION_CODE -> viewModel.updateNotificationGranted(
                sharePreference, granted
            )
        }
        if (granted) {
            showToast(if (requestCode == RequestKey.STORAGE_PERMISSION_CODE) R.string.granted_storage else R.string.granted_notification)
        }
    }

    override fun onStart() {
        super.onStart()
        viewModel.updateStorageGranted(
            sharePreference, checkPermissions(viewModel.getStoragePermissions())
        )
        viewModel.updateNotificationGranted(
            sharePreference, checkPermissions(viewModel.getNotificationPermissions())
        )
    }

    private fun handleContinue() {
        Admob.getInstance().showInterAds(this@PermissionActivity, inter, object : InterCallback() {
            override fun onNextAction() {
                super.onNextAction()
                startIntentAnim(HomeActivity::class.java, FROM_INTRO)
                finishAffinity()
            }
        })
    }

    override fun initAds() {
        Admob.getInstance().loadNativeAd(this,getString(R.string.native_per),binding.nativeAds, R.layout.ads_native_avg2)

//        Admob.getInstance().loadNativeAd(this, getString(R.string.native_per), object : NativeCallback() {
//            override fun onNativeAdLoaded(nativeAd: NativeAd) {
//                super.onNativeAdLoaded(nativeAd)
//                val adView = LayoutInflater.from(this@PermissionActivity)
//                    .inflate(R.layout.ads_native_avg2, null) as NativeAdView
//                binding.nativeAds.removeAllViews()
//                binding.nativeAds.addView(adView)
//            }
//
//                override fun onAdFailedToLoad() {
//                    super.onAdFailedToLoad()
//                    binding.nativeAds.gone()
//
//                }
//        })
    }
}
