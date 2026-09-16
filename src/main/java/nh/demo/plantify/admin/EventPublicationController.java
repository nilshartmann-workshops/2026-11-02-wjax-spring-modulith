package nh.demo.plantify.admin;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.modulith.events.EventPublication;
import org.springframework.modulith.events.FailedEventPublications;
import org.springframework.modulith.events.ResubmissionOptions;
import org.springframework.modulith.events.core.EventPublicationRepository;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

// 🕵️‍♂️ Erzählen: eigenes Modul, die Registry gehört keiner Fachlichkeit
@RestController
@RequestMapping("/api/admin/event-publications")
class EventPublicationController {

    private static final Logger log = LoggerFactory.getLogger(EventPublicationController.class);

    private final EventPublicationRepository eventPublicationRepository;
    private final FailedEventPublications failedEventPublications;

    EventPublicationController(EventPublicationRepository eventPublicationRepository,
                               FailedEventPublications failedEventPublications) {
        this.eventPublicationRepository = eventPublicationRepository;
        this.failedEventPublications = failedEventPublications;
    }

    // 🕵️‍♂️ Erzählen: die öffentliche API listet nur abgeschlossene Publications, EventPublicationRepository ist kein dokumentiertes API
    @GetMapping
    List<Map<String, Object>> findByStatus(@RequestParam(defaultValue = "FAILED") EventPublication.Status status) {
        return eventPublicationRepository.findByStatus(status)
            .stream()
            .map(publication -> Map.<String, Object>of(
                "listener", publication.getTargetIdentifier().getValue(),
                "status", publication.getStatus(),
                "completionAttempts", publication.getCompletionAttempts(),
                "event", publication.getEvent()
            ))
            .toList();
    }

    // 🕵️‍♂️ Erzählen: nur FAILED, gedrosselt, und was schon zu oft gescheitert ist, bleibt liegen
    @PostMapping("/resubmit")
    void resubmitFailed(@RequestParam(defaultValue = "3") int maxAttempts) {
        log.info("🔁 Resubmitting failed event publications with less than {} attempts ...", maxAttempts);

        failedEventPublications.resubmit(
            ResubmissionOptions.defaults()
                .withBatchSize(50)
                .withMaxInFlight(200)
                .withFilter(publication -> publication.getCompletionAttempts() < maxAttempts)
        );
    }
}
