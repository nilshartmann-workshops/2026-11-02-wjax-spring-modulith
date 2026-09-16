-- Stellt ein gescheitertes Event zur Laufzeit erneut zu, nachdem sein
-- Payload in der Datenbank korrigiert wurde.
--
-- Vorbedingung: eine Pflanze mit "location": "" anlegen (Swagger UI). Der
-- Listener in CareService scheitert dann an "location must not be blank".
-- Einzeln im Datenbank-Explorer ausführen.

-- 1) Die gescheiterte Publication ansehen, im Payload steht "location":""
SELECT id, listener_id, status, completion_attempts, serialized_event
FROM event_publication
WHERE status = 'FAILED';

-- 2) Den Payload korrigieren
UPDATE event_publication
SET serialized_event = REPLACE(serialized_event, '"location":""', '"location":"Schlafzimmer"')
WHERE status = 'FAILED';

-- 3) Ohne Neustart erneut zustellen, mit mehr Versuchen, als der Filter sonst erlaubt:
--      ./resubmit-publications.sh 10
--    Danach steht die Publication auf COMPLETED, und die Care-Tasks sind angelegt.
