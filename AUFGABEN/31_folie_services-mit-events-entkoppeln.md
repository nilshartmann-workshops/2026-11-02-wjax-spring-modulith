# Services mit Events entkoppeln

Die Folien zeigen, was die Kopplung über Services kostet und was Events daran ändern:

- **Kopplung über Services.** Eine Erweiterung ändert zwei Stellen, und die Tests brauchen die Beans der Nachbarn. Das verletzt das Open-Closed-Prinzip.
- **Eine Transaktion für alles.** Sie garantiert Konsistenz, wird aber zum Ressourcenproblem, weil sie so lange dauert wie alle Beteiligten zusammen.
- **Entkopplung über Events.** Ein Modul veröffentlicht, was geschehen ist. Neue Empfänger kommen dazu, ohne dass sich der Sender ändert.
- **Eigene Transaktionen.** Die Empfänger arbeiten asynchron, jeder in seiner eigenen Transaktion.

## Material

- Spring Modulith, Working with Application Events: https://docs.spring.io/spring-modulith/reference/events.html
