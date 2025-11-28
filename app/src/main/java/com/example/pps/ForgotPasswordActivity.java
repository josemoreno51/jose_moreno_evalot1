
package com.example.pps;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

public class ForgotPasswordActivity extends AppCompatActivity {

    private TextInputEditText etEmail;
    private TextInputLayout tilEmail;

    private MaterialButton btnResetPassword, btnBackToLogin;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_forgot_password);

        initViews();
        setupClickListeners();
    }

    private void initViews() {
        etEmail = findViewById(R.id.etEmail);
        tilEmail = findViewById(R.id.tilEmail);
        btnResetPassword = findViewById(R.id.btnResetPassword);
        btnBackToLogin = findViewById(R.id.btnBackToLogin);
    }

    private void setupClickListeners() {
        btnResetPassword.setOnClickListener(v -> validateAndReset());
        btnBackToLogin.setOnClickListener(v -> finish());
    }

    private void validateAndReset() {
        String email = etEmail.getText().toString().trim();

        tilEmail.setError(null);

        if (email.isEmpty()) {
            tilEmail.setError("Ingrese su correo");
            return;
        }

        if (!email.contains("@")) {
            tilEmail.setError("Correo inválido");
            return;
        }

        Toast.makeText(this, "Enlace de recuperación enviado a " + email, Toast.LENGTH_SHORT).show();
    }
}