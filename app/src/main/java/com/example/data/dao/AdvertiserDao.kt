package com.example.data.dao

import androidx.room.*
import com.example.data.model.Advertiser
import kotlinx.coroutines.flow.Flow

@Dao
interface AdvertiserDao {
    @Query("SELECT * FROM advertisers ORDER BY id ASC")
    fun getAllAdvertisers(): Flow<List<Advertiser>>

    @Query("SELECT COUNT(*) FROM advertisers")
    suspend fun getAdvertisersCount(): Int

    @Query("SELECT * FROM advertisers WHERE isActive = 1 ORDER BY id DESC")
    fun getActiveAdvertisers(): Flow<List<Advertiser>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAdvertiser(advertiser: Advertiser): Long

    @Update
    suspend fun updateAdvertiser(advertiser: Advertiser)

    @Delete
    suspend fun deleteAdvertiser(advertiser: Advertiser)

    @Query("UPDATE advertisers SET clickCount = clickCount + 1 WHERE id = :id")
    suspend fun incrementClickCount(id: Int)
}
