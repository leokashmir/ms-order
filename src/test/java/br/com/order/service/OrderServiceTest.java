package br.com.order.service;

import br.com.order.dto.OrderItemDTO;
import br.com.order.dto.request.OrderRequestDTO;
import br.com.order.dto.response.OrderResponseDTO;
import br.com.order.enums.OrderStatusEnum;
import br.com.order.exception.DuplicateOrderException;
import br.com.order.exception.InsufficientStockException;
import br.com.order.exception.OrderNotFoundException;
import br.com.order.exception.ProductNotFoundException;
import br.com.order.mapper.OrderMapper;
import br.com.order.model.Order;
import br.com.order.model.OrderItem;
import br.com.order.model.Product;
import br.com.order.repository.OrderRepository;
import br.com.order.repository.ProductRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("OrderService Unit Tests")
class OrderServiceTest {

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private ProductRepository productRepository;

    @Mock
    private ExternalIntegrationService externalIntegrationService;

    @Mock
    private OrderMapper orderMapper;

    @InjectMocks
    @Spy
    private OrderService orderService;

    private OrderRequestDTO orderRequestDTO;
    private Product product;
    private Order order;
    private OrderResponseDTO orderResponseDTO;

    @BeforeEach
    void setUp() {
        // Setup Product
        product = Product.builder()
                .id(1L)
                .productId("PROD-001")
                .productName("Notebook Dell")
                .quantity(10)
                .unitPrice(new BigDecimal("3500.00"))
                .build();

        // Setup OrderRequestDTO
        OrderItemDTO itemDTO = OrderItemDTO.builder()
                .productId("PROD-001")
                .quantity(2)
                .build();

        orderRequestDTO = OrderRequestDTO.builder()
                .externalId("ORD-001")
                .customerId("CUST-001")
                .items(List.of(itemDTO))
                .build();

        // Setup Order
        OrderItem orderItem = OrderItem.builder()
                .id(1L)
                .productId("PROD-001")
                .productName("Notebook Dell")
                .quantity(2)
                .unitPrice(new BigDecimal("3500.00"))
                .totalPrice(new BigDecimal("7000.00"))
                .build();

        order = Order.builder()
                .id(1L)
                .externalId("ORD-001")
                .customerId("CUST-001")
                .status(OrderStatusEnum.CREATED)
                .totalAmount(new BigDecimal("7000.00"))
                .items(List.of(orderItem))
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        orderItem.setOrder(order);

        // Setup OrderResponseDTO
        orderResponseDTO = OrderResponseDTO.builder()
                .id(1L)
                .externalId("ORD-001")
                .customerId("CUST-001")
                .status(OrderStatusEnum.CREATED)
                .totalAmount(new BigDecimal("7000.00"))
                .build();
    }


    @Test
    @DisplayName("Should throw DuplicateOrderException when externalId already exists")
    void shouldThrowDuplicateOrderExceptionWhenExternalIdExists() {
        // Given
        when(orderRepository.existsByExternalId("ORD-001")).thenReturn(true);

        // When & Then
        assertThatThrownBy(() -> orderService.createOrder(orderRequestDTO))
                .isInstanceOf(DuplicateOrderException.class)
                .hasMessageContaining("ORD-001");

        verify(orderRepository).existsByExternalId("ORD-001");
        verify(orderRepository, never()).save(any(Order.class));
    }


    @Test
    @DisplayName("Should get order by id successfully")
    void shouldGetOrderByIdSuccessfully() {
        // Given
        when(orderRepository.findById(1L)).thenReturn(Optional.of(order));
        when(orderMapper.toDto(order)).thenReturn(orderResponseDTO);

        // When
        OrderResponseDTO result = orderService.getOrderById(1L);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getExternalId()).isEqualTo("ORD-001");
        verify(orderRepository).findById(1L);
    }

    @Test
    @DisplayName("Should throw OrderNotFoundException when order not found by id")
    void shouldThrowOrderNotFoundExceptionWhenOrderNotFoundById() {
        // Given
        when(orderRepository.findById(999L)).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> orderService.getOrderById(999L))
                .isInstanceOf(OrderNotFoundException.class)
                .hasMessageContaining("999");

        verify(orderRepository).findById(999L);
    }

    @Test
    @DisplayName("Should get order by externalId successfully")
    void shouldGetOrderByExternalIdSuccessfully() {
        // Given
        when(orderRepository.findByExternalId("ORD-001")).thenReturn(Optional.of(order));
        when(orderMapper.toDto(order)).thenReturn(orderResponseDTO);

        // When
        OrderResponseDTO result = orderService.getOrderByExternalId("ORD-001");

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getExternalId()).isEqualTo("ORD-001");
        verify(orderRepository).findByExternalId("ORD-001");
    }

    @Test
    @DisplayName("Should throw OrderNotFoundException when order not found by externalId")
    void shouldThrowOrderNotFoundExceptionWhenOrderNotFoundByExternalId() {
        // Given
        when(orderRepository.findByExternalId("ORD-999")).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> orderService.getOrderByExternalId("ORD-999"))
                .isInstanceOf(OrderNotFoundException.class)
                .hasMessageContaining("ORD-999");

        verify(orderRepository).findByExternalId("ORD-999");
    }

    @Test
    @DisplayName("Should get all orders with pagination")
    void shouldGetAllOrdersWithPagination() {
        // Given
        Pageable pageable = PageRequest.of(0, 10);
        List<Order> orders = List.of(order);
        Page<Order> orderPage = new PageImpl<>(orders, pageable, 1);

        when(orderRepository.findAll(pageable)).thenReturn(orderPage);
        when(orderMapper.toDto(any(Order.class))).thenReturn(orderResponseDTO);

        // When
        Page<OrderResponseDTO> result = orderService.getAllOrders(pageable);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getTotalElements()).isEqualTo(1);
        verify(orderRepository).findAll(pageable);
    }

    @Test
    @DisplayName("Should get orders by status with pagination")
    void shouldGetOrdersByStatusWithPagination() {
        // Given
        Pageable pageable = PageRequest.of(0, 10);
        List<Order> orders = List.of(order);
        Page<Order> orderPage = new PageImpl<>(orders, pageable, 1);

        when(orderRepository.findByStatus(OrderStatusEnum.CREATED, pageable)).thenReturn(orderPage);
        when(orderMapper.toDto(any(Order.class))).thenReturn(orderResponseDTO);

        // When
        Page<OrderResponseDTO> result = orderService.getOrdersByStatus(OrderStatusEnum.CREATED, pageable);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getContent()).hasSize(1);
        verify(orderRepository).findByStatus(OrderStatusEnum.CREATED, pageable);
    }

    @Test
    @DisplayName("Should update order status successfully")
    void shouldUpdateOrderStatusSuccessfully() {
        // Given
        Long orderId = 1L;
        OrderStatusEnum newStatus = OrderStatusEnum.CREATED;

        doNothing().when(orderRepository).updateOrderStatus(orderId, newStatus);

        // When
        orderService.updateOrderStatus(orderId, newStatus);

        // Then
        verify(orderRepository).updateOrderStatus(orderId, newStatus);
    }


    @Test
    @DisplayName("Should find product by productId successfully")
    void shouldFindProductByProductIdSuccessfully() {
        // Given
        when(productRepository.findByProductId("PROD-001")).thenReturn(Optional.of(product));

        // When
        Product result = orderService.findProductById("PROD-001");

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getProductId()).isEqualTo("PROD-001");
        verify(productRepository).findByProductId("PROD-001");
    }

    @Test
    @DisplayName("Should throw ProductNotFoundException when product not found by productId")
    void shouldThrowProductNotFoundExceptionWhenProductNotFoundByProductId() {
        // Given
        when(productRepository.findByProductId("PROD-999")).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> orderService.findProductById("PROD-999"))
                .isInstanceOf(ProductNotFoundException.class)
                .hasMessageContaining("PROD-999");

        verify(productRepository).findByProductId("PROD-999");
    }
}
