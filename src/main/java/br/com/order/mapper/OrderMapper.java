package br.com.order.mapper;

import br.com.order.dto.request.OrderRequestDTO;
import br.com.order.dto.response.OrderResponseDTO;
import br.com.order.model.Order;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface OrderMapper {

    Order toEntity(OrderRequestDTO dto);
    OrderResponseDTO toDto(Order entity);
}
