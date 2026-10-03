package com.example.data.local

import androidx.room.Dao
import androidx.room.Entity
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.PrimaryKey
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Entity(tableName = "cached_saved_jobs")
data class SavedJobEntity(
    @PrimaryKey val jobId: String,
    val title: String,
    val companyName: String,
    val location: String,
    val employmentType: String,
    val remoteType: String,
    val applicationUrl: String,
    val applicationDeadline: String?,
    val deadlineSource: String?,
    val category: String = "Saved",
    val notes: String? = null,
    val savedAt: Long = System.currentTimeMillis()
)

@Dao
interface SavedJobDao {
    @Query("SELECT * FROM cached_saved_jobs ORDER BY savedAt DESC")
    fun getAllSavedJobs(): Flow<List<SavedJobEntity>>

    @Query("SELECT COUNT(*) FROM cached_saved_jobs")
    suspend fun getCount(): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSavedJob(savedJob: SavedJobEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(savedJobs: List<SavedJobEntity>)

    @Query("DELETE FROM cached_saved_jobs WHERE jobId = :jobId")
    suspend fun deleteSavedJob(jobId: String)

    @Query("DELETE FROM cached_saved_jobs")
    suspend fun clearSavedJobs()
}
