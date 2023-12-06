package com.bangkit.crabify.presentation.upload

import android.Manifest
import android.annotation.SuppressLint
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.navigation.Navigation
import com.bangkit.crabify.R
import com.bangkit.crabify.databinding.FragmentUploadBinding
import com.bangkit.crabify.ml.CompressedModelWithMetadataV2
import com.bangkit.crabify.utils.rotateFile
import com.bangkit.crabify.utils.uriToFile
import dagger.hilt.android.AndroidEntryPoint
import org.tensorflow.lite.support.image.TensorImage
import java.io.File

@AndroidEntryPoint
class UploadFragment : Fragment() {
    private var getFile: File? = null
    private var _binding: FragmentUploadBinding? = null
    private val binding get() = _binding!!

    private val requestPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted: Boolean ->
            val message = if (isGranted) "Permission granted" else "Permission denied"
            Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
        }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentUploadBinding.inflate(inflater, container, false)
        return binding.root
    }

    @Suppress("DEPRECATION")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        if (!allPermissionsGranted()) {
            requestPermissionLauncher.launch(Manifest.permission.CAMERA)
        }

        val fileUri = arguments?.get(ARG_SELECTED_IMAGE)
        if (fileUri != null) {
            val uri: Uri = fileUri as Uri
            val result = rotateFile(
                BitmapFactory.decodeStream(
                    requireContext().contentResolver.openInputStream(uri)
                )
            )
            binding.ivPickFile.setImageBitmap(result)
        }

        binding.openFile.setOnClickListener {
            startGallery()
        }
        binding.openCamera.setOnClickListener(Navigation.createNavigateOnClickListener(R.id.cameraFragment))
    }

    private fun allPermissionsGranted() = REQUIRED_PERMISSIONS.all {
        ContextCompat.checkSelfPermission(
            requireContext(),
            it
        ) == PackageManager.PERMISSION_GRANTED
    }

    @SuppressLint("IntentReset")
    private fun startGallery() {
        val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        intent.type = "image/*"
        val mimeTypes = arrayOf("image/jpeg", "image/png", "image/jpg")
        intent.putExtra(Intent.EXTRA_MIME_TYPES, mimeTypes)
        intent.flags = Intent.FLAG_GRANT_READ_URI_PERMISSION
        galleryLauncher.launch(intent)
    }

    private val galleryLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == AppCompatActivity.RESULT_OK) {
                val selectedImg: Uri = result.data?.data as Uri
                selectedImg.let { uri ->
                    val myFile = uriToFile(uri, requireContext())
                    getFile = myFile
                    binding.ivPickFile.setImageURI(uri)
                    outputGenerator(BitmapFactory.decodeFile(myFile.path))
                }
            } else {
                Log.e("TAG", "Gallery selection canceled")
            }
        }


    private fun outputGenerator(bitmap: Bitmap) {
        val crabifyModel = CompressedModelWithMetadataV2.newInstance(requireContext())

        // Creates inputs for reference.
        val newImage = bitmap.copy(Bitmap.Config.ARGB_8888, true)
        val tfImage = TensorImage.fromBitmap(newImage)

        // Runs model inference and gets result.
        val outputs = crabifyModel.process(tfImage)
            .probabilityAsCategoryList.apply {
                sortByDescending { it.score }
            }
        val highProbabilityOutput = outputs[0]

//        val expectedLabel = "Kepiting Soka"
//        val resultText = if (highProbabilityOutput.label != expectedLabel) {
//            "Kepiting Biasa"
//        } else {
//            "${highProbabilityOutput.label}\nScore: ${highProbabilityOutput.score}"
//        }

        val resultText = "${highProbabilityOutput.label}\nScore: ${highProbabilityOutput.score}"
        binding.tvOutput.text = resultText
        Log.d(
            "TAG",
            "outputGenerator: ${highProbabilityOutput.label} ${highProbabilityOutput.score}"
        )
    }


    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        private val REQUIRED_PERMISSIONS =
            arrayOf(Manifest.permission.CAMERA, Manifest.permission.READ_EXTERNAL_STORAGE)
        const val ARG_SELECTED_IMAGE = "selected_image"
    }

}