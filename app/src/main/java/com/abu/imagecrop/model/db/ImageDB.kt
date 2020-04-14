package com.abu.imagecrop.model.db

import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.abu.imagecrop.MyApplication

@Database(entities = arrayOf(ImageEntity::class), version = 1)
abstract class ImageDB : RoomDatabase() {
    abstract fun imageDao(): ImageDao

    companion object {
        const val TABLE_NAME = "ImageDB_table"
        const val COLUMN_TIME_STAMP = "time_stamp"
        const val COLUMN_THUMBNAIL = "thumbnail"

        private var INSTANCE: ImageDB? = null

        fun getInstance(): ImageDB {
            if (INSTANCE == null) {
                synchronized(ImageDB::class) {
                    INSTANCE = Room.databaseBuilder(
                        MyApplication.context,
                        ImageDB::class.java,
                        ImageDB::class.java.simpleName
                    ).build()
                }
            }
            return INSTANCE!!
        }
    }
}