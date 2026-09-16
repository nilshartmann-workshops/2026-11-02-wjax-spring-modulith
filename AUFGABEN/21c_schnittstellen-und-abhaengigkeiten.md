# Ein neues Modul, verborgenes Innenleben, eine Schnittstelle und erlaubte Abhängigkeiten


## Dateien

- `rose/RoseCareSuggestionFactory.java` (neu)
- `rose/package-info.java` (neu)
- `care/CareSuggestion.java`, `care/CareSuggestionFactory.java`, `care/CareSuggestionDefaultFactory.java`, `care/CareSuggestionService.java` (wandern in ein neues Paket)
- `care/CareService.java` (Imports)

## Aufgabe

Rosen brauchen mehr Pflege, als die Vorgaben von Plantify vorsehen. Leg dafür ein neues Modul `rose` an, das eigene Pflegevorschläge für Rosen liefert. Verbirg danach das Innenleben von `care` und öffne genau so viel davon, wie `rose` braucht. Schreib zuletzt am Modul `rose` auf, was es benutzen darf.

Starte nach jedem Teil den Architekturtest.

## Vorbereitung

Die Übung setzt auf meinem Stand auf. Sichere deine eigenen Änderungen und wechsle auf den Branch `live_coding`:

```bash
git stash -u
git fetch origin
git switch -C live_coding origin/live_coding
```

Deine Änderungen liegen danach im Stash, `git stash list` zeigt sie. Starte den Architekturtest. Er ist grün, denn ein Filter blendet den bekannten Zyklus zwischen `plant` und `care` aus.

## Teil 1: Ein neues Modul anlegen

`care` holt sich seine Pflegevorschläge bei allen Spring Beans, die `CareSuggestionFactory` implementieren, und nimmt je Art der Pflege den Vorschlag mit der höchsten `confidence`. Die eingebaute `CareSuggestionDefaultFactory` liefert für jede Pflanze Umtopfen und Gießen alle fünf Tage, beides mit `confidence` 1.

1. Leg neben `plant`, `care` und den anderen ein neues Paket `rose` an. Allein dadurch ist es für Spring Modulith ein Application Module.

2. Leg darin die Klasse `RoseCareSuggestionFactory` an und übernimm sie vollständig:

   ```java
   package nh.demo.plantify.rose;

   import nh.demo.plantify.care.CareSuggestion;
   import nh.demo.plantify.care.CareSuggestionFactory;
   import nh.demo.plantify.plant.PlantType;
   import nh.demo.plantify.shared.CareTaskType;
   import org.springframework.stereotype.Component;

   import java.util.List;

   @Component
   class RoseCareSuggestionFactory implements CareSuggestionFactory {

       @Override
       public List<CareSuggestion> createSuggestion(PlantType plantType, String location) {
           if (plantType != PlantType.ROSES) {
               return List.of();
           }

           return List.of(
               CareSuggestion.recurring(CareTaskType.WATERING, 10, 3),
               CareSuggestion.recurring(CareTaskType.FERTILIZING, 10, 14),
               CareSuggestion.recurring(CareTaskType.PRUNING, 10, 30)
           );
       }
   }
   ```

3. Starte den Architekturtest. Er ist grün: `CareSuggestion` und `CareSuggestionFactory` liegen direkt im Paket `care`, sind `public` und damit API des Moduls.

4. Gib `RoseCareSuggestionFactory` das `OwnerRepository` aus dem Modul `owner` in den Konstruktor:

   ```java
   private final OwnerRepository ownerRepository;

   RoseCareSuggestionFactory(OwnerRepository ownerRepository) {
       this.ownerRepository = ownerRepository;
   }
   ```

   Starte den Architekturtest. Er bleibt grün, denn `OwnerRepository` liegt direkt im Modulpaket von `owner` und ist `public`, also ebenfalls API.

5. Starte die Anwendung und leg über Swagger UI eine Pflanze mit `"plantType": "ROSES"` an. Öffne die H2-Console unter http://localhost:8080/h2-console (JDBC URL `jdbc:h2:./data/plantify;AUTO_SERVER=TRUE;MODE=PostgreSQL`, Benutzer `sa`, kein Passwort). In der Tabelle `CARE_TASKS` steht Gießen alle drei Tage statt alle fünf, dazu Düngen und Schneiden.

## Teil 2: Das Innenleben verbergen

Die Pflegevorschläge sind Innenleben von `care`. Der Weg, auf dem `care` seine Vorschläge zusammensucht, geht kein anderes Modul etwas an.

6. Leg unterhalb von `care` ein Paket `suggestions` an und verschiebe die Klassen `CareSuggestion`, `CareSuggestionDefaultFactory`, `CareSuggestionFactory` und `CareSuggestionService` dorthin. Markiere sie dafür in IntelliJ und nimm "Refactor → Move" (F6), die Imports zieht die IDE mit.

7. Der Compiler meldet sich: `CareService` liegt weiter in `care` und kommt nicht mehr an `CareSuggestionService`. Mach die Klasse `public`. Damit ist sie in Java für die ganze Anwendung sichtbar.

8. Starte den Architekturtest. Er ist rot, und die Meldung lautet:

   ```
   Module 'rose' depends on non-exposed type
     nh.demo.plantify.care.suggestions.CareSuggestionFactory within module 'care'!
   Module 'rose' depends on non-exposed type
     nh.demo.plantify.care.suggestions.CareSuggestion within module 'care'!
   ```

9. Am Code von `rose` haben sich nur die Imports geändert, und er übersetzt weiter. Warum ist er trotzdem ein Verstoß? Was ist für Modulith der Unterschied zwischen `care` und `care.suggestions`?

## Teil 3: Die Schnittstelle öffnen

`rose` braucht die Factory. Erkläre sie zur Schnittstelle.

10. Setz `@NamedInterface("suggestions")` an die Schnittstelle `CareSuggestionFactory`:

    ```java
    @NamedInterface("suggestions")
    public interface CareSuggestionFactory {
    ```

    Der Test ist grün.

11. `CareSuggestion` hast du **nicht** annotiert, und trotzdem darf `rose` den Typ benutzen. Sieh dir die Signatur von `createSuggestion` an. Welche Typen bringt ein Named Interface mit?

12. Sieh dir `CareSuggestionService` an: Die Klasse ist `public`, weil `CareService` sie braucht, und für jedes andere Modul ist sie unerreichbar. Die Schnittstelle eines Moduls ist damit etwas anderes als das, was in Java sichtbar ist.

## Teil 4: Abhängigkeiten erlauben

Bis hierhin hat `care` bestimmt, was es hergibt. Jetzt bestimmt `rose`, was es benutzen will.

13. Leg in `rose` eine `package-info.java` an und schreib auf, was das Modul benutzen darf. Fang mit der Factory an:

    ```java
    @ApplicationModule(
        displayName = "Individual Care Tasks for Roses 🌹",
        allowedDependencies = {"care :: suggestions"}
    )
    package nh.demo.plantify.rose;

    import org.springframework.modulith.ApplicationModule;
    ```

    Der Name hinter `::` ist der Name des Named Interface.

14. Starte den Architekturtest. Er ist rot und nennt jede Abhängigkeit, die `allowedDependencies` nicht erlaubt. Ergänze `allowedDependencies` um die Module, die `rose` für seine Pflegevorschläge braucht. Danach meldet der Test nur noch das `OwnerRepository`:

    ```
    Module 'rose' depends on module 'owner' via
      nh.demo.plantify.rose.RoseCareSuggestionFactory -> nh.demo.plantify.owner.OwnerRepository.
      Allowed targets: care :: suggestions, plant, shared.
    ```

    In Schritt 4 war derselbe Zugriff erlaubt. Rot ist er allein deshalb, weil `rose` jetzt aufgeschrieben hat, was es benutzen will.

15. Bau das `OwnerRepository` wieder aus, samt Import und Konstruktor. Der Test ist grün.

## 🧐 Optional: Ein offenes Modul

`shared` ist ein Modul wie jedes andere: Andere Module dürfen nur die öffentlichen Typen benutzen, die direkt im Paket `shared` liegen. `shared.text` und `shared.config` sind Innenleben.

16. Prüfe in `CareSuggestionService` den Standort mit `TextUtils.hasText(location)` aus `nh.demo.plantify.shared.text`, statt mit `isBlank`. Starte den Architekturtest. Er ist rot.

17. Leg in `shared` eine `package-info.java` an und erkläre das Modul für offen:

    ```java
    @ApplicationModule(type = ApplicationModule.Type.OPEN)
    package nh.demo.plantify.shared;

    import org.springframework.modulith.ApplicationModule;
    ```

    Starte den Architekturtest. Er ist grün.

18. Lass `TextUtils` etwas aus `plant` benutzen, zum Beispiel eine Konstante vom Typ `PlantType`. `plant` benutzt seinerseits `ResourceNotFoundException` aus `shared`. Starte den Architekturtest. Nimm danach `type = ApplicationModule.Type.OPEN` heraus und starte ihn noch einmal.

19. Was verliert ein Modul, wenn es offen ist?

20. Nimm die Änderungen dieses Teils wieder heraus: die Konstante in `TextUtils`, die `package-info.java` in `shared` und den Aufruf in `CareSuggestionService`.

## Material

- Spring Modulith, Named Interfaces: https://docs.spring.io/spring-modulith/reference/fundamentals.html#modules.named-interfaces
- Spring Modulith, erlaubte Abhängigkeiten: https://docs.spring.io/spring-modulith/reference/fundamentals.html#modules.explicit-dependencies
- Spring Modulith, offene Module: https://docs.spring.io/spring-modulith/reference/fundamentals.html#modules.open

## Hintergrund

**Was ist ein Named Interface?** Ein benannter Ausschnitt aus einem Modul, den andere Module benutzen dürfen. Ohne ein solches Interface gilt: Ein Typ, der direkt im Modulpaket liegt und `public` ist, ist API, und alles in Unterpaketen ist Innenleben. Mit einem Named Interface entscheidest du, welche Typen aus einem Unterpaket nach außen gehören, und der Rest bleibt verborgen, auch wenn Java ihn sieht.

**Warum erlaubte Abhängigkeiten?** Ohne sie darf ein Modul jedes andere Modul benutzen, solange es dessen API trifft. Das ist selten gewollt: Je weniger ein Modul kennt, desto leichter lässt es sich ändern, testen und im Zweifel herausziehen. `allowedDependencies` schreibt diese Absicht auf, und der Architekturtest hält sie fest. Jede neue Abhängigkeit wird damit zu einer bewussten Entscheidung, und dazu ist `allowedDependencies` da.

**Offene Module.** Ein offenes Modul gibt alles her, auch die Typen in seinen Unterpaketen. Außerdem nimmt Spring Modulith es von der Prüfung auf Zyklen aus: Ein Zyklus über ein offenes Modul bleibt unbemerkt. Gedacht ist `Type.OPEN` für Altbestand, der Schritt für Schritt in eine saubere Paketstruktur wandert.
