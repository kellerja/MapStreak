package com.example.mapstreakplaceholder.location.callback;

import android.app.Activity;
import android.content.IntentSender;
import android.support.annotation.NonNull;

import com.example.mapstreakplaceholder.permissions.Permissions;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResult;
import com.google.android.gms.location.LocationSettingsStates;
import com.google.android.gms.location.LocationSettingsStatusCodes;

public class LocationSettingsResultCallback implements ResultCallback<LocationSettingsResult> {

    private final PendingResult<LocationSettingsResult> result;
    private Activity caller;

    public LocationSettingsResultCallback(LocationRequest mLocationRequest, GoogleApiClient mGoogleApiClient, Activity caller) {
        this.caller = caller;

        LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder()
                .addLocationRequest(mLocationRequest);

        result = LocationServices.SettingsApi.checkLocationSettings(mGoogleApiClient, builder.build());

        result.setResultCallback(this);
    }

    @Override
    public void onResult(@NonNull LocationSettingsResult locationSettingsResult) {
        final Status status = locationSettingsResult.getStatus();
        final LocationSettingsStates locationSettingsStates = locationSettingsResult.getLocationSettingsStates();
        switch (status.getStatusCode()) {
            case LocationSettingsStatusCodes.SUCCESS:
                // All location settings are satisfied. The client can
                // initialize location requests here.
                break;
            case LocationSettingsStatusCodes.RESOLUTION_REQUIRED:
                // LocationServiceManager settings are not satisfied, but this can be fixed
                // by showing the user a dialog.
                try {
                    // Show the dialog by calling startResolutionForResult(),
                    // and check the result in onActivityResult().
                    status.startResolutionForResult(
                            caller,
                            Permissions.REQUEST_CHECK_SETTINGS.getPermissionCode());
                } catch (IntentSender.SendIntentException e) {
                    // Ignore the error.
                }
                break;
            case LocationSettingsStatusCodes.SETTINGS_CHANGE_UNAVAILABLE:
                // LocationServiceManager settings are not satisfied. However, we have no way
                // to fix the settings so we won't show the dialog.
                break;
        }
    }
}
