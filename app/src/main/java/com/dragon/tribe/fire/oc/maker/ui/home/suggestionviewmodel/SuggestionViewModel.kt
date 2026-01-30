package com.dragon.tribe.fire.oc.maker.ui.home.suggestionviewmodel

import androidx.lifecycle.ViewModel
import com.dragon.tribe.fire.oc.maker.data.model.SuggestionModel

class SuggestionViewModel : ViewModel() {
    val randomList = ArrayList<SuggestionModel>()
    //-----------------------------------------------------------------------------------------------------------------

    suspend fun updateRandomList(suggestionModel: SuggestionModel){
        randomList.add(suggestionModel)
    }


}