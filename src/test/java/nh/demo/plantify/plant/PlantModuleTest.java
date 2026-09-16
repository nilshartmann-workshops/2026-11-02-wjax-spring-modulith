package nh.demo.plantify.plant;

import nh.demo.plantify.TestIds;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.modulith.test.ApplicationModuleTest;
import org.springframework.modulith.test.AssertablePublishedEvents;
import org.springframework.test.context.ActiveProfiles;

import static org.assertj.core.api.Assertions.assertThat;

// 🕵️‍♂️ Erzählen: plant startet allein, keine Mocks, die Nachbarn hören nur noch zu
@ApplicationModuleTest
@ActiveProfiles("test")
class PlantModuleTest {

    @Autowired
    PlantService plantService;

    @Test
    void registers_plant_and_publishes_event(AssertablePublishedEvents events) {
        var plant = plantService.registerPlant(TestIds.existingOwnerId, "Monstera", PlantType.INDOOR, "Balkon");

        assertThat(events)
            .contains(PlantRegisteredEvent.class)
            .matching(PlantRegisteredEvent::plantId, plant.getId());
    }
}
