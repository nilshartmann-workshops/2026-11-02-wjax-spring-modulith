package nh.demo.plantify.care;

import nh.demo.plantify.care.suggestions.CareSuggestion;
import nh.demo.plantify.care.suggestions.CareSuggestionService;
import nh.demo.plantify.plant.PlantRegisteredEvent;
import nh.demo.plantify.plant.PlantType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.modulith.events.ApplicationModuleListener;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.UUID;

@Service
public class CareService {

    private static final Logger log = LoggerFactory.getLogger(CareService.class);

    private final CareTaskRepository careTaskRepository;
    private final ApplicationEventPublisher applicationEventPublisher;
    private final CareSuggestionService careSuggestionService;

    CareService(CareTaskRepository careTaskRepository, ApplicationEventPublisher applicationEventPublisher, CareSuggestionService careSuggestionService) {
        this.careTaskRepository = careTaskRepository;
        this.applicationEventPublisher = applicationEventPublisher;
        this.careSuggestionService = careSuggestionService;
    }

    // 🕵️‍♂️ Zeigen: erst @Async dazu, der Listener läuft in einem eigenen Thread
    // 🕵️‍♂️ Zeigen: dann @TransactionalEventListener mit REQUIRES_NEW, erst nach dem Commit von registerPlant
    // 🕵️‍♂️ Erzählen: @ApplicationModuleListener fasst die drei Annotationen zusammen
    // 🕵️‍♂️ Fallstrick: nur @Async, und registerPlant scheitert danach, dann bleiben die Care-Tasks trotzdem gespeichert
    @ApplicationModuleListener
    void onPlantRegistered(PlantRegisteredEvent event) {
        setupInitialCareTasks(event.plantId(), event.plantType(), event.location());
    }

    private void setupInitialCareTasks(UUID plantId, PlantType plantType, String location) {
        var suggestionsForPlant = careSuggestionService.getBestSuggestionsByPlantType(
            plantType,
            location
        );

        var careTasks = suggestionsForPlant
            .stream()
            .map(t -> createFromSuggestion(
                plantId,
                t
            ))
            .toList();

        careTaskRepository.saveAll(careTasks);

        log.info("""
            
            
            🌱
            🌱 Initial Care Tasks created for plant '{}'
            🌱
            
            """, plantId);
    }

    private CareTask createFromSuggestion(UUID plantId, CareSuggestion suggestion) {
        return switch (suggestion) {
            case CareSuggestion.OneTimeCareSuggestion s -> new CareTask(
                plantId,
                s.taskType(),
                CareTaskSource.SYSTEM,
                s.dueDate(),
                null  // kein Interval
            );
            case CareSuggestion.RecurringCareSuggestion s -> new CareTask(
                plantId,
                s.taskType(),
                CareTaskSource.SYSTEM,
                LocalDate.now().plusDays(s.intervalDays()),
                s.intervalDays()
            );
        };
    }
}
