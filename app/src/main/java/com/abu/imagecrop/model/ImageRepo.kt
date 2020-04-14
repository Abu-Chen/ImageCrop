package com.abu.imagecrop.model

import com.abu.imagecrop.model.db.ImageDB
import com.abu.imagecrop.model.db.ImageDao
import com.abu.imagecrop.model.db.ImageEntity

class ImageRepo {
    companion object {
        val IMAGE_SIZE = 300

        private var INSTANCE: ImageRepo? = null

        fun getInstance(): ImageRepo {
            if (INSTANCE == null) {
                synchronized(ImageRepo::class) { INSTANCE = ImageRepo() }
            }
            return INSTANCE!!
        }
    }

    private val dao: ImageDao = ImageDB.getInstance().imageDao()

    fun save(entity: ImageEntity) {
        dao.insertImage(entity)
    }

    fun queryThumbnailList(): List<ImageEntity> {
        return dao.loadAll()
    }

    fun deleteAll() {
        dao.deleteAll()
    }
}