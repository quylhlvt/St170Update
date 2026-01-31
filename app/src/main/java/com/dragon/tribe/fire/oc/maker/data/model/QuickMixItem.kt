package com.dragon.tribe.fire.oc.maker.data.model

data class QuickMixItem(
    val model: CustomModel,
    val imagePaths: List<String>,  // ← Đường dẫn ảnh sẵn
    val coordSet: ArrayList<ArrayList<Int>>
)