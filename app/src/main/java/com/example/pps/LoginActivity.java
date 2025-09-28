package com.example.ppsapp;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

public class LoginActivity extends AppCompatActivity {

    private TextInputEditText etUsername, etPassword;
    private TextInputLayout tilUsername, tilPassword;
    private MaterialButton btnLogin;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        initViews();
        setupClickListeners();
    }

    private void initViews() {
        etUsername = findViewById(R.id.etUsername);
        etPassword = findViewById(R.id.etPassword);
        tilUsername = findViewById(R.id.tilUsername);
        tilPassword = findViewById(R.id.tilPassword);
        btnLogin = findViewById(R.id.btnLogin);
    }

    private void setupClickListeners() {
        // Botón Login
        btnLogin.setOnClickListener(v -> validateAndLogin());

        // Crear cuenta
        findViewById(R.id.tvCreateAccount).setOnClickListener(v -> {
            Intent intent = new Intent(LoginActivity.this, CreateAccountActivity.class);
            startActivity(intent);
        });

        // Recuperar contraseña
        findViewById(R.id.tvForgotPassword).setOnClickListener(v -> {
            Intent intent = new Intent(LoginActivity.this, ForgotPasswordActivity.class);
            startActivity(intent);
        });
    }

    private void validateAndLogin() {
        String username = etUsername.getText().toString().trim();
        String password = etPassword.getText().toString().trim();

        // Reset errors
        tilUsername.setError(null);
        tilPassword.setError(null);

        boolean isValid = true;

        // Validar usuario
        if (username.isEmpty()) {
            tilUsername.setError("Ingrese su usuario");
            isValid = false;
        }

        // Validar contraseña
        if (password.isEmpty()) {
            tilPassword.setError("Ingrese su contraseña");
            isValid = false;
        }

        if (isValid) {
            // Simular login exitoso
            Toast.makeText(this, "¡Bienvenido a PPS!", Toast.LENGTH_SHORT).show();
            // Aquí iría la lógica real de autenticación
        }
    }
}