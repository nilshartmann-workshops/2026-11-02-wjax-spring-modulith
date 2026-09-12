# Einrichten und Plantify kennenlernen

Arbeite die Schritte der Reihe nach ab. Danach läuft Plantify bei dir, und du kennst die Anwendung, an der wir heute arbeiten.

## Voraussetzungen

Du brauchst Git, ein JDK 21 oder neuer und eine Java-IDE, zum Beispiel IntelliJ IDEA.

## Schritt 1: Projekt holen und bauen

Falls noch nicht gemacht:

```bash
git clone https://github.com/nilshartmann-workshops/2026-11-02-wjax-spring-modulith.git

cd 2026-11-02-wjax-spring-modulith

./mvnw verify
```

Unter Windows in der Eingabeaufforderung `mvnw.cmd verify`. Am Ende muss `BUILD SUCCESS` stehen.

## Schritt 2: Projekt in der IDE öffnen

Öffne in IntelliJ mit "File → Open" das Verzeichnis des Repositories. IntelliJ erkennt an der `pom.xml`, dass es ein Maven-Projekt ist, und lädt die Abhängigkeiten. In einer anderen IDE importierst du das Verzeichnis als Maven-Projekt.

## Schritt 3: Anwendung starten

Öffne die Klasse `nh.demo.plantify.PlantifyApplication` und starte sie über den grünen Pfeil neben der Methode `main`. Die Anwendung lauscht auf Port 8080.

Ohne IDE startest du sie im Terminal mit `./mvnw spring-boot:run`.

## Schritt 4: Eine Pflanze anlegen

Öffne Swagger UI unter http://localhost:8080/swagger-ui.html. Klapp `POST /api/plants` aus, klick "Try it out" und dann "Execute". Als `ownerId` nimm `ee3829e4-fe2b-4d03-b2a1-70f1425d8c1c`.

**Optional:** Sieh dir die Pflanze und ihre Pflegepläne in der Datenbank an. Öffne dafür die H2-Console unter http://localhost:8080/h2-console (JDBC URL `jdbc:h2:./data/plantify;AUTO_SERVER=TRUE;MODE=PostgreSQL`, Benutzer `sa`, kein Passwort). Die Pflanze steht in der Tabelle `PLANT_SCHEMA.PLANTS`, ihre Pflegepläne stehen in `CARE_SCHEMA.CARE_TASKS`.

## Schritt 5: Den Code ansehen

Öffne in der IDE das Paket `nh.demo.plantify`. Darunter liegen die Pakete `plant`, `care`, `billing`, `owner` und `shared`.

Sieh dir in `plant/PlantService.java` die Methode `registerPlant` an. Sie legt die Pflanze an und ruft dafür `CareService` und `UsageTracker` auf.

## Meine Lösungen

Meinen Code und die Aufgaben zu jedem Thema findest du während des Workshops auf GitHub im Branch `live_coding`: https://github.com/nilshartmann-workshops/2026-11-02-wjax-spring-modulith/tree/live_coding

Die Änderungen je Thema siehst du in der Commit-Ansicht: https://github.com/nilshartmann-workshops/2026-11-02-wjax-spring-modulith/commits/live_coding/

## Gut zu wissen

- In der Bash kannst du das Script `./add-plant.sh` verwenden, um eine Pflanze anzulegen.

## Wenn etwas nicht klappt

| Symptom | Ursache |
|---|---|
| `./mvnw verify` kann nichts herunterladen | Proxy oder Firewall blockieren Maven Central. Im Firmennetz gehört der Proxy in die `~/.m2/settings.xml`. |
| `release version 21 not supported` | Maven läuft mit einem älteren JDK. Prüf `java -version` und `JAVA_HOME`. |
| `./mvnw: Permission denied` | Die Datei ist nicht ausführbar, etwa nach dem Download als ZIP. `chmod +x mvnw` behebt das. |
| IntelliJ übersetzt das Projekt nicht und meldet eine zu alte Java-Version | Das Projekt-SDK ist älter als 21. Stell es unter "File → Project Structure → Project" auf ein JDK 21 oder neuer. |
| `Port 8080 was already in use` | Eine andere Anwendung belegt den Port, etwa eine zweite Plantify-Instanz. Beende sie. |

## Hilfe?

Bleibt etwas hängen, meld dich. Ich helfe im Raum.
