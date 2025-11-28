package com.example.pps;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.Task;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;

public class LoginActivity extends AppCompatActivity {

    private  TextInputEditText etUsername, etPassword;
    private TextInputLayout tilUsername, tilPassword;
    private MaterialButton btnLogin, btnGoogleLogin;
    private FirebaseAuth auth;
    private GoogleSignInClient googleSignInClient;
    private static final int RC_GOOGLE_SIGN_IN = 123;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        Log.d("DEBUG_LOGIN", "=== LOGIN ACTIVITY INICIADA ===");

        // Inicializar Firebase Auth
        auth = FirebaseAuth.getInstance();

        // Configurar Google Sign In
        setupGoogleSignIn();

        // Verificar si ya hay un usuario logueado
        checkCurrentUser();

        initViews();
        setupClickListeners();
    }

    private void setupGoogleSignIn() {
        Log.d("DEBUG_LOGIN", "🔧 Configurando Google Sign In...");

        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id)) // Necesitarás agregar este string
                .requestEmail()
                .build();

        googleSignInClient = GoogleSignIn.getClient(this, gso);

        Log.d("DEBUG_LOGIN", "✅ Google Sign In configurado");
    }

    private void checkCurrentUser() {
        FirebaseUser currentUser = auth.getCurrentUser();
        if (currentUser != null) {
            Log.d("DEBUG_LOGIN", "✅ Usuario ya logueado: " + currentUser.getEmail());
            goToHome();
        }
    }

    private void initViews() {
        etUsername = findViewById(R.id.etUsername);
        etPassword = findViewById(R.id.etPassword);
        tilUsername = findViewById(R.id.tilUsername);
        tilPassword = findViewById(R.id.tilPassword);
        btnLogin = findViewById(R.id.btnLogin);
        btnGoogleLogin = findViewById(R.id.btnGoogleLogin); // Necesitarás agregar este botón en tu XML

        Log.d("DEBUG_LOGIN", "✅ Vistas inicializadas");
    }

    private void setupClickListeners() {
        // Botón Login con email/password
        btnLogin.setOnClickListener(v -> validateAndLogin());

        // Botón Login con Google
        btnGoogleLogin.setOnClickListener(v -> signInWithGoogle());

        // Crear cuenta
        findViewById(R.id.btnCreateAccount).setOnClickListener(v ->
                startActivity(new Intent(LoginActivity.this, CreateAccountActivity.class))
        );

        // Recuperar contraseña
        findViewById(R.id.tvForgotPassword).setOnClickListener(v ->
                startActivity(new Intent(LoginActivity.this, ForgotPasswordActivity.class))
        );
    }

    private void signInWithGoogle() {
        Log.d("DEBUG_LOGIN", "🔵 Iniciando login con Google...");
        Intent signInIntent = googleSignInClient.getSignInIntent();
        startActivityForResult(signInIntent, RC_GOOGLE_SIGN_IN);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == RC_GOOGLE_SIGN_IN) {
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            try {
                // Google Sign In exitoso, autenticar con Firebase
                GoogleSignInAccount account = task.getResult(ApiException.class);
                Log.d("DEBUG_LOGIN", "✅ Google Sign In exitoso: " + account.getEmail());
                firebaseAuthWithGoogle(account.getIdToken());
            } catch (ApiException e) {
                Log.e("DEBUG_LOGIN", "❌ Google Sign In falló: " + e.getMessage());
                Toast.makeText(this, "Error con Google Sign In: " + e.getMessage(), Toast.LENGTH_LONG).show();
            }
        }
    }

    private void firebaseAuthWithGoogle(String idToken) {
        Log.d("DEBUG_LOGIN", "🔥 Autenticando con Firebase usando token de Google");

        AuthCredential credential = GoogleAuthProvider.getCredential(idToken, null);
        auth.signInWithCredential(credential)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        Log.d("DEBUG_LOGIN", "✅ Firebase auth con Google exitoso");
                        FirebaseUser user = auth.getCurrentUser();
                        Toast.makeText(this, "¡Bienvenido " + user.getDisplayName() + "!", Toast.LENGTH_SHORT).show();
                        goToHome();
                    } else {
                        Log.e("DEBUG_LOGIN", "❌ Firebase auth con Google falló: " + task.getException().getMessage());
                        Toast.makeText(this, "Error de autenticación: " + task.getException().getMessage(), Toast.LENGTH_LONG).show();
                    }
                });
    }

    private void validateAndLogin() {
        String email = etUsername.getText() != null ? etUsername.getText().toString().trim() : "";
        String password = etPassword.getText() != null ? etPassword.getText().toString().trim() : "";

        tilUsername.setError(null);
        tilPassword.setError(null);

        boolean isValid = true;

        if (email.isEmpty()) {
            tilUsername.setError("Ingrese su email");
            isValid = false;
        } else if (!isValidEmail(email)) {
            tilUsername.setError("Ingrese un email válido");
            isValid = false;
        }

        if (password.isEmpty()) {
            tilPassword.setError("Ingrese su contraseña");
            isValid = false;
        } else if (password.length() < 6) {
            tilPassword.setError("La contraseña debe tener al menos 6 caracteres");
            isValid = false;
        }

        if (isValid) {
            loginWithFirebase(email, password);
        }
    }

    private void loginWithFirebase(String email, String password) {
        Log.d("DEBUG_LOGIN", "🔄 Intentando login con: " + email);

        btnLogin.setEnabled(false);
        btnLogin.setText("Iniciando sesión...");

        auth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        Log.d("DEBUG_LOGIN", "✅ LOGIN EXITOSO");
                        FirebaseUser user = auth.getCurrentUser();
                        Log.d("DEBUG_LOGIN", "Usuario: " + user.getEmail() + ", UID: " + user.getUid());

                        Toast.makeText(LoginActivity.this, "¡Bienvenido a PPS!", Toast.LENGTH_SHORT).show();
                        goToHome();

                    } else {
                        Log.e("DEBUG_LOGIN", "❌ LOGIN FALLIDO: " + task.getException().getMessage());

                        btnLogin.setEnabled(true);
                        btnLogin.setText("Iniciar Sesión");

                        String errorMessage = "Error al iniciar sesión";
                        if (task.getException() != null) {
                            String error = task.getException().getMessage();
                            if (error.contains("invalid credential") || error.contains("wrong password")) {
                                errorMessage = "Email o contraseña incorrectos";
                            } else if (error.contains("user not found")) {
                                errorMessage = "Usuario no encontrado";
                            } else if (error.contains("network error")) {
                                errorMessage = "Error de conexión";
                            } else {
                                errorMessage = error;
                            }
                        }

                        Toast.makeText(LoginActivity.this, errorMessage, Toast.LENGTH_LONG).show();
                    }
                });
    }

    private void goToHome() {
        Log.d("DEBUG_LOGIN", "🚀 Redirigiendo a HomeActivity...");
        Intent intent = new Intent(LoginActivity.this, HomeActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
        finish();
    }

    private boolean isValidEmail(String email) {
        return android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches();
    }
}