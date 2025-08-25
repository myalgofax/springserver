-- Migration script to add MPIN column to existing users table
-- Run this if you have an existing database

ALTER TABLE users ADD COLUMN IF NOT EXISTS mpin VARCHAR(255);