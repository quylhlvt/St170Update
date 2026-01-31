package com.dragon.tribe.fire.oc.maker.ui.main

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Rect
import android.graphics.RectF
import android.net.ConnectivityManager
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.dragon.tribe.fire.oc.maker.base.AbsBaseActivity
import com.dragon.tribe.fire.oc.maker.data.model.CustomModel
import com.dragon.tribe.fire.oc.maker.data.repository.ApiRepository
import com.dragon.tribe.fire.oc.maker.dialog.DialogExit
import com.dragon.tribe.fire.oc.maker.ui.category.CategoryActivity
import com.dragon.tribe.fire.oc.maker.ui.customview.CustomviewActivity
import com.dragon.tribe.fire.oc.maker.ui.my_creation.MyCreationActivity
import com.dragon.tribe.fire.oc.maker.ui.quick_mix.QuickAdapter
import com.dragon.tribe.fire.oc.maker.ui.setting.SettingActivity
import com.dragon.tribe.fire.oc.maker.utils.DataHelper
import com.dragon.tribe.fire.oc.maker.utils.DataHelper.getData
import com.dragon.tribe.fire.oc.maker.utils.SharedPreferenceUtils
import com.dragon.tribe.fire.oc.maker.utils.backPress
import com.dragon.tribe.fire.oc.maker.utils.isInternetAvailable
import com.dragon.tribe.fire.oc.maker.utils.newIntent
import com.dragon.tribe.fire.oc.maker.utils.onSingleClick
import com.dragon.tribe.fire.oc.maker.utils.showInter
import com.lvt.ads.util.Admob
import com.dragon.tribe.fire.oc.maker.R
import com.dragon.tribe.fire.oc.maker.databinding.ActivityMainBinding
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.Collections.emptyList
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.atomic.AtomicInteger
import javax.inject.Inject
import kotlin.coroutines.coroutineContext
import kotlin.math.abs

@AndroidEntryPoint
class MainActivity : AbsBaseActivity<ActivityMainBinding>() {
    private var sizeMix = 10
    private var isLoading = false
    private var isOfflineMode = false
    private val arrMix = arrayListOf<CustomModel>()
    @Inject
    lateinit var apiRepository: ApiRepository
    val adapter by lazy { QuickAdapter(this@MainActivity) }

    // Thread-safe cache
    private val layerCache = ConcurrentHashMap<String, Bitmap>()
    val arrBitmap = ConcurrentHashMap<Int, Bitmap>()

    // Quản lý loading jobs
    private val loadingJobs = ConcurrentHashMap<Int, Job>()
    private val loadingPositions = ConcurrentHashMap<Int, Boolean>()

    // Giới hạn số lượng load đồng thời
    private val maxConcurrentLoads = 10
    private val currentLoadingCount = AtomicInteger(0)

    // Dispatcher với thread pool tối ưu
    private val bitmapDispatcher = Dispatchers.IO.limitedParallelism(maxConcurrentLoads)

    // Network state tracking
    @Volatile
    private var isNetworkAvailable = true
    var checkCallingDataOnline = false

    // Track visible range để ưu tiên
    @Volatile
    private var currentVisibleRange = IntRange.EMPTY

    // Cache size limit
    private val maxCacheSize = 10

    override fun getLayoutId(): Int = R.layout.activity_main

    private fun registerNetworkReceiver() {
        val filter = IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION)
        registerReceiver(networkReceiver, filter)
    }

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

            // ✅ PHẦN NÀY CHỈ LO VIỆC CẬP NHẬT MIX HIỂN THỊ
            val wasAvailable = isNetworkAvailable
            isNetworkAvailable = isInternetAvailable(this@MainActivity)

            // Chỉ cập nhật khi trạng thái mạng thay đổi
            if (wasAvailable != isNetworkAvailable) {
                isOfflineMode = !isNetworkAvailable

                lifecycleScope.launch(Dispatchers.Main) {
                    // ✅ Clear UI
                    clearAdapterAndCache()
                    sizeMix = 10

                    // ✅ Reload mix từ data có sẵn (không gọi API)
                    updateMixDisplay()
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
        registerNetworkReceiver()
        isNetworkAvailable = isInternetAvailable(this@MainActivity)
        isOfflineMode = !isNetworkAvailable

        if (DataHelper.arrBg.isEmpty()) {
            finish()
        } else {
            binding.recyclerSticker.itemAnimator = null
            binding.recyclerSticker.adapter = adapter

            binding.recyclerSticker.addOnScrollListener(object : RecyclerView.OnScrollListener() {
                override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                    super.onScrolled(recyclerView, dx, dy)

                    val layoutManager = binding.recyclerSticker.layoutManager as? LinearLayoutManager
                    val firstVisible = layoutManager?.findFirstVisibleItemPosition() ?: RecyclerView.NO_POSITION
                    val lastVisible = layoutManager?.findLastVisibleItemPosition() ?: RecyclerView.NO_POSITION

                    if (firstVisible != RecyclerView.NO_POSITION && lastVisible != RecyclerView.NO_POSITION) {
                        val newRange = firstVisible..lastVisible

                        if (currentVisibleRange != newRange) {
                            currentVisibleRange = newRange
                            cancelNonVisibleJobs(firstVisible, lastVisible)
                            clearDistantCache(firstVisible, lastVisible)
                        }
                    }

                    preloadVisibleAndNext()
                }
            })

            // ✅ Sử dụng hàm mới
            if (DataHelper.arrBlackCentered.isEmpty()) {
                lifecycleScope.launch {
                    delay(2000)
                    updateMixDisplay()
                }
            } else {
                updateMixDisplay()
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
            adapter.onCLick = { position ->
                val model = arrMix[position]
                val index = DataHelper.arrBlackCentered.indexOf(model)

                if (index != -1) {
                    startActivity(
                        newIntent(this@MainActivity, CustomviewActivity::class.java)
                            .putExtra("data", index)
                            .putExtra("arr", adapter.listArrayInt[position])
                    )
                }
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

    // ✅ HÀM MỚI: Clear toàn bộ adapter và cache
    private fun clearAdapterAndCache() {
        // Clear loading jobs
        loadingPositions.clear()
        currentLoadingCount.set(0)

        // Clear cache
        layerCache.clear()
        arrBitmap.clear()

        // Clear data arrays
        arrMix.clear()
        adapter.arrListImageSortView.clear()
        adapter.listArrayInt.clear()

        // Clear adapter - hiển thị danh sách rỗng
        adapter.submitList(emptyList())
        adapter.notifyDataSetChanged()
    }

    /**
     * ✅ HÀM MỚI: Cập nhật hiển thị mix dựa trên trạng thái mạng
     * CHỈ sử dụng data có sẵn trong DataHelper.arrBlackCentered
     */
    private fun updateMixDisplay() {
        if (DataHelper.arrBlackCentered.isEmpty()) {
            // Nếu chưa có data, delay rồi thử lại
            lifecycleScope.launch {
                delay(2000)
                updateMixDisplay()
            }
            return
        }

        if (isOfflineMode) {
            // ✅ OFFLINE MODE: Hiển thị 1 nhân vật offline cuối cùng
            loadOfflineMix()
        } else {
            // ✅ ONLINE MODE: Hiển thị tất cả nhân vật
            loadOnlineMix()
        }
    }

    /**
     * ✅ Load mix cho offline mode - CHỈ 1 NHÂN VẬT CUỐI CÙNG
     */
    private fun loadOfflineMix() {
        val offlineModels = DataHelper.arrBlackCentered.filter { !it.checkDataOnline }

        if (offlineModels.isEmpty()) {
            // Không có nhân vật offline, lấy nhân vật cuối cùng của toàn bộ danh sách
            val lastCharacter = DataHelper.arrBlackCentered.lastOrNull()
            if (lastCharacter != null) {
                loadCharactersWithRandomVariants(
                    characterList = listOf(lastCharacter),
                    totalItems = sizeMix  // 10 variants từ 1 nhân vật
                )
            }
            return
        }

        // ✅ Lấy 1 nhân vật cuối cùng trong danh sách offline
        val lastOfflineCharacter = offlineModels.last()

        loadCharactersWithRandomVariants(
            characterList = listOf(lastOfflineCharacter),
            totalItems = sizeMix  // 10 variants từ 1 nhân vật
        )
    }

    /**
     * ✅ Load mix cho online mode - TẤT CẢ NHÂN VẬT
     */
    private fun loadOnlineMix() {
        loadCharactersWithRandomVariants(
            characterList = DataHelper.arrBlackCentered,
            totalItems = sizeMix  // 10 items phân bổ cho tất cả nhân vật
        )
    }

    // ✅ HÀM MỚI: Load danh sách nhân vật với tổng số items = sizeMix (10)
    private fun loadCharactersWithRandomVariants(
        characterList: List<CustomModel>,
        totalItems: Int = sizeMix
    ) {
        if (characterList.isEmpty()) return

        lifecycleScope.launch(Dispatchers.Default) {
            val tempArrMix = arrayListOf<CustomModel>()
            val tempArrListImageSortView = arrayListOf<ArrayList<String>>()
            val tempResultList = arrayListOf<ArrayList<ArrayList<Int>>>()

            // Tính số items cho mỗi nhân vật
            val itemsPerCharacter = totalItems / characterList.size
            val remainder = totalItems % characterList.size

            characterList.forEachIndexed { index, character ->
                // Nhân vật đầu tiên nhận thêm phần dư
                val count = if (index == 0) itemsPerCharacter + remainder else itemsPerCharacter

                if (count > 0) {
                    val (models, iconLists, variantLists) = generateRandomVariantsFromSingleCharacter(
                        model = character,
                        count = count
                    )

                    tempArrMix.addAll(models)
                    tempArrListImageSortView.addAll(iconLists)
                    tempResultList.addAll(variantLists)
                }
            }

            withContext(Dispatchers.Main) {
                // Update data
                arrMix.addAll(tempArrMix)
                adapter.arrListImageSortView.addAll(tempArrListImageSortView)
                adapter.listArrayInt.addAll(tempResultList)

                // Notify adapter
                adapter.submitList(ArrayList(arrMix))
                adapter.notifyDataSetChanged()

                isLoading = false

                // Preload visible items
                preloadVisibleAndNext()
            }
        }
    }

    /**
     * ✅ HÀM TIỆN ÍCH: Tạo random variants từ một nhân vật
     */
    private fun generateRandomVariantsFromSingleCharacter(
        model: CustomModel,
        count: Int = 20
    ): Triple<ArrayList<CustomModel>, ArrayList<ArrayList<String>>, ArrayList<ArrayList<ArrayList<Int>>>> {
        val resultModels = arrayListOf<CustomModel>()
        val resultIconLists = arrayListOf<ArrayList<String>>()
        val resultVariantLists = arrayListOf<ArrayList<ArrayList<Int>>>()

        // Lặp count lần để tạo nhiều biến thể
        repeat(count) {
            // 1. Tạo list icon mặc định
            val iconList = ArrayList<String>().apply {
                repeat(model.bodyPart.size) { add("") }
            }

            model.bodyPart.forEach { part ->
                val icon = part.icon
                try {
                    val fileName = icon.substringBeforeLast("/").substringAfterLast("/")
                    val (x, _) = fileName.split("-").map { it.toInt() }
                    if (x in 1..model.bodyPart.size) {
                        iconList[x - 1] = icon
                    }
                } catch (e: Exception) {
                    // Parse lỗi → bỏ qua
                }
            }

            // 2. Tạo random variant + color cho từng layer
            val variantAndColorList = arrayListOf<ArrayList<Int>>()

            iconList.forEach { iconPath ->
                val part = model.bodyPart.find { it.icon == iconPath }
                if (part == null) {
                    variantAndColorList.add(arrayListOf(-1, -1))
                    return@forEach
                }

                val firstPathGroup = part.listPath.getOrNull(0) ?: run {
                    variantAndColorList.add(arrayListOf(-1, -1))
                    return@forEach
                }

                val pathList = firstPathGroup.listPath
                val colorList = part.listPath

                // Logic random variant
                val randomVariant = when {
                    pathList.isEmpty() -> -1
                    pathList[0] == "none" -> {
                        if (pathList.size > 3) (2 until pathList.size).random() else 2
                    }
                    pathList.size > 2 -> (1 until pathList.size).random()
                    else -> if (pathList.isNotEmpty()) 1 else -1
                }

                // Random color index
                val randomColor = if (colorList.isNotEmpty()) {
                    (0 until colorList.size).random()
                } else -1

                variantAndColorList.add(arrayListOf(randomVariant, randomColor))
            }

            // 3. Lưu kết quả cho item này
            resultModels.add(model)
            resultIconLists.add(iconList)
            resultVariantLists.add(variantAndColorList)
        }

        return Triple(resultModels, resultIconLists, resultVariantLists)
    }

    private fun cancelNonVisibleJobs(firstVisible: Int, lastVisible: Int) {
        val preloadBuffer = 4
        val keepRange = (firstVisible - preloadBuffer)..(lastVisible + preloadBuffer)

        loadingJobs.entries.removeAll { (position, job) ->
            if (position !in keepRange) {
                job.cancel()
                loadingPositions.remove(position)
                true
            } else {
                false
            }
        }
    }

    private fun clearDistantCache(firstVisible: Int, lastVisible: Int) {
        val keepRange = (firstVisible - 10)..(lastVisible + 10)

        if (arrBitmap.size > maxCacheSize) {
            val toRemove = arrBitmap.keys.filter { it !in keepRange }.sortedBy {
                minOf(abs(it - firstVisible), abs(it - lastVisible))
            }.reversed().take(arrBitmap.size - maxCacheSize)

            toRemove.forEach { position ->
                arrBitmap.remove(position)?.recycle()
            }
        }
    }

    private fun preloadVisibleAndNext() {
        val layoutManager = binding.recyclerSticker.layoutManager as? LinearLayoutManager ?: return
        val firstVisible = layoutManager.findFirstVisibleItemPosition()
        val lastVisible = layoutManager.findLastVisibleItemPosition()

        if (firstVisible == RecyclerView.NO_POSITION) return

        val positions = (firstVisible..minOf(lastVisible + 4, arrMix.size - 1)).toList()

        val visibleFirst = positions.filter { it in firstVisible..lastVisible }
        val nextItems = positions.filter { it !in firstVisible..lastVisible }
        val prioritized = visibleFirst + nextItems

        lifecycleScope.launch {
            prioritized.map { position ->
                async(bitmapDispatcher) {
                    if (!arrBitmap.containsKey(position) && loadingPositions.putIfAbsent(position, true) == null) {
                        loadBitmapAsync(position, position in firstVisible..lastVisible)
                    }
                }
            }.awaitAll()
        }
    }

    private suspend fun loadBitmapAsync(position: Int, isVisible: Boolean = false) {
        val job = coroutineContext[Job]
        job?.let { loadingJobs[position] = it }

        try {
            // ✅ Check bounds
            if (position >= arrMix.size) return

            val model = arrMix[position]

            if (model.checkDataOnline && isOfflineMode) {
                return
            }

            if (!isVisible) {
                while (currentLoadingCount.get() >= maxConcurrentLoads) {
                    delay(50)
                    if (model.checkDataOnline && isOfflineMode) {
                        return
                    }
                }
            }

            currentLoadingCount.incrementAndGet()

            // ✅ Lấy data trực tiếp từ adapter bằng position
            if (position >= adapter.arrListImageSortView.size ||
                position >= adapter.listArrayInt.size) {
                currentLoadingCount.decrementAndGet()
                loadingPositions.remove(position)
                loadingJobs.remove(position)
                return
            }

            val listImageSortView = adapter.arrListImageSortView[position]
            val coordSet = adapter.listArrayInt[position]

            if (model.checkDataOnline && isOfflineMode) {
                return
            }

            val targetSize = calculateTargetSize(model, listImageSortView, coordSet)

            val merged = mergeBitmapAsync(model, listImageSortView, coordSet, targetSize.first, targetSize.second)

            if (merged != null) {
                arrBitmap[position] = merged

                withContext(Dispatchers.Main) {
                    adapter.notifyItemChanged(position)
                }
            }
        } catch (e: CancellationException) {
            // Job canceled
        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            currentLoadingCount.decrementAndGet()
            loadingPositions.remove(position)
            loadingJobs.remove(position)
        }
    }

    private suspend fun calculateTargetSize(
        model: CustomModel,
        listImageSortView: List<String>,
        coordSet: ArrayList<ArrayList<Int>>
    ): Pair<Int, Int> = withContext(Dispatchers.IO) {
        try {
            if (model.checkDataOnline && isOfflineMode) {
                return@withContext Pair(256, 256)
            }

            for (index in listImageSortView.indices) {
                val icon = listImageSortView[index]
                val coord = coordSet[index]

                if (coord[0] > 0) {
                    val targetPath = model.bodyPart
                        .find { it.icon == icon }
                        ?.listPath?.getOrNull(coord[1])
                        ?.listPath?.getOrNull(coord[0])

                    if (!targetPath.isNullOrEmpty()) {
                        val bmp = Glide.with(this@MainActivity)
                            .asBitmap()
                            .load(targetPath)
                            .submit()
                            .get()

                        return@withContext Pair(bmp.width / 3, bmp.height / 3)
                    }
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }

        return@withContext Pair(256, 256)
    }

    private suspend fun loadSingleBitmap(path: String, width: Int, height: Int, isOnlineData: Boolean): Bitmap? {
        layerCache[path]?.let { return it }

        if (isOnlineData && isOfflineMode) {
            return null
        }

        return withContext(Dispatchers.IO) {
            try {
                if (isOnlineData && isOfflineMode) {
                    return@withContext null
                }

                val bmp = Glide.with(this@MainActivity)
                    .asBitmap()
                    .load(path)
                    .override(width, height)
                    .diskCacheStrategy(DiskCacheStrategy.ALL)
                    .submit()
                    .get()
                layerCache[path] = bmp
                bmp
            } catch (e: Exception) {
                null
            }
        }
    }

    private suspend fun mergeBitmapAsync(
        blackCentered: CustomModel,
        listImageSortView: List<String>,
        coordSet: ArrayList<ArrayList<Int>>,
        width: Int,
        height: Int
    ): Bitmap? = withContext(bitmapDispatcher) {
        try {
            if (blackCentered.checkDataOnline && isOfflineMode) {
                return@withContext null
            }

            val merged = Glide.get(this@MainActivity).bitmapPool
                .get(width, height, Bitmap.Config.ARGB_8888)

            val canvas = Canvas(merged)
            val dstRect = RectF(0f, 0f, width.toFloat(), height.toFloat())

            val layers = listImageSortView.mapIndexed { index, icon ->
                async(Dispatchers.IO) {
                    if (blackCentered.checkDataOnline && isOfflineMode) {
                        return@async null
                    }

                    val coord = coordSet[index]
                    if (coord[0] > 0) {
                        val targetPath = blackCentered.bodyPart
                            .find { it.icon == icon }
                            ?.listPath?.getOrNull(coord[1])
                            ?.listPath?.getOrNull(coord[0])

                        if (!targetPath.isNullOrEmpty()) {
                            loadSingleBitmap(targetPath, width, height, blackCentered.checkDataOnline)
                        } else null
                    } else null
                }
            }.awaitAll().filterNotNull()

            layers.forEach { bmp ->
                val srcRect = Rect(0, 0, bmp.width, bmp.height)
                canvas.drawBitmap(bmp, srcRect, dstRect, null)
            }

            merged
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    fun requestBitmap(position: Int) {
        if (arrBitmap.containsKey(position)) return
        if (loadingPositions.containsKey(position)) return

        val layoutManager = binding.recyclerSticker.layoutManager as? LinearLayoutManager
        val firstVisible = layoutManager?.findFirstVisibleItemPosition() ?: RecyclerView.NO_POSITION
        val lastVisible = layoutManager?.findLastVisibleItemPosition() ?: RecyclerView.NO_POSITION
        val isVisible = position in firstVisible..lastVisible

        lifecycleScope.launch(bitmapDispatcher) {
            loadBitmapAsync(position, isVisible)
        }
    }
}