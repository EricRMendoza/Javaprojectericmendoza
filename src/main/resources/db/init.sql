-- resources/db/init.sql
DB_URL=jdbc:sqlite:/absolute/path/to/gcash.db
PRAGMA foreign_keys = ON;

CREATE TABLE IF NOT EXISTS users (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    name TEXT NOT NULL,
    email TEXT UNIQUE NOT NULL,
    number TEXT UNIQUE NOT NULL,
    pin TEXT NOT NULL
);

CREATE TABLE IF NOT EXISTS balance (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    amount REAL NOT NULL DEFAULT 0.0,
    user_ID INTEGER NOT NULL,
    FOREIGN KEY(user_ID) REFERENCES users(id)
);

CREATE TABLE IF NOT EXISTS transaction (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    amount REAL NOT NULL,
    name TEXT NOT NULL,
    account_ID INTEGER NOT NULL,
    date TEXT DEFAULT CURRENT_TIMESTAMP,
    transferFromID INTEGER,
    transferToID INTEGER,
    FOREIGN KEY(account_ID) REFERENCES users(id),
    FOREIGN KEY(transferFromID) REFERENCES users(id),
    FOREIGN KEY(transferToID) REFERENCES users(id)
);