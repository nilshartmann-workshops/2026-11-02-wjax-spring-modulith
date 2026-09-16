# Der Rechnungslauf schreibt nach Monatswechsel eine Rechnung

## Dateien

- `billing/InvoiceGenerator.java`
- `src/test/java/nh/demo/plantify/billing/MonthlyBillingTest.java` (neu)

## Aufgabe

Am Monatsersten schreibt `InvoiceGenerator` die Rechnungen für den Vormonat. Heute stößt ihn ein Cron-Ausdruck an. Stell den Rechnungslauf auf ein Event der Moments API um. Schreib danach einen Test, der einen Monat vergehen lässt und auf die Rechnung wartet.

## Teil 1: Auf das Event hören

1. Sieh dir in `InvoiceGenerator` die Methode `generateInvoicesForPreviousMonth` an, vor allem die beiden Fragen darüber.

2. Ersetze die Methode durch einen Listener auf `MonthHasPassed` aus `org.springframework.modulith.moments`:
   - Den Monat nennt das Event mit `getMonth()`. Die Rechnungen dafür erzeugt die vorhandene Methode `generateInvoicesForMonth`.
   - Das Event kommt von Moments selbst und aus keinem anderen Modul. Hör deshalb mit `@EventListener` und `@Transactional` darauf.
   - Entferne den Import von `Scheduled`.

3. Wer veröffentlicht `MonthHasPassed` in der laufenden Anwendung? Und was beschreibt der Code jetzt besser als der Cron-Ausdruck?

## Teil 2: Ein Monat vergeht im Test

4. Lege im Testverzeichnis im Paket `nh.demo.plantify.billing` die Klasse `MonthlyBillingTest` an:

   ```java
   package nh.demo.plantify.billing;

   import nh.demo.plantify.TestIds;
   import org.junit.jupiter.api.Test;
   import org.springframework.beans.factory.annotation.Autowired;
   import org.springframework.modulith.test.ApplicationModuleTest;
   import org.springframework.modulith.test.Scenario;
   import org.springframework.test.context.ActiveProfiles;

   import java.time.Instant;

   import static org.assertj.core.api.Assertions.assertThat;

   @ApplicationModuleTest(ApplicationModuleTest.BootstrapMode.DIRECT_DEPENDENCIES)
   @ActiveProfiles("test")
   class MonthlyBillingTest {

       @Autowired
       UsageRepository usageRepository;

       @Autowired
       InvoiceRepository invoiceRepository;

       @Test
       void creates_invoice_when_month_has_passed(Scenario scenario) {
           usageRepository.save(new UsageRecord(
               TestIds.existingOwnerId, UsageRecord.UsageType.SETUP_FEE, Instant.now(), 1000L));

           // todo:
           //  Lass mit der TimeMachine einen Monat vergehen.
           //  Warte mit dem scenario auf die Rechnung und prüfe sie.
       }
   }
   ```

5. Lass dir die `TimeMachine` in den Test injizieren. Sie kommt aus `org.springframework.modulith.moments.support`. Schalte sie für diesen Test mit der Property `spring.modulith.moments.enable-time-machine=true` ein, zum Beispiel mit `@TestPropertySource` an der Klasse.

6. Ersetze den Kommentar mit `todo` durch den Test:
   - Die `TimeMachine` springt mit `shiftBy` in der Zeit vor.
   - Ein Scenario kann statt mit einem Event auch mit einer beliebigen Aktion beginnen. Wie das geht, steht in der Doku zu Scenarios.
   - Auf die Rechnung wartest du mit `andWaitForStateChange` und `invoiceRepository.findByOwnerId(...)`, prüfen kannst du sie mit `andVerify`.

7. Starte den Test. Er ist grün.

8. Nimm die Property probeweise heraus und starte den Test noch einmal. Was passiert? Und warum ist die `TimeMachine` nicht von vornherein eingeschaltet?

## Material

- Spring Modulith, Moments: https://docs.spring.io/spring-modulith/reference/moments.html
- Spring Modulith, Scenarios: https://docs.spring.io/spring-modulith/reference/testing.html#scenarios

## Hintergrund

**Die Moments API** veröffentlicht den Lauf der Zeit als Events: `HourHasPassed`, `DayHasPassed`, `WeekHasPassed`, `MonthHasPassed`, `QuarterHasPassed` und `YearHasPassed`. Ein Modul hört darauf wie auf jedes andere Event und hängt damit an keinem Scheduler und keiner Uhrzeit. Die Properties unter `spring.modulith.moments` legen fest, wie fein die Zeit läuft (`granularity`), in welcher Zeitzone (`zone-id`) und in welchem Monat das Quartal beginnt (`quarter-start-month`).

**Warum `@EventListener`.** `MonthHasPassed` kommt nicht aus einem anderen Modul, sondern von Moments selbst. Deshalb hört der Rechnungslauf mit `@EventListener` und `@Transactional` darauf und nicht mit `@ApplicationModuleListener`.

**Die `TimeMachine`** ersetzt im Test die Uhr, von der Moments die Zeit liest. Mit `shiftBy` springt sie vor und veröffentlicht dabei jedes Event, das auf dem Weg liegt. Eingeschaltet wird sie mit `spring.modulith.moments.enable-time-machine=true`, sinnvollerweise nur im Test.
