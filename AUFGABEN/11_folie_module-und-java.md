# Module, und die Grenzen von Java

Die Folien zeigen, was ein Modul ausmacht und was Java dafür fehlt:

- **Monolith, Microservices, Modulith.** Der Modulith hat wie der Monolith eine Codebasis und ein Deployment, aber klare fachliche Module mit loser Kopplung, ohne die Betriebskosten von Microservices.
- **Merkmale eines Moduls.** Eine fachliche Verantwortung, hohe Kohäsion, verborgenes Innenleben und nach außen eine explizite Schnittstelle.
- **Hexagonale Architektur.** Sie gliedert die Anwendung in technische Schichten, der Modulith in fachliche Module. Innerhalb eines Moduls lassen sich beide verbinden.
- **Packages als Module.** Mit Java-Bordmitteln bleibt offen, wie die Abhängigkeiten aussehen und welche Klassen die Schnittstelle eines Moduls bilden. Die Sichtbarkeit von Klassen hilft nur innerhalb eines Pakets.
- **Die Lücke.** Es fehlen echte Module, klare Schnittstellen und explizite Abhängigkeiten. Diese Lücke füllt Spring Modulith.

## Material

- Spring Modulith, Grundlagen: https://docs.spring.io/spring-modulith/reference/fundamentals.html
