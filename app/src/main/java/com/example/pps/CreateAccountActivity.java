<<<<<<< HEAD
package com.example.ppsapp;
=======
package com.example.pps;
>>>>>>> bc0cbcb (solucione errores)

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FieldValue;

import java.util.HashMap;
import java.util.Map;

import android.util.Log;

public class CreateAccountActivity extends AppCompatActivity {

    private TextInputEditText etFullName, etEmail, etPhone, etAddress, etPassword, etConfirmPassword;
    private TextInputLayout tilFullName, tilEmail, tilPhone, tilAddress, tilPassword, tilConfirmPassword;
    private MaterialButton btnCreateAccount, btnBackToLogin;

    private FirebaseAuth auth;
    private FirebaseFirestore database;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_account);

        // Inicializar Firebase
        auth = FirebaseAuth.getInstance();
        database = FirebaseFirestore.getInstance();

        initViews();
        setupClickListeners();
    }

    private void initViews() {
        etFullName = findViewById(R.id.etFullName);
        etEmail = findViewById(R.id.etEmail);
        etPhone = findViewById(R.id.etPhone);
        etAddress = findViewById(R.id.etAddress);
        etPassword = findViewById(R.id.etPassword);
        etConfirmPassword = findViewById(R.id.etConfirmPassword);

        tilFullName = findViewById(R.id.tilFullName);
        tilEmail = findViewById(R.id.tilEmail);
        tilPhone = findViewById(R.id.tilPhone);
        tilAddress = findViewById(R.id.tilAddress);
        tilPassword = findViewById(R.id.tilPassword);
        tilConfirmPassword = findViewById(R.id.tilConfirmPassword);

        btnCreateAccount = findViewById(R.id.btnCreateAccount);
        btnBackToLogin = findViewById(R.id.btnBackToLogin);
    }

    private void setupClickListeners() {
        if (btnCreateAccount != null) {
            btnCreateAccount.setOnClickListener(v -> validateAndCreateAccount());
        }
        if (btnBackToLogin != null) {
            btnBackToLogin.setOnClickListener(v -> {
                startActivity(new Intent(CreateAccountActivity.this, LoginActivity.class));
                finish();
            });
        }
    }

    private void validateAndCreateAccount() {
        String fullName = safeGetText(etFullName);
        String email = safeGetText(etEmail);
        String phone = safeGetText(etPhone);
        String address = safeGetText(etAddress);
        String password = safeGetText(etPassword);
        String confirmPassword = safeGetText(etConfirmPassword);

        clearErrors();

        boolean isValid = true;

        if (fullName.isEmpty()) {
            setError(tilFullName, "Ingrese su nombre completo");
            isValid = false;
        }

        if (email.isEmpty()) {
            setError(tilEmail, "Ingrese su email");
            isValid = false;
        } else if (!isValidEmail(email)) {
            setError(tilEmail, "Email inválido");
            isValid = false;
        }

        if (phone.isEmpty()) {
            setError(tilPhone, "Ingrese su teléfono");
            isValid = false;
        }

        if (address.isEmpty()) {
            setError(tilAddress, "Ingrese su dirección");
            isValid = false;
        }

        if (password.isEmpty()) {
            setError(tilPassword, "Ingrese una contraseña");
            isValid = false;
        } else if (password.length() < 6) {
            setError(tilPassword, "La contraseña debe tener al menos 6 caracteres");
            isValid = false;
        }

        if (confirmPassword.isEmpty()) {
            setError(tilConfirmPassword, "Confirme su contraseña");
            isValid = false;
        } else if (!password.equals(confirmPassword)) {
            setError(tilConfirmPassword, "Las contraseñas no coinciden");
            isValid = false;
        }

        if (isValid) {
            createAccountWithFirebase(fullName, email, phone, address, password);
        }
    }

    private void createAccountWithFirebase(String fullName, String email, String phone, String address, String password) {
        if (btnCreateAccount != null) {
            btnCreateAccount.setEnabled(false);
            btnCreateAccount.setText("Creando cuenta...");
        }

        if (auth == null) {
            Log.e("CreateAccount", "FirebaseAuth no inicializado");
            showAlert("Error", "Servicio de autenticación no disponible. Intente más tarde.");
            restoreButtonState();
            return;
        }

        auth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, task -> {
                    restoreButtonState();

                    if (task.isSuccessful()) {
                        Log.d("CreateAccount", "createUser:success");
                        showAlert("Cuenta creada", "¡Cuenta creada exitosamente!", (dialog, which) -> {
                            String uid = null;
                            if (task.getResult() != null && task.getResult().getUser() != null) {
                                uid = task.getResult().getUser().getUid();
                            }
                            if (uid == null && auth.getCurrentUser() != null) {
                                uid = auth.getCurrentUser().getUid();
                            }

                            if (uid != null) {
                                saveUserDataToFirestore(uid, fullName, email, phone, address);
                            } else {
                                Log.w("CreateAccount", "UID es null después de createUser; se continúa sin guardar en Firestore");
                                navigateToHomeDelayed();
                            }
                        });
                    } else {
                        Log.w("CreateAccount", "createUser:failure", task.getException());
                        String msg = task.getException() != null ? task.getException().getMessage() : "No se pudo crear cuenta";
                        showAlert("Error al crear cuenta", msg);
                    }
                });
    }

    private void saveUserDataToFirestore(String userId, String fullName, String email, String phone, String address) {
        if (database == null) {
            Log.e("CreateAccount", "Firestore no inicializado");
            navigateToHomeDelayed();
            return;
        }

        Map<String, Object> userData = new HashMap<>();
        userData.put("fullName", fullName);
        userData.put("email", email);
        userData.put("phone", phone);
        userData.put("address", address);
        userData.put("createdAt", FieldValue.serverTimestamp());

        Log.d("CreateAccount", "Guardando usuario en Firestore: " + userId);

        database.collection("users")
                .document(userId)
                .set(userData)
                .addOnSuccessListener(aVoid -> {
                    Log.d("CreateAccount", "Datos guardados en Firestore con éxito: " + userId);
                    Toast.makeText(CreateAccountActivity.this, "Datos guardados correctamente", Toast.LENGTH_SHORT).show();
                    navigateToHomeDelayed();
                })
                .addOnFailureListener(e -> {
                    Log.e("CreateAccount", "Error guardando datos en Firestore", e);
                    showAlert("Atención", "Cuenta creada, pero no se pudieron guardar los datos: " + e.getMessage(), (d, w) -> navigateToHomeDelayed());
                });
    }

    private void restoreButtonState() {
        if (btnCreateAccount != null) {
            btnCreateAccount.setEnabled(true);
            btnCreateAccount.setText("Crear Cuenta");
        }
    }

    private void navigateToHomeDelayed() {
        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            Intent intent = new Intent(CreateAccountActivity.this, HomeActivity.class);
            startActivity(intent);
            finish();
        }, 400);
    }

    private void showAlert(String title, String message) {
        showAlert(title, message, null);
    }

    private void showAlert(String title, String message, DialogInterface.OnClickListener okListener) {
        new AlertDialog.Builder(this)
                .setTitle(title)
                .setMessage(message)
                .setCancelable(false)
                .setPositiveButton("OK", okListener != null ? okListener : (dialog, which) -> dialog.dismiss())
                .show();
    }

    private String safeGetText(TextInputEditText et) {
        return et != null && et.getText() != null ? et.getText().toString().trim() : "";
    }

    private boolean isValidEmail(String email) {
        return android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches();
    }

    private void setError(TextInputLayout til, String msg) {
        if (til != null) til.setError(msg);
    }

    private void clearErrors() {
        if (tilFullName != null) tilFullName.setError(null);
        if (tilEmail != null) tilEmail.setError(null);
        if (tilPhone != null) tilPhone.setError(null);
        if (tilAddress != null) tilAddress.setError(null);
        if (tilPassword != null) tilPassword.setError(null);
        if (tilConfirmPassword != null) tilConfirmPassword.setError(null);
    }
}