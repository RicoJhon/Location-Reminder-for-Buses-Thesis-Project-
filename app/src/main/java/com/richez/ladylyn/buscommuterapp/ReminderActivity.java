package com.richez.ladylyn.buscommuterapp;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Location;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.geofire.GeoFire;
import com.firebase.geofire.GeoLocation;
import com.firebase.geofire.GeoQuery;
import com.firebase.geofire.GeoQueryEventListener;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesNotAvailableException;
import com.google.android.gms.common.GooglePlayServicesRepairableException;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.drive.query.Query;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.ui.PlaceAutocomplete;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.Circle;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
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
import com.google.zxing.Result;
import com.rengwuxian.materialedittext.MaterialEditText;
import com.richez.ladylyn.buscommuterapp.Adapter.ListItemAdapter;
import com.richez.ladylyn.buscommuterapp.Common.Common;
import com.richez.ladylyn.buscommuterapp.Model.Reminders;
import com.richez.ladylyn.buscommuterapp.Model.User;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import dmax.dialog.SpotsDialog;
import me.dm7.barcodescanner.zxing.ZXingScannerView;


public class ReminderActivity extends AppCompatActivity implements OnMapReadyCallback, GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener,
        LocationListener, ZXingScannerView.ResultHandler {
    private GoogleMap mMap;
    SupportMapFragment mapFragment;
    private static final int MY_PERMISSION_REQUEST_CODE = 1234;
    private static final int PLAY_SERVICE_RES_REQUEST = 7001;

    private static int UPDATE_INTERVAL = 5000;
    private static int FATEST_INTERVAL = 3000;
    private static int DISPLACEMENT = 10;
    View view;
    FirebaseDatabase db;
    FirebaseAuth auth;
    FirebaseUser user;
    DatabaseReference users, reference, sendReference, currentReference, drivers;
    String reminderID;
    EditText remindername;
    SpotsDialog dialog;
    Button save;
    Reminders reminders;
    String address, lat, lng;
    String id;
    TextView destination;
    EditText radius;
    AlertDialog alertDialog;
    private int counter = 0;
    Marker mReminder, mNewReminder;
    Circle circle;
    public boolean isUpdate = false;
    String titleDestination, SetRadius;
    String currentDriver;
    //Find Driver
    private Location mLastLocation;
    GeoFire geoFire;
    private GoogleApiClient mGoogleApiClient;
    private LocationRequest mLocationRequest;
    boolean isDriverFound = false;
    String driverID;
    int radiusDistance = 1;
    SeekBar seekBar;
    int min = 0, max = 1000, current = 500;
    double finalRadius;
    Button scanQr;
    private ZXingScannerView zXingScannerView;
    String driverId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reminder);
        mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.mapReminder);
        mapFragment.getMapAsync(this);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbarReminder);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("Add Reminder");
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
        }
        Intent intent = getIntent();
        if (intent != null) {

            address = intent.getStringExtra("address");
            lat = intent.getStringExtra("lat");
            lng = intent.getStringExtra("lng");
        }

        view = this.getWindow().getDecorView();
        view.setBackgroundResource(R.color.colorReminder);
        remindername = (EditText) findViewById(R.id.edtNameReminder);
        destination = (TextView) findViewById(R.id.txt_destination);
        destination.setText(address);
        radius = (EditText) findViewById(R.id.txt_radius);

        radius.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {


            }
        });

        scanQr = (Button) findViewById(R.id.txt_Scan);
        scanQr.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (TextUtils.isEmpty(remindername.getText().toString())) {
                    remindername.setText("None");
                }
                Intent reminderIntent = new Intent(ReminderActivity.this, ScanQRActivity.class);
                reminderIntent.putExtra("address", address);
                reminderIntent.putExtra("lat", lat);
                reminderIntent.putExtra("lng", lng);
                reminderIntent.putExtra("radius", radius.getText().toString());
                reminderIntent.putExtra("name", remindername.getText().toString().trim());
                startActivity(reminderIntent);

            }
        });

        dialog = new SpotsDialog(this);

        db = FirebaseDatabase.getInstance();
        auth = FirebaseAuth.getInstance();
        user = auth.getCurrentUser();
        users = db.getReference(Common.user_rider_tbl).child(FirebaseAuth.getInstance().getCurrentUser().getUid()).child("Reminders");
        reference = db.getReference(Common.user_driver_tbl);
        currentReference = db.getReference(Common.user_driver_tbl).child(user.getUid());
        id = users.push().getKey();
        destination.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                PlaceMODEFULLSCREEN();
            }
        });
        currentReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                Common.currentUser = dataSnapshot.getValue(User.class);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
        drivers = FirebaseDatabase.getInstance().getReference(Common.rider_tbl);
        geoFire = new GeoFire(drivers);
        seekBar = (SeekBar) findViewById(R.id.seek_bar);

        seekBar.setMax(max);
        seekBar.setProgress(current - min);
        radius.setText("" + current);
        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekbar, int progress, boolean fromUser) {
                current = progress + min;
                if (progress < 100) {
                    seekbar.setProgress(100);
                }
                finalRadius = Double.parseDouble(radius.getText().toString());
                radius.setText("" + current);


            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                double latitude = Double.parseDouble(lat);
                double longitude = Double.parseDouble(lng);
                if (circle.isVisible()) {
                    circle.remove();
                }


                LatLng geofence_area = new LatLng(latitude, longitude);
                circle = mMap.addCircle(new CircleOptions()
                        .center(geofence_area)
                        .radius(finalRadius)
                        .strokeColor(Color.BLUE)
                        .fillColor(0x220000FF)
                        .strokeWidth(5.0f));
                mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(latitude, longitude), 15.0f));

            }
        });


        setUpLocation();


    }

    private void setUpLocation() {
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(this, new String[]{
                    android.Manifest.permission.ACCESS_COARSE_LOCATION,

            }, MY_PERMISSION_REQUEST_CODE);
        } else {

            if (checkPlayServices()) {
                buildingApiClient();
                createLocationRequest();

                displayLocation();

            }

        }
    }

    private void createLocationRequest() {
        mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(UPDATE_INTERVAL);
        mLocationRequest.setFastestInterval(FATEST_INTERVAL);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        mLocationRequest.setSmallestDisplacement(DISPLACEMENT);
    }

    private void buildingApiClient() {

        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();
        mGoogleApiClient.connect();
    }


    private boolean checkPlayServices() {
        int resultCode = GooglePlayServicesUtil.isGooglePlayServicesAvailable(this);
        if (resultCode != ConnectionResult.SUCCESS) {
            if (GooglePlayServicesUtil.isUserRecoverableError(resultCode))
                GooglePlayServicesUtil.getErrorDialog(resultCode, this, PLAY_SERVICE_RES_REQUEST).show();
            else {
                Toast.makeText(this, "This device is not supported", Toast.LENGTH_SHORT).show();
                finish();
            }
            return false;

        }
        return true;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.reminder_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();


        if (id == R.id.toolbarCancel) {
            startActivity(new Intent(ReminderActivity.this, ListReminderActivity.class));
        } else if (id == R.id.toolbarSave) {
            final AlertDialog.Builder dialog = new AlertDialog.Builder(this);
            dialog.setMessage("Don't send this reminder to driver?");
            dialog.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    Addreminders();
                }
            });
            dialog.setNegativeButton("CANCEL", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.dismiss();


                }
            });

            alertDialog = dialog.create();
            alertDialog.show();

        }


        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case MY_PERMISSION_REQUEST_CODE:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    if (checkPlayServices()) {
                        buildingApiClient();
                        createLocationRequest();

                        displayLocation();

                    }
                }
        }
    }

    private void findDriver() {

        DatabaseReference drivers = FirebaseDatabase.getInstance().getReference(Common.driver_tbl);
        GeoFire geoFire = new GeoFire(drivers);

        GeoQuery geoQuery = geoFire.queryAtLocation(new GeoLocation(mLastLocation.getLatitude(), mLastLocation.getLongitude()), radiusDistance);
        geoQuery.removeAllListeners();
        geoQuery.addGeoQueryEventListener(new GeoQueryEventListener() {
            @Override
            public void onKeyEntered(String key, GeoLocation location) {
                if (!isDriverFound) {
                    isDriverFound = true;
                    driverID = key;
                    Toast.makeText(ReminderActivity.this, "Driver has found!:" + key, Toast.LENGTH_SHORT).show();
                }


            }

            @Override
            public void onKeyExited(String key) {

            }

            @Override
            public void onKeyMoved(String key, GeoLocation location) {

            }

            @Override
            public void onGeoQueryReady() {
                if (!isDriverFound) {
                    radiusDistance++;
                    findDriver();

                }

            }

            @Override
            public void onGeoQueryError(DatabaseError error) {

            }
        });
    }

    private void startLocationUpdates() {
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {


        }
        LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, mLocationRequest, this);
    }

    private void displayLocation() {
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(ReminderActivity.this, new String[]{
                    android.Manifest.permission.ACCESS_FINE_LOCATION,
                    android.Manifest.permission.ACCESS_COARSE_LOCATION}, MY_PERMISSION_REQUEST_CODE);

        }
        mLastLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
        if (mLastLocation != null) {

            final double latitude = mLastLocation.getLatitude();
            final double longitude = mLastLocation.getLongitude();


            geoFire.setLocation(FirebaseAuth.getInstance().getCurrentUser().getUid(), new GeoLocation(latitude, longitude), new GeoFire.CompletionListener() {
                @Override
                public void onComplete(String key, DatabaseError error) {


                }
            });

            Log.d("ERROR", String.format("Your location was changed : %f / %f", latitude, longitude));

        } else {
            Log.d("ERROR", "Cannot get your location");
        }
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
                        // reminderID=reminders1.getId();

                        currentDriver = driverId;
                        sendReference = db.getReference(Common.user_driver_tbl).child(currentDriver).child("Reminders").child(id);
                        reminders1.setId(id);
                        reminders1.setName(remindername.getText().toString().trim());
                        reminders1.setPlaceaddress(address);
                        reminders1.setLat(lat);
                        reminders1.setLng(lng);
                        reminders1.setRadius(radius.getText().toString());
                        sendReference.setValue(reminders1).addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                if (task.isSuccessful()) {
                                    Toast.makeText(getApplicationContext(), "Reminder has been sent to driver.", Toast.LENGTH_SHORT).show();
                                    getNameandEmail();
                                    Addreminders();


                                } else {
                                    Toast.makeText(getApplicationContext(), "Failed: Reminder was not sent.", Toast.LENGTH_SHORT).show();
                                }
                            }
                        });


                    }
                } else {
                    Toast.makeText(getApplicationContext(), "Driver is not available", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

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
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });
    }

    int PLACE_ATOCOMPLETE_REQUEST = 1;

    private void PlaceMODEFULLSCREEN() {
        try {
            Intent intent = new PlaceAutocomplete.IntentBuilder(PlaceAutocomplete.MODE_FULLSCREEN)
                    .build(this);
            startActivityForResult(intent, PLACE_ATOCOMPLETE_REQUEST);

        } catch (GooglePlayServicesNotAvailableException e) {
            e.printStackTrace();
        } catch (GooglePlayServicesRepairableException e) {
            e.printStackTrace();
        }
    }

    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == PLACE_ATOCOMPLETE_REQUEST) {
            if (resultCode == RESULT_OK) {
                Place place = PlaceAutocomplete.getPlace(this, data);

                titleDestination = place.getAddress().toString();
                mNewReminder = mMap.addMarker(new MarkerOptions()
                        .icon(BitmapDescriptorFactory.defaultMarker())
                        .position(place.getLatLng())
                        .title(titleDestination));
                mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(place.getLatLng(), 15.0f));
                destination.setText(titleDestination);

            } else if (resultCode == PlaceAutocomplete.RESULT_ERROR) {
                Status status = PlaceAutocomplete.getStatus(this, data);
                Toast.makeText(getBaseContext(), "ERROR: " + status.getStatusMessage(), Toast.LENGTH_SHORT).show();
            } else if (resultCode == RESULT_CANCELED) {


            }
        }
    }


    private void Addreminders() {
        users.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                getValues();
                if (TextUtils.isEmpty(remindername.getText().toString())) {
                    String name = remindername.getText().toString();
                    remindername.setText("None");
                }


                users.child(id).setValue(reminders).addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {

                        startActivity(new Intent(ReminderActivity.this, ListReminderActivity.class));
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
        reminders.setName(remindername.getText().toString().trim());
        reminders.setPlaceaddress(address);
        reminders.setLat(lat);
        reminders.setLng(lng);
        reminders.setRadius(radius.getText().toString());


    }


    public void onMapReady(GoogleMap googleMap) {

        double latitude = Double.parseDouble(lat);
        double longitude = Double.parseDouble(lng);
        mMap = googleMap;
        mReminder = mMap.addMarker(new MarkerOptions()
                .icon(BitmapDescriptorFactory.defaultMarker())
                .position(new LatLng(latitude, longitude))
                .title(address));


        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(latitude, longitude), 15.0f));

        LatLng geofence_area = new LatLng(latitude, longitude);
        circle = mMap.addCircle(new CircleOptions()
                .center(geofence_area)
                .radius(500)
                .strokeColor(Color.BLUE)
                .fillColor(0x220000FF)
                .strokeWidth(5.0f));


    }


    @Override
    public void onConnected(@Nullable Bundle bundle) {
        displayLocation();
        startLocationUpdates();

    }

    @Override
    public void onConnectionSuspended(int i) {
        mGoogleApiClient.connect();

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }

    @Override
    public void onLocationChanged(Location location) {
        mLastLocation = location;
        displayLocation();

    }

    @Override
    protected void onPause() {
        super.onPause();

    }


    @Override
    public void handleResult(Result result) {
        driverId = result.getText();

        Toast.makeText(getApplicationContext(), "Scanned Successfully!", Toast.LENGTH_SHORT).show();
        sendToDriver();


    }
}
