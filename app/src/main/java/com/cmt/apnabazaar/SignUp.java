package com.cmt.apnabazaar;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Typeface;
import android.hardware.fingerprint.FingerprintManager;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.annotation.RequiresApi;
import android.support.design.widget.BottomSheetBehavior;
import android.support.design.widget.BottomSheetDialog;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.util.TypedValue;
import android.view.View;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.database.FirebaseDatabase;

import java.util.Objects;
import java.util.regex.Pattern;

public class SignUp extends AppCompatActivity
{
    EditText editTextEmail, editTextPhone, password, editTextName;
    TextView loginText, signUpButton;
    private ProgressBar progressBar;
    private FirebaseAuth firebaseAuth;
    private FingerprintManager fingerprintManager;

    boolean validInputs = true;

    final Boolean[] fingerprintServiceAvailability = {false};

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup);

        firebaseAuth = FirebaseAuth.getInstance();

        signUpButton = findViewById(R.id.signUpButton);
        loginText = findViewById(R.id.loginText);
        editTextName = findViewById(R.id.usrusr);
        password = findViewById(R.id.pswrdd);
        editTextEmail = findViewById(R.id.mail);
        editTextPhone = findViewById(R.id.mobphone);
        Typeface custom_font = Typeface.createFromAsset(getAssets(), "fonts/LatoLight.ttf");
        Typeface custom_font1 = Typeface.createFromAsset(getAssets(), "fonts/LatoRegular.ttf");
        editTextPhone.setTypeface(custom_font);
        signUpButton.setTypeface(custom_font1);
        password.setTypeface(custom_font);
        loginText.setTypeface(custom_font);
        editTextName.setTypeface(custom_font);
        editTextEmail.setTypeface(custom_font);

        progressBar = findViewById(R.id.progressBar);
        progressBar.setVisibility(View.GONE);

        loginText.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                Intent it = new Intent(SignUp.this,Login.class);
                startActivity(it);
            }
        });

        signUpButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (checkValidity()) {
                    AlertDialog.Builder builder = new AlertDialog.Builder(SignUp.this);
                    builder.setTitle("Alert");
                    builder.setMessage("Do you want to register your fingerprint for login ?");

                    builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                        @TargetApi(Build.VERSION_CODES.M)
                        @Override
                        public void onClick(DialogInterface dialog, int which) {

                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {

                                fingerprintManager = (FingerprintManager) getSystemService(FINGERPRINT_SERVICE);
                                assert fingerprintManager != null;
                                if (fingerprintManager.isHardwareDetected()) {

                                    if (checkValidity()) {

                                        BottomSheetDialog bottomSheetDialog = new BottomSheetDialog(SignUp.this);
                                        @SuppressLint("InflateParams") View parentView = getLayoutInflater().inflate(R.layout.fingerprint_layout, null);
                                        bottomSheetDialog.setContentView(parentView);
                                        BottomSheetBehavior bottomSheetBehavior = BottomSheetBehavior.from( (View) parentView.getParent());
                                        bottomSheetBehavior.setPeekHeight((int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 300, getResources().getDisplayMetrics()));
                                        bottomSheetDialog.show();
                                    }

                                    fingerprintServiceAvailability[0] = true;
                                }
                                else {
                                    Toast.makeText(getApplicationContext(), "It seems that fingerprint service is not available in your phone!", Toast.LENGTH_LONG).show();
                                }
                            }

                            if (checkValidity()) {
                                registerUser();
                            }
                        }
                    });

                    builder.setNegativeButton("No", new DialogInterface.OnClickListener() {
                        @TargetApi(Build.VERSION_CODES.M)
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            registerUser();
                        }
                    });

                    AlertDialog alertDialog = builder.create();
                    alertDialog.show();
                }
            }
        });
    }

    private boolean checkValidity() {
        final String email = editTextEmail.getText().toString().trim();
        String pass = password.getText().toString().trim();
        final String mobile = editTextPhone.getText().toString().trim();
        final String name = editTextName.getText().toString().trim();

        String emailRegex = "^[a-zA-Z0-9_+&*-]+(?:\\."+
                "[a-zA-Z0-9_+&*-]+)*@" +
                "(?:[a-zA-Z0-9-]+\\.)+[a-z" +
                "A-Z]{2,7}$";

        Pattern pat = Pattern.compile(emailRegex);

        if (email.isEmpty()) {
            // username is empty
            editTextEmail.setError("email required");
            editTextEmail.requestFocus();
            validInputs = false;
            return false;
        }

        else if (!pat.matcher(email).matches()) {
            editTextEmail.setError("not a valid email");
            editTextEmail.requestFocus();
            validInputs = false;
            return false;
        }

        if (TextUtils.isEmpty(pass)) {
            // password is empty
            password.setError("password required");
            password.requestFocus();
            validInputs = false;
            return false;
        }

        if (TextUtils.isEmpty(mobile)) {
            // mobile is empty
            editTextPhone.setError("mobile number required");
            editTextPhone.requestFocus();
            validInputs = false;
            return false;
        }

        if (TextUtils.isEmpty(name)) {
            // full name is empty
            editTextPhone.setError("full name is required");
            editTextPhone.requestFocus();
            validInputs = false;
            return false;
        }

        if (pass.length() < 8) {
            password.setError("password should be atleast 8 characters long");
            password.requestFocus();
            validInputs = false;
            return false;
        }

        if (mobile.length() != 10) {
            editTextPhone.setError("not a valid phone number");
            editTextPhone.requestFocus();
            return false;
        }

        return validInputs;
    }

    @Override
    protected void onStart() {
        super.onStart();

        if (firebaseAuth.getCurrentUser() != null) {
            // handle already logged in user
            startActivity(new Intent(getApplicationContext(), MainActivity.class));
            finish();
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    private void registerUser() {
        final String email = editTextEmail.getText().toString().trim();
        String pass = password.getText().toString().trim();
        final String mobile = editTextPhone.getText().toString().trim();
        final String name = editTextName.getText().toString().trim();

        // if validations ok!

        progressBar.setVisibility(View.VISIBLE);
        firebaseAuth.createUserWithEmailAndPassword(email, pass)
                .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // we will store additional fields in firebase

                            User user = new User(name, email, mobile);

                            FirebaseDatabase.getInstance().getReference("Users")
                                    .child(Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getUid())
                                    .setValue(user).addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    progressBar.setVisibility(View.GONE);
                                    if (task.isSuccessful()) {
                                        Intent it = new Intent(SignUp.this, Login.class);
                                        startActivity(it);
                                        Toast.makeText(getApplicationContext(), "Registered Successfully!", Toast.LENGTH_LONG).show();
                                    }
                                    else {
                                        Toast.makeText(getApplicationContext(), "Registration failed!", Toast.LENGTH_LONG).show();
                                    }
                                }
                            });

                            userProfile();

                        }

                        else {
                            Toast.makeText(getApplicationContext(), Objects.requireNonNull(task.getException()).getMessage(), Toast.LENGTH_LONG).show();
                            progressBar.setVisibility(View.GONE);
                        }
                    }
                });
    }


    private void userProfile() {
        final FirebaseUser user = firebaseAuth.getCurrentUser();
        if (user != null) {
            UserProfileChangeRequest profileUpdates = new UserProfileChangeRequest.Builder()
                    .setDisplayName(editTextName.getText().toString().trim())
                    .build();

            user.updateProfile(profileUpdates)
                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if (task.isSuccessful()) {
                                Log.d(user.getUid(), "User Profile Updated");
                            }
                        }
                    });
        }
    }
}
