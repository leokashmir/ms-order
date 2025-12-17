package br.com.order.controller;

import br.com.order.dto.OrderItemDTO;
import br.com.order.dto.request.OrderRequestDTO;
import br.com.order.dto.response.OrderResponseDTO;
import br.com.order.enums.OrderStatusEnum;
import br.com.order.service.OrderService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("OrderController Unit Tests")
class OrderControllerTest {

    @Mock
    private OrderService orderService;

    @InjectMocks
    private OrderController orderController;

    private OrderRequestDTO orderRequestDTO;
    private OrderResponseDTO orderResponseDTO;
    private OrderItemDTO orderItemDTO;

    @BeforeEach
    void setUp() {
        // Setup OrderItemDTO
        orderItemDTO = OrderItemDTO.builder()
                .productId("PROD-001")
                .productName("Produto Teste")
                .quantity(2)
                .unitPrice(new BigDecimal("50.00"))
                .totalPrice(new BigDecimal("100.00"))
                .build();

        // Setup OrderRequestDTO
        orderRequestDTO = OrderRequestDTO.builder()
                .externalId("ORD-001")
                .customerId("CUST-100")
                .items(List.of(orderItemDTO))
                .build();

        // Setup OrderResponseDTO
        orderResponseDTO = OrderResponseDTO.builder()
                .id(1L)
                .externalId("ORD-001")
                .customerId("CUST-100")
                .status(OrderStatusEnum.CREATED)
                .totalAmount(new BigDecimal("100.00"))
                .items(List.of(orderItemDTO))
                .createdAt(LocalDateTime.now())
                .build();
    }

    @Test
    @DisplayName("Should create order successfully and return 201 CREATED")
    void shouldCreateOrderSuccessfully() {
        // Given
        when(orderService.createOrder(any(OrderRequestDTO.class))).thenReturn(orderResponseDTO);

        // When
        ResponseEntity<OrderResponseDTO> response = orderController.createOrder(orderRequestDTO);

        // Then
        assertThat(response).isNotNull();
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getExternalId()).isEqualTo("ORD-001");
        assertThat(response.getBody().getStatus()).isEqualTo(OrderStatusEnum.CREATED);
        assertThat(response.getBody().getTotalAmount()).isEqualTo(new BigDecimal("100.00"));

        verify(orderService, times(1)).createOrder(any(OrderRequestDTO.class));
    }

    @Test
    @DisplayName("Should get order by ID successfully and return 200 OK")
    void shouldGetOrderByIdSuccessfully() {
        // Given
        Long orderId = 1L;
        when(orderService.getOrderById(orderId)).thenReturn(orderResponseDTO);

        // When
        ResponseEntity<OrderResponseDTO> response = orderController.getOrderById(orderId);

        // Then
        assertThat(response).isNotNull();
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getId()).isEqualTo(1L);
        assertThat(response.getBody().getExternalId()).isEqualTo("ORD-001");

        verify(orderService, times(1)).getOrderById(orderId);
    }

    @Test
    @DisplayName("Should get order by external ID successfully and return 200 OK")
    void shouldGetOrderByExternalIdSuccessfully() {
        // Given
        String externalId = "ORD-001";
        when(orderService.getOrderByExternalId(externalId)).thenReturn(orderResponseDTO);

        // When
        ResponseEntity<OrderResponseDTO> response = orderController.getOrderByExternalId(externalId);

        // Then
        assertThat(response).isNotNull();
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getExternalId()).isEqualTo("ORD-001");

        verify(orderService, times(1)).getOrderByExternalId(externalId);
    }

    @Test
    @DisplayName("Should get all orders with pagination and return 200 OK")
    void shouldGetAllOrdersWithPagination() {
        // Given
        Pageable pageable = PageRequest.of(0, 10);
        Page<OrderResponseDTO> page = new PageImpl<>(List.of(orderResponseDTO), pageable, 1);
        when(orderService.getAllOrders(any(Pageable.class))).thenReturn(page);

        // When
        ResponseEntity<Page<OrderResponseDTO>> response = orderController.getAllOrders(pageable);

        // Then
        assertThat(response).isNotNull();
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getContent()).hasSize(1);
        assertThat(response.getBody().getContent().get(0).getExternalId()).isEqualTo("ORD-001");
        assertThat(response.getBody().getTotalElements()).isEqualTo(1);

        verify(orderService, times(1)).getAllOrders(any(Pageable.class));
    }

    @Test
    @DisplayName("Should get orders by status and return 200 OK")
    void shouldGetOrdersByStatus() {
        // Given
        OrderStatusEnum status = OrderStatusEnum.CREATED;
        Pageable pageable = PageRequest.of(0, 10);
        Page<OrderResponseDTO> page = new PageImpl<>(List.of(orderResponseDTO), pageable, 1);
        when(orderService.getOrdersByStatus(eq(status), any(Pageable.class))).thenReturn(page);

        // When
        ResponseEntity<Page<OrderResponseDTO>> response = orderController.getOrdersByStatus(status, pageable);

        // Then
        assertThat(response).isNotNull();
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getContent()).hasSize(1);
        assertThat(response.getBody().getContent().get(0).getStatus()).isEqualTo(OrderStatusEnum.CREATED);

        verify(orderService, times(1)).getOrdersByStatus(eq(status), any(Pageable.class));
    }

    @Test
    @DisplayName("Should get today orders count and return 200 OK")
    void shouldGetTodayOrdersCount() {
        // Given
        Long expectedCount = 5L;
        when(orderService.getTodayOrdersCount()).thenReturn(expectedCount);

        // When
        ResponseEntity<Long> response = orderController.getTodayOrdersCount();

        // Then
        assertThat(response).isNotNull();
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isEqualTo(5L);

        verify(orderService, times(1)).getTodayOrdersCount();
    }

    @Test
    @DisplayName("Should update order status and return 204 NO CONTENT")
    void shouldUpdateOrderStatus() {
        // Given
        Long orderId = 1L;
        OrderStatusEnum newStatus = OrderStatusEnum.FAILED;
        doNothing().when(orderService).updateOrderStatus(orderId, newStatus);

        // When
        ResponseEntity<Void> response = orderController.updateOrderStatus(orderId, newStatus);

        // Then
        assertThat(response).isNotNull();
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
        assertThat(response.getBody()).isNull();

        verify(orderService, times(1)).updateOrderStatus(orderId, newStatus);
    }

    @Test
    @DisplayName("Should handle empty page when getting all orders")
    void shouldHandleEmptyPageWhenGettingAllOrders() {
        // Given
        Pageable pageable = PageRequest.of(0, 10);
        Page<OrderResponseDTO> emptyPage = new PageImpl<>(List.of(), pageable, 0);
        when(orderService.getAllOrders(any(Pageable.class))).thenReturn(emptyPage);

        // When
        ResponseEntity<Page<OrderResponseDTO>> response = orderController.getAllOrders(pageable);

        // Then
        assertThat(response).isNotNull();
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getContent()).isEmpty();
        assertThat(response.getBody().getTotalElements()).isEqualTo(0);

        verify(orderService, times(1)).getAllOrders(any(Pageable.class));
    }

    @Test
    @DisplayName("Should handle empty page when getting orders by status")
    void shouldHandleEmptyPageWhenGettingOrdersByStatus() {
        // Given
        OrderStatusEnum status = OrderStatusEnum.FAILED;
        Pageable pageable = PageRequest.of(0, 10);
        Page<OrderResponseDTO> emptyPage = new PageImpl<>(List.of(), pageable, 0);
        when(orderService.getOrdersByStatus(eq(status), any(Pageable.class))).thenReturn(emptyPage);

        // When
        ResponseEntity<Page<OrderResponseDTO>> response = orderController.getOrdersByStatus(status, pageable);

        // Then
        assertThat(response).isNotNull();
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getContent()).isEmpty();

        verify(orderService, times(1)).getOrdersByStatus(eq(status), any(Pageable.class));
    }

    @Test
    @DisplayName("Should return zero when no orders today")
    void shouldReturnZeroWhenNoOrdersToday() {
        // Given
        when(orderService.getTodayOrdersCount()).thenReturn(0L);

        // When
        ResponseEntity<Long> response = orderController.getTodayOrdersCount();

        // Then
        assertThat(response).isNotNull();
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isEqualTo(0L);

        verify(orderService, times(1)).getTodayOrdersCount();
    }
}
