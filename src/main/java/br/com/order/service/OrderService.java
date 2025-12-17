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
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.context.annotation.Lazy;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import lombok.extern.log4j.Log4j2;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;


@Log4j2
@Service
@RequiredArgsConstructor
public class OrderService {

    private final OrderRepository orderRepository;
    private final ProductRepository productRepository;
    private final ExternalIntegrationService externalIntegrationService;
    private final OrderMapper orderMapper;


    @Autowired @Lazy
    private OrderService selfOrderService;

    @Transactional
    public OrderResponseDTO createOrder(OrderRequestDTO request) {

        if (orderRepository.existsByExternalId(request.getExternalId())) {
            throw new DuplicateOrderException(
                    "Pedido com externalId " + request.getExternalId() + " já existe"
            );
        }

        BigDecimal totalAmount = calculateTotalAmount(request.getItems());
        Order order = Order.builder()
                .externalId(request.getExternalId())
                .customerId(request.getCustomerId())
                .status(OrderStatusEnum.PROCESSING)
                .totalAmount(totalAmount)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();


        List<OrderItem> items = request.getItems().stream()
                .map(itemDto -> {
                    Product product = selfOrderService.findProductById(itemDto.getProductId());

                    if (product.getQuantity() < itemDto.getQuantity()) {
                        throw new InsufficientStockException(
                                String.format("Estoque insuficiente para o produto %s. Disponível: %d, Solicitado: %d",
                                        product.getProductName(),
                                        product.getQuantity(),
                                        itemDto.getQuantity()));
                    }
                    product.setQuantity(product.getQuantity() - itemDto.getQuantity());
                    productRepository.save(product);

                    BigDecimal unitPrice = product.getUnitPrice();
                    return OrderItem.builder()
                            .order(order)
                            .productId(itemDto.getProductId())
                            .productName(product.getProductName())
                            .quantity(itemDto.getQuantity())
                            .unitPrice(unitPrice)
                            .totalPrice(unitPrice.multiply(BigDecimal.valueOf(itemDto.getQuantity())))
                            .build();
                })
                .toList();

        order.setItems(items);
        processOrder(order);

        Order savedOrder = orderRepository.save(order);
        log.info("Pedido criado: {}", savedOrder.getExternalId());
        return mapToResponseDTO(savedOrder);
    }

    @Async
    @Transactional
    public CompletableFuture<Void> processOrder(Order order) {
        try {

            order.setStatus(OrderStatusEnum.CREATED);
            orderRepository.save(order);
            externalIntegrationService.notifyProductB(order);
            log.info("Pedido {} processado com sucesso", order.getExternalId());

        } catch (Exception e) {
            order.setStatus(OrderStatusEnum.FAILED);
            orderRepository.save(order);
            log.error("Erro processando pedido {}: {}", order.getExternalId(), e.getMessage());
        }

        return CompletableFuture.completedFuture(null);
    }

    @Cacheable(value = "orders", key = "#id")
    @Transactional(readOnly = true)
    public OrderResponseDTO getOrderById(Long id) {
        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new OrderNotFoundException("Pedido não encontrado com id: " + id));

        return mapToResponseDTO(order);
    }

    @Cacheable(value = "ordersByExternalId", key = "#externalId")
    @Transactional(readOnly = true)
    public OrderResponseDTO getOrderByExternalId(String externalId) {
        Order order = orderRepository.findByExternalId(externalId)
                .orElseThrow(() -> new OrderNotFoundException(
                        "Pedido não encontrado com externalId: " + externalId));

        return mapToResponseDTO(order);
    }

    @Transactional(readOnly = true)
    public Page<OrderResponseDTO> getAllOrders(Pageable pageable) {
        return orderRepository.findAll(pageable)
                .map(this::mapToResponseDTO);
    }

    @Transactional(readOnly = true)
    public Page<OrderResponseDTO> getOrdersByStatus(OrderStatusEnum status, Pageable pageable) {
        return orderRepository.findByStatus(status, pageable)
                .map(this::mapToResponseDTO);
    }

    @CacheEvict(value = {"orders", "ordersByExternalId"}, allEntries = true)
    @Transactional
    public void updateOrderStatus(Long id, OrderStatusEnum status) {
        orderRepository.updateOrderStatus(id, status);
        log.info("Pedido {} status atualizado para {}", id, status);
    }

    public Long getTodayOrdersCount() {
        LocalDateTime startOfDay = LocalDateTime.now().withHour(0).withMinute(0).withSecond(0);
        return orderRepository.countOrdersSince(startOfDay);
    }

    private BigDecimal calculateTotalAmount(List<OrderItemDTO> items) {
        return items.stream()
                .map(item -> {
                    BigDecimal unitPrice = selfOrderService.findProductById(item.getProductId()).getUnitPrice();
                    item.setUnitPrice(unitPrice);
                    return unitPrice.multiply(BigDecimal.valueOf(item.getQuantity()));
                })
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    @Cacheable(value = "products", key = "#productId")
    public Product findProductById(String productId) {
        return productRepository.findByProductId(productId)
                .orElseThrow(() -> new ProductNotFoundException(
                        "Produto não encontrado com productId: " + productId));
    }

    private OrderResponseDTO mapToResponseDTO(Order order) {
          return orderMapper.toDto(order);
    }
}
