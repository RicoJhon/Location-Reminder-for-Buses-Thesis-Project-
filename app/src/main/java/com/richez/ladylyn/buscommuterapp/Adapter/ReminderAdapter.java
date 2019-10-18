package com.richez.ladylyn.buscommuterapp.Adapter;

import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.richez.ladylyn.buscommuterapp.Common.Common;
import com.richez.ladylyn.buscommuterapp.MapsActivity;
import com.richez.ladylyn.buscommuterapp.Model.Reminders;
import com.richez.ladylyn.buscommuterapp.R;
import com.squareup.picasso.Picasso;

import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

/**
 * Created by Ricojhon on 15/03/2019.
 */

public class ReminderAdapter extends RecyclerView.Adapter<ReminderAdapter.ViewHolder> {
    private List<Reminders> listItems;
    private Context mContext;


    public ReminderAdapter(List<Reminders> listItems, Context mContext) {
        this.listItems = listItems;
        this.mContext = mContext;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_item_new, parent, false);
        ViewHolder v = new ViewHolder(view);
        return v;
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, final int position) {
        final Reminders data = listItems.get(position);
        holder.itemName.setText(data.getName());
        holder.itemPlaceAdress.setText(data.getPlaceaddress());
        // Picasso.with(mContext).load(data.getPhotoUrl()).into(holder.photo);
        holder.itemDelete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DatabaseReference dReminders = FirebaseDatabase.getInstance().getReference(Common.user_rider_tbl).child(FirebaseAuth.getInstance().getCurrentUser().getUid()).child("Reminders").child(data.getId());
                dReminders.removeValue().addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        listItems.remove(position);
                        notifyDataSetChanged();
                        Toast.makeText(mContext, "Deleted Succesfully", Toast.LENGTH_LONG).show();
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(mContext, "Failed: " + e.getMessage(), Toast.LENGTH_LONG).show();
                    }
                });


            }
        });
        holder.setItemClickListener(new ItemClickListener() {
            @Override
            public void onClick(int position) {

                Reminders reminders = listItems.get(position);
                Intent intent = new Intent(mContext, MapsActivity.class);
                intent.putExtra("lat", reminders.getLat());
                intent.putExtra("long", reminders.getLng());
                intent.putExtra("radius", reminders.getRadius());
                intent.putExtra("name", reminders.getName());
                mContext.startActivity(intent);


            }
        });


    }

    @Override
    public int getItemCount() {
        return listItems.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        TextView itemName;
        TextView itemPlaceAdress;
        TextView itemDelete;
        CircleImageView photo;
        ItemClickListener itemClickListener;

        public ViewHolder(View itemView) {
            super(itemView);

            itemName = (TextView) itemView.findViewById(R.id.item_name);
            itemPlaceAdress = (TextView) itemView.findViewById(R.id.item_PlaceAddress);
            itemDelete = (TextView) itemView.findViewById(R.id.item_delete);
            photo = (CircleImageView) itemView.findViewById(R.id.item_photo);


        }


        @Override
        public void onClick(View v) {
            itemClickListener.onClick(getAdapterPosition());

        }

        public void setItemClickListener(ItemClickListener itemClickListener) {
            this.itemClickListener = itemClickListener;
        }
    }
}
