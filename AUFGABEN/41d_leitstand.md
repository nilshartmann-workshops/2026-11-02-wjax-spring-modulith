# Ein Leitstand für die Event Publication Registry


## Dateien

- `pom.xml`
- `src/main/java/nh/demo/plantify/admin/EventPublicationController.java` (neu)
- `mailroom/ConfirmationMailService.java`
- `src/main/resources/application.properties`

## Aufgabe

Sieh dir an, wie Spring Modulith die Zustellung jedes Events festhält, lass die Poststelle scheitern und stell ihr Event mit einem Neustart erneut zu. Bau danach ein kleines Modul `admin`: einen Leitstand, der zeigt, was gescheitert ist, und gescheiterte Publications erneut zustellt. Lass damit einen Listener scheitern, korrigiere den Payload seiner Publication und stell sie in der laufenden Anwendung erneut zu.

Ruf den Leitstand mit `curl` auf, er antwortet mit JSON. Unter Windows schreib `curl.exe` statt `curl`, dann funktionieren die Befehle auch in der PowerShell.

## Teil 1: Ein Listener scheitert, und der Neustart stellt erneut zu

1. Setz die Datenbank zurück, indem du `./reset-db.sh` ausführst oder das Verzeichnis `data` löschst. Starte die Anwendung und lege eine Pflanze an, über Swagger UI unter http://localhost:8080/swagger-ui.html oder mit `./add-plant.sh`.

2. Öffne die H2-Console unter http://localhost:8080/h2-console (JDBC URL `jdbc:h2:./data/plantify;AUTO_SERVER=TRUE;MODE=PostgreSQL`, Benutzer `sa`, kein Passwort) und sieh dir die Tabelle `EVENT_PUBLICATION` an.

   Für jeden Listener steht dort ein Eintrag, und `COMPLETION_DATE` ist gesetzt. Wozu merkt sich Modulith, welcher Listener ein Event schon verarbeitet hat?

3. Lass die Poststelle scheitern: Wirf in `ConfirmationMailService` am Ende von `onPlantRegistered` eine `IllegalStateException`, etwa mit der Meldung `No delivery receipt from mail server`. Starte die Anwendung neu und lege eine Pflanze an.

4. Sieh dir `EVENT_PUBLICATION` wieder an. Zwei Einträge der neuen Pflanze stehen auf `COMPLETED`, der Eintrag der Poststelle auf `FAILED`. In `PLANTS`, `CARE_TASKS` und `USAGE_RECORDS` ist die Pflanze trotzdem angekommen.

5. Trag in `application.properties` ein, dass Modulith beim Start alles erneut zustellt, was noch nicht abgeschlossen ist:

   ```properties
   spring.modulith.events.republish-outstanding-events-on-restart=true
   ```

6. Entferne die Exception und starte die Anwendung neu. Der Eintrag der Poststelle steht danach auf `COMPLETED`, und im Log steht die 📧-Meldung für dieselbe Pflanze ein zweites Mal.

7. Die Mail ist zweimal hinausgegangen. Was folgt daraus für einen Listener, der etwas außerhalb seiner Transaktion tut?

## Teil 2: Den Leitstand bauen

8. Gescheiterte Publications stellt `FailedEventPublications` aus der öffentlichen API von Spring Modulith erneut zu. Zum Auflisten brauchst du `EventPublicationRepository` aus `spring-modulith-events-core`, und dieses Jar bringen die Starter nur zur Laufzeit mit. Trag es deshalb in die `pom.xml` ein, direkt unter `spring-modulith-starter-jdbc`:

   ```xml
   <dependency>
       <groupId>org.springframework.modulith</groupId>
       <artifactId>spring-modulith-events-core</artifactId>
   </dependency>
   ```

   Das Jar liegt schon in deinem lokalen Maven-Repository. Lass die IDE das Projekt neu laden.

9. Lege das Paket `nh.demo.plantify.admin` an und darin die Klasse `EventPublicationController`:

   ```java
   package nh.demo.plantify.admin;

   import org.slf4j.Logger;
   import org.slf4j.LoggerFactory;
   import org.springframework.modulith.events.EventPublication;
   import org.springframework.modulith.events.FailedEventPublications;
   import org.springframework.modulith.events.ResubmissionOptions;
   import org.springframework.modulith.events.core.EventPublicationRepository;
   import org.springframework.web.bind.annotation.GetMapping;
   import org.springframework.web.bind.annotation.PostMapping;
   import org.springframework.web.bind.annotation.RequestMapping;
   import org.springframework.web.bind.annotation.RequestParam;
   import org.springframework.web.bind.annotation.RestController;

   import java.util.List;
   import java.util.Map;

   @RestController
   @RequestMapping("/api/admin/event-publications")
   class EventPublicationController {

       private static final Logger log = LoggerFactory.getLogger(EventPublicationController.class);

       @GetMapping
       List<Map<String, Object>> findByStatus(@RequestParam(defaultValue = "FAILED") EventPublication.Status status) {
           // todo: Lies mit dem EventPublicationRepository die Publications mit diesem Status und gib sie zurück.
           //  Welche Informationen du zurücklieferst und in welchem Format, überlegst du dir selbst.

           return List.of();
       }

       @PostMapping("/resubmit")
       void resubmitFailed(@RequestParam(defaultValue = "3") int maxAttempts) {
           log.info("🔁 Resubmitting failed event publications with less than {} attempts ...", maxAttempts);

           // todo: Stelle mit FailedEventPublications die gescheiterten Publications erneut zu.
           //  Lass jede liegen, die schon maxAttempts Versuche hinter sich hat.
           //  Welche weiteren ResubmissionOptions hältst du für sinnvoll?
       }
   }
   ```

10. Lass dir `EventPublicationRepository` und `FailedEventPublications` in den Controller injizieren und fülle die Stellen mit `todo`. Für die folgenden Schritte braucht die Liste je Publication den Listener, den Status, die Zahl der Versuche und das Event.

11. Setz die Datenbank zurück, indem du `./reset-db.sh` ausführst oder das Verzeichnis `data` löschst. Starte die Anwendung und lege eine Pflanze an. Ruf dann die abgeschlossenen Publications ab:

   ```bash
   curl "http://localhost:8080/api/admin/event-publications?status=COMPLETED"
   ```

   Für jeden Listener steht dort ein Eintrag.

## Teil 3: Ein gescheitertes Event im Leitstand

12. Lege über Swagger UI eine Pflanze mit leerem Standort an, also `"location": ""`. Der Request ist erfolgreich, aber im Log scheitert `CareService` an `location must not be blank`.

13. Ruf die gescheiterten Publications ab:

   ```bash
   curl http://localhost:8080/api/admin/event-publications
   ```

   Ohne Parameter liefert der Leitstand die Publications mit Status `FAILED`. Du siehst den Listener von `CareService`, das Event mit seinem Payload und die Zahl der Versuche.

14. Stell die gescheiterten Publications erneut zu, und zwar dreimal hintereinander:

   ```bash
   curl -X POST http://localhost:8080/api/admin/event-publications/resubmit
   ```

   Sieh dir danach die Liste wieder an. Die Zahl der Versuche steigt, bis sie bei drei stehen bleibt: Der Filter in `resubmitFailed` lässt eine Publication liegen, die schon dreimal gescheitert ist.

15. Warum ist es sinnvoll, eine Publication nach einigen Versuchen liegen zu lassen, statt es immer wieder zu probieren?

## Teil 4: Den Payload korrigieren und erneut zustellen

16. Öffne die H2-Console unter http://localhost:8080/h2-console (JDBC URL `jdbc:h2:./data/plantify;AUTO_SERVER=TRUE;MODE=PostgreSQL`, Benutzer `sa`, kein Passwort) und korrigiere den Payload in der Datenbank:

   ```sql
   UPDATE event_publication
   SET serialized_event = REPLACE(serialized_event, '"location":""', '"location":"Schlafzimmer"')
   WHERE status = 'FAILED';
   ```

17. Stell erneut zu, diesmal mit einer höheren Grenze für den Filter:

    ```bash
    curl -X POST "http://localhost:8080/api/admin/event-publications/resubmit?maxAttempts=10"
    ```

    Danach steht der Eintrag auf `COMPLETED`, und in `CARE_TASKS` stehen die Pflegeaufgaben der Pflanze.

## 🧐 Optional: Ein Listener bleibt hängen

18. Ein Listener kann auch weder fertig werden noch scheitern, etwa weil der Mailserver nicht antwortet. Gib der Poststelle dafür eine einstellbare Verzögerung. Ergänze in `ConfirmationMailService` einen Konstruktor und rufe am Anfang von `onPlantRegistered` `Thread.sleep` auf:

    ```java
    private final Duration delay;

    ConfirmationMailService(@Value("${plantify.mail.delay:0s}") Duration delay) {
        this.delay = delay;
    }

    @ApplicationModuleListener
    void onPlantRegistered(PlantRegisteredEvent event) {
        try {
            Thread.sleep(delay);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        // ... wie bisher
    }
    ```

    `Value` kommt aus `org.springframework.beans.factory.annotation`.

19. Schalte in `application.properties` den Staleness-Monitor ein. Er sieht alle fünf Sekunden nach und setzt eine Publication auf `FAILED`, die länger als zehn Sekunden in `PROCESSING` steht:

    ```properties
    spring.modulith.events.staleness.processing=10s
    spring.modulith.events.staleness.check-interval=5s
    ```

20. Starte die Anwendung mit einer Verzögerung von 30 Sekunden, etwa mit `./mvnw spring-boot:run "-Dspring-boot.run.arguments=--plantify.mail.delay=30s"` oder mit der Zeile `plantify.mail.delay=30s` in `application.properties`. Lege eine Pflanze an und ruf die Liste mehrmals mit `?status=PROCESSING` und mit `?status=FAILED` ab: Erst steht die Poststelle auf `PROCESSING`, nach etwa 15 bis 20 Sekunden auf `FAILED`, und wenn der Listener nach 30 Sekunden doch fertig wird, auf `COMPLETED`.

21. Was passiert, wenn du die Publication der Poststelle erneut zustellst, solange sie auf `FAILED` steht, der erste Aufruf aber noch läuft? Probier es aus und zähl die 📧-Zeilen im Log. Was folgt daraus für die Dauer, die du für `staleness.processing` wählst?

## 🧐 Optional: Was mit abgeschlossenen Publications passiert

22. Setz die Datenbank zurück und starte die Anwendung mit `spring.modulith.events.completion-mode=DELETE`, etwa mit `./mvnw spring-boot:run "-Dspring-boot.run.arguments=--spring.modulith.events.completion-mode=DELETE"` oder mit der Zeile in `application.properties`. Lege eine Pflanze an, ruf die Liste mit `?status=COMPLETED` ab und sieh in der H2-Console in `EVENT_PUBLICATION` nach.

23. Mach dasselbe mit `ARCHIVE`. Welche Tabellen gibt es danach, und was zeigt der Leitstand für `COMPLETED`?

24. Wann ist welcher Modus sinnvoll? Nimm die Zeile danach wieder heraus.

## Material

- Spring Modulith, Event Publication Registry: https://docs.spring.io/spring-modulith/reference/events.html#publication-registry
- Spring Modulith, gescheiterte Publications erneut zustellen: https://docs.spring.io/spring-modulith/reference/events.html#publication-registry.lifecycle.failed-and-resubmission
- Spring Modulith, Publications verwalten: https://docs.spring.io/spring-modulith/reference/events.html#publication-registry.managing-publications
- Spring Modulith, Event Publication Repositories: https://docs.spring.io/spring-modulith/reference/events.html#publication-registry.publication-repositories
- Spring Modulith, Staleness-Monitor: https://docs.spring.io/spring-modulith/reference/events.html#publication-registry.lifecycle.staleness
- Spring Modulith, abgeschlossene Publications: https://docs.spring.io/spring-modulith/reference/events.html#publication-registry.completion
- Spring Modulith, Issue "Allow access to all incomplete event publications": https://github.com/spring-projects/spring-modulith/issues/581

## Hintergrund

**Die Event Publication Registry** hält fest, welches Event an welchen Listener zugestellt werden soll und ob er es verarbeitet hat. Ein Event, das ein Listener nicht verarbeiten konnte, geht dadurch nicht verloren.

**Die Status einer Publication.** Jeder Eintrag beginnt als `PUBLISHED` und geht auf `PROCESSING`, sobald sein Listener läuft. Danach steht er auf `COMPLETED` oder `FAILED`. Wer eine gescheiterte Publication erneut zustellt, setzt sie zuerst auf `RESUBMITTED`. `completionAttempts` zählt jeden Versuch, auch einen, bei dem die Anwendung mitten im Listener abgestürzt ist.

**Warum nur `FAILED` erneut zustellen.** Neben `FailedEventPublications` gibt es `IncompleteEventPublications`, das alles erneut zustellt, was nicht `COMPLETED` ist. Dazu gehört auch ein Listener, der gerade noch läuft, und dann läuft er zweimal. `FailedEventPublications` nimmt nur, was wirklich gescheitert ist, und kann über `ResubmissionOptions` drosseln und filtern.

**Warum die öffentliche API nicht auflisten kann.** Ein `findAll()` für offene Publications ist seit 2024 gewünscht (Issue #581) und bisher nicht umgesetzt. Spring Modulith bietet für die Registry vor allem Verarbeitung an: erneut zustellen, aufräumen und, ab Version 2.2, aufgeben. Wer sehen will, was offen ist, geht wie hier über `EventPublicationRepository` und benutzt damit eine Schnittstelle, die sich mit einer neuen Version ändern darf.

**Abgeschlossene Publications.** Ohne Angabe bleibt ein abgeschlossener Eintrag mit `COMPLETION_DATE` in `EVENT_PUBLICATION` stehen (`UPDATE`), und die Tabelle wächst, bis jemand aufräumt. Mit `DELETE` verschwindet er sofort. Mit `ARCHIVE` wandert er in eine eigene Tabelle `EVENT_PUBLICATION_ARCHIVE`. `EVENT_PUBLICATION` enthält dann nur noch offene Einträge, und der Leitstand liest die abgeschlossenen aus dem Archiv.
