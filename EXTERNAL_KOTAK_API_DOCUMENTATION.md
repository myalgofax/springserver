# Kotak Neo API Documentation

## Base URL
```
http://localhost:8000
```

## Authentication
All endpoints except authentication routes require a valid session after TOTP/QR login.

### Authentication Methods
1. **TOTP Authentication** - Use mobile number, UCC, and TOTP from authenticator app
2. **QR Code Authentication** - Scan QR code with Kotak Neo mobile app

### How to Authenticate

#### Method 1: TOTP Flow (2-Step Process)
1. **Step 1:** Call `POST /api/auth/totp-login` with credentials → Creates session
2. **Step 2:** Call `POST /api/auth/totp-validate` with MPIN → **Completes 2FA**
3. **Step 3:** Use authenticated endpoints

#### Method 2: QR Code Flow (2-Step Process)
1. **Step 1:** Call `POST /api/auth/qr-link` to get QR code → Creates session
2. **Step 2:** Scan QR code with Kotak Neo mobile app
3. **Step 3:** Call `POST /api/auth/qr-session` with received OTT → **Completes 2FA**
4. **Step 4:** Use authenticated endpoints

⚠️ **IMPORTANT:** You MUST complete BOTH steps for each method. Step 1 alone is insufficient.

### Session Management
- Authentication creates a global session
- Session persists until logout or server restart
- All protected endpoints use the active session
- Call `POST /api/auth/logout` to terminate session

### Headers for Protected Endpoints
After successful authentication, all protected endpoints require:
```
Content-Type: application/json
Authorization: Bearer YOUR_ACCESS_TOKEN
```

**Note:** Current implementation uses global session state. In production, implement proper token-based authentication.

---

## 🔐 Authentication Endpoints

### 1. TOTP Login
**POST** `/api/auth/totp-login`

**Headers:**
```
Content-Type: application/json
```

**Request Body:**
```json
{
  "mobile_number": "string",
  "ucc": "string", 
  "totp": "string",
  "consumer_key": "string",
  "consumer_secret": "string",
  "environment": "uat"
}
```

### 2. TOTP Validate
**POST** `/api/auth/totp-validate`

**Headers:**
```
Content-Type: application/json
```

**Request Body:**
```json
{
  "mpin": "string"
}
```

**Note:** Requires active session from TOTP login

### 3. QR Code Link
**POST** `/api/auth/qr-link`

**Request Body:**
```json
{
  "ucc": "string",
  "consumer_key": "string", 
  "consumer_secret": "string",
  "environment": "uat"
}
```

**Note:** QR code flow doesn't require redirect URL. The QR code is scanned directly with Kotak Neo mobile app.

### 3a. QR Callback
**GET** `/api/auth/qr-callback`

**Query Parameters:**
- `ott`: string (optional) - One Time Token from QR scan

**Note:** This endpoint is for reference only. QR flow uses mobile app scanning, not web redirect.

### 4. QR Session
**POST** `/api/auth/qr-session`

**Request Body:**
```json
{
  "ott": "string",
  "ucc": "string"
}
```

### 5. Logout
**POST** `/api/auth/logout`

**Request Body:** None

**Response:**
```json
{
  "message": "Logged out successfully",
  "websockets_closed": true
}
```

**Note:** Automatically closes all active WebSocket connections

---

## 📈 Order Management Endpoints

### 1. Place Order
**POST** `/api/orders/place`

**Headers:**
```
Content-Type: application/json
```

**Authentication:** Required

**Request Body:**
```json
{
  "exchange_segment": "nse_cm|bse_cm|nse_fo|bse_fo|cde_fo",
  "product": "NRML|CNC|MIS|CO|BO",
  "price": "string",
  "order_type": "L|MKT|SL|SL-M",
  "quantity": "string",
  "validity": "DAY|IOC|GTC|EOS|GTD",
  "trading_symbol": "string",
  "transaction_type": "B|S",
  "amo": "YES|NO",
  "disclosed_quantity": "0",
  "market_protection": "0", 
  "pf": "N",
  "trigger_price": "0",
  "tag": "string"
}
```

### 2. Modify Order
**PUT** `/api/orders/modify`

**Request Body:**
```json
{
  "order_id": "string",
  "price": "string",
  "quantity": "string",
  "disclosed_quantity": "0",
  "trigger_price": "0",
  "validity": "DAY",
  "order_type": "L|MKT|SL|SL-M"
}
```

### 3. Cancel Order
**DELETE** `/api/orders/{order_id}`

**Path Parameters:**
- `order_id`: string

### 4. Get Orders
**GET** `/api/orders`

**Request Body:** None

### 5. Get Order History
**GET** `/api/orders/{order_id}/history`

**Path Parameters:**
- `order_id`: string

---

## 💼 Portfolio Endpoints

### 1. Get Positions
**GET** `/api/portfolio/positions`

**Headers:**
```
Content-Type: application/json
```

**Request Body:** None

**Authentication:** Required (must be authenticated first)

### 2. Get Holdings
**GET** `/api/portfolio/holdings`

**Headers:**
```
Content-Type: application/json
```

**Request Body:** None

**Authentication:** Required

### 3. Get Limits
**GET** `/api/portfolio/limits`

**Headers:**
```
Content-Type: application/json
```

**Query Parameters:**
- `segment`: CASH|CUR|FO|ALL (default: ALL)
- `exchange`: ALL|NSE|BSE (default: ALL)
- `product`: NRML|CNC|MIS|ALL (default: ALL)

**Request Body:** None

**Authentication:** Required

**Example:**
```bash
curl -X GET "http://localhost:8000/api/portfolio/limits?segment=ALL&exchange=NSE&product=CNC" \
  -H "Content-Type: application/json"
```

---

## 📊 Market Data Endpoints

### 1. Get Quotes
**POST** `/api/market/quotes`

**Request Body:**
```json
{
  "instrument_tokens": [
    {
      "instrument_token": "string",
      "exchange_segment": "nse_cm|bse_cm|nse_fo|bse_fo|cde_fo"
    }
  ],
  "quote_type": "all|depth|ohlc|ltp|oi|52w|circuit_limits|scrip_details"
}
```

### 2. Get Scrip Master
**GET** `/api/market/scrip-master`

**Query Parameters:**
- `exchange_segment`: nse_cm|bse_cm|nse_fo|bse_fo|cde_fo (optional)

---

## 📋 Trade Endpoints

### 1. Get Trades
**GET** `/api/trades`

**Query Parameters:**
- `order_id`: string (optional)

---

## 🌐 WebSocket Streaming

### WebSocket Connection
**WS** `/ws/{client_id}`

**WebSocket URL:**
```
ws://localhost:8000/ws/{client_id}
```

**Path Parameters:**
- `client_id`: string (unique identifier for your connection)

### Testing WebSocket in Postman

#### Method 1: Postman WebSocket Request
1. **Create New Request** → Select "WebSocket Request"
2. **URL:** `ws://localhost:8000/ws/test_client_123`
3. **Connect** → Click "Connect" button
4. **Send Messages:** Type JSON messages in message box
5. **Receive:** View real-time responses in message history

#### Method 2: Browser WebSocket Test
```javascript
// Open browser console and run:
const ws = new WebSocket('ws://localhost:8000/ws/test_client_123');

ws.onopen = function(event) {
    console.log('Connected to WebSocket');
    ws.send('Hello Server!');
};

ws.onmessage = function(event) {
    console.log('Received:', event.data);
};

ws.onclose = function(event) {
    console.log('WebSocket closed');
};
```

### WebSocket Message Formats

#### 1. Subscribe to Market Data (No Auth Needed)
**Note:** Use existing REST API session
**Send (NO COMMENTS in actual JSON):**
```json
{
  "action": "subscribe",
  "instrument_tokens": [
    {"instrument_token": "11536", "exchange_segment": "nse_cm"},
    {"instrument_token": "2885", "exchange_segment": "nse_cm"},
    {"instrument_token": "1594", "exchange_segment": "nse_cm"}
  ],
  "isIndex": false,
  "isDepth": false
}
```

**One-line format for wscat:**
```
{"action":"subscribe","instrument_tokens":[{"instrument_token":"11536","exchange_segment":"nse_cm"},{"instrument_token":"2885","exchange_segment":"nse_cm"}],"isIndex":false,"isDepth":false}
```

#### 2. Unsubscribe
**Send:**
```json
{
  "action": "unsubscribe",
  "instrument_tokens": [
    {
      "instrument_token": "11536",
      "exchange_segment": "nse_cm"
    }
  ]
}
```

#### 3. Subscribe to Order Feed
**Send:**
```json
{
  "action": "subscribe_orderfeed"
}
```

#### Receive Messages
**Connection:**
```json
{
  "type": "connection",
  "message": "Connected successfully"
}
```

**Market Data:**
```json
{
  "type": "market_data",
  "data": {
    "instrument_token": "11536",
    "ltp": "2500.50",
    "timestamp": "2024-01-01T10:30:00Z"
  }
}
```

**Error:**
```json
{
  "type": "error",
  "message": "Error description"
}
```

### WebSocket Testing Tools

#### 1. Postman (Recommended)
- ✅ Built-in WebSocket support
- ✅ Easy message sending/receiving
- ✅ Connection management

#### 2. WebSocket King (Chrome Extension)
- ✅ Simple WebSocket client
- ✅ Real-time testing

#### 3. wscat (Command Line)
```bash
# Install wscat
npm install -g wscat

# Connect to WebSocket
wscat -c ws://localhost:8000/ws/test_client_123

# Subscribe to popular stocks (NO COMMENTS in actual JSON)
{"action":"subscribe","instrument_tokens":[{"instrument_token":"11536","exchange_segment":"nse_cm"},{"instrument_token":"2885","exchange_segment":"nse_cm"},{"instrument_token":"1594","exchange_segment":"nse_cm"},{"instrument_token":"4963","exchange_segment":"nse_cm"},{"instrument_token":"1333","exchange_segment":"nse_cm"}],"isIndex":false,"isDepth":false}

# Subscribe to order feed
{"action":"subscribe_orderfeed"}

# Unsubscribe from all feeds
{"action":"unsubscribe_all"}

# Close WebSocket connection
{"action":"close"}
```

#### 4. curl (HTTP Upgrade)
```bash
curl --include \
     --no-buffer \
     --header "Connection: Upgrade" \
     --header "Upgrade: websocket" \
     --header "Sec-WebSocket-Key: SGVsbG8sIHdvcmxkIQ==" \
     --header "Sec-WebSocket-Version: 13" \
     http://localhost:8000/ws/test_client_123
```

### WebSocket Authentication Flow
**UPDATED:** WebSocket uses existing authenticated session from REST API.

**Correct Flow:**
1. **Authenticate via REST API** (TOTP/QR flow)
2. **Connect** to WebSocket
3. **Directly send subscribe messages** (no separate auth needed)
4. **Receive real-time market data**

**✅ Correct:** Authenticate via REST API first, then use WebSocket
**❌ Wrong:** Separate WebSocket authentication

### WebSocket Events
- `onopen`: Connection established
- `onmessage`: Receive data from server
- `onclose`: Connection closed
- `onerror`: Connection error

---

## 🌐 WebSocket Management Endpoints

### 1. Unsubscribe Tokens
**POST** `/api/websocket/unsubscribe`

**Headers:**
```
Content-Type: application/json
```

**Request Body:**
```json
{
  "client_id": "string",
  "instrument_tokens": [
    {
      "instrument_token": "11536",
      "exchange_segment": "nse_cm"
    }
  ]
}
```

**Use Case:** Page navigation - unsubscribe tokens when leaving a page

### 2. Disconnect Client
**POST** `/api/websocket/disconnect/{client_id}`

**Path Parameters:**
- `client_id`: string

**Use Case:** Force disconnect a specific WebSocket client

### 3. WebSocket Status
**GET** `/api/websocket/status`

**Response:**
```json
{
  "active_connections": 5,
  "clients": ["client_1", "client_2", "client_3"]
}
```

### 4. Client Subscriptions
**GET** `/api/websocket/subscriptions/{client_id}`

**Response:**
```json
{
  "client_id": "client_1",
  "subscribed_tokens": [
    {"instrument_token": "11536", "exchange_segment": "nse_cm"}
  ],
  "count": 1
}
```

---

## 🏥 Health Endpoints

### 1. Root
**GET** `/`

**Response:**
```json
{
  "message": "Kotak Neo Microservice Running"
}
```

### 2. Health Check
**GET** `/health`

**Response:**
```json
{
  "status": "healthy"
}
```

---

## 📝 Common Response Format

### Success Response
```json
{
  "status": "success",
  "data": {},
  "message": "string"
}
```

### Error Response
```json
{
  "detail": "Error message"
}
```

---

## ⚠️ Common Issues & Troubleshooting

### 401 Unauthorized Error
**Problem:** `INFO: 127.0.0.1:52330 - "GET /api/portfolio/limits HTTP/1.1" 401 Unauthorized`

**Cause:** Endpoint requires authentication but no valid session exists.

**Solution:**
1. **First authenticate** using TOTP or QR method
2. **Ensure authentication was successful** (check response)
3. **Then call protected endpoints**

### "Complete the 2fa process" Error
**Problem:** `{"Error Message": "Complete the 2fa process before accessing this application"}`

**Cause:** You only completed Step 1 of authentication. Kotak Neo requires 2-Factor Authentication (2FA).

**Solution - TOTP Method:**
1. ✅ **Step 1:** `POST /api/auth/totp-login` (you did this)
2. ❌ **Step 2:** `POST /api/auth/totp-validate` (you missed this)
3. **Step 3:** Call protected endpoints

**Solution - QR Method:**
1. ✅ **Step 1:** `POST /api/auth/qr-link` (you did this)
2. ❌ **Step 2:** `POST /api/auth/qr-session` (you missed this)
3. **Step 3:** Call protected endpoints

**⚠️ CRITICAL:** Both authentication methods require TWO API calls to complete 2FA.

### Invalid Redirect URL Error
**Problem:** `{"error": [{"code": "90413", "message": "Invalid redirect URL"}]}`

**Cause:** QR code flow requires redirect URL to be configured at API application level in Kotak Neo portal.

**Solutions:**
1. **Use TOTP Authentication instead** (recommended for API integration)
2. **Configure redirect URL in Kotak Neo developer portal**
3. **Contact Kotak Neo support** to whitelist your redirect URL

**Recommended: Use TOTP Flow:**
```bash
# Step 1: TOTP Login
curl -X POST "http://localhost:8000/api/auth/totp-login" \
  -H "Content-Type: application/json" \
  -d '{
    "mobile_number": "+919876543210",
    "ucc": "XVZAX",
    "totp": "123456",
    "consumer_key": "PYsI0ULDGQefPAfRsbp0GDwCTE4a",
    "consumer_secret": "LBhr5QwByT15x9v0qPgU_PDG4XUa",
    "environment": "prod"
  }'

# Step 2: TOTP Validate
curl -X POST "http://localhost:8000/api/auth/totp-validate" \
  -H "Content-Type: application/json" \
  -d '{"mpin": "1234"}'
```

### ⚠️ **TOTP Limitation:**
- **Manual Entry Required:** You must manually enter the 6-digit TOTP code from your authenticator app
- **Not Fully Automated:** Cannot be completely automated due to 2FA security requirements

### 🔄 **Automation Options:**

#### **Option 1: Long-lived Sessions**
- Authenticate once manually
- Keep session active for extended periods
- Re-authenticate only when session expires

#### **Option 2: Access Token (if available)**
```python
# If you have a long-lived access token
client = NeoAPI(
    access_token="YOUR_LONG_LIVED_TOKEN",
    environment="prod",
    base_url=base_url
)
```

#### **Option 3: Scheduled Authentication**
- Set up a scheduled task to authenticate daily
- Store session tokens securely
- Use stored tokens for API calls

### 🎯 **Recommended Approach:**
1. **Manual auth once per day** during market hours
2. **Store session tokens** securely
3. **Use stored tokens** for all API calls
4. **Re-authenticate** only when tokens expire

**Yes, TOTP requires manual entry of 6-digit codes, but can be minimized with proper session management.**

### WebSocket Errors
**Problem:** `{"type": "error", "message": "Unknown action"}`

**Cause:** Using wrong message format. WebSocket expects `action` field, not `type`.

**Solution:**
```json
// ❌ Wrong
{"type":"ping","data":"hello"}

// ✅ Correct
{"action":"authenticate","ucc":"YOUR_UCC","consumer_key":"KEY","consumer_secret":"SECRET"}
```

**Problem:** `{"type": "error", "message": "Invalid JSON"}`

**Cause:** JSON comments or invalid JSON format.

**Solution:**
```json
// ❌ Wrong - JSON doesn't support comments
{
  "action": "subscribe",
  "instrument_tokens": [
    {"instrument_token": "11536", "exchange_segment": "nse_cm"}, // RELIANCE
  ]
}

// ✅ Correct - Remove all comments
{"action":"subscribe","instrument_tokens":[{"instrument_token":"11536","exchange_segment":"nse_cm"},{"instrument_token":"2885","exchange_segment":"nse_cm"}],"isIndex":false,"isDepth":false}
```

### WebSocket Threading Error
**Problem:** `Exception in thread Thread-1 (start_websocket): ... self.on_open()`

**Cause:** Kotak Neo WebSocket library callback error during connection setup.

**Solution:**
1. **Ensure REST API authentication is complete first**
2. **Restart server** if callbacks are corrupted
3. **Try simple subscription** without complex callbacks

**Problem:** `Error in on_open callback: no running event loop`

**Cause:** WebSocket callbacks running in different thread without event loop.

**Solution:** Fixed using `asyncio.run_coroutine_threadsafe()` for cross-thread async calls.

**Testing Steps:**
```bash
# 1. Complete REST API auth first
curl -X POST "http://localhost:8000/api/auth/totp-login" ...
curl -X POST "http://localhost:8000/api/auth/totp-validate" ...

# 2. Then connect to WebSocket
wscat -c ws://localhost:8000/ws/test_client

# 3. Send simple subscription
{"action":"subscribe","instrument_tokens":[{"instrument_token":"11536","exchange_segment":"nse_cm"}],"isIndex":false,"isDepth":false}
```

### WebSocket Message Flow
1. **Authenticate via REST API:** Complete TOTP/QR authentication
2. **Connect:** `ws://localhost:8000/ws/client_123`
3. **Subscribe:** Send subscription message (no auth needed)
4. **Receive:** Get real-time market data/order updates

### Popular Stock Instrument Tokens
```json
{
  "RELIANCE": {"instrument_token": "11536", "exchange_segment": "nse_cm"},
  "TCS": {"instrument_token": "2885", "exchange_segment": "nse_cm"},
  "INFY": {"instrument_token": "1594", "exchange_segment": "nse_cm"},
  "HDFC_BANK": {"instrument_token": "4963", "exchange_segment": "nse_cm"},
  "ICICI_BANK": {"instrument_token": "1333", "exchange_segment": "nse_cm"},
  "NIFTY_50": {"instrument_token": "26000", "exchange_segment": "nse_cm"}
}
```

**Example Fix:**
```bash
# Step 1: Authenticate first
curl -X POST "http://localhost:8000/api/auth/totp-login" \
  -H "Content-Type: application/json" \
  -d '{"mobile_number":"+919876543210","ucc":"YOUR_UCC","totp":"123456","consumer_key":"KEY","consumer_secret":"SECRET"}'

# Step 2: Validate
curl -X POST "http://localhost:8000/api/auth/totp-validate" \
  -H "Content-Type: application/json" \
  -d '{"mpin":"1234"}'

# Step 3: Now call protected endpoint
curl -X GET "http://localhost:8000/api/portfolio/limits" \
  -H "Content-Type: application/json"
```

### Session State
- **Current Implementation:** Uses global session state
- **Session Expires:** On server restart or logout
- **Multiple Users:** Not supported (single global session)
- **WebSocket Clients:** Multiple clients supported with individual token tracking

### Page Navigation Pattern
**Common Use Case:** User navigates between pages frequently

**Recommended Flow:**
```javascript
// When leaving a page
fetch('/api/websocket/unsubscribe', {
  method: 'POST',
  headers: {'Content-Type': 'application/json'},
  body: JSON.stringify({
    client_id: 'user_page_1',
    instrument_tokens: [{"instrument_token": "11536", "exchange_segment": "nse_cm"}]
  })
});

// When entering new page
ws.send(JSON.stringify({
  action: 'subscribe',
  instrument_tokens: [{"instrument_token": "2885", "exchange_segment": "nse_cm"}]
}));
```

---

## 🔧 Environment Variables

```env
HOST=0.0.0.0
PORT=8000
KOTAK_CONSUMER_KEY=your_consumer_key
KOTAK_CONSUMER_SECRET=your_consumer_secret
KOTAK_ENVIRONMENT=uat|prod
```

---

## 📚 Usage Examples

### Complete Authentication Examples

#### TOTP Authentication Example
```bash
# Step 1: TOTP Login
curl -X POST "http://localhost:8000/api/auth/totp-login" \
  -H "Content-Type: application/json" \
  -d '{
    "mobile_number": "+919876543210",
    "ucc": "YOUR_UCC",
    "totp": "123456",
    "consumer_key": "YOUR_CONSUMER_KEY",
    "consumer_secret": "YOUR_CONSUMER_SECRET",
    "environment": "uat"
  }'

# Step 2: TOTP Validate
curl -X POST "http://localhost:8000/api/auth/totp-validate" \
  -H "Content-Type: application/json" \
  -d '{
    "mpin": "1234"
  }'
```

#### QR Code Authentication Example
```bash
# Step 1: Get QR Link
curl -X POST "http://localhost:8000/api/auth/qr-link" \
  -H "Content-Type: application/json" \
  -d '{
    "ucc": "YOUR_UCC",
    "consumer_key": "YOUR_CONSUMER_KEY",
    "consumer_secret": "YOUR_CONSUMER_SECRET",
    "environment": "uat"
  }'

# Step 2: Scan QR code with mobile app
# Step 3: Generate Session with OTT
curl -X POST "http://localhost:8000/api/auth/qr-session" \
  -H "Content-Type: application/json" \
  -d '{
    "ott": "RECEIVED_OTT_FROM_QR_SCAN",
    "ucc": "YOUR_UCC"
  }'
```

### Place Order Example
```bash
curl -X POST "http://localhost:8000/api/orders/place" \
  -H "Content-Type: application/json" \
  -d '{
    "exchange_segment": "nse_cm",
    "product": "CNC",
    "price": "100.50",
    "order_type": "L",
    "quantity": "10",
    "validity": "DAY",
    "trading_symbol": "RELIANCE",
    "transaction_type": "B"
  }'
```

### Complete WebSocket Example
```javascript
// Step 1: First authenticate via REST API (TOTP/QR flow)
// Step 2: Then connect to WebSocket

const ws = new WebSocket('ws://localhost:8000/ws/my_client_id');

// Handle connection
ws.onopen = () => {
    console.log('WebSocket Connected');
    
    // Directly subscribe to popular stocks (no separate auth needed)
    ws.send(JSON.stringify({
        "action": "subscribe",
        "instrument_tokens": [
            {"instrument_token": "11536", "exchange_segment": "nse_cm"}, // RELIANCE
            {"instrument_token": "2885", "exchange_segment": "nse_cm"},  // TCS
            {"instrument_token": "1594", "exchange_segment": "nse_cm"},  // INFY
            {"instrument_token": "4963", "exchange_segment": "nse_cm"},  // HDFC BANK
            {"instrument_token": "1333", "exchange_segment": "nse_cm"}   // ICICI BANK
        ],
        "isIndex": false,
        "isDepth": false
    }));
    
    // Subscribe to order feed
    ws.send(JSON.stringify({
        "action": "subscribe_orderfeed"
    }));
};

// Handle real-time messages
ws.onmessage = (event) => {
    const data = JSON.parse(event.data);
    console.log('Received:', data);
    
    if (data.type === 'market_data') {
        console.log('Stock Update:', data.data);
    } else if (data.type === 'subscription') {
        console.log('Subscribed to:', data.instruments);
    }
};

ws.onerror = (error) => {
    console.error('WebSocket Error:', error);
};

ws.onclose = () => {
    console.log('WebSocket Disconnected');
};
```