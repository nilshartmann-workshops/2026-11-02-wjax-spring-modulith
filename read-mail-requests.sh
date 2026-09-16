#!/bin/bash
#
# Liest das Topic mail-requests von Anfang an und zeigt zu jeder Nachricht den
# Message-Key. Der Broker aus der compose.yaml muss laufen. Das Image des
# Brokers enthaelt die Kommandozeilenwerkzeuge nicht, deshalb laeuft der
# Consumer in einem zweiten Container im Netz des Brokers. Beenden mit Strg+C.

docker run --rm -it --network container:wjax-plantify-kafka apache/kafka:4.1.1 \
  /opt/kafka/bin/kafka-console-consumer.sh \
  --bootstrap-server localhost:9092 \
  --topic mail-requests \
  --from-beginning \
  --property print.key=true
