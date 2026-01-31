package com.dragon.tribe.fire.oc.maker.data.room

import android.content.Context
import androidx.room.Room
import com.dragon.tribe.fire.oc.maker.utils.SingletonHolder


open class BaseRoomDBHelper(context: Context) {
    val db = Room.databaseBuilder(context, AppDB::class.java,"Avatar").build()
    companion object : SingletonHolder<BaseRoomDBHelper, Context>(::BaseRoomDBHelper)
}