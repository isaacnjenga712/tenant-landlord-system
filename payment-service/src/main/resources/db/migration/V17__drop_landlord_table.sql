-- V17__drop_landlord_table.sql
-- Removes duplicate Landlord table from payment-service.
-- Canonical owner: property-management-service (landlords table).
-- payment-service stores landlord IDs as bare UUIDs on payments / invoices.

DROP TABLE IF EXISTS landlords CASCADE;