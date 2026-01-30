import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.drawable.Drawable
import android.util.Log
import android.view.View
import androidx.core.graphics.createBitmap
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.target.Target
import com.dragon.tribe.fire.oc.maker.core.base.BaseAdapter
import com.dragon.tribe.fire.oc.maker.core.extensions.gone
import com.dragon.tribe.fire.oc.maker.core.extensions.invisible
import com.dragon.tribe.fire.oc.maker.core.extensions.onSingleClick
import com.dragon.tribe.fire.oc.maker.core.extensions.visible
import com.dragon.tribe.fire.oc.maker.data.model.SuggestionModel
import com.dragon.tribe.fire.oc.maker.databinding.ItemListStickerBinding
import kotlinx.coroutines.*
import androidx.core.graphics.scale


class SuggestionAdapter(
    private val context: Context
) : BaseAdapter<SuggestionModel, ItemListStickerBinding>(ItemListStickerBinding::inflate) {

    var onItemClick: ((SuggestionModel) -> Unit) = {}

    private var totalItems = 0
    private var itemsLoaded = 0

    fun setList(list: List<SuggestionModel>) {
        totalItems = list.size
        itemsLoaded = 0
        items.clear()
        items.addAll(list)
        notifyDataSetChanged()
    }

    override fun onBind(binding: ItemListStickerBinding, item: SuggestionModel, position: Int) {
        binding.apply {
            sflShimmer.visible()
            sflShimmer.startShimmer()
            imvImage.invisible()
            imvBackgound.invisible()

            item.randomBackgroundPath?.let { bgPath ->
                // 🔹 Nếu path chưa có prefix, thêm "file:///android_asset/"
                val fullBgPath = if (bgPath.startsWith("file:///")) bgPath
                else "file:///android_asset/$bgPath"

                Glide.with(imvBackgound)
                    .load(fullBgPath)
                    .listener(object : RequestListener<Drawable> {
                        override fun onLoadFailed(
                            e: GlideException?, model: Any?, target: Target<Drawable>, isFirstResource: Boolean
                        ) = applyItemLoaded()

                        override fun onResourceReady(
                            resource: Drawable,
                            model: Any,
                            target: Target<Drawable>,
                            dataSource: DataSource,
                            isFirstResource: Boolean
                        ): Boolean {
                            imvBackgound.visible() // ✅ hiển thị khi load xong
                            return applyItemLoaded(binding)
                        }
                    }).into(imvBackgound)
            } ?: applyItemLoaded()
            imvImage.post {
                val density = context.resources.displayMetrics.density
                val width = (120 * density).toInt()
                val height = (120 * density).toInt()
                val listBitmap = arrayListOf<Bitmap>()
                val handler = CoroutineExceptionHandler { _, throwable ->
                    itemsLoaded++
                }

                CoroutineScope(SupervisorJob() + Dispatchers.IO + handler).launch {
                    item.pathSelectedList.forEach { path ->
                        try { listBitmap.add(Glide.with(context).asBitmap().load(path).submit().get()) }
                        catch (_: Exception) {}
                    }

                    val combinedBitmap = createBitmap(width, height)
                    val canvas = Canvas(combinedBitmap)

                    listBitmap.forEach { bitmap ->
                        val scale = minOf(
                            width.toFloat() / bitmap.width,
                            height.toFloat() / bitmap.height
                        )
                        val scaledBitmap = bitmap.scale((bitmap.width*scale).toInt(), (bitmap.height*scale).toInt())
                        canvas.drawBitmap(scaledBitmap, (width-scaledBitmap.width)/2f, (height-scaledBitmap.height)/2f, null)
                    }

                    withContext(Dispatchers.Main) {
                        Glide.with(imvImage)
                            .load(combinedBitmap)
                            .centerCrop()
                            .listener(object : RequestListener<Drawable> {
                                override fun onLoadFailed(
                                    e: GlideException?, model: Any?, target: Target<Drawable>, isFirstResource: Boolean
                                ) = applyItemLoaded()

                                override fun onResourceReady(
                                    resource: Drawable,
                                    model: Any,
                                    target: Target<Drawable>,
                                    dataSource: DataSource,
                                    isFirstResource: Boolean
                                ): Boolean {
                                    imvImage.visible()
                                    return applyItemLoaded(binding)
                                }
                            }).into(imvImage)
                    }
                }
            }

            root.onSingleClick { onItemClick.invoke(item) }
        }
    }

    private fun applyItemLoaded(binding: ItemListStickerBinding? = null): Boolean {
        itemsLoaded++
        binding?.apply {
            sflShimmer.stopShimmer()
            sflShimmer.gone()
        }
        return false
    }


}
