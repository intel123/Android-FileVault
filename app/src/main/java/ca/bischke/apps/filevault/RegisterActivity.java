package ca.bischke.apps.filevault;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.Log;
import android.util.Patterns;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;

public class RegisterActivity extends AppCompatActivity
{
    private final String TAG = "FileVault";
    private FirebaseAuth firebaseAuth;
    private EditText editTextEmail;
    private EditText editTextPassword;
    private EditText editTextPassword2;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

        Permissions permissions = new Permissions(this);

        // Switch to PermissionsActivity if permissions are not granted
        if (!permissions.hasStoragePermission())
        {
            Intent intent = new Intent(this, PermissionsActivity.class);
            startActivity(intent);
            finish();
        }

        // Sets Activity Layout
        setContentView(R.layout.activity_register);

        // Adds the Toolbar to the Layout
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        // Displays Back Button in Toolbar
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        setTitle(R.string.register);

        editTextEmail = findViewById(R.id.edittext_email);
        editTextPassword = findViewById(R.id.edittext_password);
        editTextPassword2 = findViewById(R.id.edittext_confirm_password);

        editTextEmail.addTextChangedListener(new EditTextWatcher(editTextEmail));
        editTextPassword.addTextChangedListener(new EditTextWatcher(editTextPassword));
        editTextPassword2.addTextChangedListener(new EditTextWatcher(editTextPassword2));

        Intent intent = getIntent();
        Bundle extras = intent.getExtras();

        if (extras != null)
        {
            if (extras.containsKey("Email"))
            {
                editTextEmail.setText(extras.getString("Email"));
            }

            if (extras.containsKey("Password"))
            {
                editTextPassword.setText(extras.getString("Password"));
            }
        }

        firebaseAuth = FirebaseAuth.getInstance();
    }

    @Override
    public boolean onSupportNavigateUp()
    {
        // Handles Toolbar back button click event
        onBackPressed();
        return true;
    }

    public void buttonRegister(View view)
    {
        String email = editTextEmail.getText().toString();
        String password = editTextPassword.getText().toString();
        String password2 = editTextPassword2.getText().toString();

        if (TextUtils.isEmpty(email))
        {
            editTextEmail.setError("Email cannot be blank");
            return;
        }

        if (!isValidEmail(email))
        {
            editTextEmail.setError("Email address is not valid");
            return;
        }

        if (TextUtils.isEmpty(password))
        {
            editTextPassword.setError("Password cannot be blank");
            return;
        }

        if (password.length() < 6)
        {
            editTextPassword.setError("Password must be greater than 6 characters");
            return;
        }

        if (TextUtils.isEmpty(password2))
        {
            editTextPassword2.setError("Confirm password cannot be blank");
            return;
        }

        if (!password.equals(password2))
        {
            editTextPassword2.setError("Passwords do not match");
            return;
        }

        createAccount(email, password);
    }

    public void buttonLogin(View view)
    {
        String email = editTextEmail.getText().toString();
        String password = editTextPassword.getText().toString();

        Intent intent = new Intent(this, LoginActivity.class);
        intent.putExtra("Email", email);
        intent.putExtra("Password", password);

        startActivity(intent);
        finish();
    }

    private boolean isValidEmail(String email)
    {
        return Patterns.EMAIL_ADDRESS.matcher(email).matches();
    }

    private void createAccount(String email, String password)
    {
        firebaseAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(new OnCompleteListener<AuthResult>()
                {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task)
                    {
                        if (task.isSuccessful())
                        {
                            firebaseAuth.getCurrentUser().sendEmailVerification()
                                    .addOnCompleteListener(new OnCompleteListener<Void>()
                                    {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task)
                                        {
                                            if (task.isSuccessful())
                                            {
                                                Log.d(TAG, "Please verify your email");
                                            }
                                            else
                                            {
                                                Log.d(TAG, "Failed to send verification email");
                                            }
                                        }
                                    });

                            Toast toast = Toast.makeText(getApplicationContext(), "Account created. Please verify your email", Toast.LENGTH_SHORT);
                            toast.show();

                            Log.d(TAG, "Account created. Please verify your email");

                            Intent intent = new Intent(getApplicationContext(), LoginActivity.class);
                            startActivity(intent);
                            finish();
                        }
                        else
                        {
                            Toast toast = Toast.makeText(getApplicationContext(), "Registration failed", Toast.LENGTH_SHORT);
                            toast.show();

                            Log.d(TAG, "Registration failed");
                        }
                    }
                });
    }
}
