package com.example.map.activities;

import android.Manifest;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.content.res.AppCompatResources;
import androidx.core.app.ActivityCompat;
import androidx.core.content.res.ResourcesCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
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
import com.mapbox.maps.plugin.Plugin;
import com.mapbox.maps.plugin.gestures.OnMoveListener;
import com.mapbox.maps.plugin.locationcomponent.LocationComponentPlugin;
import com.mapbox.maps.plugin.locationcomponent.OnIndicatorBearingChangedListener;
import com.mapbox.maps.plugin.locationcomponent.OnIndicatorPositionChangedListener;
import com.mapbox.maps.plugin.scalebar.*;
import com.mapbox.maps.plugin.scalebar.generated.ScaleBarSettings;
import com.mapbox.maps.plugin.scalebar.generated.ScaleBarSettingsInterface;

import static com.mapbox.maps.plugin.gestures.GesturesUtils.getGestures;
import static com.mapbox.maps.plugin.locationcomponent.LocationComponentUtils.getLocationComponent;

public class MainActivity extends AppCompatActivity implements SelectStyleMapsFragment.OnMapStyleSelectedListener{

    private ActivityMainBinding activityMainBinding;
    private MapView mapView;
    private SharedPreferences sharedPreferences;

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
        mapView.getMapboxMap().setCamera(new CameraOptions.Builder().zoom(16.0).build());
        LocationComponentPlugin locationComponentPlugin = getLocationComponent(mapView);
        locationComponentPlugin.setEnabled(true);
        LocationPuck2D locationPuck2D = new LocationPuck2D();
        locationPuck2D.setBearingImage(ImageHolder.from(BitmapFactory.decodeResource(getApplicationContext().getResources(), R.drawable.my_location)));
        locationComponentPlugin.setLocationPuck(locationPuck2D);
        locationComponentPlugin.addOnIndicatorBearingChangedListener(onIndicatorBearingChangedListener);
        locationComponentPlugin.addOnIndicatorPositionChangedListener(onIndicatorPositionChangedListener);
        getGestures(mapView).addOnMoveListener(onMoveListener);
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
            mapView.getMapboxMap().setCamera(new CameraOptions.Builder().center(point).zoom(16.0).build());
            getGestures(mapView).setFocalPoint(mapView.getMapboxMap().pixelForCoordinate(point));
        }
    };

    private final OnMoveListener onMoveListener = new OnMoveListener() {
        @Override
        public void onMoveBegin(@NonNull MoveGestureDetector moveGestureDetector) {
            getLocationComponent(mapView).removeOnIndicatorBearingChangedListener(onIndicatorBearingChangedListener);
            getLocationComponent(mapView).removeOnIndicatorPositionChangedListener(onIndicatorPositionChangedListener);
            getGestures(mapView).removeOnMoveListener(onMoveListener);
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