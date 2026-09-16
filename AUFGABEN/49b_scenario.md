# Auf ein asynchrones Ergebnis warten, mit `Scenario`

## Dateien

- `src/test/java/nh/demo/plantify/billing/BillingModuleTest.java` (neu)

## Aufgabe

Schreib einen Modultest für `billing`. Er veröffentlicht ein `PlantRegisteredEvent` und prüft, dass `UsageTracker` daraufhin eine Einrichtungsgebühr speichert. Weil der Listener asynchron arbeitet, wartet der Test mit einem `Scenario` auf das Ergebnis.

## Schritte

1. Lege im Testverzeichnis im Paket `nh.demo.plantify.billing` die Klasse `BillingModuleTest` an:

   ```java
   package nh.demo.plantify.billing;

   import nh.demo.plantify.TestIds;
   import nh.demo.plantify.plant.PlantRegisteredEvent;
   import nh.demo.plantify.plant.PlantType;
   import org.junit.jupiter.api.Test;
   import org.springframework.beans.factory.annotation.Autowired;
   import org.springframework.modulith.test.ApplicationModuleTest;
   import org.springframework.modulith.test.Scenario;
   import org.springframework.test.context.ActiveProfiles;

   import static org.assertj.core.api.Assertions.assertThat;

   @ApplicationModuleTest(ApplicationModuleTest.BootstrapMode.DIRECT_DEPENDENCIES)
   @ActiveProfiles("test")
   class BillingModuleTest {

       @Autowired
       UsageRepository usageRepository;

       @Test
       void registers_setup_fee_for_registered_plant(Scenario scenario) {
           // todo:
           //  Veröffentliche mit scenario.publish() ein PlantRegisteredEvent.
           //  Frag mit andWaitForStateChange() die gespeicherten Einträge über das usageRepository ab
           //  und warte, bis mindestens ein Eintrag gespeichert ist.
           //  Prüfe mit andVerify() die gelesenen Einträge, zum Beispiel ob der UsageType SETUP_FEE ist,
           //  ob costCents positiv ist oder ob die ownerId der aus deinem Event entspricht.
       }
   }
   ```

   `billing` braucht das `OwnerRepository`, deshalb startet der Test mit `DIRECT_DEPENDENCIES` auch das Modul `owner`. Den `Scenario`-Parameter stellt Modulith bereit.

2. Ersetze den Kommentar mit `todo` durch den Test:
   - Für das Event enthält die Klasse `TestIds` eine `existingOwnerId` und eine `randomPlantId`.
   - Die Einträge fragst du mit `usageRepository.findByOwnerId(...)` ab.

3. Starte den Test. Er ist grün.

4. Warum reicht es nicht, das Event mit einem `ApplicationEventPublisher` zu veröffentlichen und direkt danach das Repository abzufragen? Denk an die Annotationen, die `@ApplicationModuleListener` zusammenfasst.

## Material

- Spring Modulith, Scenarios: https://docs.spring.io/spring-modulith/reference/testing.html#scenarios
- Spring Modulith, Bootstrap Modes: https://docs.spring.io/spring-modulith/reference/testing.html#bootstrap-modes

## Hintergrund

**Warum `Scenario`.** Ein `@ApplicationModuleListener` läuft in einem eigenen Thread und erst nach dem Commit der Transaktion, die das Event veröffentlicht hat. Ein Test, der direkt nach dem Veröffentlichen prüft, sieht deshalb noch nichts. `Scenario` veröffentlicht das Event in einer Transaktion und wartet danach, bis die erwartete Änderung eingetreten ist oder die Wartezeit abläuft.
