package com.your.plugin.nearbyconnections;
import com.getcapacitor.PermissionState;
import android.Manifest;
import android.util.Log;

import com.getcapacitor.JSObject;
import com.getcapacitor.Plugin;
import com.getcapacitor.PluginCall;
import com.getcapacitor.PluginMethod;
import com.getcapacitor.annotation.CapacitorPlugin;
import com.getcapacitor.annotation.Permission;
import com.getcapacitor.annotation.PermissionCallback;

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

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@CapacitorPlugin(
    name = "NearbyConnections",
    permissions = {
        @Permission(
            strings = {
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.BLUETOOTH,
                Manifest.permission.BLUETOOTH_ADMIN,
                Manifest.permission.BLUETOOTH_ADVERTISE,
                Manifest.permission.BLUETOOTH_CONNECT,
                Manifest.permission.BLUETOOTH_SCAN,
                Manifest.permission.ACCESS_WIFI_STATE,
                Manifest.permission.CHANGE_WIFI_STATE,
                Manifest.permission.NEARBY_WIFI_DEVICES
            },
            alias = "nearby"
        )
    }
)
public class NearbyConnectionsPlugin extends Plugin {
    private static final String TAG = "NearbyConnections";
    
    private ConnectionsClient connectionsClient;
    private String serviceName;
    private Strategy strategy;
    private boolean isAdvertising = false;
    private boolean isDiscovering = false;
    private final List<String> connectedEndpoints = new ArrayList<>();
    private final Map<String, PluginCall> pendingCalls = new HashMap<>();
    
    @Override
    public void load() {
        connectionsClient = Nearby.getConnectionsClient(getContext());
        Log.d(TAG, "NearbyConnections plugin loaded");
    }
    
    @PluginMethod
    public void initialize(PluginCall call) {
        serviceName = call.getString("serviceName", "CapacitorNearby");
        String strategyName = call.getString("strategy", "P2P_CLUSTER");
        
        Log.d(TAG, "Initializing NearbyConnections with service: " + serviceName + ", strategy: " + strategyName);
        
        switch (strategyName) {
            case "P2P_CLUSTER":
                strategy = Strategy.P2P_CLUSTER;
                break;
            case "P2P_STAR":
                strategy = Strategy.P2P_STAR;
                break;
            case "P2P_POINT_TO_POINT":
                strategy = Strategy.P2P_POINT_TO_POINT;
                break;
            default:
                strategy = Strategy.P2P_CLUSTER;
                break;
        }
        
        if (!hasRequiredPermissions()) {
            Log.d(TAG, "Requesting permissions");
            requestPermissions(call);
            return;
        }
        
        Log.d(TAG, "Initialized successfully");
        call.resolve();
    }
    
    @Override
    public boolean hasRequiredPermissions() {
        // Check for required permissions
        return getPermissionState("nearby") == PermissionState.GRANTED;
    }
    
    @PermissionCallback
    private void permissionsCallback(PluginCall call) {
        if (hasRequiredPermissions()) {
            if ("initialize".equals(call.getMethodName())) {
                call.resolve();
            } else {
                // Retry the original method
                bridge.saveCall(call);
                try {
                    NearbyConnectionsPlugin.class.getMethod(call.getMethodName(), PluginCall.class)
                            .invoke(this, call);
                } catch (Exception e) {
                    call.reject("Unable to execute method: " + e.getMessage());
                }
            }
        } else {
            call.reject("Required permissions not granted");
        }
    }
    
    @PluginMethod
    public void startAdvertising(PluginCall call) {
        if (!hasRequiredPermissions()) {
            Log.d(TAG, "Requesting permissions for advertising");
            requestPermissions(call);
            return;
        }
        
        if (isAdvertising) {
            Log.d(TAG, "Already advertising");
            call.resolve();
            return;
        }
        
        String name = call.getString("name", android.os.Build.MODEL);
        Log.d(TAG, "Starting advertising as: " + name);
        
        connectionsClient.startAdvertising(
            name,
            serviceName,
            connectionLifecycleCallback,
            new AdvertisingOptions.Builder().setStrategy(strategy).build()
        ).addOnSuccessListener(unused -> {
            isAdvertising = true;
            Log.d(TAG, "Advertising started successfully");
            call.resolve();
        }).addOnFailureListener(e -> {
            Log.e(TAG, "Failed to start advertising: " + e.getMessage(), e);
            call.reject("Failed to start advertising: " + e.getMessage());
        });
    }
    
    @PluginMethod
    public void stopAdvertising(PluginCall call) {
        if (isAdvertising) {
            Log.d(TAG, "Stopping advertising");
            connectionsClient.stopAdvertising();
            isAdvertising = false;
        }
        call.resolve();
    }
    
    @PluginMethod
    public void startDiscovery(PluginCall call) {
        if (!hasRequiredPermissions()) {
            Log.d(TAG, "Requesting permissions for discovery");
            requestPermissions(call);
            return;
        }
        
        if (isDiscovering) {
            Log.d(TAG, "Already discovering");
            call.resolve();
            return;
        }
        
        Log.d(TAG, "Starting discovery for service: " + serviceName);
        
        connectionsClient.startDiscovery(
            serviceName,
            endpointDiscoveryCallback,
            new DiscoveryOptions.Builder().setStrategy(strategy).build()
        ).addOnSuccessListener(unused -> {
            isDiscovering = true;
            Log.d(TAG, "Discovery started successfully");
            call.resolve();
        }).addOnFailureListener(e -> {
            Log.e(TAG, "Failed to start discovery: " + e.getMessage(), e);
            call.reject("Failed to start discovery: " + e.getMessage());
        });
    }
    
    @PluginMethod
    public void stopDiscovery(PluginCall call) {
        if (isDiscovering) {
            Log.d(TAG, "Stopping discovery");
            connectionsClient.stopDiscovery();
            isDiscovering = false;
        }
        call.resolve();
    }
    
// Check for proper exception handling in your plugin's requestConnection method
    @PluginMethod
    public void requestConnection(PluginCall call) {
        if (!hasRequiredPermissions()) {
            Log.d(TAG, "Requesting permissions for connection");
            requestPermissions(call);
            return;
        }
        
        String endpointId = call.getString("endpointId");
        String name = call.getString("name", android.os.Build.MODEL);
        
        if (endpointId == null) {
            call.reject("endpointId is required");
            return;
        }
        
        Log.d(TAG, "Requesting connection to endpoint: " + endpointId + " as: " + name);
        
        pendingCalls.put(endpointId, call);
        
        connectionsClient.requestConnection(
            name,
            endpointId,
            connectionLifecycleCallback
        ).addOnSuccessListener(unused -> {
            Log.d(TAG, "Connection request successfully sent to " + endpointId);
            // Don't resolve the call here, wait for the connection result callback
        }).addOnFailureListener(e -> {
            Log.e(TAG, "Failed to request connection: " + e.getMessage(), e);
            pendingCalls.remove(endpointId);
            call.reject("Failed to request connection: " + e.getMessage());
        });
    }
    
    @PluginMethod
    public void acceptConnection(PluginCall call) {
        String endpointId = call.getString("endpointId");
        
        if (endpointId == null) {
            call.reject("endpointId is required");
            return;
        }
        
        Log.d(TAG, "Accepting connection from endpoint: " + endpointId);
        
        connectionsClient.acceptConnection(endpointId, payloadCallback)
            .addOnSuccessListener(unused -> {
                Log.d(TAG, "Connection acceptance initiated");
                call.resolve();
            })
            .addOnFailureListener(e -> {
                Log.e(TAG, "Failed to accept connection: " + e.getMessage(), e);
                call.reject("Failed to accept connection: " + e.getMessage());
            });
    }
    
    @PluginMethod
    public void rejectConnection(PluginCall call) {
        String endpointId = call.getString("endpointId");
        
        if (endpointId == null) {
            call.reject("endpointId is required");
            return;
        }
        
        Log.d(TAG, "Rejecting connection from endpoint: " + endpointId);
        
        connectionsClient.rejectConnection(endpointId)
            .addOnSuccessListener(unused -> {
                Log.d(TAG, "Connection rejection successful");
                call.resolve();
            })
            .addOnFailureListener(e -> {
                Log.e(TAG, "Failed to reject connection: " + e.getMessage(), e);
                call.reject("Failed to reject connection: " + e.getMessage());
            });
    }
    
    @PluginMethod
    public void sendMessage(PluginCall call) {
        if (!hasRequiredPermissions()) {
            Log.d(TAG, "Requesting permissions for sending message");
            requestPermissions(call);
            return;
        }
        
        String endpointId = call.getString("endpointId");
        String data = call.getString("data");
        
        if (endpointId == null || data == null) {
            call.reject("endpointId and data are required");
            return;
        }
        
        if (!connectedEndpoints.contains(endpointId)) {
            call.reject("Not connected to endpoint: " + endpointId);
            return;
        }
        
        Log.d(TAG, "Sending message to endpoint: " + endpointId);
        
        Payload payload = Payload.fromBytes(data.getBytes(StandardCharsets.UTF_8));
        String messageId = UUID.randomUUID().toString();
        
        connectionsClient.sendPayload(endpointId, payload)
            .addOnSuccessListener(unused -> {
                Log.d(TAG, "Message sent successfully with ID: " + messageId);
                JSObject result = new JSObject();
                result.put("messageId", messageId);
                call.resolve(result);
            })
            .addOnFailureListener(e -> {
                Log.e(TAG, "Failed to send message: " + e.getMessage(), e);
                call.reject("Failed to send message: " + e.getMessage());
            });
    }
    
    @PluginMethod
    public void disconnect(PluginCall call) {
        String endpointId = call.getString("endpointId");
        
        if (endpointId == null) {
            call.reject("endpointId is required");
            return;
        }
        
        Log.d(TAG, "Disconnecting from endpoint: " + endpointId);
        
        if (connectedEndpoints.contains(endpointId)) {
            connectionsClient.disconnectFromEndpoint(endpointId);
            connectedEndpoints.remove(endpointId);
        }
        
        call.resolve();
    }
    
    @PluginMethod
    public void getStatus(PluginCall call) {
        JSObject status = new JSObject();
        status.put("isAdvertising", isAdvertising);
        status.put("isDiscovering", isDiscovering);
        
        JSObject endpoints = new JSObject();
        for (String endpoint : connectedEndpoints) {
            endpoints.put(endpoint, true);
        }
        
        status.put("connectedEndpoints", connectedEndpoints);
        Log.d(TAG, "Returning status: " + status.toString());
        call.resolve(status);
    }
    
    // Callback for connections to other devices
    private final ConnectionLifecycleCallback connectionLifecycleCallback =
        new ConnectionLifecycleCallback() {
        @Override
        public void onConnectionInitiated(String endpointId, ConnectionInfo connectionInfo) {
            Log.d(TAG, "Connection initiated with endpoint: " + endpointId + ", name: " + connectionInfo.getEndpointName());
            
            // Notify JS about the connection request
            JSObject connectionEvent = new JSObject();
            connectionEvent.put("endpointId", endpointId);
            connectionEvent.put("endpointName", connectionInfo.getEndpointName());
            connectionEvent.put("authenticationToken", connectionInfo.getAuthenticationToken());
            connectionEvent.put("isIncomingConnection", connectionInfo.isIncomingConnection());
            
            notifyListeners("onConnectionInitiated", connectionEvent);
        }

        @Override
        public void onConnectionResult(String endpointId, ConnectionResolution result) {
            Log.d(TAG, "Connection result for endpoint: " + endpointId + ", status: " + result.getStatus());
            
            if (result.getStatus().isSuccess()) {
                connectedEndpoints.add(endpointId);
                
                // Notify JS about the successful connection
                JSObject connectionResult = new JSObject();
                connectionResult.put("endpointId", endpointId);
                connectionResult.put("status", "connected");
                notifyListeners("onConnectionResult", connectionResult);
                
                // Resolve pending call if exists
                PluginCall pendingCall = pendingCalls.remove(endpointId);
                if (pendingCall != null && !pendingCall.isReleased()) {
                    pendingCall.resolve();
                }
            } else {
                // Notify JS about the failed connection
                JSObject connectionResult = new JSObject();
                connectionResult.put("endpointId", endpointId);
                connectionResult.put("status", "failed");
                connectionResult.put("message", result.getStatus().getStatusMessage());
                notifyListeners("onConnectionResult", connectionResult);
                
                // Reject pending call if exists
                PluginCall pendingCall = pendingCalls.remove(endpointId);
                if (pendingCall != null && !pendingCall.isReleased()) {
                    pendingCall.reject("Connection failed: " + result.getStatus().getStatusMessage());
                }
            }
        }

        @Override
        public void onDisconnected(String endpointId) {
            Log.d(TAG, "Disconnected from endpoint: " + endpointId);
            
            connectedEndpoints.remove(endpointId);
            
            // Notify JS about the disconnection
            JSObject disconnectEvent = new JSObject();
            disconnectEvent.put("endpointId", endpointId);
            notifyListeners("onDisconnected", disconnectEvent);
        }
    };
    
    // Callback for discovery
    private final EndpointDiscoveryCallback endpointDiscoveryCallback =
        new EndpointDiscoveryCallback() {
        @Override
        public void onEndpointFound(String endpointId, DiscoveredEndpointInfo info) {
            Log.d(TAG, "Endpoint found: " + endpointId + ", name: " + info.getEndpointName());
            
            // Notify JS about the discovered endpoint
            JSObject endpoint = new JSObject();
            endpoint.put("endpointId", endpointId);
            endpoint.put("endpointName", info.getEndpointName());
            endpoint.put("serviceId", info.getServiceId());
            notifyListeners("onEndpointFound", endpoint);
        }

        @Override
        public void onEndpointLost(String endpointId) {
            Log.d(TAG, "Endpoint lost: " + endpointId);
            
            // Notify JS about the lost endpoint
            JSObject endpoint = new JSObject();
            endpoint.put("endpointId", endpointId);
            notifyListeners("onEndpointLost", endpoint);
        }
    };
    
    // Callback for received payloads
    private final PayloadCallback payloadCallback = new PayloadCallback() {
        @Override
        public void onPayloadReceived(String endpointId, Payload payload) {
            if (payload.getType() == Payload.Type.BYTES) {
                byte[] receivedBytes = payload.asBytes();
                String receivedData = new String(receivedBytes, StandardCharsets.UTF_8);
                
                Log.d(TAG, "Payload received from endpoint: " + endpointId + ", data length: " + receivedData.length());
                
                // Notify JS about the received message
                JSObject message = new JSObject();
                message.put("endpointId", endpointId);
                message.put("data", receivedData);
                message.put("messageId", UUID.randomUUID().toString());
                notifyListeners("onMessageReceived", message);
            }
        }

        @Override
        public void onPayloadTransferUpdate(String endpointId, PayloadTransferUpdate update) {
            // For tracking transfer progress if needed
            if (update.getStatus() == PayloadTransferUpdate.Status.SUCCESS) {
                Log.d(TAG, "Payload transfer successful for endpoint: " + endpointId);
            }
        }
    };
}