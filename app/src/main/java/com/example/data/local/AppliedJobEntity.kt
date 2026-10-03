package com.example.data.local

import androidx.room.Dao
import androidx.room.Entity
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.PrimaryKey
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Entity(tableName = "cached_applied_jobs")
data class AppliedJobEntity(
    @PrimaryKey val id: String,
    val jobId: String,
    val title: String,
    val companyName: String,
    val location: String,
    val status: String,
    val appliedDate: String,
    val notes: String?,
    val applicationUrl: String,
    val updatedAt: Long = System.currentTimeMillis()
)

@Dao
interface AppliedJobDao {
    @Query("SELECT * FROM cached_applied_jobs ORDER BY appliedDate DESC")
    fun getAllAppliedJobs(): Flow<List<AppliedJobEntity>>

    @Query("SELECT COUNT(*) FROM cached_applied_jobs")
    suspend fun getCount(): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAppliedJob(job: AppliedJobEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(jobs: List<AppliedJobEntity>)

    @Query("UPDATE cached_applied_jobs SET status = :status, notes = :notes WHERE id = :id")
    suspend fun updateStatus(id: String, status: String, notes: String?)

    @Query("DELETE FROM cached_applied_jobs")
    suspend fun clear()
}
