#!/bin/bash
#
# Setzt die Datenbank zurück: Datei löschen, beim nächsten Start legt
# Flyway alles neu an. Die Anwendung muss dafür gestoppt sein, sonst ist
# die Datei gesperrt.

set -e
cd "$(dirname "$0")"

if [ ! -d data ]; then
  echo "Kein data/-Verzeichnis, es gibt nichts zurückzusetzen."
  exit 0
fi

rm -rf data
echo "Datenbank gelöscht. Beim nächsten Start legt Flyway sie neu an."
