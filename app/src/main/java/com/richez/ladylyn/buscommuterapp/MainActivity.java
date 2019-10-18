package com.richez.ladylyn.buscommuterapp;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.RelativeLayout;
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
import com.richez.ladylyn.buscommuterapp.Common.Common;
import com.richez.ladylyn.buscommuterapp.Model.User;

import dmax.dialog.SpotsDialog;
import io.paperdb.Paper;

public class
MainActivity extends AppCompatActivity {
    private static int SPLASH_TIME_OUT = 2000;
    FirebaseAuth auth;
    FirebaseDatabase db;
    DatabaseReference users;
    FirebaseUser firebaseUser;
    RelativeLayout rootLayout;
    ConnectionDetector connectionDetector;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        //auto login
        Paper.init(this);
        auth = FirebaseAuth.getInstance();
        firebaseUser = auth.getCurrentUser();
        db = FirebaseDatabase.getInstance();
        users = db.getReference(Common.user_driver_tbl);
        rootLayout = (RelativeLayout) findViewById(R.id.rootLayout);
        String user = Paper.book().read(Common.user_field);
        String pwd = Paper.book().read(Common.pwd_field);
        //Check internet connection
        connectionDetector = new ConnectionDetector(this);
        if (connectionDetector.isConnected()) {
            if (user != null && pwd != null) {
                if (!TextUtils.isEmpty(user) && !TextUtils.isEmpty(pwd)) {
                    RememberMe(user, pwd);
                }
            } else {
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        Intent loginIntent = new Intent(MainActivity.this, IntroActivity.class);
                        startActivity(loginIntent);
                        finish();

                    }
                }, SPLASH_TIME_OUT);
            }
        } else {
            AlertDialog.Builder dialog = new AlertDialog.Builder(this);
            dialog.setTitle("No Internet Connection");
            dialog.setMessage("Make sure your WIFI or Cellular Data is turned on, then try again");
            dialog.setPositiveButton("Retry", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    Intent retryIntent = new Intent(MainActivity.this, MainActivity.class);
                    startActivity(retryIntent);
                    finish();
                }
            });
            dialog.setNegativeButton("Exit", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    finish();
                }
            });
            dialog.show();

        }
    }


    public void RememberMe(String user, String pwd) {
        //Login
        auth.signInWithEmailAndPassword(user, pwd)
                .addOnSuccessListener(new OnSuccessListener<AuthResult>() {
                    @Override
                    public void onSuccess(AuthResult authResult) {

                        getNameandEmail();
                    }
                }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {

                Toast.makeText(getBaseContext(), "Failed" + e.getMessage(), Toast.LENGTH_SHORT).show();
                finish();
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
                        startActivity(new Intent(MainActivity.this, Home.class));
                        finish();
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {
                    }
                });
    }
}
