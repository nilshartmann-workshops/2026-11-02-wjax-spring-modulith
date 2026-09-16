# Der Preis der Entkopplung, und der Weg zum Microservice

Wir reden darüber, und diese Datei ist deine Mitschrift.

## Die Kosten der Entkopplung

Mit Events und `@ApplicationModuleListener` hat jedes Modul seine eigene Transaktion. Die eine große Transaktion, die früher alles oder nichts garantiert hat, gibt es nicht mehr. Daraus folgen zwei Dinge, die keine Eigenheit von Spring Modulith sind. Sie gelten für jede Anwendung, die über Events kommuniziert, und werden hier nur sichtbar.

## Eventual Consistency

Der Aufrufer bekommt seine Antwort, sobald seine eigene Transaktion committet ist. Die Listener laufen danach. Für einen kurzen Moment ist die Pflanze gespeichert, aber es gibt noch keine Pflegeaufgaben und keine Einrichtungsgebühr.

- **Wer sofort nachfragt, sieht einen Zwischenstand.** Eine Oberfläche, die direkt nach dem Anlegen die Pflegeaufgaben lädt, findet womöglich noch keine.
- **Irgendwann stimmt alles wieder.** Das garantiert die Event Publication Registry, denn ein Event, das ein Listener nicht verarbeitet hat, bleibt dort stehen und wird erneut zugestellt.
- **Die Frage ist fachlich.** Ob ein Zwischenstand von ein paar Millisekunden stört, entscheidet die Fachlichkeit und nicht die Technik. Für eine Einrichtungsgebühr ist er meist egal, für eine Kontobuchung oft nicht.

## At-least-once und idempotente Listener

Die Registry stellt ein Event mindestens einmal zu, aber nicht genau einmal. Ein Listener kann also dasselbe Event zweimal bekommen, etwa wenn er abbricht und nach dem Neustart erneut aufgerufen wird.

Für die Arbeit in der Datenbank ist das unkritisch, solange sie in der Transaktion des Listeners liegt: Bricht er ab, wird sie zurückgerollt. Alles außerhalb der Transaktion lässt sich dagegen nicht zurückrollen:

- eine verschickte Mail
- ein Aufruf eines fremden Dienstes
- eine Nachricht an einen Message Broker

Beim zweiten Aufruf passiert es ein zweites Mal. Dazu kommt ein seltener, aber möglicher Fall: Der Listener committet seine Arbeit, und die Anwendung stürzt ab, bevor die Registry den Eintrag als erledigt markiert. Dann läuft auch die Arbeit in der Datenbank zweimal.

Ein Listener muss deshalb **idempotent** sein. Derselbe Aufruf darf mehrfach kommen, und das Ergebnis ist dasselbe wie nach einem Aufruf. Dafür gibt es mehrere Wege:

- **Prüfen, ob die Arbeit schon getan ist.** Bevor `UsageTracker` eine Einrichtungsgebühr speichert, sieht er nach, ob für diese Pflanze schon eine existiert.
- **Die Datenbank prüfen lassen.** Ein Unique Constraint auf der Pflanzen-ID lässt den zweiten Eintrag scheitern.
- **Sich merken, was schon zugestellt ist.** Die Poststelle führt die IDs der Pflanzen, deren Mail schon hinausging, und überspringt eine Wiederholung. Sie merkt sich die ID erst nach dem Versand, sonst ginge eine gescheiterte Mail nie hinaus.
- **Wiederholungen hinnehmen.** Eine zweite Bestätigungsmail ist lästig, aber kein Schaden.

## Der Weg zum Microservice

Ein Modul, das sauber geschnitten ist, lässt sich später herauslösen. Die Poststelle `mailroom` ist dafür ein guter Kandidat:

- **Sie kennt nur ein Event aus `plant`.** Keine Klasse eines anderen Moduls ruft sie auf, und sie ruft keine auf.
- **Sie hat keine eigenen Tabellen.** Beim Herauslösen ist keine Datenbank zu teilen.
- **Ihr Ergebnis verlässt die Anwendung ohnehin.** Die Mail geht an einen Mailserver, und für ihn ist gleich, ob ein Modul oder ein eigener Service sie schickt.

Eine eigene Poststelle hätte ein eigenes Deployment und eine eigene Pipeline. Das `PlantRegisteredEvent`, das heute innerhalb der Anwendung veröffentlicht wird, ginge dann über den Broker, und sein Aufbau bliebe derselbe.

Der Schritt lohnt sich, wenn es einen Grund gibt, den der Modulith nicht erfüllt:

| Grund | Beispiel |
|---|---|
| Unabhängige Skalierung | Ein Rundschreiben an alle Owner erzeugt auf einen Schlag Tausende Mails, der Rest der Anwendung hat nicht mehr zu tun als sonst |
| Unabhängige Releases | Ein eigenes Team will die Poststelle mehrmals am Tag ausliefern, ohne auf die übrigen Module zu warten |
| Andere Technik | Das Modul braucht eine Datenbank oder eine Laufzeit, die zum Rest nicht passt |
| Fehlerisolation | Ein hängender Mailserver soll nicht die Pflege und die Abrechnung mitreißen |

Solange keiner dieser Gründe vorliegt, kostet der Microservice nur: Netzwerk, Deployment, Monitoring, verteilte Fehlersuche. Der Modulith hält die fachlichen Grenzen genauso ein, prüft sie bei jedem Build und lässt die Tür für den Microservice offen.

## Material

- Spring Modulith, Event Publication Registry: https://docs.spring.io/spring-modulith/reference/events.html#publication-registry
- Spring Modulith, Externalizing Events: https://docs.spring.io/spring-modulith/reference/events.html#externalization
