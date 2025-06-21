package com.example.tomatomall.service.serviceImpl;

import com.example.tomatomall.dto.CheckoutRequest;
import com.example.tomatomall.enums.OrderStatusEnum;
import com.example.tomatomall.exception.TomatoException;
import com.example.tomatomall.po.*;
import com.example.tomatomall.repository.*;
import com.example.tomatomall.service.CartService;
import com.example.tomatomall.util.SecurityUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.math.BigDecimal;
import java.util.*;

/**
 * 购物车业务逻辑实现类，负责购物车的增删改查和结算操作。
 * 涉及商品库存校验、用户认证、订单创建与库存锁定等核心功能。
 *
 * 实现了 CartService 接口。
 * 使用 Spring 的 @Service 注解注册为 Bean。
 */
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
    private OrderItemRepository orderItemRepository;

    @Autowired
    private CouponRepository couponRepository;

    /**
     * 将指定商品添加到当前用户的购物车中。
     *
     * @param productId 商品 ID
     * @param quantity  添加的商品数量
     * @return 包含购物车项详情的 Map，包括商品信息、数量等
     * @throws TomatoException 如果商品不存在或库存不足
     */
    @Override
    public Map<String, Object> addCartItem(Long productId, Integer quantity) {
        // 检查商品是否存在
        if (!productRepository.existsById(productId)) {
            throw TomatoException.productNotFound();
        }

        //获取当前用户ID
        User user = securityUtil.getCurrentUser();
        Long userId = user.getId();

        //检查购物车中是否已经存在该商品，如果不存在将其初始化为0
        CartItem cartItem = cartRepository.findByUserIdAndProductId(userId, productId).
                orElse(new CartItem(userId, productId, 0));

        //更新购物车中的商品数量
        int newQuantity = quantity + cartItem.getQuantity();

        // 检查库存是否足够
        if (stockpileRepository.findByProductId(productId).get().getAmount() < newQuantity) {
            throw TomatoException.insufficientStock();
        }

        //设置并保存购物车中的商品数量
        cartItem.setQuantity(newQuantity);
        cartRepository.save(cartItem);

        Product product = productRepository.findById(productId).orElseThrow(TomatoException::productNotFound);

        // 将添加进购物车商品的信息返回给前端
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

    /**
     * 删除当前用户购物车中的某一项商品。
     *
     * @param cartItemId 购物车项 ID
     * @throws TomatoException 如果购物车项不存在或不属于当前用户
     */
    @Override
    public void deleteCartItem(Long cartItemId) {
        User user = securityUtil.getCurrentUser();
        Long userId = user.getId();
        // 通过userId和cartItemId查找对应的购物车项
        CartItem cartItem = cartRepository.findByUserIdAndCartItemId(userId, cartItemId)
                .orElseThrow(TomatoException::cartItemNotFound);
        cartRepository.delete(cartItem);
    }

    /**
     * 修改购物车中某一项商品的数量。
     *
     * @param cartItemId 购物车项 ID
     * @param quantity   修改后的商品数量（0 表示删除该项）
     * @throws TomatoException 如果购物车项不存在、库存不足或数量非法
     */
    @Transactional
    @Override
    public void updateCartItemQuantity(Long cartItemId, Integer quantity) {
        User user = securityUtil.getCurrentUser();
        Long userId = user.getId();

        // 通过userId和cartItemId查找对应的购物车项
        CartItem cartItem = cartRepository.findByUserIdAndCartItemId(userId, cartItemId)
                .orElseThrow(TomatoException::cartItemNotFound);

        // 更新购物车中相关商品的数量
        if (quantity == 0) {
            cartRepository.delete(cartItem);
            return;
        }

        if (quantity < 0) {
            throw TomatoException.insufficientStock();
        }

        // 检查库存是否足够
        Stockpile stock = stockpileRepository.findByProductId(cartItem.getProductId())
                .orElseThrow(TomatoException::stockpileNotFound);
        int stockAmount = stock.getAmount();
        // 库存不足抛出异常
        if (stockAmount < quantity) {
            throw TomatoException.insufficientStock();
        }
        // 设置并保存更新过后的数量
        cartItem.setQuantity(quantity);
        cartRepository.save(cartItem);
    }

    /**
     * 获取当前用户所有的购物车商品项。
     *
     * @return 包含购物车商品列表、商品总数、总金额的 Map
     * @throws TomatoException 如果商品信息无法获取
     */
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
        // 返回购物车商品信息列表和总数量以及总价
        Map<String, Object> result = new HashMap<>();
        result.put("items", itemList);
        result.put("total", itemList.size());
        result.put("totalAmount", totalAmount);
        return result;
    }

    /**
     * 进行购物车结算，创建订单，扣减库存并锁定库存。
     *
     * @param request 结算请求对象，包含所选购物车项 ID、地址、支付方式等
     * @return 创建成功的订单对象
     * @throws IllegalArgumentException 如果请求的购物车项为空
     * @throws TomatoException 如果商品/库存/优惠券不存在或库存不足
     */
    @Override
    public Order checkout(CheckoutRequest request) {
//        logger.info("order request: " + request);
        if (request.getCartItemIds() == null || request.getCartItemIds().isEmpty()) {
            throw new IllegalArgumentException("cartItemIDs 不能为空");
        }
        List<CartItem> selectedItems = cartRepository.findAllById(request.getCartItemIds());

        // 获取当前用户ID
        Long userId = securityUtil.getCurrentUser().getId();
        logger.info("current user: " + userId);

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
        Long couponId = request.getSelectedCouponId();
        if(couponId != null) {
            // 检查优惠券是否存在
            Coupon coupon = couponRepository.findById(request.getSelectedCouponId())
                    .orElseThrow(TomatoException::couponNotFound);
            totalAmount = totalAmount.subtract(coupon.getDiscountAmount());
            // 防止金额为负数
            totalAmount = totalAmount.max(BigDecimal.ZERO);
        }


        order.setUserId(userId);
        order.setTotalAmount(totalAmount);
        order.setPaymentMethod(request.getPaymentMethod());
        order.setStatus(OrderStatusEnum.PENDING);
        order.setCreateTime(new Date());
        order.setShippingAddressId(request.getShippingAddressId());

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

        // 锁定库存
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
