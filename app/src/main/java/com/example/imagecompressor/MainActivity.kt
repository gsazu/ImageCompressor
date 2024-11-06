package com.example.imagecompressor

import android.os.Bundle
import android.webkit.MimeTypeMap
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import com.example.imagecompressor.databinding.ActivityMainBinding
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    val scope = lifecycleScope
    private lateinit var imageCompressor: ImageCompressor
    private lateinit var fileManager: FileManager
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        imageCompressor = ImageCompressor(this)
        fileManager = FileManager(this)

        binding.btnPickImage.setOnClickListener {
            photoPicker.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
        }
    }

    val photoPicker = registerForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
        if(uri != null){
            val mimeType = this.contentResolver.getType(uri)
            val extension = MimeTypeMap.getSingleton().getExtensionFromMimeType(mimeType)
            scope.launch {
                fileManager.saveImage(
                    contentUri = uri,
                    fileName = "uncompressed.${extension}"
                )
            }

            scope.launch {
                val compressedImage = imageCompressor.compressImage(contentUri = uri,
                    compressionThreshold = 200*1024L )

                scope.launch {
                    fileManager.saveImage(
                        bytes = compressedImage ?: return@launch,
                        fileName = "compressed.${extension}"
                    )
                }
            }
        }
    }
}