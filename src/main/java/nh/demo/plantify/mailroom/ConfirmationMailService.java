package nh.demo.plantify.mailroom;

import nh.demo.plantify.plant.PlantRegisteredEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;

// 🕵️‍♂️ Erzählen: ein neues Modul hört zu, und in plant ändert sich keine Zeile
// 🕵️‍♂️ Zeigen: components.puml, mailroom "listens to" plant
@Service
class ConfirmationMailService {

    private static final Logger log = LoggerFactory.getLogger(ConfirmationMailService.class);

    @EventListener
    void onPlantRegistered(PlantRegisteredEvent event) {
        log.info("""
            
            
            📧
            📧 Confirmation mail sent for plant '{}'
            📧
            
            """, event.name());
    }
}
