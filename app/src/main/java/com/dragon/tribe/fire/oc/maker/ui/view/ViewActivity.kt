package com.dragon.tribe.fire.oc.maker.ui.view

import android.Manifest
import android.content.pm.PackageManager
import android.graphics.Canvas
import android.os.Build
import android.view.LayoutInflater
import android.view.View
import androidx.activity.viewModels
import androidx.core.app.ActivityCompat
import androidx.core.graphics.createBitmap

import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import com.bumptech.glide.Glide
import com.dragon.tribe.fire.oc.maker.R
import com.dragon.tribe.fire.oc.maker.core.base.BaseActivity
import com.dragon.tribe.fire.oc.maker.core.dialog.ConfirmDialog
import com.dragon.tribe.fire.oc.maker.core.extensions.*
import com.dragon.tribe.fire.oc.maker.core.helper.SharePreferenceHelper
import com.dragon.tribe.fire.oc.maker.core.utils.KeyApp.STORAGE_PERMISSION_CODE
import com.dragon.tribe.fire.oc.maker.core.utils.SaveState
import com.dragon.tribe.fire.oc.maker.core.utils.SystemUtils.setLocale
import com.dragon.tribe.fire.oc.maker.core.utils.key.IntentKey
import com.dragon.tribe.fire.oc.maker.core.utils.key.RequestKey
import com.dragon.tribe.fire.oc.maker.databinding.ActivityViewBinding
import com.dragon.tribe.fire.oc.maker.ui.permission.PermissionViewModel
import com.lvt.ads.util.Admob
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import java.io.File

class ViewActivity : BaseActivity<ActivityViewBinding>() {
    private val permissionViewModel: PermissionViewModel by viewModels()

    private val viewModel: ViewViewModel by viewModels()
    private var imagePath: String? = null
    private var isProcessing = false
    private var currentJob: Job? = null

    override fun setViewBinding(): ActivityViewBinding {
        return ActivityViewBinding.inflate(LayoutInflater.from(this))
    }

    override fun initView() {
        imagePath = intent.getStringExtra(IntentKey.INTENT_KEY)
        imagePath?.takeIf { it.isNotEmpty() }?.let { path ->
            Glide.with(this).load(path).into(binding.imvImage)
        }

        binding.apply {
            txtShare.isSelected = true
            txtDownLoad.isSelected = true
        }
    }

    override fun viewListener() {
        binding.apply {
            btnDowLoad.onSingleClick(1000) { checkPermissionAndDownload() }

            btnShare.onSingleClick(1000) {
                if (isProcessing) return@onSingleClick
                isProcessing = true

                lifecycleScope.launch {
                    showLoading()
                    try {
                        val bitmap = createBitmap(
                            binding.layoutCustomLayer.width,
                            binding.layoutCustomLayer.height
                        )
                        val canvas = Canvas(bitmap)
                        binding.layoutCustomLayer.draw(canvas)
                        handleShare(this@ViewActivity, bitmap)
                    } catch (e: Exception) {
                        e.printStackTrace()
                        showToast(getString(R.string.error))
                    } finally {
                        dismissLoading(true)
                        isProcessing = false
                    }
                }
            }

            btnDelete.onSingleClick { confirmDelete() }

            btnBack.onSingleClick { showInterAll { handleBack() } }
        }
    }

    private fun checkPermissionAndDownload() {
        // Android 10 trở lên => không cần xin quyền
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
            viewModel.saveImageToExternalStorage(this@ViewActivity, binding.layoutCustomLayer).collect { result ->
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

    private fun confirmDelete() {
        val dialog = ConfirmDialog(this, R.string.delete, R.string.do_you_want_to_delete)
        setLocale(this)
        dialog.onYesClick = {
            handleReset()
            dialog.dismiss()
            handleBack()
        }
        dialog.onNoClick = {
            dialog.dismiss()
            hideNavigation()
        }
        dialog.show()
    }

    private fun handleReset() {
        imagePath?.let { path ->
            val file = File(path)
            if (file.exists() && file.delete()) {
                showToast(getString(R.string.delete_success))
            } else {
                showToast(getString(R.string.delete_failed))
            }
        }
    }

    override fun initText() {}

    override fun initAds() {
        Admob.getInstance().loadNativeCollap(this, getString(R.string.native_all), binding.nativeAds2)
    }
}
