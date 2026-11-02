package nh.demo.plantify;

import org.junit.jupiter.api.Test;
import org.springframework.modulith.core.ApplicationModules;
import org.springframework.modulith.core.Violation;
import org.springframework.modulith.docs.Documenter;

import java.util.function.Predicate;

class PlantifyModuleTest {

    @Test
    void write_documentation() {
        var modules = ApplicationModules.of(PlantifyApplication.class);

        new Documenter(modules).writeDocumentation();
    }

    @Test
    void verify_modules() {
        var modules = ApplicationModules.of(PlantifyApplication.class);

        modules.detectViolations()
//            .filter(Predicate.not(this::isPlantCareCycle))
            .throwIfPresent();
    }

    private boolean isPlantCareCycle(Violation violation) {
        return violation.hasMessageContaining("Cycle detected")
                      && violation.hasMessageContaining("Slice plant")
                      && violation.hasMessageContaining("Slice care");
    }

}
