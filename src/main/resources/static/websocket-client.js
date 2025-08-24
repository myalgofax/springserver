/**
 * WebSocket Client for MyAlgoFax Application
 * Usage example for connecting to authenticated WebSocket endpoint
 */

class MyAlgoFaxWebSocketClient {
    constructor(serverUrl = 'ws://localhost:8080') {
        this.serverUrl = serverUrl;
        this.socket = null;
        this.connected = false;
        this.token = null;
        this.messageHandlers = {
            CHAT: [],
            NOTIFICATION: [],
            SYSTEM: [],
            ERROR: []
        };
    }

    /**
     * Connect to WebSocket with JWT token
     * @param {string} jwtToken - JWT token for authentication
     * @returns {Promise} - Resolves when connected, rejects on error
     */
    connect(jwtToken) {
        return new Promise((resolve, reject) => {
            if (this.socket) {
                this.socket.close();
            }

            this.token = jwtToken;
            this.socket = new WebSocket(`${this.serverUrl}/ws/chat`);

            this.socket.onopen = () => {
                console.log('WebSocket connection opened');
                
                // Send authentication message
                const authMessage = {
                    userToken: this.token,
                    message: "authenticate"
                };
                this.socket.send(JSON.stringify(authMessage));
            };

            this.socket.onmessage = (event) => {
                try {
                    const message = JSON.parse(event.data);
                    this.handleMessage(message);
                    
                    if (message.type === 'SYSTEM' && message.content === 'Connected successfully') {
                        this.connected = true;
                        resolve();
                    } else if (message.type === 'ERROR') {
                        this.connected = false;
                        reject(new Error(message.content));
                    }
                } catch (error) {
                    console.error('Error parsing message:', error);
                }
            };

            this.socket.onclose = () => {
                console.log('WebSocket connection closed');
                this.connected = false;
            };

            this.socket.onerror = (error) => {
                console.error('WebSocket error:', error);
                reject(error);
            };
        });
    }

    /**
     * Send a message through WebSocket
     * @param {string} message - Message to send
     */
    sendMessage(message) {
        if (!this.connected || !this.socket) {
            throw new Error('WebSocket not connected');
        }
        this.socket.send(message);
    }

    /**
     * Add message handler for specific message type
     * @param {string} type - Message type (CHAT, NOTIFICATION, SYSTEM, ERROR)
     * @param {function} handler - Handler function
     */
    onMessage(type, handler) {
        if (this.messageHandlers[type]) {
            this.messageHandlers[type].push(handler);
        }
    }

    /**
     * Handle incoming messages
     * @param {object} message - Parsed message object
     */
    handleMessage(message) {
        const handlers = this.messageHandlers[message.type] || [];
        handlers.forEach(handler => {
            try {
                handler(message);
            } catch (error) {
                console.error('Error in message handler:', error);
            }
        });
    }

    /**
     * Disconnect from WebSocket
     */
    disconnect() {
        if (this.socket) {
            this.socket.close();
            this.socket = null;
            this.connected = false;
        }
    }

    /**
     * Check if connected
     * @returns {boolean}
     */
    isConnected() {
        return this.connected;
    }
}

// Usage Example:
/*
const client = new MyAlgoFaxWebSocketClient();

// Set up message handlers
client.onMessage('CHAT', (message) => {
    console.log(`Chat from ${message.senderName}: ${message.content}`);
});

client.onMessage('NOTIFICATION', (message) => {
    console.log(`Notification: ${message.content}`);
});

client.onMessage('ERROR', (message) => {
    console.error(`Error: ${message.content}`);
});

// Connect with JWT token
client.connect('your-jwt-token-here')
    .then(() => {
        console.log('Connected successfully');
        client.sendMessage('Hello, WebSocket!');
    })
    .catch((error) => {
        console.error('Connection failed:', error);
    });
*/

// Export for Node.js environments
if (typeof module !== 'undefined' && module.exports) {
    module.exports = MyAlgoFaxWebSocketClient;
}