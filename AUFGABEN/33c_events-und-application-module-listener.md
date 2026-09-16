# Ein Event statt zwei Aufrufen, mit `@ApplicationModuleListener`

## Dateien

- `plant/PlantRegisteredEvent.java` (neu)
- `plant/PlantService.java`
- `care/CareService.java`
- `billing/UsageTracker.java`
- `mailroom/ConfirmationMailService.java` (neu)
- `src/test/java/.../PlantifyModuleTest.java`
- `src/test/java/.../plant/PlantModuleTest.java` (neu)
- `src/test/java/.../owner/OwnerModuleTest.java`

## Aufgabe

Ersetze in `PlantService` die Aufrufe von `CareService` und `UsageTracker` durch ein Event. Lass beide mit `@ApplicationModuleListener` darauf hören, dazu ein neues Modul, für das sich `plant` nicht ändert. Sieh dir danach an, was aus dem Zyklus geworden ist.

## Vorbereitung

Setz vor jedem Start der Anwendung die Datenbank zurück, indem du `./reset-db.sh` ausführst oder das Verzeichnis `data` löschst. Lege eine Pflanze über Swagger UI unter http://localhost:8080/swagger-ui.html an oder mit `./add-plant.sh`.

## Teil 1: Ein Event veröffentlichen

1. Lege im Paket `plant` das Event an. Es ist `public`, denn die anderen Module hören darauf:

   ```java
   package nh.demo.plantify.plant;

   import java.util.UUID;

   public record PlantRegisteredEvent(UUID plantId, UUID ownerId, String name, PlantType plantType, String location) {
   }
   ```

2. Lass dir in `PlantService` einen `ApplicationEventPublisher` in den Konstruktor geben und entferne `CareService` und `UsageTracker` daraus.

3. Veröffentliche in `registerPlant` nach dem Speichern ein `PlantRegisteredEvent` mit `publishEvent`, anstatt die beiden Services aufzurufen.

## Teil 2: Auf das Event hören

4. Gib `CareService` eine neue Methode `onPlantRegistered(PlantRegisteredEvent event)`, die `setupInitialCareTasks` aufruft, und annotiere sie mit `@ApplicationModuleListener` aus `org.springframework.modulith.events`. Mach `setupInitialCareTasks` `private`.

5. Mach dasselbe in `UsageTracker` für `registerSetupFee`.

6. Leg die Poststelle an, ein neues Modul (`mailroom`), das zu jeder neuen Pflanze eine Bestätigungsmail verschickt. Lege dafür das Paket `nh.demo.plantify.mailroom` an und darin die Klasse `ConfirmationMailService`:

   ```java
   package nh.demo.plantify.mailroom;

   import nh.demo.plantify.plant.PlantRegisteredEvent;
   import org.slf4j.Logger;
   import org.slf4j.LoggerFactory;
   import org.springframework.modulith.events.ApplicationModuleListener;
   import org.springframework.stereotype.Service;

   @Service
   class ConfirmationMailService {

       private static final Logger log = LoggerFactory.getLogger(ConfirmationMailService.class);

       @ApplicationModuleListener
       void onPlantRegistered(PlantRegisteredEvent event) {
           log.info("📧 Confirmation mail sent for plant '{}'", event.name());
       }
   }
   ```

7. Starte die Anwendung und lege eine Pflanze an. Achte im Log auf die Thread-Namen in eckigen Klammern: Die Meldung mit ✅ kommt aus dem Thread des Requests `nio-8080-exec-...`, die Meldungen mit 🌱, 🤑 und 📧 kommen aus eigenen Threads `task-...`.

## Teil 3: Das Event testen

8. Lege im Testverzeichnis im Paket `nh.demo.plantify.plant` die Klasse `PlantModuleTest` an. Sie prüft, dass `registerPlant` das Event veröffentlicht. Gibt es die Klasse bei dir schon, ersetze sie:

    ```java
    package nh.demo.plantify.plant;

    import nh.demo.plantify.TestIds;
    import org.junit.jupiter.api.Test;
    import org.springframework.beans.factory.annotation.Autowired;
    import org.springframework.modulith.test.ApplicationModuleTest;
    import org.springframework.modulith.test.AssertablePublishedEvents;
    import org.springframework.test.context.ActiveProfiles;

    import static org.assertj.core.api.Assertions.assertThat;

    @ApplicationModuleTest
    @ActiveProfiles("test")
    class PlantModuleTest {

        @Autowired
        PlantService plantService;

        @Test
        void registers_plant_and_publishes_event(AssertablePublishedEvents events) {
            // todo:
            //  Lege mit dem plantService eine Pflanze an.
            //  Prüfe mit assertThat(events), dass ein PlantRegisteredEvent veröffentlicht wurde
            //  und dass seine plantId der ID der neuen Pflanze entspricht.
        }
    }
    ```

    Den Parameter `AssertablePublishedEvents` stellt Modulith bereit. Er enthält jedes Event, das während des Tests veröffentlicht wurde.

9. Ersetze den Kommentar mit `todo` durch den Test:
    - Für die Pflanze enthält die Klasse `TestIds` eine `existingOwnerId`.
    - `assertThat(events)` bietet `contains(...)` für den Typ des Events und danach `matching(...)` für eines seiner Felder.

    Starte den Test. Er ist grün.

10. Der Test kommt ohne `CareService` und `UsageTracker` aus, auch ohne Mocks. Warum?

## Teil 4: Die Architektur prüfen

11. Nimm im `OwnerModuleTest` `verifyAutomatically = false` heraus.

12. Entferne im `PlantifyModuleTest` den Filter für den Zyklus, sodass dort wieder nur `modules.verify()` steht. Starte alle Tests mit `./mvnw verify` und sieh dir danach `target/spring-modulith-docs/components.puml` an.

13. Welche Beziehung zeigt das Diagramm jetzt zwischen `plant` und den anderen Modulen, und was ist aus dem Zyklus geworden?

## 🧐 Optional: Die Dokumentation je Modul, und was ins Event gehört

14. Öffne in `target/spring-modulith-docs` die Module Canvases `module-care.adoc`, `module-mailroom.adoc` und `module-plant.adoc`. Wo taucht `PlantRegisteredEvent` auf, und wo fehlt es?

15. Die Poststelle soll die Mail an den Owner schicken und braucht dafür seine Mailadresse. Das Event enthält sie nicht. Welche Möglichkeiten hast du, und was bedeutet jede für die Abhängigkeiten zwischen den Modulen?

## Material

- Spring Modulith, Working with Application Events: https://docs.spring.io/spring-modulith/reference/events.html
- Spring Framework, Transaction-bound Events: https://docs.spring.io/spring-framework/reference/data-access/transaction/event.html
- Spring Modulith, Testing published events: https://docs.spring.io/spring-modulith/reference/events.html#testing
- Spring Modulith, Application Module Canvas: https://docs.spring.io/spring-modulith/reference/documentation.html#application-module-canvas

## Hintergrund

**Warum verschwindet der Zyklus?** Ein Modul, das ein Event veröffentlicht, kennt die Empfänger nicht. `plant` weiß nach dem Umbau nichts mehr von `care` und `billing`. Die Abhängigkeit zeigt nur noch in eine Richtung: `care`, `billing` und `mailroom` hören auf ein Event aus `plant`. Modulith zeichnet das im Diagramm als "listens to" ein.

**Die Bestandteile von `@ApplicationModuleListener`.** Die Annotation fasst drei andere zusammen:

| Annotation | Wirkung |
|---|---|
| `@Async` | Der Listener läuft in einem eigenen Thread, und der Request wartet nicht auf ihn. |
| `@TransactionalEventListener` | Der Listener wird erst nach einem erfolgreichen Commit des Veröffentlichers aufgerufen. Scheitert die Transaktion, wird er gar nicht aufgerufen. |
| `@Transactional(propagation = REQUIRES_NEW)` | Der Listener bekommt eine eigene Transaktion, weil die ursprüngliche bereits abgeschlossen ist. |

Modulith empfiehlt `@ApplicationModuleListener` für jeden Listener, der auf ein Event aus einem anderen Modul hört.

**Veröffentlichte Events im Module Canvas.** Unter "Events listened to" führt der Canvas jedes Event, auf das ein Listener des Moduls hört. Ein veröffentlichtes Event erkennt Spring Modulith nur, wenn es mit `@DomainEvent` aus jMolecules gekennzeichnet ist. jMolecules gehört nicht zu Plantify, deshalb bleibt der Canvas von `plant` leer.

**Was ins Event gehört.** Ein Event entkoppelt den Veröffentlicher von seinen Empfängern, aber sein Inhalt ist eine Vereinbarung zwischen ihnen. Fehlen einem neuen Listener Daten, bleiben zwei Wege. Entweder holt sich der Empfänger die Daten selbst, und es entsteht eine neue Abhängigkeit, hier von `mailroom` zu `owner`. Oder das veröffentlichende Modul erweitert sein Event, und `plant` muss dafür die Mailadresse kennen.
