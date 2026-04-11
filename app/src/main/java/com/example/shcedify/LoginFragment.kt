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
import com.example.shcedify.databinding.FragmentLoginBinding

class LoginFragment : Fragment() {

    private var _binding: FragmentLoginBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentLoginBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Botón deshabilitado al inicio
        binding.btnLogin.isEnabled = false

        setupValidation()

        binding.btnLogin.setOnClickListener {
            // TODO: validar credenciales con backend
            // Por ahora navega directo (simula login exitoso)
        }

        binding.btnRegister.setOnClickListener {
            findNavController().navigate(R.id.action_login_to_register)
        }

        binding.btnForgot.setOnClickListener {
            findNavController().navigate(R.id.action_login_to_forgot)
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

        binding.etEmail.addTextChangedListener(watcher)
        binding.etPassword.addTextChangedListener(watcher)
    }

    private fun validateFields() {
        val email = binding.etEmail.text.toString().trim()
        val password = binding.etPassword.text.toString().trim()

        val isEmailValid = isValidEmail(email)
        val isPasswordValid = password.length >= 8

        // Mostrar errores solo si el campo tiene texto
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

        // Habilitar botón solo si todo es válido
        binding.btnLogin.isEnabled =
            email.isNotEmpty() && password.isNotEmpty() && isEmailValid && isPasswordValid
    }

    private fun isValidEmail(email: String): Boolean {
        return Patterns.EMAIL_ADDRESS.matcher(email).matches()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}