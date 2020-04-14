package com.abu.imagecrop.view

import android.app.Activity
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.abu.imagecrop.R
import com.abu.imagecrop.databinding.ActivityCropBinding
import com.abu.imagecrop.viewmodel.CropActivityViewModel
import kotlinx.android.synthetic.main.actionbar_crop.view.*

class CropActivity : AppCompatActivity() {
    private lateinit var binding: ActivityCropBinding
    private lateinit var viewModel: CropActivityViewModel
    private lateinit var cropBitmap: Bitmap

    companion object {
        const val EXTRA_SOURCE_URI = "com.abu.imagecrop.view.EXTRA_SOURCE_URI"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_crop)
        viewModel = ViewModelProvider(this).get(CropActivityViewModel::class.java)
        viewModel.getImageFromUri(Uri.parse(intent.getStringExtra(EXTRA_SOURCE_URI)))
            .observe(this, Observer {
                binding.cropView.setBitmap(it) })

        initView()
    }

    private fun initView() {
        binding.includeActionbarCrop.iv_crop_back.setOnClickListener {
            setResult(Activity.RESULT_CANCELED)
            finish()
        }

        binding.includeActionbarCrop.iv_crop_confirm.setOnClickListener {
            binding.flProgress.visibility = View.VISIBLE
            cropBitmap = binding.cropView.getCroppedData()
            viewModel.saveBitmap(cropBitmap) { finish() }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        System.gc()
    }
}