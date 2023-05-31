package com.arria.ping.database

import androidx.room.*

@Dao
interface StateListDAO {
    @Query("SELECT * FROM  statelistentity")
    suspend fun getAllState(): List<StateListEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAllState(vararg todo: StateListEntity)

    @Query("DELETE FROM statelistentity")
    suspend fun deleteAllState()

    @Query("UPDATE statelistentity SET isSelect = :isSelect WHERE id =:int")
    suspend fun updateStoreListSelectionState(isSelect: Boolean, int: Int)

    @Query("UPDATE statelistentity SET isSelect = :isSelect")
    suspend fun updateAllStoreListSelectionState(isSelect: Boolean)

    @Query("SELECT stateCode FROM  statelistentity WHERE isSelect IN (:isSelect)")
    suspend fun getAllSelectedStateList(isSelect: Boolean):List<String>

    @Query("SELECT name FROM  statelistentity WHERE isSelect IN (:isSelect)")
    suspend fun getAllSelectedStateName(isSelect: Boolean):List<String>

}