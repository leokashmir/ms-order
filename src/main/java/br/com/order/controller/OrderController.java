package br.com.order.controller;


import br.com.order.dto.request.OrderRequestDTO;
import br.com.order.dto.response.OrderResponseDTO;
import br.com.order.enums.OrderStatusEnum;
import br.com.order.service.OrderService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/orders")
@RequiredArgsConstructor
@Tag(name = "Gerenciamento de Pedidos", description = "APIs para Gerenciar a criação e consulta de pedidos")
public class OrderController {

    private final OrderService orderService;

    @PostMapping
    @Operation(summary = "Criar um novo pedido")
    public ResponseEntity<OrderResponseDTO> createOrder(@Valid @RequestBody OrderRequestDTO request) {
        OrderResponseDTO response = orderService.createOrder(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Obter pedido por ID do registro")
    public ResponseEntity<OrderResponseDTO> getOrderById(@PathVariable Long id) {
        OrderResponseDTO response = orderService.getOrderById(id);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/external/{externalId}")
    @Operation(summary = "Obter pedido por ID externo")
    public ResponseEntity<OrderResponseDTO> getOrderByExternalId(@PathVariable String externalId) {
        OrderResponseDTO response = orderService.getOrderByExternalId(externalId);
        return ResponseEntity.ok(response);
    }

    @GetMapping
    @Operation(summary = "Obter todos os pedidos com paginação")
    public ResponseEntity<Page<OrderResponseDTO>> getAllOrders(Pageable pageable) {
        Page<OrderResponseDTO> response = orderService.getAllOrders(pageable);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/status/{status}")
    @Operation(summary = "Obter pedidos por status")
    public ResponseEntity<Page<OrderResponseDTO>> getOrdersByStatus(
            @PathVariable OrderStatusEnum status, Pageable pageable) {
        Page<OrderResponseDTO> response = orderService.getOrdersByStatus(status, pageable);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/metrics/today")
    @Operation(summary = "Obter contagem de pedidos diarios")
    public ResponseEntity<Long> getTodayOrdersCount() {
        Long count = orderService.getTodayOrdersCount();
        return ResponseEntity.ok(count);
    }

    @PutMapping("/{id}/status/{status}")
    @Operation(summary = "Atualizar status do pedido")
    public ResponseEntity<Void> updateOrderStatus(
            @PathVariable Long id,
            @PathVariable OrderStatusEnum status) {
        orderService.updateOrderStatus(id, status);
        return ResponseEntity.noContent().build();
    }
}