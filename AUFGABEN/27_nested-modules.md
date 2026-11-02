# Nested Application Modules, und warum sie heute noch nicht tragen

Wir reden darüber, und diese Datei ist deine Mitschrift. Der Code unten ist eine Skizze und steht nicht im Repository.

## Ein Modul gliedert sich selbst

Ein Unterpaket wird zu einem Nested Module, wenn seine `package-info.java` die Annotation `@ApplicationModule` hat. Für ein Nested Module gelten folgende Regeln:

- Benutzen dürfen es nur das übergeordnete Modul und die anderen Nested Modules darin, und auch die nur über seine API.
- Für jedes andere Modul ist es unsichtbar, auch mit `@NamedInterface`.
- Es selbst darf alles im übergeordneten Modul benutzen, auch dessen Innenleben.

Innerhalb eines gewöhnlichen Moduls prüft Spring Modulith nichts. Ein Nested Module zieht eine Grenze innerhalb des Moduls, ohne das Modul nach außen zu öffnen.

## Das Beispiel: `billing`

`billing` enthält zwei Fachlichkeiten: die Nutzung erfassen und Rechnungen schreiben. Gegliedert sähe das Modul so aus:

```
billing/
  usage/
    package-info.java          // @ApplicationModule
    UsageTracker.java          // Listener auf PlantRegisteredEvent, package-private
    Usages.java                // API für invoicing, public
    internal/
      UsageRecord.java
      UsageRepository.java
  invoicing/
    package-info.java          // @ApplicationModule
    InvoiceGenerator.java      // benutzt nur Usages
    Invoice.java
    InvoiceRepository.java
```

`Usages` liefert die Nutzung je Owner und Zeitraum als eigenes Record und nicht als Entity:

```java
@Service
public class Usages {

    public List<UUID> findOwnerIdsBetween(Instant start, Instant end) { ... }

    public List<Usage> findUsages(UUID ownerId, Instant start, Instant end) { ... }

    public record Usage(String type, long costCents) {
    }
}
```

## Was der Architekturtest daraus macht

Je nach Anordnung gilt Folgendes:

| Anordnung | `invoicing` benutzt `usage` | andere Module benutzen `usage` |
|---|---|---|
| gewöhnliche Unterpakete von `billing` | alles erlaubt, auch `usage.internal` | verboten |
| `usage` und `invoicing` als eigene Module neben `billing` | nur die API | die API ist für jedes Modul erlaubt |
| Nested Modules | nur die API | verboten |

Ausprobiert am 2026-11-01 mit Spring Modulith 2.1.2:

- Benutzt `InvoiceGenerator` das `UsageRepository` direkt, meldet der Test `Module 'billing.invoicing' depends on non-exposed type nh.demo.plantify.billing.usage.internal.UsageRepository within module 'billing.usage'!`
- Mit `Usages` ist der Test grün.
- Ein Zugriff aus `admin` auf `Usages` meldet `Invalid sub-module reference from module 'admin' to module 'billing.usage'`, auch mit `@NamedInterface` an `Usages`.

Für die Architektur funktioniert die Gliederung also wie gedacht.

## Die Grenzen heute

Nested Modules vertragen sich in Spring Modulith 2.1.2 weder mit Flyway je Modul noch mit den Modultests.

**Flyway je Modul.** Spring Modulith sucht die Migrationen von `billing.usage` unter `db/migration/billing/usage`. Dabei gibt es folgende Hindernisse:

- Bevor es migriert, prüft es, ob es unter `db/migration/billing.usage` Dateien gibt, mit Punkt statt Schrägstrich. Es findet keine und lässt das Modul ohne Meldung aus. Das beschreibt Issue #1962 samt Ursache im Code. Die Historientabelle hieße außerdem `flyway_schema_history_billing.usage`, mit Punkt im Namen.
- Flyway durchsucht `db/migration/billing` samt Unterverzeichnissen. Liegen Migrationen in `billing/usage` und `billing/invoicing`, findet die Migration von `billing` alle und bricht ab: `Found more than one migration with version 1`.

Issue #1962 nennt als Umweg eine eigene Kennung ohne Punkt, etwa `@ApplicationModule(id = "usage")`. Issue #1545 wünscht sich, Nested Modules bei Flyway ganz auszulassen, weil sie meist das Schema ihres übergeordneten Moduls teilen. Beide sind offen.

**Modultests.** Es gibt folgende Stolperstellen:

- Ein `@ApplicationModuleTest` im Paket `billing` startet die Nested Modules nicht mit, es fehlt etwa das `UsageRepository`.
- Liegt der Test im Paket `billing.usage`, startet er `billing` nicht mit, und die Migrationen unter `db/migration/billing` laufen nicht. Es fehlt die Tabelle `usage_records`.
- Mit `extraIncludes = "billing"` wird das Paket samt Unterpaketen ein zweites Mal gescannt, und der Start bricht mit `BeanDefinitionOverrideException` für `usageRepository` ab.

**Documenter.** In `components.puml` verschwindet `billing` ganz, und mit ihm jedes Modul, das nur mit `billing` verbunden ist. Die Nested Modules bekommen eigene Dateien, etwa `module-billing.usage.puml`.

Die Doku beschreibt Nested Modules nur bei den Grundlagen. Bei Tests, Flyway und Dokumentation erwähnt sie sie nicht.

## Fragen

- Wann gliederst du ein Modul mit Nested Modules, und wann schneidest du es in zwei Module nebeneinander?
- Was müsste `billing` tun, wenn ein anderes Modul die Nutzung abfragen will?

## Material

- Spring Modulith, Nested Application Modules: https://docs.spring.io/spring-modulith/reference/fundamentals.html#modules.nested
- Issue #1962, Module-aware Flyway silently skips migrations of nested application modules: https://github.com/spring-projects/spring-modulith/issues/1962
- Issue #1545, Skip nested modules when configuring flyway: https://github.com/spring-projects/spring-modulith/issues/1545
- Issue #578, Add support for nested modules: https://github.com/spring-projects/spring-modulith/issues/578
