package com.ecommerce.order_service.mapper;

import com.ecommerce.order_service.dto.OrderLineItemsRequest;
import com.ecommerce.order_service.dto.OrderLineItemsResponse;
import com.ecommerce.order_service.dto.OrderRequest;
import com.ecommerce.order_service.dto.OrderResponse;
import com.ecommerce.order_service.model.Order;
import com.ecommerce.order_service.model.OrderLineItems;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface OrderMapper {

    // De Request a Entidad
    // Mapeamos ls lista porque los nombres no coinciden
//    @Mapping(source = "orderLineItemsDtoList", target = "orderLineItemsList")
    Order toOrder(OrderRequest orderRequest);

    // metodo aux
    OrderLineItems toOrderLineItems(OrderLineItemsRequest orderLineItemsRequest);


    // De Entidad a Response
    // Mapeamos  la lista de vuelta
//    @Mapping(source = "orderLineItemsList", target = "orderLineItemsDtoList")
    OrderResponse toOrderResponse(Order order);

    // metodo aux
    OrderLineItemsResponse toOrderLineItemsResponse(OrderLineItems orderLineItems);
}