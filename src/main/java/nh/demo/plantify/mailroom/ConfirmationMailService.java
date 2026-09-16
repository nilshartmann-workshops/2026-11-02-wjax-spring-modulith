package nh.demo.plantify.mailroom;

import nh.demo.plantify.plant.PlantRegisteredEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.modulith.events.ApplicationModuleListener;
import org.springframework.stereotype.Service;

import java.time.Duration;

// 🕵️‍♂️ Erzählen: ein neues Modul hört zu, und in plant ändert sich keine Zeile
// 🕵️‍♂️ Zeigen: components.puml, mailroom "listens to" plant
@Service
class ConfirmationMailService {

    private static final Logger log = LoggerFactory.getLogger(ConfirmationMailService.class);

    private final Duration delay;

    ConfirmationMailService(@Value("${plantify.mail.delay:0s}") Duration delay) {
        this.delay = delay;
    }

    @ApplicationModuleListener
    void onPlantRegistered(PlantRegisteredEvent event) {
        // 🕵️‍♂️ Zeigen: mit --plantify.mail.delay=30s starten, der Mailserver antwortet nicht
        try {
            Thread.sleep(delay);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        log.info("""
            
            
            📧
            📧 Confirmation mail sent for plant '{}'
            📧
            
            """, event.name());

        // 🕵️‍♂️ Zeigen: einkommentieren, Pflanze anlegen, plants, care_tasks und usage_records gefüllt
        // 🕵️‍♂️ Zeigen: event_publication, je Listener ein Eintrag, zwei COMPLETED und einer FAILED
        // 🕵️‍♂️ Zeigen: auskommentieren und neu starten, nur die Poststelle läuft noch einmal
        // 🕵️‍♂️ Fallstrick: die 📧-Zeile steht danach zweimal im Log, die Mail geht at-least-once hinaus
        // if (true) { throw new IllegalStateException("No delivery receipt from mail server"); }
    }
}
