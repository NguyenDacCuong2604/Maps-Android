package com.example.map.activities;

import android.Manifest;
import android.animation.ValueAnimator;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.view.View;
import android.view.animation.LinearInterpolator;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.content.res.AppCompatResources;
import androidx.core.app.ActivityCompat;
import androidx.databinding.DataBindingUtil;

import com.example.map.R;
import com.example.map.databinding.ActivityMainBinding;
import com.mapbox.android.gestures.MoveGestureDetector;
import com.mapbox.geojson.Point;
import com.mapbox.maps.CameraOptions;
import com.mapbox.maps.ImageHolder;
import com.mapbox.maps.MapView;
import com.mapbox.maps.Style;
import com.mapbox.maps.plugin.LocationPuck2D;
import com.mapbox.maps.plugin.gestures.OnMoveListener;
import com.mapbox.maps.plugin.locationcomponent.LocationComponentPlugin;
import com.mapbox.maps.plugin.locationcomponent.OnIndicatorBearingChangedListener;
import com.mapbox.maps.plugin.locationcomponent.OnIndicatorPositionChangedListener;

import static com.mapbox.maps.plugin.gestures.GesturesUtils.getGestures;
import static com.mapbox.maps.plugin.locationcomponent.LocationComponentUtils.getLocationComponent;

public class MainActivity extends AppCompatActivity implements SelectStyleMapsFragment.OnMapStyleSelectedListener{

    private ActivityMainBinding activityMainBinding;
    private MapView mapView;
    private SharedPreferences sharedPreferences;
    private ImageView btnMyLocation;
    private Point lastKnownPosition;
    private double scaleView = 16.0;
    private boolean isMyLocation = true;

    private final ActivityResultLauncher<String> activityResultLauncher = registerForActivityResult(new ActivityResultContracts.RequestPermission(), new ActivityResultCallback<Boolean>() {
        @Override
        public void onActivityResult(Boolean o) {
            if(o){
                Toast.makeText(MainActivity.this, "Permission Granted!", Toast.LENGTH_SHORT).show();
            }else {
                Toast.makeText(MainActivity.this, "Permission not granted!", Toast.LENGTH_SHORT).show();
            }
        }
    });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        activityMainBinding = DataBindingUtil.setContentView(this, R.layout.activity_main);
        sharedPreferences = getSharedPreferences("Maps", MODE_PRIVATE);
        this.mapView = activityMainBinding.mapView;
        this.btnMyLocation = activityMainBinding.btnMyLocation;
        loadStyleInPreferences();
        //Permission
        if (ActivityCompat.checkSelfPermission(MainActivity.this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            activityResultLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION);
        }

        activityMainBinding.btnSelectStyle.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                SelectStyleMapsFragment selectStyleMapsFragment = SelectStyleMapsFragment.newInstance(sharedPreferences.getString("mapStyle", Style.MAPBOX_STREETS));
                selectStyleMapsFragment.show(getSupportFragmentManager(), "Select Style Maps");
            }
        });

        //My Location
        loadMyLocation();
    }

    private void loadMyLocation() {
        mapView.getMapboxMap().setCamera(new CameraOptions.Builder().zoom(scaleView).build());
        LocationComponentPlugin locationComponentPlugin = getLocationComponent(mapView);
        locationComponentPlugin.setEnabled(true);
        LocationPuck2D locationPuck2D = new LocationPuck2D();
        locationPuck2D.setBearingImage(ImageHolder.from(BitmapFactory.decodeResource(getApplicationContext().getResources(), R.drawable.my_location)));
        locationComponentPlugin.setLocationPuck(locationPuck2D);
        locationComponentPlugin.addOnIndicatorBearingChangedListener(onIndicatorBearingChangedListener);
        locationComponentPlugin.addOnIndicatorPositionChangedListener(onIndicatorPositionChangedListener);
        getGestures(mapView).addOnMoveListener(onMoveListener);

        //Event my location
        btnMyLocation.setOnClickListener(view -> {
            if(!isMyLocation){
                isMyLocation = true;
                btnMyLocation.setImageResource(R.drawable.ic_my_location);
                btnMyLocation.setBackgroundResource(R.drawable.background_focus);
                if (lastKnownPosition != null) {
                    float currentZoom = (float) mapView.getMapboxMap().getCameraState().getZoom(); // Get current zoom
                    animateCameraToPositionWithCurrentScale(lastKnownPosition, currentZoom);
                } else {
                    Toast.makeText(this, "Current location not available", Toast.LENGTH_SHORT).show();
                }
                getGestures(mapView).addOnMoveListener(onMoveListener);
            }
        });
    }

    private void animateCameraToPositionWithCurrentScale(Point targetPosition, double startZoom) {
        // First animation: Move to the location with the current zoom level
        ValueAnimator moveAnimator = ValueAnimator.ofFloat(0, 1);
        moveAnimator.setDuration(600); // Duration for moving to the position
        moveAnimator.setInterpolator(new LinearInterpolator());

        Point startPosition = mapView.getMapboxMap().getCameraState().getCenter();

        moveAnimator.addUpdateListener(valueAnimator -> {
            float fraction = valueAnimator.getAnimatedFraction();
            double interpolatedLatitude = startPosition.latitude() + (targetPosition.latitude() - startPosition.latitude()) * fraction;
            double interpolatedLongitude = startPosition.longitude() + (targetPosition.longitude() - startPosition.longitude()) * fraction;

            mapView.getMapboxMap().setCamera(new CameraOptions.Builder()
                    .center(Point.fromLngLat(interpolatedLongitude, interpolatedLatitude))
                    .zoom(startZoom) // Maintain current zoom level during movement
                    .build());
        });

        moveAnimator.addListener(new android.animation.AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(android.animation.Animator animation) {
                // Second animation: Zoom in to the desired zoom level (e.g., 16.0) after reaching the location
                animateZoomToLevel();
            }
        });

        moveAnimator.start();
    }

    private void animateZoomToLevel() {
        float currentZoom = (float) mapView.getMapboxMap().getCameraState().getZoom();

        ValueAnimator zoomAnimator = ValueAnimator.ofFloat(currentZoom, (float) 16.0);
        zoomAnimator.setDuration(600); // Duration for zooming in
        zoomAnimator.setInterpolator(new LinearInterpolator());

        zoomAnimator.addUpdateListener(valueAnimator -> {
            double zoomLevel = (float) valueAnimator.getAnimatedValue();

            mapView.getMapboxMap().setCamera(new CameraOptions.Builder()
                    .zoom(zoomLevel)
                    .build());
        });

        zoomAnimator.start();
    }

    public void loadStyleInPreferences(){
        mapView.getMapboxMap().loadStyleUri(sharedPreferences.getString("mapStyle", Style.MAPBOX_STREETS));
    }

    @Override
    public void onMapStyleSelected(String style) {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        switch (style) {
            case "default":
                editor.putString("mapStyle", Style.MAPBOX_STREETS);
                break;
            case "satellite":
                editor.putString("mapStyle", Style.SATELLITE_STREETS);
                break;
            case "terrain":
                editor.putString("mapStyle", Style.OUTDOORS);
                break;
        }
        editor.apply();
        loadStyleInPreferences();
    }

    //Event
    private final OnIndicatorBearingChangedListener onIndicatorBearingChangedListener = new OnIndicatorBearingChangedListener() {
        @Override
        public void onIndicatorBearingChanged(double v) {
            mapView.getMapboxMap().setCamera(new CameraOptions.Builder().bearing(v).build());
        }
    };

    private final OnIndicatorPositionChangedListener onIndicatorPositionChangedListener = new OnIndicatorPositionChangedListener() {
        @Override
        public void onIndicatorPositionChanged(@NonNull Point point) {
            mapView.getMapboxMap().setCamera(new CameraOptions.Builder().center(point).zoom(scaleView).build());
            getGestures(mapView).setFocalPoint(mapView.getMapboxMap().pixelForCoordinate(point));
            lastKnownPosition = point;
        }
    };

    private final OnMoveListener onMoveListener = new OnMoveListener() {
        @Override
        public void onMoveBegin(@NonNull MoveGestureDetector moveGestureDetector) {
            getLocationComponent(mapView).removeOnIndicatorBearingChangedListener(onIndicatorBearingChangedListener);
            getLocationComponent(mapView).removeOnIndicatorPositionChangedListener(onIndicatorPositionChangedListener);
            getGestures(mapView).removeOnMoveListener(onMoveListener);
            if(isMyLocation){
                isMyLocation = false;
                btnMyLocation.setImageResource(R.drawable.ic_out_location);
                btnMyLocation.setBackgroundResource(R.drawable.background_multilayer);
            }

        }

        @Override
        public boolean onMove(@NonNull MoveGestureDetector moveGestureDetector) {
            return false;
        }

        @Override
        public void onMoveEnd(@NonNull MoveGestureDetector moveGestureDetector) {

        }
    };

}