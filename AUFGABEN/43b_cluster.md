# Erneut zustellen im Cluster

## Dateien

- `src/main/resources/application.properties`

## Aufgabe

Diese Übung ist optional. Starte Plantify zweimal, auf den Ports 8080 und 8081. Beide Instanzen arbeiten gegen dieselbe H2-Datei und bilden damit einen kleinen Cluster. Probier aus, welcher der beiden Wege zum erneuten Zustellen eine Mail doppelt verschickt.

Die Übung setzt auf meinem Stand auf. Sichere deine eigenen Änderungen und wechsle auf den Branch `live_coding`:

```bash
git stash -u
git fetch origin
git switch -C live_coding origin/live_coding
```

## Vorbereitung

Starte eine zweite Instanz im Terminal mit:

```bash
./mvnw spring-boot:run "-Dspring-boot.run.arguments=--server.port=8081"
```

In IntelliJ kopiere die Run Configuration und trag unter "Program arguments" `--server.port=8081` ein.

Unter Windows schreib `curl.exe` statt `curl`, dann funktionieren die Befehle auch in der PowerShell.

## Teil 1: Ein Neustart stellt doppelt zu

1. Setz in `application.properties` `spring.modulith.events.republish-outstanding-events-on-restart=true`.

2. Setz die Datenbank zurück, indem du `./reset-db.sh` ausführst oder das Verzeichnis `data` löschst. Starte die erste Instanz mit einer Verzögerung von 60 Sekunden:

   ```bash
   ./mvnw spring-boot:run "-Dspring-boot.run.arguments=--plantify.mail.delay=60s"
   ```

3. Lege über http://localhost:8080/swagger-ui.html eine Pflanze an. Die Poststelle der ersten Instanz wartet jetzt eine Minute.

4. Starte in dieser Minute die zweite Instanz auf Port 8081, mit dem Befehl aus der Vorbereitung.

5. Warte, bis die erste Instanz fertig ist, und zähl die 📧-Zeilen in beiden Logs. Die Mail ist zweimal hinausgegangen, einmal je Instanz.

6. Die zweite Instanz hat beim Start alles erneut zugestellt, was noch nicht `COMPLETED` war. Was hätte sie stattdessen tun sollen, und woher hätte sie wissen sollen, dass die erste Instanz noch arbeitet?

## Teil 2: Erneut zustellen ohne Dubletten

7. Setz in `application.properties` `spring.modulith.events.republish-outstanding-events-on-restart=false`, setz die Datenbank zurück und starte beide Instanzen, die erste mit `./mvnw spring-boot:run`, die zweite mit dem Befehl aus der Vorbereitung.

8. Lege über Port 8080 eine Pflanze mit `"location": ""` an. `CareService` scheitert, und `curl http://localhost:8080/api/admin/event-publications` zeigt den Eintrag auf `FAILED`.

9. Öffne die H2-Console unter http://localhost:8080/h2-console (JDBC URL `jdbc:h2:./data/plantify;AUTO_SERVER=TRUE;MODE=PostgreSQL`, Benutzer `sa`, kein Passwort) und korrigiere den Payload:

   ```sql
   UPDATE event_publication
   SET serialized_event = REPLACE(serialized_event, '"location":""', '"location":"Schlafzimmer"')
   WHERE status = 'FAILED';
   ```

10. Stoß auf beiden Instanzen gleichzeitig das erneute Zustellen an:

    ```bash
    curl -X POST localhost:8080/api/admin/event-publications/resubmit & curl -X POST localhost:8081/api/admin/event-publications/resubmit & wait
    ```

    Ohne Bash geht das in der Eingabeaufforderung von Windows mit:

    ```bat
    start /b curl.exe -X POST localhost:8080/api/admin/event-publications/resubmit & curl.exe -X POST localhost:8081/api/admin/event-publications/resubmit
    ```

11. Sieh in `CARE_TASKS` nach: Die Pflanze hat ihre Pflegeaufgaben genau einmal. Der Eintrag in `EVENT_PUBLICATION` steht auf `COMPLETED` mit zwei Versuchen.

12. Beide Instanzen haben dieselbe gescheiterte Publication gelesen. Warum hat trotzdem nur eine sie zugestellt?

## Material

- Spring Modulith, Event Publication Registry und Neustart: https://docs.spring.io/spring-modulith/reference/events.html#publication-registry
- Spring Modulith, gescheiterte Publications erneut zustellen: https://docs.spring.io/spring-modulith/reference/events.html#publication-registry.lifecycle.failed-and-resubmission

## Hintergrund

**Eine Datenbank für zwei Instanzen.** Beide Instanzen dürfen dieselbe H2-Datei öffnen, weil die JDBC-URL `AUTO_SERVER=TRUE` enthält. Die erste startet einen kleinen Datenbankserver, die zweite verbindet sich mit ihm.

**Wer gewinnt.** Bevor eine Instanz eine gescheiterte Publication erneut zustellt, setzt sie sie in der Datenbank auf `RESUBMITTED`, und zwar nur, wenn sie noch nicht `RESUBMITTED` ist. Diese Änderung gelingt genau einer Instanz, die andere lässt die Publication liegen.

**Warum der Neustart doppelt zustellt.** `republish-outstanding-events-on-restart` stellt alles erneut zu, was nicht abgeschlossen ist, auch Publications, die eine andere Instanz gerade verarbeitet. In einer einzelnen Instanz ist das harmlos, denn nach einem Neustart läuft dort nichts mehr. Im Cluster gehört die Property abgeschaltet.

**Idempotent bleibt Pflicht.** Auch ohne Cluster wird ein Event mindestens einmal zugestellt, nicht genau einmal. Ein Listener, der etwas außerhalb seiner Transaktion tut, etwa eine Mail verschicken, muss eine Wiederholung erkennen.
