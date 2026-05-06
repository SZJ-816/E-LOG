CREATE DATABASE IF NOT EXISTS log_analysis DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE log_analysis;

CREATE TABLE IF NOT EXISTS log_stats (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    stat_date DATE NOT NULL,
    stat_hour INT NOT NULL,
    pv BIGINT DEFAULT 0,
    uv BIGINT DEFAULT 0,
    error_count BIGINT DEFAULT 0,
    avg_response_time DOUBLE DEFAULT 0,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    UNIQUE KEY uk_date_hour (stat_date, stat_hour)
);

CREATE TABLE IF NOT EXISTS api_stats (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    stat_date DATE NOT NULL,
    api_path VARCHAR(191) NOT NULL,
    call_count BIGINT DEFAULT 0,
    avg_time DOUBLE DEFAULT 0,
    error_count BIGINT DEFAULT 0,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    UNIQUE KEY uk_date_api (stat_date, api_path)
);

CREATE TABLE IF NOT EXISTS error_log (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    timestamp DATETIME NOT NULL,
    log_level VARCHAR(20) NOT NULL DEFAULT 'ERROR',
    service_name VARCHAR(100),
    message TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_timestamp (timestamp),
    INDEX idx_level (log_level)
);

CREATE TABLE IF NOT EXISTS system_health (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    record_time DATETIME NOT NULL,
    status VARCHAR(20) DEFAULT 'HEALTHY',
    cpu_usage DOUBLE DEFAULT 0,
    memory_usage DOUBLE DEFAULT 0,
    disk_usage DOUBLE DEFAULT 0,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_record_time (record_time)
);

INSERT IGNORE INTO log_stats (stat_date, stat_hour, pv, uv, error_count, avg_response_time) VALUES
(CURDATE(), 0, 12500, 3200, 12, 145.5),
(CURDATE(), 1, 8900, 2100, 8, 132.3),
(CURDATE(), 2, 5600, 1500, 5, 128.7),
(CURDATE(), 3, 3200, 800, 3, 135.2),
(CURDATE(), 4, 2800, 700, 2, 140.1),
(CURDATE(), 5, 4500, 1200, 4, 138.9),
(CURDATE(), 6, 8900, 2300, 7, 125.6),
(CURDATE(), 7, 15600, 4100, 15, 118.3),
(CURDATE(), 8, 28900, 7500, 22, 105.7),
(CURDATE(), 9, 35200, 9200, 28, 98.4),
(CURDATE(), 10, 41500, 10800, 35, 92.1),
(CURDATE(), 11, 38700, 10100, 31, 95.8),
(CURDATE(), 12, 42300, 11200, 38, 88.5),
(CURDATE(), 13, 39800, 10400, 33, 91.2),
(CURDATE(), 14, 36100, 9500, 29, 96.7),
(CURDATE(), 15, 33400, 8800, 25, 102.3),
(CURDATE(), 16, 29800, 7800, 21, 108.9),
(CURDATE(), 17, 25600, 6700, 18, 115.4),
(CURDATE(), 18, 31200, 8200, 24, 110.2),
(CURDATE(), 19, 28900, 7600, 20, 112.8),
(CURDATE(), 20, 24500, 6400, 16, 119.5),
(CURDATE(), 21, 19800, 5200, 13, 125.7),
(CURDATE(), 22, 15200, 4000, 10, 130.4),
(CURDATE(), 23, 11800, 3100, 8, 138.2);

INSERT IGNORE INTO api_stats (stat_date, api_path, call_count, avg_time, error_count) VALUES
(CURDATE(), '/api/v1/users', 45230, 45.2, 12),
(CURDATE(), '/api/v1/orders', 38910, 78.5, 23),
(CURDATE(), '/api/v1/products', 32150, 32.1, 8),
(CURDATE(), '/api/v1/payments', 28760, 125.3, 45),
(CURDATE(), '/api/v1/auth/login', 25430, 89.7, 15),
(CURDATE(), '/api/v1/search', 19870, 156.8, 32),
(CURDATE(), '/api/v1/cart', 16540, 52.3, 7),
(CURDATE(), '/api/v1/reviews', 12380, 67.9, 11);

INSERT IGNORE INTO error_log (timestamp, log_level, service_name, message) VALUES
(NOW() - INTERVAL 5 MINUTE, 'ERROR', 'payment-service', 'Connection timeout to payment gateway'),
(NOW() - INTERVAL 12 MINUTE, 'ERROR', 'order-service', 'Database deadlock detected on order creation'),
(NOW() - INTERVAL 18 MINUTE, 'WARN', 'api-gateway', 'Rate limit exceeded for client IP 192.168.1.100'),
(NOW() - INTERVAL 25 MINUTE, 'ERROR', 'user-service', 'Failed to decode JWT token: expired'),
(NOW() - INTERVAL 32 MINUTE, 'ERROR', 'search-service', 'Elasticsearch cluster health check failed'),
(NOW() - INTERVAL 40 MINUTE, 'WARN', 'cart-service', 'Redis connection pool exhausted, retrying'),
(NOW() - INTERVAL 48 MINUTE, 'ERROR', 'notification-service', 'SMTP connection refused: email server unreachable'),
(NOW() - INTERVAL 55 MINUTE, 'ERROR', 'order-service', 'Inventory service returned 503 Service Unavailable'),
(NOW() - INTERVAL 65 MINUTE, 'WARN', 'api-gateway', 'Upstream service response time exceeded 5s threshold'),
(NOW() - INTERVAL 72 MINUTE, 'ERROR', 'payment-service', 'Invalid payment callback signature detected');

INSERT IGNORE INTO system_health (record_time, status, cpu_usage, memory_usage, disk_usage) VALUES
(NOW(), 'HEALTHY', 42.5, 58.3, 45.2);
