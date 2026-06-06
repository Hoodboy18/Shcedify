package com.example.shcedify.onboarding.forgot

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Patterns
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.shcedify.databinding.FragmentForgotPasswordBinding
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth

class ForgotPasswordFragment : Fragment() {

    private var _binding: FragmentForgotPasswordBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentForgotPasswordBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.btnSend.isEnabled = false
        setupValidation()

        binding.btnSend.setOnClickListener {
            val email = binding.etEmail.text.toString().trim()
            sendPasswordReset(email)
        }

        binding.btnBack.setOnClickListener {
            findNavController().popBackStack()
        }
    }

    private fun setupValidation() {
        binding.etEmail.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) { validateFields() }
        })
    }

    private fun validateFields() {
        val email = binding.etEmail.text.toString().trim()
        val isEmailValid = Patterns.EMAIL_ADDRESS.matcher(email).matches()
        binding.tilEmail.error = if (email.isNotEmpty() && !isEmailValid) "Correo inválido" else null
        binding.btnSend.isEnabled = email.isNotEmpty() && isEmailValid
    }

    private fun sendPasswordReset(email: String) {
        binding.btnSend.isEnabled = false
        FirebaseAuth.getInstance().sendPasswordResetEmail(email)
            .addOnSuccessListener {
                Snackbar.make(
                    binding.root,
                    "¡Enlace enviado! Revisa tu correo.",
                    Snackbar.LENGTH_LONG
                ).show()
                findNavController().popBackStack()
            }
            .addOnFailureListener { e ->
                binding.btnSend.isEnabled = true
                Snackbar.make(
                    binding.root,
                    "Error: ${e.localizedMessage ?: "No se pudo enviar el enlace"}",
                    Snackbar.LENGTH_LONG
                ).show()
            }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}