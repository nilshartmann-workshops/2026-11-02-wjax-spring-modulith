package nh.demo.plantify;

import org.junit.jupiter.api.Test;
import org.springframework.modulith.core.ApplicationModules;
import org.springframework.modulith.core.Violation;
import org.springframework.modulith.docs.Documenter;

import static java.util.function.Predicate.not;

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

        modules.detectViolations()
            .filter(not(this::isPlantCareCycle))
            .throwIfPresent();

        // 🕵️‍♂️ Erzählen: eine Lösung unter denen, die ihr gesammelt habt, und die billigste
        // 🕵️‍♂️ Erzählen: grün, obwohl der Zyklus unverändert im Diagramm steht
        // 🕵️‍♂️ Erzählen: im Bestand fängt niemand bei grün an, das Bekannte wird ausgeblendet
        // 🕵️‍♂️ Fallstrick: jede andere Verletzung färbt den Test weiter rot
    }

    private boolean isPlantCareCycle(Violation violation) {

        // Die Meldung lautet "Cycle detected: Slice care -> Slice plant -> Slice care",
        // und ausgeblendet wird allein dieser eine Zyklus.
        return violation.hasMessageContaining("Cycle detected")
            && violation.hasMessageContaining("Slice plant")
            && violation.hasMessageContaining("Slice care");
    }

}
