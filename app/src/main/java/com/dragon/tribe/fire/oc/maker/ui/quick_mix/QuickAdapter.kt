package com.dragon.tribe.fire.oc.maker.ui.quick_mix

import android.app.Activity
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.dragon.tribe.fire.oc.maker.base.AbsBaseAdapter
import com.dragon.tribe.fire.oc.maker.base.AbsBaseDiffCallBack
import com.dragon.tribe.fire.oc.maker.data.model.CustomModel
import com.dragon.tribe.fire.oc.maker.dialog.DialogExit
import com.dragon.tribe.fire.oc.maker.ui.main.MainActivity
import com.dragon.tribe.fire.oc.maker.utils.DataHelper
import com.dragon.tribe.fire.oc.maker.utils.hide
import com.dragon.tribe.fire.oc.maker.utils.isInternetAvailable
import com.dragon.tribe.fire.oc.maker.utils.show
import com.dragon.tribe.fire.oc.maker.utils.showToast
import com.dragon.tribe.fire.oc.maker.R
import com.dragon.tribe.fire.oc.maker.databinding.ItemMixBinding

class QuickAdapter(private val activity: Activity) : AbsBaseAdapter<CustomModel, ItemMixBinding>(
    R.layout.item_mix, DiffCallBack()
) {
    var arrListImageSortView = arrayListOf<ArrayList<String>>()
    var onCLick: ((Int) -> Unit)? = null
    var listArrayInt = arrayListOf<ArrayList<ArrayList<Int>>>()

    override fun bind(
        binding: ItemMixBinding,
        position: Int,
        data: CustomModel,
        holder: RecyclerView.ViewHolder
    ) {



        val quickActivity = activity as? MainActivity ?: return
        val bitmap = quickActivity.arrBitmap[position]

        // ✅ Kiểm tra xem đã bind position này chưa
        val currentTag = binding.imvImage.tag as? Int

        if (bitmap != null) {
            // ✅ Có bitmap rồi
            binding.shimmer.stopShimmer()
            binding.shimmer.hide()

            // ✅ Chỉ load lại nếu position khác hoặc image đang trống
            if (currentTag != position || binding.imvImage.drawable == null) {
                binding.imvImage.tag = position
                Glide.with(binding.root.context)
                    .load(bitmap)
                    .encodeQuality(40)
                    .override(256)
                    .dontTransform()
                    .diskCacheStrategy(DiskCacheStrategy.NONE)
                    .into(binding.imvImage)
            }
        } else {
            // ✅ Chưa có bitmap
            // Chỉ hiển thị shimmer nếu position thay đổi hoặc shimmer đang ẩn
            if (currentTag != position) {
                binding.imvImage.tag = position
                binding.shimmer.startShimmer()
                binding.shimmer.show()

                // Clear image cũ
                Glide.with(binding.root.context).clear(binding.imvImage)
                binding.imvImage.setImageDrawable(null)
            }

            // ✅ Request load bitmap
            quickActivity.requestBitmap(position)

            // ✅ Click vào shimmer
            if (binding.shimmer.tag != position) {
                binding.shimmer.tag = position
                binding.shimmer.setOnClickListener {
                    if (!isInternetAvailable(activity) && position < DataHelper.arrBlackCentered.size - 1) {
                        DialogExit(activity, "network").show()
                        return@setOnClickListener
                    }
                    showToast(binding.root.context, R.string.wait_a_few_second)
                }
            }
        }

        // ✅ Set click listeners chỉ một lần
        if (binding.root.tag != position) {
            binding.root.tag = position
            binding.root.setOnClickListener { onCLick?.invoke(position) }
            binding.imvImage.setOnClickListener { onCLick?.invoke(position) }
        }
    }

    class DiffCallBack : AbsBaseDiffCallBack<CustomModel>() {
        override fun itemsTheSame(oldItem: CustomModel, newItem: CustomModel): Boolean {
            return oldItem.avt == newItem.avt
        }

        override fun contentsTheSame(oldItem: CustomModel, newItem: CustomModel): Boolean {
            return oldItem.avt == newItem.avt
        }
    }
}