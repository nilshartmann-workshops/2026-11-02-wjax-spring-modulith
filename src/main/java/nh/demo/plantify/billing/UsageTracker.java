package nh.demo.plantify.billing;

import nh.demo.plantify.plant.PlantRegisteredEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.event.EventListener;
import org.springframework.modulith.events.ApplicationModuleListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.event.TransactionalEventListener;

import java.time.Instant;
import java.util.UUID;

@Service
public class UsageTracker {

    private static final Logger log = LoggerFactory.getLogger(UsageTracker.class);
    private final UsageRepository usageRepository;

    UsageTracker(UsageRepository usageRepository) {
        this.usageRepository = usageRepository;
    }

    @ApplicationModuleListener
    void onPlantRegistered(PlantRegisteredEvent event) {
        registerSetupFee(event.plantId(), event.ownerId());
    }

    private void registerSetupFee(UUID plantId, UUID ownerId) {
        UsageRecord usageRecord = new UsageRecord(
            ownerId,
            UsageRecord.UsageType.SETUP_FEE,
            Instant.now(),
            1000L
        );

        usageRepository.save(usageRecord);

        log.info("""
            
            
            🤑
            🤑 Setup Fee registered for plant '{}'
            🤑
            
            """, plantId);
    }

}
