package com.richez.ladylyn.buscommuterapp;

import android.Manifest;
import android.app.Dialog;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AlertDialog;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
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
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.places.AutocompleteFilter;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.ui.PlaceAutocomplete;
import com.google.android.gms.location.places.ui.PlaceAutocompleteFragment;
import com.google.android.gms.location.places.ui.PlacePicker;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.rengwuxian.materialedittext.MaterialEditText;
import com.richez.ladylyn.buscommuterapp.Common.Common;
import com.richez.ladylyn.buscommuterapp.Model.Reminders;
import com.squareup.picasso.Picasso;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Random;
import java.util.UUID;

import de.hdodenhof.circleimageview.CircleImageView;
import dmax.dialog.SpotsDialog;
import io.paperdb.Paper;

public class Home extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener, OnMapReadyCallback,
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener,
        LocationListener, BottomSheetFragment.BottomSheetListener {

    private GoogleMap mMap;

    private static final int MY_PERMISSION_REQUEST_CODE = 1234;
    private static final int PLAY_SERVICE_RES_REQUEST = 7001;
    public static final int PICK_IMAGE_REQUEST = 1;
    public static final int PLACE_ATOCOMPLETE_REQUEST = 2;
    private static final int PLACE_PICKER_REQUEST = 123;

    private LocationRequest mLocationRequest;
    private GoogleApiClient mGoogleApiClient;
    private Location mLastLocation;

    private static int UPDATE_INTERVAL = 5000;
    private static int FATEST_INTERVAL = 3000;
    private static int DISPLACEMENT = 10;

    private PlaceAutocompleteFragment places;

    DatabaseReference drivers, reminderReference;
    FirebaseUser user;
    FirebaseAuth auth;
    FirebaseDatabase db;
    GeoFire geoFire;

    Marker mCurrent, mReminder, reminderMarker;


    SupportMapFragment mapFragment;
    private long backPressedTime;
    private Toast backToast;
    String destination;
    BottomSheetFragment mbottomsheet;
    AutocompleteFilter typeFilter;
    String placeName, placeAddress, placeID;
    Double placeLat, placeLng;
    Button addReminder;
    FirebaseStorage firebaseStorage;
    StorageReference storageReference;
    Uri saveUri;
    CircleImageView profilePic;
    CircleImageView profilepic;
    ImageView uploadImage;
    TextView user_name, user_email;
    Dialog dialog;
    byte[] data;
    GeoQuery geoQuery;

    //permission
    private boolean permissionGranted = false;
    private static final String FINE_LOCATION = Manifest.permission.ACCESS_FINE_LOCATION;
    private static final String COURSE_LOCATION = Manifest.permission.ACCESS_COARSE_LOCATION;
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1234;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON
                | WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        setContentView(R.layout.activity_home);
        mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
        auth = FirebaseAuth.getInstance();
        db = FirebaseDatabase.getInstance();
        user = auth.getCurrentUser();
        dialog = new Dialog(this);

        firebaseStorage = FirebaseStorage.getInstance();
        storageReference = firebaseStorage.getReference();
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onAddPlaceButtonClicked();
            }
        });

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);
        if (savedInstanceState == null) {
            navigationView.setCheckedItem(R.id.nav_home);
        }

        View header = navigationView.getHeaderView(0);
        user_name = (TextView) header.findViewById(R.id.txtriderName);
        user_email = (TextView) header.findViewById(R.id.txtriderEmail);
        profilePic = (CircleImageView) header.findViewById(R.id.imageView);

        if (user != null) {
            user_name.setText(user.getDisplayName());
            user_email.setText(user.getEmail());
            Picasso.with(this).load(user.getPhotoUrl()).into(profilePic);
        }

        typeFilter = new AutocompleteFilter.Builder()
                .setTypeFilter(AutocompleteFilter.TYPE_FILTER_NONE)
                .setCountry("PH")
                .build();

        addReminder = (Button) findViewById(R.id.btnReminder);

        drivers = FirebaseDatabase.getInstance().getReference(Common.rider_tbl);
        geoFire = new GeoFire(drivers);
        reminderReference = FirebaseDatabase.getInstance().getReference(Common.user_rider_tbl).child(user.getUid()).child("Reminders");

        setUpLocation();

    }

    private void onAddPlaceButtonClicked() {
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            Toast.makeText(this, "Need permission", Toast.LENGTH_LONG).show();
            return;
        }
        try {
            // Start a new Activity for the Place Picker API, this will trigger {@code #onActivityResult}
            // when a place is selected or with the user cancels.
            PlacePicker.IntentBuilder builder = new PlacePicker.IntentBuilder();
            Intent i = builder.build(this);
            startActivityForResult(i, PLACE_PICKER_REQUEST);
        } catch (GooglePlayServicesRepairableException e) {
            Log.e("ListReminder", String.format("GooglePlayServices Not Available [%s]", e.getMessage()));
        } catch (GooglePlayServicesNotAvailableException e) {
            Log.e("ListReminder", String.format("GooglePlayServices Not Available [%s]", e.getMessage()));
        } catch (Exception e) {
            Log.e("ListReminder", String.format("PlacePicker Exception: %s", e.getMessage()));
        }

    }

    private void displayLocation() {
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(Home.this, new String[]{
                    android.Manifest.permission.ACCESS_FINE_LOCATION,
                    android.Manifest.permission.ACCESS_COARSE_LOCATION}, MY_PERMISSION_REQUEST_CODE);

        }
        mLastLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
        if (mLastLocation != null) {

            final double latitude = mLastLocation.getLatitude();
            final double longitude = mLastLocation.getLongitude();
            final String address = getAddress(this, latitude, longitude);

            geoFire.setLocation(FirebaseAuth.getInstance().getCurrentUser().getUid(), new GeoLocation(latitude, longitude), new GeoFire.CompletionListener() {
                @Override
                public void onComplete(String key, DatabaseError error) {

                    if (mCurrent != null)
                        mCurrent.remove();
                    mCurrent = mMap.addMarker(new MarkerOptions()
                            .icon(BitmapDescriptorFactory.fromResource(R.drawable.commutermarker))
                            .position(new LatLng(latitude, longitude))
                            .title(address))
                    ;


                    mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(latitude, longitude), 15.0f));

                }
            });

            Log.d("ERROR", String.format("Your location was changed : %f / %f", latitude, longitude));

        } else {
            Log.d("ERROR", "Cannot get your location");
        }


    }

    public String getAddress(Context c, double lat, double lng) {
        String fullAddress = null;
        try {
            Geocoder geocoder = new Geocoder(c, Locale.getDefault());
            List<Address> addresses = geocoder.getFromLocation(lat, lng, 1);
            if (addresses.size() > 0) {
                Address address = addresses.get(0);

                fullAddress = address.getAddressLine(0);
            }

        } catch (IOException ex) {
            ex.printStackTrace();
        }
        return fullAddress;
    }

    private void setUpLocation() {
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(this, new String[]{
                    android.Manifest.permission.ACCESS_COARSE_LOCATION, android.Manifest.permission.ACCESS_FINE_LOCATION

            }, MY_PERMISSION_REQUEST_CODE);
        } else {

            if (checkPlayServices()) {
                buildingApiClient();
                createLocationRequest();

                displayLocation();

            }

        }
    }

    private void startLocationUpdates() {
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {


        }
        LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, mLocationRequest, this);
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            if (backPressedTime + 2000 > System.currentTimeMillis()) {
                backToast.cancel();
                super.onBackPressed();
                return;
            } else {
                backToast = Toast.makeText(getBaseContext(), "Press back again to exit", Toast.LENGTH_SHORT);
                backToast.show();
            }
            backPressedTime = System.currentTimeMillis();
        }

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.home, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.toolbarSearch) {
            PlaceMODEFULLSCREEN();
        }


        return super.onOptionsItemSelected(item);
    }

    private void PlaceMODEFULLSCREEN() {


        try {
            Intent intent = new PlaceAutocomplete.IntentBuilder(PlaceAutocomplete.MODE_FULLSCREEN)
                    .setFilter(typeFilter)
                    .build(this);
            startActivityForResult(intent, PLACE_ATOCOMPLETE_REQUEST);

        } catch (GooglePlayServicesNotAvailableException e) {
            e.printStackTrace();
        } catch (GooglePlayServicesRepairableException e) {
            e.printStackTrace();
        }

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case PLACE_ATOCOMPLETE_REQUEST:
                if (resultCode == RESULT_OK) {
                    Place place = PlaceAutocomplete.getPlace(this, data);
                    placeLat = place.getLatLng().latitude;
                    placeLng = place.getLatLng().longitude;

                    destination = place.getAddress().toString();
                    mReminder = mMap.addMarker(new MarkerOptions()
                            .icon(BitmapDescriptorFactory.defaultMarker())
                            .position(place.getLatLng())
                            .title(destination));


                    mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(place.getLatLng(), 15.0f));
                    mbottomsheet = BottomSheetFragment.newInstance(destination, placeLat, placeLng);
                    mbottomsheet.show(getSupportFragmentManager(), mbottomsheet.getTag());

                } else if (resultCode == PlaceAutocomplete.RESULT_ERROR) {
                    Status status = PlaceAutocomplete.getStatus(this, data);
                    Toast.makeText(getBaseContext(), "ERROR: " + status.getStatusMessage(), Toast.LENGTH_SHORT).show();
                } else if (resultCode == RESULT_CANCELED) {


                }
                break;


            case PICK_IMAGE_REQUEST:
                if (resultCode == RESULT_OK && data != null) {

                    // the user has successfully picked an image
                    // we need to save its reference to a Uri variable
                    saveUri = data.getData();
                    profilepic.setImageURI(saveUri);


                }
                break;
            case PLACE_PICKER_REQUEST:
                if (resultCode == RESULT_OK) {
                    Place place = PlacePicker.getPlace(this, data);
                    if (place == null) {
                        Log.i("ListReminder", "No place selected");
                        return;
                    }

                    // Extract the place information from the API
                    placeName = place.getName().toString();
                    placeAddress = place.getAddress().toString();
                    placeID = place.getId();
                    placeLat = place.getLatLng().latitude;
                    placeLng = place.getLatLng().longitude;

                    Intent reminderIntent = new Intent(Home.this, ReminderActivity.class);
                    if (placeAddress.isEmpty()) {
                        placeAddress = placeLat.toString() + "," + placeLng.toString();
                    }
                    reminderIntent.putExtra("address", placeAddress);
                    reminderIntent.putExtra("lat", placeLat.toString());
                    reminderIntent.putExtra("lng", placeLng.toString());

                    startActivity(reminderIntent);
                } else if (resultCode == PlaceAutocomplete.RESULT_ERROR) {
                    Status status = PlaceAutocomplete.getStatus(this, data);
                    Toast.makeText(getBaseContext(), "ERROR: " + status.getStatusMessage(), Toast.LENGTH_SHORT).show();
                } else if (resultCode == RESULT_CANCELED) {


                }
        }


    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.nav_home) {
            Intent homeIntent = new Intent(Home.this, Home.class);
            startActivity(homeIntent);
            // Haome
        } else if (id == R.id.nav_notification) {
            Intent homeIntent = new Intent(Home.this, ListReminderActivity.class);
            startActivity(homeIntent);
        } else if (id == R.id.nav_about) {
            startActivity(new Intent(Home.this, AboutActivity.class));

        } else if (id == R.id.nav_setting) {
            startActivity(new Intent(Home.this, SettingsActivity.class));
        } else if (id == R.id.nav_logout) {
            signOut();

        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    private void signOut() {
        Paper.init(this);
        Paper.book().destroy();
        auth.signOut();
        Intent signoutIntent = new Intent(Home.this, Login2Activity.class);
        startActivity(signoutIntent);
        finish();
    }

    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {


        switch (requestCode) {
            case MY_PERMISSION_REQUEST_CODE:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                    if (checkPlayServices()) {
                        buildingApiClient();
                        createLocationRequest();

                        displayLocation();

                    }
                } else {
                    Toast.makeText(getApplicationContext(), "Permission Denied, You cannot access location services", Toast.LENGTH_LONG).show();
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                        if (shouldShowRequestPermissionRationale(android.Manifest.permission.CAMERA)) {
                            showMessageOKCancel("You need to allow access to both the permissions",
                                    new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {
                                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                                                ActivityCompat.requestPermissions(Home.this, new String[]{
                                                        android.Manifest.permission.ACCESS_COARSE_LOCATION,
                                                        android.Manifest.permission.ACCESS_FINE_LOCATION
                                                }, MY_PERMISSION_REQUEST_CODE);
                                            }
                                        }
                                    });
                            return;
                        }
                    }


                }
                break;


        }
    }

    private void showMessageOKCancel(String message, DialogInterface.OnClickListener okListener) {
        new android.support.v7.app.AlertDialog.Builder(Home.this)
                .setMessage(message)
                .setPositiveButton("OK", okListener)
                .setNegativeButton("Cancel", null)
                .create()
                .show();

    }

    private void createLocationRequest() {
        if (isLowPowerEnabled()) {
            mLocationRequest = new LocationRequest();
            mLocationRequest.setInterval(UPDATE_INTERVAL);
            mLocationRequest.setFastestInterval(FATEST_INTERVAL);
            mLocationRequest.setPriority(LocationRequest.PRIORITY_LOW_POWER);
            mLocationRequest.setSmallestDisplacement(DISPLACEMENT);
        } else {
            mLocationRequest = new LocationRequest();
            mLocationRequest.setInterval(UPDATE_INTERVAL);
            mLocationRequest.setFastestInterval(FATEST_INTERVAL);
            mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
            mLocationRequest.setSmallestDisplacement(DISPLACEMENT);
        }
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

    public boolean isNotificationEnabled() {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        return sharedPreferences.getBoolean("switch_notification", true);
    }

    public boolean isLowPowerEnabled() {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        return sharedPreferences.getBoolean("switch_save_battery", false);
    }

    public String getNotify() {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        return sharedPreferences.getString("list_notify", "both");
    }

    private void displayReminders() {
        reminderReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for (DataSnapshot childDs : dataSnapshot.getChildren()) {
                    Reminders reminders = childDs.getValue(Reminders.class);
                    double latitude = Double.parseDouble(reminders.getLat());
                    double longitude = Double.parseDouble(reminders.getLng());
                    double radius = Double.parseDouble(reminders.getRadius());
                    double proximityRadius = radius / 1000f;
                    LatLng location = new LatLng(latitude, longitude);
                    reminderMarker = mMap.addMarker(new MarkerOptions()
                            .icon(BitmapDescriptorFactory.defaultMarker())
                            .position(location)
                            .title(reminders.getPlaceaddress()));
                    mMap.addCircle(new CircleOptions()
                            .center(location)
                            .radius(radius)
                            .strokeColor(Color.BLUE)
                            .fillColor(0x220000FF)
                            .strokeWidth(5.0f));
                    if (geoQuery != null) {
                        geoQuery.removeAllListeners();
                    }
                    //Geoquery
                    geoQuery = geoFire.queryAtLocation(new GeoLocation(location.latitude, location.longitude), proximityRadius);

                    geoQuery.addGeoQueryEventListener(new GeoQueryEventListener() {
                        @Override
                        public void onKeyEntered(String key, GeoLocation location) {
                            if (isNotificationEnabled()) {
                                if (getNotify().equals("both") || getNotify().equals("arrive")) {
                                    sendNotification("Busz", String.format("You entered the destination area"), String.format("You entered the destination area", key));
                                }
                            }
                            Intent showReminderIntent = new Intent(getBaseContext(), ShowReminderActivity.class);
                            startActivity(showReminderIntent);
                        }

                        @Override
                        public void onKeyExited(String key) {
                            if (isNotificationEnabled()) {
                                if (getNotify().equals("both") || getNotify().equals("leave")) {
                                    sendNotification("Busz", String.format("You are no longer in the destination area"), String.format("You are no longer in the destination area", key));
                                }
                            }

                        }

                        @Override
                        public void onKeyMoved(String key, GeoLocation location) {

                        }

                        @Override
                        public void onGeoQueryReady() {

                        }

                        @Override
                        public void onGeoQueryError(DatabaseError error) {
                            Log.d("ERROR: ", "" + error);

                        }
                    });
                }

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        Toast.makeText(this, "Map is Ready", Toast.LENGTH_SHORT).show();
        Log.d("BUSZ", "onMapReady: map is ready");
        mMap = googleMap;
        mMap = googleMap;
        mMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);


        mMap.setTrafficEnabled(false);
        mMap.setIndoorEnabled(false);
        mMap.setBuildingsEnabled(false);
        displayReminders();

    }

    private void sendNotification(String title, String format, String ticker) {
        Notification.Builder builder = new Notification.Builder(this)
                .setSmallIcon(R.mipmap.ic_newreminder)
                .setTicker(ticker)

                .setWhen(System.currentTimeMillis())
                .setContentTitle(title)
                .setContentText(format)
                .setVibrate(new long[]{1000, 1000, 1000, 1000, 1000});

        NotificationManager manger = (NotificationManager) this.getSystemService(Context.NOTIFICATION_SERVICE);
        Intent intent = new Intent(this, ListReminderActivity.class);
        PendingIntent contentIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_IMMUTABLE);
        builder.setContentIntent(contentIntent);

        Notification notification = builder.build();
        notification.flags |= Notification.FLAG_AUTO_CANCEL;
        notification.defaults |= Notification.DEFAULT_SOUND;

        manger.notify(new Random().nextInt(), notification);


    }

    @Override
    public void onButtonAddReminderClicked(String address, double lat, double lng) {
        Intent reminderIntent = new Intent(Home.this, ReminderActivity.class);
        reminderIntent.putExtra("address", destination);
        reminderIntent.putExtra("lat", placeLat.toString());
        reminderIntent.putExtra("lng", placeLng.toString());
        startActivity(reminderIntent);

    }

    @Override
    public void onbuttonCancelled() {
        mbottomsheet.dismiss();
        mReminder.remove();


    }

}
