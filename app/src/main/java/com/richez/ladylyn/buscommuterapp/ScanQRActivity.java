package com.richez.ladylyn.buscommuterapp;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Vibrator;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.zxing.Result;
import com.richez.ladylyn.buscommuterapp.Common.Common;
import com.richez.ladylyn.buscommuterapp.Model.Reminders;
import com.richez.ladylyn.buscommuterapp.Model.User;

import dmax.dialog.SpotsDialog;
import me.dm7.barcodescanner.zxing.ZXingScannerView;

public class ScanQRActivity extends AppCompatActivity implements ZXingScannerView.ResultHandler {
    private ZXingScannerView zXingScannerView;
    String driverId;
    AlertDialog alertDialog;
    DatabaseReference users, reference, sendReference, currentReference, drivers;
    String id;
    FirebaseDatabase db;
    FirebaseAuth auth;
    FirebaseUser user;
    Reminders reminders;
    String address, lat, name, lng, radius;
    private static final int REQUEST_CAMERA = 1;
    Vibrator vibrator;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        zXingScannerView = new ZXingScannerView(getApplicationContext());
        setContentView(zXingScannerView);
        int currentapiVersion = android.os.Build.VERSION.SDK_INT;
        if (currentapiVersion >= android.os.Build.VERSION_CODES.M) {
            if (checkPermission()) {
                Toast.makeText(getApplicationContext(), "Permission already granted", Toast.LENGTH_LONG).show();

            } else {
                requestPermission();
            }
        } else {
            startCameraQrCode();
        }
        vibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);

        db = FirebaseDatabase.getInstance();
        auth = FirebaseAuth.getInstance();
        user = auth.getCurrentUser();
        users = db.getReference(Common.user_rider_tbl).child(FirebaseAuth.getInstance().getCurrentUser().getUid()).child("Reminders");
        reference = db.getReference(Common.user_driver_tbl);
        currentReference = db.getReference(Common.user_driver_tbl).child(user.getUid());
        id = users.push().getKey();
        Intent intent = getIntent();
        if (intent != null) {

            address = intent.getStringExtra("address");
            lat = intent.getStringExtra("lat");
            lng = intent.getStringExtra("lng");
            radius = intent.getStringExtra("radius");
            name = intent.getStringExtra("name");
        }

    }

    private boolean checkPermission() {
        return (ContextCompat.checkSelfPermission(getApplicationContext(), android.Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED);
    }

    private void requestPermission() {
        ActivityCompat.requestPermissions(this, new String[]{android.Manifest.permission.CAMERA}, REQUEST_CAMERA);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case REQUEST_CAMERA:
                if (grantResults.length > 0) {

                    boolean cameraAccepted = grantResults[0] == PackageManager.PERMISSION_GRANTED;
                    if (cameraAccepted) {
                        Toast.makeText(getApplicationContext(), "Permission Granted, Now you can access camera", Toast.LENGTH_LONG).show();
                        startCameraQrCode();
                    } else {
                        Toast.makeText(getApplicationContext(), "Permission Denied, You cannot access camera", Toast.LENGTH_LONG).show();
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                            if (shouldShowRequestPermissionRationale(android.Manifest.permission.CAMERA)) {
                                showMessageOKCancel("You need to allow access to both the permissions",
                                        new DialogInterface.OnClickListener() {
                                            @Override
                                            public void onClick(DialogInterface dialog, int which) {
                                                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                                                    requestPermissions(new String[]{android.Manifest.permission.CAMERA},
                                                            REQUEST_CAMERA);
                                                }
                                            }
                                        });
                                return;
                            }
                        }
                    }
                }
                break;
        }
    }

    private void showMessageOKCancel(String message, DialogInterface.OnClickListener okListener) {
        new android.support.v7.app.AlertDialog.Builder(ScanQRActivity.this)
                .setMessage(message)
                .setPositiveButton("OK", okListener)
                .setNegativeButton("Cancel", null)
                .create()
                .show();

    }

    private void startCameraQrCode() {
        zXingScannerView.setResultHandler(this);
        zXingScannerView.startCamera();
    }


    @Override
    public void handleResult(Result result) {
        driverId = result.getText();
        vibrator.vibrate(50);
        final AlertDialog.Builder dialog = new AlertDialog.Builder(this);
        dialog.setTitle("Confirmation");
        dialog.setMessage("Send this reminder to the driver?");
        dialog.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                sendToDriver();

            }
        });
        dialog.setNegativeButton("CANCEL", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
                zXingScannerView.setResultHandler(ScanQRActivity.this);
                zXingScannerView.resumeCameraPreview(ScanQRActivity.this);

            }
        });

        alertDialog = dialog.create();
        alertDialog.show();


    }


    private void sendToDriver() {
        com.google.firebase.database.Query query = reference.orderByChild("userID").equalTo(driverId);
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    Reminders reminders1 = null;
                    for (DataSnapshot childDs : dataSnapshot.getChildren()) {
                        reminders1 = childDs.getValue(Reminders.class);

                        sendReference = db.getReference(Common.user_driver_tbl).child(driverId).child("Reminders").child(id);
                        reminders1.setId(id);
                        reminders1.setName(name);
                        reminders1.setPlaceaddress(address);
                        reminders1.setLat(lat);
                        reminders1.setLng(lng);
                        reminders1.setRadius(radius);
                        sendReference.setValue(reminders1).addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                if (task.isSuccessful()) {

                                    Toast.makeText(getApplicationContext(), "Reminder has been sent to driver.", Toast.LENGTH_SHORT).show();
                                    getNameandEmail();
                                    Addreminders();


                                } else {

                                    Toast.makeText(getApplicationContext(), "Failed: " + task.getException(), Toast.LENGTH_SHORT).show();
                                }
                            }
                        });


                    }
                } else {
                    Toast.makeText(getApplicationContext(), "This QR Code is not a Driver Code.", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    @Override
    protected void onPause() {
        super.onPause();
        zXingScannerView.stopCamera();

    }

    @Override
    protected void onResume() {
        super.onResume();
        int currentapiVersion = android.os.Build.VERSION.SDK_INT;
        if (currentapiVersion >= android.os.Build.VERSION_CODES.M) {
            if (checkPermission()) {
                if (zXingScannerView == null) {
                    zXingScannerView = new ZXingScannerView(this);
                    setContentView(zXingScannerView);
                }
                zXingScannerView.setResultHandler(this);
                zXingScannerView.startCamera();
            } else {
                requestPermission();
            }
        } else {
            startCameraQrCode();
        }
    }


    private void Addreminders() {
        users.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                getValues();


                users.child(id).setValue(reminders).addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {

                        startActivity(new Intent(ScanQRActivity.this, ListReminderActivity.class));
                        finish();
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(getBaseContext(), "Failed: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

    }

    private void getValues() {
        reminders = new Reminders();
        reminders.setId(id);
        reminders.setName(name);
        reminders.setPlaceaddress(address);
        reminders.setLat(lat);
        reminders.setLng(lng);
        reminders.setRadius(radius);


    }

    private void getNameandEmail() {
        FirebaseDatabase.getInstance().getReference(Common.user_rider_tbl)
                .child(FirebaseAuth.getInstance().getCurrentUser().getUid())
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        Common.currentUser = dataSnapshot.getValue(User.class);


                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });

    }
}
