CREATE EXTENSION IF NOT EXISTS postgis;
CREATE EXTENSION IF NOT EXISTS pgcrypto;

CREATE TABLE users (
  user_id UUID PRIMARY KEY,
  email TEXT NOT NULL UNIQUE,
  username TEXT NOT NULL,
  points_total INTEGER NOT NULL DEFAULT 0,
  auth_provider TEXT NOT NULL DEFAULT 'FIREBASE',
  created_at TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE TABLE capsules (
  capsule_id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  creator_id UUID NOT NULL REFERENCES users(user_id),
  location geometry(Point,4326) NOT NULL,
  text_content TEXT,
  content_type TEXT NOT NULL,
  expiry_at TIMESTAMPTZ NOT NULL,
  created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
  status TEXT NOT NULL DEFAULT 'ACTIVE'
);

CREATE TABLE media (
  media_id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  capsule_id UUID NOT NULL REFERENCES capsules(capsule_id) ON DELETE CASCADE,
  storage_key TEXT NOT NULL UNIQUE,
  media_type TEXT NOT NULL,
  size_bytes BIGINT,
  uploaded_at TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE TABLE discoveries (
  discovery_id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  capsule_id UUID NOT NULL REFERENCES capsules(capsule_id) ON DELETE CASCADE,
  discoverer_id UUID NOT NULL REFERENCES users(user_id),
  discovered_at TIMESTAMPTZ NOT NULL DEFAULT now(),
  points_awarded INTEGER NOT NULL,
  UNIQUE(capsule_id, discoverer_id)
);

CREATE INDEX idx_capsules_location_gist ON capsules USING GIST (location);
CREATE INDEX idx_capsules_creator ON capsules (creator_id);
CREATE INDEX idx_capsules_expiry_status ON capsules (status, expiry_at);
CREATE INDEX idx_discoveries_discoverer ON discoveries (discoverer_id);

CREATE OR REPLACE FUNCTION rounded_coord(value double precision)
RETURNS numeric AS $$
SELECT round(value::numeric, 5);
$$ LANGUAGE sql IMMUTABLE;

CREATE UNIQUE INDEX uq_same_creator_same_spot_24h
ON capsules (creator_id, rounded_coord(ST_X(location::geometry)), rounded_coord(ST_Y(location::geometry)), date_trunc('day', created_at));
