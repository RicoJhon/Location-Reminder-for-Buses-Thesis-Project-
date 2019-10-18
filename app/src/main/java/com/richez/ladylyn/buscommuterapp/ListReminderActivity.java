package com.richez.ladylyn.buscommuterapp;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.GooglePlayServicesNotAvailableException;
import com.google.android.gms.common.GooglePlayServicesRepairableException;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.ui.PlaceAutocomplete;
import com.google.android.gms.location.places.ui.PlacePicker;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.richez.ladylyn.buscommuterapp.Adapter.ListItemAdapter;
import com.richez.ladylyn.buscommuterapp.Adapter.ReminderAdapter;
import com.richez.ladylyn.buscommuterapp.Adapter.ReminderItemAdapter;
import com.richez.ladylyn.buscommuterapp.Common.Common;
import com.richez.ladylyn.buscommuterapp.Model.Reminders;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import dmax.dialog.SpotsDialog;

public class ListReminderActivity extends AppCompatActivity implements BottomSheetFragment.BottomSheetListener {
    View view;

    FirebaseDatabase db;
    DatabaseReference users;

    RecyclerView listItem;
    RecyclerView.LayoutManager layoutManager;

    // ListItemAdapter adapter;
    ReminderAdapter adapter;
    SpotsDialog dialog;
    List<Reminders> remindersList;
    TextView rem1, rem2;
    ProgressBar loading;

    SharedPreferences sharedPreferences;
    SharedPreferences.Editor editor;
    public static final String MyPreferences = "MyPreferences";

    private static final int PLACE_PICKER_REQUEST = 1;
    BottomSheetFragment mbottomsheet;
    String placeName, placeAddress, placeID;
    Double placeLat, placeLng;

    AlertDialog alertDialog;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.list_reminder);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbarReminderList);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("Reminder List");
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
        }
        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fabaddReminder);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onAddPlaceButtonClicked();

            }
        });
        view = this.getWindow().getDecorView();
        view.setBackgroundResource(R.color.colorReminder);
        dialog = new SpotsDialog(this);
        remindersList = new ArrayList<>();
        sharedPreferences = this.getSharedPreferences(MyPreferences, Context.MODE_PRIVATE);
        rem1 = (TextView) findViewById(R.id.reminder1);
        loading = (ProgressBar) findViewById(R.id.loading_progress);
        rem1.setVisibility(View.GONE);
        loading.setVisibility(View.GONE);
        db = FirebaseDatabase.getInstance();
        listItem = (RecyclerView) findViewById(R.id.list_reminder);
        layoutManager = new LinearLayoutManager(this);
        listItem.setLayoutManager(layoutManager);
        listItem.setHasFixedSize(true);
        adapter = new ReminderAdapter(remindersList, ListReminderActivity.this);
        users = db.getReference(Common.user_rider_tbl).child(FirebaseAuth.getInstance().getCurrentUser().getUid()).child("Reminders");
        //loads the list of reminder
        loadData();

    }

    public void onAddPlaceButtonClicked() {
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

    private void loadData() {

        if (remindersList.size() > 0) {
            remindersList.clear();
        }
        users.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                Reminders reminders = dataSnapshot.getValue(Reminders.class);
                remindersList.add(reminders);
                adapter = new ReminderAdapter(remindersList, ListReminderActivity.this);
                listItem.setAdapter(adapter);
                adapter.notifyDataSetChanged();
                loading.setVisibility(View.GONE);

            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {

                Reminders reminders = dataSnapshot.getValue(Reminders.class);

                remindersList.remove(reminders);
                adapter.notifyDataSetChanged();


            }

            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    private void sendNewNotification(String title, String format) {
        Notification.Builder builder = new Notification.Builder(this)
                .setSmallIcon(R.mipmap.ic_newreminder)
                .setWhen(System.currentTimeMillis())
                .setTicker("New reminder has been added!")
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
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == PLACE_PICKER_REQUEST && resultCode == RESULT_OK) {
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

            Intent reminderIntent = new Intent(ListReminderActivity.this, ReminderActivity.class);
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

    @Override
    public void onButtonAddReminderClicked(String address, double lat, double lng) {
        Intent reminderIntent = new Intent(ListReminderActivity.this, ReminderActivity.class);
        reminderIntent.putExtra("address", placeAddress);
        reminderIntent.putExtra("lat", placeLat.toString());
        reminderIntent.putExtra("lng", placeLng.toString());
        startActivity(reminderIntent);
    }

    @Override
    public void onbuttonCancelled() {
        mbottomsheet.dismiss();


    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.delete_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_delete) {

            deleteAllReminders();
        }


        return super.onOptionsItemSelected(item);
    }

    private void deleteAllReminders() {
        final AlertDialog.Builder dialog = new AlertDialog.Builder(this);
        dialog.setTitle("Confirmation");
        dialog.setMessage("Are you sure you want to delete all reminders?");
        dialog.setPositiveButton("YES", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                DatabaseReference dReminders = FirebaseDatabase.getInstance().getReference(Common.user_rider_tbl).child(FirebaseAuth.getInstance().getCurrentUser().getUid()).child("Reminders");
                dReminders.removeValue().addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Toast.makeText(getApplicationContext(), "All reminders has been deleted.", Toast.LENGTH_SHORT).show();

                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(getApplicationContext(), "Failed: " + e.getMessage(), Toast.LENGTH_SHORT).show();


                    }
                });

            }
        });
        dialog.setNegativeButton("NO", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();


            }
        });

        alertDialog = dialog.create();
        alertDialog.show();

    }


}