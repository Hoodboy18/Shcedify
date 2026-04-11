package com.example.shcedify

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Patterns
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.shcedify.databinding.FragmentRegisterBinding

class RegisterFragment : Fragment() {

    private var _binding: FragmentRegisterBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentRegisterBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Botón deshabilitado al inicio
        binding.btnRegister.isEnabled = false

        setupValidation()

        binding.btnRegister.setOnClickListener {
            findNavController().navigate(R.id.action_register_to_personal_info)
        }

        binding.btnBack.setOnClickListener {
            findNavController().popBackStack()
        }
    }

    private fun setupValidation() {
        val watcher = object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                validateFields()
            }
        }

        binding.etName.addTextChangedListener(watcher)
        binding.etEmail.addTextChangedListener(watcher)
        binding.etPassword.addTextChangedListener(watcher)
    }

    private fun validateFields() {
        val name = binding.etName.text.toString().trim()
        val email = binding.etEmail.text.toString().trim()
        val password = binding.etPassword.text.toString().trim()

        val isNameValid = name.length >= 2
        val isEmailValid = isValidEmail(email)
        val isPasswordValid = password.length >= 8

        if (name.isNotEmpty() && !isNameValid) {
            binding.tilName.error = "Nombre muy corto"
        } else {
            binding.tilName.error = null
        }

        if (email.isNotEmpty() && !isEmailValid) {
            binding.tilEmail.error = "Correo inválido"
        } else {
            binding.tilEmail.error = null
        }

        if (password.isNotEmpty() && !isPasswordValid) {
            binding.tilPassword.error = "Mínimo 8 caracteres"
        } else {
            binding.tilPassword.error = null
        }

        binding.btnRegister.isEnabled =
            name.isNotEmpty() && email.isNotEmpty() && password.isNotEmpty() &&
                    isNameValid && isEmailValid && isPasswordValid
    }

    private fun isValidEmail(email: String): Boolean {
        return Patterns.EMAIL_ADDRESS.matcher(email).matches()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}