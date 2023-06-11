package com.ale.texttospeech.core.database

import androidx.lifecycle.LiveData
import androidx.room.ColumnInfo
import androidx.room.Dao
import androidx.room.Entity
import androidx.room.Insert
import androidx.room.PrimaryKey
import androidx.room.Query
import androidx.room.Update

@Entity
data class Setting (
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    var language: String?,
    var voice: String?,
    var speechRate: Float,
    var pitch: Float,
    var currentText: String?,
)

@Dao
interface SettingDao {
    @Query("SELECT * FROM setting")
    fun getAll(): LiveData<List<Setting>>

    @Insert
    suspend fun insert(setting: Setting)

    @Update
    suspend fun update(setting: Setting)
}