package com.richez.ladylyn.buscommuterapp;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.BottomSheetDialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

/**
 * Created by Ricojhon on 26/10/2018.
 */

public class BottomSheetFragment extends BottomSheetDialogFragment {
    String mDestination;
    Double mLatitude, mLongitude;
    private BottomSheetListener mbottomSheetListener;


    public static BottomSheetFragment newInstance(String destination, Double latitude, Double longitude) {
        BottomSheetFragment f = new BottomSheetFragment();
        Bundle args = new Bundle();
        args.putString("destination", destination);
        args.putString("latitude", latitude.toString());
        args.putString("longitude", longitude.toString());
        f.setArguments(args);
        return f;
    }

    public void onCreate(@Nullable Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        mDestination = getArguments().getString("destination");
        mLatitude = getArguments().getDouble("latitude");
        mLongitude = getArguments().getDouble("longitude");
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {


        View view = inflater.inflate(R.layout.bottom_sheet, container, false);
        Button btnReminder = view.findViewById(R.id.btnReminder);
        Button btnCancel = view.findViewById(R.id.btnCancel);

        btnReminder.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mbottomSheetListener.onButtonAddReminderClicked(mDestination, mLatitude, mLongitude);

            }
        });
        btnCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mbottomSheetListener.onbuttonCancelled();

            }
        });
        return view;


    }

    public interface BottomSheetListener {
        void onButtonAddReminderClicked(String address, double lat, double lng);


        void onbuttonCancelled();
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

        mbottomSheetListener = (BottomSheetListener) context;
    }
}
