package br.com.order.controller;

import br.com.order.dto.request.ProductRequestDTO;
import br.com.order.dto.response.ProductResponseDTO;
import br.com.order.service.ProductService;
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
@RequestMapping("/api/products")
@RequiredArgsConstructor
@Tag(name = "Gerenciamento de Produtos", description = "APIs para adcionar e consultar produtos para testes do Gerenciamento de Pedidos")
public class ProductController {

    private final ProductService productService;

    @PostMapping
    @Operation(summary = "Adcionar um produto")
    public ResponseEntity<ProductResponseDTO> createProduct(@Valid @RequestBody ProductRequestDTO request) {
        ProductResponseDTO response = productService.createProduct(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/{productId}")
    @Operation(summary = "Buscar produto por productId")
    public ResponseEntity<ProductResponseDTO> getProductByProductId(@PathVariable String productId) {
        ProductResponseDTO response = productService.getProductByProductId(productId);
        return ResponseEntity.ok(response);
    }

    @GetMapping
    @Operation(summary = "Obter todos os produtos com paginação")
    public ResponseEntity<Page<ProductResponseDTO>> getAllProducts(Pageable pageable) {
        Page<ProductResponseDTO> response = productService.getAllProducts(pageable);
        return ResponseEntity.ok(response);
    }
}
