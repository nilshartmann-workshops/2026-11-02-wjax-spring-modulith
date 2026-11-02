package nh.demo.plantify.care;

import nh.demo.plantify.TestIds;
import nh.demo.plantify.care.suggestions.CareSuggestion;
import nh.demo.plantify.plant.PlantType;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.modulith.test.ApplicationModuleTest;
import org.springframework.test.context.ActiveProfiles;

@ApplicationModuleTest(verifyAutomatically = false, extraIncludes = {"rose"})
@ActiveProfiles("test")
public class CareModuleTest {

    @Autowired
    private CareService careService;

    @Autowired
    private CareTaskRepository careTaskRepository;

    @Test
    void creates_care_tasks() {
        careService.setupInitialCareTasks(
            TestIds.randomPlantId,
            PlantType.ROSES,
            "Lobby"
        );

        var tasks = careTaskRepository.findAll();
        tasks.forEach(c -> {
            System.out.printf("Interval: %d Type: %s%n", c.getInterval(), c.getType());
        });

    }


}
