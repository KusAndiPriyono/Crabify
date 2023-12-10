package com.bangkit.crabify.presentation.upload

import android.Manifest
import android.annotation.SuppressLint
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.navigation.Navigation
import com.bangkit.crabify.R
import com.bangkit.crabify.databinding.FragmentUploadBinding
import com.bangkit.crabify.ml.CompressedModelWithMetadataV2
import com.bangkit.crabify.presentation.notification.NotificationActivity
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

        createNotificationChannel()

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

        val resultText =
            "${highProbabilityOutput.label}\nScore: ${(highProbabilityOutput.score * 100).toInt()}%"
        binding.tvOutput.text = resultText
        Log.d(
            "TAG",
            "outputGenerator: ${highProbabilityOutput.label} ${highProbabilityOutput.score}"
        )

        if (highProbabilityOutput.score >= 0.80) {
            showNotification()
        }
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

//            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
//                val name = "Notification Channel"
//                val descriptionText = "Channel for notifications"
//                val importance = NotificationManagerCompat.IMPORTANCE_DEFAULT
//                val channel = NotificationChannel(channelId, name, importance).apply {
//                    description = descriptionText
//                    setAllowBubbles(true)
//                }
//
//                val notificationManager =
//                    requireContext().getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
//                notificationManager.createNotificationChannel(channel)
//            }

            with(NotificationManagerCompat.from(requireContext())) {
                notify(notificationId, notificationBuilder.build())
            }
        } else {
            requestPermissionLauncher.launch(Manifest.permission.VIBRATE)
        }
    }

    @SuppressLint("WrongConstant")
    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = "Notification Channel"
            val descriptionText = "Channel for notifications"
            val importance = NotificationManagerCompat.IMPORTANCE_DEFAULT
            val channel = NotificationChannel(channelId, name, importance).apply {
                description = descriptionText
            }

            val notificationManager =
                requireContext().getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }


    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        const val channelId = "notification_channel_id"
        const val notificationId = 1
        private val REQUIRED_PERMISSIONS =
            arrayOf(Manifest.permission.CAMERA, Manifest.permission.READ_EXTERNAL_STORAGE)
        const val ARG_SELECTED_IMAGE = "selected_image"
    }

}