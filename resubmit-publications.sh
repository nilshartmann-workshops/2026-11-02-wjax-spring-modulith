#!/bin/bash
#
# Stellt die gescheiterten Event Publications erneut zu, die weniger als
# maxAttempts Versuche hinter sich haben, ohne Angabe 3.
# Beispiel: ./resubmit-publications.sh 10

curl -s -X POST "http://127.0.0.1:8080/api/admin/event-publications/resubmit?maxAttempts=${1:-3}"
