package com.bangkit.crabify.presentation.camera

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.camera.core.AspectRatio
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageCapture
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.bangkit.crabify.R
import com.bangkit.crabify.databinding.FragmentCameraBinding
import com.bangkit.crabify.ml.CompressedModelWithMetadataV2
import com.bangkit.crabify.utils.ImageClassifierHelper
import com.google.android.material.bottomnavigation.BottomNavigationView
import dagger.hilt.android.AndroidEntryPoint
import org.tensorflow.lite.task.vision.classifier.Classifications
import java.text.NumberFormat
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

@AndroidEntryPoint
class CameraFragment : Fragment() {

    private var _binding: FragmentCameraBinding? = null
    private val binding get() = _binding!!

    private lateinit var cameraExecutor: ExecutorService
    private var imageCapture: ImageCapture? = null
    private lateinit var imageClassifier: CompressedModelWithMetadataV2
    private lateinit var imageClassifierHelper: ImageClassifierHelper


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentCameraBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        cameraExecutor = Executors.newSingleThreadExecutor()
        imageClassifier = CompressedModelWithMetadataV2.newInstance(requireContext())
        startCamera()

//        binding.captureImage.setOnClickListener { takePhoto() }
    }

//    private fun takePhoto() {
//        val imageCapture = imageCapture ?: return
//        val photoFile = createFile(requireActivity().application)
//        val outputOptions = ImageCapture.OutputFileOptions.Builder(photoFile).build()
//        imageCapture.takePicture(
//            outputOptions,
//            ContextCompat.getMainExecutor(requireContext()),
//            object : ImageCapture.OnImageSavedCallback {
//                override fun onImageSaved(outputFileResults: ImageCapture.OutputFileResults) {
//                    val savedUri = outputFileResults.savedUri ?: Uri.fromFile(photoFile)
//                    val bundle = Bundle()
//                    bundle.putParcelable("selected_image", savedUri)
//                    // Load the captured image and classify it using ImageClassifierHelper
//                    val bitmap = BitmapFactory.decodeStream(
//                        requireActivity().contentResolver.openInputStream(savedUri)
//                    )
//                    crabifyImage(bitmap)
//                    // Navigate to the next fragment
//                    findNavController().navigate(
//                        R.id.action_cameraFragment_to_uploadFragment,
//                        bundle
//                    )
//                }
//
//                override fun onError(exception: ImageCaptureException) {
//                    Toast.makeText(requireContext(), "Gagal mengambil gambar.", Toast.LENGTH_SHORT)
//                        .show()
//                }
//            })
//    }

//    private fun crabifyImage(bitmap: Bitmap) {
//        val tfImage = TensorImage.fromBitmap(bitmap)
//        val outputs = imageClassifier.process(tfImage)
//            .probabilityAsCategoryList.apply {
//                sortByDescending { it.score }
//            }
//        val highProbabilityOutput = outputs[0]
//        Log.d(
//            TAG,
//            "outputGenerator: ${highProbabilityOutput.label} ${highProbabilityOutput.score}"
//        )
//    }

    private fun startCamera() {

        (requireActivity() as AppCompatActivity).findViewById<BottomNavigationView>(R.id.bottomNavigationView).visibility =
            View.GONE

        imageClassifierHelper = ImageClassifierHelper(
            context = requireContext(),
            imageClassifierListener = object : ImageClassifierHelper.ClassifierListener {
                override fun onError(error: String) {
                    activity?.runOnUiThread {
                        Toast.makeText(context, error, Toast.LENGTH_SHORT).show()
                    }
                }

                override fun onResults(results: List<Classifications>?, inferenceTime: Long) {
                    activity?.runOnUiThread {
                        results?.let { it ->
                            if (it.isNotEmpty() && it[0].categories.isNotEmpty()) {
                                println(it)
                                val sortedCategories =
                                    it[0].categories.sortedByDescending { it?.score }
                                val displayResult =
                                    sortedCategories.joinToString("\n") {
                                        "${it.label} " + NumberFormat.getPercentInstance()
                                            .format(it.score).trim()
                                    }
                                binding.tvResult.text = displayResult
                            }
                        }
                    }
                }
            })

        val cameraProviderFuture = ProcessCameraProvider.getInstance(requireContext())

        cameraProviderFuture.addListener({
            val cameraProvider: ProcessCameraProvider = cameraProviderFuture.get()

            @Suppress("DEPRECATION")
            val preview = Preview.Builder()
                .setTargetAspectRatio(AspectRatio.RATIO_4_3)
                .build()
                .also {
                    it.setSurfaceProvider(binding.viewFinder.surfaceProvider)
                }

            imageCapture = ImageCapture.Builder().build()

            @Suppress("DEPRECATION") val imageAnalyzer =
                ImageAnalysis.Builder()
                    .setTargetAspectRatio(AspectRatio.RATIO_4_3)
                    .setTargetRotation(binding.viewFinder.display.rotation)
                    .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                    .setOutputImageFormat(ImageAnalysis.OUTPUT_IMAGE_FORMAT_RGBA_8888)
                    .build()
                    // The analyzer can then be assigned to the instance
                    .also {
                        it.setAnalyzer(Executors.newSingleThreadExecutor()) { image ->
                            // Pass Bitmap and rotation to the image classifier helper for processing and classification
                            imageClassifierHelper.classify(image)
                        }
                    }
            try {
                cameraProvider.unbindAll()
                cameraProvider.bindToLifecycle(
                    this,
                    CameraSelector.DEFAULT_BACK_CAMERA,
                    preview,
                    imageAnalyzer,
                    imageCapture
                )
            } catch (exc: Exception) {
                Toast.makeText(requireContext(), "Gagal memunculkan kamera.", Toast.LENGTH_SHORT)
                    .show()
            }
        }, ContextCompat.getMainExecutor(requireContext()))
    }

    override fun onDestroyView() {
        super.onDestroyView()
        (requireActivity() as AppCompatActivity).findViewById<BottomNavigationView>(R.id.bottomNavigationView).visibility =
            View.VISIBLE
        _binding = null
        cameraExecutor.shutdown()
    }


//    companion object {
//        private const val TAG = "CameraFragment"
//    }
}