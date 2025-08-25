# JWT Authentication Guide

## Overview
This microservice now supports JWT token-based authentication that bypasses the 2FA process. The 2FA will be handled by another microservice, and this service will work with the JWT token provided.

## Authentication Flow

### 1. JWT Login (No 2FA Required)
```bash
POST /api/auth/jwt-login
Content-Type: application/json

{
    "user_id": "your_user_id",
    "ucc": "your_ucc",
    "consumer_key": "your_consumer_key",
    "consumer_secret": "your_consumer_secret",
    "environment": "uat"
}
```

**Response:**
```json
{
    "access_token": "eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzI1NiJ9...",
    "token_type": "bearer",
    "user_id": "your_user_id",
    "ucc": "your_ucc",
    "environment": "uat"
}
```

### 2. Using the JWT Token
Include the token in the Authorization header for all subsequent requests:

```bash
Authorization: Bearer eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzI1NiJ9...
```

### 3. Example API Call
```bash
GET /api/portfolio/positions
Authorization: Bearer eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzI1NiJ9...
```

## Environment Variables
Set these in your `.env` file:

```env
JWT_SECRET=your-super-secret-jwt-key-change-in-production
PORT=8000
HOST=0.0.0.0
```

## Key Features
- **No 2FA Required**: JWT login bypasses TOTP/QR code authentication
- **Token-based**: Secure JWT tokens with expiration (24 hours default)
- **Client Caching**: Efficient client management per user
- **Render.com Compatible**: Proper port binding for deployment

## Endpoints

### Authentication
- `POST /api/auth/jwt-login` - Login with JWT (no 2FA)
- `GET /api/auth/verify` - Verify JWT token
- `POST /api/auth/logout` - Logout and clear cache

### Legacy 2FA (Still Available)
- `POST /api/auth/totp-login` - TOTP login
- `POST /api/auth/totp-validate` - TOTP validation
- `POST /api/auth/qr-link` - QR code link
- `POST /api/auth/qr-session` - QR session

All other endpoints remain the same and now require JWT authentication.