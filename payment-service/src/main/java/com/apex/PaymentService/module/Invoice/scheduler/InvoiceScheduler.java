package com.apex.PaymentService.module.Invoice.scheduler;

import com.apex.PaymentService.module.Invoice.service.InvoiceService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class InvoiceScheduler {

    private final InvoiceService invoiceService;

    /**
     * Runs daily at 01:00 server time.
     * Marks invoices as overdue once their due date + grace period has passed.
     */
    @Scheduled(cron = "0 0 1 * * *")
    public void sweepOverdueInvoices() {
        log.info("Running daily overdue invoice sweep…");
        try {
            invoiceService.markOverdueInvoices();
        } catch (Exception e) {
            log.error("Overdue sweep failed", e);
        }
    }
}
