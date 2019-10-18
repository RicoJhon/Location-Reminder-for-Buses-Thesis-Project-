package com.richez.ladylyn.buscommuterapp;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
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
import uk.co.chrisjenx.calligraphy.CalligraphyConfig;

public class LoginActivity extends AppCompatActivity {
    Button btnSignIn, btnRegister;
    RelativeLayout rootLayout;
    FirebaseAuth auth;
    FirebaseDatabase db;
    DatabaseReference users;
    TextView txt_forgot_password;
    FirebaseUser firebaseUser;

    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(newBase);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        CalligraphyConfig.initDefault(new CalligraphyConfig.Builder()
                .setDefaultFontPath("fonts/Roboto-Black.ttf")
                .setFontAttrId(R.attr.fontPath)
                .build());
        setContentView(R.layout.activity_login);
        //auto login
        Paper.init(this);
        auth = FirebaseAuth.getInstance();
        db = FirebaseDatabase.getInstance();
        firebaseUser = auth.getCurrentUser();
        users = db.getReference(Common.user_rider_tbl);

        btnRegister = (Button) findViewById(R.id.btnRegister);
        btnSignIn = (Button) findViewById(R.id.btnSignIn);
        rootLayout = (RelativeLayout) findViewById(R.id.rootLayout);
        txt_forgot_password = (TextView) findViewById(R.id.txt_forgotPassword);


        txt_forgot_password.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                showForgotPassword();
                return false;
            }
        });

        //Event
        btnRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showRegisterDialog();

            }
        });
        btnSignIn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showLoginDialog();
            }
        });

        String user = Paper.book().read(Common.user_field);
        String pwd = Paper.book().read(Common.pwd_field);
        if (user != null && pwd != null) {
            if (!TextUtils.isEmpty(user) && !TextUtils.isEmpty(pwd)) {
                RememberMe(user, pwd);
            }
        }

    }

    private void RememberMe(String user, String pwd) {
        final SpotsDialog waitingDialog = new SpotsDialog(LoginActivity.this);
        waitingDialog.setCanceledOnTouchOutside(false);
        waitingDialog.setCancelable(false);
        waitingDialog.show();


        //Login
        auth.signInWithEmailAndPassword(user, pwd)
                .addOnSuccessListener(new OnSuccessListener<AuthResult>() {
                    @Override
                    public void onSuccess(AuthResult authResult) {
                        waitingDialog.dismiss();
                        getNameandEmail();


                    }
                }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                waitingDialog.dismiss();
                Snackbar.make(rootLayout, "Failed" + e.getMessage(), Snackbar.LENGTH_SHORT)
                        .show();

            }
        });
    }

    private void getNameandEmail() {
        FirebaseDatabase.getInstance().getReference(Common.user_rider_tbl)
                .child(FirebaseAuth.getInstance().getCurrentUser().getUid())
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        Common.currentUser = dataSnapshot.getValue(User.class);
                        startActivity(new Intent(LoginActivity.this, Home.class));
                        finish();


                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });
    }

    private void showForgotPassword() {
        final AlertDialog.Builder dialog = new AlertDialog.Builder(LoginActivity.this);
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
                    Snackbar.make(rootLayout, "Please enter email address", Snackbar.LENGTH_SHORT)
                            .show();
                    return;

                }
                final SpotsDialog waitingDialog = new SpotsDialog(LoginActivity.this);
                waitingDialog.show();

                auth.sendPasswordResetEmail(edtEmail.getText().toString().trim())
                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                dialogInterface.dismiss();
                                waitingDialog.dismiss();

                                Snackbar.make(rootLayout, "Reset Password link has been sent", Snackbar.LENGTH_SHORT)
                                        .show();
                            }
                        }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        dialogInterface.dismiss();
                        waitingDialog.dismiss();

                        Snackbar.make(rootLayout, "Failed" + e.getMessage(), Snackbar.LENGTH_SHORT)
                                .show();

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

    private void showLoginDialog() {
        final AlertDialog.Builder dialog = new AlertDialog.Builder(this);
        dialog.setTitle("Sign In");
        dialog.setMessage("Please use email to sign in.");

        LayoutInflater inflater = LayoutInflater.from(this);
        View login_layout = inflater.inflate(R.layout.layout_login, null);

        final MaterialEditText edtEmail = login_layout.findViewById(R.id.edtEmail);
        final MaterialEditText edtPassword = login_layout.findViewById(R.id.edtPassword);


        dialog.setView(login_layout);

        //set button
        dialog.setPositiveButton("SIGN IN", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int which) {
                dialogInterface.dismiss();


                if (TextUtils.isEmpty(edtEmail.getText().toString())) {
                    Snackbar.make(rootLayout, "Please enter email address", Snackbar.LENGTH_SHORT)
                            .show();
                    return;

                }

                if (TextUtils.isEmpty(edtPassword.getText().toString())) {
                    Snackbar.make(rootLayout, "Please enter password", Snackbar.LENGTH_SHORT)
                            .show();
                    return;

                }
                final SpotsDialog waitingDialog = new SpotsDialog(LoginActivity.this);
                waitingDialog.show();


                //Login
                auth.signInWithEmailAndPassword(edtEmail.getText().toString(), edtPassword.getText().toString())
                        .addOnSuccessListener(new OnSuccessListener<AuthResult>() {
                            @Override
                            public void onSuccess(AuthResult authResult) {
                                waitingDialog.dismiss();
                                FirebaseUser firebaseUser = auth.getCurrentUser();
                                if (firebaseUser.isEmailVerified()) {
                                    Paper.book().write(Common.user_field, edtEmail.getText().toString());
                                    Paper.book().write(Common.pwd_field, edtPassword.getText().toString());
                                    getNameandEmail();

                                } else {
                                    Toast.makeText(getBaseContext(), "Email is not yet verified", Toast.LENGTH_SHORT).show();
                                }
                            }
                        }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        waitingDialog.dismiss();

                        Snackbar.make(rootLayout, "Failed " + e.getMessage(), Snackbar.LENGTH_SHORT)
                                .show();

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

    private void showRegisterDialog() {
        final AlertDialog.Builder dialog = new AlertDialog.Builder(this);
        dialog.setTitle("Register");
        dialog.setMessage("Please use email to register.");

        LayoutInflater inflater = LayoutInflater.from(this);
        View register_layout = inflater.inflate(R.layout.layout_register, null);

        final MaterialEditText edtEmail = register_layout.findViewById(R.id.edtEmail);
        final MaterialEditText edtPassword = register_layout.findViewById(R.id.edtPassword);
        final MaterialEditText edtName = register_layout.findViewById(R.id.edtName);
        final MaterialEditText edtPhone = register_layout.findViewById(R.id.edtPhone);

        dialog.setView(register_layout);

        //set button
        dialog.setPositiveButton("REGISTER", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int which) {
                dialogInterface.dismiss();

                if (TextUtils.isEmpty(edtEmail.getText().toString())) {
                    Snackbar.make(rootLayout, "Please enter email address", Snackbar.LENGTH_SHORT)
                            .show();
                    return;

                }
                if (TextUtils.isEmpty(edtPhone.getText().toString())) {
                    Snackbar.make(rootLayout, "Please enter phone number", Snackbar.LENGTH_SHORT)
                            .show();
                    return;

                }
                if (TextUtils.isEmpty(edtPassword.getText().toString())) {
                    Snackbar.make(rootLayout, "Please enter password", Snackbar.LENGTH_SHORT)
                            .show();
                    return;

                }
                if (edtEmail.getText().toString().length() < 6) {
                    Snackbar.make(rootLayout, "Password is too short!", Snackbar.LENGTH_SHORT)
                            .show();
                    return;

                }

                //REGISTER NEW USER
                auth.createUserWithEmailAndPassword(edtEmail.getText().toString(), edtPassword.getText().toString())
                        .addOnSuccessListener(new OnSuccessListener<AuthResult>() {
                            @Override
                            public void onSuccess(AuthResult authResult) {
                                firebaseUser = auth.getCurrentUser();

                                User user = new User();
                                user.setEmail(edtEmail.getText().toString());
                                user.setPassword(edtPassword.getText().toString());
                                user.setName(edtName.getText().toString());
                                user.setPhone(edtPhone.getText().toString());
                                user.setuserID(firebaseUser.getUid());

                                //use email
                                users.child(FirebaseAuth.getInstance().getCurrentUser().getUid())
                                        .setValue(user).addOnSuccessListener(new OnSuccessListener<Void>() {
                                    @Override
                                    public void onSuccess(Void aVoid) {
                                        sendVerification();

                                        Snackbar.make(rootLayout, "Register Successfully", Snackbar.LENGTH_SHORT)
                                                .show();
                                    }
                                }).addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {

                                        Snackbar.make(rootLayout, "Failed. " + e.getMessage(), Snackbar.LENGTH_SHORT)
                                                .show();
                                    }
                                })
                                        .addOnFailureListener(new OnFailureListener() {
                                            @Override
                                            public void onFailure(@NonNull Exception e) {

                                                Snackbar.make(rootLayout, "Failed. " + e.getMessage(), Snackbar.LENGTH_SHORT)
                                                        .show();
                                            }
                                        });


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

    private void sendVerification() {
        if (firebaseUser != null) {

            firebaseUser.sendEmailVerification().addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> task) {
                    if (task.isSuccessful()) {
                        Toast.makeText(getBaseContext(), "Email sent for verification", Toast.LENGTH_SHORT).show();
                        auth.signOut();

                    } else {
                        Toast.makeText(getBaseContext(), "Cannot sent an email", Toast.LENGTH_SHORT).show();

                    }
                }
            });
        }
    }

}

