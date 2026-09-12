package nh.demo.plantify;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

/// Prüft, dass der Spring-Kontext hochkommt.
///
/// `@ActiveProfiles("test")` schaltet die `application-test.properties` dazu und
/// damit die Datenbank im Speicher. Ohne die Annotation läuft der Test gegen die
/// Datei-Datenbank der Anwendung unter `data/`, und zwar ohne Fehlermeldung. Jeder
/// Test, der einen Spring-Kontext startet, braucht sie deshalb.
@SpringBootTest
@ActiveProfiles("test")
class PlantifyApplicationTests {

    @Test
    void contextLoads() {
    }

}
