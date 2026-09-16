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

        // 🕵️‍♂️ Erzählen: der Filter ist weg, und verify() ist trotzdem grün
        // 🕵️‍♂️ Zeigen: components.puml, care "listens to" plant, und von plant nach care führt kein Pfeil mehr
    }

}
