package com.arria.ping.database

import androidx.room.*

@Dao
interface SuperVisorListDAO {
    @Query("SELECT * FROM  supervisorlistentity")
    suspend fun getAllSuperVisor(): List<SuperVisorListEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAllSuperVisor(vararg todo: SuperVisorListEntity)

    @Query("DELETE FROM supervisorlistentity")
    suspend fun deleteAllSupervisor()

    @Query("UPDATE supervisorlistentity SET isSelect = :isSelect WHERE id =:id")
    suspend fun updateStoreListSelectionSuperVisor(isSelect: Boolean, id: Int)

    @Query("UPDATE supervisorlistentity SET isSelect = :isSelect")
    suspend fun updateAllStoreListSelectionSuperVisor(isSelect: Boolean)

    @Query("SELECT supervisorNumber FROM  supervisorlistentity WHERE isSelect IN (:isSelect)")
    suspend fun getAllSelectedSuperVisorList(isSelect: Boolean):List<String>

    @Query("SELECT name FROM  supervisorlistentity WHERE isSelect IN (:isSelect)")
    suspend fun getAllSelectedSuperVisorName(isSelect: Boolean):List<String>

    @Query("SELECT COUNT(*) FROM SuperVisorListEntity WHERE isSelect =:isSelect")
    suspend fun getCountSelectedSuperVisorList(isSelect: Int):Int

}