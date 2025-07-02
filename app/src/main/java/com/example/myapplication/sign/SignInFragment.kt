package com.example.myapplication.sign
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.example.myapplication.R

class SignInFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_sign_in, container, false)

        val emailEditText = view.findViewById<EditText>(R.id.emailEditText)
        val passwordEditText = view.findViewById<EditText>(R.id.passwordEditText)
        val signInButton = view.findViewById<Button>(R.id.signInButton)
        val signUpTextView = view.findViewById<TextView>(R.id.signUpTextView)

        signInButton.setOnClickListener {
            val email = emailEditText.text.toString()
            val password = passwordEditText.text.toString()

            // Ajoute ici la logique pour vérifier les identifiants
            Toast.makeText(requireContext(), "Connexion réussie !", Toast.LENGTH_SHORT).show()
        }

        signUpTextView.setOnClickListener {
            // Naviguer vers l'écran d'inscription
           // findNavController().navigate(R.id.nav_signup)
        }

        return view
    }
}