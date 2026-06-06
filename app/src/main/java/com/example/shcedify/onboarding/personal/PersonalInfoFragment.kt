package com.example.shcedify.onboarding.personal

import android.app.DatePickerDialog
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.example.shcedify.core.FragmentCommunicator
import com.example.shcedify.core.ResponseService
import com.example.shcedify.databinding.FragmentPersonalInfoBinding
import com.example.shcedify.onboarding.MainActivity
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.launch
import java.util.Calendar

class PersonalInfoFragment : Fragment() {

    private var _binding: FragmentPersonalInfoBinding? = null
    private val binding get() = _binding!!
    private val viewModel by viewModels<PersonalInfoViewModel>()
    private lateinit var communicator: FragmentCommunicator

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentPersonalInfoBinding.inflate(inflater, container, false)
        communicator = requireActivity() as FragmentCommunicator
        setupValidation()
        setupDatePicker()
        setupClickListeners()
        observeState()
        return binding.root
    }

    private fun setupValidation() {
        binding.btnContinue.isEnabled = false
        binding.etFirstName.addTextChangedListener { validateAndEnable() }
        binding.etLastName.addTextChangedListener { validateAndEnable() }
        binding.etNumCuenta.addTextChangedListener { validateAndEnable() }
        binding.etPhone.addTextChangedListener { validateAndEnable() }
        binding.etBirthdate.addTextChangedListener { validateAndEnable() }
    }

    private fun validateAndEnable() {
        val firstName  = binding.etFirstName.text.toString().trim()
        val lastName   = binding.etLastName.text.toString().trim()
        val numCuenta  = binding.etNumCuenta.text.toString().trim()
        val phone      = binding.etPhone.text.toString().trim()
        val birthDate  = binding.etBirthdate.text.toString().trim()

        binding.tilFirstName.error  = viewModel.validateFirstName(firstName)
        binding.tilLastName.error   = viewModel.validateLastName(lastName)
        binding.tilNumCuenta.error  = viewModel.validateNumCuenta(numCuenta)
        binding.tilPhone.error      = viewModel.validatePhone(phone)
        binding.tilBirthdate.error  = viewModel.validateBirthDate(birthDate)

        binding.btnContinue.isEnabled =
            viewModel.isFormValid(firstName, lastName, numCuenta, phone, birthDate)
    }

    private fun setupDatePicker() {
        val showPicker = {
            val cal = Calendar.getInstance()
            DatePickerDialog(
                requireContext(),
                { _, year, month, day ->
                    binding.etBirthdate.setText("%02d/%02d/%04d".format(day, month + 1, year))
                },
                cal.get(Calendar.YEAR) - 18,
                cal.get(Calendar.MONTH),
                cal.get(Calendar.DAY_OF_MONTH)
            ).apply { datePicker.maxDate = System.currentTimeMillis() }.show()
        }
        binding.etBirthdate.setOnClickListener { showPicker() }
        binding.tilBirthdate.setEndIconOnClickListener { showPicker() }
    }

    private fun setupClickListeners() {
        binding.btnContinue.setOnClickListener {
            val uid = FirebaseAuth.getInstance().currentUser?.uid
            if (uid == null) {
                Snackbar.make(binding.root, "Sesión inválida", Snackbar.LENGTH_LONG).show()
                return@setOnClickListener
            }
            viewModel.saveProfile(
                uid        = uid,
                firstName  = binding.etFirstName.text.toString().trim(),
                secondName = binding.etSecondName.text.toString().trim(),
                lastName   = binding.etLastName.text.toString().trim(),
                secondLastName = binding.etSecondLastName.text.toString().trim(),
                numCuenta  = binding.etNumCuenta.text.toString().trim(),
                phone      = binding.etPhone.text.toString().trim(),
                birthDate  = binding.etBirthdate.text.toString().trim()
            )
        }

        binding.btnSkip.setOnClickListener {
            FirebaseAuth.getInstance().currentUser?.delete()?.addOnCompleteListener {
                FirebaseAuth.getInstance().signOut()
                goToLogin()
            } ?: goToLogin()
        }
    }

    private fun goToLogin() {
        val intent = Intent(requireContext(), MainActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
    }

    private fun observeState() {
        viewLifecycleOwner.lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.saveState.collect { state ->
                    when (state) {
                        is ResponseService.Loading -> {
                            communicator.manageLoader(true)
                            binding.btnContinue.isEnabled = false
                        }
                        is ResponseService.Success -> {
                            communicator.manageLoader(false)
                            // Perfil guardado exitosamente → ir al home
                            val intent = Intent(requireContext(),
                                com.example.shcedify.home.HomeActivity::class.java)
                            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or
                                    Intent.FLAG_ACTIVITY_CLEAR_TASK
                            startActivity(intent)
                        }
                        is ResponseService.Error -> {
                            communicator.manageLoader(false)
                            binding.btnContinue.isEnabled = true
                            Snackbar.make(binding.root, state.error, Snackbar.LENGTH_LONG).show()
                        }
                        null -> Unit
                    }
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}