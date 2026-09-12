package nh.demo.plantify.billing;

import org.springframework.data.repository.Repository;

import java.util.List;
import java.util.UUID;

interface InvoiceRepository extends Repository<Invoice, UUID> {

    Invoice save(Invoice invoice);

    List<Invoice> findByOwnerId(UUID ownerId);
}
