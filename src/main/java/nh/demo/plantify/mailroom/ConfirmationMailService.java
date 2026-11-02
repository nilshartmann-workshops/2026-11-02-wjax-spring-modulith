package nh.demo.plantify.mailroom;


import nh.demo.plantify.plant.PlantRegisteredEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.modulith.events.ApplicationModuleListener;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;

@Service
class ConfirmationMailService {

    private static final Logger log = LoggerFactory.getLogger(ConfirmationMailService.class);

    @ApplicationModuleListener
    void onPlantRegistered(PlantRegisteredEvent event) {

//        if (true) {
//            throw new IllegalStateException("Keine Mail heute :-(");
//        }

        log.info("""
            
            
            📧
            📧 Confirmation mail sent for plant '{}'
            📧
            
            """, event.name());


    }
}