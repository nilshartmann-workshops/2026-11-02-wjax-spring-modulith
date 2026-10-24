package nh.demo.plantify;

import org.junit.jupiter.api.Test;
import org.springframework.modulith.core.ApplicationModules;
import org.springframework.modulith.docs.Documenter;

class PlantifyModuleTest {

    @Test
    void write_documentation() {

        var modules = ApplicationModules
            .of(PlantifyApplication.class);

        new Documenter(modules)
            .writeDocumentation();

        // 🕵️‍♂️ Zeigen: target/spring-modulith-docs/components.puml
        // 🕵️‍♂️ Erzählen: fünf Module, und keine einzige Annotation dafür
        // 🕵️‍♂️ Erzählen: der Pfeil zwischen plant und care geht in beide Richtungen
    }

    @Test
    void verify_modules() {

        var modules = ApplicationModules
            .of(PlantifyApplication.class);

        modules.verify();

        // 🕵️‍♂️ Erzählen: rot, Cycle detected care -> plant -> care
        // 🕵️‍♂️ Erzählen: derselbe Zyklus wie im Diagramm, nur jetzt als Fehler
        // 🕵️‍♂️ Fallstrick: rot bleibt rot, der Zyklus bleibt zunächst bestehen
    }

}
