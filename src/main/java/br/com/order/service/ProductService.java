package br.com.order.service;

import br.com.order.dto.request.ProductRequestDTO;
import br.com.order.dto.response.ProductResponseDTO;
import br.com.order.exception.DuplicateOrderException;
import br.com.order.exception.ProductNotFoundException;
import br.com.order.mapper.ProductMapper;
import br.com.order.model.Product;
import br.com.order.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class ProductService {

    private final ProductRepository productRepository;
    private final ProductMapper productMapper;

    @Transactional
    public ProductResponseDTO createProduct(ProductRequestDTO request) {

        if (productRepository.findByProductId(request.getProductId()).isPresent()) {
            throw new DuplicateOrderException(
                    "Produto com productId " + request.getProductId() + " já existe"
            );
        }

        Product product = Product.builder()
                .productId(request.getProductId())
                .productName(request.getProductName())
                .quantity(request.getQuantity())
                .unitPrice(request.getUnitPrice())
                .build();

        Product savedProduct = productRepository.save(product);
        log.info("Produto criado: {}", savedProduct.getProductId());

        return mapToResponseDTO(savedProduct);
    }

    @Transactional(readOnly = true)
    public ProductResponseDTO getProductByProductId(String productId) {
        Product product = productRepository.findByProductId(productId)
                .orElseThrow(() -> new ProductNotFoundException(
                        "Produto não encontrado com productId: " + productId));

        return mapToResponseDTO(product);
    }

    @Transactional(readOnly = true)
    public Page<ProductResponseDTO> getAllProducts(Pageable pageable) {
        Page<Product> products = productRepository.findAll(pageable);
        return products.map(this::mapToResponseDTO);
    }

    private ProductResponseDTO mapToResponseDTO(Product product) {
        return productMapper.toDto(product);
    }
}
