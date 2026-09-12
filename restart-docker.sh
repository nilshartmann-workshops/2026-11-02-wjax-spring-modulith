#!/bin/bash
#
# Setzt Postgres zurück: Container und Volume weg, dann neu starten. Danach
# die Anwendung neu starten, denn Flyway legt die Tabellen nur beim Start an.
# Eine laufende Anwendung verbindet sich zwar wieder, findet aber eine leere
# Datenbank ohne Tabellen vor. Kafka und Zipkin bleiben unberührt.

set -e
cd "$(dirname "$0")"

docker compose rm --stop --force --volumes postgres
docker compose up -d postgres
echo "Postgres läuft wieder leer. Jetzt die Anwendung neu starten."
