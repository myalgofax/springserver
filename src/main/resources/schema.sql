-- Users table
CREATE TABLE IF NOT EXISTS users (
    id BIGSERIAL PRIMARY KEY,
    user_id VARCHAR(255) UNIQUE NOT NULL,
    first_name VARCHAR(255),
    last_name VARCHAR(255),
    email VARCHAR(255) UNIQUE NOT NULL,
    broker_code VARCHAR(255),
    password VARCHAR(255) NOT NULL,
    active BOOLEAN DEFAULT true,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Brokers table
CREATE TABLE IF NOT EXISTS brokers (
    broker_id BIGSERIAL PRIMARY KEY,
    phone_number VARCHAR(255),
    ucc VARCHAR(255),
    user_id VARCHAR(255) NOT NULL,
    username VARCHAR(255),
    broker_code VARCHAR(255),
    password TEXT,
    consumerkey TEXT,
    consumer_secret_key TEXT,
    active_inv VARCHAR(255),
    totp TEXT,
    neo_fin_key VARCHAR(255)
);

-- Executed Strategies table
CREATE TABLE IF NOT EXISTS com_executed_strategies (
    id BIGSERIAL PRIMARY KEY,
    user_id VARCHAR(255) NOT NULL,
    name VARCHAR(255) NOT NULL,
    description TEXT,
    category VARCHAR(255),
    risk VARCHAR(255),
    active BOOLEAN DEFAULT true,
    capital DECIMAL(15,2),
    positions INTEGER,
    pnl DECIMAL(15,2) DEFAULT 0.0,
    status VARCHAR(255),
    executed_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    lot_size INTEGER,
    underlying VARCHAR(255),
    symbol VARCHAR(255),
    legs JSONB
);

-- Scrip Master table
CREATE TABLE IF NOT EXISTS scrip_master (
    id BIGSERIAL PRIMARY KEY,
    p_symbol_name VARCHAR(255),
    p_symbol DECIMAL(15,2),
    p_trd_symbol VARCHAR(255),
    p_option_type VARCHAR(255),
    description TEXT,
    d_strike_price DECIMAL(15,2),
    p_segment VARCHAR(255),
    p_exchange VARCHAR(255),
    d_high_price_range DECIMAL(15,2),
    d_open_interest DECIMAL(15,2),
    d_low_price_range DECIMAL(15,2)
);

-- Indexes for better performance
CREATE INDEX IF NOT EXISTS idx_users_user_id ON users(user_id);
CREATE INDEX IF NOT EXISTS idx_users_email ON users(email);
CREATE INDEX IF NOT EXISTS idx_brokers_user_id ON brokers(user_id);
CREATE INDEX IF NOT EXISTS idx_executed_strategies_user_id ON com_executed_strategies(user_id);
CREATE INDEX IF NOT EXISTS idx_scrip_master_symbol ON scrip_master(p_symbol);