# Named Interfaces per Konvention, mit einer eigenen Detection Strategy

Diese Datei ist deine Mitschrift. Der Code unten steht nicht im Repository.

## Die Strategie

Spring Modulith fragt eine `ApplicationModuleDetectionStrategy`, welche Pakete Module sind und welche Named Interfaces sie haben. Der Standard nimmt jedes direkte Unterpaket als Modul und liest dazu `@NamedInterface`. Eine eigene Strategie legt beides per Konvention fest. Diese hier macht jedes Unterpaket `suggestions` zu einem Named Interface, ohne Annotation:

```java
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
```

## Einschalten

Eingeschaltet wird die Strategie mit ihrem Klassennamen in der `application.properties`:

```properties
spring.modulith.detection-strategy=nh.demo.plantify.PlantifyModuleDetectionStrategy
```

Die Klasse gehört in `src/main/java`, weil auch die laufende Anwendung die Module ermittelt, etwa für Flyway je Modul. Dafür muss `spring-modulith-core` in der `pom.xml` stehen, denn der Starter bringt es nur zur Laufzeit mit:

```xml
<dependency>
    <groupId>org.springframework.modulith</groupId>
    <artifactId>spring-modulith-core</artifactId>
</dependency>
```

Danach entfällt `@NamedInterface("suggestions")` an `CareSuggestionFactory`.

## Ausprobiert am 2026-11-02 mit Spring Modulith 2.1.2

Auf dem Stand mit dem Modul `rose` ergab sich Folgendes:

- Mit der Strategie und ohne die Annotation ist der Architekturtest grün. Die Angabe `"care :: suggestions"` in den `allowedDependencies` von `rose` greift weiter.
- Ohne `matching("suggestions")` scheitert schon `ApplicationModules.of(...)` mit `No named interface named 'suggestions' found! Original dependency declaration: rose -> care :: suggestions.` Das gilt auch dann, wenn `@NamedInterface` wieder an der Factory steht. Eine eigene `detectNamedInterfaces` ersetzt die Erkennung der Annotationen also ganz.

## Paket statt Typ

Mit der Konvention ist das ganze Paket `care.suggestions` das Named Interface, also auch `CareSuggestionService` und `CareSuggestionDefaultFactory`. Mit der Annotation an der Factory sind es nur die Factory und die Typen ihrer Signatur.

## Fragen

- Wann lohnt sich eine Konvention statt einer Annotation an jeder Stelle?
- Woran erkennt jemand, der den Code liest, dass `care.suggestions` zur API gehört?

## Material

- Spring Modulith, Customizing Module Detection: https://docs.spring.io/spring-modulith/reference/fundamentals.html#customizing-modules
- Spring Modulith, Customizing Named Interface Detection: https://docs.spring.io/spring-modulith/reference/fundamentals.html#customizing-named-interfaces
- Spring Modulith, Named Interfaces: https://docs.spring.io/spring-modulith/reference/fundamentals.html#modules.named-interfaces
