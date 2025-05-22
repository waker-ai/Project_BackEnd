package com.example.tomatomall.util;

import com.example.tomatomall.enums.OrderStatusEnum;
import com.example.tomatomall.exception.TomatoException;
import com.example.tomatomall.po.CartItem;
import com.example.tomatomall.po.Order;
import com.example.tomatomall.po.OrderItem;
import com.example.tomatomall.po.Stockpile;
import com.example.tomatomall.repository.CartRepository;
import com.example.tomatomall.repository.OrderItemRepository;
import com.example.tomatomall.repository.OrderRepository;
import com.example.tomatomall.repository.StockpileRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import javax.transaction.Transactional;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Slf4j
@Component
@RequiredArgsConstructor
public class ScheduledOrderCleanUpUtil {

    private final OrderRepository orderRepository;
    private final OrderItemRepository orderItemRepository;
    private final StockpileRepository stockpileRepository;
    private final CartRepository cartRepository;

    @Scheduled(fixedRate =  5 * 60 * 1000) // 每5分钟执行一次
    @Transactional
    public void releaseExiredOrders() {
        Date deadline = new Date(System.currentTimeMillis() - 5* 60 * 1000 );
        List<Order> expiredOrders = orderRepository.findByStatusAndCreateTimeBefore(OrderStatusEnum.PENDING, deadline);

        if (expiredOrders.isEmpty()) {
            return;
        }

        for (Order order : expiredOrders) {
            log.info("Release expired order: {}", order.getOrderId());
            List<OrderItem> orderItems = orderItemRepository.findByOrderId(order.getOrderId());

            for (OrderItem orderItem : orderItems) {
                Long cartItemId = orderItem.getCartItemId();
                Optional<CartItem> cartItemOpt = cartRepository.findById(cartItemId);
                if (!cartItemOpt.isPresent()) {
                    log.warn("CartItem not found for orderItem: {}, cartItemId: {}", orderItem.getId(), cartItemId);
                    continue; // 跳过这个 cartItem，不影响其他处理
                }
                //得到的OrderItem中对应的cartItem
                CartItem cartItem = cartItemOpt.get();
                // 得到cartItem对应的stockpile
                Stockpile stockpile = stockpileRepository.findByProductId(cartItem.getProductId()).orElseThrow(TomatoException::stockpileNotFound);

                stockpile.setFrozen(stockpile.getFrozen() - cartItem.getQuantity());
                stockpile.setAmount(stockpile.getAmount() + cartItem.getQuantity());
                stockpileRepository.save(stockpile);
            }
            order.setStatus(OrderStatusEnum.TIMEOUT);
            orderRepository.save(order);
            log.info("Order " + order.getOrderId() + " canceled and stock released");
        }
    }
}