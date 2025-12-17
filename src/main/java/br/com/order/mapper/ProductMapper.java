package br.com.order.mapper;
import br.com.order.dto.request.ProductRequestDTO;
import br.com.order.dto.response.ProductResponseDTO;
import br.com.order.model.Product;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface ProductMapper {

    Product toEntity(ProductRequestDTO dto);
    ProductResponseDTO toDto(Product entity);
}
