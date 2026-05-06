-- ============================================================
-- Momento database schema
-- Requires PostgreSQL with PostGIS and pgcrypto extensions.
-- Run once against a fresh database:
--   psql -d <dbname> -f schema.sql
-- ============================================================

CREATE EXTENSION IF NOT EXISTS postgis;
CREATE EXTENSION IF NOT EXISTS pgcrypto;

-- ------------------------------------------------------------
-- users
-- user_id is a UUID derived from the Firebase UID via
-- UUID.nameUUIDFromBytes("firebase:" + uid) in the backend.
-- This keeps Hibernate happy (UUID column <-> UUID Java type)
-- while still being stable and deterministic per Firebase user.
-- ------------------------------------------------------------
CREATE TABLE users (
  user_id          UUID        PRIMARY KEY,
  email            TEXT        NOT NULL UNIQUE,
  username         TEXT        NOT NULL,
  points_total     INTEGER     NOT NULL DEFAULT 0,
  dropped_count    INTEGER     NOT NULL DEFAULT 0,
  discovered_count INTEGER     NOT NULL DEFAULT 0,
  fcm_token        TEXT,
  auth_provider    TEXT        NOT NULL DEFAULT 'FIREBASE',
  created_at       TIMESTAMPTZ NOT NULL DEFAULT now()
);

-- ------------------------------------------------------------
-- capsules
-- latitude/longitude stored as plain columns alongside the
-- PostGIS geometry for easy JSON serialisation.
-- created_date (DATE) is a plain column used in the unique
-- index to avoid any function-call immutability issues.
-- ------------------------------------------------------------
CREATE TABLE capsules (
  capsule_id   UUID             PRIMARY KEY DEFAULT gen_random_uuid(),
  creator_id   UUID             NOT NULL REFERENCES users(user_id),
  location     geometry(Point, 4326) NOT NULL,
  latitude     DOUBLE PRECISION NOT NULL,
  longitude    DOUBLE PRECISION NOT NULL,
  text_content TEXT,
  content_type TEXT             NOT NULL,
  expiry_at    TIMESTAMPTZ      NOT NULL,
  created_at   TIMESTAMPTZ      NOT NULL DEFAULT now(),
  created_date DATE             NOT NULL DEFAULT CURRENT_DATE,
  status       TEXT             NOT NULL DEFAULT 'ACTIVE'
    CONSTRAINT capsules_status_check CHECK (status IN ('ACTIVE', 'EXPIRED', 'DELETED'))
);

-- ------------------------------------------------------------
-- media
-- ------------------------------------------------------------
CREATE TABLE media (
  media_id    UUID        PRIMARY KEY DEFAULT gen_random_uuid(),
  capsule_id  UUID        NOT NULL REFERENCES capsules(capsule_id) ON DELETE CASCADE,
  storage_key TEXT        NOT NULL UNIQUE,
  media_type  TEXT        NOT NULL
    CONSTRAINT media_type_check CHECK (media_type IN ('PHOTO', 'VIDEO', 'AUDIO')),
  size_bytes  BIGINT,
  uploaded_at TIMESTAMPTZ NOT NULL DEFAULT now()
);

-- ------------------------------------------------------------
-- discoveries
-- ------------------------------------------------------------
CREATE TABLE discoveries (
  discovery_id   UUID        PRIMARY KEY DEFAULT gen_random_uuid(),
  capsule_id     UUID        NOT NULL REFERENCES capsules(capsule_id) ON DELETE CASCADE,
  discoverer_id  UUID        NOT NULL REFERENCES users(user_id),
  discovered_at  TIMESTAMPTZ NOT NULL DEFAULT now(),
  points_awarded INTEGER     NOT NULL,
  UNIQUE (capsule_id, discoverer_id)
);

-- ------------------------------------------------------------
-- Indexes
-- ------------------------------------------------------------
CREATE INDEX idx_capsules_location_gist ON capsules USING GIST (location);
CREATE INDEX idx_capsules_creator       ON capsules (creator_id);
CREATE INDEX idx_capsules_expiry_status ON capsules (status, expiry_at);
CREATE INDEX idx_discoveries_discoverer ON discoveries (discoverer_id);
CREATE INDEX idx_media_capsule          ON media (capsule_id);

-- ------------------------------------------------------------
-- Duplicate-drop prevention
-- Plain columns only — no function calls, no immutability issue.
-- ROUND() on numeric is natively IMMUTABLE in PostgreSQL.
-- ------------------------------------------------------------
CREATE UNIQUE INDEX uq_same_creator_same_spot_24h
ON capsules (
  creator_id,
  ROUND(latitude::numeric,  5),
  ROUND(longitude::numeric, 5),
  created_date
);

-- ------------------------------------------------------------
-- Triggers: keep dropped_count / discovered_count in sync
-- ------------------------------------------------------------

CREATE OR REPLACE FUNCTION trg_increment_dropped_count()
RETURNS trigger AS $$
BEGIN
  UPDATE users SET dropped_count = dropped_count + 1
  WHERE user_id = NEW.creator_id;
  RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER after_capsule_insert
AFTER INSERT ON capsules
FOR EACH ROW EXECUTE FUNCTION trg_increment_dropped_count();


CREATE OR REPLACE FUNCTION trg_decrement_dropped_count()
RETURNS trigger AS $$
BEGIN
  UPDATE users SET dropped_count = GREATEST(dropped_count - 1, 0)
  WHERE user_id = OLD.creator_id;
  RETURN OLD;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER after_capsule_delete
AFTER DELETE ON capsules
FOR EACH ROW EXECUTE FUNCTION trg_decrement_dropped_count();


CREATE OR REPLACE FUNCTION trg_increment_discovered_count()
RETURNS trigger AS $$
BEGIN
  UPDATE users SET discovered_count = discovered_count + 1
  WHERE user_id = NEW.discoverer_id;
  RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER after_discovery_insert
AFTER INSERT ON discoveries
FOR EACH ROW EXECUTE FUNCTION trg_increment_discovered_count();