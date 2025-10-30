-- Database schema for HolySync Chat App
-- PostgreSQL Database Setup

-- Create database (run this as superuser)
-- CREATE DATABASE chat_app;
-- CREATE USER chat_user WITH PASSWORD 'chat123';
-- GRANT ALL PRIVILEGES ON DATABASE chat_app TO chat_user;

-- Connect to chat_app database and run the following:

-- Create users table
CREATE TABLE IF NOT EXISTS users (
    id SERIAL PRIMARY KEY,
    username VARCHAR(50) UNIQUE NOT NULL,
    email VARCHAR(100) UNIQUE NOT NULL,
    password VARCHAR(255) NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Create index for faster username lookups
CREATE INDEX IF NOT EXISTS idx_users_username ON users(username);

-- Create index for faster email lookups
CREATE INDEX IF NOT EXISTS idx_users_email ON users(email);

-- Grant permissions to chat_user
GRANT ALL PRIVILEGES ON TABLE users TO chat_user;
GRANT USAGE, SELECT ON SEQUENCE users_id_seq TO chat_user;
