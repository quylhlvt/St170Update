package com.dragon.tribe.fire.oc.maker.core.extensions

import android.content.Context
import android.view.ViewGroup
import android.widget.ImageView
import com.bumptech.glide.Glide
import com.dragon.tribe.fire.oc.maker.core.utils.DataLocal
import com.facebook.shimmer.ShimmerDrawable

fun loadImageGlide(
    viewGroup: ViewGroup, path: Int, imageView: ImageView, isLoadShimmer: Boolean = true
) {
    val shimmerDrawable = ShimmerDrawable().apply {
        setShimmer(DataLocal.shimmer)
    }
    if (isLoadShimmer) {
        Glide.with(viewGroup).load(path).placeholder(shimmerDrawable).error(shimmerDrawable)
            .into(imageView)
    } else {
        Glide.with(viewGroup).load(path).into(imageView)
    }
}

fun loadImageGlide(
    viewGroup: ViewGroup, path: String, imageView: ImageView, isLoadShimmer: Boolean = true
) {
    val shimmerDrawable = ShimmerDrawable().apply {
        setShimmer(DataLocal.shimmer)
    }
    if (isLoadShimmer) {
        Glide.with(viewGroup).load(path).placeholder(shimmerDrawable).error(shimmerDrawable)
            .into(imageView)
    } else {
        Glide.with(viewGroup).load(path).placeholder(shimmerDrawable).error(shimmerDrawable)
            .into(imageView)
    }
}
fun loadImageGlide(
    context: Context, path: String, imageView: ImageView?, isLoadShimmer: Boolean = true
) {
    val shimmerDrawable = ShimmerDrawable().apply {
        setShimmer(DataLocal.shimmer)
    }
    if (path != "-1") {

        if (isLoadShimmer) {
            Glide.with(context).load(path).placeholder(shimmerDrawable).error(shimmerDrawable)
                .into(imageView!!)
        } else {
            Glide.with(context).load(path).placeholder(shimmerDrawable).error(shimmerDrawable)
                .into(imageView!!)
        }

    }






}