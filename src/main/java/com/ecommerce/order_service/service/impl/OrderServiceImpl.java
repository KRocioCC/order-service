package com.ecommerce.order_service.service.impl;

import com.ecommerce.order_service.config.WebClientConfig;
import com.ecommerce.order_service.dto.OrderRequest;
import com.ecommerce.order_service.dto.OrderResponse;
import com.ecommerce.order_service.exception.ResourceNotFoundException;
import com.ecommerce.order_service.mapper.OrderMapper;
import com.ecommerce.order_service.model.Order;
import com.ecommerce.order_service.model.OrderLineItems;
import com.ecommerce.order_service.repository.OrderRepository;
import com.ecommerce.order_service.service.OrderService;
import com.ecommerce.order_service.service.client.InventoryClient;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.Collections;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
@RefreshScope
public class OrderServiceImpl implements OrderService {

    private final OrderRepository orderRepository;
    private final OrderMapper orderMapper;
//    private final WebClient.Builder webClientBuilder;
    private final InventoryClient inventoryClient;

    @Value("${order.enabled:true}")
    private boolean ordersEnabled;

    //metodo fallback que se ejecuta cuando el circuito esta abierto, cuando el servicio de inventario no esta disponible
    public OrderResponse fallbackMethod(OrderRequest orderRequest, String userId, Throwable throwable) {
        log.error("CIRCUIT BREAKER ACTIVADO: Fallo al colocar la orden. Causa: {}", throwable.getMessage());
        throw new RuntimeException("El Servicio de Inventario no responde. Por favor intente más tarde.");
    }

    @Override
    @Transactional
    //inventory es el nombre del circuito en configuracion cd, fallbackMethod es el metodo que se ejecuta si el circuito esta abierto
    @CircuitBreaker(name = "inventory", fallbackMethod = "fallbackMethod")
    @Retry(name = "inventory")
    public OrderResponse placeOrder(OrderRequest orderRequest, String userId) {

        if(!ordersEnabled){
            log.warn("Pedido rechazado, Servicio deshabilitado por configuracion.");
            throw new RuntimeException("El servicio de ordenes está actualmente deshabilitado. Por favor, intentelo más tarde.");
        }

        log.info("Colocando nueva orden...");
        Order order = orderMapper.toOrder(orderRequest);

        order.setUserId(userId);


        for (var item : order.getOrderLineItemsList()) {
            String sku = item.getSku();
            Integer quantity = item.getQuantity();

            log.info(
                    "Solicitando reducción de stock. sku={}, quantity={}",
                    sku,
                    quantity
            );

            try {
                inventoryClient.reduceStock(sku, quantity);

            } catch (Exception e) {
                log.error(
                        "Error al reducir stock. sku={}, quantity={}",
                        sku,
                        quantity,
                        e
                );

                throw new IllegalArgumentException(
                        "No se pudo procesar la orden para el producto: " + sku,
                        e
                );
            }
        }
        order.setOrderNumber(UUID.randomUUID().toString());

        // Guardamos
        Order savedOrder = orderRepository.save(order);
        log.info("Orden guardada con éxito. ID: {}", savedOrder.getId());

        return orderMapper.toOrderResponse(savedOrder);
    }

    //Mtodo para obtener todas las ordenes de un usuario o todas las ordenes si es ADMIN
    @Override
    @Transactional(readOnly = true)
    public List<OrderResponse> getOrders(String userId, boolean isAdmin) {
        List<Order> orders;
        if (isAdmin) {
            orders = orderRepository.findAll();
        } else {
            orders = orderRepository.findByUserId(userId);
        }
        return orders.stream()
                .map(orderMapper::toOrderResponse)
                .toList();
    }

    //@Override
    //@Transactional(readOnly = true)
    //public List<OrderResponse> getAllOrders() {
    //    return orderRepository.findAll().stream()
    //           .map(orderMapper::toOrderResponse)
    //            .toList();
    //}

    @Override
    @Transactional(readOnly = true)
    public OrderResponse getOrderById(Long id) {
        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Orden", "id", id));
        return orderMapper.toOrderResponse(order);
    }

    @Override
    @Transactional
    public void deleteOrder(Long id) {
        if (!orderRepository.existsById(id)) {
            throw new ResourceNotFoundException("Orden", "id", id);
        }
        orderRepository.deleteById(id);
        log.info("Orden eliminada. ID: {}", id);
    }
}