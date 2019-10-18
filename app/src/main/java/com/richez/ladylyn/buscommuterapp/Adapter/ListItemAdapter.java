package com.richez.ladylyn.buscommuterapp.Adapter;

import android.support.v7.widget.RecyclerView;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.richez.ladylyn.buscommuterapp.ListReminderActivity;
import com.richez.ladylyn.buscommuterapp.Model.Reminders;
import com.richez.ladylyn.buscommuterapp.R;
import com.richez.ladylyn.buscommuterapp.ReminderActivity;

import java.util.List;

/**
 * Created by Ricojhon on 24/10/2018.
 */
class ListItemViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener, View.OnCreateContextMenuListener {
    ItemClickListener itemClickListener;
    TextView reminder_name, placeaddress;

    public ListItemViewHolder(View itemView) {
        super(itemView);
        itemView.setOnClickListener(this);
        itemView.setOnCreateContextMenuListener(this);

        reminder_name = (TextView) itemView.findViewById(R.id.item_name);

        placeaddress = (TextView) itemView.findViewById(R.id.item_PlaceAddress);

    }

    public void setItemClickListener(ItemClickListener itemClickListener) {
        this.itemClickListener = itemClickListener;
    }

    @Override
    public void onClick(View v) {
        itemClickListener.onClick(getAdapterPosition());


    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
        menu.setHeaderTitle("Select the action");
        menu.add(0, 0, getAdapterPosition(), "DELETE");

    }
}

public class ListItemAdapter extends RecyclerView.Adapter<ListItemViewHolder> {
    ListReminderActivity reminderActivity;
    List<Reminders> remindersList;

    public ListItemAdapter(ListReminderActivity reminderActivity, List<Reminders> remindersList) {
        this.reminderActivity = reminderActivity;
        this.remindersList = remindersList;
    }


    @Override
    public ListItemViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(reminderActivity.getBaseContext());
        View view = inflater.inflate(R.layout.list_item, parent, false);

        return new ListItemViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ListItemViewHolder holder, int position) {
        Reminders data = remindersList.get(position);


        holder.reminder_name.setText(data.getName());
        holder.placeaddress.setText(data.getPlaceaddress());


    }

    @Override
    public int getItemCount() {
        return remindersList.size();
    }
}
