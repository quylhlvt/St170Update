package com.dragon.tribe.fire.oc.maker.ui.category

import android.util.Log
import android.view.LayoutInflater
import androidx.activity.viewModels
import androidx.lifecycle.lifecycleScope
import com.dragon.tribe.fire.oc.maker.R
import com.dragon.tribe.fire.oc.maker.core.base.BaseActivity
import com.dragon.tribe.fire.oc.maker.core.dialog.NoInternetDialog
import com.dragon.tribe.fire.oc.maker.core.extensions.handleBack
import com.dragon.tribe.fire.oc.maker.core.extensions.onSingleClick
import com.dragon.tribe.fire.oc.maker.core.extensions.showInterAll
import com.dragon.tribe.fire.oc.maker.core.extensions.startIntent
import com.dragon.tribe.fire.oc.maker.core.extensions.startIntentDataAnim
import com.dragon.tribe.fire.oc.maker.core.extensions.startIntentRightToLeft
import com.dragon.tribe.fire.oc.maker.core.helper.InternetHelper
import com.dragon.tribe.fire.oc.maker.core.utils.HandleState
import com.dragon.tribe.fire.oc.maker.core.utils.key.IntentKey.FROM_CATEGORY
import com.dragon.tribe.fire.oc.maker.core.utils.key.IntentKey.STATUS_FROM_KEY
import com.dragon.tribe.fire.oc.maker.core.utils.key.ValueKey
import com.dragon.tribe.fire.oc.maker.databinding.ActivityCategoryBinding
import com.dragon.tribe.fire.oc.maker.ui.customize.CustomizeActivity
import com.dragon.tribe.fire.oc.maker.ui.home.DataViewModel
import com.lvt.ads.util.Admob
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.compareTo
import kotlin.text.compareTo

class CategoryActivity : BaseActivity<ActivityCategoryBinding>() {
    private val dataViewModel: DataViewModel by viewModels()
    private val avatarAdapter by lazy { CategoryAdapter(this) }
    override fun setViewBinding():ActivityCategoryBinding {
        return ActivityCategoryBinding.inflate(LayoutInflater.from(this))
    }

    override fun initView() {
        initRcv()
        lifecycleScope.launch {
            showLoading()
            delay(300)
            dataViewModel.ensureData(this@CategoryActivity)
        }
    }
    override fun dataObservable() {
        lifecycleScope.launch {
            dataViewModel.allData.collect { list ->
                if (list.isNotEmpty()) {
                    dismissLoading()
                    avatarAdapter.submitList(list)
                }
            }
        }
    }

    override fun viewListener() {
        binding.apply {
            btnBack.onSingleClick {
                showInterAll { handleBack() }
            }
            swipeRefreshLayout.setOnRefreshListener {
                refreshData()
            }
        }
        handleRcv()
    }

    override fun initText() {

    }

    private fun initRcv() {
        binding.apply {
            rcv.adapter = avatarAdapter
            rcv.itemAnimator = null
        }
    }
    private fun handleRcv() {
        binding.apply {
            avatarAdapter.onItemClick = { path, position ->
                    if (position>=ValueKey.POSITION_API) {
                        InternetHelper.checkInternet(this@CategoryActivity) { state ->
                            when (state) {
                                HandleState.SUCCESS -> {
                                    showInterAll {

                                    startIntentRightToLeft(CustomizeActivity::class.java, position)}
                                }
                                HandleState.FAIL -> {
                                    NoInternetDialog(this@CategoryActivity).apply {
                                        onOkClick = {
                                            dismiss()
                                        }
                                    }.show()
                                }

                                else -> {

                                }
                            }
                        }
                    } else {
                        // Dữ liệu local → mở luôn
                        startIntentRightToLeft(CustomizeActivity::class.java, position)

                }
            }
        }


    }


    private fun refreshData(){
        if (dataViewModel.allData.value.size < ValueKey.POSITION_API && InternetHelper.checkInternet(this)){
            lifecycleScope.launch {
                showLoading()
                delay(300)
                dataViewModel.ensureData(this@CategoryActivity)
                binding.swipeRefreshLayout.isRefreshing = false
            }
        }else{
            binding.swipeRefreshLayout.isRefreshing = false
        }
    }
    override fun initAds() {
        super.initAds()
        Admob.getInstance().loadNativeCollap(this, getString(R.string.native_cl_category), binding.nativeAds2)
    }

    override fun onRestart() {
        super.onRestart()
        Admob.getInstance().loadNativeCollap(this, getString(R.string.native_cl_category), binding.nativeAds2)
    }

}