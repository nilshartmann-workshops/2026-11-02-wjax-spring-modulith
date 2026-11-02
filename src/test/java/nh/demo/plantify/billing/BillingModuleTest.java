package nh.demo.plantify.billing;

import nh.demo.plantify.TestIds;
import nh.demo.plantify.plant.PlantRegisteredEvent;
import nh.demo.plantify.plant.PlantType;
import org.apache.kafka.shaded.com.google.protobuf.Duration;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.modulith.moments.support.TimeMachine;
import org.springframework.modulith.test.ApplicationModuleTest;
import org.springframework.modulith.test.Scenario;
import org.springframework.test.context.ActiveProfiles;

import static org.assertj.core.api.Assertions.assertThat;

@ApplicationModuleTest(mode = ApplicationModuleTest.BootstrapMode.DIRECT_DEPENDENCIES)
@ActiveProfiles("test")
public class BillingModuleTest {

    @Autowired
    UsageTracker usageTracker;

    @Autowired
    UsageRepository usageRepository;

    @Autowired
    private ApplicationEventPublisher applicationEventPublisher;

    @Test
    void setup_fee_is_inserted_in_db(Scenario scenario) {

        TimeMachine timeMachine;

        timeMachine.shiftBy()

        scenario.publish(new PlantRegisteredEvent(
            TestIds.randomPlantId,
            TestIds.existingOwnerId,
            "Monstera",
            PlantType.SUMMER_FLOWERS,
            "Schlafzimmer"))
            .andWaitForStateChange(
                () -> usageRepository.findByOwnerId(TestIds.existingOwnerId),
                usageRecords -> !usageRecords.isEmpty()
            )
            .andVerify(usageRecords -> {
                assertThat(usageRecords)
                    .singleElement()
                    .satisfies(ur -> {
                        assertThat(ur.getUsageType()).isEqualTo(UsageRecord.UsageType.SETUP_FEE);
                    });
            });




        // ...
    }

}
