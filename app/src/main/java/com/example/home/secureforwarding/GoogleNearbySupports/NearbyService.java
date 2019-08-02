package com.example.home.secureforwarding.GoogleNearbySupports;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.support.annotation.NonNull;
import android.util.Log;
import android.widget.Toast;

import com.example.home.secureforwarding.DatabaseHandler.AppDatabase;
import com.example.home.secureforwarding.Entities.DataShares;
import com.example.home.secureforwarding.Entities.KeyShares;
import com.example.home.secureforwarding.Entities.KeyStore;
import com.example.home.secureforwarding.KeyHandler.SingletoneECPRE;
import com.example.home.secureforwarding.MainActivity;
import com.example.home.secureforwarding.SharedPreferenceHandler.SharedPreferenceHandler;
import com.google.android.gms.nearby.Nearby;
import com.google.android.gms.nearby.connection.AdvertisingOptions;
import com.google.android.gms.nearby.connection.ConnectionInfo;
import com.google.android.gms.nearby.connection.ConnectionLifecycleCallback;
import com.google.android.gms.nearby.connection.ConnectionResolution;
import com.google.android.gms.nearby.connection.ConnectionsClient;
import com.google.android.gms.nearby.connection.DiscoveredEndpointInfo;
import com.google.android.gms.nearby.connection.DiscoveryOptions;
import com.google.android.gms.nearby.connection.EndpointDiscoveryCallback;
import com.google.android.gms.nearby.connection.Payload;
import com.google.android.gms.nearby.connection.PayloadCallback;
import com.google.android.gms.nearby.connection.PayloadTransferUpdate;
import com.google.android.gms.nearby.connection.Strategy;
import com.google.android.gms.tasks.OnFailureListener;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.Random;

public class NearbyService extends Service {
    public static final String TAG = NearbyService.class.getSimpleName();
    public static String SERVICE_ID = "secure_forwarding";
    static String NICKNAME = "XSFX";
    public static final int HANDLE_DELAY = 5000;
    //    ArrayList<String> previousConnectedDeviceId = new ArrayList<>();
    public static final String MSG_RECIVED = "msg_received";
    boolean flag = false;
    boolean receivedMsg = false;
    AppDatabase appDatabase;
    boolean destroyed;
    private ConnectionsClient connectionsClient;
    Random rand;
    String id;
    private static final Strategy STRATEGY = Strategy.P2P_POINT_TO_POINT;
    private String lastConnected;
    private String connectedFella;

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
        connectionsClient.stopAdvertising();
        connectionsClient.stopDiscovery();
        connectionsClient.stopAllEndpoints();
//        previousConnectedDeviceId.clear();
        setFlagsFalse();
//        adverDiscoverHandler.removeCallbacks(adverDiscoverRunnable);
//        checkRequestHandler.removeCallbacks(checkRequestRunnable);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d(TAG, "Service is started!");
        destroyed = false;
        appDatabase = AppDatabase.getAppDatabase(this);
        connectionsClient = Nearby.getConnectionsClient(this);
        rand = new Random();
        id = SharedPreferenceHandler.getStringValues(this, MainActivity.DEVICE_ID);
//        adverDiscoverHandler.postDelayed(adverDiscoverRunnable, HANDLE_DELAY);
        startAdvertising();
        startDiscovery();
        return START_NOT_STICKY;
    }

    private void startDiscovery() {
        connectionsClient.stopDiscovery();
        connectionsClient.startDiscovery(getPackageName(), endpointDiscoveryCallback,
                new DiscoveryOptions.Builder().setStrategy(STRATEGY).build()).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Log.d(TAG, "Failed discovery!");
                e.printStackTrace();
            }
        });
    }

    private void startAdvertising() {
        connectionsClient.stopAdvertising();
        connectionsClient.startAdvertising(id, getPackageName(), connectionLifecycleCallback,
                new AdvertisingOptions.Builder().setStrategy(STRATEGY).build()).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Log.d(TAG, "Failed advertising!");
                e.printStackTrace();
            }
        });
    }

//    Handler adverDiscoverHandler = new Handler();
//    Runnable adverDiscoverRunnable = new Runnable() {
//        @Override
//        public void run() {
//            if (flag == false) {
//                startAdverstisingDiscovery();
//                adverDiscoverHandler.postDelayed(adverDiscoverRunnable, HANDLE_DELAY);
//            }
//        }
//    };

    /**
     * This method starts the discovery and advertising of Nearby to other devices
     */
//    private void startAdverstisingDiscovery() {
//        Log.d(TAG, "Inside ad and dis method!");
//        Toast.makeText(this, "Searching for nearby devices!", Toast.LENGTH_SHORT).show();
//        Nearby.getConnectionsClient(getApplicationContext()).stopAdvertising();
//        Nearby.getConnectionsClient(getApplicationContext()).stopDiscovery();
//        Nearby.getConnectionsClient(getApplicationContext()).stopAllEndpoints();
//        Nearby.getConnectionsClient(getApplicationContext()).startAdvertising(NICKNAME, SERVICE_ID,
//                connectionLifecycleCallback, new AdvertisingOptions.Builder()
//                        .setStrategy(Strategy.P2P_POINT_TO_POINT).build());
//        Nearby.getConnectionsClient(getApplicationContext()).startDiscovery(SERVICE_ID,
//                endpointDiscoveryCallback, new DiscoveryOptions.Builder()
//                        .setStrategy(Strategy.P2P_POINT_TO_POINT).build());
//    }

    ConnectionLifecycleCallback connectionLifecycleCallback = new ConnectionLifecycleCallback() {
        @Override
        public void onConnectionInitiated(@NonNull String endpointID, @NonNull ConnectionInfo connectionInfo) {
            connectionsClient.acceptConnection(endpointID, payloadCallback);
            connectedFella = connectionInfo.getEndpointName();
        }

        @Override
        public void onConnectionResult(@NonNull String endpointId, @NonNull ConnectionResolution result) {
            if (result.getStatus().isSuccess()) {
                Log.d(TAG, "Connection successfully created with :" + endpointId);
//                    adverDiscoverHandler.removeCallbacks(adverDiscoverRunnable);
                Metadata metadata = new Metadata(SharedPreferenceHandler.getStringValues(getApplicationContext(), MainActivity.DEVICE_ID)
                        , SingletoneECPRE.getInstance(null).pubKey);
                sendStream(endpointId, metadata);
            } else {
                Log.d(TAG, "Connection unsuccessful");
                Toast.makeText(NearbyService.this, "Searching and discovering nearby devices!",
                        Toast.LENGTH_SHORT).show();
                setFlagsFalse();
                connectionsClient.stopAllEndpoints();
                connectionsClient.stopAdvertising();
                connectionsClient.stopDiscovery();
                startAdvertising();
                startDiscovery();
            }
        }

        @Override
        public void onDisconnected(@NonNull String s) {
            Log.d(TAG, "Disconnected from the other device");
            Toast.makeText(NearbyService.this,
                    "Disconnected from other device!! Searching and discovering nearby " +
                            "devices!", Toast.LENGTH_SHORT).show();
            setFlagsFalse();
            connectionsClient.stopAllEndpoints();
            connectionsClient.stopAdvertising();
            connectionsClient.stopDiscovery();
            startDiscovery();
            startAdvertising();
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
            lastConnected = endpointId;
            SharesPOJO pojo = null;
            if (payload.getType() == Payload.Type.STREAM) {
                Object receivedObj;
                InputStream inputStream = payload.asStream().asInputStream();
                try {
                    ObjectInputStream ois = new ObjectInputStream(inputStream);
                    receivedObj = ois.readObject();
                    ois.close();
                    if (receivedObj.getClass().equals(Metadata.class)) {
                        Metadata metadata = (Metadata) receivedObj;
                        appDatabase.dao().insertKeyStore(new KeyStore(metadata.deviceId, metadata.devicePubKey));
                        Toast.makeText(NearbyService.this, "Connected to the nearby device, device id:" + metadata.deviceId, Toast.LENGTH_SHORT).show();
                        P2PHandler p2PHandler = new P2PHandler(metadata.deviceId,
                                metadata.devicePubKey, getApplicationContext());
                        pojo = p2PHandler.fetchFilesToSend();
                        sendStream(endpointId, pojo);
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
            if (payload.getType() == Payload.Type.BYTES) {
                String msg = new String(payload.asBytes());
                if (receivedMsg == true && msg.contains(MSG_RECIVED)) {
                    updateKeysData(pojo);
                    Toast.makeText(NearbyService.this, "File transferred!", Toast.LENGTH_SHORT).show();
                    setFlagsFalse();
                    connectionsClient.stopAllEndpoints();
                    Toast.makeText(NearbyService.this, "Disconnected from " +
                                    "nearby device and searching for nearby devices!",
                            Toast.LENGTH_SHORT).show();
                    startAdvertising();
                    startDiscovery();
                }
            }
        }

        @Override
        public void onPayloadTransferUpdate(@NonNull String s, @NonNull PayloadTransferUpdate payloadTransferUpdate) {

        }
    };

    public void updateKeysData(SharesPOJO pojo){
        if(pojo != null){
            if(pojo.dataSharesToSend != null) {
                for (DataShares dataShares : pojo.dataSharesToSend) {
                    appDatabase.dao().insertDataShares(dataShares);
                }
            }
            if(pojo.keySharesToSend != null) {
                for (KeyShares keyShares : pojo.keySharesToSend) {
                    appDatabase.dao().insertKeyShares(keyShares);
                }
            }
        }

    }

    EndpointDiscoveryCallback endpointDiscoveryCallback = new EndpointDiscoveryCallback() {
        @Override
        public void onEndpointFound(@NonNull String endpointID, @NonNull DiscoveredEndpointInfo discoveredEndpointInfo) {
            Log.d(TAG, "Endpoint found:" + endpointID + " " + discoveredEndpointInfo.getServiceId());
            if (!endpointID.equals(lastConnected)) {
                Log.d(TAG, "Start connection:");
                Random r = new Random();
                int rand = r.nextInt(5000) + 1000;
                try {
                    Thread.sleep(rand);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                connectionsClient.stopDiscovery();
                connectionsClient.stopAdvertising();
                connectionsClient.requestConnection(id, endpointID, connectionLifecycleCallback)
                        .addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                startAdvertising();
                                startDiscovery();
                            }
                        });
            }
        }

        @Override
        public void onEndpointLost(@NonNull String s) {
            if (destroyed == false) {
                setFlagsFalse();
//                adverDiscoverHandler.removeCallbacks(adverDiscoverRunnable);
//                adverDiscoverHandler.postDelayed(adverDiscoverRunnable, HANDLE_DELAY);

                connectionsClient.stopAllEndpoints();
                connectionsClient.stopAdvertising();
                connectionsClient.stopDiscovery();
                Toast.makeText(NearbyService.this, "Searching and discovering nearby devices!",
                        Toast.LENGTH_SHORT).show();
                startAdvertising();
                startDiscovery();
            }
        }
    };


    private void setFlagsFalse() {
        flag = false;
        receivedMsg = false;
    }

//    Handler checkRequestHandler = new Handler();
//
//    Runnable checkRequestRunnable = new Runnable() {
//        @Override
//        public void run() {
//            if(requested == true){
//                requested = false;
//                flag = false;
//                Log.d(TAG, "inside checkrequestRunnable!");
//                adverDiscoverHandler.post(adverDiscoverRunnable);
//            }
//        }
//    };
}
