package com.richez.ladylyn.buscommuterapp.Adapter;

import android.app.Activity;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.richez.ladylyn.buscommuterapp.ListReminderActivity;
import com.richez.ladylyn.buscommuterapp.Model.Reminders;
import com.richez.ladylyn.buscommuterapp.R;

import java.util.List;

/**
 * Created by Ricojhon on 12/11/2018.
 */

public class ReminderItemAdapter extends ArrayAdapter<Reminders> {
    Activity reminderActivity;
    List<Reminders> remindersList;

    public ReminderItemAdapter(Activity reminderActivity, List<Reminders> remindersList) {
        super(reminderActivity, R.layout.list_item, remindersList);
        this.reminderActivity = reminderActivity;
        this.remindersList = remindersList;
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {


        LayoutInflater inflater = reminderActivity.getLayoutInflater();
        View listviewItem = inflater.inflate(R.layout.list_item, null, true);

        TextView textViewName = (TextView) listviewItem.findViewById(R.id.item_name);
        TextView textViewPlacename = (TextView) listviewItem.findViewById(R.id.item_PlaceAddress);

        Reminders reminders = remindersList.get(position);

        textViewName.setText(reminders.getName());
        textViewPlacename.setText(reminders.getPlaceaddress());


        return listviewItem;
    }
}
