# Broker Data Encryption Utility

## Overview
This utility provides AES encryption with SHA-256 key derivation for securing sensitive broker information in the database.

## Files Created
- `CryptoUtils.java` - Main encryption/decryption utility
- `CryptoConfig.java` - Configuration for secret key management
- `BrokerSecurityService.java` - Service demonstrating usage
- Updated `Broker.java` entity with automatic encryption

## Usage

### Automatic Encryption in Broker Entity
The following fields are automatically encrypted/decrypted:
- `password`
- `consumerkey` 
- `consumerSecretKey`
- `totp`

```java
Broker broker = new Broker();
broker.setPassword("plainPassword"); // Automatically encrypted before saving
String decryptedPassword = broker.getPassword(); // Automatically decrypted when retrieved
```

### Manual Encryption/Decryption
```java
@Autowired
private CryptoUtils cryptoUtils;

// Encrypt sensitive data
String encrypted = cryptoUtils.encode("sensitive_data");

// Decrypt sensitive data  
String decrypted = cryptoUtils.decode(encrypted);
```

### Configuration
Set the encryption key in your environment variables:
```bash
export CRYPTO_SECRET_KEY="YourSecretKey2024!@#$%"
```

Or in `application-dev.properties`:
```properties
app.crypto.secret-key=${CRYPTO_SECRET_KEY:MyAlgoFaxSecretKey2024!@#$%}
```

## Security Features
- AES encryption algorithm
- SHA-256 key derivation
- Base64 encoding for database storage
- Configurable secret key via environment variables
- Automatic encryption/decryption in entity getters/setters

## Important Notes
- Keep your secret key secure and never commit it to version control
- Use environment variables for production deployments
- The utility handles null values gracefully
- All sensitive broker data is encrypted before database storage