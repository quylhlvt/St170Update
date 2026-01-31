package com.ocmaker.fullbody.creator.ui.quick_mix

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.net.ConnectivityManager
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.lvt.ads.util.Admob
import com.ocmaker.fullbody.creator.R
import com.ocmaker.fullbody.creator.base.AbsBaseActivity
import com.ocmaker.fullbody.creator.databinding.ActivityQuickMixBinding
import com.ocmaker.fullbody.creator.dialog.DialogExit
import com.ocmaker.fullbody.creator.ui.customview.CustomviewActivity
import com.ocmaker.fullbody.creator.utils.DataHelper
import com.ocmaker.fullbody.creator.utils.isInternetAvailable
import com.ocmaker.fullbody.creator.utils.newIntent
import com.ocmaker.fullbody.creator.utils.onSingleClick
import com.ocmaker.fullbody.creator.data.model.CustomModel
import com.ocmaker.fullbody.creator.data.repository.ApiRepository
import com.ocmaker.fullbody.creator.utils.showInter
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.*
import javax.inject.Inject

@AndroidEntryPoint
class QuickMixActivity : AbsBaseActivity<ActivityQuickMixBinding>() {
    private var sizeMix = 100
    private var isLoading = false

    private val arrMix = arrayListOf<CustomModel>()
    val adapter by lazy { QuickAdapter(this@QuickMixActivity) }
    @Volatile
    private var isNetworkAvailable = true

    @Volatile
    var isOfflineMode = false
    @Inject
    lateinit var apiRepository: ApiRepository




    private val networkReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            val wasAvailable = isNetworkAvailable
            isNetworkAvailable = isInternetAvailable(this@QuickMixActivity)

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

    override fun getLayoutId(): Int = R.layout.activity_quick_mix

    private fun initNativeCollab() {
        Admob.getInstance().loadNativeCollapNotBanner(
            this,
            getString(R.string.native_cl_random),
            binding.flNativeCollab
        )
    }

    override fun onRestart() {
        super.onRestart()
        initNativeCollab()
    }

    override fun initView() {
        initNativeCollab()
        binding.titleQuick.isSelected = true

        registerNetworkReceiver()
        isNetworkAvailable = isInternetAvailable(this@QuickMixActivity)

        // ✅ Sync state với adapter
        adapter.isNetworkAvailable = isNetworkAvailable

        if (DataHelper.arrBg.isEmpty()) {
            finish()
        } else {
            binding.rcv.itemAnimator = null
            binding.rcv.adapter = adapter

            // ✅ Tối ưu RecyclerView
            binding.rcv.setHasFixedSize(true)
            binding.rcv.setItemViewCacheSize(12)
            // ✅ QUAN TRỌNG: Track visible items để ưu tiên load
            binding.rcv.addOnScrollListener(object : RecyclerView.OnScrollListener() {
                override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                    super.onScrolled(recyclerView, dx, dy)
                    updateVisibleRange()
                }
            })

            if (!isNetworkAvailable) {
                isOfflineMode = true
                adapter.isOfflineMode = true
                sizeMix = 25
                loadOfflineLastCharacter()
            } else {
                isOfflineMode = false
                adapter.isOfflineMode = false
                sizeMix = 100
                loadAllItems()
            }
        }
    }

    // ✅ Update visible range cho adapter để ưu tiên load
    private fun updateVisibleRange() {
        val layoutManager = binding.rcv.layoutManager as? LinearLayoutManager ?: return
        val firstVisible = layoutManager.findFirstVisibleItemPosition()
        val lastVisible = layoutManager.findLastVisibleItemPosition()

        if (firstVisible != RecyclerView.NO_POSITION) {
            adapter.updateVisibleRange(firstVisible, lastVisible)
        }
    }

    private fun registerNetworkReceiver() {
        val filter = IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION)
        registerReceiver(networkReceiver, filter)
    }

    private suspend fun switchToOnlineMode() {
        if (!isOfflineMode) return

        withContext(Dispatchers.Main) {
            // ✅ Clear adapter cache
            adapter.clearCache()

            arrMix.clear()
            adapter.arrListImageSortView.clear()
            adapter.listArrayInt.clear()

            isOfflineMode = false
            adapter.isOfflineMode = false
            sizeMix = 100

            loadAllItems()
        }
    }

    private suspend fun switchToOfflineMode() {
        if (isOfflineMode) return

        withContext(Dispatchers.Main) {
            // ✅ Clear adapter cache
            adapter.clearCache()

            arrMix.clear()
            adapter.arrListImageSortView.clear()
            adapter.listArrayInt.clear()

            isOfflineMode = true
            adapter.isOfflineMode = true
            sizeMix = 25

            loadOfflineLastCharacter()

            adapter.submitList(ArrayList(arrMix))
            adapter.notifyDataSetChanged()

            DialogExit(this@QuickMixActivity, "network").show()
        }
    }

    private fun loadOfflineLastCharacter() {
        val lastModel = DataHelper.arrBlackCentered.lastOrNull() ?: return
        val tempArrMix = arrayListOf<CustomModel>()
        val tempArrListImageSortView = mutableListOf<ArrayList<String>>()
        val resultList = mutableListOf<ArrayList<ArrayList<Int>>>()

        repeat(sizeMix) {
            val list = ArrayList<String>().apply {
                repeat(lastModel.bodyPart.size) { add("") }
            }
            lastModel.bodyPart.forEach {
                val (x, _) = it.icon.substringBeforeLast("/")
                    .substringAfterLast("/")
                    .split("-")
                    .map { it.toInt() }
                list[x - 1] = it.icon
            }
            tempArrListImageSortView.add(list)

            val i = arrayListOf<ArrayList<Int>>()
            list.forEach { data ->
                val x = lastModel.bodyPart.find { it.icon == data }
                val pair = if (x != null) {
                    val path = x.listPath[0].listPath
                    val color = x.listPath
                    val randomValue = if (path[0] == "none") {
                        if (path.size > 3) (2 until path.size).random() else 2
                    } else {
                        if (path.size > 2) (1 until path.size).random() else 1
                    }
                    val randomColor = (0 until color.size).random()
                    arrayListOf(randomValue, randomColor)
                } else {
                    arrayListOf(-1, -1)
                }
                i.add(pair)
            }
            resultList.add(i)
            tempArrMix.add(lastModel)
        }

        adapter.arrListImageSortView.addAll(tempArrListImageSortView)
        adapter.listArrayInt.addAll(resultList)
        arrMix.addAll(tempArrMix)
        adapter.submitList(ArrayList(arrMix))

        // ✅ Update visible range sau khi load xong
        updateVisibleRange()
    }

    private fun loadAllItems() {
        if (isLoading) return
        isLoading = true

        lifecycleScope.launch(Dispatchers.Default) {
            val tempArrMix = arrayListOf<CustomModel>()
            val tempArrListImageSortView = mutableListOf<ArrayList<String>>()
            val resultList = mutableListOf<ArrayList<ArrayList<Int>>>()

            for (pos in 0 until sizeMix) {
                val mModel = DataHelper.arrBlackCentered[pos % DataHelper.arrBlackCentered.size]

                val list = ArrayList<String>().apply {
                    repeat(mModel.bodyPart.size) { add("") }
                }
                mModel.bodyPart.forEach {
                    val (x, _) = it.icon.substringBeforeLast("/")
                        .substringAfterLast("/")
                        .split("-")
                        .map { it.toInt() }
                    list[x - 1] = it.icon
                }
                tempArrListImageSortView.add(list)

                val i = arrayListOf<ArrayList<Int>>()
                list.forEach { data ->
                    val x = mModel.bodyPart.find { it.icon == data }
                    val pair = if (x != null) {
                        val path = x.listPath[0].listPath
                        val color = x.listPath
                        val randomValue = if (path[0] == "none") {
                            if (path.size > 3) (2 until path.size).random() else 2
                        } else {
                            if (path.size > 2) (1 until path.size).random() else 1
                        }
                        val randomColor = (0 until color.size).random()
                        arrayListOf(randomValue, randomColor)
                    } else {
                        arrayListOf(-1, -1)
                    }
                    i.add(pair)
                }
                resultList.add(i)
                tempArrMix.add(mModel)
            }

            withContext(Dispatchers.Main) {
                adapter.arrListImageSortView.addAll(tempArrListImageSortView)
                adapter.listArrayInt.addAll(resultList)
                arrMix.addAll(tempArrMix)
                adapter.submitList(ArrayList(arrMix))
                isLoading = false

                // ✅ Update visible range sau khi load xong
                updateVisibleRange()
            }
        }
    }

    override fun initAction() {
        binding.apply {
            imvBack.onSingleClick { finish() }
            adapter.onCLick = {
                if (isOfflineMode) {
                    startActivity(
                        newIntent(this@QuickMixActivity, CustomviewActivity::class.java)
                            .putExtra("data", DataHelper.arrBlackCentered.size - 1)
                            .putExtra("arr", adapter.listArrayInt[it])
                    )
                } else {
                    val index = it % DataHelper.arrBlackCentered.size
                    val model = DataHelper.arrBlackCentered[index]

                    if (model.checkDataOnline) {
                        if (isInternetAvailable(this@QuickMixActivity)) {
                            showInter {
                                startActivity(
                                    newIntent(this@QuickMixActivity, CustomviewActivity::class.java)
                                        .putExtra("data", index)
                                        .putExtra("arr", adapter.listArrayInt[it])
                                )
                            }
                        } else {
                            DialogExit(this@QuickMixActivity, "network").show()
                        }
                    } else {
                        startActivity(
                            newIntent(this@QuickMixActivity, CustomviewActivity::class.java)
                                .putExtra("data", index)
                                .putExtra("arr", adapter.listArrayInt[it])
                        )
                    }
                }
            }
        }
    }

//    override fun onDestroy() {
//        super.onDestroy()
//        try {
//            unregisterReceiver(networkReceiver)
//        } catch (e: Exception) {
//            e.printStackTrace()
//        }
//        // ✅ Clear adapter cache khi destroy
//        adapter.clearCache()
//    }
}