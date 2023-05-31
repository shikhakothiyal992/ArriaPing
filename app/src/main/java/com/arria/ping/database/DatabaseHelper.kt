package com.arria.ping.database

interface DatabaseHelper {
    // area
    suspend fun getUsersArea(): List<AreaListEntity>

    suspend fun insertAllArea(users: AreaListEntity)

    suspend fun updateStoreListCheckedArea(isSelect: Boolean, id: Int)

    suspend fun updateAllStoreListSelectionArea(isSelect: Boolean)

    suspend fun getAllSelectedAreaList(isSelect: Boolean): List<String>
    suspend fun deleteArea()
    suspend fun getAllSelectedAreaName(isSelect: Boolean): List<String>

    // state

    suspend fun getUsersState(): List<StateListEntity>

    suspend fun insertAllState(users: StateListEntity)

    suspend fun updateStoreListCheckedState(isSelect: Boolean, id: Int)

    suspend fun updateAllStoreListSelectionState(isSelect: Boolean)

    suspend fun getAllSelectedStoreListState(isSelect: Boolean): List<String>
    suspend fun deleteAllState()
    suspend fun getAllSelectedStateName(isSelect: Boolean): List<String>


    // store

    suspend fun getUsers(): List<StoreListEntity>

    suspend fun insertAll(users: StoreListEntity)

    suspend fun updateStoreListChecked(isSelect: Boolean, id: Int)

    suspend fun updateAllStoreListSelection(isSelect: Boolean)

    suspend fun getAllSelectedStoreList(isSelect: Boolean): List<String>
    suspend fun deleteAllStore()
    suspend fun getAllSelectedStoreName(isSelect: Boolean): List<String>


    // supervisor

    suspend fun getUsersSupervisor(): List<SuperVisorListEntity>

    suspend fun insertAllSupervisor(users: SuperVisorListEntity)

    suspend fun updateStoreListCheckedSupervisor(isSelect: Boolean, id: Int)

    suspend fun updateAllStoreListSelectionSupervisor(isSelect: Boolean)

    suspend fun getAllSelectedStoreListSupervisor(isSelect: Boolean): List<String>
    suspend fun deleteAllSupervisor()
    suspend fun getAllSelectedSuperVisorName(isSelect: Boolean): List<String>

    suspend fun getCountSelectedSuperVisorList(isSelect: Int):Int

}