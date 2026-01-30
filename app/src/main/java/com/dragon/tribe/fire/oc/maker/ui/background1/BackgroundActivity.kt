package com.dragon.tribe.fire.oc.maker.ui.background1

import android.view.LayoutInflater
import androidx.activity.viewModels
import androidx.lifecycle.lifecycleScope
import com.dragon.tribe.fire.oc.maker.R
import android.content.Intent
import androidx.core.app.ActivityOptionsCompat
import com.dragon.tribe.fire.oc.maker.core.extensions.handleBack
import com.dragon.tribe.fire.oc.maker.core.extensions.onSingleClick
import com.dragon.tribe.fire.oc.maker.core.extensions.showToast
import com.dragon.tribe.fire.oc.maker.core.utils.SaveState
import com.dragon.tribe.fire.oc.maker.core.utils.key.ValueKey
import com.dragon.tribe.fire.oc.maker.ui.success.SuccessActivity
import kotlinx.coroutines.launch
import com.dragon.tribe.fire.oc.maker.core.base.BaseActivity
import com.dragon.tribe.fire.oc.maker.core.extensions.loadImageGlide
import com.dragon.tribe.fire.oc.maker.core.extensions.showInterAll
import com.dragon.tribe.fire.oc.maker.core.utils.key.IntentKey
import com.dragon.tribe.fire.oc.maker.databinding.ActivityBackgroundBinding
import com.lvt.ads.util.Admob

class BackgroundActivity : BaseActivity<ActivityBackgroundBinding>() {
    private val backgroundAdapter by lazy { BackgroundAdapter(this) }
    private val viewModel: BackgroundViewModel by viewModels()
    private var savedImagePath: String = ""       // path ảnh vừa lưu
    private var selectedBackgroundPath: String? = null // path background

    override fun setViewBinding(): ActivityBackgroundBinding {
        return ActivityBackgroundBinding.inflate(LayoutInflater.from(this))
    }

    override fun initView() {
        binding.txtSave.isSelected=true
        savedImagePath = intent.getStringExtra(IntentKey.PREVIOUS_IMAGE_KEY) ?: ""
        selectedBackgroundPath = intent.getStringExtra(IntentKey.BACKGROUND_IMAGE_KEY)

        if (savedImagePath.isNotEmpty()) {
            loadImageGlide(this, savedImagePath, binding.ivPreviousImage, false)
            viewModel.setPathInternalTemp(savedImagePath)
        }


        binding.rcvLayer.adapter = backgroundAdapter
        binding.rcvLayer.itemAnimator = null

        viewModel.loadBackground(this)
    }


    override fun dataObservable() {
        lifecycleScope.launch {
            viewModel.backgroundList.collect { list ->
                backgroundAdapter.submitList(list)

                selectedBackgroundPath?.let { selectedPath ->
                    val index = list.indexOfFirst { it.path == selectedPath }
                    if (index != -1) {
                        viewModel.changeFocusBackgroundList(index)
                        backgroundAdapter.notifyItemChanged(index)

                        // 🔹 Load lại background tương ứng khi khởi tạo
                        val fullPath = if (selectedPath.startsWith("file:///")) selectedPath
                        else "file:///android_asset/$selectedPath"
                        loadImageGlide(this@BackgroundActivity, fullPath, binding.ivBackground, false)
                    } else {
                        // 🔹 Nếu là None thì xóa background
                        binding.ivBackground.setImageDrawable(null)
                    }
                }
            }
        }
    }


    override fun initText() {

    }

    override fun viewListener() {
        binding.apply {
            actionBar.apply {
                btnBack.onSingleClick { handleBack() }
                btnSave.onSingleClick { handleSave() }
            }
            backgroundAdapter.onItemClick = { item, position ->
                val path = item.path

                if (path.isNullOrEmpty()) {
                    selectedBackgroundPath = null
                    binding.ivBackground.setImageDrawable(null)
                } else {
                    selectedBackgroundPath = path
                    val fullPath = if (path.startsWith("file:///")) path
                    else "file:///android_asset/$path"
                    loadImageGlide(this@BackgroundActivity, fullPath, binding.ivBackground, false)
                }

                viewModel.changeFocusBackgroundList(position)
            }



        }

    }


    private fun handleSave() {
        lifecycleScope.launch {
            binding.apply {
                viewModel.saveImageFromView(this@BackgroundActivity, layoutCustomLayer).collect { result ->
                    when (result) {
                        is SaveState.Loading -> showLoading()
                        is SaveState.Error -> {
                            dismissLoading(true)
                            showToast(R.string.save_failed_please_try_again)
                        }

                        is SaveState.Success -> {
                            dismissLoading(true)
                            showInterAll {
                            val intent = Intent(this@BackgroundActivity, SuccessActivity::class.java)
                            intent.putExtra(IntentKey.INTENT_KEY, result.path)
                            intent.putExtra(IntentKey.TYPE_KEY, ValueKey.TYPE_SUCCESS)
                            val options = ActivityOptionsCompat.makeCustomAnimation(
                                this@BackgroundActivity, R.anim.slide_in_right, R.anim.slide_out_left
                            )
                            startActivity(intent, options.toBundle())
                        }}
                    }
                }
            }
        }

    }
    override fun initAds() {
        Admob.getInstance().loadNativeCollap(this, getString(R.string.native_cl_bg), binding.nativeAds2)
    }

    override fun onRestart() {
        super.onRestart()
        Admob.getInstance().loadNativeCollap(this, getString(R.string.native_cl_bg), binding.nativeAds2)
    }

}