package br.com.order.dto.response;

import br.com.order.dto.OrderItemDTO;
import br.com.order.enums.OrderStatusEnum;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;


@Getter
@Builder
@Setter
public class OrderResponseDTO {

    private Long id;
    private String externalId;
    private String customerId;
    private OrderStatusEnum status;
    private BigDecimal totalAmount;
    private List<OrderItemDTO> items;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
