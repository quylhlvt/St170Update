package com.ocmaker.fullbody.creator.ui.quick_mix

import android.app.Activity
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Rect
import android.graphics.RectF
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.ocmaker.fullbody.creator.R
import com.ocmaker.fullbody.creator.databinding.ItemMixBinding
import com.ocmaker.fullbody.creator.dialog.DialogExit
import com.ocmaker.fullbody.creator.utils.DataHelper
import com.ocmaker.fullbody.creator.utils.hide
import com.ocmaker.fullbody.creator.utils.isInternetAvailable
import com.ocmaker.fullbody.creator.utils.show
import com.ocmaker.fullbody.creator.utils.showToast
import com.ocmaker.fullbody.creator.data.model.CustomModel
import kotlinx.coroutines.*
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.PriorityBlockingQueue
import java.util.concurrent.atomic.AtomicInteger
import kotlin.text.clear

class QuickAdapter(private val activity: Activity) :
    ListAdapter<CustomModel, QuickAdapter.ViewHolder>(DiffCallback()) {

    var arrListImageSortView = arrayListOf<ArrayList<String>>()
    var onCLick: ((Int) -> Unit)? = null
    var listArrayInt = arrayListOf<ArrayList<ArrayList<Int>>>()

    // ✅ Cache bitmap
    private val bitmapCache = ConcurrentHashMap<Int, Bitmap>()
    private val layerCache = ConcurrentHashMap<String, Bitmap>(1500)

    // ✅ Cache size của từng characterIndex để tránh load lại
    private val sizeCache = ConcurrentHashMap<Int, Pair<Int, Int>>()

    // ✅ Priority queue cho visible items
    private data class LoadTask(
        val position: Int,
        val priority: Int,
        val holder: ViewHolder
    ) : Comparable<LoadTask> {
        override fun compareTo(other: LoadTask): Int = priority.compareTo(other.priority)
    }

    private val loadQueue = PriorityBlockingQueue<LoadTask>()
    private val loadingPositions = ConcurrentHashMap<Int, Boolean>()
    private val currentLoadingCount = AtomicInteger(0)
    private var queueProcessor: Job? = null

    // ✅ Tăng concurrent loads
    private val maxConcurrentLoads = 300
    private val bitmapDispatcher = Dispatchers.IO.limitedParallelism(maxConcurrentLoads)

    var isOfflineMode = false
    var isNetworkAvailable = true

    // ✅ Track visible range
    private var firstVisiblePosition = 0
    private var lastVisiblePosition = 0

    init {
        startQueueProcessor()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemMixBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val data = getItem(position)
        holder.bind(position, data)
    }

    // ✅ Khởi động worker xử lý queue
    private fun startQueueProcessor() {
        val lifecycleOwner = activity as? LifecycleOwner ?: return

        queueProcessor = lifecycleOwner.lifecycleScope.launch(bitmapDispatcher) {
            while (isActive) {
                try {
                    val task = loadQueue.poll()
                    if (task != null) {
                        if (!bitmapCache.containsKey(task.position)) {
                            loadBitmapAsync(task.position, task.holder)
                        }
                    } else {
                        delay(5) // ✅ Giảm delay
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }
    }

    // ✅ Update visible range
    fun updateVisibleRange(first: Int, last: Int) {
        firstVisiblePosition = first
        lastVisiblePosition = last
    }

    inner class ViewHolder(val binding: ItemMixBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(position: Int, data: CustomModel) {
            val bitmap = bitmapCache[position]
            val currentTag = binding.imvImage.tag as? Int

            if (bitmap != null) {
                val sizeWidth = bitmap.width / 3
                val sizeHeight = bitmap.height / 3

                binding.shimmer.stopShimmer()
                binding.shimmer.hide()

                if (currentTag != position || binding.imvImage.drawable == null) {
                    binding.imvImage.tag = position
                    Glide.with(binding.root.context)
                        .load(bitmap)
                        .encodeQuality(40)
                        .override(sizeWidth, sizeHeight)
                        .dontTransform()
                        .diskCacheStrategy(DiskCacheStrategy.NONE)
                        .into(binding.imvImage)
                }
            } else {
                if (currentTag != position) {
                    binding.imvImage.tag = position
                    binding.shimmer.startShimmer()
                    binding.shimmer.show()

                    Glide.with(binding.root.context).clear(binding.imvImage)
                    binding.imvImage.setImageDrawable(null)
                }

                requestLoadBitmap(position, this)

                if (binding.shimmer.tag != position) {
                    binding.shimmer.tag = position
                    binding.shimmer.setOnClickListener {
                        if (!isOfflineMode && !isInternetAvailable(activity)) {
                            DialogExit(activity, "network").show()
                            return@setOnClickListener
                        }
                        showToast(binding.root.context, R.string.wait_a_few_second)
                    }
                }
            }

            if (binding.root.tag != position) {
                binding.root.tag = position
                binding.root.setOnClickListener { onCLick?.invoke(position) }
                binding.imvImage.setOnClickListener { onCLick?.invoke(position) }
            }
        }
    }

    // ✅ Request load với priority
    private fun requestLoadBitmap(position: Int, holder: ViewHolder) {
        if (bitmapCache.containsKey(position)) return
        if (loadingPositions.containsKey(position)) return

        val priority = when {
            position in firstVisiblePosition..lastVisiblePosition -> 0
            position in (firstVisiblePosition - 3)..(lastVisiblePosition + 3) -> 1
            position in (firstVisiblePosition - 10)..(lastVisiblePosition + 10) -> 2
            else -> 3
        }

        loadQueue.offer(LoadTask(position, priority, holder))
    }

    private suspend fun loadBitmapAsync(position: Int, holder: ViewHolder) {
        if (loadingPositions.putIfAbsent(position, true) != null) return

        try {
            if (!isOfflineMode && !isNetworkAvailable) return

            val characterIndex = position % DataHelper.arrBlackCentered.size
            val model = currentList.getOrNull(position) ?: return

            if (model.checkDataOnline && !isNetworkAvailable) return

            // ✅ Giới hạn concurrent loads
            while (currentLoadingCount.get() >= maxConcurrentLoads) {
                delay(5)
                if (model.checkDataOnline && !isNetworkAvailable) return
            }

            currentLoadingCount.incrementAndGet()

            val listImageSortView = arrListImageSortView.getOrNull(characterIndex) ?: return
            val coordSet = listArrayInt.getOrNull(position) ?: return

            if (model.checkDataOnline && !isNetworkAvailable) return

            val merged = mergeBitmapAsync(model, listImageSortView, coordSet, characterIndex)

            if (merged != null) {
                bitmapCache[position] = merged

                withContext(Dispatchers.Main) {
                    if (holder.binding.imvImage.tag == position) {
                        val displayWidth = merged.width / 3
                        val displayHeight = merged.height / 3

                        holder.binding.shimmer.stopShimmer()
                        holder.binding.shimmer.hide()

                        Glide.with(holder.binding.root.context)
                            .load(merged)
                            .encodeQuality(40)
                            .override(displayWidth, displayHeight)
                            .dontTransform()
                            .diskCacheStrategy(DiskCacheStrategy.NONE)
                            .into(holder.binding.imvImage)
                    }
                }
            }
        } catch (e: CancellationException) {
            // Cancelled
        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            currentLoadingCount.decrementAndGet()
            loadingPositions.remove(position)
        }
    }

    // ✅ Tối ưu: giảm quality để load nhanh hơn
    private suspend fun loadSingleBitmap(path: String, w: Int, h: Int, isOnlineData: Boolean): Bitmap? {
        layerCache[path]?.let { return it }

        if (isOnlineData && !isNetworkAvailable) return null

        return withContext(Dispatchers.IO) {
            try {
                if (isOnlineData && !isNetworkAvailable) return@withContext null

                val bmp = if (w == 0 || h == 0) {
                    Glide.with(activity)
                        .asBitmap()
                        .load(path)
                        .encodeQuality(30) // ✅ Giảm quality
                        .diskCacheStrategy(DiskCacheStrategy.ALL)
                        .skipMemoryCache(false)
                        .submit()
                        .get()
                } else {
                    Glide.with(activity)
                        .asBitmap()
                        .load(path)
                        .override(w, h)
                        .encodeQuality(30) // ✅ Giảm quality
                        .diskCacheStrategy(DiskCacheStrategy.ALL)
                        .skipMemoryCache(false)
                        .submit()
                        .get()
                }

                layerCache[path] = bmp
                bmp
            } catch (e: Exception) {
                null
            }
        }
    }

    // ✅ Tối ưu: cache size để không load lại first layer
    private suspend fun mergeBitmapAsync(
        blackCentered: CustomModel,
        listImageSortView: List<String>,
        coordSet: ArrayList<ArrayList<Int>>,
        characterIndex: Int
    ): Bitmap? = withContext(bitmapDispatcher) {
        try {
            if (blackCentered.checkDataOnline && !isNetworkAvailable) return@withContext null

            // ✅ Kiểm tra size cache
            val cachedSize = sizeCache[characterIndex]
            val (actualWidth, actualHeight) = if (cachedSize != null) {
                cachedSize
            } else {
                // Load first layer để detect size
                val firstLayerPath = listImageSortView.firstOrNull()?.let { icon ->
                    val coord = coordSet.firstOrNull() ?: return@withContext null
                    if (coord[0] > 0) {
                        blackCentered.bodyPart
                            .find { it.icon == icon }
                            ?.listPath?.getOrNull(coord[1])
                            ?.listPath?.getOrNull(coord[0])
                    } else null
                }

                val firstBitmap = firstLayerPath?.let { path ->
                    loadSingleBitmap(path, 0, 0, blackCentered.checkDataOnline)
                } ?: return@withContext null

                val size = (firstBitmap.width / 3) to (firstBitmap.height / 3)
                sizeCache[characterIndex] = size // ✅ Cache size
                size
            }

            val merged = Glide.get(activity).bitmapPool
                .get(actualWidth, actualHeight, Bitmap.Config.ARGB_8888)

            val canvas = Canvas(merged)
            val dstRect = RectF(0f, 0f, actualWidth.toFloat(), actualHeight.toFloat())

            // ✅ Load layers parallel
            val layers = listImageSortView.mapIndexed { index, icon ->
                async(Dispatchers.IO) {
                    if (blackCentered.checkDataOnline && !isNetworkAvailable) return@async null

                    val coord = coordSet[index]
                    if (coord[0] > 0) {
                        val targetPath = blackCentered.bodyPart
                            .find { it.icon == icon }
                            ?.listPath?.getOrNull(coord[1])
                            ?.listPath?.getOrNull(coord[0])

                        if (!targetPath.isNullOrEmpty()) {
                            loadSingleBitmap(targetPath, actualWidth, actualHeight, blackCentered.checkDataOnline)
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

    fun clearCache() {
        queueProcessor?.cancel()
        loadQueue.clear()
        loadingPositions.clear()
        bitmapCache.clear()
        layerCache.clear()
        sizeCache.clear() // ✅ Clear size cache
        currentLoadingCount.set(0)
        startQueueProcessor()
    }

    override fun onDetachedFromRecyclerView(recyclerView: RecyclerView) {
        super.onDetachedFromRecyclerView(recyclerView)
        clearCache()
    }

    class DiffCallback : DiffUtil.ItemCallback<CustomModel>() {
        override fun areItemsTheSame(oldItem: CustomModel, newItem: CustomModel): Boolean {
            return oldItem.avt == newItem.avt
        }

        override fun areContentsTheSame(oldItem: CustomModel, newItem: CustomModel): Boolean {
            return oldItem.avt == newItem.avt
        }
    }
}