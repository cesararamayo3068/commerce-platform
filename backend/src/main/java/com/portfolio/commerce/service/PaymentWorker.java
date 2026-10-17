package com.portfolio.commerce.service;
import com.portfolio.commerce.domain.payment.PaymentRepository;
import org.springframework.data.domain.PageRequest;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
@ConditionalOnProperty(name = "payments.worker.enabled", havingValue = "true", matchIfMissing = true)
@Component
public class PaymentWorker {
 private final PaymentRepository repository; private final PaymentService service;
 public PaymentWorker(PaymentRepository repository,PaymentService service){this.repository=repository;this.service=service;}
 @Scheduled(fixedDelayString="${payments.poll-ms:3000}")
 public void poll(){for(Long id:repository.findPendingIds(PageRequest.of(0,50)))service.process(id);}
}
