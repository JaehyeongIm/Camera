package com.example.camera

import android.content.ContentValues
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.ImageDecoder
import android.net.Uri
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.text.TextUtils
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.viewbinding.BuildConfig
import com.example.camera.databinding.ActivityMainBinding
import java.io.File
import java.text.SimpleDateFormat

class MainActivity : BaseActivity() {
    val PERM_STORAGE = 9
    val PERM_CAMERA = 10
    val PERM_GALLERY = 12

    val REQ_CAMERA = 11
    val REQ_GALLERY = 13

    lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        binding = ActivityMainBinding.inflate(layoutInflater)
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        requirePermissions(arrayOf(android.Manifest.permission.WRITE_EXTERNAL_STORAGE), PERM_STORAGE)
    }

    override fun permissionGranted(requestCode: Int) {
        when(requestCode){
            PERM_STORAGE -> initView()
            PERM_CAMERA -> openCamera()
            PERM_GALLERY -> openGallery()
        }
    }

    override fun permissionDenied(requestCode: Int) {
        when(requestCode){
            PERM_STORAGE ->{
                Toast.makeText(this, "공용 저장소 권한을 승인해야 앱을 사용할 수 있습니다.",
                   Toast.LENGTH_SHORT).show()
                finish()
            }
            PERM_CAMERA->{
                Toast.makeText(this, "카메라 권한을 승인해야 카메라를 사용할 수 있습니다.",
                    Toast.LENGTH_SHORT).show()
            }
            PERM_GALLERY->{
                Toast.makeText(this, "갤러리 권한을 승인해야 카메라를 사용할 수 있습니다.",
                    Toast.LENGTH_SHORT).show()
            }
        }
    }

    fun initView(){
        binding.cameraBtn.setOnClickListener {
            requirePermissions(arrayOf(android.Manifest.permission.CAMERA), PERM_CAMERA)
        }

        binding.galleryBtn.setOnClickListener {
            requirePermissions(
                arrayOf(
                    android.Manifest.permission.WRITE_EXTERNAL_STORAGE,
                    android.Manifest.permission.READ_EXTERNAL_STORAGE
                ), PERM_GALLERY
            )
        }
    }

    var realUri: Uri?=null

    fun createImageUri(filename:String,mimeType:String): Uri? {
        val values = ContentValues()
        values.put(MediaStore.Images.Media.DISPLAY_NAME,filename)
        values.put(MediaStore.Images.Media.MIME_TYPE,mimeType)

        return contentResolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values)
    }

    fun newfileName() : String{
        val sdf = SimpleDateFormat("yyyyMMdd_HHmmss")
        val filename = sdf.format(System.currentTimeMillis())
        return "${filename}.jpg"
    }

    fun openGallery(){
        val intent = Intent(Intent.ACTION_PICK)
        intent.type = MediaStore.Images.Media.CONTENT_TYPE
        startActivityForResult(intent, REQ_GALLERY)
    }

    fun openCamera(){
        val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        createImageUri(newfileName(), "image/jpg")?.let{
            uri->realUri = uri
            intent.putExtra(MediaStore.EXTRA_OUTPUT, realUri)
            startActivityForResult( intent, REQ_CAMERA)
        }

    }


    fun loadBitmap(photoUri: Uri) : Bitmap?{
        try{
            return if(Build.VERSION.SDK_INT>Build.VERSION_CODES.O_MR1){
                val source = ImageDecoder.createSource(contentResolver, photoUri)
                ImageDecoder.decodeBitmap(source)
            }else{
                MediaStore.Images.Media.getBitmap(contentResolver,photoUri)
            }
        }catch (e:Exception){
            e.printStackTrace()
        }
        return null
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if(resultCode == RESULT_OK){
            when(requestCode){
                REQ_CAMERA->{
                    realUri?.let { uri->
                        val bitmap = loadBitmap(uri)
                        binding.imageView.setImageBitmap(bitmap)

                        realUri = null
                    }

                }
                REQ_GALLERY->{
                    data?.data.let { uri->
                        binding.imageView.setImageURI(uri)
                    }
                }
            }
        }
    }


}