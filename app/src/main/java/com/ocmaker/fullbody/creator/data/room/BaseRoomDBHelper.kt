package com.ocmaker.fullbody.creator.data.room

import android.content.Context
import androidx.room.Room
import com.ocmaker.fullbody.creator.utils.SingletonHolder


open class BaseRoomDBHelper(context: Context) {
    val db = Room.databaseBuilder(context, AppDB::class.java,"Avatar").build()
    companion object : SingletonHolder<BaseRoomDBHelper, Context>(::BaseRoomDBHelper)
}