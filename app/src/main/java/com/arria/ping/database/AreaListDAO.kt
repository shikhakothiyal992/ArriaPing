package com.arria.ping.database

import androidx.room.*

@Dao
interface AreaListDAO {
    @Query("SELECT * FROM  arealistentity")
    suspend fun getAllArea(): List<AreaListEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAllArea(vararg todo: AreaListEntity)

    @Query("DELETE FROM arealistentity")
    suspend fun deleteAllArea()

    @Query("UPDATE arealistentity SET isSelect = :isSelect WHERE id =:areaCode")
    suspend fun updateStoreListSelectionArea(isSelect: Boolean, areaCode: Int)

    @Query("UPDATE arealistentity SET isSelect = :isSelect")
    suspend fun updateAllStoreListSelectionArea(isSelect: Boolean)

    @Query("SELECT areaCode FROM  arealistentity WHERE isSelect IN (:isSelect)")
    suspend fun getAllSelectedAreaList(isSelect: Boolean):List<String>

    @Query("SELECT name FROM  arealistentity WHERE isSelect IN (:isSelect)")
    suspend fun getAllSelectedAreaName(isSelect: Boolean):List<String>

}