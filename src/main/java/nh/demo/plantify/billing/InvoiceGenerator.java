package nh.demo.plantify.billing;

import nh.demo.plantify.owner.Owner;
import nh.demo.plantify.owner.OwnerRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.*;
import java.util.List;
import java.util.UUID;
import java.util.function.Consumer;

@Component
class InvoiceGenerator {

    private static final Logger log = LoggerFactory.getLogger(InvoiceGenerator.class);
    private final UsageRepository usageRepository;
    private final InvoiceRepository invoiceRepository;

    private final ObjectProvider<Clock> clockProvider;
    private final OwnerRepository ownerRepository;

    InvoiceGenerator(UsageRepository usageRepository, InvoiceRepository invoiceRepository, ObjectProvider<Clock> clockProvider, OwnerRepository ownerRepository) {
        this.usageRepository = usageRepository;
        this.invoiceRepository = invoiceRepository;
        this.clockProvider = clockProvider;
        this.ownerRepository = ownerRepository;
    }

    // 🤔 Warum genau dieser Ausdruck? Kein "fachlicher" Zeitraum
    // 🤔 Wie würden wir das testen (manuell oder automatisch)?
    @Scheduled(cron = "0 0 2 1 * *")
    @Transactional
    void generateInvoicesForPreviousMonth() {
        YearMonth previousMonth = YearMonth
            .now(clockProvider.getIfAvailable(Clock::systemDefaultZone))
            .minusMonths(1);

        log.info("🗓️ Rechnungslauf (Cron) für {}", previousMonth);

        generateInvoicesForMonth(
            previousMonth,
            invoiceRepository::save
        );
    }

    private void generateInvoicesForMonth(YearMonth month, Consumer<Invoice> onInvoiceGenerated) {
        Instant start = month.atDay(1).atStartOfDay().toInstant(ZoneOffset.UTC);
        Instant end = month.atEndOfMonth().atTime(23, 59, 59).toInstant(ZoneOffset.UTC);

        List<UUID> ownerIds = usageRepository.findOwnerIdsBetween(
            start, end
        );

        LocalDateTime invoiceCreatedAt = LocalDateTime.now(
            clockProvider.getIfAvailable(Clock::systemDefaultZone)
        );

        ownerIds.forEach(ownerId -> {
                var usageRecords = usageRepository.getUsagesForOwnerRecordedBetween(
                    ownerId, start, end
                );

                var amount = BigDecimal
                    .valueOf(
                        usageRecords
                            .stream()
                            .mapToLong(UsageRecord::getCostCents)
                            .sum()
                    )
                    .movePointLeft(2);

                var ownerName = ownerRepository
                    .getById(ownerId)
                    .map(Owner::name)
                    .orElse("Unknown");

                var invoice = new Invoice(
                    ownerId,
                    ownerName,
                    month.toString(),
                    amount,
                    invoiceCreatedAt,
                    usageRecords
                        .stream()
                        .map(ur -> new Invoice.BillingItem(
                            ur.getUsageType().toString(),
                            BigDecimal.valueOf(ur.getCostCents()).movePointLeft(2)
                        ))
                        .toList()
                );

                log.info("💶 Invoice gespeichert für {} ({})", invoice.getOwnerName(), invoice.getAmount());
                onInvoiceGenerated.accept(invoice);
            }
        );
    }
}
