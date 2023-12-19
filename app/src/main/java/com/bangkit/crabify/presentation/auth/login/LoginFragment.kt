package com.bangkit.crabify.presentation.auth.login

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.util.Patterns
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.bangkit.crabify.R
import com.bangkit.crabify.databinding.FragmentLoginBinding
import com.bangkit.crabify.utils.UiState
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.AuthCredential
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class LoginFragment : Fragment() {

    private var _binding: FragmentLoginBinding? = null
    private val binding get() = _binding!!

    private val viewModel: LoginViewModel by viewModels()

    private lateinit var googleSignInClient: GoogleSignInClient
    private lateinit var auth: FirebaseAuth


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentLoginBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val gso = GoogleSignInOptions
            .Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id))
            .requestEmail()
            .build()
        googleSignInClient = GoogleSignIn.getClient(requireActivity(), gso)
        auth = FirebaseAuth.getInstance()

        binding.btnLogin.setOnClickListener {
            if (validation()) {
                viewModel.login(
                    binding.etEmail.text.toString(),
                    binding.etPassword.text.toString()
                )
            }
        }
        binding.ivGoogle.setOnClickListener { signIn() }
        binding.tvRegisterHere.setOnClickListener {
            findNavController().navigate(R.id.action_loginFragment_to_registerFragment)
        }

        binding.tvForgotPassword.setOnClickListener {
            findNavController().navigate(R.id.action_loginFragment_to_forgotPassword)
        }

        viewModel.login.observe(viewLifecycleOwner) { state ->
            when (state) {
                is UiState.Loading -> {
                    binding.btnLogin.text = ""
                    binding.progressBar.visibility = View.VISIBLE
                }

                is UiState.Error -> {
                    binding.btnLogin.text = getString(R.string.login)
                    binding.progressBar.visibility = View.GONE
                    Toast.makeText(requireContext(), state.message, Toast.LENGTH_SHORT).show()
                }

                is UiState.Success -> {
                    binding.btnLogin.text = getString(R.string.login)
                    binding.progressBar.visibility = View.GONE
                    val action = LoginFragmentDirections.actionLoginFragmentToHomeActivity()
                    findNavController().navigate(action)
                }

                else -> {}
            }
        }
    }

    private fun validation(): Boolean {
        var isValid = true
        if (binding.etEmail.text.isNullOrEmpty()) {
            isValid = false
            Toast.makeText(requireContext(), "Enter email address", Toast.LENGTH_SHORT).show()
        } else {
            if (binding.etEmail.text.toString()
                    .isEmpty() && Patterns.EMAIL_ADDRESS.matcher(binding.etEmail.text.toString())
                    .matches()
            ) {
                isValid = false
                Toast.makeText(requireContext(), "Email must be valid", Toast.LENGTH_SHORT).show()
            }
        }
        if (binding.etPassword.text.isNullOrEmpty()) {
            isValid = false
            Toast.makeText(requireContext(), "Enter password", Toast.LENGTH_SHORT).show()
        } else {
            if (binding.etPassword.text.toString().length < 6) {
                isValid = false
                Toast.makeText(
                    requireContext(),
                    "Password must be at least 6 characters",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
        return isValid
    }

    private fun signIn() {
        val signInIntent = googleSignInClient.signInIntent
        resultLauncher.launch(signInIntent)
    }

    private var resultLauncher: ActivityResultLauncher<Intent> =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                val task: Task<GoogleSignInAccount> =
                    GoogleSignIn.getSignedInAccountFromIntent(result.data)
                try {
                    val account: GoogleSignInAccount = task.getResult(ApiException::class.java)!!
                    Log.d(TAG, "firebaseAuthWithGoogle:" + account.id)
                    firebaseAuthWithGoogle(account.idToken!!)
                } catch (e: ApiException) {
                    Log.w(TAG, "Google sign in failed", e)
                }
            }
        }

    private fun firebaseAuthWithGoogle(idToken: String) {
        val credential: AuthCredential = GoogleAuthProvider.getCredential(idToken, null)
        auth.signInWithCredential(credential)
            .addOnCompleteListener(requireActivity()) { task ->
                if (task.isSuccessful) {
                    Log.d(TAG, "signInWithCredential:success")
                    val user: FirebaseUser? = auth.currentUser
                    updateUI(user)
                } else {
                    Log.w(TAG, "signInWithCredential:failure", task.exception)
                    updateUI(null)
                }
            }
    }

    private fun updateUI(currentUser: FirebaseUser?) {
        if (currentUser != null) {
            val action = LoginFragmentDirections.actionLoginFragmentToHomeActivity()
            findNavController().navigate(action)
        }
    }

    companion object {
        private const val TAG = "LoginFragment"
    }
}