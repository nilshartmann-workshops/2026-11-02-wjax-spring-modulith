#!/bin/bash
#
# Legt eine Pflanze an. Wer lieber klickt, nimmt Swagger UI:
# http://localhost:8080/swagger-ui.html

curl -s -X POST http://127.0.0.1:8080/api/plants \
  -H "Content-Type: application/json" \
  -d '{
        "location": "Balkon",
        "name": "Cannabis (nur für medizinische Zwecke)",
        "ownerId": "ee3829e4-fe2b-4d03-b2a1-70f1425d8c1c",
        "plantType": "INDOOR"
      }'
