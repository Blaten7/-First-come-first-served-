CREATE TABLE IF NOT EXISTS member
(
    user_id       BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_name     VARCHAR(255) NOT NULL,
    user_email    VARCHAR(255) NOT NULL UNIQUE,
    user_pw       VARCHAR(255) NOT NULL,
    user_address  VARCHAR(255),
    user_ph       VARCHAR(20),
    profile_img   VARCHAR(255),
    description   TEXT,
    status        VARCHAR(50),
    roles         TEXT,
    created_at    TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    pw_updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);
