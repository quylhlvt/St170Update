package com.dragon.tribe.fire.oc.maker.data.model

import android.graphics.Bitmap
import com.dragon.tribe.fire.oc.maker.data.custom.ItemColorModel
import com.dragon.tribe.fire.oc.maker.data.custom.ItemNavCustomModel
import com.google.gson.Gson
import com.google.gson.annotations.SerializedName
import java.io.Serializable
/**
 * Model đại diện cho một suggestion (gợi ý trang phục)
 */
data class SuggestionModel(
    val avatarPath: String = "",
    var isLoaded: Boolean = false,
    val positionColorItemList : ArrayList<Int> = arrayListOf(),
    val itemNavList : ArrayList<ArrayList<ItemNavCustomModel>> = arrayListOf(),
    var colorItemNavList : ArrayList<ArrayList<ItemColorModel>> = arrayListOf(),
    val isSelectedItemList : ArrayList<Boolean> = arrayListOf(),
    val keySelectedItemList : ArrayList<String> = arrayListOf(),
    val isShowColorList : ArrayList<Boolean> = arrayListOf(),
    val pathSelectedList : ArrayList<String> = arrayListOf(),
    @SerializedName("backgroundList")
    var backgroundList: ArrayList<BackGroundModel>? = null,

    @SerializedName("selectedBackgroundPath")
    var selectedBackgroundPath: String? = null,

    @SerializedName("randomBackgroundPath")
    var randomBackgroundPath: String? = null
):Serializable
