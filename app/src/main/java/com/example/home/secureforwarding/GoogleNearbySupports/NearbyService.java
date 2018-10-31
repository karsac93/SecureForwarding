package com.example.home.secureforwarding.GoogleNearbySupports;

import android.app.Service;
import android.content.Intent;
import android.os.Handler;
import android.os.IBinder;
import android.support.annotation.NonNull;
import android.util.Log;
import android.widget.Toast;

import com.example.home.secureforwarding.DatabaseHandler.AppDatabase;
import com.example.home.secureforwarding.Entities.DataShares;
import com.example.home.secureforwarding.Entities.KeyShares;
import com.example.home.secureforwarding.Entities.KeyStore;
import com.example.home.secureforwarding.KeyHandler.DecipherKeyShare;
import com.example.home.secureforwarding.KeyHandler.SingletoneECPRE;
import com.example.home.secureforwarding.MainActivity;
import com.example.home.secureforwarding.SharedPreferenceHandler.SharedPreferenceHandler;
import com.google.android.gms.nearby.Nearby;
import com.google.android.gms.nearby.connection.AdvertisingOptions;
import com.google.android.gms.nearby.connection.ConnectionInfo;
import com.google.android.gms.nearby.connection.ConnectionLifecycleCallback;
import com.google.android.gms.nearby.connection.ConnectionResolution;
import com.google.android.gms.nearby.connection.ConnectionsStatusCodes;
import com.google.android.gms.nearby.connection.DiscoveredEndpointInfo;
import com.google.android.gms.nearby.connection.DiscoveryOptions;
import com.google.android.gms.nearby.connection.EndpointDiscoveryCallback;
import com.google.android.gms.nearby.connection.Payload;
import com.google.android.gms.nearby.connection.PayloadCallback;
import com.google.android.gms.nearby.connection.PayloadTransferUpdate;
import com.google.android.gms.nearby.connection.Strategy;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.security.Key;
import java.util.ArrayList;
import java.util.List;

public class NearbyService extends Service {
    public static final String TAG = NearbyService.class.getSimpleName();
    public static String SERVICE_ID = "secure_forwarding";
    static String NICKNAME = "XSFX";
    public static final int HANDLE_DELAY = 5000;
    ArrayList<String> previousConnectedDeviceId = new ArrayList<>();
    public static final String MSG_RECIVED = "msg_received";
    boolean flag = false;
    boolean receivedMsg = false;
    AppDatabase appDatabase;
    boolean destroyed;

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
        destroyed = true;
        Nearby.getConnectionsClient(getApplicationContext()).stopAdvertising();
        Nearby.getConnectionsClient(getApplicationContext()).stopDiscovery();
        Nearby.getConnectionsClient(getApplicationContext()).stopAllEndpoints();
        previousConnectedDeviceId.clear();
        setFlagsFalse();
        adverDiscoverHandler.removeCallbacks(adverDiscoverRunnable);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d(TAG, "Service is started!");
        destroyed = false;
        Nearby.getConnectionsClient(getApplicationContext()).stopAllEndpoints();
        appDatabase = AppDatabase.getAppDatabase(this);
        adverDiscoverHandler.postDelayed(adverDiscoverRunnable, HANDLE_DELAY);
        return START_NOT_STICKY;
    }

    Handler adverDiscoverHandler = new Handler();
    Runnable adverDiscoverRunnable = new Runnable() {
        @Override
        public void run() {
            if (flag == false) {
                startAdverstisingDiscovery();
                adverDiscoverHandler.postDelayed(adverDiscoverRunnable, HANDLE_DELAY);
            }
        }
    };

    /**
     * This method starts the discovery and advertising of Nearby to other devices
     */
    private void startAdverstisingDiscovery() {
        Log.d(TAG, "Inside ad and dis method!");
        Toast.makeText(this, "Searching for nearby devices!", Toast.LENGTH_SHORT).show();
        Nearby.getConnectionsClient(getApplicationContext()).stopAdvertising();
        Nearby.getConnectionsClient(getApplicationContext()).stopDiscovery();
        Nearby.getConnectionsClient(getApplicationContext()).stopAllEndpoints();
        Nearby.getConnectionsClient(getApplicationContext()).startAdvertising(NICKNAME, SERVICE_ID,
                connectionLifecycleCallback, new AdvertisingOptions.Builder()
                        .setStrategy(Strategy.P2P_POINT_TO_POINT).build());
        Nearby.getConnectionsClient(getApplicationContext()).startDiscovery(SERVICE_ID,
                endpointDiscoveryCallback, new DiscoveryOptions.Builder()
                        .setStrategy(Strategy.P2P_POINT_TO_POINT).build());
    }

    ConnectionLifecycleCallback connectionLifecycleCallback = new ConnectionLifecycleCallback() {
        @Override
        public void onConnectionInitiated(@NonNull String endpointId, @NonNull ConnectionInfo connectionInfo) {
            if (connectionInfo.isIncomingConnection() && connectionInfo.getEndpointName().contains(NICKNAME)) {
                Log.d(TAG, "device_id name:" + connectionInfo.getEndpointName());
                flag = true;
                Nearby.getConnectionsClient(getApplicationContext()).stopDiscovery();
                Nearby.getConnectionsClient(getApplicationContext()).stopAdvertising();
                Nearby.getConnectionsClient(getApplicationContext())
                        .acceptConnection(endpointId, payloadCallback);
            } else {
                Nearby.getConnectionsClient(getApplicationContext())
                        .acceptConnection(endpointId, payloadCallback);
            }
        }

        @Override
        public void onConnectionResult(@NonNull String endpointId, @NonNull ConnectionResolution result) {
            switch (result.getStatus().getStatusCode()) {
                case ConnectionsStatusCodes.STATUS_OK:
                    Log.d(TAG, "Connection successfully created with :" + endpointId);
                    adverDiscoverHandler.removeCallbacks(adverDiscoverRunnable);
                    Metadata metadata = new Metadata(SharedPreferenceHandler.getStringValues(getApplicationContext(), MainActivity.DEVICE_ID)
                            , SingletoneECPRE.getInstance().pubKey);
                    sendStream(endpointId, metadata);
                    break;
                case ConnectionsStatusCodes.STATUS_CONNECTION_REJECTED:
                    Log.d(TAG, "Connection Failed");
                    if(destroyed == false) {
                        setFlagsFalse();
                        adverDiscoverHandler.postDelayed(adverDiscoverRunnable, HANDLE_DELAY);
                    }
                    break;
                default:
                    Log.d(TAG, "Connection broken");
                    Toast.makeText(NearbyService.this,
                            "Connection broken, searching nearby devices again!", Toast.LENGTH_SHORT).show();
                    if(destroyed == false) {
                        setFlagsFalse();
                        adverDiscoverHandler.postDelayed(adverDiscoverRunnable, HANDLE_DELAY);
                    }
            }
        }

        @Override
        public void onDisconnected(@NonNull String s) {
            Log.d(TAG, "Disconnected");
            if(destroyed == false) {
                setFlagsFalse();
                adverDiscoverHandler.postDelayed(adverDiscoverRunnable, HANDLE_DELAY);
            }
        }
    };

    private void sendStream(String endpointId, Object obj) {
        try {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            ObjectOutputStream oos = new ObjectOutputStream(baos);
            oos.writeObject(obj);
            oos.flush();
            oos.close();
            InputStream is = new ByteArrayInputStream(baos.toByteArray());
            Nearby.getConnectionsClient(this).sendPayload(endpointId, Payload.fromStream(is));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    PayloadCallback payloadCallback = new PayloadCallback() {
        @Override
        public void onPayloadReceived(@NonNull String endpointId, @NonNull Payload payload) {
            if (payload.getType() == Payload.Type.STREAM) {
                Object receivedObj;
                InputStream inputStream = payload.asStream().asInputStream();
                try {
                    ObjectInputStream ois = new ObjectInputStream(inputStream);
                    receivedObj = ois.readObject();
                    ois.close();
                    if (receivedObj.getClass().equals(Metadata.class)) {
                        Metadata metadata = (Metadata) receivedObj;
                        if (!previousConnectedDeviceId.contains(metadata.deviceId)) {
                            appDatabase.dao().insertKeyStore(new KeyStore(metadata.deviceId, metadata.devicePubKey));
                            previousConnectedDeviceId.add(metadata.deviceId);
                            Toast.makeText(NearbyService.this, "Connected to the nearby device, device id:" + metadata.deviceId, Toast.LENGTH_SHORT).show();
                            P2PHandler p2PHandler = new P2PHandler(metadata.deviceId,
                                    metadata.devicePubKey, getApplicationContext());
                            sendStream(endpointId, p2PHandler.fetchFilesToSend());
                            return;
                        } else {
                            quitConnection(endpointId);
                        }
                    } else if (receivedObj.getClass().equals(SharesPOJO.class)) {
                        receivedMsg = true;
                        SharesPOJO sharesPOJO = (SharesPOJO) receivedObj;
                        IncomingMsgHandler incomingMsgHandler = new IncomingMsgHandler(getApplicationContext(), sharesPOJO);
                        Thread thread = new Thread(incomingMsgHandler);
                        thread.start();
                        Nearby.getConnectionsClient(getApplicationContext()).
                                sendPayload(endpointId, Payload.fromBytes(MSG_RECIVED.getBytes()));

                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            if(payload.getType() == Payload.Type.BYTES){
                if(receivedMsg == true){
                    Toast.makeText(NearbyService.this, "File transferred!", Toast.LENGTH_SHORT).show();
                    quitConnection(endpointId);
                }
            }
        }

        @Override
        public void onPayloadTransferUpdate(@NonNull String s, @NonNull PayloadTransferUpdate payloadTransferUpdate) {

        }
    };

    private void quitConnection(String endpointId) {
        setFlagsFalse();
        Nearby.getConnectionsClient(getApplicationContext()).disconnectFromEndpoint(endpointId);
        adverDiscoverHandler.postDelayed(adverDiscoverRunnable, HANDLE_DELAY);
    }

    EndpointDiscoveryCallback endpointDiscoveryCallback = new EndpointDiscoveryCallback() {
        @Override
        public void onEndpointFound(@NonNull String endpointId, @NonNull DiscoveredEndpointInfo discoveredEndpointInfo) {
            if (discoveredEndpointInfo.getServiceId().equals(SERVICE_ID)) {
                flag = true;
                Log.d(TAG, "Discovered endpoint name:" + discoveredEndpointInfo.getEndpointName());
                Nearby.getConnectionsClient(getApplicationContext()).stopDiscovery();
                Nearby.getConnectionsClient(getApplicationContext()).stopAdvertising();
                Log.d(TAG, "Requesting connection!");
                Nearby.getConnectionsClient(getApplicationContext())
                        .requestConnection(discoveredEndpointInfo.getEndpointName(),
                                endpointId, connectionLifecycleCallback);
            }
        }

        @Override
        public void onEndpointLost(@NonNull String s) {
            if(destroyed == false) {
                setFlagsFalse();
                adverDiscoverHandler.removeCallbacks(adverDiscoverRunnable);
                adverDiscoverHandler.postDelayed(adverDiscoverRunnable, HANDLE_DELAY);
            }
        }
    };

    private void setFlagsFalse(){
        flag = false;
        receivedMsg = false;
    }
}
