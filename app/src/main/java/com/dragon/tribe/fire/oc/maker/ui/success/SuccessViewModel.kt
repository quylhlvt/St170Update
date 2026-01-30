package com.dragon.tribe.fire.oc.maker.ui.success

import android.app.Activity
import android.graphics.Canvas
import android.view.View
import android.widget.Toast
import androidx.lifecycle.ViewModel
import com.dragon.tribe.fire.oc.maker.core.helper.MediaHelper
import com.dragon.tribe.fire.oc.maker.core.utils.SaveState
import com.dragon.tribe.fire.oc.maker.core.utils.HandleState
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.withContext
import androidx.core.graphics.createBitmap
import com.dragon.tribe.fire.oc.maker.R
import com.dragon.tribe.fire.oc.maker.core.extensions.showToast
import com.dragon.tribe.fire.oc.maker.core.utils.key.ValueKey
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class SuccessViewModel : ViewModel() {
    private val _pathInternal = MutableStateFlow<String>("")
    val pathInternal: StateFlow<String> = _pathInternal.asStateFlow()
    private val _typeUI = MutableStateFlow<Int>(-1)
    val typeUI: StateFlow<Int> = _typeUI.asStateFlow()
    fun setPath(path: String) {
        _pathInternal.value = path
    }
    fun setType(type: Int) {
        _typeUI.value = type
    }


    fun saveImageToExternalStorage(activity: Activity, targetView: View): Flow<SaveState> = flow {
        emit(SaveState.Loading)

        try {
            val bitmap = createBitmap(targetView.width, targetView.height)
            val canvas = Canvas(bitmap)
            targetView.draw(canvas)

            MediaHelper.saveBitmapToExternal(activity, bitmap).collect { handle ->
                when (handle) {
                    HandleState.LOADING -> {}
                    HandleState.SUCCESS -> {
                        withContext(Dispatchers.Main) {

                            Toast.makeText(activity,activity.getString(R.string.download_success), Toast.LENGTH_SHORT).show()
                        }
                        emit(SaveState.Success(path = ""))
                    }

                    HandleState.FAIL -> {
                        withContext(Dispatchers.Main) {
                            Toast.makeText(activity,activity.getString(R.string.save_failed_please_try_again), Toast.LENGTH_SHORT).show()
                        }
                        emit(SaveState.Error(Exception("Save failed")))
                    }

                    else -> Unit
                }
            }

        } catch (e: Exception) {
            withContext(Dispatchers.Main) {
                Toast.makeText(activity, "Đã xảy ra lỗi: ${e.message}", Toast.LENGTH_SHORT).show()
            }
            emit(SaveState.Error(e))
        }
    }.flowOn(Dispatchers.IO)

}
