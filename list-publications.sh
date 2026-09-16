#!/bin/bash
#
# Listet die Event Publications mit einem Status, ohne Angabe FAILED.
# Beispiel: ./list-publications.sh COMPLETED

curl -s "http://127.0.0.1:8080/api/admin/event-publications?status=${1:-FAILED}"
echo
