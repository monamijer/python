-- create_database.sql
-- Run this in phpMyAdmin (XAMPP) or via mysql CLI

CREATE DATABASE IF NOT EXISTS series_tv_db
    CHARACTER SET utf8mb4
    COLLATE utf8mb4_unicode_ci;

USE series_tv_db;

-- ============================================
-- Table: serie
-- ============================================
CREATE TABLE serie (
    id              BIGINT AUTO_INCREMENT PRIMARY KEY,
    titre           VARCHAR(150) NOT NULL,
    genre           VARCHAR(80),
    description     TEXT,
    annee_sortie    INT,
    note            DOUBLE,
    image_url       VARCHAR(500),
    tmdb_id         BIGINT,
    INDEX idx_serie_tmdb_id (tmdb_id)
) ENGINE=InnoDB;

-- ============================================
-- Table: saison
-- ============================================
CREATE TABLE saison (
    id              BIGINT AUTO_INCREMENT PRIMARY KEY,
    numero          INT NOT NULL,
    nb_episodes     INT,
    serie_id        BIGINT NOT NULL,
    CONSTRAINT fk_saison_serie
        FOREIGN KEY (serie_id) REFERENCES serie(id)
        ON DELETE CASCADE,
    -- A series cannot have two seasons with the same number
    CONSTRAINT uq_saison_serie_numero UNIQUE (serie_id, numero)
) ENGINE=InnoDB;

-- ============================================
-- Table: episode
-- ============================================
CREATE TABLE episode (
    id              BIGINT AUTO_INCREMENT PRIMARY KEY,
    numero          INT NOT NULL,
    titre           VARCHAR(200),
    duree_minutes   INT,
    saison_id       BIGINT NOT NULL,
    CONSTRAINT fk_episode_saison
        FOREIGN KEY (saison_id) REFERENCES saison(id)
        ON DELETE CASCADE,
    CONSTRAINT uq_episode_saison_numero UNIQUE (saison_id, numero)
) ENGINE=InnoDB;

-- ============================================
-- Table: utilisateur
-- ============================================
CREATE TABLE utilisateur (
    id              BIGINT AUTO_INCREMENT PRIMARY KEY,
    pseudo          VARCHAR(50) NOT NULL UNIQUE,
    email           VARCHAR(150) NOT NULL UNIQUE
) ENGINE=InnoDB;

-- ============================================
-- Table: visionnage (join table user <-> episode)
-- ============================================
CREATE TABLE visionnage (
    id                  BIGINT AUTO_INCREMENT PRIMARY KEY,
    utilisateur_id      BIGINT NOT NULL,
    episode_id          BIGINT NOT NULL,
    date_visionnage     DATETIME NOT NULL,
    CONSTRAINT fk_visionnage_utilisateur
        FOREIGN KEY (utilisateur_id) REFERENCES utilisateur(id)
        ON DELETE CASCADE,
    CONSTRAINT fk_visionnage_episode
        FOREIGN KEY (episode_id) REFERENCES episode(id)
        ON DELETE CASCADE,
    -- Prevents the same user from marking the same episode watched twice
    CONSTRAINT uq_visionnage_user_episode UNIQUE (utilisateur_id, episode_id)
) ENGINE=InnoDB;