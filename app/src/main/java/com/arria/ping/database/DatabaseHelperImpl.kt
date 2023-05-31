package com.arria.ping.database

class DatabaseHelperImpl(private val appDatabase: AppDatabase) : DatabaseHelper {
    // area

    override suspend fun getUsersArea(): List<AreaListEntity> = appDatabase.areaListDao().getAllArea()

    override suspend fun insertAllArea(users: AreaListEntity) = appDatabase.areaListDao().insertAllArea(users)

    override suspend fun updateStoreListCheckedArea(isSelect: Boolean, id: Int)  = appDatabase.areaListDao().updateStoreListSelectionArea(isSelect,id)

    override suspend fun updateAllStoreListSelectionArea(isSelect: Boolean)  = appDatabase.areaListDao().updateAllStoreListSelectionArea(isSelect)

    override suspend fun getAllSelectedAreaList(isSelect: Boolean): List<String> = appDatabase.areaListDao().getAllSelectedAreaList(isSelect)

    override suspend fun deleteArea() = appDatabase.areaListDao().deleteAllArea()

    override suspend fun getAllSelectedAreaName(isSelect: Boolean): List<String>  = appDatabase.areaListDao().getAllSelectedAreaName(isSelect)


    // state
    override suspend fun getUsersState(): List<StateListEntity> = appDatabase.stateListDao().getAllState()

    override suspend fun insertAllState(users: StateListEntity) = appDatabase.stateListDao().insertAllState(users)

    override suspend fun updateStoreListCheckedState(isSelect: Boolean, id: Int)  = appDatabase.stateListDao().updateStoreListSelectionState(isSelect,id)

    override suspend fun updateAllStoreListSelectionState(isSelect: Boolean)  = appDatabase.stateListDao().updateAllStoreListSelectionState(isSelect)

    override suspend fun getAllSelectedStoreListState(isSelect: Boolean) :List<String> = appDatabase.stateListDao().getAllSelectedStateList(isSelect)

    override suspend fun deleteAllState() = appDatabase.stateListDao().deleteAllState()

    override suspend fun getAllSelectedStateName(isSelect: Boolean) :List<String> = appDatabase.stateListDao().getAllSelectedStateName(isSelect)




    // store
    override suspend fun getUsers(): List<StoreListEntity> = appDatabase.storeListDao().getAll()

    override suspend fun insertAll(users: StoreListEntity) = appDatabase.storeListDao().insertAll(users)

    override suspend fun updateStoreListChecked(isSelect: Boolean, id: Int)  = appDatabase.storeListDao().updateStoreListSelection(isSelect,id)

    override suspend fun updateAllStoreListSelection(isSelect: Boolean)  = appDatabase.storeListDao().updateAllStoreListSelection(isSelect)

    override suspend fun getAllSelectedStoreList(isSelect: Boolean) :List<String> = appDatabase.storeListDao().getAllSelectedStoreList(isSelect)

    override suspend fun deleteAllStore() = appDatabase.storeListDao().deleteAllStore()

    override suspend fun getAllSelectedStoreName(isSelect: Boolean) :List<String> = appDatabase.storeListDao().getAllSelectedStoreName(isSelect)

    // supervisor
    override suspend fun getUsersSupervisor(): List<SuperVisorListEntity> = appDatabase.supervisorListDao().getAllSuperVisor()

    override suspend fun insertAllSupervisor(users: SuperVisorListEntity) = appDatabase.supervisorListDao().insertAllSuperVisor(users)

    override suspend fun updateStoreListCheckedSupervisor(isSelect: Boolean, id: Int)  = appDatabase.supervisorListDao().updateStoreListSelectionSuperVisor(isSelect,id)

    override suspend fun updateAllStoreListSelectionSupervisor(isSelect: Boolean)  = appDatabase.supervisorListDao().updateAllStoreListSelectionSuperVisor(isSelect)

    override suspend fun getAllSelectedStoreListSupervisor(isSelect: Boolean) :List<String> = appDatabase.supervisorListDao().getAllSelectedSuperVisorList(isSelect)

    override suspend fun deleteAllSupervisor() = appDatabase.supervisorListDao().deleteAllSupervisor()

    override suspend fun getAllSelectedSuperVisorName(isSelect: Boolean) :List<String> = appDatabase.supervisorListDao().getAllSelectedSuperVisorName(isSelect)

    override suspend fun getCountSelectedSuperVisorList(isSelect: Int) : Int = appDatabase.supervisorListDao().getCountSelectedSuperVisorList(isSelect)


}