package com.ocmaker.fullbody.creator.data.model

data class QuickMixItem(
    val model: CustomModel,
    val imagePaths: List<String>,  // ← Đường dẫn ảnh sẵn
    val coordSet: ArrayList<ArrayList<Int>>
)