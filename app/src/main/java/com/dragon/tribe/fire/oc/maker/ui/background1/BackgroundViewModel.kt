package com.dragon.tribe.fire.oc.maker.ui.background1

import android.content.Context
import android.view.View
import androidx.lifecycle.ViewModel
import com.dragon.tribe.fire.oc.maker.core.helper.AssetHelper
import com.dragon.tribe.fire.oc.maker.core.helper.BitmapHelper
import com.dragon.tribe.fire.oc.maker.core.helper.MediaHelper
import com.dragon.tribe.fire.oc.maker.core.utils.SaveState
import com.dragon.tribe.fire.oc.maker.core.utils.key.AssetsKey
import com.dragon.tribe.fire.oc.maker.core.utils.key.ValueKey
import com.dragon.tribe.fire.oc.maker.data.model.BackGroundModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class BackgroundViewModel : ViewModel() {
    private val _backgroundList = MutableStateFlow<ArrayList<BackGroundModel>>(arrayListOf())
    val backgroundList: StateFlow<ArrayList<BackGroundModel>> = _backgroundList.asStateFlow()

    private val _pathInternalTemp = MutableStateFlow<String>("")
    val pathInternalTemp: StateFlow<String> = _pathInternalTemp.asStateFlow()



    fun loadBackground(context: Context) {
        val list = arrayListOf<BackGroundModel>()
        list.add(BackGroundModel(path = null, isSelected = true))
        val assetList = AssetHelper.getSubfoldersAsset(context, AssetsKey.BACKGROUND_ASSET)
        assetList.forEachIndexed { index, path ->
            list.add(BackGroundModel(path = path, isSelected = false))
        }
        _backgroundList.value = list
    }
    fun changeFocusBackgroundList(position: Int) {
        _backgroundList.value = _backgroundList.value.mapIndexed { index, model ->
            model.copy(isSelected = index == position)
        }.toCollection(ArrayList())
    }

    fun setPathInternalTemp(path: String) {
        _pathInternalTemp.value = path
    }

    fun saveImageFromView(context: Context, view: View): Flow<SaveState> = flow {
        emit(SaveState.Loading)
        val bitmap = BitmapHelper.createBimapFromView(view)
        MediaHelper.saveBitmapToInternalStorage(
            context,
            ValueKey.ALBUM_BACKGROUND,
            bitmap
        ).collect { state ->
            emit(state)
        }
    }.flowOn(Dispatchers.IO)


}