# Die Architektur sichtbar machen, und eine Lösung für den Zyklus finden

## Dateien

- `src/test/java/nh/demo/plantify/PlantifyModuleTest.java`
- für deine Lösung: was immer du dafür brauchst

## Aufgabe

Vervollständige einen Test, der die Modulstruktur der Anwendung aufzeichnet und gegen die Regeln von Spring Modulith prüft. Der Test findet ein Problem. Such die Ursache im Code und überleg dir eine Lösung.

## Schritte

### Teil 1: Die Struktur aufzeichnen

1. Öffne im Test-Verzeichnis die Klasse `PlantifyModuleTest`.

2. Fülle die Methode `write_documentation`:

   ```java
   @Test
   void write_documentation() {
       var modules = ApplicationModules.of(PlantifyApplication.class);

       new Documenter(modules).writeDocumentation();
   }
   ```

   `ApplicationModules` kommt aus `org.springframework.modulith.core`, `Documenter` aus `org.springframework.modulith.docs`.

3. Führe den Test aus. Öffne danach `target/spring-modulith-docs/components.puml`, das PlantUML-Diagramm deiner Anwendung.
   - In IntelliJ öffnet das Plugin "PlantUML Integration" die Datei direkt.
   - Ohne Plugin kopiere den Inhalt in einen Online-Viewer, zum Beispiel https://www.plantuml.com/plantuml
   - Ohne Viewer lies die Zeilen mit `Rel(...)`: Jede ist ein Pfeil von einem Modul zu einem anderen.

### Teil 2: Die Struktur prüfen

4. Fülle die Methode `verify_modules`. Das ist der Architekturtest, er prüft die Struktur gegen die Regeln von Modulith:

   ```java
   @Test
   void verify_modules() {
       var modules = ApplicationModules.of(PlantifyApplication.class);

       modules.verify();
   }
   ```

5. Führe den Test aus und lies die Fehlermeldung. Sie nennt jede Stelle, die zum Problem beiträgt.

### Teil 3: Das Problem verstehen und lösen

6. Such die Stellen aus der Fehlermeldung im Code. Was genau ist das Problem, und warum ist es eines, obwohl die Anwendung übersetzt und läuft?

7. Überleg dir eine Lösung. **Alles ist erlaubt.** Setz sie um und lass den Test noch einmal laufen, oder skizziere sie auf Papier oder als Kommentar im Code.

8. Was kostet deine Lösung? Wer muss dafür was ändern, und was wird dadurch schwieriger oder leichter?

9. Am Ende der Übung kannst du deine Lösung vorstellen.

### 🧐 Optional: Die Dokumentation je Modul

10. Sieh dir die übrigen Dateien in `target/spring-modulith-docs` an. Neben `components.puml` schreibt der Documenter je Modul ein Diagramm `module-<name>.puml` und einen Module Canvas `module-<name>.adoc`.

11. Öffne `module-plant.adoc`. Unter "Bean references" steht, welche Beans aus anderen Modulen `plant` benutzt. Was haben sie mit dem Problem aus der Fehlermeldung zu tun?

## Material

- Spring Modulith, Architekturregeln prüfen: https://docs.spring.io/spring-modulith/reference/verification.html
- Spring Modulith, Dokumentation erzeugen: https://docs.spring.io/spring-modulith/reference/documentation.html
- Spring Modulith, Application Module Canvas: https://docs.spring.io/spring-modulith/reference/documentation.html#application-module-canvas
