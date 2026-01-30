package com.dragon.tribe.fire.oc.maker.ui.success

import android.Manifest
import android.content.pm.PackageManager
import android.graphics.Canvas
import android.os.Build
import android.view.LayoutInflater
import android.view.View
import androidx.activity.viewModels
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.graphics.createBitmap
import androidx.lifecycle.lifecycleScope
import com.dragon.tribe.fire.oc.maker.R
import com.dragon.tribe.fire.oc.maker.core.base.BaseActivity
import com.dragon.tribe.fire.oc.maker.core.extensions.*
import com.dragon.tribe.fire.oc.maker.core.helper.SharePreferenceHelper
import com.dragon.tribe.fire.oc.maker.core.utils.KeyApp.STORAGE_PERMISSION_CODE
import com.dragon.tribe.fire.oc.maker.core.utils.SaveState
import com.dragon.tribe.fire.oc.maker.core.utils.key.IntentKey
import com.dragon.tribe.fire.oc.maker.core.utils.key.RequestKey
import com.dragon.tribe.fire.oc.maker.core.utils.key.ValueKey
import com.dragon.tribe.fire.oc.maker.databinding.ActivitySuccessBinding
import com.dragon.tribe.fire.oc.maker.ui.home.HomeActivity
import com.dragon.tribe.fire.oc.maker.ui.permission.PermissionViewModel
import com.lvt.ads.util.Admob
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch

class SuccessActivity : BaseActivity<ActivitySuccessBinding>() {

    private val viewModel: SuccessViewModel by viewModels()
    private var isProcessing = false
    private var currentJob: Job? = null
    private val permissionViewModel: PermissionViewModel by viewModels()

    private var typeView: Int = ValueKey.TYPE_VIEW

    override fun setViewBinding(): ActivitySuccessBinding {
        return ActivitySuccessBinding.inflate(LayoutInflater.from(this))
    }

    override fun initView() {
        viewModel.setPath(intent.getStringExtra(IntentKey.INTENT_KEY)!!)
        typeView = intent.getIntExtra(IntentKey.TYPE_KEY, ValueKey.TYPE_VIEW)
        viewModel.setType(typeView)

        binding.apply {
            txtShare.isSelected = true
            txtDownLoad.isSelected = true
            txtTitle1.visibility = if (typeView == ValueKey.TYPE_VIEW) View.GONE else View.VISIBLE
        }
    }

    override fun dataObservable() {
        lifecycleScope.launch {
            viewModel.pathInternal.collect { path ->
                loadImageGlide(this@SuccessActivity, path, binding.imvImage)
            }
        }
    }

    override fun initText() {}

    override fun viewListener() {
        binding.apply {
            btnBack.onSingleClick { handleBack() }

            btnHome.onSingleClick {
                showInterAll {
                    startIntent(HomeActivity::class.java)
                    finishAffinity()
                }
            }

            btnShare.onSingleClick(1000) {
                if (isProcessing) return@onSingleClick
                isProcessing = true
                lifecycleScope.launch {
                    showLoading()
                    try {
                        val bitmap = createBitmap(binding.layoutCustomLayer.width, binding.layoutCustomLayer.height)
                        val canvas = Canvas(bitmap)
                        binding.layoutCustomLayer.draw(canvas)
                        handleShare(this@SuccessActivity, bitmap)
                    } finally {
                        dismissLoading(true)
                        isProcessing = false
                    }
                }
            }

            btnDowLoad.onSingleClick(1500) {
                if (isProcessing) return@onSingleClick
                checkPermissionAndDownload()
            }
        }
    }

    private fun checkPermissionAndDownload() {
        // Android 13 trở lên => không cần xin quyền
        if (Build.VERSION.SDK_INT >=  Build.VERSION_CODES.Q) {
            safeDownloadImages()
            return
        }

        val sharePref = SharePreferenceHelper(this)
        val perms = permissionViewModel.getStoragePermissions()

        if (checkPermissions(perms)) {
            // Quyền đã được cấp
            safeDownloadImages()
        } else if (sharePref.getStoragePermission() >= 3) {
            goToSettings()
        }
        else {
            // Xin quyền
            requestPermission(perms, RequestKey.STORAGE_PERMISSION_CODE)
        }
    }
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        val sharePref = SharePreferenceHelper(this)
        val granted = grantResults.isNotEmpty() && grantResults.all { it == PackageManager.PERMISSION_GRANTED }

        when (requestCode) {
            RequestKey.STORAGE_PERMISSION_CODE -> {
                permissionViewModel.updateStorageGranted(sharePref, granted)
                if (granted) {
                    safeDownloadImages()
                    showToast(R.string.granted_storage)
                } else {

                    showToast(R.string.denied_storage)

                }
            }
        }
    }


    private fun safeDownloadImages() {
        if (isProcessing) return
        isProcessing = true
        currentJob?.cancel()
        currentJob = lifecycleScope.launch {
            viewModel.saveImageToExternalStorage(this@SuccessActivity, binding.layoutCustomLayer).collect { result ->
                when (result) {
                    is SaveState.Loading -> showLoading()
                    is SaveState.Success -> {
                        dismissLoading(true)
                        isProcessing = false
                    }
                    is SaveState.Error -> {
                        dismissLoading(true)
                        isProcessing = false
                    }
                }
            }
        }
    }

    override fun initAds() {
        Admob.getInstance().loadNativeCollap(this, getString(R.string.native_cl_ss), binding.nativeAds)
    }
}
