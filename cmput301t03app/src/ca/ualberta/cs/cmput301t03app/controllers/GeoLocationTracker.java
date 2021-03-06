package ca.ualberta.cs.cmput301t03app.controllers;

import java.util.Timer;
import java.util.TimerTask;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.util.Log;
import ca.ualberta.cs.cmput301t03app.models.GeoLocation;

/**
 * This class is used as a controller for GeoLocation to get 
 * the user's current location and city. Both the network and gps
 * can be used to find the location, only if it is enabled.
 * 
 * This code is taken and modified from
 * http://stackoverflow.com/questions/3145089/what-is-the-simplest-and-most-robust-way-to-get-the-users-current-location-in-a
 * Author: Fedor
 * http://stackoverflow.com/users/95313/fedor
 */

public class GeoLocationTracker {
	
	private GeoLocation geoLocation;
	private Timer timer;
	private LocationManager locationManager;
	private Context context;
	
	private boolean gpsEnabled = false;
	
	public GeoLocationTracker(Context context, GeoLocation location) {
		this.geoLocation = location;
		this.context = context;
	}
	
	/**
	 * Gets the location of the user using GPS.
	 * If GPS is not enabled, the user will be prompted to enable it.
	 * 
	 */
	public boolean getLocation() {
        if (locationManager==null) {
            locationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
        }

        //exceptions will be thrown if provider is not permitted.
        try {
        	gpsEnabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
        	
        } catch (Exception e) {
        		
        }
        
        //don't start listeners if no provider is enabled
        if (!gpsEnabled) {
        	showGPSDisabledAlertBox();
        }
        
        if (gpsEnabled) {
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 35, 0, locationListenerGps);
        }
        
        timer = new Timer();
        timer.schedule(new GetLastLocation(), 10000);
        return true;
    }
	
    LocationListener locationListenerGps = new LocationListener() {
    	@Override
        public void onLocationChanged(Location location) {
            timer.cancel();
            geoLocation.setLatitude(location.getLatitude());
            geoLocation.setLongitude(location.getLongitude());
            Log.d("Loc","Lat: " + location.getLatitude());
            Log.d("Loc","Long: " + location.getLongitude());
            locationManager.removeUpdates(this);
        }
    	@Override
        public void onProviderDisabled(String provider) {
        	
        }
    	@Override
        public void onProviderEnabled(String provider) {
        	
        }
    	@Override
        public void onStatusChanged(String provider, int status, Bundle extras) {
        	
        }
    };


	
    /**
     * This class gets the last known location of the user if 
     * getLocation() fails to get the current location.
     */
    class GetLastLocation extends TimerTask {
        @Override
        public void run() {
        	((Activity) context).runOnUiThread(new Runnable() {
        		public void run() {
        	
		        	locationManager.removeUpdates(locationListenerGps);
		
		             Location gps_loc=null;
		             
		             if (gpsEnabled) {
		                 gps_loc=locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
		             }
		
		             if (gps_loc!=null) {
		              	geoLocation.setLatitude(gps_loc.getLatitude());
		          		geoLocation.setLongitude(gps_loc.getLongitude());
		                 return;
		             }
        		}
        	});
        }
    }
    
    /**
     * Creates a pop up dialog box to notify the user that GPS is not enabled
     * and gives the option for the user to be redirected to enable it.
     */
    public void showGPSDisabledAlertBox() {
    	AlertDialog.Builder alertBuilder = new AlertDialog.Builder(context);
    	alertBuilder.setMessage("GPS is disabled on this device. Do you want to enable it?")
    				.setCancelable(false)
    				.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
    	public void onClick(final DialogInterface dialog, final int id) {
                context.startActivity(new Intent(android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS));
            }
        })
        .setNegativeButton("No", new DialogInterface.OnClickListener() {
            public void onClick(final DialogInterface dialog, final int id) {
                 dialog.cancel();
            }
        });
    	AlertDialog alert = alertBuilder.create();
    	alert.show();
    }
}
