package com.abu.imagecrop.viewmodel

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.abu.imagecrop.model.ImageRepo
import com.abu.imagecrop.model.db.ImageEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

class MainViewModel: ViewModel() {
    private val repo = ImageRepo.getInstance()
    private val thumbnailList: MutableLiveData<List<ImageEntity>> = MutableLiveData<List<ImageEntity>>()

    fun bindThumbnailList(): MutableLiveData<List<ImageEntity>> {
        return thumbnailList
    }

    fun updateThumbnailList() {
        GlobalScope.launch(Dispatchers.IO) {
            repo.queryThumbnailList().let {
                thumbnailList.postValue(it)
            }
        }
    }

    fun clearThumbnailList() {
        GlobalScope.launch(Dispatchers.IO) {
            repo.deleteAll()
            thumbnailList.postValue(listOf())
        }
    }
}
