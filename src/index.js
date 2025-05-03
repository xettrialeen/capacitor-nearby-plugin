import { registerPlugin } from '@capacitor/core';
import { NearbyConnectionsWeb } from './web.js';

const NearbyConnections = registerPlugin('NearbyConnections', {
  web: () => new NearbyConnectionsWeb(),
});

/**
 * Connection strategies for Nearby Connections
 */
export const Strategy = {
  /** P2P_CLUSTER: Allows mesh-like connections between multiple devices */
  P2P_CLUSTER: 'P2P_CLUSTER',
  /** P2P_STAR: One device acts as a hub with multiple connections */
  P2P_STAR: 'P2P_STAR',
  /** P2P_POINT_TO_POINT: Simple one-to-one connection */
  P2P_POINT_TO_POINT: 'P2P_POINT_TO_POINT'
};

export { NearbyConnections };