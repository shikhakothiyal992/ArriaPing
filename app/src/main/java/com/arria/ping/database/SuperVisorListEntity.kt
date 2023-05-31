package com.arria.ping.database

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class SuperVisorListEntity(
    @ColumnInfo(name = "name")
    var name: String,
    @ColumnInfo(name = "supervisorNumber")
    var supervisorNumber: String,
    @ColumnInfo(name = "isSelect")
    var isSelect: Boolean
){
    @PrimaryKey(autoGenerate = true)
    var id: Int = 0
}