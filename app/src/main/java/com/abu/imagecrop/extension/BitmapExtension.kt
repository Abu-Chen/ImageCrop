package com.abu.imagecrop.extension

import android.graphics.Bitmap
import java.nio.ByteBuffer


fun Bitmap.toByteArray(): ByteArray {
    val bytesBitmap = ByteArray(this.byteCount)

    ByteBuffer.allocate(this.byteCount).also {
        this.copyPixelsToBuffer(it)
    }.let {
        //Rewinds this buffer.  The position is set to zero and the mark is discarded.
        it.rewind()
        //This method transfers bytes from this buffer into the given destination array.
        it.get(bytesBitmap)
    }
    return bytesBitmap
}

fun ByteArray.byteArrayToBitmap(): Bitmap {
    return Bitmap.createBitmap(300, 300, Bitmap.Config.ARGB_8888).apply {
        this.copyPixelsFromBuffer(ByteBuffer.wrap(this@byteArrayToBitmap))
    }
    //return BitmapFactory.decodeByteArray(this, 0, size)
}

