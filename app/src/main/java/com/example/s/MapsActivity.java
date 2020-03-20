package com.example.s;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentActivity;

import android.Manifest;
import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.location.Location;
import android.media.AudioManager;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;

import static android.media.AudioManager.RINGER_MODE_NORMAL;
import static android.media.AudioManager.RINGER_MODE_SILENT;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback {

    private GoogleMap mMap;
    FusedLocationProviderClient mFusedLocationProviderClient;
    TextView currentProInfo;
    boolean mLocationPerssionGranted;
    private Location myLastLocation;
    private static final int DEFAULT_ZOOM=15;
    private LatLng mydefaultLocation=new LatLng(-33.534,140.324);
    AudioManager mAudioManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        mFusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);
        //REF to text view
        currentProInfo = findViewById(R.id.currentproinfo);
        //Reference to button
        Button changeAudioBtn=(Button)findViewById(R.id.button) ;

        changeAudioBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                NotificationManager notificationManager=(NotificationManager)getBaseContext().getSystemService(Context.NOTIFICATION_SERVICE);
                //get permission for DND
                //check build version
                if(Build.VERSION.SDK_INT>=Build.VERSION_CODES.M &&!notificationManager.isNotificationPolicyAccessGranted()){
                    Intent intent=new Intent(Settings.ACTION_NOTIFICATION_POLICY_ACCESS_SETTINGS);//Intent to access settings of DND
                    startActivity(intent);
                }
            }
        });
        getCurrentAudioProfile();
        BroadcastReceiver aReceiver=new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                getCurrentAudioProfile();

            }
        };
        IntentFilter aFilter=new IntentFilter(AudioManager.RINGER_MODE_CHANGED_ACTION);
        registerReceiver(aReceiver,aFilter);


    }
    private void getCurrentAudioProfile(){
        mAudioManager=(AudioManager)getBaseContext().getSystemService(Context.AUDIO_SERVICE);
        int currentRingerMode=mAudioManager.getRingerMode();
        //checking current ringer mode
        if(currentRingerMode==RINGER_MODE_NORMAL){
            currentProInfo.setText("Normal Profile");
        }
        else if(currentRingerMode==RINGER_MODE_SILENT){currentProInfo.setText("Silent Profile");}
        else {currentProInfo.setText("Vibrational Profile");}



    }


    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        //prompt the user to give location access
        getLocationPermission();
        //to update location ui
        updateLocationUI();
        //get current location
        getDeviceLocation();

    }

    private void getLocationPermission() {
        if (ContextCompat.checkSelfPermission(this.getApplicationContext(), Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            //Checks the status of permission
            mLocationPerssionGranted = true;
        } else {
            //request permission
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1);


        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        mLocationPerssionGranted=false;
        switch(requestCode)
        {
            case 1:{
                if(grantResults.length>0&&grantResults[0]==PackageManager.PERMISSION_GRANTED){
                    //grantResults.length>0 if permission is granted
                    mLocationPerssionGranted=true;
                }
            }
        }
        updateLocationUI();
    }


    private void updateLocationUI(){
        if(mMap==null){
            return;

        }
        try{
            if(mLocationPerssionGranted==true){
                mMap.setMyLocationEnabled(true);
                mMap.getUiSettings().setMyLocationButtonEnabled(true);
            }
            else
            {
                mMap.setMyLocationEnabled(false);
                mMap.getUiSettings().setMyLocationButtonEnabled(false);
                getLocationPermission();
            }

        }catch(SecurityException e){
            Log.e("SecExec",e.getMessage());
        }
    }
    private void getDeviceLocation(){
        try{
            if(mLocationPerssionGranted)
            {
                Task<Location> locationResult=mFusedLocationProviderClient.getLastLocation();
                locationResult.addOnCompleteListener(this, new OnCompleteListener<Location>() {
                    @Override
                    public void onComplete(@NonNull Task<Location> task) {
                        if(task.isSuccessful()){
                            myLastLocation=task.getResult();
                            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(
                                    new LatLng(myLastLocation.getLatitude(),
                                            myLastLocation.getLongitude()),DEFAULT_ZOOM));


                        }else{
                            Log.d("result_status","Null");
                            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(mydefaultLocation,DEFAULT_ZOOM));
                            mMap.getUiSettings().setMyLocationButtonEnabled(false);
                        }
                    }
                });
            }

        }catch(SecurityException e){
            Log.e("SecExecp",e.getMessage());
        }
    }
}
