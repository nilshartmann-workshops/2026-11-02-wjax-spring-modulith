package nh.demo.plantify.mailroom;

import nh.demo.plantify.plant.PlantRegisteredEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.modulith.events.ApplicationModuleListener;
import org.springframework.stereotype.Service;

// 🕵️‍♂️ Erzählen: ein neues Modul hört zu, und in plant ändert sich keine Zeile
// 🕵️‍♂️ Zeigen: components.puml, mailroom "listens to" plant
@Service
class ConfirmationMailService {

    private static final Logger log = LoggerFactory.getLogger(ConfirmationMailService.class);

    @ApplicationModuleListener
    void onPlantRegistered(PlantRegisteredEvent event) {
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
