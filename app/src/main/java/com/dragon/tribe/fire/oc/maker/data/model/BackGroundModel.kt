package com.dragon.tribe.fire.oc.maker.data.model

import java.io.Serializable
data class BackGroundModel(
    val path: String?,
    var isSelected: Boolean = false,
    val type: BgType = BgType.NORMAL
): Serializable
enum class BgType {
    NONE, NORMAL
}
