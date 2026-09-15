-- V2__add_landlord_id_to_units.sql
-- Adds landlord_id column to units table if it doesn't already exist.

ALTER TABLE units ADD COLUMN IF NOT EXISTS landlord_id UUID;