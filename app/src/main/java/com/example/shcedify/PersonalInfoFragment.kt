package com.example.shcedify

import android.app.DatePickerDialog
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.shcedify.databinding.FragmentPersonalInfoBinding
import java.util.Calendar

class PersonalInfoFragment : Fragment() {

    private var _binding: FragmentPersonalInfoBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentPersonalInfoBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Botón deshabilitado al inicio
        binding.btnContinue.isEnabled = false

        setupValidation()
        setupDatePicker()

        binding.btnContinue.setOnClickListener {
            findNavController().navigate(R.id.action_personal_info_to_home)
        }

        binding.btnSkip.setOnClickListener {
            // TODO: navegar a la app principal sin guardar
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

        binding.etFirstName.addTextChangedListener(watcher)
        binding.etLastName.addTextChangedListener(watcher)
        binding.etUsername.addTextChangedListener(watcher)
        binding.etPhone.addTextChangedListener(watcher)
        binding.etBirthdate.addTextChangedListener(watcher)
    }

    private fun setupDatePicker() {
        binding.etBirthdate.setOnClickListener { showDatePicker() }
        binding.tilBirthdate.setEndIconOnClickListener { showDatePicker() }
    }

    private fun showDatePicker() {
        val calendar = Calendar.getInstance()
        DatePickerDialog(
            requireContext(),
            { _, year, month, day ->
                binding.etBirthdate.setText(
                    String.format("%02d/%02d/%04d", day, month + 1, year)
                )
            },
            calendar.get(Calendar.YEAR) - 18,
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)
        ).show()
    }

    private fun validateFields() {
        val firstName = binding.etFirstName.text.toString().trim()
        val lastName = binding.etLastName.text.toString().trim()
        val username = binding.etUsername.text.toString().trim()
        val phone = binding.etPhone.text.toString().trim()
        val birthdate = binding.etBirthdate.text.toString().trim()

        if (firstName.isNotEmpty() && firstName.length < 2) {
            binding.tilFirstName.error = "Nombre muy corto"
        } else {
            binding.tilFirstName.error = null
        }

        if (lastName.isNotEmpty() && lastName.length < 2) {
            binding.tilLastName.error = "Apellido muy corto"
        } else {
            binding.tilLastName.error = null
        }

        if (username.isNotEmpty() && username.length < 3) {
            binding.tilUsername.error = "Mínimo 3 caracteres"
        } else {
            binding.tilUsername.error = null
        }

        if (phone.isNotEmpty() && phone.length < 10) {
            binding.tilPhone.error = "Teléfono inválido"
        } else {
            binding.tilPhone.error = null
        }

        binding.btnContinue.isEnabled =
            firstName.length >= 2 &&
                    lastName.length >= 2 &&
                    username.length >= 3 &&
                    phone.length >= 10 &&
                    birthdate.isNotEmpty()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}