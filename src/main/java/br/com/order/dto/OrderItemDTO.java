package br.com.order.dto;

import jakarta.validation.constraints.*;
import lombok.*;

import java.math.BigDecimal;

@Getter
@Builder
@Setter @NoArgsConstructor @AllArgsConstructor
public class OrderItemDTO {

    @NotBlank(message = "produto ID é obrigatório")
    @Size(max = 50, message = "produto ID não pode exceder 50 caracteres")
    private String productId;
    @NotBlank(message = "nome do produto é obrigatório")
    private String productName;
    @NotNull(message = "quantidade é obrigatória")
    private Integer quantity;
    private BigDecimal unitPrice;
    private BigDecimal totalPrice;
}
