package nh.demo.plantify.billing;

import nh.demo.plantify.TestIds;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.modulith.moments.support.TimeMachine;
import org.springframework.modulith.test.ApplicationModuleTest;
import org.springframework.modulith.test.Scenario;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;

import java.time.Duration;
import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;

@ApplicationModuleTest(ApplicationModuleTest.BootstrapMode.DIRECT_DEPENDENCIES)
@ActiveProfiles("test")
@TestPropertySource(properties = "spring.modulith.moments.enable-time-machine=true")
class MonthlyBillingTest {

    @Autowired
    UsageRepository usageRepository;

    @Autowired
    InvoiceRepository invoiceRepository;

    @Autowired
    TimeMachine timeMachine;

    @Test
    void creates_invoice_when_month_has_passed(Scenario scenario) {

        // 🕵️‍♂️ Zeigen: shiftBy(31 Tage) löst MonthHasPassed aus, und der Rechnungslauf schreibt die Rechnung
        usageRepository.save(new UsageRecord(
            TestIds.existingOwnerId, UsageRecord.UsageType.SETUP_FEE, Instant.now(), 1000L));

        scenario.stimulate(() -> timeMachine.shiftBy(Duration.ofDays(31)))
            .andWaitForStateChange(
                () -> invoiceRepository.findByOwnerId(TestIds.existingOwnerId),
                invoices -> !invoices.isEmpty())
            .andVerify(invoices -> assertThat(invoices)
                .singleElement()
                .satisfies(invoice -> assertThat(invoice.getAmount()).isPositive()));
    }
}
