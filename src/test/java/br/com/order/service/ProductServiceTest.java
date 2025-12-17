package br.com.order.service;

import br.com.order.dto.request.ProductRequestDTO;
import br.com.order.dto.response.ProductResponseDTO;
import br.com.order.exception.DuplicateOrderException;
import br.com.order.exception.ProductNotFoundException;
import br.com.order.mapper.ProductMapper;
import br.com.order.model.Product;
import br.com.order.repository.ProductRepository;
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

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("ProductService Unit Tests")
class ProductServiceTest {

    @Mock
    private ProductRepository productRepository;

    @Mock
    private ProductMapper productMapper;

    @InjectMocks
    private ProductService productService;

    private ProductRequestDTO productRequestDTO;
    private Product product;
    private ProductResponseDTO productResponseDTO;

    @BeforeEach
    void setUp() {
        // Setup ProductRequestDTO
        productRequestDTO = ProductRequestDTO.builder()
                .productId("PROD-001")
                .productName("Notebook Dell")
                .quantity(10)
                .unitPrice(new BigDecimal("3500.00"))
                .build();

        // Setup Product
        product = Product.builder()
                .id(1L)
                .productId("PROD-001")
                .productName("Notebook Dell")
                .quantity(10)
                .unitPrice(new BigDecimal("3500.00"))
                .build();

        // Setup ProductResponseDTO
        productResponseDTO = ProductResponseDTO.builder()
                .id(1L)
                .productId("PROD-001")
                .productName("Notebook Dell")
                .quantity(10)
                .unitPrice(new BigDecimal("3500.00"))
                .build();
    }

    @Test
    @DisplayName("Should create product successfully")
    void shouldCreateProductSuccessfully() {
        // Given
        when(productRepository.findByProductId("PROD-001")).thenReturn(Optional.empty());
        when(productRepository.save(any(Product.class))).thenReturn(product);
        when(productMapper.toDto(product)).thenReturn(productResponseDTO);

        // When
        ProductResponseDTO result = productService.createProduct(productRequestDTO);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getProductId()).isEqualTo("PROD-001");
        assertThat(result.getProductName()).isEqualTo("Notebook Dell");
        assertThat(result.getQuantity()).isEqualTo(10);
        assertThat(result.getUnitPrice()).isEqualByComparingTo(new BigDecimal("3500.00"));

        verify(productRepository).findByProductId("PROD-001");
        verify(productRepository).save(any(Product.class));
        verify(productMapper).toDto(product);
    }

    @Test
    @DisplayName("Should throw DuplicateOrderException when productId already exists")
    void shouldThrowDuplicateOrderExceptionWhenProductIdExists() {
        // Given
        when(productRepository.findByProductId("PROD-001")).thenReturn(Optional.of(product));

        // When & Then
        assertThatThrownBy(() -> productService.createProduct(productRequestDTO))
                .isInstanceOf(DuplicateOrderException.class)
                .hasMessageContaining("PROD-001");

        verify(productRepository).findByProductId("PROD-001");
        verify(productRepository, never()).save(any(Product.class));
    }

    @Test
    @DisplayName("Should get product by productId successfully")
    void shouldGetProductByProductIdSuccessfully() {
        // Given
        when(productRepository.findByProductId("PROD-001")).thenReturn(Optional.of(product));
        when(productMapper.toDto(product)).thenReturn(productResponseDTO);

        // When
        ProductResponseDTO result = productService.getProductByProductId("PROD-001");

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getProductId()).isEqualTo("PROD-001");
        assertThat(result.getProductName()).isEqualTo("Notebook Dell");

        verify(productRepository).findByProductId("PROD-001");
        verify(productMapper).toDto(product);
    }

    @Test
    @DisplayName("Should throw ProductNotFoundException when product not found by productId")
    void shouldThrowProductNotFoundExceptionWhenProductNotFound() {
        // Given
        when(productRepository.findByProductId("PROD-999")).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> productService.getProductByProductId("PROD-999"))
                .isInstanceOf(ProductNotFoundException.class)
                .hasMessageContaining("PROD-999");

        verify(productRepository).findByProductId("PROD-999");
    }

    @Test
    @DisplayName("Should get all products with pagination")
    void shouldGetAllProductsWithPagination() {
        // Given
        Pageable pageable = PageRequest.of(0, 10);
        List<Product> products = List.of(product);
        Page<Product> productPage = new PageImpl<>(products, pageable, 1);

        when(productRepository.findAll(pageable)).thenReturn(productPage);
        when(productMapper.toDto(any(Product.class))).thenReturn(productResponseDTO);

        // When
        Page<ProductResponseDTO> result = productService.getAllProducts(pageable);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getTotalElements()).isEqualTo(1);
        assertThat(result.getContent().get(0).getProductId()).isEqualTo("PROD-001");

        verify(productRepository).findAll(pageable);
        verify(productMapper).toDto(product);
    }

    @Test
    @DisplayName("Should return empty page when no products exist")
    void shouldReturnEmptyPageWhenNoProductsExist() {
        // Given
        Pageable pageable = PageRequest.of(0, 10);
        Page<Product> emptyPage = new PageImpl<>(List.of(), pageable, 0);

        when(productRepository.findAll(pageable)).thenReturn(emptyPage);

        // When
        Page<ProductResponseDTO> result = productService.getAllProducts(pageable);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getContent()).isEmpty();
        assertThat(result.getTotalElements()).isEqualTo(0);

        verify(productRepository).findAll(pageable);
        verify(productMapper, never()).toDto(any(Product.class));
    }

    @Test
    @DisplayName("Should create product with minimum valid values")
    void shouldCreateProductWithMinimumValidValues() {
        // Given
        ProductRequestDTO minProductRequest = ProductRequestDTO.builder()
                .productId("PROD-MIN")
                .productName("Minimal Product")
                .quantity(0)
                .unitPrice(new BigDecimal("0.01"))
                .build();

        Product minProduct = Product.builder()
                .id(2L)
                .productId("PROD-MIN")
                .productName("Minimal Product")
                .quantity(0)
                .unitPrice(new BigDecimal("0.01"))
                .build();

        ProductResponseDTO minProductResponse = ProductResponseDTO.builder()
                .id(2L)
                .productId("PROD-MIN")
                .productName("Minimal Product")
                .quantity(0)
                .unitPrice(new BigDecimal("0.01"))
                .build();

        when(productRepository.findByProductId("PROD-MIN")).thenReturn(Optional.empty());
        when(productRepository.save(any(Product.class))).thenReturn(minProduct);
        when(productMapper.toDto(minProduct)).thenReturn(minProductResponse);

        // When
        ProductResponseDTO result = productService.createProduct(minProductRequest);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getQuantity()).isEqualTo(0);
        assertThat(result.getUnitPrice()).isEqualByComparingTo(new BigDecimal("0.01"));

        verify(productRepository).save(any(Product.class));
    }

    @Test
    @DisplayName("Should create product with large quantity and price")
    void shouldCreateProductWithLargeQuantityAndPrice() {
        // Given
        ProductRequestDTO largeProductRequest = ProductRequestDTO.builder()
                .productId("PROD-LARGE")
                .productName("Expensive Product")
                .quantity(999999)
                .unitPrice(new BigDecimal("999999.99"))
                .build();

        Product largeProduct = Product.builder()
                .id(3L)
                .productId("PROD-LARGE")
                .productName("Expensive Product")
                .quantity(999999)
                .unitPrice(new BigDecimal("999999.99"))
                .build();

        ProductResponseDTO largeProductResponse = ProductResponseDTO.builder()
                .id(3L)
                .productId("PROD-LARGE")
                .productName("Expensive Product")
                .quantity(999999)
                .unitPrice(new BigDecimal("999999.99"))
                .build();

        when(productRepository.findByProductId("PROD-LARGE")).thenReturn(Optional.empty());
        when(productRepository.save(any(Product.class))).thenReturn(largeProduct);
        when(productMapper.toDto(largeProduct)).thenReturn(largeProductResponse);

        // When
        ProductResponseDTO result = productService.createProduct(largeProductRequest);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getQuantity()).isEqualTo(999999);
        assertThat(result.getUnitPrice()).isEqualByComparingTo(new BigDecimal("999999.99"));

        verify(productRepository).save(any(Product.class));
    }
}
