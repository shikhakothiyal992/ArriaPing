package com.arria.ping.database

import androidx.room.*

@Dao
interface StoreListDAO {
    @Query("SELECT * FROM  storelistentity")
    suspend fun getAll(): List<StoreListEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(vararg todo: StoreListEntity)

    @Query("DELETE FROM storelistentity")
    suspend fun deleteAllStore()

    @Query("UPDATE storelistentity SET isSelect = :isSelect WHERE id =:id")
    suspend fun updateStoreListSelection(isSelect: Boolean, id: Int)

    @Query("UPDATE storelistentity SET isSelect = :isSelect")
    suspend fun updateAllStoreListSelection(isSelect: Boolean)

    @Query("SELECT storeNumber FROM  storelistentity WHERE isSelect IN (:isSelect)")
    suspend fun getAllSelectedStoreList(isSelect: Boolean):List<String>

    @Query("SELECT name FROM  storelistentity WHERE isSelect IN (:isSelect)")
    suspend fun getAllSelectedStoreName(isSelect: Boolean):List<String>

}