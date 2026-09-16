package nh.demo.plantify.mailroom;

import org.springframework.modulith.events.Externalized;

import java.util.UUID;

// 🕵️‍♂️ Zeigen: docker compose up -d, IntelliJ, Kafka-Plugin, Topic mail-requests, dann add-plant.sh: eine Pflanze, eine Nachricht
// ./read-mail-requests.sh zum Lesen des Topics
// 🕵️‍♂️ Erzählen: Topic vor den ::, dahinter der Message-Key als SpEL
// 🕵️‍♂️ Zeigen: docker stop wjax-plantify-kafka, Pflanze anlegen: 200, aber keine Nachricht, und nach einer Minute steht der Eintrag in event_publication auf FAILED
// 🕵️‍♂️ Zeigen: docker start wjax-plantify-kafka, erst der Neustart der Anwendung liefert nach
@Externalized(target = "mail-requests::#{#this.plantId()}")
record MailRequestedEvent(UUID plantId, String plantName) {
}
