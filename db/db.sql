CREATE DATABASE IF NOT EXISTS receipt_db;
USE receipt_db;

CREATE TABLE IF NOT EXISTS customers (
                                         id INT AUTO_INCREMENT PRIMARY KEY,
                                         name VARCHAR(100) NOT NULL,
    phone VARCHAR(20),
    email VARCHAR(100),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
    );

CREATE TABLE IF NOT EXISTS receipts (
                                        id INT AUTO_INCREMENT PRIMARY KEY,
                                        receipt_number VARCHAR(20) NOT NULL UNIQUE,
    customer_id INT,
    total_amount DECIMAL(10, 2) NOT NULL,
    payment_method VARCHAR(50),
    notes TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (customer_id) REFERENCES customers(id)
    );

CREATE TABLE IF NOT EXISTS receipt_items (
                                             id INT AUTO_INCREMENT PRIMARY KEY,
                                             receipt_id INT NOT NULL,
                                             item_name VARCHAR(100) NOT NULL,
    quantity INT NOT NULL,
    unit_price DECIMAL(10, 2) NOT NULL,
    total_price DECIMAL(10, 2) NOT NULL,
    FOREIGN KEY (receipt_id) REFERENCES receipts(id)
    );