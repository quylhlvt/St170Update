package com.dragon.tribe.fire.oc.maker.ui.splash

import android.content.Intent
import androidx.lifecycle.lifecycleScope
import com.dragon.tribe.fire.oc.maker.base.AbsBaseActivity
import com.dragon.tribe.fire.oc.maker.data.callapi.reponse.DataResponse
import com.dragon.tribe.fire.oc.maker.data.callapi.reponse.LoadingStatus
import com.dragon.tribe.fire.oc.maker.data.model.BodyPartModel
import com.dragon.tribe.fire.oc.maker.data.model.ColorModel
import com.dragon.tribe.fire.oc.maker.data.model.CustomModel
import com.dragon.tribe.fire.oc.maker.data.repository.ApiRepository
import com.dragon.tribe.fire.oc.maker.ui.language.LanguageActivity
import com.dragon.tribe.fire.oc.maker.ui.tutorial.TutorialActivity
import com.dragon.tribe.fire.oc.maker.utils.CONST
import com.dragon.tribe.fire.oc.maker.utils.DataHelper
import com.dragon.tribe.fire.oc.maker.utils.DataHelper.getData
import com.dragon.tribe.fire.oc.maker.utils.SharedPreferenceUtils
import com.lvt.ads.callback.InterCallback
import com.lvt.ads.util.Admob
import com.dragon.tribe.fire.oc.maker.R
import com.dragon.tribe.fire.oc.maker.databinding.ActivitySplashBinding
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import javax.inject.Inject
import kotlin.collections.toList

@AndroidEntryPoint
class SplashActivity : AbsBaseActivity<ActivitySplashBinding>() {
    @Inject
    lateinit var apiRepository: ApiRepository
    var interCallBack: InterCallback? = null

    @Inject
    lateinit var sharedPreferenceUtils: SharedPreferenceUtils

    private var minDelayPassed = false
    private var dataReady = false

    override fun getLayoutId(): Int = R.layout.activity_splash

    override fun initView() {
        observeDataLoading()
//        Admob.getInstance().setOpenShowAllAds(false)
        // Observe data loading TRƯỚC khi load
        Admob.getInstance().setTimeLimitShowAds(30000)
        Admob.getInstance().setTimeCountdownNativeCollab(20000)
        interCallBack = object : InterCallback() {
            override fun onNextAction() {
                super.onNextAction()
                lifecycleScope.launch {
                    minDelayPassed = true
                    // Nếu data đã sẵn sàng thì chuyển màn ngay
                    if (dataReady) {
                        navigateToNextScreen()
                    }
                }
            }
        }
        Admob.getInstance().loadSplashInterAds(
            this@SplashActivity, getString(R.string.inter_splash), 30000, 3000, interCallBack
        )

        // Đợi tối thiểu 3 giây

    }
    override fun onResume() {
        super.onResume()
        Admob.getInstance().onCheckShowSplashWhenFail(this, interCallBack, 1000)
    }

    override fun initAction() {
        // Bắt đầu load data
        lifecycleScope.launch(Dispatchers.IO) {
            getData(apiRepository)
        }
    }

    private fun observeDataLoading() {
        DataHelper.arrDataOnline.observe(this) { response ->
            response?.let {
                when (it.loadingStatus) {
                    LoadingStatus.Loading -> {
                        // Đang loading
                    }

                    LoadingStatus.Success -> {
                        // XỬ LÝ DATA ONLINE (logic từ MainActivity cũ)
                        if (DataHelper.arrBlackCentered.isNotEmpty() && !DataHelper.arrBlackCentered[0].checkDataOnline) {
                            val listA = (it as DataResponse.DataSuccess).body

                            if (listA != null) {
                                // Sort và merge data online
                                val sortedMap = listA
                                    .toList()
                                    .sortedBy { (_, list) ->
                                        list.firstOrNull()?.level ?: Int.MAX_VALUE
                                    }
                                    .toMap()

                                sortedMap.forEach { key, list ->
                                    val bodyPartList = arrayListOf<BodyPartModel>()

                                    list.forEach { x10 ->
                                        val colorList = arrayListOf<ColorModel>()

                                        x10.colorArray.split(",").forEach { color ->
                                            val pathList = arrayListOf<String>()

                                            if (color == "") {
                                                for (i in 1..x10.quantity) {
                                                    pathList.add(CONST.BASE_URL + "${CONST.BASE_CONNECT}/${x10.position}/${x10.parts}/${i}.png")
                                                }
                                                colorList.add(ColorModel("#", pathList))
                                            } else {
                                                for (i in 1..x10.quantity) {
                                                    pathList.add(CONST.BASE_URL + "${CONST.BASE_CONNECT}/${x10.position}/${x10.parts}/${color}/${i}.png")
                                                }
                                                colorList.add(ColorModel(color, pathList))
                                            }
                                        }

                                        bodyPartList.add(
                                            BodyPartModel(
                                                "${CONST.BASE_URL}${CONST.BASE_CONNECT}$key/${x10.parts}/nav.png",
                                                colorList
                                            )
                                        )
                                    }

                                    val dataModel = CustomModel(
                                        "${CONST.BASE_URL}${CONST.BASE_CONNECT}$key/avatar.png",
                                        bodyPartList,
                                        true
                                    )

                                    // Thêm "dice" và "none"
                                    dataModel.bodyPart.forEach { mbodyPath ->
                                        if (mbodyPath.icon.substringBeforeLast("/")
                                                .substringAfterLast("/").substringAfter("-") == "1"
                                        ) {
                                            mbodyPath.listPath.forEach { colorModel ->
                                                if (colorModel.listPath[0] != "dice") {
                                                    colorModel.listPath.add(0, "dice")
                                                }
                                            }
                                        } else {
                                            mbodyPath.listPath.forEach { colorModel ->
                                                if (colorModel.listPath[0] != "none") {
                                                    colorModel.listPath.add(0, "none")
                                                    colorModel.listPath.add(1, "dice")
                                                }
                                            }
                                        }
                                    }

                                    DataHelper.arrBlackCentered.add(0, dataModel)
                                }
                            }
                        }

                        // Set dataReady sau khi merge xong
                        dataReady = true

                        // Nếu đã qua 3 giây thì chuyển màn ngay
                        if (minDelayPassed) {
                            navigateToNextScreen()
                        }
                    }

                    LoadingStatus.Error -> {
                        // Nếu lỗi nhưng đã có data offline thì vẫn cho qua
                        if (DataHelper.arrBlackCentered.isNotEmpty()) {
                            dataReady = true
                            if (minDelayPassed) {
                                navigateToNextScreen()
                            }
                        } else {
                            // Thử load lại sau 2 giây
                            lifecycleScope.launch(Dispatchers.IO) {
                                delay(2000)
                                getData(apiRepository)
                            }
                        }
                    }

                    else -> {
                        // Loading hoặc trạng thái khác - đợi
                    }
                }
            }
        }
    }

    private fun navigateToNextScreen() {
        // Double-check: CHỈ navigate khi data thực sự sẵn sàng
        if (!dataReady || DataHelper.arrBlackCentered.isEmpty()) {
            return
        }

        if (!sharedPreferenceUtils.getBooleanValue(CONST.LANGUAGE)) {
            startActivity(Intent(this@SplashActivity, LanguageActivity::class.java))
        } else {
            startActivity(Intent(this@SplashActivity, TutorialActivity::class.java))
        }
        finish()
    }

    override fun onBackPressed() {
        // Không cho phép back ở splash
    }
}