package nh.demo.plantify.plant;

import nh.demo.plantify.TestIds;
import nh.demo.plantify.billing.UsageTracker;
import nh.demo.plantify.care.CareService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.modulith.test.ApplicationModuleTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import static org.mockito.Mockito.verify;

@ApplicationModuleTest(verifyAutomatically = false,
    mode = ApplicationModuleTest.BootstrapMode.ALL_DEPENDENCIES)
@ActiveProfiles("test")
public class PlantModuleTest {

    @Autowired
    PlantService plantService;

//    @MockitoBean
//    CareService careService;
//
//    @MockitoBean
//    UsageTracker usageTracker;

    @Test
    void registers_plant_and_calls_its_neighbours() {
        var plant = plantService
            .registerPlant(TestIds.existingOwnerId, "Monstera", PlantType.INDOOR, "Balkon");

//        verify(careService).setupInitialCareTasks(plant.getId(), PlantType.INDOOR, "Balkon");
//        verify(usageTracker).registerSetupFee(plant.getId(), TestIds.existingOwnerId);


    }

    }
