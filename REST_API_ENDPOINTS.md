# AlgoFax Trading Engine REST API Documentation

## Authentication
All endpoints require JWT authentication via `Authorization: Bearer <token>` header.

### Login Endpoint
```http
POST /api/auth/login
Content-Type: application/json

{
  "username": "trader@algofax.com",
  "password": "password123"
}
```
**Response:**
```json
{
  "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "expiresIn": 3600,
  "role": "TRADER"
}
```

---

## 1. Strategy Management

### Get Available Strategy Templates
```http
GET /api/strategies
Authorization: Bearer <token>
```
**Response:**
```json
[
  {
    "name": "IRON_CONDOR",
    "description": "Iron Condor options strategy",
    "parameters": {
      "strikeWidth": 100,
      "daysToExpiry": 30,
      "maxCapital": 10000
    }
  },
  {
    "name": "BULL_CALL_SPREAD",
    "description": "Bull Call Spread strategy",
    "parameters": {
      "strikeWidth": 50,
      "daysToExpiry": 21,
      "maxCapital": 5000
    }
  }
]
```

### Deploy New Strategy Instance
```http
POST /api/strategies/instances
Authorization: Bearer <token>
Content-Type: application/json

{
  "strategyType": "IRON_CONDOR",
  "symbol": "NIFTY",
  "parameters": {
    "strikeWidth": 100,
    "daysToExpiry": 30,
    "rsiThreshold": 70
  },
  "maxCapital": 10000,
  "riskLimit": 0.02
}
```
**Response:**
```json
{
  "id": "IRON_CONDOR_1703123456789",
  "name": "Iron Condor NIFTY",
  "symbol": "NIFTY",
  "status": "RUNNING",
  "currentPnl": 0.0,
  "unrealizedPnl": 0.0,
  "realizedPnl": 0.0,
  "startedAt": "2024-01-15T10:30:00",
  "currentGreeks": {
    "delta": 0.0,
    "gamma": 0.0,
    "theta": 0.0,
    "vega": 0.0
  },
  "config": {
    "strategyType": "IRON_CONDOR",
    "symbol": "NIFTY",
    "parameters": {
      "strikeWidth": 100,
      "daysToExpiry": 30
    },
    "maxCapital": 10000,
    "riskLimit": 0.02
  },
  "openPositions": []
}
```

### Get All Running Strategies
```http
GET /api/strategies/instances
Authorization: Bearer <token>
```
**Response:** Array of strategy instances (same format as deploy response)

### Get Specific Strategy Details
```http
GET /api/strategies/instances/{strategyId}
Authorization: Bearer <token>
```
**Response:** Single strategy instance object

### Update Strategy Configuration
```http
PUT /api/strategies/instances/{strategyId}/config
Authorization: Bearer <token>
Content-Type: application/json

{
  "parameters": {
    "rsiThreshold": 75,
    "strikeWidth": 120
  },
  "maxCapital": 15000
}
```
**Response:** Updated strategy instance object

### Pause Strategy
```http
POST /api/strategies/instances/{strategyId}/pause
Authorization: Bearer <token>
```
**Response:** `204 No Content`

### Resume Strategy
```http
POST /api/strategies/instances/{strategyId}/resume
Authorization: Bearer <token>
```
**Response:** `204 No Content`

### Shutdown Strategy
```http
DELETE /api/strategies/instances/{strategyId}
Authorization: Bearer <token>
```
**Response:** `204 No Content`

---

## 2. Performance & Monitoring

### Get Strategy Performance History
```http
GET /api/performance/strategy/{strategyId}?from=2024-01-01T00:00:00&to=2024-01-31T23:59:59
Authorization: Bearer <token>
```
**Response:**
```json
[
  {
    "timestamp": "2024-01-15T10:30:00Z",
    "equity": 101500.0,
    "dailyPnl": 1500.0,
    "strategyPerformance": {
      "IRON_CONDOR_1": 800.0,
      "BULL_CALL_2": 700.0
    }
  }
]
```

### Get Portfolio Performance Summary
```http
GET /api/performance/portfolio
Authorization: Bearer <token>
```
**Response:**
```json
{
  "totalPnL": 15750.0,
  "equity": 115750.0,
  "sharpeRatio": 1.85,
  "maxDrawdown": 0.08,
  "activeStrategies": 5,
  "winRate": 0.72,
  "profitFactor": 2.1
}
```

### Get Strategy Metrics
```http
GET /api/performance/metrics/{strategyId}
Authorization: Bearer <token>
```
**Response:**
```json
{
  "totalReturn": 0.15,
  "sharpeRatio": 1.65,
  "maxDrawdown": 0.05,
  "winRate": 0.68,
  "profitFactor": 1.8,
  "totalTrades": 45,
  "avgTradeReturn": 0.003
}
```

---

## 3. Dashboard

### Get Dashboard Overview
```http
GET /api/dashboard/overview
Authorization: Bearer <token>
```
**Response:**
```json
{
  "totalPnL": 15750.0,
  "equity": 115750.0,
  "sharpeRatio": 1.85,
  "activeStrategies": 5,
  "todaysPnL": 2150.0,
  "openPositions": 12,
  "systemStatus": "HEALTHY"
}
```

### Get Real-Time Metrics Stream
```http
GET /api/dashboard/real-time-metrics
Authorization: Bearer <token>
```
**Response:** Server-Sent Events stream with portfolio updates every second

### Get Active Alerts
```http
GET /api/dashboard/alerts
Authorization: Bearer <token>
```
**Response:**
```json
[
  {
    "id": "ALERT_1703123456789",
    "type": "RISK_ALERT",
    "message": "Portfolio VaR exceeded threshold",
    "severity": "HIGH",
    "timestamp": "2024-01-15T14:30:00",
    "data": {
      "currentVaR": 12500.0,
      "threshold": 10000.0
    }
  }
]
```

### Acknowledge Alert
```http
POST /api/dashboard/alerts/acknowledge/{alertId}
Authorization: Bearer <token>
```
**Response:** `204 No Content`

---

## 4. Risk Management

### Get Current Risk Exposure
```http
GET /api/risk/exposure
Authorization: Bearer <token>
```
**Response:**
```json
{
  "totalDelta": 2.5,
  "totalGamma": 0.8,
  "totalTheta": -45.0,
  "totalVega": 125.0,
  "portfolioVar": 8500.0
}
```

### Get VaR Metrics
```http
GET /api/risk/var
Authorization: Bearer <token>
```
**Response:**
```json
{
  "dailyVaR": 2500.0,
  "weeklyVaR": 5500.0,
  "confidence": 95.0,
  "portfolioValue": 115750.0,
  "utilizationPercent": 65.0
}
```

### Get Risk Limits
```http
GET /api/risk/limits
Authorization: Bearer <token>
```
**Response:**
```json
{
  "maxPortfolioVaR": 10000.0,
  "maxSingleStrategyRisk": 5000.0,
  "maxDrawdown": 0.15,
  "currentUtilization": 0.65,
  "maxLeverage": 2.0
}
```

### Update Risk Limits (Admin Only)
```http
PUT /api/risk/limits
Authorization: Bearer <admin_token>
Content-Type: application/json

{
  "maxPortfolioVaR": 12000.0,
  "maxSingleStrategyRisk": 6000.0,
  "maxDrawdown": 0.20
}
```
**Response:** `204 No Content`

---

## 5. Market Data

### Get Watched Symbols
```http
GET /api/market/symbols
Authorization: Bearer <token>
```
**Response:**
```json
["NIFTY", "BANKNIFTY", "RELIANCE", "TCS", "INFY"]
```

### Get Current Price
```http
GET /api/market/price/{symbol}
Authorization: Bearer <token>
```
**Response:**
```json
{
  "symbol": "NIFTY",
  "price": 18525.75,
  "change": 45.25,
  "changePercent": 0.24,
  "timestamp": "2024-01-15T15:30:00",
  "volume": 1250000
}
```

### Get Greeks Data
```http
GET /api/market/greeks/{symbol}
Authorization: Bearer <token>
```
**Response:**
```json
{
  "delta": 0.65,
  "gamma": 0.12,
  "theta": -2.5,
  "vega": 18.5,
  "rho": 0.08
}
```

### Get Volatility Data
```http
GET /api/market/volatility/{symbol}
Authorization: Bearer <token>
```
**Response:**
```json
{
  "symbol": "NIFTY",
  "impliedVolatility": 0.18,
  "historicalVolatility": 0.15,
  "volatilityRank": 75.5,
  "timestamp": "2024-01-15T15:30:00"
}
```

---

## 6. Order Management

### Get Recent Orders
```http
GET /api/orders/recent?limit=50
Authorization: Bearer <token>
```
**Response:**
```json
[
  {
    "orderId": "ORD_1703123456789",
    "strategyId": "IRON_CONDOR_1",
    "symbol": "NIFTY",
    "side": "BUY",
    "quantity": 50,
    "price": 18525.0,
    "status": "FILLED",
    "timestamp": "2024-01-15T14:25:00",
    "fillPrice": 18527.5,
    "commission": 25.0
  }
]
```

### Get Order Status
```http
GET /api/orders/status/{orderId}
Authorization: Bearer <token>
```
**Response:**
```json
{
  "orderId": "ORD_1703123456789",
  "status": "FILLED",
  "fillPrice": 18527.5,
  "fillQuantity": 50,
  "remainingQuantity": 0,
  "fillTime": "2024-01-15T14:25:30"
}
```

### Get Execution Quality Metrics
```http
GET /api/orders/execution-quality
Authorization: Bearer <token>
```
**Response:**
```json
{
  "avgSlippage": 0.02,
  "fillRate": 0.95,
  "avgLatency": 125.0,
  "implementationShortfall": 0.015,
  "period": "LAST_24H"
}
```

### Cancel Order
```http
POST /api/orders/cancel/{orderId}
Authorization: Bearer <token>
```
**Response:**
```json
{
  "orderId": "ORD_1703123456789",
  "status": "CANCELLED",
  "message": "Order cancelled successfully"
}
```

---

## 7. Meta-Strategy Management

### Rebalance Portfolio
```http
POST /api/meta-strategy/rebalance?totalCapital=100000
Authorization: Bearer <token>
```
**Response:**
```json
{
  "IRON_CONDOR_1": 0.25,
  "BULL_CALL_2": 0.30,
  "BEAR_PUT_3": 0.20,
  "STRADDLE_4": 0.25
}
```

### Get Current Allocations
```http
GET /api/meta-strategy/allocations
Authorization: Bearer <token>
```
**Response:**
```json
{
  "IRON_CONDOR_1": 0.25,
  "BULL_CALL_2": 0.30,
  "BEAR_PUT_3": 0.20,
  "STRADDLE_4": 0.25
}
```

### Generate New Strategies
```http
POST /api/meta-strategy/generate-strategies?numStrategies=10&backtestDays=252
Authorization: Bearer <token>
```
**Response:**
```json
[
  {
    "strategyId": "GENERATED_1703123456789",
    "indicators": ["RSI", "MACD"],
    "optionStructure": "IRON_CONDOR",
    "parameters": {
      "RSI_PERIOD": 14,
      "RSI_OVERSOLD": 30,
      "RSI_OVERBOUGHT": 70
    },
    "entryConditions": {
      "RSI_ENTRY": "RSI < 30 OR RSI > 70"
    },
    "exitConditions": {
      "PROFIT_TARGET": "PNL > 0.5 * MAX_PROFIT"
    }
  }
]
```

---

## 8. WebSocket Real-Time Updates

### Connect to Dashboard WebSocket
```javascript
const ws = new WebSocket('ws://localhost:8080/ws/dashboard');

ws.onmessage = function(event) {
  const data = JSON.parse(event.data);
  console.log('Received:', data);
};
```

**Message Types:**
- `STRATEGY_UPDATE`: Strategy state changes
- `PNL_UPDATE`: P&L updates
- `RISK_ALERT`: Risk threshold breaches
- `MARKET_DATA`: Price and volatility updates
- `ORDER_UPDATE`: Order execution updates

**Example WebSocket Message:**
```json
{
  "type": "PNL_UPDATE",
  "data": {
    "IRON_CONDOR_1": 1250.0,
    "BULL_CALL_2": 850.0
  },
  "timestamp": 1703123456789
}
```

---

## Error Responses

All endpoints return standard HTTP status codes:
- `200`: Success
- `201`: Created
- `204`: No Content
- `400`: Bad Request
- `401`: Unauthorized
- `403`: Forbidden
- `404`: Not Found
- `500`: Internal Server Error

**Error Response Format:**
```json
{
  "error": "VALIDATION_ERROR",
  "message": "Invalid strategy configuration",
  "details": {
    "field": "maxCapital",
    "reason": "Must be greater than 0"
  },
  "timestamp": "2024-01-15T15:30:00Z"
}
```

---

## Rate Limits
- **General API**: 1000 requests/minute
- **Real-time endpoints**: 100 requests/minute
- **WebSocket**: 1 connection per user

## Base URL
- **Development**: `http://localhost:8080`
- **Production**: `https://api.algofax.com`