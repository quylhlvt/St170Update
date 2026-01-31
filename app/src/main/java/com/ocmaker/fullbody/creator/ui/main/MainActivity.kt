package com.ocmaker.fullbody.creator.ui.main

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.net.ConnectivityManager
import androidx.lifecycle.lifecycleScope
import com.lvt.ads.util.Admob
import com.ocmaker.fullbody.creator.R
import com.ocmaker.fullbody.creator.base.AbsBaseActivity
import com.ocmaker.fullbody.creator.data.callapi.reponse.LoadingStatus
import com.ocmaker.fullbody.creator.data.model.BodyPartModel
import com.ocmaker.fullbody.creator.data.model.ColorModel
import com.ocmaker.fullbody.creator.data.model.CustomModel
import com.ocmaker.fullbody.creator.databinding.ActivityMainBinding
import com.ocmaker.fullbody.creator.dialog.DialogExit
import com.ocmaker.fullbody.creator.ui.category.CategoryActivity
import com.ocmaker.fullbody.creator.ui.my_creation.MyCreationActivity
import com.ocmaker.fullbody.creator.ui.quick_mix.QuickMixActivity
import com.ocmaker.fullbody.creator.ui.setting.SettingActivity
import com.ocmaker.fullbody.creator.utils.CONST
import com.ocmaker.fullbody.creator.utils.DataHelper
import com.ocmaker.fullbody.creator.utils.DataHelper.getData
import com.ocmaker.fullbody.creator.utils.SharedPreferenceUtils
import com.ocmaker.fullbody.creator.utils.backPress
import com.ocmaker.fullbody.creator.utils.newIntent
import com.ocmaker.fullbody.creator.utils.onSingleClick
import com.ocmaker.fullbody.creator.data.callapi.reponse.DataResponse
import com.ocmaker.fullbody.creator.data.repository.ApiRepository
import com.ocmaker.fullbody.creator.ui.quick_mix.QuickAdapter
import com.ocmaker.fullbody.creator.utils.isInternetAvailable
import com.ocmaker.fullbody.creator.utils.showInter
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : AbsBaseActivity<ActivityMainBinding>() {
    private var sizeMix = 100
    private var isLoading = false

    private val arrMix = arrayListOf<CustomModel>()
    val adapter by lazy { QuickAdapter(this@MainActivity) }
    @Volatile
    private var isNetworkAvailable = true

    @Volatile
    var isOfflineMode = false
    @Inject
    lateinit var apiRepository: ApiRepository
    var checkCallingDataOnline = false
    override fun getLayoutId(): Int = R.layout.activity_main
    private var networkReceiver: BroadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            val connectivityManager =
                context?.getSystemService(CONNECTIVITY_SERVICE) as ConnectivityManager
            val networkInfo = connectivityManager.activeNetworkInfo
            if (!checkCallingDataOnline) {
                if (networkInfo != null && networkInfo.isConnected) {
                    var checkDataOnline = false
                    DataHelper.arrBlackCentered.forEach {
                        if (it.checkDataOnline) {
                            checkDataOnline = true
                            return@forEach
                        }
                    }
                    if (!checkDataOnline) {
                        lifecycleScope.launch(Dispatchers.IO) {
                            getData(apiRepository)
                        }
                    }
                } else {
                    if (DataHelper.arrBlackCentered.isEmpty()) {
                        lifecycleScope.launch(Dispatchers.IO) {
                            getData(apiRepository)
                        }
                    }
                }
            }
            val wasAvailable = isNetworkAvailable
            isNetworkAvailable = isInternetAvailable(this@MainActivity)

            // ✅ Update adapter state
            adapter.isNetworkAvailable = isNetworkAvailable

            if (wasAvailable && !isNetworkAvailable) {
                lifecycleScope.launch(Dispatchers.Main) {
                    switchToOfflineMode()
                }
            } else if (!wasAvailable && isNetworkAvailable) {
                lifecycleScope.launch(Dispatchers.Main) {
                    switchToOnlineMode()
                }
            }
        }
    }
    private fun initNativeCollab() {
        Admob.getInstance().loadNativeCollapNotBanner(this, getString(R.string.native_cl_home), binding.flNativeCollab)
    }
    override fun initView() {
        Admob.getInstance().loadInterAll(this@MainActivity, getString(R.string.inter_all))
        initNativeCollab()
        binding.apply {
            txtSticker1.isSelected = true
            txtCreate1.isSelected = true
        }
        val filter = IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION)
        registerReceiver(networkReceiver, filter)
        DataHelper.arrDataOnline.observe(this) {
            it?.let {
                when (it.loadingStatus) {
                    LoadingStatus.Loading -> {
                        checkCallingDataOnline = true
                    }

                    LoadingStatus.Success -> {
                        if (DataHelper.arrBlackCentered.isNotEmpty() && !DataHelper.arrBlackCentered[0].checkDataOnline) {
                            checkCallingDataOnline = false
                            val listA = (it as DataResponse.DataSuccess).body ?: return@observe
                            checkCallingDataOnline = true
                            val sortedMap = listA
                                .toList() // Chuyển map -> list<Pair<String, List<X10>>>
                                .sortedBy { (_, list) ->
                                    list.firstOrNull()?.level ?: Int.MAX_VALUE
                                }
                                .toMap()
                            sortedMap.forEach { key, list ->
                                var a = arrayListOf<BodyPartModel>()
                                list.forEachIndexed { index, x10 ->
                                    var b = arrayListOf<ColorModel>()
                                    x10.colorArray.split(",").forEach { coler ->
                                        var c = arrayListOf<String>()
                                        if (coler == "") {
                                            for (i in 1..x10.quantity) {
                                                c.add(CONST.BASE_URL + "${CONST.BASE_CONNECT}/${x10.position}/${x10.parts}/${i}.png")
                                            }
                                            b.add(
                                                ColorModel(
                                                    "#",
                                                    c
                                                )
                                            )
                                        } else {
                                            for (i in 1..x10.quantity) {
                                                c.add(CONST.BASE_URL + "${CONST.BASE_CONNECT}/${x10.position}/${x10.parts}/${coler}/${i}.png")
                                            }
                                            b.add(
                                                ColorModel(
                                                    coler,
                                                    c
                                                )
                                            )
                                        }
                                    }
                                    a.add(
                                        BodyPartModel(
                                            "${CONST.BASE_URL}${CONST.BASE_CONNECT}$key/${x10.parts}/nav.png",
                                            b
                                        )
                                    )
                                }
                                var dataModel =
                                    CustomModel(
                                        "${CONST.BASE_URL}${CONST.BASE_CONNECT}$key/avatar.png",
                                        a,
                                        true
                                    )
                                dataModel.bodyPart.forEach { mbodyPath ->
                                    if (mbodyPath.icon.substringBeforeLast("/")
                                            .substringAfterLast("/").substringAfter("-") == "1"
                                    ) {
                                        mbodyPath.listPath.forEach {
                                            if (it.listPath[0] != "dice") {
                                                it.listPath.add(0, "dice")
                                            }
                                        }
                                    } else {
                                        mbodyPath.listPath.forEach {
                                            if (it.listPath[0] != "none") {
                                                it.listPath.add(0, "none")
                                                it.listPath.add(1, "dice")
                                            }
                                        }
                                    }
                                }
                                DataHelper.arrBlackCentered.add(0, dataModel)
                            }
                        }
                        checkCallingDataOnline = false
                    }

                    LoadingStatus.Error -> {
                        checkCallingDataOnline = false
                    }

                    else -> {
                        checkCallingDataOnline = true
                    }
                }
            }
        }
    }

    override fun initAction() {
        binding.apply {
            btnWing.onSingleClick {
                if (isDataReady()) {
                    startActivity(
                        newIntent(
                            this@MainActivity,
                            CategoryActivity::class.java
                        )
                    )
                } else {
                    DialogExit(
                        this@MainActivity,
                        "awaitdata"
                    ).show()
                }
            }

            btnMyCreation.onSingleClick {
                if (isDataReady()) {
                    showInter {
                    startActivity(
                        newIntent(
                            this@MainActivity,
                            MyCreationActivity::class.java
                        )
                    )}
                } else {
                        DialogExit(
                            this@MainActivity,
                            "awaitdata"
                        ).show()

                }
            }

            btnSetting.onSingleClick {
                startActivity(
                    newIntent(
                        this@MainActivity,
                        SettingActivity::class.java
                    )
                )
            }
        }
    }

    private fun isDataReady(): Boolean {
        return DataHelper.arrBlackCentered.isNotEmpty()
    }

    override fun onBackPressed() {
        lifecycleScope.launch {
            backPress(
                SharedPreferenceUtils(this@MainActivity)
            )
        }
    }
    override fun onStop() {
        super.onStop()
        try {
            unregisterReceiver(networkReceiver)
        } catch (e: Exception) {

        }
    }
    override fun onRestart() {
        super.onRestart()
        val filter = IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION)
        registerReceiver(networkReceiver, filter)
        initNativeCollab()
    }

}