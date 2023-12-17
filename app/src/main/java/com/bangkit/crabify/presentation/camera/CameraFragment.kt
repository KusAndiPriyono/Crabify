package com.bangkit.crabify.presentation.camera

import android.Manifest
import android.annotation.SuppressLint
import android.app.PendingIntent
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.camera.core.AspectRatio
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.bangkit.crabify.R
import com.bangkit.crabify.data.model.Crab
import com.bangkit.crabify.databinding.FragmentCameraBinding
import com.bangkit.crabify.ml.CompressedModelWithMetadataV2
import com.bangkit.crabify.presentation.notification.NotificationActivity
import com.bangkit.crabify.presentation.upload.ClassificationFragment
import com.bangkit.crabify.presentation.upload.ClassificationFragment.Companion.channelId
import com.bangkit.crabify.presentation.upload.ClassificationViewModel
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
    private val classificationViewModel: ClassificationViewModel by viewModels()

    private lateinit var cameraExecutor: ExecutorService
    private lateinit var imageClassifier: CompressedModelWithMetadataV2
    private lateinit var imageClassifierHelper: ImageClassifierHelper
    private var imageUri: Uri? = null


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
    }

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
                                val highestProbability = sortedCategories[0]
                                if (highestProbability.label == "kepiting soka" && highestProbability.score >= 0.80) {
                                    val crab = Crab(
                                        label = arrayListOf(highestProbability.label),
                                        score = arrayListOf(highestProbability.score),
                                    )
                                    classificationViewModel.addCrab(crab)
                                    showNotification()
                                }
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
                )
            } catch (exc: Exception) {
                Toast.makeText(requireContext(), "Gagal memunculkan kamera.", Toast.LENGTH_SHORT)
                    .show()
            }
        }, ContextCompat.getMainExecutor(requireContext()))
    }

    @SuppressLint("WrongConstant")
    private fun showNotification() {
        if (ContextCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.VIBRATE
            ) == PackageManager.PERMISSION_GRANTED
        ) {

//            val channelId = "your_notification_channel_id"
            val notificationBuilder = NotificationCompat.Builder(requireContext(), channelId)
                .setSmallIcon(R.drawable.ic_notification)
                .setContentTitle("Kepiting sudah molting")
                .setContentText("The score is greater than or equal to 0.80!")
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)

            // Intent untuk membuka NotificationActivity
            val intent = Intent(requireContext(), NotificationActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            val pendingIntent = PendingIntent.getActivity(
                requireContext(),
                0,
                intent,
                PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
            )

            notificationBuilder.setContentIntent(pendingIntent)
            notificationBuilder.setAutoCancel(true)

            with(NotificationManagerCompat.from(requireContext())) {
                notify(ClassificationFragment.notificationId, notificationBuilder.build())
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        (requireActivity() as AppCompatActivity).findViewById<BottomNavigationView>(R.id.bottomNavigationView).visibility =
            View.VISIBLE
        _binding = null
        cameraExecutor.shutdown()
    }
    
    companion object {
        private const val TAG = "CameraFragment"
    }
}