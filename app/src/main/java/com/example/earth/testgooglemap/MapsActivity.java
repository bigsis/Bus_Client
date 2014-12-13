package com.example.earth.testgooglemap;

import android.content.Context;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.Timer;
import java.util.TimerTask;

import de.tavendo.autobahn.WebSocketConnection;
import de.tavendo.autobahn.WebSocketException;
import de.tavendo.autobahn.WebSocketHandler;


public class MapsActivity extends FragmentActivity {

    private GoogleMap mMap; // Might be null if Google Play services APK is not available.
    private Marker marker;
    private LatLng userPos;
    private Requesttask rt;
    private URL url;
    private LocationManager lm;
    private double lat, lng;
    private Timer timer;
    private TimerTask task;
    private HashMap<String,Marker> markers;
    private WebSocketConnection wsc = new WebSocketConnection();
    private ParseBusXml pbx;
    private MarkerController mc;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        pbx = ParseBusXml.getInstance();
        setContentView(R.layout.activity_maps);
        setUpMapIfNeeded();
        markers = new HashMap<String, Marker>();
        mc = MarkerController.getInstance(mMap, markers);
        final String wsuri= "ws://158.108.141.84:8080";
        try {
            wsc.connect(wsuri, new WebSocketHandler(){
                @Override
                public void onOpen() {

                    wsc.sendTextMessage("Hello, world!");
                }

                @Override
                public void onTextMessage(String payload) {
//                    Toast.makeText(getApplicationContext(), payload, Toast.LENGTH_LONG).show();
                    mc.setBusLocation(pbx.parseXmlToBusWebSoc(payload));
                }

                @Override
                public void onClose(int code, String reason) {
//                    Log.d(TAG, "Connection lost.");
                }
            });
        } catch (WebSocketException e) {
            System.out.println(e);
            e.printStackTrace();
        }
//        timer = new Timer();
//        task = new TimerTask() {
//            @Override
//            public void run() {
//                System.out.println("fuck");
//                new Requesttask(mMap, markers).execute("http://180.183.52.23:8080/busesposition");
//            }
//        };
//        try {
//            setClient();
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
    }

    protected  void setClient() throws IOException {

        timer.schedule(task,0,15000);



    }

    @Override
    protected void onResume() {
        super.onResume();
        setUpMapIfNeeded();
    }

    /**
     * Sets up the map if it is possible to do so (i.e., the Google Play services APK is correctly
     * installed) and the map has not already been instantiated.. This will ensure that we only ever
     * call {@link #setUpMap()} once when {@link #mMap} is not null.
     * <p/>
     * If it isn't installed {@link SupportMapFragment} (and
     * {@link com.google.android.gms.maps.MapView MapView}) will show a prompt for the user to
     * install/update the Google Play services APK on their device.
     * <p/>
     * A user can return to this FragmentActivity after following the prompt and correctly
     * installing/updating/enabling the Google Play services. Since the FragmentActivity may not
     * have been completely destroyed during this process (it is likely that it would only be
     * stopped or paused), {@link #onCreate(Bundle)} may not be called again so we should call this
     * method in {@link #onResume()} to guarantee that it will be called.
     */
    private void setUpMapIfNeeded() {
        // Do a null check to confirm that we have not already instantiated the map.
        if (mMap == null) {
            // Try to obtain the map from the SupportMapFragment.
            mMap = ((SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map))
                    .getMap();
            // Check if we were successful in obtaining the map.
            if (mMap != null) {
                setUpMap();
            }
        }
    }

    LocationListener listener = new LocationListener() {
        @Override
        public void onLocationChanged(Location loc) {
            LatLng coor = new LatLng(loc.getLatitude(), loc.getLongitude());
            lat = loc.getLatitude();
            lng = loc.getLongitude();

            if( marker != null )
                marker.remove();
            marker = mMap.addMarker(new MarkerOptions().position(new LatLng(lat, lng)));
            mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(coor, 17));
        }

        @Override
        public void onStatusChanged(String provider, int status, Bundle extras) {

        }

        @Override
        public void onProviderEnabled(String provider) {

        }

        @Override
        public void onProviderDisabled(String provider) {

        }
    };

    /**
     * This is where we can add markers or lines, add listeners or move the camera. In this case, we
     * just add a marker near Africa.
     * <p/>
     * This should only be called once and when we are sure that {@link #mMap} is not null.
     */
    private void setUpMap() {
        lm = (LocationManager)getSystemService(Context.LOCATION_SERVICE);

        boolean isNetwork =
                lm.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
        boolean isGPS =
                lm.isProviderEnabled(LocationManager.GPS_PROVIDER);

        if(isNetwork) {
            lm.requestLocationUpdates(LocationManager.NETWORK_PROVIDER
                    , 5000, 10, listener);
            Location loc = lm.getLastKnownLocation(
                    LocationManager.NETWORK_PROVIDER);
            if(loc != null) {
                lat = loc.getLatitude();
                lng = loc.getLongitude();
            }
        }

//        if(isGPS) {
//            lm.requestLocationUpdates(LocationManager.GPS_PROVIDER
//                    , 5000, 10, listener);
//            Location loc = lm.getLastKnownLocation(
//                    LocationManager.GPS_PROVIDER);
//            if(loc != null) {
//                lat = loc.getLatitude();
//                lng = loc.getLongitude();
//            }
//        }
    }

    public void onPause(){
        super.onPause();
        lm.removeUpdates(listener);
    }
}
