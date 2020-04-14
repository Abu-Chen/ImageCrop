package com.abu.imagecrop.model.db

import androidx.room.*

@Dao
interface ImageDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertImage(entity: ImageEntity)

    @Update
    fun updteImage(entity: ImageEntity)

    @Delete
    fun deleteImage(entity: ImageEntity)

    @Query("DELETE FROM ImageDB_table")
    fun deleteAll()

    @Query("SELECT * FROM ImageDB_table ORDER BY time_stamp DESC")
    fun loadAll(): List<ImageEntity>
}