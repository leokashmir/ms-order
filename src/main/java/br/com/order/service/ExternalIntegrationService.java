package br.com.order.service;

import br.com.order.model.Order;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.concurrent.CompletableFuture;

@Service
@RequiredArgsConstructor

@Slf4j
public class ExternalIntegrationService {

    private final RestTemplate restTemplate;


    @Value("${app.external.product-b.endpoint}")
    private String productBEndpoint;

    @Async
    public CompletableFuture<Void> notifyProductB(Order order) {
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);

            String payload = createProductBPayload(order);
            HttpEntity<String> request = new HttpEntity<>(payload, headers);

            restTemplate.postForObject(productBEndpoint, request, String.class);

            log.info("Order {} notified to Product B successfully", order.getExternalId());

        } catch (Exception e) {
            log.error("Error notifying Product B for order {}: {}",
                    order.getExternalId(), e.getMessage());

        }

        return CompletableFuture.completedFuture(null);
    }

    private String createProductBPayload(Order order) {
        return String.format("""
            {
                "orderId": "%s",
                "externalId": "%s",
                "customerId": "%s",
                "totalAmount": %s,
                "status": "%s",
                "calculatedAt": "%s"
            }
            """,
                order.getId(),
                order.getExternalId(),
                order.getCustomerId(),
                order.getTotalAmount(),
                order.getStatus(),
                order.getUpdatedAt()
        );
    }
}
