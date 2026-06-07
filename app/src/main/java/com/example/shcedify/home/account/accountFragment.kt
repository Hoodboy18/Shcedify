package com.example.shcedify.home.account

import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.example.shcedify.databinding.FragmentAccountBinding
import com.example.shcedify.onboarding.MainActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class accountFragment : Fragment() {

    private var _binding: FragmentAccountBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAccountBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        loadUserData()
        binding.btnLogout.setOnClickListener { logout() }
    }

    private fun loadUserData() {
        val uid = FirebaseAuth.getInstance().currentUser?.uid ?: return
        binding.tvEmail.text = FirebaseAuth.getInstance().currentUser?.email ?: "—"

        FirebaseFirestore.getInstance()
            .collection("users")
            .document(uid)
            .get()
            .addOnSuccessListener { doc ->
                if (doc.exists()) {
                    val firstName      = doc.getString("firstName")      ?: ""
                    val lastName       = doc.getString("lastName")        ?: ""
                    val numCuenta      = doc.getString("numCuenta")       ?: "—"
                    val phone          = doc.getString("phone")           ?: "—"
                    val birthDate      = doc.getString("birthDate")       ?: "—"

                    val fullName = "$firstName $lastName".trim()
                    binding.tvNombre.text    = fullName.ifEmpty { "—" }
                    binding.tvUsername.text  = "Facultad de Contaduría y Administración"
                    binding.tvNumCuenta.text = numCuenta
                    binding.tvPhone.text     = phone
                    binding.tvBirthdate.text = birthDate

                    // Iniciales para el avatar
                    val initials = buildString {
                        if (firstName.isNotEmpty()) append(firstName.first().uppercaseChar())
                        if (lastName.isNotEmpty())  append(lastName.first().uppercaseChar())
                    }
                    binding.tvInitials.text = initials.ifEmpty { "?" }
                }
            }
    }

    private fun logout() {
        FirebaseAuth.getInstance().signOut()
        val intent = Intent(requireContext(), MainActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}