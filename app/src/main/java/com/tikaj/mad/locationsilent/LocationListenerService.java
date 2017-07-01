package com.tikaj.mad.locationsilent;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.media.AudioManager;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;
import android.widget.Toast;


public class LocationListenerService extends Service {
    private static double Location_Latitude =  26.89352919;
    private static double Location_Longitude =  80.99545512;
    private static double Range =  6;//in meters


    private static final int TWO_MINUTES = 1000 * 60 * 2;
    public LocationManager locationManager;
    public MyLocationListener listener;
    public Location previousBestLocation = null;
    public AudioManager audioManager;

    private static int currentMode =0;


    Intent intent;
    int counter = 0;

    @Override
    public void onCreate() {
        super.onCreate();

    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        super.onStartCommand(intent, flags, startId);
        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        listener = new MyLocationListener();
        locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0, 0, listener);
        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, listener);
        audioManager = (AudioManager) getSystemService(AUDIO_SERVICE);

        return START_STICKY;
    }
    @Override
    public void onStart(Intent intent, int startId) {

    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    protected boolean isBetterLocation(Location location, Location currentBestLocation) {
        if (currentBestLocation == null) {
            // A new location is always better than no location
            return true;
        }

        // Check whether the new location fix is newer or older
        long timeDelta = location.getTime() - currentBestLocation.getTime();
        boolean isSignificantlyNewer = timeDelta > TWO_MINUTES;
        boolean isSignificantlyOlder = timeDelta < -TWO_MINUTES;
        boolean isNewer = timeDelta > 0;

        // If it's been more than two minutes since the current location, use the new location
        // because the user has likely moved
        if (isSignificantlyNewer) {
            return true;
            // If the new location is more than two minutes older, it must be worse
        } else if (isSignificantlyOlder) {
            return false;
        }

        // Check whether the new location fix is more or less accurate
        int accuracyDelta = (int) (location.getAccuracy() - currentBestLocation.getAccuracy());
        boolean isLessAccurate = accuracyDelta > 0;
        boolean isMoreAccurate = accuracyDelta < 0;
        boolean isSignificantlyLessAccurate = accuracyDelta > 200;

        // Check if the old and new location are from the same provider
        boolean isFromSameProvider = isSameProvider(location.getProvider(),
                currentBestLocation.getProvider());

        // Determine location quality using a combination of timeliness and accuracy
        if (isMoreAccurate) {
            return true;
        } else if (isNewer && !isLessAccurate) {
            return true;
        } else if (isNewer && !isSignificantlyLessAccurate && isFromSameProvider) {
            return true;
        }
        return false;
    }

    /**
     * Checks whether two providers are the same
     */
    private boolean isSameProvider(String provider1, String provider2) {
        if (provider1 == null) {
            return provider2 == null;
        }
        return provider1.equals(provider2);
    }

    @Override
    public void onDestroy() {
        // handler.removeCallbacks(sendUpdatesToUI);
        super.onDestroy();
        Log.v("STOP_SERVICE", "DONE");
        locationManager.removeUpdates(listener);
    }


    public class MyLocationListener implements LocationListener {

        public void onLocationChanged(final Location loc) {
            String tag ="LOC";
            Log.i(tag, "Location changed");
            if (isBetterLocation(loc, previousBestLocation)) {
                loc.getLatitude();
                loc.getLongitude();
                Log.i(tag,"Latitude - "+ loc.getLatitude());
                Log.i(tag,"Longitude - "+ loc.getLongitude());
                Log.i(tag,"Provider - "+ loc.getProvider());
                float[] results = new float[1];
                Location.distanceBetween(Location_Latitude,Location_Longitude , loc.getLatitude(), loc.getLongitude(), results);
                float distanceInMeters = results[0];

                Log.i(tag,"Distance - "+distanceInMeters);
                if(distanceInMeters<=Range && currentMode==0){
                    Log.i(tag,"Silent");
                    audioManager.setRingerMode(AudioManager.RINGER_MODE_SILENT);
                    currentMode=1;
                }else if( distanceInMeters> Range && currentMode==1){
                    Log.i(tag,"Normal");
                    audioManager.setRingerMode(AudioManager.RINGER_MODE_NORMAL);
                    currentMode=0;
                }



            /*final Geocoder geocoder = new Geocoder(getApplicationContext(), Locale.getDefault());
            String Text = "";
            try {
                List<Address> addresses = geocoder.getFromLocation(loc.getLatitude(), loc.getLongitude(), 1);
                Text = "My current location is: "+addresses.get(0).getAddressLine(0);

            } catch (Exception e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
                Text = "My current location is: " +"Latitude = " + loc.getLatitude() + ", Longitude = " + loc.getLongitude();
            }
            */
                //Toast.makeText( getApplicationContext(), "Location polled to server", Toast.LENGTH_SHORT).show();
            }
        }

        public void onProviderDisabled(String provider) {
            Toast.makeText(getApplicationContext(), "Gps Disabled", Toast.LENGTH_SHORT).show();
        }


        public void onProviderEnabled(String provider) {
            Toast.makeText(getApplicationContext(), "Gps Enabled", Toast.LENGTH_SHORT).show();
        }


        public void onStatusChanged(String provider, int status, Bundle extras) {

        }

    }
}