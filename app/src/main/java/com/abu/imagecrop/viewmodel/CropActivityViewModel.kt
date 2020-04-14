package com.abu.imagecrop.viewmodel

import android.app.Application
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import com.abu.imagecrop.extension.toByteArray
import com.abu.imagecrop.model.ImageRepo
import com.abu.imagecrop.model.ImageRepo.Companion.IMAGE_SIZE
import com.abu.imagecrop.model.db.ImageEntity
import io.reactivex.Completable
import io.reactivex.Single
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers

class CropActivityViewModel(val app: Application) : AndroidViewModel(app) {

    private val imageRepo = ImageRepo.getInstance()
    private val disposable = CompositeDisposable()

    private val sourceImageLiveData = MutableLiveData<Bitmap>()

    fun saveBitmap(cropImage: Bitmap, finish: () -> Unit) {
        Completable.create { emitter ->
                val thumbnail = Bitmap.createScaledBitmap(cropImage, IMAGE_SIZE, IMAGE_SIZE, false)
                ImageEntity(System.currentTimeMillis(), thumbnail.toByteArray()).let {
                    imageRepo.save(it)
                    emitter.onComplete()
                }
            }
            .observeOn(AndroidSchedulers.mainThread())
            .subscribeOn(Schedulers.io())
            .subscribe(finish)
            .apply {
                disposable.add(this)
            }
    }

    fun getImageFromUri(srcUri: Uri): MutableLiveData<Bitmap> {
        Single.create<Bitmap> { emitter ->
                val srcImage = BitmapFactory.decodeStream(
                    app.applicationContext.contentResolver.openInputStream(srcUri), null, null
                )!!
                emitter.onSuccess(srcImage)
            }
            .observeOn(AndroidSchedulers.mainThread())
            .subscribeOn(Schedulers.computation())
            .subscribe { src ->
                sourceImageLiveData.value = src
            }.apply { disposable.add(this) }
        return sourceImageLiveData
    }

    override fun onCleared() {
        super.onCleared()
        if (disposable.isDisposed.not()) {
            disposable.dispose()
        }
    }
}