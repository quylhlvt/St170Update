package com.ocmaker.fullbody.creator.ui.succes

import android.content.pm.PackageManager
import android.os.Build
import android.view.LayoutInflater
import android.widget.Toast
import androidx.activity.viewModels
import androidx.core.app.ActivityCompat
import com.bumptech.glide.Glide
import com.google.android.gms.ads.nativead.NativeAd
import com.google.android.gms.ads.nativead.NativeAdView
import com.lvt.ads.callback.NativeCallback
import com.lvt.ads.util.Admob
import com.ocmaker.fullbody.creator.R
import com.ocmaker.fullbody.creator.base.AbsBaseActivity
import com.ocmaker.fullbody.creator.databinding.ActivitySuccessBinding
import com.ocmaker.fullbody.creator.ui.main.MainActivity
import com.ocmaker.fullbody.creator.ui.my_creation.MyCreationActivity
import com.ocmaker.fullbody.creator.ui.permision.PermissionViewModel
import com.ocmaker.fullbody.creator.utils.CONST
import com.ocmaker.fullbody.creator.utils.CONST.REQUEST_NOTIFICATION_PERMISSION
import com.ocmaker.fullbody.creator.utils.SharedPreferenceUtils
import com.ocmaker.fullbody.creator.utils.newIntent
import com.ocmaker.fullbody.creator.utils.onClick
import com.ocmaker.fullbody.creator.utils.onSingleClick
import com.ocmaker.fullbody.creator.utils.saveFileToExternalStorage
import com.ocmaker.fullbody.creator.utils.scanMediaFile
import com.ocmaker.fullbody.creator.utils.shareListFiles
import com.ocmaker.fullbody.creator.utils.showDialogNotifiListener
import com.ocmaker.fullbody.creator.utils.showToast
import com.ocmaker.fullbody.creator.utils.PermissionHelper
import com.ocmaker.fullbody.creator.utils.hide
import com.ocmaker.fullbody.creator.utils.loadNativeCollabAds
import com.ocmaker.fullbody.creator.utils.show
import com.ocmaker.fullbody.creator.utils.showInter
import dagger.hilt.android.AndroidEntryPoint
import java.io.File
import javax.inject.Inject
import kotlin.getValue

@AndroidEntryPoint
class SuccessActivity : AbsBaseActivity<ActivitySuccessBinding>() {
    var path = ""
    override fun getLayoutId(): Int = R.layout.activity_success
    private val permissionViewModel: PermissionViewModel by viewModels()

    @Inject
    lateinit var sharedPreference: SharedPreferenceUtils
    override fun initView() {
        loadNativeCollabAds(
            getString(R.string.native_success), binding.flNativeCollab
        )

        path = intent.getStringExtra("path").toString()
        Glide.with(this@SuccessActivity).load(path).into(binding.imv)
        binding.apply {
            tvDownload.isSelected = true
            tvMyWork.isSelected = true
            tvTitle.isSelected = true
        }
    }

    override fun initAction() {
        binding.apply {
            imvBack.onSingleClick { finish() }
            imvShare.onClick {
                shareListFiles(
                    this@SuccessActivity,
                    arrayListOf(path)
                )
            }
            imvHome.onSingleClick {
                showInter {
                    startActivity(
                        newIntent(
                            this@SuccessActivity,
                            MainActivity::class.java
                        )
                    )
                    finish()
                }
            }
            btnMyWork.onSingleClick {
                showInter {
                    startActivity(
                        newIntent(
                            this@SuccessActivity,
                            MyCreationActivity::class.java
                        )
                    )
                    finish()
                }
            }

            btnDownload.onClick {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    performDownload()
                } else {
                    handlePermissionRequest(isStorage = true)
                }

            }
        }
    }

    private fun handlePermissionRequest(isStorage: Boolean) {
        val permissions = if (isStorage) {
            permissionViewModel.getStoragePermissions()
        } else {
            permissionViewModel.getNotificationPermissions()
        }

        // Kiểm tra đã có permission chưa
        if (PermissionHelper.checkPermissions(permissions, this@SuccessActivity)) {
            performDownload()
            return
        }

        // Kiểm tra nếu đã từ chối nhiều lần → gợi ý vào Settings
        if (permissionViewModel.needGoToSettings(sharedPreference, isStorage)) {
            val dialogRes =
                if (isStorage) R.string.reques_storage else R.string.content_dialog_notification
            showDialogNotifiListener(dialogRes)
            return
        }

        // Request permission bình thường
        val requestCode =
            if (isStorage) CONST.REQUEST_STORAGE_PERMISSION else REQUEST_NOTIFICATION_PERMISSION
        ActivityCompat.requestPermissions(this, permissions, requestCode)
    }

    private fun performDownload() {
        saveFileToExternalStorage(
            applicationContext,
            path,
            ""
        ) { check, path ->
            if (check) {
                Toast.makeText(
                    applicationContext,
                    getString(R.string.download_successfully) + " " + CONST.NAME_SAVE_FILE,
                    Toast.LENGTH_SHORT
                ).show()
                scanMediaFile(this@SuccessActivity, File(path))
            } else {
                showToast(this@SuccessActivity, R.string.download_failed)
            }
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        val isGranted = grantResults.isNotEmpty() &&
                grantResults.all { it == PackageManager.PERMISSION_GRANTED }

        when (requestCode) {
            CONST.REQUEST_STORAGE_PERMISSION -> {
                permissionViewModel.updateStorageGranted(sharedPreference, isGranted)

                if (isGranted) {
                    performDownload()
                }
            }
        }

    }

}