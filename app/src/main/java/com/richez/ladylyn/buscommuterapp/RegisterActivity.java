package com.richez.ladylyn.buscommuterapp;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import com.journeyapps.barcodescanner.BarcodeEncoder;
import com.rengwuxian.materialedittext.MaterialEditText;
import com.richez.ladylyn.buscommuterapp.Common.Common;
import com.richez.ladylyn.buscommuterapp.Model.User;

import java.io.ByteArrayOutputStream;
import java.util.HashMap;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;

public class RegisterActivity extends AppCompatActivity {
    CircleImageView ImgUserPhoto;
    private static final int PReqCode = 1;
    private static final int REQUESCODE = 1;
    Uri pickedImgUri;
    MaterialEditText userEmail, userPassword, userPAssword2, userName, userPhone;
    ProgressBar loadingProgress;
    Button regButton;
    FirebaseAuth auth;
    FirebaseDatabase db;
    DatabaseReference users;
    FirebaseUser firebaseUser;
    TextView signin;
    String email, password, password2, name, phone;
    byte[] data;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);
        auth = FirebaseAuth.getInstance();
        db = FirebaseDatabase.getInstance();
        users = db.getReference(Common.user_rider_tbl);

        //ini views

        userEmail = (MaterialEditText) findViewById(R.id.edtEmail);
        userPassword = (MaterialEditText) findViewById(R.id.edtPassword);
        userPAssword2 = (MaterialEditText) findViewById(R.id.edtPassword2);
        userName = (MaterialEditText) findViewById(R.id.edtName);
        userPhone = (MaterialEditText) findViewById(R.id.edtPhone);
        loadingProgress = findViewById(R.id.regProgressBar);
        loadingProgress.setVisibility(View.INVISIBLE);
        //info
        signin = (TextView) findViewById(R.id.textSignIn);
        signin.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                startActivity(new Intent(RegisterActivity.this, Login2Activity.class));
                finish();
                return false;
            }
        });


        regButton = (Button) findViewById(R.id.regBtn);
        regButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Register();
            }
        });
        ImgUserPhoto = (CircleImageView) findViewById(R.id.regUserPhoto);

        ImgUserPhoto.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {

                    checkAndRequestForPermission();


                } else {
                    openGallery();
                }


            }
        });
    }

    private void checkAndRequestForPermission() {
        if (ContextCompat.checkSelfPermission(RegisterActivity.this, android.Manifest.permission.READ_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(RegisterActivity.this, android.Manifest.permission.READ_EXTERNAL_STORAGE)) {

                Toast.makeText(RegisterActivity.this, "Please accept for required permission", Toast.LENGTH_SHORT).show();

            } else {
                ActivityCompat.requestPermissions(RegisterActivity.this,
                        new String[]{android.Manifest.permission.READ_EXTERNAL_STORAGE},
                        PReqCode);
            }

        } else {
            openGallery();

        }
    }

    private void openGallery() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, "Select Picture:"), REQUESCODE);
    }

    private void Register() {
        regButton.setVisibility(View.INVISIBLE);
        loadingProgress.setVisibility(View.VISIBLE);
        email = userEmail.getText().toString().trim();
        password = userPassword.getText().toString().trim();
        password2 = userPAssword2.getText().toString().trim();
        name = userName.getText().toString().trim();
        phone = userPhone.getText().toString().trim();
        if (name.isEmpty()) {
            Toast.makeText(getApplicationContext(), "Please enter username", Toast.LENGTH_LONG).show();
            regButton.setVisibility(View.VISIBLE);
            loadingProgress.setVisibility(View.INVISIBLE);
        } else if (email.isEmpty()) {
            Toast.makeText(getApplicationContext(), "Please enter email address", Toast.LENGTH_LONG).show();
            regButton.setVisibility(View.VISIBLE);
            loadingProgress.setVisibility(View.INVISIBLE);
        } else if (password.isEmpty()) {
            Toast.makeText(getApplicationContext(), "Please enter password", Toast.LENGTH_LONG).show();
            regButton.setVisibility(View.VISIBLE);
            loadingProgress.setVisibility(View.INVISIBLE);
        } else if (password.length() < 6) {
            Toast.makeText(getApplicationContext(), "Password too weak!", Toast.LENGTH_LONG).show();
            regButton.setVisibility(View.VISIBLE);
            loadingProgress.setVisibility(View.INVISIBLE);
        } else if (!password.equals(password2)) {
            Toast.makeText(getApplicationContext(), "Password don't match!", Toast.LENGTH_LONG).show();
            regButton.setVisibility(View.VISIBLE);
            loadingProgress.setVisibility(View.INVISIBLE);
        } else if (phone.length() < 11) {
            Toast.makeText(getApplicationContext(), "Phone number is incorrect", Toast.LENGTH_LONG).show();
            regButton.setVisibility(View.VISIBLE);
            loadingProgress.setVisibility(View.INVISIBLE);
        } else if (pickedImgUri == null) {
            Toast.makeText(getApplicationContext(), "Select your image profile", Toast.LENGTH_LONG).show();
            regButton.setVisibility(View.VISIBLE);
            loadingProgress.setVisibility(View.INVISIBLE);
        } else {

            CreateUserAccount(email, name, password, phone);
        }

    }

    private void isDisabled() {
        userEmail.setEnabled(false);
        userName.setEnabled(false);
        userPhone.setEnabled(false);
        userPassword.setEnabled(false);
        userPAssword2.setEnabled(false);
    }

    private void isEnabled() {
        userEmail.setEnabled(true);
        userName.setEnabled(true);
        userPhone.setEnabled(true);
        userPassword.setEnabled(true);
        userPAssword2.setEnabled(true);
    }

    private void CreateUserAccount(final String email, final String name, final String password, final String phone) {
        isDisabled();
        auth.createUserWithEmailAndPassword(email, password)
                .addOnSuccessListener(new OnSuccessListener<AuthResult>() {
                    @Override
                    public void onSuccess(AuthResult authResult) {
                        firebaseUser = auth.getCurrentUser();
                        User user = new User();
                        user.setEmail(email);
                        user.setPassword(password);
                        user.setName(name);
                        user.setPhone(phone);
                        user.setuserID(firebaseUser.getUid());
                        //user.setQrCodeUrl("");
                        //save to database
                        users.child(FirebaseAuth.getInstance().getCurrentUser().getUid())
                                .setValue(user).addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void aVoid) {
                                updateUserInfo(name, pickedImgUri, auth.getCurrentUser());

                            }
                        }).addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                Toast.makeText(getApplicationContext(), "Failed: " + e.getMessage(), Toast.LENGTH_LONG).show();
                                isEnabled();
                                regButton.setVisibility(View.VISIBLE);
                                loadingProgress.setVisibility(View.INVISIBLE);

                            }
                        });
                    }
                }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(getApplicationContext(), "Account creation failed: " + e.getMessage(), Toast.LENGTH_LONG).show();
                isEnabled();
                regButton.setVisibility(View.VISIBLE);
                loadingProgress.setVisibility(View.INVISIBLE);
            }
        });


    }

    private void sendVerification() {
        if (firebaseUser != null) {

            firebaseUser.sendEmailVerification().addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> task) {
                    if (task.isSuccessful()) {
                        Toast.makeText(getBaseContext(), "Register Successfully. Email has been sent for verification", Toast.LENGTH_SHORT).show();
                        auth.signOut();
                        startActivity(new Intent(RegisterActivity.this, Login2Activity.class));
                        finish();

                    } else {
                        Toast.makeText(getApplicationContext(), "Can't sent an email " + task.getException().getMessage(), Toast.LENGTH_LONG).show();
                        isEnabled();
                        regButton.setVisibility(View.VISIBLE);
                        loadingProgress.setVisibility(View.INVISIBLE);

                    }
                }
            });
        }
    }

    private void updateUserInfo(final String name, Uri pickedImgUri, final FirebaseUser currentUser) {
        // first we need to upload user photo to firebase storage and get url
        String imageName = FirebaseAuth.getInstance().getCurrentUser().getUid();
        StorageReference mStorage = FirebaseStorage.getInstance().getReference().child("users_photos");
        final StorageReference imageFilePath = mStorage.child("commuter_user_photos/" + imageName);
        imageFilePath.putFile(pickedImgUri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {

                // image uploaded succesfully
                // now we can get our image url

                imageFilePath.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                    @Override
                    public void onSuccess(Uri uri) {

                        // uri contain user image url


                        UserProfileChangeRequest profleUpdate = new UserProfileChangeRequest.Builder()
                                .setDisplayName(name)
                                .setPhotoUri(uri)
                                .build();


                        currentUser.updateProfile(profleUpdate)
                                .addOnCompleteListener(new OnCompleteListener<Void>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Void> task) {

                                        if (task.isSuccessful()) {
                                            // user info updated successfully
                                            sendVerification();
                                        } else {

                                            // account creation failed
                                            Toast.makeText(getApplicationContext(), "Account creation failed: " + task.getException().getMessage(), Toast.LENGTH_LONG).show();
                                            isEnabled();
                                            regButton.setVisibility(View.VISIBLE);
                                            loadingProgress.setVisibility(View.INVISIBLE);

                                        }

                                    }
                                });
                    }
                });

            }
        });

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == RESULT_OK && requestCode == REQUESCODE && data != null) {

            // the user has successfully picked an image
            // we need to save its reference to a Uri variable
            pickedImgUri = data.getData();
            ImgUserPhoto.setImageURI(pickedImgUri);
        }
    }
}
