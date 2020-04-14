package com.abu.imagecrop.view

import android.Manifest
import android.app.Activity
import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.RequiresApi
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.GridLayoutManager
import com.abu.imagecrop.R
import com.abu.imagecrop.databinding.MainFragmentBinding
import com.abu.imagecrop.utils.Permssion
import com.abu.imagecrop.utils.Utils
import com.abu.imagecrop.view.CropActivity.Companion.EXTRA_SOURCE_URI
import com.abu.imagecrop.view.component.ThumbnailAdapter
import com.abu.imagecrop.viewmodel.MainViewModel


class MainFragment : Fragment() {

    companion object {
        private const val REQUEST_CAMERA = 0x00
        private const val REQUEST_GALLERY = 0x01

        private const val REQ_PERMIT_STORAGE = 0x20
        private const val REQ_PERMIT_CAMERA = 0x21

        fun newInstance() = MainFragment()
    }

    private val TAG = "MainFragment"
    private lateinit var viewModel: MainViewModel
    private lateinit var binding: MainFragmentBinding

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        binding = DataBindingUtil.inflate(inflater, R.layout.main_fragment, container, false)
        return binding.root
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        viewModel = ViewModelProvider(this).get(MainViewModel::class.java)
        viewModel.bindThumbnailList().observe(viewLifecycleOwner, Observer {
            binding.rvThumbnailList.adapter?.notifyDataSetChanged()
        })
        binding.rvThumbnailList.apply {
            this.adapter = ThumbnailAdapter(viewModel.bindThumbnailList())
            this.layoutManager = GridLayoutManager(context, 2)
        }
        binding.fabCrop.setOnClickListener { showChooseSource() }
        binding.fabClear.setOnClickListener { viewModel.clearThumbnailList() }
    }

    override fun onResume() {
        super.onResume()
        viewModel.updateThumbnailList()
    }

    private fun showChooseSource() {
        context?.let { context ->
            AlertDialog.Builder(context)
                .setItems(arrayOf("Take from camera", "Take from gallery")) { _, which ->
                    when (which) {
                        0 -> getImageFromCamera(context)
                        1 -> getImageFromGallery(context)
                    }
                }.setNeutralButton("Cancel", null).show()
        }
    }

    private fun getImageFromCamera(context: Context) {
        if (Utils.checkPermission(context, Manifest.permission.CAMERA) == Permssion.DENIED) {
            requestPermissions(arrayOf(Manifest.permission.CAMERA), REQ_PERMIT_CAMERA)
            return
        }

        Intent(MediaStore.ACTION_IMAGE_CAPTURE).apply {
            putExtra(MediaStore.EXTRA_OUTPUT, Utils.getCacheUri(context))
        }.let {
            startActivityForResult(it, REQUEST_CAMERA)
        }
    }

    private fun getImageFromGallery(context: Context) {
        if (Utils.checkPermission(
                context,
                Manifest.permission.READ_EXTERNAL_STORAGE,
                Manifest.permission.WRITE_EXTERNAL_STORAGE
            ) == Permssion.DENIED
        ) {
            requestPermissions(
                arrayOf(
                    Manifest.permission.READ_EXTERNAL_STORAGE,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE
                ), REQ_PERMIT_STORAGE
            )
            return
        }

        startActivityForResult(
            Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI),
            REQUEST_GALLERY
        )
    }

    @RequiresApi(Build.VERSION_CODES.M)
    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        Log.d(TAG, "requestCode:${requestCode}, permissions:${permissions}, grantResults:${grantResults}, ")
        if (grantResults.contains(PackageManager.PERMISSION_DENIED)) {
            //TODO shouldShowRequestPermissionRationale
            return
        }
        when (requestCode) {
            REQ_PERMIT_CAMERA -> context?.let { getImageFromCamera(it) }
            REQ_PERMIT_STORAGE -> context?.let { getImageFromGallery(it) }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        Log.d(TAG, "requestCode:${requestCode}, resultCode:${resultCode}, ")
        if (resultCode != Activity.RESULT_OK) return
        when (requestCode) {
            REQUEST_CAMERA -> context?.let { triggerCrop(Utils.getCacheCameraUri(it)) }
            REQUEST_GALLERY -> data?.data?.let { triggerCrop(it) }
        }
    }

    private fun triggerCrop(srcUri: Uri) {
        context?.let {
            Intent(it, CropActivity::class.java).putExtra(EXTRA_SOURCE_URI, srcUri.toString()).apply {
                it.startActivity(this)
            }
        }
    }
}
