# Kotak Neo API Documentation

## Base URL
```
http://localhost:8080/api/kotak
```

## Authentication Endpoints

### 1. JWT Login
**POST** `/login`

**Request:**
```json
{
  "user_id": "string",
  "ucc": "string",
  "consumer_key": "string",
  "consumer_secret": "string",
  "environment": "uat|prod"
}
```

**Response:**
```json
{
  "status": "success",
  "message": "JWT login successful",
  "data": {
    "access_token": "eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzI1NiJ9...",
    "token_type": "bearer",
    "user_id": "string",
    "ucc": "string"
  },
  "userToken": "eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzI1NiJ9...",
  "otpRequired": false
}
```

### 2. TOTP Login
**POST** `/totp-login`

**Request:**
```json
{
  "mobile_number": "+919876543210",
  "ucc": "string",
  "totp": "123456",
  "consumer_key": "string",
  "consumer_secret": "string",
  "environment": "uat|prod"
}
```

### 3. Validate TOTP
**POST** `/validate-totp`

**Request:**
```json
{
  "mpin": "1234"
}
```

### 4. Generate QR Code
**POST** `/qr-code`

**Request:**
```json
{
  "ucc": "string",
  "consumer_key": "string",
  "consumer_secret": "string",
  "environment": "uat|prod"
}
```

### 5. QR Session
**POST** `/qr-session`

**Request:**
```json
{
  "ott": "string",
  "ucc": "string"
}
```

### 6. Logout
**POST** `/logout`

**Request:** No body required

---

## Order Management Endpoints

### 1. Place Order
**POST** `/place-order`

**Request:**
```json
{
  "exchange_segment": "nse_cm|bse_cm|nse_fo|bse_fo|cde_fo",
  "product": "NRML|CNC|MIS|CO|BO",
  "price": "100.50",
  "order_type": "L|MKT|SL|SL-M",
  "quantity": "10",
  "validity": "DAY|IOC|GTC|EOS|GTD",
  "trading_symbol": "RELIANCE",
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
**PUT** `/modify-order`

**Request:**
```json
{
  "order_id": "string",
  "price": "100.50",
  "quantity": "10",
  "disclosed_quantity": "0",
  "trigger_price": "0",
  "validity": "DAY",
  "order_type": "L|MKT|SL|SL-M"
}
```

### 3. Cancel Order
**DELETE** `/cancel-order/{orderId}`

**Path Parameters:**
- `orderId`: string

### 4. Get Orders
**GET** `/orders`

### 5. Get Order History
**GET** `/order-history/{orderId}`

**Path Parameters:**
- `orderId`: string

---

## Portfolio Endpoints

### 1. Get Positions
**GET** `/positions`

### 2. Get Holdings
**GET** `/holdings`

### 3. Get Limits
**GET** `/limits`

**Query Parameters:**
- `segment`: CASH|CUR|FO|ALL (default: ALL)
- `exchange`: ALL|NSE|BSE (default: ALL)
- `product`: NRML|CNC|MIS|ALL (default: ALL)

**Example:**
```
GET /limits?segment=ALL&exchange=NSE&product=CNC
```

---

## Market Data Endpoints

### 1. Get Quotes
**POST** `/quotes`

**Request:**
```json
{
  "instrument_tokens": [
    {
      "instrument_token": "11536",
      "exchange_segment": "nse_cm"
    },
    {
      "instrument_token": "2885",
      "exchange_segment": "nse_cm"
    }
  ],
  "quote_type": "all|depth|ohlc|ltp|oi|52w|circuit_limits|scrip_details"
}
```

### 2. Get Instruments (Scrip Master)
**GET** `/instruments`

**Query Parameters:**
- `exchangeSegment`: nse_cm|bse_cm|nse_fo|bse_fo|cde_fo (optional)

**Example:**
```
GET /instruments?exchangeSegment=nse_cm
```

---

## Trade Endpoints

### 1. Get Trades
**GET** `/trades`

**Query Parameters:**
- `orderId`: string (optional)

**Example:**
```
GET /trades?orderId=12345
```

---

## WebSocket Management Endpoints

### 1. Unsubscribe Tokens
**POST** `/websocket/unsubscribe`

**Request:**
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

### 2. Disconnect Client
**POST** `/websocket/disconnect/{clientId}`

**Path Parameters:**
- `clientId`: string

### 3. WebSocket Status
**GET** `/websocket/status`

**Response:**
```json
{
  "status": "success",
  "data": {
    "active_connections": 5,
    "clients": ["client_1", "client_2"]
  }
}
```

### 4. Client Subscriptions
**GET** `/websocket/subscriptions/{clientId}`

**Path Parameters:**
- `clientId`: string

**Response:**
```json
{
  "status": "success",
  "data": {
    "client_id": "client_1",
    "subscribed_tokens": [
      {"instrument_token": "11536", "exchange_segment": "nse_cm"}
    ],
    "count": 1
  }
}
```

---

## Common Response Format

### Success Response
```json
{
  "status": "success",
  "message": "Operation successful",
  "data": {},
  "userToken": "string|null",
  "otpRequired": false
}
```

### Error Response
```json
{
  "status": "error",
  "message": "Error description",
  "data": null,
  "userToken": null,
  "otpRequired": false
}
```

---

## Popular Instrument Tokens
```json
{
  "RELIANCE": {"instrument_token": "11536", "exchange_segment": "nse_cm"},
  "TCS": {"instrument_token": "2885", "exchange_segment": "nse_cm"},
  "INFY": {"instrument_token": "1594", "exchange_segment": "nse_cm"},
  "HDFC_BANK": {"instrument_token": "4963", "exchange_segment": "nse_cm"},
  "ICICI_BANK": {"instrument_token": "1333", "exchange_segment": "nse_cm"}
}
```

---

### WebSocket Streaming

### WebSocket Connection
**WS** `/ws/kotak/{clientId}`

**WebSocket URL:**
```
ws://localhost:8080/ws/kotak/{clientId}
```

**Path Parameters:**
- `clientId`: string (unique identifier for your connection)

### Subscribe to Market Data
**Send Message:**
```json
{
  "action": "subscribe",
  "instrument_tokens": [
    {"instrument_token": "11536", "exchange_segment": "nse_cm"},
    {"instrument_token": "2885", "exchange_segment": "nse_cm"}
  ],
  "isIndex": false,
  "isDepth": false
}
```

**Receive Real-time Data:**
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

### JavaScript Example
```javascript
const ws = new WebSocket('ws://localhost:8080/ws/kotak/my_client_123');

ws.onopen = () => {
    // Subscribe to stocks
    ws.send(JSON.stringify({
        "action": "subscribe",
        "instrument_tokens": [
            {"instrument_token": "11536", "exchange_segment": "nse_cm"}
        ],
        "isIndex": false,
        "isDepth": false
    }));
};

ws.onmessage = (event) => {
    const data = JSON.parse(event.data);
    console.log('Real-time data:', data);
};
```

---

# Usage Examples

### Complete Authentication Flow
```bash
# 1. JWT Login
curl -X POST "http://localhost:8080/api/kotak/login" \
  -H "Content-Type: application/json" \
  -d '{
    "user_id": "your_user_id",
    "ucc": "your_ucc",
    "consumer_key": "your_key",
    "consumer_secret": "your_secret",
    "environment": "uat"
  }'

# 2. Place Order
curl -X POST "http://localhost:8080/api/kotak/place-order" \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer YOUR_TOKEN" \
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