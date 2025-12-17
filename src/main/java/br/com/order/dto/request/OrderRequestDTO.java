package br.com.order.dto.request;

import br.com.order.dto.OrderItemDTO;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import lombok.*;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@Builder @AllArgsConstructor
public class OrderRequestDTO {

    @NotBlank(message = "proproductId é obrigatório")
    @Size(max = 50, message = "proproductId não pode exceder 50 caracteres")
    private String externalId;

    @NotBlank(message = "customerId é obrigatório")
    private String customerId;

    @NotEmpty(message = "Pedido deve conter pelo menos um item")
    @Valid
    private List<OrderItemDTO> items;
}