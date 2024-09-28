package com.example.map.activities;

import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;

import com.example.map.R;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.mapbox.maps.Style;

public class SelectStyleMapsFragment extends BottomSheetDialogFragment {
    private OnMapStyleSelectedListener listener;
    private String style;
    private FrameLayout frameLayoutDefault;
    private FrameLayout frameLayoutSatellite;
    private FrameLayout frameLayoutTerrain;

    private TextView textViewDefault;
    private TextView textViewSatellite;
    private TextView textViewTerrain;

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        if (context instanceof OnMapStyleSelectedListener) {
            listener = (OnMapStyleSelectedListener) context;
        } else {
            throw new ClassCastException(context.toString() + " must implement OnMapStyleSelectedListener.");
        }
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            style = getArguments().getString("style");
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_select_stylemaps, container, false);

        ImageView imageView = view.findViewById(R.id.btn_close);
        imageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dismiss();
            }
        });

        frameLayoutDefault = view.findViewById(R.id.frame_image_default);
        frameLayoutSatellite = view.findViewById(R.id.frame_image_satellite);
        frameLayoutTerrain = view.findViewById(R.id.frame_image_terrain);

        textViewDefault = view.findViewById(R.id.text_default);
        textViewSatellite = view.findViewById(R.id.text_satellite);
        textViewTerrain = view.findViewById(R.id.text_terrain);

        loadCheckStyle();



        // Bắt sự kiện ấn vào các layout
        LinearLayout viewDefault = view.findViewById(R.id.view_default);
        LinearLayout viewSatellite = view.findViewById(R.id.view_satellite);
        LinearLayout viewTerrain = view.findViewById(R.id.view_terrain);

        viewDefault.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                loadCheckStyle(Style.MAPBOX_STREETS);
                listener.onMapStyleSelected("default");
            }
        });

        viewSatellite.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                loadCheckStyle(Style.SATELLITE_STREETS);
                listener.onMapStyleSelected("satellite");
            }
        });

        viewTerrain.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                loadCheckStyle(Style.OUTDOORS);
                listener.onMapStyleSelected("terrain");
            }
        });

        return view;
    }

    public void loadCheckStyle(String styleText){
        frameLayoutDefault.setBackgroundResource(R.drawable.background_icon);
        textViewDefault.setTextColor(ContextCompat.getColor(getContext(), R.color.black));
        frameLayoutSatellite.setBackgroundResource(R.drawable.background_icon);
        textViewSatellite.setTextColor(ContextCompat.getColor(getContext(), R.color.black));
        frameLayoutTerrain.setBackgroundResource(R.drawable.background_icon);
        textViewTerrain.setTextColor(ContextCompat.getColor(getContext(), R.color.black));

        switch (styleText){
            case Style.MAPBOX_STREETS:
                frameLayoutDefault.setBackgroundResource(R.drawable.background_icon_select);
                textViewDefault.setTextColor(ContextCompat.getColor(getContext(), R.color.blue));
                break;
            case Style.SATELLITE_STREETS:
                frameLayoutSatellite.setBackgroundResource(R.drawable.background_icon_select);
                textViewSatellite.setTextColor(ContextCompat.getColor(getContext(), R.color.blue));
                break;
            case Style.OUTDOORS:
                frameLayoutTerrain.setBackgroundResource(R.drawable.background_icon_select);
                textViewTerrain.setTextColor(ContextCompat.getColor(getContext(), R.color.blue));
                break;
        }
    }

    public void loadCheckStyle(){
        switch (style){
            case Style.MAPBOX_STREETS:
                frameLayoutDefault.setBackgroundResource(R.drawable.background_icon_select);
                textViewDefault.setTextColor(ContextCompat.getColor(getContext(), R.color.blue));
                break;
            case Style.SATELLITE_STREETS:
                frameLayoutSatellite.setBackgroundResource(R.drawable.background_icon_select);
                textViewSatellite.setTextColor(ContextCompat.getColor(getContext(), R.color.blue));
                break;
            case Style.OUTDOORS:
                frameLayoutTerrain.setBackgroundResource(R.drawable.background_icon_select);
                textViewTerrain.setTextColor(ContextCompat.getColor(getContext(), R.color.blue));
                break;
        }
    }

    public static SelectStyleMapsFragment newInstance(String style) {
        SelectStyleMapsFragment fragment = new SelectStyleMapsFragment();
        Bundle args = new Bundle();
        args.putString("style", style);
        fragment.setArguments(args);
        return fragment;
    }

    // Interface để gửi sự kiện về MainActivity
    public interface OnMapStyleSelectedListener {
        void onMapStyleSelected(String style);
    }
}
