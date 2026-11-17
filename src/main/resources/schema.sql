CREATE TABLE subdivision (
    id BIGINT PRIMARY KEY,
    parent_id BIGINT,
    country_code VARCHAR(2) NOT NULL,
    level INT NOT NULL,
    code VARCHAR(32),
    iso_code VARCHAR(32),
    name VARCHAR(255) NOT NULL,
    name_lower VARCHAR(255) NOT NULL,
    native_name VARCHAR(255),
    type VARCHAR(64),
    latitude DOUBLE PRECISION,
    longitude DOUBLE PRECISION,
    population BIGINT,
    timezone VARCHAR(64),
    wiki_data_id VARCHAR(32),
    capital VARCHAR(255),
    area VARCHAR(64),
    founding_date VARCHAR(32),
    languages VARCHAR(255),
    ethnicities VARCHAR(255),
    source VARCHAR(32) NOT NULL
);

CREATE INDEX idx_subdivision_country_level ON subdivision (country_code, level);
CREATE INDEX idx_subdivision_country_iso ON subdivision (country_code, iso_code);
CREATE INDEX idx_subdivision_parent ON subdivision (parent_id);
CREATE INDEX idx_subdivision_name ON subdivision (name_lower);

CREATE TABLE subdivision_translation (
    subdivision_id BIGINT NOT NULL,
    lang VARCHAR(16) NOT NULL,
    name VARCHAR(255) NOT NULL,
    PRIMARY KEY (subdivision_id, lang)
);

CREATE TABLE subdivision_leader (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    subdivision_id BIGINT NOT NULL,
    role VARCHAR(32) NOT NULL,
    name VARCHAR(255) NOT NULL,
    sector VARCHAR(255),
    appointment_date VARCHAR(32),
    image_url VARCHAR(1024)
);

CREATE INDEX idx_leader_subdivision ON subdivision_leader (subdivision_id);

CREATE TABLE city (
    id BIGINT PRIMARY KEY,
    subdivision_id BIGINT,
    country_code VARCHAR(2) NOT NULL,
    name VARCHAR(255) NOT NULL,
    name_lower VARCHAR(255) NOT NULL,
    latitude DOUBLE PRECISION,
    longitude DOUBLE PRECISION,
    population BIGINT,
    type VARCHAR(64)
);

CREATE INDEX idx_city_country ON city (country_code);
CREATE INDEX idx_city_subdivision ON city (subdivision_id);
CREATE INDEX idx_city_name ON city (name_lower);
