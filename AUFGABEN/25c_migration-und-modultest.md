# Die Migration gehört zum Modul, und ein Modul startet allein


## Dateien

- `src/main/resources/db/migration/` (die Migrationen ziehen um)
- `src/main/resources/application.properties`
- `src/test/java/nh/demo/plantify/owner/OwnerModuleTest.java` (neu)

## Aufgabe

Bisher liegen alle Migrationen in einem Verzeichnis und sind über alle Module hinweg durchnummeriert. Verschiebe sie in ein Verzeichnis je Modul, damit Spring Modulith sie je Modul ausführt. Schreib danach einen Modultest ohne Fachlichkeit, der zeigt, dass für `owner` nur dieses Modul startet und nur seine Migrationen laufen.

## Teil 1: Migrationen je Modul

1. Sieh dir `src/main/resources/db/migration` an. Die Nummern sind nach Modulen gruppiert: `V1` für `owner`, `V11` bis `V13` für `billing`, `V21` und `V22` für `plant`, `V31` und `V32` für `care`. Das weiß allerdings nur, wer die Konvention kennt.

2. Führe im Verzeichnis des Repositories `./move-migrations.sh` aus. Das Skript verschiebt die Migrationen in ein Verzeichnis je Modul und zählt sie dort neu ab `V1`. Die Migrationen von `plant` bleiben mit ihren alten Nummern als Altbestand in `__root`.

   Läuft das Skript bei dir nicht, etwa unter Windows ohne Git Bash, verschiebe die Dateien unter `src/main/resources/db/migration` von Hand:

   | Modul | Quelle | Ziel |
   |---|---|---|
   | `owner` | `V1__create_owner.sql` | `owner/V1__create_owner.sql` |
   | `billing` | `V11__create_billing_schema.sql` | `billing/V1__create_billing_schema.sql` |
   | `billing` | `V12__add_usage_records_table.sql` | `billing/V2__add_usage_records_table.sql` |
   | `billing` | `V13__add_invoices_table.sql` | `billing/V3__add_invoices_table.sql` |
   | `plant` | `V21__create_plant_schema.sql` | `__root/V21__create_plant_schema.sql` |
   | `plant` | `V22__add_plants_table.sql` | `__root/V22__add_plants_table.sql` |
   | `care` | `V31__create_care_schema.sql` | `care/V1__create_care_schema.sql` |
   | `care` | `V32__add_care_tasks_table.sql` | `care/V2__add_care_tasks_table.sql` |

3. Trag in `application.properties` ein, dass Spring Modulith die Migrationen je Modul ausführt:

   ```properties
   spring.modulith.runtime.flyway-enabled=true
   ```

4. Führe `./mvnw clean` aus und setz die Datenbank zurück, indem du `./reset-db.sh` ausführst oder das Verzeichnis `data` löschst. Starte dann die Anwendung und öffne die H2-Console unter http://localhost:8080/h2-console (JDBC URL `jdbc:h2:./data/plantify;AUTO_SERVER=TRUE;MODE=PostgreSQL`, Benutzer `sa`, kein Passwort). Dort stehen jetzt die Tabellen `flyway_schema_history_owner`, `flyway_schema_history_billing` und `flyway_schema_history_care`, dazu `flyway_schema_history` für `__root`.

5. Jedes Modul hat eine eigene `V1`. Warum stört das Flyway nicht?

## Teil 2: `owner` startet allein

6. Lege im Testverzeichnis das Paket `nh.demo.plantify.owner` an und darin die Klasse `OwnerModuleTest`:

   ```java
   package nh.demo.plantify.owner;

   import nh.demo.plantify.care.CareService;
   import org.junit.jupiter.api.Test;
   import org.springframework.beans.factory.annotation.Autowired;
   import org.springframework.context.ApplicationContext;
   import org.springframework.jdbc.core.JdbcTemplate;
   import org.springframework.modulith.test.ApplicationModuleTest;
   import org.springframework.test.context.ActiveProfiles;

   import static org.assertj.core.api.Assertions.assertThat;

   @ApplicationModuleTest(verifyAutomatically = false)
   @ActiveProfiles("test")
   class OwnerModuleTest {

       @Autowired
       ApplicationContext applicationContext;

       @Autowired
       JdbcTemplate jdbcTemplate;

       @Test
       void starts_only_the_owner_module() {
           // todo: Stelle sicher, dass nur Beans aus dem Modul owner gefunden werden, aber keine aus dem Modul care
       }

       @Test
       void migrates_only_the_owner_module() {
           // todo: Stelle sicher, dass nur Tabellen aus den Schemas von owner und plant gefunden werden
       }

       private boolean tableExists(String schema, String table) {
           var count = jdbcTemplate.queryForObject(
               "SELECT COUNT(*) FROM information_schema.tables WHERE LOWER(table_schema) = ? AND LOWER(table_name) = ?",
               Integer.class,
               schema,
               table
           );
           return count > 0;
       }
   }
   ```

   Die Klasse muss in genau diesem Paket liegen, denn daran erkennt Spring Modulith, welches Modul der Test meint.

7. Fülle die beiden Testmethoden. Für die Beans eignet sich zum Beispiel `applicationContext.getBeansOfType(...)` mit `OwnerRepository` und `CareService`. Für die Tabellen gibt es die Methode `tableExists`, etwa für `owners` in `owner_schema`, `plants` in `plant_schema` und `care_tasks` in `care_schema`. Starte den Test. Er ist grün.

8. Nimm probeweise `verifyAutomatically = false` heraus und starte den Test noch einmal. Er ist rot, mit derselben Meldung wie der Architekturtest ohne Filter. Warum prüft ein Modultest die Architektur? Setz den Schalter danach wieder ein.

9. Die Tabelle von `care` fehlt, die von `plant` ist da, obwohl `plant` ebenso wenig gestartet ist. Woran liegt das?

## 🧐 Optional: Die Rosen fehlen im Modultest

10. Lege im Testverzeichnis im Paket `nh.demo.plantify.care` die Klasse `CareModuleTest` an, mit `@ApplicationModuleTest(verifyAutomatically = false)` und `@ActiveProfiles("test")`. Lass dir `CareService` und `CareTaskRepository` injizieren.

11. Schreib einen Test, der mit `setupInitialCareTasks` die Pflegeaufgaben für eine Pflanze vom Typ `ROSES` anlegt und sie danach mit `findAll()` liest. Für die Pflanze enthält die Klasse `TestIds` eine `randomPlantId`. Welche Aufgaben entstehen, und in welchem Abstand wird gegossen?

12. Starte den Test mit `mode = ApplicationModuleTest.BootstrapMode.ALL_DEPENDENCIES`. Ändert sich etwas? Warum?

13. Ergänze `extraIncludes = "rose"` und starte den Test noch einmal. Was ändert sich?

## 🧐 Optional: Eine neue Migration für `owner`

14. Lege unter `src/main/resources/db/migration/owner` eine Migration `V2__add_owner_email.sql` an, die der Tabelle `owner_schema.owners` eine Spalte `email` gibt. Starte die Anwendung und sieh dir die Historientabellen in der H2-Console an. Welche hat einen neuen Eintrag?

15. Nimm die Migration danach wieder heraus und setz die Datenbank zurück.

## Material

- Spring Modulith, Flyway je Modul: https://docs.spring.io/spring-modulith/reference/runtime.html#module-aware-flyway-migrations
- Spring Modulith, Integration Testing Application Modules: https://docs.spring.io/spring-modulith/reference/testing.html
- Spring Modulith, Bootstrap Modes: https://docs.spring.io/spring-modulith/reference/testing.html#bootstrap-modes

## Hintergrund

**Migrationen je Modul.** Mit `spring.modulith.runtime.flyway-enabled` sucht Spring Modulith die Migrationen eines Moduls in `db/migration/<modul>` und führt für jedes Modul eine eigene Historientabelle. Die Versionen zählen deshalb je Modul. Eine Migration in `db/migration/__root` gehört keinem Modul und läuft immer als Erstes, in der Anwendung wie im Modultest.

**Der Umfang von `@ApplicationModuleTest`.** Der Test startet nur das Modul, in dessen Paket er liegt, mit seinen Beans und seinen Migrationen. Andere Module fehlen, es sei denn, der `BootstrapMode` holt sie dazu. Vor dem Start prüft der Test die Architektur der ganzen Anwendung, wie `verify()` im Architekturtest. Solange dort ein bekannter Verstoß besteht, schaltet `verifyAutomatically = false` diese Prüfung ab.

**Die Richtung von `BootstrapMode`.** `DIRECT_DEPENDENCIES` und `ALL_DEPENDENCIES` holen die Module dazu, von denen das getestete Modul abhängt. `rose` hängt von `care` ab und nicht umgekehrt. Deshalb fehlen die Pflegevorschläge für Rosen im Modultest von `care`, egal mit welchem Modus. Ein Modul, das sich in ein anderes einklinkt, kommt nur mit `extraIncludes` dazu.
