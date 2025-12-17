package br.com.order.controller;

import br.com.order.dto.request.ProductRequestDTO;
import br.com.order.dto.response.ProductResponseDTO;
import br.com.order.service.ProductService;
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
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("ProductController Unit Tests")
class ProductControllerTest {

    @Mock
    private ProductService productService;

    @InjectMocks
    private ProductController productController;

    private ProductRequestDTO productRequestDTO;
    private ProductResponseDTO productResponseDTO;

    @BeforeEach
    void setUp() {
        // Setup ProductRequestDTO
        productRequestDTO = ProductRequestDTO.builder()
                .productId("PROD-001")
                .productName("Notebook Dell")
                .quantity(10)
                .unitPrice(new BigDecimal("2500.00"))
                .build();

        // Setup ProductResponseDTO
        productResponseDTO = ProductResponseDTO.builder()
                .id(1L)
                .productId("PROD-001")
                .productName("Notebook Dell")
                .quantity(10)
                .unitPrice(new BigDecimal("2500.00"))
                .build();
    }

    @Test
    @DisplayName("Should create product successfully and return 201 CREATED")
    void shouldCreateProductSuccessfully() {
        // Given
        when(productService.createProduct(any(ProductRequestDTO.class))).thenReturn(productResponseDTO);

        // When
        ResponseEntity<ProductResponseDTO> response = productController.createProduct(productRequestDTO);

        // Then
        assertThat(response).isNotNull();
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getProductId()).isEqualTo("PROD-001");
        assertThat(response.getBody().getProductName()).isEqualTo("Notebook Dell");
        assertThat(response.getBody().getQuantity()).isEqualTo(10);
        assertThat(response.getBody().getUnitPrice()).isEqualTo(new BigDecimal("2500.00"));

        verify(productService, times(1)).createProduct(any(ProductRequestDTO.class));
    }

    @Test
    @DisplayName("Should get product by productId successfully and return 200 OK")
    void shouldGetProductByProductIdSuccessfully() {
        // Given
        String productId = "PROD-001";
        when(productService.getProductByProductId(productId)).thenReturn(productResponseDTO);

        // When
        ResponseEntity<ProductResponseDTO> response = productController.getProductByProductId(productId);

        // Then
        assertThat(response).isNotNull();
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getProductId()).isEqualTo("PROD-001");
        assertThat(response.getBody().getProductName()).isEqualTo("Notebook Dell");

        verify(productService, times(1)).getProductByProductId(productId);
    }

    @Test
    @DisplayName("Should get all products with pagination and return 200 OK")
    void shouldGetAllProductsWithPagination() {
        // Given
        Pageable pageable = PageRequest.of(0, 10);
        Page<ProductResponseDTO> page = new PageImpl<>(List.of(productResponseDTO), pageable, 1);
        when(productService.getAllProducts(any(Pageable.class))).thenReturn(page);

        // When
        ResponseEntity<Page<ProductResponseDTO>> response = productController.getAllProducts(pageable);

        // Then
        assertThat(response).isNotNull();
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getContent()).hasSize(1);
        assertThat(response.getBody().getContent().get(0).getProductId()).isEqualTo("PROD-001");
        assertThat(response.getBody().getTotalElements()).isEqualTo(1);

        verify(productService, times(1)).getAllProducts(any(Pageable.class));
    }

    @Test
    @DisplayName("Should handle empty page when getting all products")
    void shouldHandleEmptyPageWhenGettingAllProducts() {
        // Given
        Pageable pageable = PageRequest.of(0, 10);
        Page<ProductResponseDTO> emptyPage = new PageImpl<>(List.of(), pageable, 0);
        when(productService.getAllProducts(any(Pageable.class))).thenReturn(emptyPage);

        // When
        ResponseEntity<Page<ProductResponseDTO>> response = productController.getAllProducts(pageable);

        // Then
        assertThat(response).isNotNull();
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getContent()).isEmpty();
        assertThat(response.getBody().getTotalElements()).isEqualTo(0);

        verify(productService, times(1)).getAllProducts(any(Pageable.class));
    }

    @Test
    @DisplayName("Should create product with minimum valid values")
    void shouldCreateProductWithMinimumValidValues() {
        // Given
        ProductRequestDTO minProductRequest = ProductRequestDTO.builder()
                .productId("PROD-MIN")
                .productName("Produto Mínimo")
                .quantity(0)
                .unitPrice(new BigDecimal("0.01"))
                .build();

        ProductResponseDTO minProductResponse = ProductResponseDTO.builder()
                .id(2L)
                .productId("PROD-MIN")
                .productName("Produto Mínimo")
                .quantity(0)
                .unitPrice(new BigDecimal("0.01"))
                .build();

        when(productService.createProduct(any(ProductRequestDTO.class))).thenReturn(minProductResponse);

        // When
        ResponseEntity<ProductResponseDTO> response = productController.createProduct(minProductRequest);

        // Then
        assertThat(response).isNotNull();
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getQuantity()).isEqualTo(0);
        assertThat(response.getBody().getUnitPrice()).isEqualTo(new BigDecimal("0.01"));

        verify(productService, times(1)).createProduct(any(ProductRequestDTO.class));
    }

    @Test
    @DisplayName("Should get all products with multiple items")
    void shouldGetAllProductsWithMultipleItems() {
        // Given
        ProductResponseDTO product2 = ProductResponseDTO.builder()
                .id(2L)
                .productId("PROD-002")
                .productName("Mouse Logitech")
                .quantity(50)
                .unitPrice(new BigDecimal("150.00"))
                .build();

        ProductResponseDTO product3 = ProductResponseDTO.builder()
                .id(3L)
                .productId("PROD-003")
                .productName("Teclado Mecânico")
                .quantity(30)
                .unitPrice(new BigDecimal("350.00"))
                .build();

        Pageable pageable = PageRequest.of(0, 10);
        Page<ProductResponseDTO> page = new PageImpl<>(
                List.of(productResponseDTO, product2, product3),
                pageable,
                3
        );
        when(productService.getAllProducts(any(Pageable.class))).thenReturn(page);

        // When
        ResponseEntity<Page<ProductResponseDTO>> response = productController.getAllProducts(pageable);

        // Then
        assertThat(response).isNotNull();
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getContent()).hasSize(3);
        assertThat(response.getBody().getTotalElements()).isEqualTo(3);
        assertThat(response.getBody().getContent().get(0).getProductId()).isEqualTo("PROD-001");
        assertThat(response.getBody().getContent().get(1).getProductId()).isEqualTo("PROD-002");
        assertThat(response.getBody().getContent().get(2).getProductId()).isEqualTo("PROD-003");

        verify(productService, times(1)).getAllProducts(any(Pageable.class));
    }

    @Test
    @DisplayName("Should get products with custom page size")
    void shouldGetProductsWithCustomPageSize() {
        // Given
        Pageable pageable = PageRequest.of(0, 5);
        Page<ProductResponseDTO> page = new PageImpl<>(List.of(productResponseDTO), pageable, 1);
        when(productService.getAllProducts(any(Pageable.class))).thenReturn(page);

        // When
        ResponseEntity<Page<ProductResponseDTO>> response = productController.getAllProducts(pageable);

        // Then
        assertThat(response).isNotNull();
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getSize()).isEqualTo(5);

        verify(productService, times(1)).getAllProducts(any(Pageable.class));
    }

    @Test
    @DisplayName("Should get product with large quantity")
    void shouldGetProductWithLargeQuantity() {
        // Given
        ProductResponseDTO largeQuantityProduct = ProductResponseDTO.builder()
                .id(4L)
                .productId("PROD-LARGE")
                .productName("Produto Grande Estoque")
                .quantity(10000)
                .unitPrice(new BigDecimal("10.00"))
                .build();

        when(productService.getProductByProductId("PROD-LARGE")).thenReturn(largeQuantityProduct);

        // When
        ResponseEntity<ProductResponseDTO> response = productController.getProductByProductId("PROD-LARGE");

        // Then
        assertThat(response).isNotNull();
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getQuantity()).isEqualTo(10000);

        verify(productService, times(1)).getProductByProductId("PROD-LARGE");
    }

    @Test
    @DisplayName("Should get product with high unit price")
    void shouldGetProductWithHighUnitPrice() {
        // Given
        ProductResponseDTO expensiveProduct = ProductResponseDTO.builder()
                .id(5L)
                .productId("PROD-EXPENSIVE")
                .productName("Produto Caro")
                .quantity(1)
                .unitPrice(new BigDecimal("99999.99"))
                .build();

        when(productService.getProductByProductId("PROD-EXPENSIVE")).thenReturn(expensiveProduct);

        // When
        ResponseEntity<ProductResponseDTO> response = productController.getProductByProductId("PROD-EXPENSIVE");

        // Then
        assertThat(response).isNotNull();
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getUnitPrice()).isEqualTo(new BigDecimal("99999.99"));

        verify(productService, times(1)).getProductByProductId("PROD-EXPENSIVE");
    }
}
