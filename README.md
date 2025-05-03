## Capacitor Nearby Connections Plugin
 
A Capacitor plugin that provides access to Google's Nearby Connections API for peer-to-peer mesh networking without an internet connection. This plugin enables device-to-device communication using Bluetooth, Wi-Fi, and a combination of both technologies.
## Features



- Peer-to-Peer Mesh Networking: Connect multiple devices in a mesh topology
- Multi-Transport Communication: Uses both Bluetooth and Wi-Fi for optimal connectivity
- Message Relaying: Automatically routes messages through intermediate devices
- Connection Status Tracking: Monitor connections, disconnections, and message delivery
- Supports Multiple Topologies: Star, Cluster, and Point-to-Point connection strategies
- Simple API: Easy-to-use JavaScript interface with Promises and event listeners



## Installation

``` 
npm install capacitor-nearby-plugin
npx cap sync

```



    
## Android Configuration
Add the required permissions to your AndroidManifest.xml:

```
<!-- Required for Nearby Connections -->
<uses-permission android:name="android.permission.ACCESS_WIFI_STATE" />
<uses-permission android:name="android.permission.CHANGE_WIFI_STATE" />
<uses-permission android:name="android.permission.INTERNET" />

<!-- Bluetooth permissions for devices running Android 11 (API 30) or lower -->
<uses-permission android:name="android.permission.BLUETOOTH" android:maxSdkVersion="30" />
<uses-permission android:name="android.permission.BLUETOOTH_ADMIN" android:maxSdkVersion="30" />

<!-- Location permissions -->
<uses-permission android:name="android.permission.ACCESS_COARSE_LOCATION" android:maxSdkVersion="28" />
<uses-permission android:name="android.permission.ACCESS_FINE_LOCATION" android:maxSdkVersion="31" />

<!-- Bluetooth permissions for devices running Android 12 (API 31) or higher -->
<uses-permission android:name="android.permission.BLUETOOTH_ADVERTISE" android:minSdkVersion="31" />
<uses-permission android:name="android.permission.BLUETOOTH_CONNECT" android:minSdkVersion="31" />
<uses-permission
    android:name="android.permission.BLUETOOTH_SCAN"
    android:minSdkVersion="31"
    android:usesPermissionFlags="neverForLocation"
    tools:targetApi="s" />

<!-- Wi-Fi permissions for Android 13+ -->
<uses-permission
    android:name="android.permission.NEARBY_WIFI_DEVICES"
    android:minSdkVersion="32"
    android:usesPermissionFlags="neverForLocation"
    tools:targetApi="s" />

<!-- Feature declarations -->
<uses-feature android:name="android.hardware.bluetooth" android:required="false" />
<uses-feature android:name="android.hardware.bluetooth_le" android:required="false" />

```


## Configuration

```
// In capacitor.config.ts
import { CapacitorConfig } from '@capacitor/cli';
import { Strategy } from 'capacitor-nearby-plugin';

const config: CapacitorConfig = {
  plugins: {
    NearbyConnections: {
      serviceName: 'com.your.app',
      strategy: Strategy.P2P_CLUSTER, // P2P_CLUSTER, P2P_STAR, or P2P_POINT_TO_POINT
      connectionType: 'balanced', // 'balanced', 'disruptive', or 'nonDisruptive'
      lowPower: false,
      autoConnect: true,
      deviceName: 'My Device' // Optional - defaults to device model
    }
  }
};

export default config;

```


## API Reference

Import the plugin

```http
import { NearbyConnections, Strategy } from 'capacitor-nearby-plugin';
```




## Methods
Initialize the plugin

```js
await NearbyConnections.initialize({
  serviceName: 'com.your.app',
  strategy: Strategy.P2P_CLUSTER
});

```
## Parameters:

- serviceName (string): Unique identifier for your service (recommended to use your app package name)
- strategy (Strategy): Connection strategy to use (P2P_CLUSTER, P2P_STAR, or P2P_POINT_TO_POINT)
## Check and request permissions

```
// Check current permissions
const permStatus = await NearbyConnections.checkPermissions();

// Request permissions if needed
if (needsPermissions(permStatus)) {
  await NearbyConnections.requestPermissions({
    permissions: ['nearby']
  });
}

```
## Start advertising

```
await NearbyConnections.startAdvertising({
  name: 'My Device Name'
});

```
## Parameters

- name (string, optional): Human-readable name for this device
## Start discovery

```
await NearbyConnections.startDiscovery();
```
## Request a connection
```
await NearbyConnections.requestConnection({
  endpointId: 'discovered-endpoint-id',
  name: 'My Device Name'
});
```

#### Parameters:

- endpointId (string): ID of the endpoint to connect to
- name (string, optional): Name to show to the other device

### Accept a connection

```
await NearbyConnections.acceptConnection({
  endpointId: 'requesting-endpoint-id'
});

```

#### Parameters:

- endpointId (string): ID of the endpoint to accept

### Reject a connection

```
await NearbyConnections.rejectConnection({
  endpointId: 'requesting-endpoint-id'
});
```
#### Parameters:

- endpointId (string): ID of the endpoint to reject

### Send a message

```
const result = await NearbyConnections.sendMessage({
  endpointId: 'connected-endpoint-id',
  data: 'Hello world!'
});
```

- endpointId (string): ID of the endpoint to send to
- data (string): Message content

#### Returns:
- messageId (string): Unique identifier for the sent message

### Disconnect from an endpoint

```
await NearbyConnections.disconnect({
  endpointId: 'connected-endpoint-id'
});

```
#### Parameters:
- endpointId (string): ID of the endpoint to disconnect from

### Get plugin status

```
const status = await NearbyConnections.getStatus();
```

#### Returns:
- isAdvertising (boolean): Whether advertising is active
- isDiscovering (boolean): Whether discovery is active
- connectedEndpoints (string[]): Array of connected endpoint IDs

### Stop advertising

```
await NearbyConnections.stopAdvertising();
```
### Stop discovery

```
await NearbyConnections.stopDiscovery();
```

## Events
Register event listeners

```
// Device discovery events
NearbyConnections.addListener('onEndpointFound', (endpoint) => {
  console.log('Endpoint found:', endpoint);
});

NearbyConnections.addListener('onEndpointLost', (endpoint) => {
  console.log('Endpoint lost:', endpoint);
});

// Connection events
NearbyConnections.addListener('onConnectionInitiated', (info) => {
  console.log('Connection initiated:', info);
});

NearbyConnections.addListener('onConnectionResult', (result) => {
  console.log('Connection result:', result);
});

NearbyConnections.addListener('onDisconnected', (endpoint) => {
  console.log('Disconnected from:', endpoint);
});

// Message events
NearbyConnections.addListener('onMessageReceived', (message) => {
  console.log('Message received:', message);
});

```
## Remove listeners

```
// Remove all listeners
NearbyConnections.removeAllListeners();

// Or remove a specific listener
const listener = await NearbyConnections.addListener('onEndpointFound', ...);
listener.remove();
```
## Complete Usage Example

Here's a complete example of using the plugin to create a mesh network:

```
import { NearbyConnections, Strategy } from 'capacitor-nearby-plugin';

class MeshNetworkService {
  constructor() {
    this.initialized = false;
    this.endpoints = new Map();
    this.deviceName = 'Device-' + Math.random().toString(36).substring(2, 8);
  }

  async initialize() {
    if (this.initialized) return;

    try {
      // Check permissions
      const permStatus = await NearbyConnections.checkPermissions();
      
      if (this.needsPermissions(permStatus)) {
        await NearbyConnections.requestPermissions({
          permissions: ['nearby']
        });
      }
      
      // Initialize the plugin
      await NearbyConnections.initialize({
        serviceName: 'com.example.meshapp',
        strategy: Strategy.P2P_CLUSTER
      });
      
      // Set up event listeners
      this.setupListeners();
      
      this.initialized = true;
      console.log('Mesh network initialized');
      
      // Start advertising and discovery
      await this.startAdvertising();
      await this.startDiscovery();
      
      return true;
    } catch (error) {
      console.error('Failed to initialize:', error);
      throw error;
    }
  }
  
  needsPermissions(status) {
    return Object.values(status).some(s => s !== 'granted');
  }
  
  setupListeners() {
    // Handle endpoint discovery
    NearbyConnections.addListener('onEndpointFound', (endpoint) => {
      console.log('Endpoint found:', endpoint);
      this.endpoints.set(endpoint.endpointId, endpoint);
    });
    
    NearbyConnections.addListener('onEndpointLost', (endpoint) => {
      console.log('Endpoint lost:', endpoint);
      this.endpoints.delete(endpoint.endpointId);
    });
    
    // Handle connection events
    NearbyConnections.addListener('onConnectionInitiated', (info) => {
      console.log('Connection initiated:', info);
      // Auto-accept connections
      NearbyConnections.acceptConnection({ endpointId: info.endpointId });
    });
    
    NearbyConnections.addListener('onConnectionResult', (result) => {
      console.log('Connection result:', result);
    });
    
    NearbyConnections.addListener('onDisconnected', (endpoint) => {
      console.log('Endpoint disconnected:', endpoint);
    });
    
    // Handle messages
    NearbyConnections.addListener('onMessageReceived', (message) => {
      console.log('Message received:', message);
      
      // Process the message...
      const data = JSON.parse(message.data);
      
      // Handle message based on content
    });
  }
  
  async startAdvertising() {
    await NearbyConnections.startAdvertising({
      name: this.deviceName
    });
    console.log('Started advertising as:', this.deviceName);
  }
  
  async startDiscovery() {
    await NearbyConnections.startDiscovery();
    console.log('Started discovery');
  }
  
  async connectToEndpoint(endpointId) {
    try {
      await NearbyConnections.requestConnection({
        endpointId,
        name: this.deviceName
      });
      console.log('Connection requested to:', endpointId);
    } catch (error) {
      console.error('Failed to connect:', error);
      throw error;
    }
  }
  
  async sendMessage(endpointId, content) {
    try {
      const result = await NearbyConnections.sendMessage({
        endpointId,
        data: JSON.stringify({
          type: 'text',
          content,
          timestamp: new Date().toISOString()
        })
      });
      console.log('Message sent, ID:', result.messageId);
      return result;
    } catch (error) {
      console.error('Failed to send message:', error);
      throw error;
    }
  }
  
  async disconnect(endpointId) {
    await NearbyConnections.disconnect({ endpointId });
    console.log('Disconnected from:', endpointId);
  }
  
  async stop() {
    await NearbyConnections.stopAdvertising();
    await NearbyConnections.stopDiscovery();
    NearbyConnections.removeAllListeners();
    console.log('Mesh network stopped');
  }
}

export const meshNetworkService = new MeshNetworkService();

```
## Troubleshooting

### Common Issues

#### Missing Permissions
If you're seeing errors related to missing permissions, make sure you've added all the required permissions to your AndroidManifest.xml and requested them at runtime.

#### Connection Issues

- Make sure both devices have Bluetooth and Wi-Fi enabled
- Check that the devices are within range (typically 10 meters for Bluetooth)
- Verify you're using the same serviceName on both devices
- Try different connection strategies to see which works best for your use case

#### Message Delivery Issues

- Check the connection status using getStatus()
- Ensure the message size is not too large (keep under 100KB for better reliability)
- Use the message ID to track delivery status

## Debug Logging
To enable verbose logging, add this to your application:

```
typescript// Enable debug mode
await NearbyConnections.initialize({
  serviceName: 'com.your.app',
  strategy: Strategy.P2P_CLUSTER,
  debug: true  // Enable detailed logging
});
```

#### Limitations

- Maximum devices in a mesh: The recommended limit is around 8-10 devices for - optimal performance
- Message size: For reliable transfer, keep messages under 100KB
- Battery usage: Continuous advertising and discovery can impact battery life
- iOS support: Currently this plugin only supports Android; iOS implementation is - planned for future releases


## Contributing
Contributions are welcome! Please feel free to submit a Pull Request.

1. Fork the repository
1. Create your feature branch (git checkout -b feature/amazing-feature)
1. Commit your changes (git commit -m 'Add some amazing feature')
1. Push to the branch (git push origin feature/amazing-feature)
1. Open a Pull Request

## License
This project is licensed under the MIT License - see the LICENSE file for details.
Acknowledgements

Google's Nearby Connections API
Capacitor team for the excellent plugin API
All contributors to this project


Built with ❤️ by Xettri Aleen