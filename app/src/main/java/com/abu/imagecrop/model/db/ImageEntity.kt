package com.abu.imagecrop.model.db

import androidx.room.ColumnInfo
import androidx.room.Entity
import com.abu.imagecrop.model.db.ImageDB.Companion.COLUMN_THUMBNAIL
import com.abu.imagecrop.model.db.ImageDB.Companion.COLUMN_TIME_STAMP
import com.abu.imagecrop.model.db.ImageDB.Companion.TABLE_NAME


@Entity(tableName = TABLE_NAME, primaryKeys = [COLUMN_TIME_STAMP])
data class ImageEntity(
    @ColumnInfo(name = COLUMN_TIME_STAMP) val timeStamp: Long,
    @ColumnInfo(name = COLUMN_THUMBNAIL) val thumbnail: ByteArray
)