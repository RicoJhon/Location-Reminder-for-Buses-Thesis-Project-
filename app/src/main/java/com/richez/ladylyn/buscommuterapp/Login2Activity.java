package com.richez.ladylyn.buscommuterapp;

import android.content.DialogInterface;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.rengwuxian.materialedittext.MaterialEditText;
import com.richez.ladylyn.buscommuterapp.Common.Common;
import com.richez.ladylyn.buscommuterapp.Model.User;

import dmax.dialog.SpotsDialog;
import io.paperdb.Paper;

public class Login2Activity extends AppCompatActivity {
    Button btnSignIn;
    FirebaseAuth auth;
    FirebaseDatabase db;
    DatabaseReference users;
    TextView txt_forgot_password, signup;
    FirebaseUser firebaseUser;
    ProgressBar loginProgress;
    MaterialEditText edtEmail;
    MaterialEditText edtPassword;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login2);
        Paper.init(this);

        auth = FirebaseAuth.getInstance();
        firebaseUser = auth.getCurrentUser();
        edtEmail = (MaterialEditText) findViewById(R.id.login_email);
        edtPassword = (MaterialEditText) findViewById(R.id.login_password);

        loginProgress = findViewById(R.id.login_progress);
        loginProgress.setVisibility(View.INVISIBLE);
        btnSignIn = (Button) findViewById(R.id.loginBtn);
        txt_forgot_password = (TextView) findViewById(R.id.forgotPassword);
        signup = (TextView) findViewById(R.id.textSignUp);

        signup.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                startActivity(new Intent(Login2Activity.this, RegisterActivity.class));
                finish();


                return false;
            }
        });
        txt_forgot_password.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                showForgotPassword();
                return false;
            }
        });


        btnSignIn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Check();
            }
        });
    }

    private void Check() {
        loginProgress.setVisibility(View.VISIBLE);
        btnSignIn.setVisibility(View.INVISIBLE);
        String email = edtEmail.getText().toString().trim();
        String password = edtPassword.getText().toString().trim();
        if (email.isEmpty()) {
            Toast.makeText(getApplicationContext(), "Please enter email address", Toast.LENGTH_LONG).show();
            btnSignIn.setVisibility(View.VISIBLE);
            loginProgress.setVisibility(View.INVISIBLE);
            return;
        } else if (password.isEmpty()) {
            Toast.makeText(getApplicationContext(), "Please enter password", Toast.LENGTH_LONG).show();
            btnSignIn.setVisibility(View.VISIBLE);
            loginProgress.setVisibility(View.INVISIBLE);
            return;
        } else {
            signIn(email, password);

        }

    }

    private void isDisabled() {
        edtEmail.setEnabled(false);
        edtPassword.setEnabled(false);

    }

    private void isEnabled() {
        edtEmail.setEnabled(true);
        edtPassword.setEnabled(true);
    }


    private void signIn(String email, String password) {

        //Login
        isDisabled();
        auth.signInWithEmailAndPassword(email, password)
                .addOnSuccessListener(new OnSuccessListener<AuthResult>() {
                    @Override
                    public void onSuccess(AuthResult authResult) {
                        loginProgress.setVisibility(View.INVISIBLE);
                        btnSignIn.setVisibility(View.VISIBLE);
                        FirebaseUser firebaseUser = auth.getCurrentUser();
                        if (firebaseUser.isEmailVerified()) {
                            Paper.book().write(Common.user_field, edtEmail.getText().toString());
                            Paper.book().write(Common.pwd_field, edtPassword.getText().toString());

                            getNameandEmail();
                        } else {
                            Toast.makeText(getBaseContext(), "Email is not yet verified", Toast.LENGTH_SHORT).show();
                            isEnabled();
                        }
                    }
                }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                isEnabled();
                btnSignIn.setVisibility(View.VISIBLE);
                loginProgress.setVisibility(View.INVISIBLE);
                Toast.makeText(getApplicationContext(), "Failed: " + e.getMessage(), Toast.LENGTH_LONG).show();


            }
        });


    }

    private void getNameandEmail() {
        FirebaseDatabase.getInstance().getReference(Common.user_driver_tbl)
                .child(FirebaseAuth.getInstance().getCurrentUser().getUid())
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        //TODO change map activity to navigation drawer
                        Common.currentUser = dataSnapshot.getValue(User.class);
                        startActivity(new Intent(Login2Activity.this, Home.class));
                        finish();

                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });
    }

    private void showForgotPassword() {
        final AlertDialog.Builder dialog = new AlertDialog.Builder(Login2Activity.this);
        dialog.setTitle("Forgot Password");
        dialog.setMessage("Please enter your email address.");

        LayoutInflater inflater = LayoutInflater.from(this);
        View layout_ForgotPassword = inflater.inflate(R.layout.layout_forgot_password, null);

        final MaterialEditText edtEmail = (MaterialEditText) layout_ForgotPassword.findViewById(R.id.edtForgotPassword);
        dialog.setView(layout_ForgotPassword);
        dialog.setPositiveButton("RESET", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(final DialogInterface dialogInterface, int which) {
                dialogInterface.dismiss();
                if (TextUtils.isEmpty(edtEmail.getText().toString())) {
                    Toast.makeText(getApplicationContext(), "Please enter email address", Toast.LENGTH_LONG).show();
                    return;

                }
                final SpotsDialog waitingDialog = new SpotsDialog(Login2Activity.this);
                waitingDialog.show();

                auth.sendPasswordResetEmail(edtEmail.getText().toString().trim())
                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void aVoid) {
                                dialogInterface.dismiss();
                                waitingDialog.dismiss();
                                Toast.makeText(getApplicationContext(), "Reset Password link has been sent", Toast.LENGTH_LONG).show();
                            }
                        }).
                        addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                dialogInterface.dismiss();
                                waitingDialog.dismiss();
                                Toast.makeText(getApplicationContext(), "Failed: " + e.getMessage(), Toast.LENGTH_LONG).show();

                            }
                        });


            }
        });
        dialog.setNegativeButton("CANCEL", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int which) {
                dialogInterface.dismiss();
            }
        });
        dialog.show();
    }
}
