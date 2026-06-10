package com.ecommerce.order_service.service;

import com.ecommerce.order_service.dto.OrderRequest;
import com.ecommerce.order_service.dto.OrderResponse;
import java.util.List;

public interface OrderService {

    OrderResponse placeOrder(OrderRequest orderRequest); // Create
    List<OrderResponse> getAllOrders();                  // All
    OrderResponse getOrderById(Long id);                 // One
    void deleteOrder(Long id);                           // Delete
}