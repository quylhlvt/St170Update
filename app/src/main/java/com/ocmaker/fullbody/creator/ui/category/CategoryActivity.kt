package com.ocmaker.fullbody.creator.ui.category

import com.lvt.ads.util.Admob
import com.ocmaker.fullbody.creator.R
import com.ocmaker.fullbody.creator.base.AbsBaseActivity
import com.ocmaker.fullbody.creator.databinding.ActivityCategoryBinding
import com.ocmaker.fullbody.creator.dialog.DialogExit
import com.ocmaker.fullbody.creator.ui.customview.CustomviewActivity
import com.ocmaker.fullbody.creator.utils.DataHelper
import com.ocmaker.fullbody.creator.utils.isInternetAvailable
import com.ocmaker.fullbody.creator.utils.newIntent
import com.ocmaker.fullbody.creator.utils.onSingleClick
import com.ocmaker.fullbody.creator.data.repository.ApiRepository
import com.ocmaker.fullbody.creator.utils.logEvent
import com.ocmaker.fullbody.creator.utils.showInter
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject
import kotlin.text.get

@AndroidEntryPoint
class CategoryActivity : AbsBaseActivity<ActivityCategoryBinding>() {
    @Inject
    lateinit var apiRepository: ApiRepository
    val adapter by lazy { CategoryAdapter() }

    override fun getLayoutId(): Int = R.layout.activity_category



    override fun onRestart() {
        super.onRestart()
        initNativeCollab()
    }
    private fun initNativeCollab() {
        Admob.getInstance().loadNativeCollapNotBanner(
            this,
            getString(R.string.native_cl_category),
            binding.flNativeCollab
        )
    }
    override fun initView() {
        initNativeCollab()
        Admob.getInstance().loadNativeAd(
            this, getString(R.string.native_category), binding.nativeAds, R.layout.ads_native_banner
        )
        if (DataHelper.arrBlackCentered.size<2 && !isInternetAvailable(this@CategoryActivity)) {
            DialogExit(
                this@CategoryActivity,
                "awaitdataHome"
            ).show()
        }

        if (DataHelper.arrBg.size == 0) {
//            GlobalScope.launch(Dispatchers.IO) {
//                getData(apiRepository)
//            }
            finish()
        } else {
            binding.rcv.itemAnimator = null
            binding.rcv.adapter = adapter
            adapter.submitList(DataHelper.arrBlackCentered)
        }
    }

    override fun onBackPressed() {
        super.onBackPressed()
    }

    override fun initAction() {
        binding.apply {
            imvBack.onSingleClick {
                showInter {
                    finish()
                }
            }
            adapter.onCLick = {
                if (DataHelper.arrBlackCentered[it].checkDataOnline) {
                    if (isInternetAvailable(this@CategoryActivity)) {
                            var a = DataHelper.arrBlackCentered[it].avt.split("/")
                            var b = a[a.size - 1]
                        logEvent("click_item_$b", DataHelper.arrBlackCentered[it].avt)

                        showInter {
                            startActivity(
                                newIntent(
                                    this@CategoryActivity,
                                    CustomviewActivity::class.java
                                ).putExtra("data", it)
                            )
                        }
                    } else {
                             DialogExit(
                                 this@CategoryActivity,
                                 "network"
                             ).show()
                    }
                } else {
                        var a = DataHelper.arrBlackCentered[it].avt.split("/")
                        var b = a[a.size - 1]

                        startActivity(
                            newIntent(
                                this@CategoryActivity,
                                CustomviewActivity::class.java
                            ).putExtra("data", it)
                        )

                }
            }
        }
    }
}