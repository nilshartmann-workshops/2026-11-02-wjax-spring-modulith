package nh.demo.plantify;

import org.springframework.modulith.core.ApplicationModuleDetectionStrategy;
import org.springframework.modulith.core.ApplicationModuleInformation;
import org.springframework.modulith.core.JavaPackage;
import org.springframework.modulith.core.NamedInterfaces;

import java.util.stream.Stream;

public class PlantifyModuleDetectionStrategy implements ApplicationModuleDetectionStrategy {

    @Override
    public Stream<JavaPackage> getModuleBasePackages(JavaPackage rootPackage) {
        // wie der Standard: jedes direkte Unterpaket ist ein Modul
        return rootPackage.getDirectSubPackages().stream();
    }

    @Override
    public NamedInterfaces detectNamedInterfaces(JavaPackage basePackage, ApplicationModuleInformation information) {
        return NamedInterfaces.builder(basePackage)
            .matching("suggestions")
            .build();
    }
}