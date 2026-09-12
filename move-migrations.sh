#!/bin/bash
#
# Verschiebt die Migrationen in einen Ordner je Modul und zählt die
# Versionen darin neu. Flyway führt danach je Modul eine eigene
# Historientabelle. Die Migrationen von plant bleiben als Altbestand mit
# ihren alten Nummern in __root, das Spring Modulith vor allen Modulen
# ausführt.

set -e
cd "$(dirname "$0")/src/main/resources/db/migration"

if [ ! -f V1__create_owner.sql ]; then
  echo "Die Migrationen liegen schon je Modul, es gibt nichts zu verschieben."
  exit 0
fi

mkdir -p __root owner billing care

mv V1__create_owner.sql             owner/V1__create_owner.sql

mv V11__create_billing_schema.sql   billing/V1__create_billing_schema.sql
mv V12__add_usage_records_table.sql billing/V2__add_usage_records_table.sql
mv V13__add_invoices_table.sql      billing/V3__add_invoices_table.sql

mv V21__create_plant_schema.sql     __root/V21__create_plant_schema.sql
mv V22__add_plants_table.sql        __root/V22__add_plants_table.sql

mv V31__create_care_schema.sql      care/V1__create_care_schema.sql
mv V32__add_care_tasks_table.sql    care/V2__add_care_tasks_table.sql

echo "Migrationen liegen jetzt je Modul, plant als Altbestand in __root."
echo "Vor dem nächsten Start: ./mvnw clean, dann ./reset-db.sh (H2) oder ./restart-docker.sh (Postgres)"
