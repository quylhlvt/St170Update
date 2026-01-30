package com.dragon.tribe.fire.oc.maker.data.custom

import com.dragon.tribe.fire.oc.maker.data.model.BackGroundModel

data class CustomizeModel(
    val dataName: String = "",
    val avatar: String = "",
    val layerList: ArrayList<LayerListModel> = arrayListOf(),
    val backgroundList: ArrayList<BackGroundModel>? = null, // có thể null
    val selectedBackgroundPath: String? = null
)