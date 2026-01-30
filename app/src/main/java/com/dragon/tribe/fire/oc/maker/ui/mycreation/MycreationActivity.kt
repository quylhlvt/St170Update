package com.dragon.tribe.fire.oc.maker.ui.mycreation

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.widget.Toast
import androidx.activity.viewModels
import androidx.core.app.ActivityCompat
import androidx.core.app.ActivityOptionsCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.dragon.tribe.fire.oc.maker.R
import com.dragon.tribe.fire.oc.maker.core.base.BaseActivity
import com.dragon.tribe.fire.oc.maker.core.dialog.ConfirmDialog
import com.dragon.tribe.fire.oc.maker.core.extensions.checkPermissions
import com.dragon.tribe.fire.oc.maker.core.extensions.goToSettings
import com.dragon.tribe.fire.oc.maker.core.extensions.handleBack
import com.dragon.tribe.fire.oc.maker.core.extensions.hideNavigation
import com.dragon.tribe.fire.oc.maker.core.extensions.onSingleClick
import com.dragon.tribe.fire.oc.maker.core.extensions.requestPermission
import com.dragon.tribe.fire.oc.maker.core.extensions.showInterAll
import com.dragon.tribe.fire.oc.maker.core.extensions.showToast
import com.dragon.tribe.fire.oc.maker.core.helper.MediaHelper
import com.dragon.tribe.fire.oc.maker.core.helper.SharePreferenceHelper
import com.dragon.tribe.fire.oc.maker.core.utils.HandleState
import com.dragon.tribe.fire.oc.maker.core.utils.KeyApp.STORAGE_PERMISSION_CODE
import com.dragon.tribe.fire.oc.maker.core.utils.SystemUtils.setLocale
import com.dragon.tribe.fire.oc.maker.core.utils.key.IntentKey
import com.dragon.tribe.fire.oc.maker.core.utils.key.RequestKey
import com.dragon.tribe.fire.oc.maker.core.utils.key.ValueKey
import com.dragon.tribe.fire.oc.maker.core.utils.key.ValueKey.ALBUM_BACKGROUND
import com.dragon.tribe.fire.oc.maker.data.model.MyCreationModel
import com.dragon.tribe.fire.oc.maker.databinding.ActivityMycreationBinding
import com.dragon.tribe.fire.oc.maker.ui.permission.PermissionViewModel
import com.dragon.tribe.fire.oc.maker.ui.view.ViewActivity
import com.lvt.ads.util.Admob
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import java.io.File
import kotlin.getValue

class MycreationActivity : BaseActivity<ActivityMycreationBinding>() {
    private lateinit var adapter: MyCreationAdapter
    private var isProcessing = false
    private val permissionViewModel: PermissionViewModel by viewModels()

    private var currentJob: Job? = null
    private var shouldReload = false

    override fun setViewBinding(): ActivityMycreationBinding {
        return ActivityMycreationBinding.inflate(LayoutInflater.from(this))
    }

    override fun initView() {
        binding.apply {
            txtShare.isSelected = true
            txtDownLoad.isSelected = true
        }
        adapter = MyCreationAdapter(this)
        binding.rcv.apply {
            layoutManager = GridLayoutManager(this@MycreationActivity, 2)
            adapter = this@MycreationActivity.adapter
            itemAnimator = null
        }

        loadInternalImages()
        setupAdapterClick()
    }

    override fun viewListener() {
        binding.apply {
            btnBack.onSingleClick {
                if (adapter.isSelectionMode()) {
                    clearSelectionMode()
                } else {
                    showInterAll {
                        handleBack()
                    }
                }
            }

            btnShare.onSingleClick(2000) {
                if (adapter.getSelectedCount() > 0)

                    safeShareImages() else showToast(R.string.select_imgae)
            }
            btnDowLoad.onSingleClick {
                if (adapter.getSelectedCount() > 0)
                    checkPermissionAndDownload()
                else showToast(R.string.select_imgae)
            }

            btnDeleteTick.onSingleClick {
                confirmDelete()
            }
        }
    }

    private fun checkPermissionAndDownload() {
        // Android 13 trở lên => không cần xin quyền
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
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
        } else {
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
        val granted =
            grantResults.isNotEmpty() && grantResults.all { it == PackageManager.PERMISSION_GRANTED }

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


    override fun onPause() {
        super.onPause()
        shouldReload = !adapter.isSelectionMode()
    }

    override fun onResume() {
        super.onResume()
        if (shouldReload) {
            loadInternalImages()
            shouldReload = !adapter.isSelectionMode()
        }
    }

    override fun initText() {}

    private fun loadInternalImages() {
        val paths = MediaHelper.getImageInternal(this, ALBUM_BACKGROUND)
        binding.noData.visibility = if (paths.isEmpty()) View.VISIBLE else View.GONE

        val listModels = paths.map { path -> MyCreationModel(path = path) }
        adapter.submitList(ArrayList(listModels))
        updateBackIcon()
    }

    private fun setupAdapterClick() {
        adapter.onItemClick = { path ->
            val position = adapter.listMyLibrary.indexOfFirst { it.path == path }
            if (position != -1) {
                if (adapter.isSelectionMode()) {
                    clearSelectionMode()
                }
                val intent = Intent(this@MycreationActivity, ViewActivity::class.java)
                intent.putExtra(IntentKey.INTENT_KEY, path)
                intent.putExtra(IntentKey.TYPE_KEY, ValueKey.TYPE_VIEW)
                val options = ActivityOptionsCompat.makeCustomAnimation(
                    this@MycreationActivity, R.anim.slide_in_right, R.anim.slide_out_left
                )
                lifecycleScope.launch {
                    dismissLoading(true)
                    showInterAll {
                        startActivity(intent, options.toBundle())
                    }
                }
            } else {
                // 👉 Không tìm thấy ảnh (phòng lỗi)
                Toast.makeText(this, "Image not found", Toast.LENGTH_SHORT).show()

            }
        }

        adapter.onLongClick = { position ->
            // 👉 Nhấn giữ → bật chế độ chọn
            if (!adapter.isSelectionMode()) {
                adapter.enableSelectionMode(true)

                binding.layoutBot.visibility = View.VISIBLE
                binding.apply {
                    rcv.setPadding(rcv.paddingLeft, rcv.paddingTop, rcv.paddingRight, dpToPx(50))
                }
            }
            adapter.submitItem(position, true)
            updateDeleteButtonVisibility()
        }

        adapter.onItemTick = { position ->
            // 👉 Tick icon → toggle chọn/bỏ chọn
            if (adapter.isSelectionMode()) {
                lifecycleScope.launch {
                    dismissLoading(true)
                    adapter.toggleSelect(position)
                    updateDeleteButtonVisibility()
                }
            }
        }
    }


    private fun updateDeleteButtonVisibility() {

        updateBackIcon()
    }

    private fun updateBackIcon() {
        if (adapter.isSelectionMode()) {
            binding.btnBack.setImageResource(R.drawable.ic_exit)
            binding.btnDeleteTick.visibility = View.VISIBLE

        } else {
            binding.btnBack.setImageResource(R.drawable.back_language)
            binding.btnDeleteTick.visibility = View.GONE
        }
    }

    private fun confirmDelete() {
        val selectedCount = adapter.getSelectedCount()
        if (selectedCount == 0) {
            Toast.makeText(this, getString(R.string.select_imgae), Toast.LENGTH_SHORT).show()
            return
        }

        val dialog = ConfirmDialog(this, R.string.delete, R.string.do_you_want_to_delete)
        setLocale(this)
        dialog.onYesClick = {
            deleteSelectedImages()
            dialog.dismiss()
        }
        dialog.onNoClick = {
            dialog.dismiss()
            hideNavigation()
        }
        dialog.show()
    }


    private fun deleteSelectedImages() {
        val selectedPaths = adapter.listMyLibrary.filter { it.isSelected }.map { it.path }
        if (selectedPaths.isEmpty()) return

        lifecycleScope.launch {
            MediaHelper.deleteFileByPath(ArrayList(selectedPaths)).collect { state ->
                when (state) {
                    HandleState.LOADING -> showLoading()
                    HandleState.SUCCESS -> {
                        dismissLoading(true)
                        loadInternalImages()
                        clearSelectionMode()
                        showToast(R.string.delete_success)
                    }

                    else -> {
                        dismissLoading(true)
                        Toast.makeText(
                            this@MycreationActivity, "Xóa thất bại", Toast.LENGTH_SHORT
                        ).show()
                    }
                }
            }
        }
    }

    private fun safeShareImages() {
        if (isProcessing) return
        isProcessing = true

        currentJob?.cancel()
        currentJob = lifecycleScope.launch {
            try {
                val selectedPaths = adapter.listMyLibrary.filter { it.isSelected }.map { it.path }
                if (selectedPaths.isEmpty()) {
                    showToast(R.string.select_imgae)
                    return@launch
                }

                showLoading()

                val uris = ArrayList<android.net.Uri>()
                for (path in selectedPaths) {
                    val file = File(path)
                    val uri = androidx.core.content.FileProvider.getUriForFile(
                        this@MycreationActivity, "${packageName}.fileprovider", file
                    )
                    uris.add(uri)
                }

                val intent = Intent(Intent.ACTION_SEND_MULTIPLE).apply {
                    type = "image/*"
                    putParcelableArrayListExtra(Intent.EXTRA_STREAM, uris)
                    addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                }

                startActivity(Intent.createChooser(intent, "Chia sẻ ảnh bằng..."))
                clearSelectionMode()
            } catch (e: Exception) {
                e.printStackTrace()
                showToast(R.string.error)
            } finally {
                dismissLoading(true)
                isProcessing = false
            }
        }
    }

    /** --- DOWNLOAD --- **/
    private fun safeDownloadImages() {
        if (isProcessing) return
        isProcessing = true

        currentJob?.cancel()
        currentJob = lifecycleScope.launch {
            try {
                val selectedPaths = adapter.listMyLibrary.filter { it.isSelected }.map { it.path }
                if (selectedPaths.isEmpty()) {
                    showToast(R.string.select_imgae)
                    return@launch
                }

                MediaHelper.downloadPartsToExternal(this@MycreationActivity, selectedPaths)
                    .collect { state ->
                        when (state) {
                            HandleState.LOADING -> showLoading()
                            HandleState.SUCCESS -> {
                                dismissLoading(true)
                                showToast(getString(R.string.download_success))
                                clearSelectionMode()
                            }

                            else -> {
                                dismissLoading(true)
                            }
                        }
                    }
            } finally {
                isProcessing = false
            }
        }
    }


    private fun clearSelectionMode() {
        adapter.clearAllSelections()
        adapter.enableSelectionMode(false)
        binding.layoutBot.visibility = View.GONE
        binding.apply {
            rcv.setPadding(rcv.paddingLeft, rcv.paddingTop, rcv.paddingRight, 0)
        }
        updateBackIcon()
    }

    override fun initAds() {
        Admob.getInstance()
            .loadNativeCollap(this, getString(R.string.native_creation), binding.nativeAds2)
        Admob.getInstance().loadNativeAd(
            this,
            getString(R.string.native_cl_my_creation),
            binding.nativeAds,
            R.layout.ads_native_collap_banner_1
        )
    }
}
