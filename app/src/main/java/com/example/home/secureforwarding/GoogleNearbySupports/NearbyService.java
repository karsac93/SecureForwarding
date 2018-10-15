package com.example.home.secureforwarding.GoogleNearbySupports;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.support.annotation.NonNull;
import android.util.Log;

import com.example.home.secureforwarding.MainActivity;
import com.example.home.secureforwarding.SharedPreferenceHandler.SharedPreferenceHandler;
import com.google.android.gms.nearby.Nearby;
import com.google.android.gms.nearby.connection.AdvertisingOptions;
import com.google.android.gms.nearby.connection.ConnectionInfo;
import com.google.android.gms.nearby.connection.ConnectionLifecycleCallback;
import com.google.android.gms.nearby.connection.ConnectionResolution;
import com.google.android.gms.nearby.connection.DiscoveredEndpointInfo;
import com.google.android.gms.nearby.connection.DiscoveryOptions;
import com.google.android.gms.nearby.connection.EndpointDiscoveryCallback;
import com.google.android.gms.nearby.connection.Strategy;

public class NearbyService extends Service {
    public static final String TAG = NearbyService.class.getSimpleName();
    public static final String SERVICE_ID = "secure_forwarding";
    static String deviceId = "";

    public NearbyService() {
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "Service is destroyed!");
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d(TAG, "Service is started!");
        Nearby.getConnectionsClient(getApplicationContext()).stopAllEndpoints();
        deviceId = SharedPreferenceHandler.getStringValues(this, MainActivity.DEVICE_ID);
        startAdverstisingDiscovery();
        return START_NOT_STICKY;
    }

    /**
     * This method starts the discovery and advertising of Nearby to other devices
     */
    private void startAdverstisingDiscovery() {
        Nearby.getConnectionsClient(getApplicationContext()).startAdvertising(deviceId, SERVICE_ID,
                connectionLifecycleCallback, new AdvertisingOptions.Builder()
                        .setStrategy(Strategy.P2P_POINT_TO_POINT).build());
        Nearby.getConnectionsClient(getApplicationContext()).startDiscovery(SERVICE_ID,
                endpointDiscoveryCallback, new DiscoveryOptions.Builder()
                        .setStrategy(Strategy.P2P_POINT_TO_POINT).build());
    }

    ConnectionLifecycleCallback connectionLifecycleCallback = new ConnectionLifecycleCallback() {
        @Override
        public void onConnectionInitiated(@NonNull String s, @NonNull ConnectionInfo connectionInfo) {

        }

        @Override
        public void onConnectionResult(@NonNull String s, @NonNull ConnectionResolution connectionResolution) {

        }

        @Override
        public void onDisconnected(@NonNull String s) {

        }
    };

    EndpointDiscoveryCallback endpointDiscoveryCallback = new EndpointDiscoveryCallback() {
        @Override
        public void onEndpointFound(@NonNull String endpointId, @NonNull DiscoveredEndpointInfo discoveredEndpointInfo) {
            Log.d(TAG, "Endpoint:" + endpointId);
            Log.d(TAG, "Service ID:" + discoveredEndpointInfo.getServiceId());
        }

        @Override
        public void onEndpointLost(@NonNull String s) {

        }
    };
}
