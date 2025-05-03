import { WebPlugin } from '@capacitor/core';

export class NearbyConnectionsWeb extends WebPlugin {
  constructor() {
    super({
      name: 'NearbyConnections',
      platforms: ['web']
    });
  }

  /**
   * Initialize the Nearby Connections plugin
   * @param {Object} options - Initialization options
   * @param {string} options.serviceName - Unique identifier for your service
   * @param {string} options.strategy - Strategy to use (from Strategy enum)
   */
  async initialize(options) {
    console.warn('Nearby Connections API not available in the browser.');
    throw this.unavailable('Nearby Connections API not available in the browser.');
  }

  /**
   * Start advertising this device to nearby devices
   * @param {Object} options - Advertising options
   * @param {string} options.name - Human readable name for this device
   */
  async startAdvertising(options) {
    console.warn('Nearby Connections API not available in the browser.');
    throw this.unavailable('Nearby Connections API not available in the browser.');
  }

  /**
   * Stop advertising this device
   */
  async stopAdvertising() {
    console.warn('Nearby Connections API not available in the browser.');
    throw this.unavailable('Nearby Connections API not available in the browser.');
  }

  /**
   * Start discovering nearby devices
   * @param {Object} options - Discovery options
   */
  async startDiscovery(options) {
    console.warn('Nearby Connections API not available in the browser.');
    throw this.unavailable('Nearby Connections API not available in the browser.');
  }

  /**
   * Stop discovering nearby devices
   */
  async stopDiscovery() {
    console.warn('Nearby Connections API not available in the browser.');
    throw this.unavailable('Nearby Connections API not available in the browser.');
  }

  /**
   * Request a connection to a discovered endpoint
   * @param {Object} options - Connection options
   * @param {string} options.endpointId - ID of the endpoint to connect to
   * @param {string} options.name - Human readable name for this device
   */
  async requestConnection(options) {
    console.warn('Nearby Connections API not available in the browser.');
    throw this.unavailable('Nearby Connections API not available in the browser.');
  }

  /**
   * Accept a connection request from another device
   * @param {Object} options - Connection options
   * @param {string} options.endpointId - ID of the endpoint to accept
   */
  async acceptConnection(options) {
    console.warn('Nearby Connections API not available in the browser.');
    throw this.unavailable('Nearby Connections API not available in the browser.');
  }

  /**
   * Reject a connection request from another device
   * @param {Object} options - Connection options
   * @param {string} options.endpointId - ID of the endpoint to reject
   */
  async rejectConnection(options) {
    console.warn('Nearby Connections API not available in the browser.');
    throw this.unavailable('Nearby Connections API not available in the browser.');
  }

  /**
   * Send a message to a connected endpoint
   * @param {Object} options - Message options
   * @param {string} options.endpointId - ID of the endpoint to send to
   * @param {string} options.data - Data to send
   * @returns {Object} Message ID
   */
  async sendMessage(options) {
    console.warn('Nearby Connections API not available in the browser.');
    throw this.unavailable('Nearby Connections API not available in the browser.');
  }

  /**
   * Disconnect from an endpoint
   * @param {Object} options - Disconnect options
   * @param {string} options.endpointId - ID of the endpoint to disconnect from
   */
  async disconnect(options) {
    console.warn('Nearby Connections API not available in the browser.');
    throw this.unavailable('Nearby Connections API not available in the browser.');
  }

  /**
   * Get the current status of the plugin
   * @returns {Object} Status information
   */
  async getStatus() {
    console.warn('Nearby Connections API not available in the browser.');
    throw this.unavailable('Nearby Connections API not available in the browser.');
  }
}