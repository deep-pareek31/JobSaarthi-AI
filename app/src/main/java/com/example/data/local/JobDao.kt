package com.example.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface JobDao {
    @Query("SELECT * FROM cached_jobs ORDER BY lastCachedAt DESC")
    fun getAllJobs(): Flow<List<JobEntity>>

    @Query("SELECT * FROM cached_jobs WHERE isToday = 1 ORDER BY lastCachedAt DESC")
    fun getTodayJobs(): Flow<List<JobEntity>>

    @Query("SELECT * FROM cached_jobs WHERE isRecommended = 1 ORDER BY matchPercentage DESC")
    fun getRecommendedJobs(): Flow<List<JobEntity>>

    @Query("SELECT * FROM cached_jobs WHERE id = :id LIMIT 1")
    suspend fun getJobById(id: String): JobEntity?

    @Query("SELECT COUNT(*) FROM cached_jobs")
    suspend fun getCount(): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertJobs(jobs: List<JobEntity>)

    @Query("DELETE FROM cached_jobs WHERE isToday = 1")
    suspend fun clearTodayJobs()

    @Query("DELETE FROM cached_jobs WHERE isRecommended = 1")
    suspend fun clearRecommendedJobs()
}
