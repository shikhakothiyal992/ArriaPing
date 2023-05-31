package com.arria.ping.database

import androidx.room.*

@Entity
data class StateListEntity(
    @ColumnInfo(name = "name")
    var name: String,
    @ColumnInfo(name = "stateCode")
    var stateCode: String,
    @ColumnInfo(name = "isSelect")
    var isSelect: Boolean,
) {
    @PrimaryKey(autoGenerate = true)
    var id: Int = 0
}