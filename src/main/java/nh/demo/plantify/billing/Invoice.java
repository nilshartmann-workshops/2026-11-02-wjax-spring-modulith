package nh.demo.plantify.billing;

import jakarta.persistence.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "invoices", schema = "billing_schema")
class Invoice {

    @Id
    private UUID id;

    @Column(name = "owner_id", nullable = false)
    private UUID ownerId;

    @Column(name = "owner_name", nullable = false)
    private String ownerName;

    @Column(name = "billing_period", nullable = false)
    private String billingPeriod;

    @Column(nullable = false)
    private BigDecimal amount;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @ElementCollection
    @CollectionTable(name = "invoice_items", schema = "billing_schema",
        joinColumns = @JoinColumn(name = "invoice_id"))
    private List<BillingItem> items = new ArrayList<>();

    protected Invoice() {}

    Invoice(UUID ownerId, String ownerName, String billingPeriod,
            BigDecimal amount, LocalDateTime createdAt, List<BillingItem> items) {
        this.id = UUID.randomUUID();
        this.ownerId = ownerId;
        this.ownerName = ownerName;
        this.billingPeriod = billingPeriod;
        this.amount = amount;
        this.createdAt = createdAt;
        this.items = items;
    }

    UUID getId() { return id; }
    UUID getOwnerId() { return ownerId; }
    String getOwnerName() { return ownerName; }
    String getBillingPeriod() { return billingPeriod; }
    BigDecimal getAmount() { return amount; }

    @Embeddable
    record BillingItem(
        @Column(name = "description", nullable = false) String description,
        @Column(name = "amount", nullable = false)      BigDecimal amount
    ) {}
}
