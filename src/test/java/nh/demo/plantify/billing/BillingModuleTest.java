package nh.demo.plantify.billing;

import nh.demo.plantify.TestIds;
import nh.demo.plantify.plant.PlantRegisteredEvent;
import nh.demo.plantify.plant.PlantType;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.modulith.test.ApplicationModuleTest;
import org.springframework.modulith.test.Scenario;
import org.springframework.test.context.ActiveProfiles;

import static org.assertj.core.api.Assertions.assertThat;

@ApplicationModuleTest(ApplicationModuleTest.BootstrapMode.DIRECT_DEPENDENCIES)
@ActiveProfiles("test")
class BillingModuleTest {

    @Autowired
    UsageRepository usageRepository;

    @Test
    void registers_setup_fee_for_registered_plant(Scenario scenario) {

        // 🕵️‍♂️ Erzählen: der Listener arbeitet asynchron und erst nach einem Commit, publishEvent im Test reicht nicht

        scenario.publish(new PlantRegisteredEvent(
                TestIds.randomPlantId,
                TestIds.existingOwnerId,
                "Monstera",
                PlantType.SUMMER_FLOWERS,
                "Schlafzimmer"))
            .andWaitForStateChange(
                () -> usageRepository.findByOwnerId(TestIds.existingOwnerId),
                usageRecords -> !usageRecords.isEmpty())
            .andVerify(usageRecords -> assertThat(usageRecords)
                .singleElement()
                .satisfies(usageRecord -> {
                    assertThat(usageRecord.getUsageType()).isEqualTo(UsageRecord.UsageType.SETUP_FEE);
                    assertThat(usageRecord.getCostCents()).isPositive();
                }));
    }

}
