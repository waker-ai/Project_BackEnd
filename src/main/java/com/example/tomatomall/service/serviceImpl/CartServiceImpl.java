package com.example.tomatomall.service.serviceImpl;

import com.example.tomatomall.config.LoginInterceptor;
import com.example.tomatomall.dto.CheckoutRequest;
import com.example.tomatomall.enums.OrderStatusEnum;
import com.example.tomatomall.exception.TomatoException;
import com.example.tomatomall.po.*;
import com.example.tomatomall.repository.*;
import com.example.tomatomall.service.CartService;
import com.example.tomatomall.util.SecurityUtil;
import com.example.tomatomall.util.TokenUtil;
import com.example.tomatomall.vo.CartItemVO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import javax.net.ssl.HandshakeCompletedEvent;
import javax.servlet.http.HttpSession;
import javax.transaction.Transactional;
import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

import static org.hibernate.internal.CoreLogging.logger;

@Service
public class CartServiceImpl implements CartService {

    private static final Logger logger = LoggerFactory.getLogger(CartServiceImpl.class);

    @Autowired
    private SecurityUtil securityUtil;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private StockpileRepository stockpileRepository;

    @Autowired
    private CartRepository cartRepository;

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private OrderItemRepository orderItemRepository; ;


    @Override
    public Map<String, Object> addCartItem(Long productId, Integer quantity) {
        // 检查商品是否存在
        if (!productRepository.existsById(productId)) {
            throw TomatoException.productNotFound();
        }
        //获取当前用户ID
        User user = securityUtil.getCurrentUser();
        Long userId = user.getId();

        //检查购物车中是否已经存在该商品
        CartItem cartItem = cartRepository.findByUserIdAndProductId(userId, productId).
                orElse(new CartItem(userId, productId, 0));
        //更新购物车中的商品数量
        int newQuantity = quantity + cartItem.getQuantity();
        // 检查库存是否足够
        if (stockpileRepository.findByProductId(productId).get().getAmount() < newQuantity) {
            throw TomatoException.insufficientStock();
        }
        cartItem.setQuantity(newQuantity);
        //保存购物车中的商品
        cartRepository.save(cartItem);

        Product product = productRepository.findById(productId).get();

        Map<String ,Object> result = new HashMap<>();
        result.put("cartItem", cartItem.getCartItemId());
        result.put("productId", productId);
        result.put("title", product.getTitle());
        result.put("price", product.getPrice());
        result.put("description", product.getDescription());
        result.put("cover", product.getCover());
        result.put("detail", product.getDetail());
        result.put("quantity", newQuantity);
        return result;
    }

    // 删除购物车中的商品
    @Override
    public void deleteCartItem(Long cartItemId) {
        User user = securityUtil.getCurrentUser();
        Long userId = user.getId();
        CartItem cartItem = cartRepository.findByUserIdAndCartItemId(userId, cartItemId)
                .orElseThrow(TomatoException::cartItemNotFound);
        cartRepository.delete(cartItem);
    }

    //修改购物车中的商品数量
    @Transactional
    @Override
    public void updateCartItemQuantity(Long cartItemId, Integer quantity) {
        System.out.println(quantity);
        User user = securityUtil.getCurrentUser();
        Long userId = user.getId();
        CartItem cartItem = cartRepository.findByUserIdAndCartItemId(userId, cartItemId)
                .orElseThrow(TomatoException::cartItemNotFound);

        if (quantity == 0) {
            cartRepository.delete(cartItem);
            return;
        }

        if (quantity < 0) {
            throw TomatoException.insufficientStock();
        }
        //检查库存是否足够
        Stockpile stock = stockpileRepository.findByProductId(cartItem.getProductId())
                .orElseThrow(() -> TomatoException.stockpileNotFound());
        int stockAmount = stock.getAmount();
        logger.info("库存为：" + stockAmount + "，用户请求数量为：" + quantity);
        if (stockAmount < quantity) {
            logger.warn("库存不足，即将抛出异常");
            throw TomatoException.insufficientStock();
        }
        cartItem.setQuantity(quantity);
        cartRepository.save(cartItem);
    }

    @Override
    public Map<String, Object> getAllCartItems() {
        User user = securityUtil.getCurrentUser();
//        logger.info("当前登录用户：" + securityUtil.getCurrentUser());
        List<CartItem> cartItems = cartRepository.findByUserId(user.getId());

        List<Map<String, Object>> itemList = new ArrayList<>();
        BigDecimal totalAmount = BigDecimal.ZERO;
        for (CartItem cartItem : cartItems) {
            Product product = productRepository.findById(cartItem.getProductId()).orElseThrow(TomatoException::productNotFound);
            Map<String, Object> item = new HashMap<>();
            item.put("cartItemId", cartItem.getCartItemId());
            item.put("productId", product.getId());
            item.put("title", product.getTitle());
            item.put("price", product.getPrice());
            item.put("description", product.getDescription());
            item.put("cover", product.getCover());
            item.put("detail", product.getDetail());
            item.put("quantity", cartItem.getQuantity());
            itemList.add(item);
            totalAmount = totalAmount.add(product.getPrice().multiply(BigDecimal.valueOf(cartItem.getQuantity())));
        }
        Map<String, Object> result = new HashMap<>();
        result.put("items", itemList);
        result.put("total", itemList.size());
        result.put("totalAmount", totalAmount);
        return result;
    }

    @Override
    public Order checkout(CheckoutRequest request) {
        logger.info("order request: " + request);
        if (request.getCartItemIds() == null || request.getCartItemIds().isEmpty()) {
            throw new IllegalArgumentException("cartItemIDs 不能为空");
        }
        List<CartItem> selectedItems = cartRepository.findAllById(request.getCartItemIds());

        // 获取当前用户ID
        Long userId = securityUtil.getCurrentUser().getId();
        logger.info("current user: " + userId);


//        // 查找是否存在相同用户和相同的cartItemIds的未支付的订单（即重复订单）
//        List<Order> pendingOrders = orderRepository.findByUserIdAndStatus(userId, OrderStatusEnum.PENDING);
//        for (Order pendingOrder : pendingOrders) {
//            List<OrderItem> items = orderItemRepository.findByOrderId(pendingOrder.getOrderId());
//            List<Long> existingCartItemsIds = items.stream().map(OrderItem::getCartItemId).sorted().collect(Collectors.toList());
//            List<Long> currentCartItemsIds = selectedItems.stream().map(CartItem::getCartItemId).sorted().collect(Collectors.toList());
//            if (existingCartItemsIds.equals(currentCartItemsIds)) {
//                logger.info("Found existing pending order, reusing orderId:  " + pendingOrder.getOrderId());
//                return pendingOrder;
//            }
//        }
        //创建订单
        Order order = new Order();

        // 检查库存是否足够
        for (CartItem cartItem : selectedItems) {
            Stockpile stockpile = stockpileRepository.findByProductId(cartItem.getProductId()).orElseThrow(TomatoException::productNotFound);
            if(stockpile.getAmount() < cartItem.getQuantity()){
                throw TomatoException.insufficientStock();
            }
        }

        //计算总价
        BigDecimal totalAmount = selectedItems.stream()
                .map(cartItem -> {
                    Product product = productRepository.findById(cartItem.getProductId()).orElseThrow(TomatoException::productNotFound);
                    return product.getPrice().multiply(BigDecimal.valueOf(cartItem.getQuantity()));
                })
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        order.setUserId(userId);
        order.setTotalAmount(totalAmount);
        order.setPaymentMethod(request.getPaymentMethod());
        order.setStatus(OrderStatusEnum.PENDING);
        order.setCreateTime(new Date());

        //保存订单
        orderRepository.save(order);

        // 保存订单id和cartItemId的映射关系
        for (CartItem cartItem : selectedItems) {
            OrderItem orderItem = new OrderItem();
            orderItem.setCartItemId(cartItem.getCartItemId());
            orderItem.setOrderId(order.getOrderId());
            orderItem.setProductId(cartItem.getProductId());
            orderItem.setQuantity(cartItem.getQuantity());
            orderItemRepository.save(orderItem);
        }

        //锁定库存
        for(CartItem cartItem : selectedItems){
            Stockpile stockpile = stockpileRepository.findByProductId(cartItem.getProductId()).orElseThrow(TomatoException::productNotFound);
            Integer currentAmount = Optional.ofNullable(stockpile.getAmount()).orElse(0);
            Integer currentFrozen = Optional.ofNullable(stockpile.getFrozen()).orElse(0);

            stockpile.setAmount(currentAmount - cartItem.getQuantity());
            stockpile.setFrozen(currentFrozen + cartItem.getQuantity());
            stockpileRepository.save(stockpile);
        }

        return order;
    }
}
