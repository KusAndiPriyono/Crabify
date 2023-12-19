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
import androidx.fragment.app.viewModels
import androidx.navigation.Navigation
import com.bangkit.crabify.R
import com.bangkit.crabify.data.model.Crab
import com.bangkit.crabify.databinding.FragmentClassificationBinding
import com.bangkit.crabify.ml.CompressedModelWithMetadataV2
import com.bangkit.crabify.presentation.auth.login.LoginViewModel
import com.bangkit.crabify.presentation.notification.NotificationActivity
import com.bangkit.crabify.utils.UiState
import com.bangkit.crabify.utils.rotateFile
import com.bangkit.crabify.utils.uriToFile
import dagger.hilt.android.AndroidEntryPoint
import org.tensorflow.lite.support.image.TensorImage
import java.io.File
import java.text.NumberFormat

@AndroidEntryPoint
class ClassificationFragment : Fragment() {
    private var getFile: File? = null
    private var _binding: FragmentClassificationBinding? = null
    private val binding get() = _binding!!

    private val viewModel: ClassificationViewModel by viewModels()
    private val authViewModel: LoginViewModel by viewModels()

    private val requestPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted: Boolean ->
            val message = if (isGranted) "Permission granted" else "Permission denied"
            Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
        }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentClassificationBinding.inflate(inflater, container, false)
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
            binding.ivResult.setImageBitmap(result)
        }

        binding.btnGenerate.setOnClickListener {
            if (getFile != null) {
                val bitmap = BitmapFactory.decodeFile(getFile?.absolutePath)
                outputGenerator(bitmap)
            } else {
                binding.tvOutput.text = "Silahkan pilih gambar terlebih dahulu"
            }
        }

        binding.openFile.setOnClickListener {
            startGallery()
        }
        binding.openCamera.setOnClickListener(Navigation.createNavigateOnClickListener(R.id.navigation_camera))
    }

    private fun allPermissionsGranted() = REQUIRED_PERMISSIONS.all {
        ContextCompat.checkSelfPermission(
            requireContext(), it
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
                    binding.ivResult.setImageURI(uri)
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
        val outputs = crabifyModel.process(tfImage).probabilityAsCategoryList.apply {
            sortByDescending { it.score }
        }

        val validCategories = outputs.filter {
            it.label == "kepiting soka" || it.label == "kepiting biasa"
        }

        if (validCategories.isNotEmpty()) {
            val highProbabilityOutput = validCategories[0]
            val resultText =
                "${highProbabilityOutput.label}\nScore: " + NumberFormat.getPercentInstance()
                    .format(highProbabilityOutput.score)
            binding.tvOutput.text = resultText
            if (highProbabilityOutput.score >= 0.70) {
                viewModel.uploadSingleFile(Uri.fromFile(getFile)) { result ->
                    when (result) {
                        is UiState.Loading -> {
                            Log.d("TAG", "outputGenerator: Loading")
                        }

                        is UiState.Success -> {
                            val uploadUri = result.data
                            val user_id = getSession()
                            Log.d("TAG", "outputGenerator: ${result.data}")
                            val crab = createCrab(
                                highProbabilityOutput.label,
                                highProbabilityOutput.score * 100.toFloat(),
                                uploadUri.toString(),
                                user_id
                            )
                            viewModel.addCrab(crab)
                        }

                        is UiState.Error -> {
                            Log.d("TAG", "outputGenerator: ${result.message}")
                        }
                    }
                }
                showNotification()
            }
        } else {
            binding.tvOutput.text = "Tidak ada hasil"
        }
    }

    private fun getSession(): String {
        var user_id = ""
        authViewModel.getSession {
            it?.let { user ->
                user_id = user.id
            }
        }
        return user_id
    }

    private fun createCrab(label: String, score: Float, imageUri: String, user_id: String): Crab {
        val crab = Crab(
            label = arrayListOf(label),
            score = arrayListOf(score),
            image = imageUri,
            user_id = user_id
        )
        Log.d("TAG", "createCrab: $crab")
        return crab
    }

    @SuppressLint("WrongConstant")
    private fun showNotification() {
        if (ContextCompat.checkSelfPermission(
                requireContext(), Manifest.permission.VIBRATE
            ) == PackageManager.PERMISSION_GRANTED
        ) {

//            val channelId = "your_notification_channel_id"
            val notificationBuilder = NotificationCompat.Builder(requireContext(), channelId)
                .setSmallIcon(R.drawable.ic_notification).setContentTitle("Kepiting sudah molting")
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