DROP TABLE IF EXISTS scheduled_notification;
DROP TABLE IF EXISTS 'user';

CREATE TABLE IF NOT EXISTS 'user' (
    id BIGSERIAL PRIMARY KEY,
    email VARCHAR(255),
    birth_date DATE,
    platform VARCHAR(50) NOT NULL,                  -- ENUM(STRING)
    platform_user_id VARCHAR(255) NOT NULL,
    nickname VARCHAR(8),
    gender VARCHAR(50),                             -- ENUM(STRING)
    user_status VARCHAR(50) NOT NULL DEFAULT 'NEW', -- ENUM(STRING)
    couple_id BIGINT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT user_unique_idx_platform_user_id_when_not_deleted UNIQUE (platform_user_id)
);

CREATE TABLE IF NOT EXISTS scheduled_notification (
    id BIGSERIAL PRIMARY KEY,
    target_user_id BIGINT NOT NULL REFERENCES users(id),
    notification_type VARCHAR(50) NOT NULL,     -- ENUM(STRING)
    notify_at TIMESTAMP NOT NULL,               -- LocalDateTime 매핑
    title VARCHAR(255) NOT NULL,
    body VARCHAR(255) NOT NULL,
    image VARCHAR(255) NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);


