package com.arria.ping.database

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(entities = [StoreListEntity::class,AreaListEntity::class,StateListEntity::class,SuperVisorListEntity::class],version = 1,exportSchema = false)
abstract class AppDatabase : RoomDatabase() {

    abstract fun storeListDao(): StoreListDAO
    abstract fun areaListDao(): AreaListDAO
    abstract fun stateListDao(): StateListDAO
    abstract fun supervisorListDao(): SuperVisorListDAO
}
